package pl.fratik.chinczyk.client.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pl.fratik.chinczyk.game.Piece;

@Getter
@Setter
@AllArgsConstructor
public class ClientPiece implements Piece {
    private final ClientPlayerImpl player;
    private final int index;
    private int position;
}
