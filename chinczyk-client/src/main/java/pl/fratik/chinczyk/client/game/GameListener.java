package pl.fratik.chinczyk.client.game;

import pl.fratik.chinczyk.game.Event;
import pl.fratik.chinczyk.game.GameStatus;

public interface GameListener {
    /**
     * Funkcja wykonywana kiedy gra będzie gotowa — tj. po otrzymaniu pierwszego nagłówka.
     * Do momentu jego otrzymania, żadna inna funkcja nie będzie wywoływana.
     */
    void onReady();

    /**
     * Status gry został zmieniony
     * @param oldStatus Stary status
     */
    void onStatusUpdate(GameStatus oldStatus);

    /**
     * Otrzymano event
     * @param event Event z gry
     */
    void onEvent(Event event);
}
