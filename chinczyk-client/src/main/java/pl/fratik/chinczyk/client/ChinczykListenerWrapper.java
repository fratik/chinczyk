package pl.fratik.chinczyk.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.fratik.chinczyk.client.game.ClientChinczyk;

class ChinczykListenerWrapper implements ChinczykListener {
    final ChinczykListener listener;
    private final Logger logger;

    public ChinczykListenerWrapper(ChinczykListener listener) {
        this.listener = listener;
        logger = LoggerFactory.getLogger(getClass());
    }

    protected void handleException(Throwable t) {
        if (t instanceof Error) throw (Error) t;
        logger.warn("Wystąpił błąd w " + listener, t);
    }

    @Override
    public void onConnect(ClientChinczyk chinczyk) {
        try {
            listener.onConnect(chinczyk);
        } catch (Throwable t) {
            handleException(t);
        }
    }
}
