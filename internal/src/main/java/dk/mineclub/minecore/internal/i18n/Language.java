package dk.mineclub.minecore.internal.i18n;

import java.util.Locale;

public enum Language {
    ENGLISH("en", Locale.UK),
    DANISH("da", Locale.forLanguageTag("da-DK"));

    private final String code;
    private final Locale locale;

    Language(String code, Locale locale) {
        this.code = code;
        this.locale = locale;
    }

    public String code() {
        return code;
    }

    public Locale locale() {
        return locale;
    }

    public static Language from(String code) {
        return "da".equals(code) ? DANISH : ENGLISH;
    }
}
