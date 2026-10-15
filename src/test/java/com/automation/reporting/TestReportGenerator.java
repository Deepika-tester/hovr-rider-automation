package com.automation.reporting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Standalone report generator (a plain main(), not a hook): run it after the test passes.
 *
 *   mvn -q test-compile exec:java@generate-report
 *
 * Inputs (every *.json found, the newest run of a scenario wins, so re-running one feature replaces its old result):
 *   target/cucumber-reports/rider/*.json   rider app UI runs (Cucumber JSON, one file per run)
 *   target/cucumber-reports/cucumber.json  the default TestRunner output, also read
 *   target/cucumber-reports/api/*.json     ConnectRPC API runs
 *   target/cucumber-reports/driver/*.json  driver app runs
 * plus the feature files themselves, to list the scenarios that were deliberately NOT run (@blocked + "# BLOCKED:").
 *
 * Outputs: target/test-report/index.html, report.pdf, chart.png.
 *
 * Status vocabulary:
 *   PASSED   the app did what the feature file says
 *   FAILED   the app behaved differently from the feature file (the message shows expected vs actual) -- usually a spec
 *            or app finding, not a broken test; the tester decides which side to correct
 *   N/A      the scenario does not apply on this device (e.g. an iOS-only example on Android)
 *   BLOCKED  not executed; the reason is stated (needs a driver, would create a real ride, fault injection, ...)
 */
public class TestReportGenerator {

    // -Dreport.market=canada reads target/cucumber-reports/canada/... and writes target/test-report-canada/
    private static final String MARKET = System.getProperty("report.market", "india");
    private static final boolean INDIA = !"canada".equalsIgnoreCase(MARKET);
    private static final String BASE = INDIA ? "target/cucumber-reports" : "target/cucumber-reports/" + MARKET;

    private static final String[][] SUITES = {
            {"Rider app (UI)", BASE + "/rider", INDIA ? "target/cucumber-reports/cucumber.json" : null, "src/test/resources/features/rider"},
            {"Rider API", BASE + "/api", null, "src/test/resources/features/api"},
            {"Driver app (UI)", BASE + "/driver", null, "src/test/resources/features/driver"},
            {"Driver API", BASE + "/driver-api", null, "src/test/resources/features/driver-api"}
    };

    public static void main(String[] args) throws Exception {
        Path outDir = Path.of(INDIA ? "target/test-report" : "target/test-report-" + MARKET);
        Files.createDirectories(outDir);

        List<Row> rows = new ArrayList<>();
        for (String[] suite : SUITES) {
            List<Row> executed = collectExecuted(suite[0], suite[1], suite[2]);
            List<Row> blocked = collectBlocked(suite[0], suite[3], rows);
            // A scenario that is blocked NOW is reported as blocked even if an older run executed it.
            java.util.Set<String> blockedKeys = new java.util.HashSet<>();
            for (Row b : blocked) {
                blockedKeys.add(b.file() + "|" + b.scenario());
            }
            executed.removeIf(r -> blockedKeys.contains(r.file() + "|" + r.scenario()));
            rows.addAll(executed);
            rows.addAll(blocked);
        }
        if (rows.isEmpty()) {
            System.out.println("Nothing to report: no cucumber JSON under target/cucumber-reports/ and no feature files.");
            return;
        }

        Summary overall = Summary.of(rows);
        File chart = outDir.resolve("chart.png").toFile();
        writePieChart(overall, chart);

        String html = buildHtml(rows, overall);
        Path htmlPath = outDir.resolve("index.html");
        Files.writeString(htmlPath, html);
        System.out.println("Wrote " + htmlPath.toAbsolutePath());

        File pdf = outDir.resolve("report.pdf").toFile();
        renderPdf(html, outDir.toUri().toString(), pdf);
        System.out.println("Wrote " + pdf.getAbsolutePath());
    }

    // ---------------------------------------------------------------
    // Model
    // ---------------------------------------------------------------

