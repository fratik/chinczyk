package pl.fratik.chinczyk.socket;

import io.netty.buffer.ByteBuf;
import pl.fratik.chinczyk.game.Player;

public interface PlayerDeserializer<T extends Player> {
    T deserialize(int gameCode, ByteBuf buf) throws Exception;
}
