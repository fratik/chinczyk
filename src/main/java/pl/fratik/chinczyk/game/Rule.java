package pl.fratik.chinczyk.game;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

public enum Rule {
    /**
     * Szybka gra - wygrywa pierwszy pionek w polu domowym
     */
    QUICK_GAME(1),
    /**
     * Jeden rzut - wyłącza 3 rzuty kostką kiedy brak pionów na mapie
     */
    ONE_ROLL(1<<1),
    /**
     * Zakaz przeskakiwania - jeżeli próbujesz przejść przez pion innego gracza, a nie możesz go zbić nie zezwalaj na ruch
     */
    NO_PASSES(1<<2),
    /**
     * Wymuś bicie - jeżeli jeden z pionków ma bicie, nie pozwalaj na ruch innym
     */
    FORCE_CAPTURE(1<<3),
    /**
     * Szybkie rzuty - kiedy jest tylko jeden (lub 0) ruchów dozwolonych, wykonaj je automatycznie
     */
    FAST_ROLLS(1<<4),
    /**
     * Tryb dewelopera - wybierasz rzut kostką
     */
    DEV_MODE(1<<5, true),
    /**
     * 1 opuszcza start - wyrzucenie 1 (tak jak 6) opuszcza pole startowe
     */
    ONE_LEAVES_HOME(1<<6),
    /**
     * Dłuższy timeout - kiedy jesteś kurwa w pociągu do Bydgoszczy i net nie wyrabia
     */
    LONGER_TIMEOUT(1<<7);

    @Getter private final int flag;
    @Getter private final boolean cheat;

    Rule(int flag) {
        this(flag, false);
    }

    Rule(int flag, boolean cheat) {
        this.flag = flag;
        this.cheat = cheat;
    }

    public static Set<Rule> fromRaw(long raw) {
        EnumSet<Rule> rules = EnumSet.noneOf(Rule.class);
        for (Rule r : values()) if ((raw & r.flag) == r.flag) rules.add(r);
        return rules;
    }

    public static long toRaw(Set<Rule> set) {
        long raw = 0;
        for (Rule r : set) raw |= r.flag;
        return raw;
    }
}
