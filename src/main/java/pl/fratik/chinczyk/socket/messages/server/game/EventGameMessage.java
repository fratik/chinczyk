package pl.fratik.chinczyk.socket.messages.server.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Event;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.game.Player;
import pl.fratik.chinczyk.socket.GameOpCode;

import java.util.Map;
import java.util.Objects;

@Getter
public class EventGameMessage extends ServerGameMessage {
    private final Event event;

    public EventGameMessage(int gameCode, Event event) {
        super(gameCode, GameOpCode.EVENT);
        this.event = Objects.requireNonNull(event);
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        event.serialize(buf);
    }

    public static EventGameMessage deserialize(int gameCode, ByteBuf buf, Map<Place, ? extends Player> playerMap) throws Exception {
        return new EventGameMessage(gameCode, Event.deserialize(buf, playerMap));
    }
}
