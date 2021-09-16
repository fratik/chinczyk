package pl.fratik.chinczyk.socket.messages.client.game;

import io.netty.buffer.ByteBuf;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.OpCode;
import pl.fratik.chinczyk.socket.messages.client.ClientMessage;
import pl.fratik.chinczyk.socket.messages.server.game.ServerGameMessage;

public abstract class ClientGameMessage extends ClientMessage {
    protected final int gameCode;
    protected final GameOpCode gameOp;

    protected ClientGameMessage(int gameCode, GameOpCode gameOp) {
        super(OpCode.GAME);
        this.gameCode = gameCode;
        this.gameOp = gameOp;
    }

    public final int getGameCode() {
        return gameCode;
    }

    public final GameOpCode getGameOp() {
        return gameOp;
    }

    @Override
    public void serialize(ByteBuf buf) throws Exception {
        buf.writeByte(op.getOp());
        buf.writeInt(gameCode);
        buf.writeByte(gameOp.getOp());
        serializeContent(buf);
    }

    public abstract ServerGameMessage getInvalidGameMessage();
}
