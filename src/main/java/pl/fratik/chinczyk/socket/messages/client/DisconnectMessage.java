package pl.fratik.chinczyk.socket.messages.client;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.socket.OpCode;

@Getter
public class DisconnectMessage extends ClientMessage {
    private final int gameCode;

    public DisconnectMessage(int gameCode) {
        super(OpCode.DISCONNECT);
        this.gameCode = gameCode;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeInt(gameCode);
    }

    public static DisconnectMessage deserialize(ByteBuf buf) throws Exception {
        return new DisconnectMessage(buf.readInt());
    }
}
