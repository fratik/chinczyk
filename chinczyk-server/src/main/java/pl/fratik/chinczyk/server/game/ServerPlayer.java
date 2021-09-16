package pl.fratik.chinczyk.server.game;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.fratik.chinczyk.game.*;
import pl.fratik.chinczyk.server.database.PlayerData;

@Getter
@Setter
@ToString(exclude = "pieces")
public class ServerPlayer implements Player {
    @Getter(AccessLevel.PROTECTED) private final ServerChinczyk game;
    private final Place place;
    private final PlayerData playerData;
    private final Piece[] pieces = new Piece[4];
    private Language language;
    private PlayerStatus status = PlayerStatus.JOINED;
    private String name;

    public ServerPlayer(ServerChinczyk game, Place place, PlayerData playerData, String name) {
        this.game = game;
        this.place = place;
        this.playerData = playerData;
        this.name = name;
        initPieces();
    }

    private void initPieces() {
        for (int i = 0; i < pieces.length; i++)
            pieces[i] = new ServerPiece(this, i);
    }

    public void setStatus(PlayerStatus status) {
        setStatus(status, false);
    }

    public void setStatus(PlayerStatus status, boolean force) {
        if (this.status == status) return;
        if (!force && !canChangeStatus(status))
            throw new IllegalArgumentException("nie można zmienić statusu z " + this.status + " na " + status + "!");
        this.status = status;
    }

    public boolean canChangeStatus(PlayerStatus status) {
        if (this.status == status) return true;
        if (this.status == PlayerStatus.JOINED) return status == PlayerStatus.READY || status == PlayerStatus.LEFT;
        if (this.status == PlayerStatus.READY) return status == PlayerStatus.JOINED || status == PlayerStatus.PLAYING || status == PlayerStatus.LEFT;
        if (this.status == PlayerStatus.PLAYING) return status == PlayerStatus.LEFT;
        if (this.status == PlayerStatus.LEFT) return false;
        throw new IllegalStateException("Nieoczekiwany status!");
    }

    @Override
    public @NotNull String getPlayerName() {
        return playerData == null ? name : playerData.getName();
    }

    @Override
    public @Nullable Long getId() {
        return playerData == null ? null : playerData.getId();
    }
}
