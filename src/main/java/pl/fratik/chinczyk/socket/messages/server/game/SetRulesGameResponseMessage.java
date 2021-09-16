package pl.fratik.chinczyk.socket.messages.server.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.server.ResponseStatus;

@Getter
public class SetRulesGameResponseMessage extends ServerGameMessage {
    private final Status status;

    public SetRulesGameResponseMessage(int gameCode, Status status) {
        super(gameCode, GameOpCode.SET_RULES);
        this.status = status;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(status.getCode());
    }

    public static SetRulesGameResponseMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        return new SetRulesGameResponseMessage(gameCode, Status.getByCode(buf.readByte()));
    }

    @Getter
    public enum Status implements ResponseStatus {
        SUCCESS(0),
        INVALID_GAME(1);

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
