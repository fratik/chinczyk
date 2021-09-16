package pl.fratik.chinczyk.game;

import java.util.ArrayList;

public abstract class EventStorage extends ArrayList<Event> {
    public Event getLastEvent() {
        if (size() == 0) return null;
        return get(size() - 1);
    }

    @Override
    public boolean add(Event event) {
        onEvent(event);
        return super.add(event);
    }

    protected abstract void onEvent(Event event);
}
