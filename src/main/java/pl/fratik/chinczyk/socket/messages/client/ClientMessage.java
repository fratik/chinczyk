package pl.fratik.chinczyk.socket.messages.client;

import pl.fratik.chinczyk.socket.OpCode;
import pl.fratik.chinczyk.socket.messages.Message;

public abstract class ClientMessage extends Message {
    protected ClientMessage(OpCode op) {
        super(op);
    }
}
