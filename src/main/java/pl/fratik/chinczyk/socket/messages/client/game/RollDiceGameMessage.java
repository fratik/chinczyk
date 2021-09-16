package pl.fratik.chinczyk.socket.messages.client.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.server.game.RollDiceGameResponseMessage;
import pl.fratik.chinczyk.socket.messages.server.game.ServerGameMessage;

@Getter
public class RollDiceGameMessage extends ClientGameMessage {
    private final Place place;
    private final Integer rolled; // ilość wyrzuconych oczek - null jeżeli devmode wyłączony, 0-6 jeżeli włączony

    public RollDiceGameMessage(int gameCode, Place place) {
        this(gameCode, place, null);
    }

    public RollDiceGameMessage(int gameCode, Place place, Integer rolled) {
        super(gameCode, GameOpCode.ROLL_DICE);
        this.place = place;
        this.rolled = rolled;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(place.getOffset());
        if (rolled != null) buf.writeByte(rolled);
    }

    public static RollDiceGameMessage deserialize(int gameCode, boolean devModeEnabled, ByteBuf buf) throws Exception {
        return new RollDiceGameMessage(gameCode, Place.getByOffset(buf.readByte()), devModeEnabled ? (int) buf.readUnsignedByte() : null);
    }

    @Override
    public ServerGameMessage getInvalidGameMessage() {
        return new RollDiceGameResponseMessage(gameCode, place, RollDiceGameResponseMessage.Status.INVALID_GAME, null, null);
    }
}
