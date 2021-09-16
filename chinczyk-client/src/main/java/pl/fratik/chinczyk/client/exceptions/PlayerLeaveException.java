package pl.fratik.chinczyk.client.exceptions;

import lombok.Getter;
import pl.fratik.chinczyk.socket.messages.server.game.LeaveGameResponseMessage;

public class PlayerLeaveException extends RuntimeException {
    @Getter private final LeaveGameResponseMessage.Status status;

    public PlayerLeaveException(LeaveGameResponseMessage.Status status) {
        this.status = status;
    }

    public PlayerLeaveException(String message, LeaveGameResponseMessage.Status status) {
        super(message);
        this.status = status;
    }
}
