package pl.fratik.chinczyk.socket.messages.server.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Header;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.PlayerDeserializer;

import java.util.Objects;

@Getter
public class HeaderGameMessage extends ServerGameMessage {
    private final Header header;

    public HeaderGameMessage(int gameCode, Header header) {
        super(gameCode, GameOpCode.HEADER);
        this.header = Objects.requireNonNull(header);
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        header.serialize(buf);
    }

    public static HeaderGameMessage deserialize(int gameCode, ByteBuf buf, PlayerDeserializer<?> playerDeserializer) throws Exception {
        return new HeaderGameMessage(gameCode, Header.deserialize(gameCode, buf, playerDeserializer));
    }
}
