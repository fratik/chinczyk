package pl.fratik.chinczyk.game;

import lombok.Getter;

import java.util.Locale;

@Getter
public enum Language {
    ENGLISH("en-US"),
    FRENCH("fr-FR"),
    POLISH("pl");

    private final String shortName;

    Language(String shortName) {
        this.shortName = shortName;
    }
}
