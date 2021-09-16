package pl.fratik.chinczyk.client.exceptions;

import lombok.Getter;
import pl.fratik.chinczyk.socket.messages.server.game.JoinGameResponseMessage;

public class PlayerJoinException extends RuntimeException {
    @Getter private final JoinGameResponseMessage.Status status;

    public PlayerJoinException(JoinGameResponseMessage.Status status) {
        this.status = status;
    }

    public PlayerJoinException(String message, JoinGameResponseMessage.Status status) {
        super(message);
        this.status = status;
    }
}
