package pl.fratik.chinczyk.socket.messages;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import pl.fratik.chinczyk.socket.OpCode;

@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Message {
    protected final OpCode op;

    public final OpCode getOp() {
        return op;
    }

    protected abstract void serializeContent(ByteBuf buf) throws Exception;

    public void serialize(ByteBuf buf) throws Exception {
        buf.writeByte(op.getOp());
        serializeContent(buf);
    }
}
