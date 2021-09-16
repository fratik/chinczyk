package pl.fratik.chinczyk.client.exceptions;

import lombok.Getter;
import pl.fratik.chinczyk.socket.messages.server.game.RollDiceGameResponseMessage;

public class RollDiceException extends RuntimeException {
    @Getter private final RollDiceGameResponseMessage.Status status;

    public RollDiceException(RollDiceGameResponseMessage.Status status) {
        this.status = status;
    }

    public RollDiceException(String message, RollDiceGameResponseMessage.Status status) {
        super(message);
        this.status = status;
    }
}
