package pl.fratik.chinczyk.game;

public enum PlayerStatus {
    /**
     * Dołączył do rozgrywki - czeka na gotowość
     */
    JOINED,
    /**
     * Gotowy do gry
     */
    READY,
    /**
     * W grze
     */
    PLAYING,
    /**
     * Anulował / opuścił grę
     */
    LEFT
}
