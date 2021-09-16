package pl.fratik.chinczyk.socket.messages.server.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.server.ResponseStatus;

@Getter
public class StartGameResponseMessage extends ServerGameMessage {
    private final Status status;

    public StartGameResponseMessage(int gameCode, Status status) {
        super(gameCode, GameOpCode.START_GAME);
        this.status = status;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(status.getCode());
    }

    public static StartGameResponseMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        return new StartGameResponseMessage(gameCode, Status.getByCode(buf.readByte()));
    }

    @Getter
    public enum Status implements ResponseStatus {
        SUCCESS(0),
        INVALID_GAME(1),
        NOT_READY(2),
        ALREADY_STARTED(3);

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
