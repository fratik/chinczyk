package pl.fratik.chinczyk.socket.messages.client;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.socket.OpCode;

@Getter
public class InfoMessage extends ClientMessage {
    private final int gameCode;

    public InfoMessage(int gameCode) {
        super(OpCode.INFO);
        this.gameCode = gameCode;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeInt(gameCode);
    }

    public static InfoMessage deserialize(ByteBuf buf) throws Exception {
        return new InfoMessage(buf.readInt());
    }
}
