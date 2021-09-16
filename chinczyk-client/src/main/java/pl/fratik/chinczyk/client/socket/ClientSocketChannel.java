package pl.fratik.chinczyk.client.socket;

import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import pl.fratik.chinczyk.socket.messages.server.ServerMessage;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ClientSocketChannel extends NioSocketChannel {
    @Getter private final Set<Task> tasks;

    public ClientSocketChannel() {
        tasks = new HashSet<>();
    }

    public void createTask(Predicate<ServerMessage> condition, Consumer<ServerMessage> callback, Runnable disconnect) {
        createTask(new Task(condition, callback, disconnect));
    }

    public void createTask(Task task) {
        tasks.add(task);
    }
}
