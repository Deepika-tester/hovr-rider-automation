package com.automation.reporting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Standalone report generator — NOT a step definition or hook, just a plain
 * `main()` run manually after a test pass:
 *
 *   mvn -q test-compile exec:java@generate-report
 *
 * Reads whichever of these exist (produced by TestRunner / ApiTests's
 * "json:" Cucumber plugin — see their @CucumberOptions):
 *   target/cucumber-reports/cucumber.json          (rider mobile suite)
 *   target/cucumber-reports/api/cucumber.json      (backend API suite)
 *
 * and writes:
 *   target/test-report/index.html
 *   target/test-report/report.pdf
 *   target/test-report/chart.png                   (shared by both — see class javadoc below)
 *
 * Deliberately reads the already-produced cucumber.json rather than hooking into
 * the test run itself, so report generation can't ever affect (or be affected by)
 * scenario execution, and can be re-run against an old result without re-testing.
 */
public class TestReportGenerator {

    private static final String[][] SUITES = {
            {"Rider (mobile)", "target/cucumber-reports/cucumber.json"},
            {"Backend API", "target/cucumber-reports/api/cucumber.json"}
    };

    public static void main(String[] args) throws Exception {
        Path outDir = Path.of("target/test-report");
        Files.createDirectories(outDir);

        List<ScenarioResult> results = new ArrayList<>();
        for (String[] suite : SUITES) {
            File jsonFile = new File(suite[1]);
            if (jsonFile.exists()) {
                results.addAll(parseCucumberJson(suite[0], jsonFile));
            } else {
                System.out.println("Skipping " + suite[0] + " — no report at " + suite[1] + " (suite not run yet)");
            }
        }

        if (results.isEmpty()) {
            System.out.println("No cucumber.json files found under target/cucumber-reports/. Run the tests first.");
            return;
        }

        Summary summary = Summary.of(results);

        File chartFile = outDir.resolve("chart.png").toFile();
        writePieChart(summary, chartFile);

        String html = buildHtml(results, summary, "chart.png");
        Path htmlPath = outDir.resolve("index.html");
        Files.writeString(htmlPath, html);
        System.out.println("Wrote " + htmlPath.toAbsolutePath());

        // Same HTML, rendered to PDF — base URL lets flying-saucer resolve the relative
        // chart.png reference and any relative file: links against outDir.
        File pdfFile = outDir.resolve("report.pdf").toFile();
        renderPdf(html, outDir.toUri().toString(), pdfFile);
        System.out.println("Wrote " + pdfFile.getAbsolutePath());
    }

    // ---------------------------------------------------------------
    // Parsing
    // ---------------------------------------------------------------

    private record ScenarioResult(String suite, String featureName, String scenarioName,
                                   List<String> tags, String status, String failureMessage) {
    }

    private static List<ScenarioResult> parseCucumberJson(String suiteName, File jsonFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonFile);
        List<ScenarioResult> results = new ArrayList<>();

