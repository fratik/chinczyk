package pl.fratik.chinczyk.client.game;

import pl.fratik.chinczyk.game.Chinczyk;
import pl.fratik.chinczyk.game.Language;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.game.Rule;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.Future;

public interface ClientChinczyk extends Chinczyk {
    /**
     * Czas gry z poprzednich zapisów w sekundach
     * @see #calculateFullGameDuration()
     * @return Czas gry, w sekundach
     */
    long getGameDuration();

    /**
     * Data startu gry, null jeżeli nie rozpoczęta
     * @return Data startu gry
     */
    Instant getStart();

    /**
     * Data końca gry, null jeżeli w toku
     * @return Data końca gry
     */
    Instant getEnd();

    /**
     * Liczy pełny czas gry w sekundach
     * @return Czas gry w sekundach, -1 jeżeli gra nie rozpoczęta
     */
    default long calculateFullGameDuration() {
        if (getStart() == null) return -1;
        Instant end;
        if (getEnd() == null) end = Instant.now();
        else end = getEnd();
        return getGameDuration() + (end.getEpochSecond() - getStart().getEpochSecond());
    }

    /**
     * Data otrzymania nagłówka, null jeżeli jeszcze nie przyszedł
     * @return Data otrzymania nagłówka
     */
    Instant getLastHeaderReceived();

    /**
     * Dołącza do gry
     * @param place Miejsce, na które gracz ma dołączyć
     * @param id ID gracza - null jeżeli gracz anonimowy
     * @param name Nazwa gracza - wymagana jeżeli ID jest null
     * @return Future sygnalizujący wiadomość zwrotną od serwera
     */
    Future<ClientPlayer> joinGame(Place place, Long id, String name);

    /**
     * Zmienia język dla gry
     * @param language Nowy język
     * @return Future sygnalizujący wiadomość zwrotną od serwera
     */
    Future<Void> changeLanguage(Language language);

    /**
     * Zmień zasady dla gry
     * @param rules Nowe zasady
     * @return Future sygnalizujący wiadomość zwrotną od serwera
     */
    Future<Void> setRules(Set<Rule> rules);

    /**
     * Wystartuj grę
     * @return Future sygnalizujący wiadomość zwrotną od serwera
     */
    Future<Void> startGame();
}
