package pl.fratik.chinczyk.socket.messages.client;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.socket.OpCode;

@Getter
public class ConnectMessage extends ClientMessage {
    private final int gameCode;

    public ConnectMessage(int gameCode) {
        super(OpCode.CONNECT);
        this.gameCode = gameCode;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeInt(gameCode);
    }

    public static ConnectMessage deserialize(ByteBuf buf) throws Exception {
        return new ConnectMessage(buf.readInt());
    }
}
