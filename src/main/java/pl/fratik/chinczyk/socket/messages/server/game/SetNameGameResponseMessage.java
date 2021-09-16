package pl.fratik.chinczyk.socket.messages.server.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.server.ResponseStatus;

import static pl.fratik.chinczyk.util.StreamUtil.readString;
import static pl.fratik.chinczyk.util.StreamUtil.writeString;

@Getter
public class SetNameGameResponseMessage extends ServerGameMessage {
    private final Place place;
    private final Status status;
    private final String name;

    public SetNameGameResponseMessage(int gameCode, Place place, Status status, String name) {
        super(gameCode, GameOpCode.SET_NAME);
        this.place = place;
        this.status = status;
        this.name = name;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(place.getOffset());
        buf.writeByte(status.getCode());
        if (status == Status.SUCCESS) writeString(buf, name == null ? "" : name);
    }

    public static SetNameGameResponseMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        Place place = Place.getByOffset(buf.readByte());
        Status status = Status.getByCode(buf.readByte());
        if (status != Status.SUCCESS) return new SetNameGameResponseMessage(gameCode, place, status, null);
        String name = readString(buf);
        return new SetNameGameResponseMessage(gameCode, place, status, name.isEmpty() ? null : name);
    }

    @Getter
    public enum Status implements ResponseStatus {
        SUCCESS(0),
        INVALID_GAME(1),
        INVALID_PLAYER(2),
        INVALID_NAME(3); // nieprawidłowa nazwa

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
