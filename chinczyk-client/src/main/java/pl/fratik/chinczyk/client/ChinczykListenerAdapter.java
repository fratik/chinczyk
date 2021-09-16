package pl.fratik.chinczyk.client;

import pl.fratik.chinczyk.client.game.ClientChinczyk;

public abstract class ChinczykListenerAdapter implements ChinczykListener {
    @Override
    public void onConnect(ClientChinczyk chinczyk) {}
}
