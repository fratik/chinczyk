package pl.fratik.chinczyk.client.socket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.fratik.chinczyk.socket.messages.server.ServerMessage;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
@AllArgsConstructor
public class Task {
    private final Predicate<ServerMessage> condition;
    private final Consumer<ServerMessage> callback;
    private final Runnable disconnect;
}
