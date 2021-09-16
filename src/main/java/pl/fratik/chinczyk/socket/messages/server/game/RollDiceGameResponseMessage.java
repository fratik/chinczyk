package pl.fratik.chinczyk.socket.messages.server.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.server.ResponseStatus;

import java.util.Objects;

@Getter
public class RollDiceGameResponseMessage extends ServerGameMessage {
    private final Place place;
    private final Status status;
    private final Integer rolled;
    private final Byte canMoveBitmap;

    public RollDiceGameResponseMessage(int gameCode, Place place, Status status, Integer rolled, Byte canMoveBitmap) {
        super(gameCode, GameOpCode.ROLL_DICE);
        this.place = place;
        this.status = status;
        this.rolled = status == Status.SUCCESS ? Objects.requireNonNull(rolled) : null;
        this.canMoveBitmap = status == Status.SUCCESS ? Objects.requireNonNull(canMoveBitmap) : null;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(place.getOffset());
        buf.writeByte(status.getCode());
        if (rolled != null) buf.writeByte(rolled);
        if (canMoveBitmap != null) buf.writeByte(canMoveBitmap);
    }

    public static RollDiceGameResponseMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        Place place = Place.getByOffset(buf.readByte());
        Status status = Status.getByCode(buf.readByte());
        Integer rolled;
        Byte canMoveBitmap;
        if (status == Status.SUCCESS) {
            rolled = (int) buf.readUnsignedByte();
            canMoveBitmap = buf.readByte();
        } else {
            rolled = null;
            canMoveBitmap = null;
        }
        return new RollDiceGameResponseMessage(gameCode, place, status, rolled, canMoveBitmap);
    }

    @Getter
    public enum Status implements ResponseStatus {
        SUCCESS(0),
        INVALID_GAME(1),
        NOT_TURN(2),
        ALREADY_ROLLED(3);

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
