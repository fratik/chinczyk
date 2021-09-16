package pl.fratik.chinczyk.client.exceptions;

import lombok.Getter;
import pl.fratik.chinczyk.socket.messages.server.game.LeaveGameResponseMessage;
import pl.fratik.chinczyk.socket.messages.server.game.MovePieceGameResponseMessage;

public class MovePieceException extends RuntimeException {
    @Getter private final MovePieceGameResponseMessage.Status status;

    public MovePieceException(MovePieceGameResponseMessage.Status status) {
        this.status = status;
    }

    public MovePieceException(String message, MovePieceGameResponseMessage.Status status) {
        super(message);
        this.status = status;
    }
}
