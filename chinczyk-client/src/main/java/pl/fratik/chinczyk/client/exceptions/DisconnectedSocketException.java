package pl.fratik.chinczyk.client.exceptions;

public class DisconnectedSocketException extends SocketException {
    public DisconnectedSocketException() {
        super();
    }

    public DisconnectedSocketException(String message) {
        super(message);
    }

    public DisconnectedSocketException(String message, Throwable cause) {
        super(message, cause);
    }

    public DisconnectedSocketException(Throwable cause) {
        super(cause);
    }
}
