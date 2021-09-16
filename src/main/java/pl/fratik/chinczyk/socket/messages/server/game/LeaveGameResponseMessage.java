package pl.fratik.chinczyk.socket.messages.server.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.server.ResponseStatus;

@Getter
public class LeaveGameResponseMessage extends ServerGameMessage {
    private final Place place;
    private final Status status;

    public LeaveGameResponseMessage(int gameCode, Place place, Status status) {
        super(gameCode, GameOpCode.LEAVE);
        this.place = place;
        this.status = status;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(place.getOffset());
        buf.writeByte(status.getCode());
    }

    public static LeaveGameResponseMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        Place place = Place.getByOffset(buf.readByte());
        Status status = Status.getByCode(buf.readByte());
        return new LeaveGameResponseMessage(gameCode, place, status);
    }

    @Getter
    public enum Status implements ResponseStatus {
        SUCCESS(0),
        INVALID_GAME(1),
        INVALID_PLAYER(2);

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
