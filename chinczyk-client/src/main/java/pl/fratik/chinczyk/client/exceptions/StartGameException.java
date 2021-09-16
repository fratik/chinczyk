package pl.fratik.chinczyk.client.exceptions;

import lombok.Getter;
import pl.fratik.chinczyk.socket.messages.server.game.StartGameResponseMessage;

public class StartGameException extends RuntimeException {
    @Getter private final StartGameResponseMessage.Status status;

    public StartGameException(StartGameResponseMessage.Status status) {
        this.status = status;
    }

    public StartGameException(String message, StartGameResponseMessage.Status status) {
        super(message);
        this.status = status;
    }
}
