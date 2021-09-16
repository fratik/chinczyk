package pl.fratik.chinczyk.game;

public enum GameStatus {
    /**
     * Za mało graczy
     */
    WAITING_FOR_PLAYERS,
    /**
     * Czeka na start
     */
    WAITING,
    /**
     * W toku
     */
    IN_PROGRESS,
    /**
     * Gra ukończona
     */
    ENDED,
    /**
     * Gra anulowana
     */
    CANCELLED,
    /**
     * Coś się popsuło
     */
    ERRORED
}
