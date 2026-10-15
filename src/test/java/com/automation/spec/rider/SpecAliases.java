package com.automation.spec.rider;

import org.openqa.selenium.By;

/**
 * Where the feature files name a control differently from the real app, the step still performs the same action on the
 * real control. Every alias here is a spec/app wording difference that the report lists under "spec corrections" --
 * it is never used to hide a behaviour difference (those scenarios are left to fail).
 */
public final class SpecAliases {

    private SpecAliases() {}

    /** Icon-only buttons in the top-right corner (Saved Places "+", saved-place trash). */
    public static final By TOP_RIGHT_ACTION = By.xpath(
            "(//*[(self::android.view.View or self::android.widget.Button) and @clickable='true' "
                    + "and string-length(@content-desc)=0])[last()]");

    /** Label the spec uses -> label the app actually shows. */
    public static String label(String specLabel) {
        return switch (specLabel) {
            case "Set as primary" -> "Make Default";
            case "Remove payment method" -> "Delete";
            case "Add card" -> "CONFIRM PREAUTHORIZATION";
            case "Referral progress" -> "View progress";
            default -> specLabel;
        };
    }

    /** Text the spec expects -> text the app shows for the same state. */
    public static String text(String specText) {
        return switch (specText) {
            case "Primary" -> "Default";
            default -> specText;
        };
    }

    /** Handles taps on controls that have no label of their own; returns true when it handled `specLabel`. */
    public static boolean tap(SpecUi ui, String specLabel) {
        switch (specLabel) {
            case "Add new place", "Delete" -> {
                ui.tapPublic(TOP_RIGHT_ACTION);
                return true;
            }
            case "Logout" -> {
                // Logout lives on the Profile page, not on the Account tab the spec starts from.
                ui.tapPublic(By.xpath("//*[@clickable='true' and contains(@content-desc,'View profile')]"));
                ui.waitSeen("Legal Name", 15);
                ui.tapLabel("Logout");
                return true;
            }
            default -> {
                String mapped = label(specLabel);
                if (!mapped.equals(specLabel)) {
                    ui.tapLabel(mapped);
                    return true;
                }
                return false;
            }
        }
    }
}
