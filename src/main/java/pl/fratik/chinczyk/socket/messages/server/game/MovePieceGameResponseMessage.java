package pl.fratik.chinczyk.socket.messages.server.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.server.ResponseStatus;

@Getter
public class MovePieceGameResponseMessage extends ServerGameMessage {
    private final Place place;
    private final byte pieceIndex;
    private final Status status;

    public MovePieceGameResponseMessage(int gameCode, Place place, byte pieceIndex, Status status) {
        super(gameCode, GameOpCode.MOVE_PIECE);
        this.place = place;
        this.pieceIndex = pieceIndex;
        this.status = status;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(place.getOffset());
        buf.writeByte(pieceIndex);
        buf.writeByte(status.getCode());
    }

    public static MovePieceGameResponseMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        Place place = Place.getByOffset(buf.readByte());
        byte pieceIndex = buf.readByte();
        Status status = Status.getByCode(buf.readByte());
        return new MovePieceGameResponseMessage(gameCode, place, pieceIndex, status);
    }

    @Getter
    public enum Status implements ResponseStatus {
        SUCCESS(0),
        INVALID_GAME(1),
        NOT_TURN(2),
        NOT_ROLLED(3),
        INVALID_PIECE(4);

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
