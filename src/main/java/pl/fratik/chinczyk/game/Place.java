package pl.fratik.chinczyk.game;

import lombok.Getter;

import java.awt.*;
import java.util.Set;

public enum Place {
    BLUE("\uD83D\uDFE6", 2, new Color(0x0000F8), new Color(0xFFFFFF)),
    GREEN("\uD83D\uDFE9", 12, new Color(0x007C00), new Color(0xFFFFFF)),
    YELLOW("\uD83D\uDFE8", 22, new Color(0xF4F600), new Color(0x000000)),
    RED("\uD83D\uDFE5", 32, new Color(0xFF0000), new Color(0xFFFFFF));

    @Getter private final String emoji;
    @Getter private final int offset;
    @Getter private final Color bgColor;
    @Getter private final Color textColor;

    Place(String emoji, int offset, Color bgColor, Color textColor) {
        this.emoji = emoji;
        this.offset = offset;
        this.bgColor = bgColor;
        this.textColor = textColor;
    }

    public static Place getNextPlace(Place currentPlace, Set<Place> places) {
        Place nextPlace;
        if (currentPlace == BLUE) nextPlace = GREEN;
        else if (currentPlace == GREEN) nextPlace = YELLOW;
        else if (currentPlace == YELLOW) nextPlace = RED;
        else if (currentPlace == RED) nextPlace = BLUE;
        else throw new IllegalArgumentException("Nieoczekiwana wartość " + currentPlace);
        if (places.contains(nextPlace)) return nextPlace;
        else return getNextPlace(nextPlace, places);
    }

    public static Place getByOffset(byte offset) {
        return getByOffset(Byte.toUnsignedInt(offset));
    }

    public static Place getByOffset(int offset) {
        for (Place p : values()) {
            if (p.getOffset() == offset) return p;
        }
        return null;
    }
}
