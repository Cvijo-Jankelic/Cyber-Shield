package com.project.cybershield.util;

import com.project.cybershield.enums.SupportedLanguage;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;


/**
 * Simple Locale Manager for runtime language switching
 * Features: Runtime switching, easy to extend, complete coverage
 */
public class LocaleManager {

    private static LocaleManager instance;

    private static final String BUNDLE_BASE_NAME = "messages";

    private final ObjectProperty<Locale> currentLocale;
    private ResourceBundle resourceBundle;


    /**
     * Private constructor - use getInstance()
     */
    private LocaleManager() {
        // Start with English as default
        Locale initialLocale = SupportedLanguage.ENGLISH.getLocale();

        currentLocale = new SimpleObjectProperty<>(initialLocale);
        loadResourceBundle(initialLocale);

        // Listen for locale changes and reload bundle
        currentLocale.addListener((obs, oldLocale, newLocale) -> {
            loadResourceBundle(newLocale);
        });
    }

    /**
     * Get the singleton instance
     */
    public static LocaleManager getInstance() {
        if (instance == null) {
            instance = new LocaleManager();
        }
        return instance;
    }

    /**
     * Load the resource bundle for the given locale
     */
    private void loadResourceBundle(Locale locale) {
        try {
            resourceBundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);
            System.out.println("✓ Loaded language: " + locale.getDisplayLanguage());
        } catch (Exception e) {
            System.err.println("✗ Failed to load resource bundle for: " + locale);
            e.printStackTrace();
            // Fallback to English
            resourceBundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, Locale.ENGLISH);
        }
    }

    /**
     * Get localized string by key
     *
     * @param key Translation key (e.g., "nav.dashboard")
     * @return Translated text
     */
    public String getString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (Exception e) {
            System.err.println("✗ Missing translation key: " + key);
            return "!" + key + "!"; // Shows missing keys clearly
        }
    }

    /**
     * Change the application language
     *
     * @param language The language to switch to
     */
    public void setLanguage(SupportedLanguage language) {
        currentLocale.set(language.getLocale());
        System.out.println("→ Language changed to: " + language.getDisplayName());
    }

    /**
     * Change the application language by code
     *
     * @param languageCode Language code (e.g., "en", "hr")
     */
    public void setLanguage(String languageCode) {
        SupportedLanguage language = SupportedLanguage.fromCode(languageCode);
        setLanguage(language);
    }

    /**
     * Get current locale
     *
     * @return Current Locale object
     */
    public Locale getCurrentLocale() {
        return currentLocale.get();
    }

    /**
     * Get current locale property for binding
     * Listen to this property to detect language changes
     *
     * @return Observable locale property
     */
    public ObjectProperty<Locale> currentLocaleProperty() {
        return currentLocale;
    }

    /**
     * Get current language
     *
     * @return Current SupportedLanguage
     */
    public SupportedLanguage getCurrentLanguage() {
        String code = currentLocale.get().getLanguage();
        return SupportedLanguage.fromCode(code);
    }

    /**
     * Get all supported languages
     *
     * @return Array of all supported languages
     */
    public SupportedLanguage[] getSupportedLanguages() {
        return SupportedLanguage.values();
    }

    /**
     * Check if a translation key exists
     *
     * @param key Translation key to check
     * @return true if key exists, false otherwise
     */
    public boolean hasKey(String key) {
        return resourceBundle.containsKey(key);
    }

    /**
     * Get the resource bundle directly (for advanced usage)
     *
     * @return Current ResourceBundle
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
}