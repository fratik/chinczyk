package pl.fratik.chinczyk.game;

import java.util.*;

public interface Chinczyk {
    long getGameID();
    long getHosterID();
    boolean isPrivate();
    int getGameCode();
    Map<Place, ? extends Player> getPlayersMap();
    default Set<? extends Player> getPlayers() {
        return Collections.unmodifiableSet(new HashSet<>(getPlayersMap().values()));
    }
    Language getLanguage();
    Set<Rule> getRules();
    GameStatus getStatus();
}
