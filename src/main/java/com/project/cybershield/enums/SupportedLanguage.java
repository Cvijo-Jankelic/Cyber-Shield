package com.project.cybershield.enums;

import java.util.Locale;

public enum SupportedLanguage {
    ENGLISH("en", "English", "🇬🇧"),
    CROATIAN("hr", "Hrvatski", "🇭🇷");

    private final String code;
    private final String displayName;
    private final String flag;

    private SupportedLanguage(String code, String displayName, String flag) {
        this.code = code;
        this.displayName = displayName;
        this.flag = flag;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFlag() {
        return flag;
    }

    public Locale getLocale() {
        return new Locale(code);
    }


    public static SupportedLanguage fromCode(String code) {
        for (SupportedLanguage language : values()) {
            if (language.code.equals(code)) {
                return language;
            }
        }
        return ENGLISH;
    }
}
