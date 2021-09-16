package pl.fratik.chinczyk.game;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static pl.fratik.chinczyk.util.StreamUtil.writeString;

public interface Player {
    /**
     * Wyświetlana nazwa gracza, max 13 znaków
     * @return Nazwa, nigdy null
     */
    @NotNull String getPlayerName();

    /**
     * Wewnętrzne ID gracza, null jeżeli gracz anonimowy
     * @return ID gracza, możliwy null
     */
    @Nullable Long getId();

    /**
     * Stała pozycja gracza
     * @return Pozycję gracza, nigdy null
     */
    @NotNull Place getPlace();

    /**
     * Array 4 pionków
     * @return 4 pionki, nigdy null
     */
    @NotNull Piece[] getPieces();

    /**
     * Język gracza
     * @return Język, nigdy null
     */
    @NotNull Language getLanguage();

    /**
     * Status gracza
     * @return Status, nigdy null
     */
    @NotNull PlayerStatus getStatus();

    default boolean isReady() {
        return getStatus() == PlayerStatus.READY;
    }

    default void serialize(ByteBuf buf) {
        buf.writeByte(getPlace().getOffset());
        if (getId() == null) buf.writeLong(0);
        else buf.writeLong(getId());
        writeString(buf, getPlayerName());
        writeString(buf, getLanguage().name());
        writeString(buf, getStatus().name());
    }
}

