package pl.fratik.chinczyk.socket.messages.server.game;

import io.netty.buffer.ByteBuf;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.socket.GameOpCode;

public class TurnGameMessage extends ServerGameMessage {
    private final Place place;

    public TurnGameMessage(int gameCode, Place place) {
        super(gameCode, GameOpCode.TURN);
        this.place = place;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(place.getOffset());
    }

    public static TurnGameMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        return new TurnGameMessage(gameCode, Place.getByOffset(buf.readByte()));
    }
}
