package pl.fratik.chinczyk.socket.messages.client.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.server.game.ServerGameMessage;
import pl.fratik.chinczyk.socket.messages.server.game.SetNameGameResponseMessage;

import static pl.fratik.chinczyk.util.StreamUtil.readString;
import static pl.fratik.chinczyk.util.StreamUtil.writeString;

@Getter
public class SetNameGameMessage extends ClientGameMessage {
    private final Place place;
    private final String name;

    public SetNameGameMessage(int gameCode, Place place, String name) {
        super(gameCode, GameOpCode.SET_NAME);
        this.place = place;
        this.name = name;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(place.getOffset());
        writeString(buf, name);
    }

    public static SetNameGameMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        Place place = Place.getByOffset(buf.readByte());
        String name = readString(buf);
        return new SetNameGameMessage(gameCode, place, name.isEmpty() ? null : name);
    }

    @Override
    public ServerGameMessage getInvalidGameMessage() {
        return new SetNameGameResponseMessage(gameCode, place, SetNameGameResponseMessage.Status.INVALID_GAME, null);
    }
}
