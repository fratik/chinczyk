package pl.fratik.chinczyk.server.game;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.fratik.chinczyk.game.Piece;
import pl.fratik.chinczyk.game.Rule;

import java.util.Arrays;

@Getter
@ToString
public class ServerPiece implements Piece {
    private final ServerPlayer player;
    private final int index;
    @Setter(AccessLevel.PROTECTED) private int position = 0;

    public ServerPiece(ServerPlayer player, int index) {
        this.player = player;
        this.index = index;
    }

    // true - można bić, false - pole zajęte, null - pole wolne
    public Boolean canCapture() {
        ServerChinczyk game = player.getGame();
        String nextPosition;
        if (position == 0) {
            if (!game.hasRolledExit()) return null;
            nextPosition = getBoardPosition(1);
        }
        else nextPosition = getBoardPosition(position + game.getRolled());
        Piece pieceAt = game.getPieceAt(nextPosition);
        if (pieceAt != null) return !pieceAt.getPlayer().equals(player);
        return null;
    }

    public boolean canMove() {
        ServerChinczyk game = player.getGame();
        if (game.getRolled() == null) return false;
        if (position + game.getRolled() > 44) return false; // jeśli przekroczona ilość pól + strefy końcowej, nie można
        Boolean captureable = canCapture();
        if (captureable != null) return captureable;
        if (game.getRules().contains(Rule.NO_PASSES)) {
            int rolledLoop = game.getRolled();
            do {
                String boardPosition;
                if (position == 0) boardPosition = getBoardPosition(1);
                else boardPosition = getBoardPosition(position + rolledLoop);
                Piece pieceAt = game.getPieceAt(boardPosition);
                if (pieceAt != null && !pieceAt.getPlayer().equals(player)) {
                    if (rolledLoop == game.getRolled()) break; // na wszelki, choć nie powinno do tego dojść
                    return false;
                }
            } while (--rolledLoop >= 1 && position != 0); //jeżeli pos. jest 0, wykonaj pętle tylko raz bo więcej nie ma sensu
        }
        if (game.getRules().contains(Rule.FORCE_CAPTURE) && Arrays.stream(player.getPieces()).filter(p -> !p.equals(this))
                .anyMatch(p -> ((ServerPiece) p).canCapture() == Boolean.TRUE)) return false;
        return position != 0 || game.hasRolledExit(); // pole czyste, można iść
    }
}
