package pl.fratik.chinczyk.socket.messages.server.game;

import io.netty.buffer.ByteBuf;
import pl.fratik.chinczyk.socket.GameOpCode;

public class SyncEndGameMessage extends ServerGameMessage {

    public SyncEndGameMessage(int gameCode) {
        super(gameCode, GameOpCode.SYNC_END);
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        //brak treści
    }

    public static SyncEndGameMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        return new SyncEndGameMessage(gameCode);
    }
}
