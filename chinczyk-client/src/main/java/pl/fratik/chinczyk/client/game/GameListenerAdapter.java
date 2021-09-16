package pl.fratik.chinczyk.client.game;

import pl.fratik.chinczyk.game.GameStatus;

public abstract class GameListenerAdapter implements GameListener {
    @Override public void onReady() {}
    @Override public void onStatusUpdate(GameStatus oldStatus) {}
}
