package pl.fratik.chinczyk.client.exceptions;

public class SocketException extends Exception {
    public SocketException() {
        super();
    }

    public SocketException(String message) {
        super(message);
    }

    public SocketException(String message, Throwable cause) {
        super(message, cause);
    }

    public SocketException(Throwable cause) {
        super(cause);
    }
}
