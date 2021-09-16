package pl.fratik.chinczyk.server.socket.handlers;

import pl.fratik.chinczyk.game.Chinczyk;
import pl.fratik.chinczyk.server.database.DatabaseManager;

import java.util.Map;

public class MessageHandlerFactory {
    private final Map<Integer, Chinczyk> games;
    private final DatabaseManager dbm;

    public MessageHandlerFactory(Map<Integer, Chinczyk> games, DatabaseManager dbm) {
        this.games = games;
        this.dbm = dbm;
    }

    public MessageHandler getHandler(byte version) {
        if (version != 0) throw new IllegalArgumentException("nieprawidłowa wersja");
        return new Version0MessageHandler(games, dbm);
    }

    public boolean isVersionSupported(byte version) {
        return version == 0;
    }
}
