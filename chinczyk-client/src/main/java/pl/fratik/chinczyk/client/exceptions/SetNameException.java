package pl.fratik.chinczyk.client.exceptions;

import lombok.Getter;
import pl.fratik.chinczyk.socket.messages.server.game.SetNameGameResponseMessage;
import pl.fratik.chinczyk.socket.messages.server.game.StartGameResponseMessage;

public class SetNameException extends RuntimeException {
    @Getter private final SetNameGameResponseMessage.Status status;

    public SetNameException(SetNameGameResponseMessage.Status status) {
        this.status = status;
    }

    public SetNameException(String message, SetNameGameResponseMessage.Status status) {
        super(message);
        this.status = status;
    }
}
