package pl.fratik.chinczyk.socket.messages.client.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.server.game.ServerGameMessage;
import pl.fratik.chinczyk.socket.messages.server.game.StartGameResponseMessage;

@Getter
public class StartGameMessage extends ClientGameMessage {

    public StartGameMessage(int gameCode) {
        super(gameCode, GameOpCode.START_GAME);
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        // pusto
    }

    public static StartGameMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        return new StartGameMessage(gameCode);
    }

    @Override
    public ServerGameMessage getInvalidGameMessage() {
        return new StartGameResponseMessage(gameCode, StartGameResponseMessage.Status.INVALID_GAME);
    }
}
