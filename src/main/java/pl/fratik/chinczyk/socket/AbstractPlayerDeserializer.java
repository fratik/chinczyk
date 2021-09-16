package pl.fratik.chinczyk.socket;

import io.netty.buffer.ByteBuf;
import pl.fratik.chinczyk.game.Language;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.game.Player;
import pl.fratik.chinczyk.game.PlayerStatus;

import static pl.fratik.chinczyk.util.StreamUtil.readString;

public abstract class AbstractPlayerDeserializer<T extends Player> implements PlayerDeserializer<T> {
    protected abstract T getPlayerByPlace(int gameCode, Place place);
    protected abstract T createPlayer(int gameCode, Place place, Long id, String playerName, Language language, PlayerStatus status) throws Exception;
    protected abstract T updatePlayer(T player, String playerName, Language language, PlayerStatus status) throws Exception;

    @Override
    public T deserialize(int gameCode, ByteBuf buf) throws Exception {
        Place place = Place.getByOffset(buf.readByte());
        T currentPlayer = getPlayerByPlace(gameCode, place);
        long id = buf.readLong();
        String playerName = readString(buf);
        Language language = Language.valueOf(readString(buf));
        PlayerStatus status = PlayerStatus.valueOf(readString(buf));
        if (currentPlayer == null) return createPlayer(gameCode, place, id == 0 ? null : id, playerName, language, status);
        else return updatePlayer(currentPlayer, playerName, language, status);
    }
}
