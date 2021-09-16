package pl.fratik.chinczyk.socket.messages.client.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.server.game.MovePieceGameResponseMessage;
import pl.fratik.chinczyk.socket.messages.server.game.ServerGameMessage;

@Getter
public class MovePieceGameMessage extends ClientGameMessage {
    private final Place place;
    private final byte pieceIndex; //0-3 lub 255 (unsigned)

    public MovePieceGameMessage(int gameCode, Place place, byte pieceIndex) {
        super(gameCode, GameOpCode.MOVE_PIECE);
        this.place = place;
        this.pieceIndex = pieceIndex;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(place.getOffset());
        buf.writeByte(pieceIndex);
    }

    public static MovePieceGameMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        return new MovePieceGameMessage(gameCode, Place.getByOffset(buf.readByte()), buf.readByte());
    }

    @Override
    public ServerGameMessage getInvalidGameMessage() {
        return new MovePieceGameResponseMessage(gameCode, place, pieceIndex, MovePieceGameResponseMessage.Status.INVALID_GAME);
    }
}
