package pl.fratik.chinczyk.socket.messages.client;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.socket.OpCode;
import pl.fratik.chinczyk.util.StreamUtil;

@Getter
public class IdentifyMessage extends ClientMessage {
    private final String token;
    private final byte version;

    public IdentifyMessage(String token, byte version) {
        super(OpCode.IDENTIFY);
        this.token = token;
        this.version = version;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        StreamUtil.writeString(buf, token);
        buf.writeByte(version);
    }

    public static IdentifyMessage deserialize(ByteBuf buf) throws Exception {
        return new IdentifyMessage(StreamUtil.readString(buf), buf.readByte());
    }
}
