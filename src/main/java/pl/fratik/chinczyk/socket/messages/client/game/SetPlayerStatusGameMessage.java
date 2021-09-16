package pl.fratik.chinczyk.socket.messages.client.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.game.PlayerStatus;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.server.game.ServerGameMessage;
import pl.fratik.chinczyk.socket.messages.server.game.SetPlayerStatusGameResponseMessage;

import static pl.fratik.chinczyk.util.StreamUtil.readString;
import static pl.fratik.chinczyk.util.StreamUtil.writeString;

@Getter
public class SetPlayerStatusGameMessage extends ClientGameMessage {
    private final Place place;
    private final PlayerStatus playerStatus;

    public SetPlayerStatusGameMessage(int gameCode, Place place, PlayerStatus playerStatus) {
        super(gameCode, GameOpCode.SET_PLAYER_STATUS);
        this.place = place;
        this.playerStatus = playerStatus;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(place.getOffset());
        writeString(buf, playerStatus.name());
    }

    public static SetPlayerStatusGameMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        Place place = Place.getByOffset(buf.readByte());
        PlayerStatus status = PlayerStatus.valueOf(readString(buf));
        return new SetPlayerStatusGameMessage(gameCode, place, status);
    }

    @Override
    public ServerGameMessage getInvalidGameMessage() {
        return new SetPlayerStatusGameResponseMessage(gameCode, place, SetPlayerStatusGameResponseMessage.Status.INVALID_GAME, null);
    }
}
