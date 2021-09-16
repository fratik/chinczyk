package pl.fratik.chinczyk.server.socket.handlers;

import io.netty.channel.ChannelInboundHandlerAdapter;
import pl.fratik.chinczyk.game.Chinczyk;
import pl.fratik.chinczyk.server.database.DatabaseManager;

import java.util.Map;

public abstract class MessageHandler extends ChannelInboundHandlerAdapter {
    protected final Map<Integer, Chinczyk> games;
    protected final DatabaseManager dbm;

    protected MessageHandler(Map<Integer, Chinczyk> games, DatabaseManager dbm) {
        this.games = games;
        this.dbm = dbm;
    }
}
