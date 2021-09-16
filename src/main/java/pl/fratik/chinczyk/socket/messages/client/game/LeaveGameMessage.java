package pl.fratik.chinczyk.socket.messages.client.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.server.game.LeaveGameResponseMessage;
import pl.fratik.chinczyk.socket.messages.server.game.ServerGameMessage;

@Getter
public class LeaveGameMessage extends ClientGameMessage {
    private final Place place;

    public LeaveGameMessage(int gameCode, Place place) {
        super(gameCode, GameOpCode.LEAVE);
        this.place = place;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(place.getOffset());
    }

    public static LeaveGameMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        Place place = Place.getByOffset(buf.readByte());
        return new LeaveGameMessage(gameCode, place);
    }

    @Override
    public ServerGameMessage getInvalidGameMessage() {
        return new LeaveGameResponseMessage(gameCode, place, LeaveGameResponseMessage.Status.INVALID_GAME);
    }
}
