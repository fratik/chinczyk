package pl.fratik.chinczyk.client.exceptions;

import lombok.Getter;
import pl.fratik.chinczyk.socket.messages.server.game.SetPlayerStatusGameResponseMessage;

public class SetStatusException extends RuntimeException {
    @Getter private final SetPlayerStatusGameResponseMessage.Status status;

    public SetStatusException(SetPlayerStatusGameResponseMessage.Status status) {
        this.status = status;
    }

    public SetStatusException(String message, SetPlayerStatusGameResponseMessage.Status status) {
        super(message);
        this.status = status;
    }
}
