package pl.fratik.chinczyk.socket.messages.server;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import pl.fratik.chinczyk.socket.OpCode;

import java.util.Objects;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ConnectResponseMessage extends ServerMessage {
    private final int gameCode;
    private final Status status;
    private final Boolean isPrivate;

    public ConnectResponseMessage(int gameCode, Status status, Boolean isPrivate) {
        super(OpCode.CONNECT);
        this.gameCode = gameCode;
        this.status = Objects.requireNonNull(status);
        this.isPrivate = status == Status.SUCCESS ? Objects.requireNonNull(isPrivate) : null;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeInt(gameCode);
        buf.writeByte(status.getCode());
        if (status == Status.SUCCESS) buf.writeBoolean(isPrivate);
    }

    public static ConnectResponseMessage deserialize(ByteBuf buf) throws Exception {
        int gameCode = buf.readInt();
        Status status = Status.getByCode(buf.readByte());
        Boolean isPrivate;
        if (status == Status.SUCCESS) isPrivate = buf.readBoolean();
        else isPrivate = null;
        return new ConnectResponseMessage(gameCode, status, isPrivate);
    }

    public boolean isPrivate() {
        if (isPrivate == null) throw new IllegalStateException("nie sukces");
        return isPrivate;
    }

    @Getter
    public enum Status implements ResponseStatus {
        SUCCESS(0),
        INVALID_GAME(1),
        ALREADY_CONNECTED(2);

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
