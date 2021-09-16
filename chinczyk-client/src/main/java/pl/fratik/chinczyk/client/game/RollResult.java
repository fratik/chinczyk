package pl.fratik.chinczyk.client.game;

import lombok.Data;

@Data
public class RollResult {
    private final int rolled;
    private final int canMoveBitmap;
}
