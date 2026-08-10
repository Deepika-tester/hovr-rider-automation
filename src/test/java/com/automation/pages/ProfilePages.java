package com.automation.pages;

import org.openqa.selenium.By;

/**
 * Account / Profile / Saved Places / Trip History / Settings / Messages / Help screens.
 * See BasePage javadoc re: Flutter accessibility-label locators — verify all "VERIFY:" labels.
 */
class AccountPage extends BasePage {
    private final By profileMenuItem = byText("Profile");           // VERIFY
    private final By savedPlacesMenuItem = byText("Saved Places");
    private final By tripsMenuItem = byText("Trips");                // VERIFY label wording
    private final By settingsMenuItem = byText("Settings");
    private final By messagesMenuItem = byText("Messages");
    private final By logOutButton = byText("Log Out");
    private final By helpButton = byText("Help");

    public void tapProfile() {
        tap(profileMenuItem);
    }

    public void tapSavedPlaces() {
        tap(savedPlacesMenuItem);
    }

    public void tapTrips() {
        tap(tripsMenuItem);
    }

    public void tapSettings() {
        tap(settingsMenuItem);
    }

    public void tapMessages() {
        tap(messagesMenuItem);
    }

    public void tapLogOut() {
        tap(logOutButton);
    }

    public void tapHelp() {
        tap(helpButton);
    }
}

class UserProfilePage extends BasePage {
    private final By nameField = byAccessibilityId("profile_name");   // VERIFY
    private final By emailField = byAccessibilityId("profile_email"); // VERIFY
    private final By phoneField = byAccessibilityId("profile_phone"); // VERIFY
    private final By photoElement = byAccessibilityId("profile_photo"); // VERIFY
    private final By editButton = byText("Edit");

    public boolean isFieldDisplayed(String field) {
        return switch (field.toLowerCase()) {
            case "name" -> isDisplayed(nameField);
            case "email" -> isDisplayed(emailField);
            case "phone number", "phone" -> isDisplayed(phoneField);
            case "profile photo" -> isDisplayed(photoElement);
            default -> false;
        };
    }

    public void tapEdit() {
        tap(editButton);
    }
}

class EditProfilePage extends BasePage {
    private final By firstNameField = byAccessibilityId("edit_first_name"); // VERIFY
    private final By saveButton = byText("Save");

    public void setFirstName(String value) {
        typeInto(firstNameField, value);
    }

    public void clearFirstName() {
        waitFor(firstNameField).clear();
    }

    public void tapSave() {
        tap(saveButton);
    }
}

class SavedPlacesPage extends BasePage {
    private final By addNewPlaceButton = byText("Add New Place");
    private final By searchField = byAccessibilityId("place_search_field"); // VERIFY

    public void tapPlace(String label) {
        tapText(label);
    }

    public void tapAddNewPlace() {
        tap(addNewPlaceButton);
    }

    public void searchAddress(String address) {
        typeInto(searchField, address);
    }

    public void selectSuggestion(String address) {
        tapText(address); // assumes autocomplete result row shows the address text
    }

    public void tapSave() {
        tapText("Save");
    }

    public void tapEdit() {
        tapText("Edit");
    }

    public void tapDelete() {
        tapText("Delete");
    }

    public void confirmDelete() {
        tapText("Confirm"); // VERIFY exact confirm-dialog button label
    }

    public void enterLabel(String label) {
        typeInto(byAccessibilityId("place_label_field"), label); // VERIFY
    }
}

class TripHistoryPage extends BasePage {
    private final By emptyStateMessage = byAccessibilityId("trips_empty_state"); // VERIFY

    public void tapTrip(int index) {
        // VERIFY: confirm trip list item accessibility id pattern, e.g. "trip_item_0"
        tap(byAccessibilityId("trip_item_" + index));
    }

    public boolean isEmptyStateShown() {
        return isDisplayed(emptyStateMessage);
    }
}

class SettingsPage extends BasePage {
    private final By languageMenuItem = byText("Language");
    private final By notificationSettingsItem = byText("Notification Settings"); // VERIFY wording

    public void tapLanguage() {
        tap(languageMenuItem);
    }

    public void tapNotificationSettings() {
        tap(notificationSettingsItem);
    }
}

class LanguageSettingsPage extends BasePage {
    public void selectLanguage(String language) {
        tapText(language); // "French" / "English"
    }
}