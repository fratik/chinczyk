package pl.fratik.chinczyk.client.game;

import org.jetbrains.annotations.NotNull;
import pl.fratik.chinczyk.game.Language;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.game.PlayerStatus;
import pl.fratik.chinczyk.socket.AbstractPlayerDeserializer;

import java.util.Map;

public class ClientPlayerDeserializer extends AbstractPlayerDeserializer<ClientPlayerImpl> {
    private final Map<Integer, ClientChinczykImpl> games;

    public ClientPlayerDeserializer(Map<Integer, ClientChinczykImpl> games) {
        this.games = games;
    }

    @Override
    protected ClientPlayerImpl getPlayerByPlace(int gameCode, Place place) {
        ClientChinczykImpl chinczyk = getGame(gameCode);
        return chinczyk.getPlayersMap().get(place);
    }

    @NotNull
    private ClientChinczykImpl getGame(int gameCode) {
        ClientChinczykImpl chinczyk = games.get(gameCode);
        if (chinczyk == null) throw new IllegalArgumentException("nieprawidłowy gameCode");
        return chinczyk;
    }

    @Override
    protected ClientPlayerImpl createPlayer(int gameCode, Place place, Long id, String playerName, Language language, PlayerStatus status) throws Exception {
        return new ClientPlayerImpl(getGame(gameCode), place, id, playerName, language, status);
    }

    @Override
    protected ClientPlayerImpl updatePlayer(ClientPlayerImpl player, String playerName, Language language, PlayerStatus status) throws Exception {
        player.setPlayerNameInternal(playerName);
        player.setLanguageInternal(language);
        player.setStatusInternal(status);
        return player;
    }
}
