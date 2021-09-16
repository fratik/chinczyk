package pl.fratik.chinczyk.socket.messages.server;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.socket.OpCode;

@Getter
public class HostResponseMessage extends ServerMessage {
    private final Status status;
    private final int gameCode;

    public HostResponseMessage(Status status) {
        this(status, 0);
    }

    public HostResponseMessage(Status status, int gameCode) {
        super(OpCode.HOST);
        this.status = status;
        this.gameCode = gameCode;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(status.getCode());
        if (status == Status.SUCCESS) buf.writeInt(gameCode);
    }

    public static HostResponseMessage deserialize(ByteBuf buf) {
        Status status = Status.getByCode(buf.readByte());
        if (status != Status.SUCCESS) return new HostResponseMessage(status, 0);
        return new HostResponseMessage(status, buf.readInt());
    }

    @Getter
    public enum Status implements ResponseStatus {
        SUCCESS(0),
        HOST_NULL(1),
        INTERNAL_ERROR(255);

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
