package pl.fratik.chinczyk.client.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.fratik.chinczyk.game.Event;
import pl.fratik.chinczyk.game.GameStatus;

class GameListenerWrapper implements GameListener {
    final GameListener listener;
    private final Logger logger;

    public GameListenerWrapper(GameListener listener) {
        this.listener = listener;
        logger = LoggerFactory.getLogger(getClass());
    }

    protected void handleException(Throwable t) {
        if (t instanceof Error) throw (Error) t;
        logger.warn("Wystąpił błąd w " + listener, t);
    }

    @Override
    public void onReady() {
        try {
            listener.onReady();
        } catch (Throwable t) {
            handleException(t);
        }
    }

    @Override
    public void onStatusUpdate(GameStatus oldStatus) {
        try {
            listener.onStatusUpdate(oldStatus);
        } catch (Throwable t) {
            handleException(t);
        }
    }

    @Override
    public void onEvent(Event event) {
        try {
            listener.onEvent(event);
        } catch (Throwable t) {
            handleException(t);
        }
    }
}
