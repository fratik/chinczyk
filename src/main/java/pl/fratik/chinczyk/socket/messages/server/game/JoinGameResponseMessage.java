package pl.fratik.chinczyk.socket.messages.server.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.game.Player;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.PlayerDeserializer;
import pl.fratik.chinczyk.socket.messages.server.ResponseStatus;

@Getter
public class JoinGameResponseMessage extends ServerGameMessage {
    private final Place place;
    private final Status status;
    private final Player player;

    public JoinGameResponseMessage(int gameCode, Place place, Status status, Player player) {
        super(gameCode, GameOpCode.JOIN);
        this.place = place;
        this.status = status;
        this.player = player;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(place.getOffset());
        buf.writeByte(status.getCode());
        if (status == Status.SUCCESS) player.serialize(buf);
    }

    public static JoinGameResponseMessage deserialize(int gameCode, PlayerDeserializer<?> deserializer, ByteBuf buf) throws Exception {
        Place place = Place.getByOffset(buf.readByte());
        Status status = Status.getByCode(buf.readByte());
        if (status != Status.SUCCESS) return new JoinGameResponseMessage(gameCode, place, status, null);
        Player player = deserializer.deserialize(gameCode, buf);
        return new JoinGameResponseMessage(gameCode, place, status, player);
    }

    @Getter
    public enum Status implements ResponseStatus {
        SUCCESS(0),
        INVALID_GAME(1), //nie ma takiej gry lub nie połączono
        INVALID_USER(2),
        INVALID_NAME(3), //nazwa nieprawidłowa (nieprawidłowe znaki lub przekroczony limit długości), lub nazwa null i brak ID
        HOST_FIRST(4),
        PLACE_OCCUPIED(5);

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