        for (JsonNode feature : root) {
            String featureName = feature.path("name").asText("(unnamed feature)");
            for (JsonNode element : feature.path("elements")) {
                if (!"scenario".equals(element.path("type").asText())) {
                    continue; // skip Background blocks, which have no result of their own
                }
                String scenarioName = element.path("name").asText("(unnamed scenario)");

                List<String> tags = new ArrayList<>();
                for (JsonNode tag : element.path("tags")) {
                    tags.add(tag.path("name").asText());
                }

                String worst = "passed";
                String failureMessage = null;
                for (JsonNode step : element.path("steps")) {
                    String status = step.path("result").path("status").asText("undefined");
                    if (rank(status) > rank(worst)) {
                        worst = status;
                    }
                    if (("failed".equals(status) || "undefined".equals(status) || "pending".equals(status))
                            && failureMessage == null) {
                        String errorMsg = step.path("result").path("error_message").asText(null);
                        failureMessage = errorMsg != null
                                ? errorMsg.lines().findFirst().orElse(errorMsg)
                                : ("Step " + status + ": " + step.path("name").asText(""));
                    }
                }

                results.add(new ScenarioResult(suiteName, featureName, scenarioName, tags, worst, failureMessage));
            }
        }
        return results;
    }

    // Higher rank "wins" as the scenario's overall status — a single failed/undefined step
    // fails the whole scenario even if every other step passed.
    private static int rank(String status) {
        return switch (status) {
            case "passed" -> 0;
            case "skipped" -> 1;
            case "pending" -> 2;
            case "undefined" -> 3;
            case "ambiguous" -> 4;
            case "failed" -> 5;
            default -> 3;
        };
    }

    // ---------------------------------------------------------------
    // Aggregation
    // ---------------------------------------------------------------

    private record Summary(int total, int passed, int failed, int skipped, int other) {
        static Summary of(List<ScenarioResult> results) {
            int passed = 0, failed = 0, skipped = 0, other = 0;
            for (ScenarioResult r : results) {
                switch (r.status()) {
                    case "passed" -> passed++;
                    case "failed", "ambiguous" -> failed++;
                    case "skipped" -> skipped++;
                    default -> other++; // undefined/pending — not verified, distinct from a hard failure
                }
            }
            return new Summary(results.size(), passed, failed, skipped, other);
        }

        double passRate() {
            return total == 0 ? 0 : (100.0 * passed / total);
        }
    }

    // ---------------------------------------------------------------
    // Chart — rendered once as PNG and shared by the HTML <img> and the PDF <img>,
    // rather than relying on inline SVG (unreliable in HTML-to-PDF renderers).
    // ---------------------------------------------------------------

    private static final Color COLOR_PASSED = new Color(0x2E, 0xA0, 0x4B);
    private static final Color COLOR_FAILED = new Color(0xD9, 0x3B, 0x3B);
    private static final Color COLOR_SKIPPED = new Color(0xC9, 0xA2, 0x27);
    private static final Color COLOR_OTHER = new Color(0x8A, 0x8A, 0x8A);

    private static void writePieChart(Summary s, File outFile) throws Exception {
        int size = 480;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Map<String, Integer> slices = new LinkedHashMap<>();
        slices.put("Passed", s.passed());
        slices.put("Failed", s.failed());
        slices.put("Skipped", s.skipped());
        slices.put("Other", s.other());
        Map<String, Color> colors = Map.of(
                "Passed", COLOR_PASSED, "Failed", COLOR_FAILED,
                "Skipped", COLOR_SKIPPED, "Other", COLOR_OTHER);

        int margin = 20;
        Arc2D.Double oval = new Arc2D.Double(margin, margin, size - 2.0 * margin, size - 2.0 * margin, 0, 0, Arc2D.PIE);
        double startAngle = 90; // 12 o'clock, sweeping clockwise
        int total = Math.max(s.total(), 1);
        for (Map.Entry<String, Integer> slice : slices.entrySet()) {
            if (slice.getValue() == 0) continue;
            double sweep = -360.0 * slice.getValue() / total; // negative = clockwise in Java2D
            oval.setAngleStart(startAngle);
            oval.setAngleExtent(sweep);
            g.setColor(colors.get(slice.getKey()));
            g.fill(oval);
            startAngle += sweep;
        }

        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(3));
        g.draw(new java.awt.geom.Ellipse2D.Double(margin, margin, size - 2.0 * margin, size - 2.0 * margin));

        g.dispose();
        ImageIO.write(image, "png", outFile);
    }

    // ---------------------------------------------------------------
    // HTML (kept as strict, well-formed XHTML — self-closing tags, quoted attributes — so the
    // exact same string parses cleanly for both the browser and flying-saucer's XML parser)
    // ---------------------------------------------------------------

    private static String buildHtml(List<ScenarioResult> results, Summary s, String chartFileName) {
        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        StringBuilder rows = new StringBuilder();
        for (ScenarioResult r : results) {
            String badgeColor = switch (r.status()) {
                case "passed" -> "#2EA04B";
                case "failed", "ambiguous" -> "#D93B3B";
                case "skipped" -> "#C9A227";
                default -> "#8A8A8A";
            };
            rows.append("<tr>")
                    .append("<td>").append(escape(r.suite())).append("</td>")
                    .append("<td>").append(escape(r.featureName())).append("</td>")
                    .append("<td>").append(escape(r.scenarioName())).append("</td>")
                    .append("<td><span style=\"color: #FFFFFF; background-color: ").append(badgeColor)
                    .append("; padding: 2px 8px; border-radius: 4px; font-size: 12px;\">")
                    .append(escape(r.status().toUpperCase())).append("</span></td>")
                    .append("<td>").append(r.failureMessage() != null ? escape(truncate(r.failureMessage(), 140)) : "")
                    .append("</td>")
                    .append("</tr>\n");
        }

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                + "<head>\n"
                + "<title>HOVR Rider Automation — Test Report</title>\n"
                + "<style type=\"text/css\">\n"
                + bodyCss()
                + "</style>\n"
                + "</head>\n"
                + "<body>\n"
                + "<h1>HOVR Rider Automation — Test Report</h1>\n"
                + "<p class=\"muted\">Generated " + escape(generatedAt) + "</p>\n"
                + "<table class=\"summary\">\n"
                + "<tr><td class=\"chart-cell\"><img src=\"" + escape(chartFileName) + "\" width=\"260\" height=\"260\" alt=\"Pass/fail chart\" /></td>\n"
                + "<td class=\"stats-cell\">\n"
                + statRow("Total scenarios", String.valueOf(s.total()), null)
                + statRow("Passed", s.passed() + " (" + String.format("%.1f", s.passRate()) + "%)", "#2EA04B")
                + statRow("Failed", String.valueOf(s.failed()), "#D93B3B")
                + statRow("Skipped", String.valueOf(s.skipped()), "#C9A227")
                + statRow("Undefined / pending", String.valueOf(s.other()), "#8A8A8A")
                + "</td></tr>\n"
                + "</table>\n"
                + "<h2>Scenarios</h2>\n"
                + "<table class=\"details\">\n"
                + "<tr><th>Suite</th><th>Feature</th><th>Scenario</th><th>Status</th><th>Failure (first line)</th></tr>\n"
                + rows
                + "</table>\n"
                + "</body>\n"
                + "</html>\n";
    }

    private static String statRow(String label, String value, String color) {
        String style = color != null ? " style=\"color: " + color + "; font-weight: bold;\"" : "";
        return "<div class=\"stat-row\"><span class=\"stat-label\">" + escape(label) + ":</span> "
                + "<span class=\"stat-value\"" + style + ">" + escape(value) + "</span></div>\n";
    }

    private static String bodyCss() {
        return "body { font-family: Helvetica, Arial, sans-serif; color: #222222; margin: 24px; }\n"
                + "h1 { font-size: 22px; margin-bottom: 4px; }\n"
                + "h2 { font-size: 17px; margin-top: 28px; }\n"
                + ".muted { color: #777777; font-size: 12px; margin-top: 0; }\n"
                + "table.summary { border-collapse: collapse; margin-top: 12px; }\n"
                + "table.summary td { vertical-align: middle; padding: 8px 16px; }\n"
                + ".stat-row { font-size: 14px; margin: 6px 0; }\n"
                + ".stat-label { color: #555555; }\n"
                + "table.details { border-collapse: collapse; width: 100%; margin-top: 10px; }\n"
                + "table.details th { text-align: left; background-color: #F0F0F0; padding: 6px 8px; font-size: 12px; border-bottom: 1px solid #CCCCCC; }\n"
                + "table.details td { padding: 6px 8px; font-size: 12px; border-bottom: 1px solid #EEEEEE; }\n";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    // ---------------------------------------------------------------
    // PDF
    // ---------------------------------------------------------------

    private static void renderPdf(String xhtml, String baseUrl, File outFile) throws Exception {
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(xhtml, baseUrl);
        renderer.layout();
        try (FileOutputStream os = new FileOutputStream(outFile)) {
            renderer.createPDF(os);
        }
    }
}
