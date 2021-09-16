package pl.fratik.chinczyk.client.exceptions;

import lombok.Getter;
import pl.fratik.chinczyk.socket.messages.server.game.SetRulesGameResponseMessage;

public class SetRulesException extends RuntimeException {
    @Getter private final SetRulesGameResponseMessage.Status status;

    public SetRulesException(SetRulesGameResponseMessage.Status status) {
        this.status = status;
    }

    public SetRulesException(String message, SetRulesGameResponseMessage.Status status) {
        super(message);
        this.status = status;
    }
}
