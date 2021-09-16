package pl.fratik.chinczyk.socket.messages.server;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.socket.OpCode;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static pl.fratik.chinczyk.util.StreamUtil.*;

@Getter
public class IdentifyResponseMessage extends ServerMessage {
    private final Status status;
    private final String clientName;
    private final Set<String> connections;

    public IdentifyResponseMessage(Status status) {
        this(status, null, null);
    }

    public IdentifyResponseMessage(Status status, String clientName, Set<String> connections) {
        super(OpCode.IDENTIFY);
        this.status = status;
        this.clientName = status == Status.SUCCESS ? Objects.requireNonNull(clientName) : null;
        this.connections = status == Status.SUCCESS ? Collections.unmodifiableSet(connections) : null;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(status.getCode());
        if (clientName != null) writeString(buf, clientName);
        if (connections != null) writeString(buf, setToString(connections));
    }

    public static IdentifyResponseMessage deserialize(ByteBuf buf) throws Exception {
        Status status = Status.getByCode(buf.readByte());
        Set<String> connections;
        String clientName;
        if (status == Status.SUCCESS) {
            clientName = readString(buf);
            connections = setFromString(readString(buf));
        } else {
            clientName = null;
            connections = null;
        }
        return new IdentifyResponseMessage(status, clientName, connections);
    }

    @Getter
    public enum Status implements ResponseStatus {
        SUCCESS(0),
        ALREADY_IDENTIFIED(1),
        INVALID_VERSION(2),
        INVALID_TOKEN(3);

        private final byte code;

        Status(int code) {
            this.code = (byte) code;
        }

        public static Status getByCode(byte b) {
            for (Status s : values()) {
                if (s.code == b) return s;
            }
            throw new IllegalArgumentException("nieprawidłowy status");
        }
    }
}
