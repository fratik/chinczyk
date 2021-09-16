package pl.fratik.chinczyk.socket.messages.server;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import pl.fratik.chinczyk.socket.OpCode;
import pl.fratik.chinczyk.socket.messages.Message;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class ServerMessage extends Message {
    protected ServerMessage(OpCode op) {
        super(op);
    }
}
