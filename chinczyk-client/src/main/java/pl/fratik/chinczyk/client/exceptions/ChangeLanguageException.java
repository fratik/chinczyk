package pl.fratik.chinczyk.client.exceptions;

import lombok.Getter;
import pl.fratik.chinczyk.socket.messages.server.game.SetLanguageGameResponseMessage;

public class ChangeLanguageException extends RuntimeException {
    @Getter private final SetLanguageGameResponseMessage.Status status;

    public ChangeLanguageException(SetLanguageGameResponseMessage.Status status) {
        this.status = status;
    }

    public ChangeLanguageException(String message, SetLanguageGameResponseMessage.Status status) {
        super(message);
        this.status = status;
    }
}