    private record Row(String suite, String file, String feature, String scenario, int line, String status, String detail) {
    }

    // ---------------------------------------------------------------
    // Executed scenarios (Cucumber JSON)
    // ---------------------------------------------------------------

    private static List<File> jsonFiles(String dir, String extra) throws Exception {
        List<File> files = new ArrayList<>();
        Path d = Path.of(dir);
        if (Files.isDirectory(d)) {
            try (Stream<Path> s = Files.list(d)) {
                s.filter(p -> p.toString().endsWith(".json")).forEach(p -> files.add(p.toFile()));
            }
        }
        if (extra != null && new File(extra).exists()) {
            files.add(new File(extra));
        }
        files.sort(Comparator.comparingLong(File::lastModified)); // oldest first, so newer runs overwrite older ones
        return files;
    }

    private static List<Row> collectExecuted(String suite, String dir, String extra) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Row> latest = new LinkedHashMap<>();
        for (File f : jsonFiles(dir, extra)) {
            JsonNode root;
            try {
                root = mapper.readTree(f);
            } catch (Exception e) {
                continue; // a run that was cut off leaves an unreadable file
            }
            Map<String, Integer> seenInThisRun = new LinkedHashMap<>();
            for (JsonNode feature : root) {
                String uri = feature.path("uri").asText("");
                String file = uri.contains("/") ? uri.substring(uri.lastIndexOf('/') + 1) : uri;
                String featureName = feature.path("name").asText("(unnamed feature)");
                for (JsonNode el : feature.path("elements")) {
                    if (!"scenario".equals(el.path("type").asText())) {
                        continue;
                    }
                    int line = el.path("line").asInt();
                    String name = el.path("name").asText("(unnamed scenario)");
                    // Feature files get edited between runs (tags, comments), which moves line numbers, so a scenario is
                    // identified by name plus its position among same-named ones (the rows of a Scenario Outline).
                    int ordinal = seenInThisRun.merge(file + "|" + name, 1, Integer::sum);
                    String worst = "passed";
                    String detail = null;
                    for (JsonNode step : el.path("steps")) {
                        String st = step.path("result").path("status").asText("undefined");
                        if (rank(st) > rank(worst)) {
                            worst = st;
                        }
                        if (detail == null && !"passed".equals(st) && !"skipped".equals(st)) {
                            String msg = step.path("result").path("error_message").asText(null);
                            detail = step.path("name").asText("") + " -> " + (msg != null ? firstLine(msg) : st);
                        }
                    }
                    String status = switch (worst) {
                        case "passed" -> "PASSED";
                        case "pending" -> "N/A";
                        case "skipped" -> "FAILED"; // a hook failed before the first step ran
                        default -> "FAILED";
                    };
                    if ("FAILED".equals(status) && detail == null) {
                        detail = "the scenario's setup failed before any step ran (environment)";
                    }
                    if ("N/A".equals(status) && detail != null) {
                        detail = detail.replaceAll("^.*-> ", "");
                    }
                    latest.put(suite + "|" + file + "|" + name + "|" + ordinal, new Row(suite, file, featureName, name, line, status, detail));
                }
            }
        }
        return new ArrayList<>(latest.values());
    }

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

    private static String firstLine(String s) {
        String l = s.lines().findFirst().orElse(s);
        return l.replaceFirst("^(java\\.lang\\.AssertionError|org\\.openqa\\.selenium\\.[A-Za-z]+Exception|java\\.lang\\.[A-Za-z]+): ?", "");
    }

    // ---------------------------------------------------------------
    // Blocked scenarios (feature files)
    // ---------------------------------------------------------------

    private static List<Row> collectBlocked(String suite, String featuresDir, List<Row> alreadyRun) throws Exception {
        List<Row> out = new ArrayList<>();
        Path root = Path.of(featuresDir);
        if (!Files.isDirectory(root)) {
            return out;
        }
        Pattern scenario = Pattern.compile("^\\s*Scenario(?: Outline)?:\\s*(.*)$");
        Pattern feature = Pattern.compile("^\\s*Feature:\\s*(.*)$");
        List<Path> files;
        try (Stream<Path> s = Files.walk(root)) {
            files = s.filter(p -> p.toString().endsWith(".feature")).sorted().toList();
        }
        for (Path p : files) {
            List<String> lines = Files.readAllLines(p);
            String featureName = p.getFileName().toString();
            String reason = null;
            boolean blocked = false;
            for (int i = 0; i < lines.size(); i++) {
                String l = lines.get(i);
                Matcher f = feature.matcher(l);
                if (f.matches()) {
                    featureName = f.group(1).trim();
                }
                if (l.trim().startsWith("# BLOCKED:")) {
                    reason = l.trim().substring("# BLOCKED:".length()).trim();
                }
                if (INDIA && l.trim().startsWith("# INDIA-ONLY-SKIP:")) {
                    reason = l.trim().substring("# INDIA-ONLY-SKIP:".length()).trim();
                }
                if (l.trim().startsWith("@") && (l.contains("@blocked") || (INDIA && l.contains("@india-only-skip")))) {
                    blocked = true;
                }
                Matcher m = scenario.matcher(l);
                if (m.matches()) {
                    if (blocked) {
                        out.add(new Row(suite, p.getFileName().toString(), featureName, m.group(1).trim(), i + 1, "BLOCKED",
                                reason == null ? "not run" : reason));
                    }
                    blocked = false;
                    reason = null;
                }
            }
        }
        return out;
    }

    // ---------------------------------------------------------------
    // Aggregation
    // ---------------------------------------------------------------

    private record Summary(int total, int passed, int failed, int blocked, int na) {
        static Summary of(List<Row> rows) {
            int p = 0, f = 0, b = 0, n = 0;
            for (Row r : rows) {
                switch (r.status()) {
                    case "PASSED" -> p++;
                    case "FAILED" -> f++;
                    case "BLOCKED" -> b++;
                    default -> n++;
                }
            }
            return new Summary(rows.size(), p, f, b, n);
        }

        int executed() {
            return passed + failed;
        }

        double passRate() {
            return executed() == 0 ? 0 : 100.0 * passed / executed();
        }
    }

    // ---------------------------------------------------------------
    // Chart
    // ---------------------------------------------------------------

    private static final Color C_PASSED = new Color(0x2E, 0xA0, 0x4B);
    private static final Color C_FAILED = new Color(0xD9, 0x3B, 0x3B);
    private static final Color C_BLOCKED = new Color(0xC9, 0xA2, 0x27);
    private static final Color C_NA = new Color(0x8A, 0x8A, 0x8A);

    private static void writePieChart(Summary s, File out) throws Exception {
        int size = 480;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Object[][] slices = {{s.passed(), C_PASSED}, {s.failed(), C_FAILED}, {s.blocked(), C_BLOCKED}, {s.na(), C_NA}};
        int margin = 20;
        Arc2D.Double oval = new Arc2D.Double(margin, margin, size - 2.0 * margin, size - 2.0 * margin, 0, 0, Arc2D.PIE);
        double start = 90;
        int total = Math.max(s.total(), 1);
        for (Object[] sl : slices) {
            int v = (Integer) sl[0];
            if (v == 0) {
                continue;
            }
            double sweep = -360.0 * v / total;
            oval.setAngleStart(start);
            oval.setAngleExtent(sweep);
            g.setColor((Color) sl[1]);
            g.fill(oval);
            start += sweep;
        }
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(3));
        g.draw(new java.awt.geom.Ellipse2D.Double(margin, margin, size - 2.0 * margin, size - 2.0 * margin));
        g.dispose();
        ImageIO.write(img, "png", out);
    }

    // ---------------------------------------------------------------
    // HTML (strict XHTML so the same string renders in a browser and in flying-saucer)
    // ---------------------------------------------------------------

    private static String badge(String status) {
        String color = switch (status) {
            case "PASSED" -> "#2EA04B";
            case "FAILED" -> "#D93B3B";
            case "BLOCKED" -> "#C9A227";
            default -> "#8A8A8A";
        };
        return "<span style=\"color:#FFFFFF;background-color:" + color + ";padding:2px 7px;border-radius:4px;font-size:11px;\">"
                + status + "</span>";
    }

    private static String buildHtml(List<Row> rows, Summary all) {
        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        StringBuilder b = new StringBuilder();
        b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n")
                .append("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>HOVR Test Report</title>\n<style type=\"text/css\">\n")
                .append(css()).append("</style></head><body>\n");
        b.append("<h1>HOVR rider and driver apps: test report (" + (INDIA ? "India" : "Canada") + " marketplace)</h1>\n");
        b.append("<p class=\"muted\">Generated ").append(escape(generatedAt)).append(" &#183; Android emulators (API 36): rider HOVR_Test, driver HOVR_Driver &#183; rider dev build 6.3.0+588, driver dev build 6.2.4+592 &#183; GPS " + (INDIA ? "Mohali, India" : "Toronto, Canada (GTA)") + " &#183; dev backend (iam / ca.ride / ca.notification .api.dev.ridehovr.com)</p>\n");

        b.append("<table class=\"summary\"><tr><td><img src=\"chart.png\" width=\"220\" height=\"220\" alt=\"chart\"/></td><td>\n");
        b.append(stat("Scenarios in the feature files", String.valueOf(all.total()), null));
        b.append(stat("Executed", String.valueOf(all.executed()), null));
        b.append(stat("Passed", all.passed() + " (" + String.format("%.0f", all.passRate()) + "% of executed)", "#2EA04B"));
        b.append(stat("Failed (app differs from the feature file)", String.valueOf(all.failed()), "#D93B3B"));
        b.append(stat("Blocked (not run, reason given)", String.valueOf(all.blocked()), "#C9A227"));
        b.append(stat("Not applicable on Android", String.valueOf(all.na()), "#8A8A8A"));
        b.append("</td></tr></table>\n");

        b.append("<h2>How to read this</h2>\n<p>Each scenario in the supplied feature files was run against the real app or backend. ")
                .append("<b>PASSED</b>: the app does what the feature file says. <b>FAILED</b>: the app does something different; the message shows what was expected and what was found. ")
                .append("Most failures are differences between the written specification and the build (wording, behaviour, status codes) rather than broken tests, so each needs a decision: fix the app or correct the feature file. ")
                .append("<b>BLOCKED</b>: deliberately not run, with the reason (needs an approved driver, would create a real ride request or booking on the shared dev backend, needs fault injection, or is irreversible).</p>\n");

        // curated findings (docs/test-findings.txt: "area | finding | where")
        Path findings = Path.of("docs/test-findings.txt");
        if (Files.exists(findings)) {
            try {
                b.append("<h2>Main differences found between the feature files and the build</h2>\n")
                        .append("<table class=\"details\"><tr><th style=\"width:13%\">Area</th><th>Finding</th><th style=\"width:20%\">Where</th></tr>\n");
                for (String line : Files.readAllLines(findings)) {
                    if (line.isBlank() || line.startsWith("#")) {
                        continue;
                    }
                    String[] p = line.split("\\s\\|\\s", 3);
                    b.append("<tr><td>").append(escape(p[0])).append("</td><td>").append(escape(p.length > 1 ? p[1] : ""))
                            .append("</td><td>").append(escape(p.length > 2 ? p[2] : "")).append("</td></tr>\n");
                }
                b.append("</table>\n");
            } catch (java.io.IOException e) {
                // the table is optional
            }
        }

        // per suite
        Map<String, List<Row>> bySuite = new LinkedHashMap<>();
        for (Row r : rows) {
            bySuite.computeIfAbsent(r.suite(), k -> new ArrayList<>()).add(r);
        }
        b.append("<h2>Results by suite</h2>\n<table class=\"details\"><tr><th>Suite</th><th>Scenarios</th><th>Passed</th><th>Failed</th><th>Blocked</th><th>N/A</th></tr>\n");
        for (Map.Entry<String, List<Row>> e : bySuite.entrySet()) {
            Summary s = Summary.of(e.getValue());
            b.append("<tr><td>").append(escape(e.getKey())).append("</td><td>").append(s.total()).append("</td><td>").append(s.passed())
                    .append("</td><td>").append(s.failed()).append("</td><td>").append(s.blocked()).append("</td><td>").append(s.na()).append("</td></tr>\n");
        }
        b.append("</table>\n");

        for (Map.Entry<String, List<Row>> e : bySuite.entrySet()) {
            b.append("<h2>").append(escape(e.getKey())).append("</h2>\n");
            Map<String, List<Row>> byFile = new LinkedHashMap<>();
            e.getValue().stream().sorted(Comparator.comparing(Row::file).thenComparingInt(Row::line))
                    .forEach(r -> byFile.computeIfAbsent(r.file(), k -> new ArrayList<>()).add(r));
            for (Map.Entry<String, List<Row>> f : byFile.entrySet()) {
                Summary s = Summary.of(f.getValue());
                b.append("<h3>").append(escape(f.getKey())).append(" <span class=\"muted\">&#8212; ")
                        .append(s.passed()).append(" passed, ").append(s.failed()).append(" failed, ").append(s.blocked()).append(" blocked")
                        .append(s.na() > 0 ? ", " + s.na() + " n/a" : "").append("</span></h3>\n");
                b.append("<table class=\"details\"><tr><th style=\"width:30%\">Scenario</th><th style=\"width:9%\">Status</th><th>What happened</th></tr>\n");
                for (Row r : f.getValue()) {
                    b.append("<tr><td>").append(escape(r.scenario())).append(" <span class=\"muted\">(line ").append(r.line()).append(")</span></td><td>")
                            .append(badge(r.status())).append("</td><td>").append(escape(truncate(r.detail(), 420))).append("</td></tr>\n");
                }
                b.append("</table>\n");
            }
        }
        b.append("</body></html>\n");
        return b.toString();
    }

    private static String stat(String label, String value, String color) {
        String style = color != null ? " style=\"color:" + color + ";font-weight:bold;\"" : "";
        return "<div class=\"stat\"><span class=\"muted\">" + escape(label) + ":</span> <span" + style + ">" + escape(value) + "</span></div>\n";
    }

    private static String css() {
        return "body{font-family:Helvetica,Arial,sans-serif;color:#222;margin:22px;font-size:12px;}\n"
                + "h1{font-size:21px;margin-bottom:2px;}h2{font-size:16px;margin-top:26px;border-bottom:1px solid #DDD;padding-bottom:3px;}"
                + "h3{font-size:13px;margin:16px 0 4px 0;}\n"
                + ".muted{color:#777;font-size:11px;}\n"
                + "table.summary td{vertical-align:middle;padding:6px 16px;}.stat{font-size:13px;margin:5px 0;}\n"
                + "table.details{border-collapse:collapse;width:100%;margin-top:4px;}\n"
                + "table.details th{text-align:left;background-color:#F0F0F0;padding:5px 7px;font-size:11px;border-bottom:1px solid #CCC;}\n"
                + "table.details td{padding:5px 7px;font-size:11px;border-bottom:1px solid #EEE;vertical-align:top;}\n";
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private static void renderPdf(String xhtml, String baseUrl, File out) throws Exception {
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(xhtml, baseUrl);
        renderer.layout();
        try (FileOutputStream os = new FileOutputStream(out)) {
            renderer.createPDF(os);
        }
    }
}
