package pl.fratik.chinczyk.socket.messages.client.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.server.game.JoinGameResponseMessage;
import pl.fratik.chinczyk.socket.messages.server.game.ServerGameMessage;

import static pl.fratik.chinczyk.util.StreamUtil.readString;
import static pl.fratik.chinczyk.util.StreamUtil.writeString;

@Getter
public class JoinGameMessage extends ClientGameMessage {
    private final Place place;
    private final Long id;
    private final String name;

    public JoinGameMessage(int gameCode, Place place, Long id, String name) {
        super(gameCode, GameOpCode.JOIN);
        this.place = place;
        this.id = id;
        this.name = name;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(place.getOffset());
        buf.writeLong(id == null ? 0 : id);
        writeString(buf, name == null ? "" : name);
    }

    public static JoinGameMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        Place place = Place.getByOffset(buf.readByte());
        long id = buf.readLong();
        String name = readString(buf);
        return new JoinGameMessage(gameCode, place, id == 0 ? null : id, name.isEmpty() ? null : name);
    }

    @Override
    public ServerGameMessage getInvalidGameMessage() {
        return new JoinGameResponseMessage(gameCode, place, JoinGameResponseMessage.Status.INVALID_GAME, null);
    }
}
