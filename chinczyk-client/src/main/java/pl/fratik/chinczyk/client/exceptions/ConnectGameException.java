package pl.fratik.chinczyk.client.exceptions;

import lombok.Getter;
import pl.fratik.chinczyk.socket.messages.server.ConnectResponseMessage;

public class ConnectGameException extends RuntimeException {
    @Getter private final ConnectResponseMessage.Status status;

    public ConnectGameException(ConnectResponseMessage.Status status) {
        this.status = status;
    }

    public ConnectGameException(String message, ConnectResponseMessage.Status status) {
        super(message);
        this.status = status;
    }
}
