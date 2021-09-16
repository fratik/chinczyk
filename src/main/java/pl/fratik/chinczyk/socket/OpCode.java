package pl.fratik.chinczyk.socket;

import lombok.Getter;

@Getter
public enum OpCode {
    /**
     * Podłączenie do systemu - wymaga wysłania tokenu
     */
    IDENTIFY(0),
    /**
     * Przygotowuje nową grę
     */
    HOST(1),
    /**
     * Łączy do istniejącej gry
     */
    CONNECT(2),
    /**
     * Informacje o istniejącej grze
     */
    INFO(3),
    /**
     * Rozłącza z gry
     */
    DISCONNECT(4),
    /**
     * Komunikacja z grą - wymagany kod gry
     */
    GAME(255);

    private final byte op;

    OpCode(int op) {
        this.op = (byte) op;
    }

    public static OpCode decode(byte b) {
        for (OpCode value : values()) {
            if (value.op == b) return value;
        }
        throw new IllegalArgumentException("Nieprawidłowy OP!");
    }
}
