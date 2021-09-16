package pl.fratik.chinczyk.socket;

import lombok.Getter;

@Getter
public enum GameOpCode {
    /**
     * C→S Dołącz do gry<br>
     * S→C ktoś dołącza do gry
     */
    JOIN(0),
    /**
     * C→S Opuść grę<br>
     * S→C ktoś opuścił grę (w lobby)
     */
    LEAVE(1),
    /**
     * C→S Ustaw status<br>
     * S→C ktoś zmienił status
     */
    SET_PLAYER_STATUS(2),
    /**
     * C→S Zmień nazwę gracza<br>
     * S→C ktoś zmienił nazwę
     */
    SET_NAME(3),
    /**
     * C→S Zmień język gracza<br>
     * S→C ktoś zmienił język
     */
    SET_LANGUAGE(4),
    /**
     * C→S Zmiana zasad gry
     */
    SET_RULES(5),
    /**
     * C→S Startuj grę
     */
    START_GAME(6),
    /**
     * C→S Rzuć kostką<br>
     * S→C kostka rzucona
     */
    ROLL_DICE(7),
    /**
     * C→S Rusz pionkiem
     */
    MOVE_PIECE(8),
    /**
     * S→C Kolej gracza
     */
    TURN(9),
    /**
     * S→C Wszystkie eventy od startu gry do teraz zostały pomyślnie przesłane
     */
    SYNC_END(10),
    /**
     * S→C Zmiana statusu gry
     */
    GAME_STATUS_UPDATE(253),
    /**
     * S→C Nagłówek gry; emitowany po dołączeniu, co zmianę języka / zasad w lobby i przed startem
     */
    HEADER(254),
    /**
     * S→C Wydarzenie w grze
     */
    EVENT(255);

    private final byte op;

    GameOpCode(int op) {
        this.op = (byte) op;
    }

    public static GameOpCode decode(byte b) {
        for (GameOpCode value : values()) {
            if (value.op == b) return value;
        }
        throw new IllegalArgumentException("Nieprawidłowy OP!");
    }
}
