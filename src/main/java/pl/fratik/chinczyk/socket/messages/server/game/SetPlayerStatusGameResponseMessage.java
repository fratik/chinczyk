package pl.fratik.chinczyk.socket.messages.server.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.game.PlayerStatus;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.server.ResponseStatus;

import java.util.Objects;

import static pl.fratik.chinczyk.util.StreamUtil.readString;
import static pl.fratik.chinczyk.util.StreamUtil.writeString;

@Getter
public class SetPlayerStatusGameResponseMessage extends ServerGameMessage {
    private final Place place;
    private final Status status;
    private final PlayerStatus playerStatus;

    public SetPlayerStatusGameResponseMessage(int gameCode, Place place, Status status, PlayerStatus playerStatus) {
        super(gameCode, GameOpCode.SET_PLAYER_STATUS);
        this.place = place;
        this.status = status;
        this.playerStatus = status == Status.SUCCESS ? Objects.requireNonNull(playerStatus) : null;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(place.getOffset());
        buf.writeByte(status.getCode());
        if (playerStatus != null) writeString(buf, playerStatus.name());
    }

    public static SetPlayerStatusGameResponseMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        Place place = Place.getByOffset(buf.readByte());
        Status status = Status.getByCode(buf.readByte());
        if (status != Status.SUCCESS) return new SetPlayerStatusGameResponseMessage(gameCode, place, status, null);
        PlayerStatus playerStatus = PlayerStatus.valueOf(readString(buf));
        return new SetPlayerStatusGameResponseMessage(gameCode, place, status, playerStatus);
    }

    @Getter
    public enum Status implements ResponseStatus {
        SUCCESS(0),
        INVALID_GAME(1),
        INVALID_PLAYER(2), //nieprawidłowy gracz
        UNEXPECTED_STATUS(3);

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
