package pl.fratik.chinczyk.game;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public interface Piece {
    /**
     * Gracz, który jest właścicielem tego pionka
     * @return Gracz, nigdy null
     */
    @NotNull Player getPlayer();

    /**
     * Indeks pionka — jego stały identyfikator
     * @return Indeks pionka, od 0 do 3
     */
    @Range(from = 0, to = 3) int getIndex();

    /**
     * Relatywna pozycja pionka, gdzie 0 - pole startowe; 1-40 - pola na planszy; 41-44 - pola końcowe
     * @return Pozycja pionka, od 0 do 44
     */
    @Range(from = 0, to = 44) int getPosition();

    default String getBoardPosition() {
        return getBoardPosition(getPosition());
    }

    default String getBoardPosition(int position) {
        if (position == 0) return String.valueOf(getPlayer().getPlace().name().toLowerCase().charAt(0)) + (getIndex() + 1);
        if (position > 40) return String.valueOf(getPlayer().getPlace().name().toLowerCase().charAt(0)) + (position - 36);
        int offset = getPlayer().getPlace().getOffset();
        int pos = (position + offset) % 40;
        return String.valueOf(pos != 0 ? pos : 40);
    }
}
