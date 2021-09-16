package pl.fratik.chinczyk.client.game;

import pl.fratik.chinczyk.game.Language;
import pl.fratik.chinczyk.game.Piece;
import pl.fratik.chinczyk.game.Player;
import pl.fratik.chinczyk.game.PlayerStatus;

import java.util.concurrent.Future;

public interface ClientPlayer extends Player {
    Future<Void> leaveGame();
    Future<Void> movePiece(Piece piece); //null jezeli koniec ruchu
    default Future<RollResult> rollDice() {
        return rollDice(null);
    }
    Future<RollResult> rollDice(Integer rolled); // cheatmode
    Future<Void> setLanguage(Language language);
    Future<Void> setName(String name);
    Future<Void> setStatus(PlayerStatus status);
}
