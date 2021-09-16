package pl.fratik.chinczyk.socket.messages.server;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.socket.OpCode;

import java.util.Objects;

@Getter
public class DisconnectResponseMessage extends ServerMessage {
    private final int gameCode;
    private final Status status;

    public DisconnectResponseMessage(int gameCode, Status status) {
        super(OpCode.DISCONNECT);
        this.gameCode = gameCode;
        this.status = Objects.requireNonNull(status);
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeInt(gameCode);
        buf.writeByte(status.getCode());
    }

    public static DisconnectResponseMessage deserialize(ByteBuf buf) throws Exception {
        return new DisconnectResponseMessage(buf.readInt(), Status.getByCode(buf.readByte()));
    }

    @Getter
    public enum Status implements ResponseStatus {
        SUCCESS(0),
        NOT_CONNECTED(1);

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
