package pl.fratik.chinczyk.socket.messages.server;

public interface ResponseStatus {
    byte getCode();

    default boolean isSuccess() {
        return getCode() == 0;
    }
}
