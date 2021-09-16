package pl.fratik.chinczyk.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import pl.fratik.chinczyk.client.exceptions.ConnectGameException;
import pl.fratik.chinczyk.client.exceptions.DisconnectedSocketException;
import pl.fratik.chinczyk.client.exceptions.SocketException;
import pl.fratik.chinczyk.client.game.*;
import pl.fratik.chinczyk.client.socket.*;
import pl.fratik.chinczyk.socket.OpCode;
import pl.fratik.chinczyk.socket.PlayerDeserializer;
import pl.fratik.chinczyk.socket.messages.client.ConnectMessage;
import pl.fratik.chinczyk.socket.messages.server.ConnectResponseMessage;
import pl.fratik.chinczyk.socket.messages.server.DisconnectResponseMessage;
import pl.fratik.chinczyk.socket.messages.server.ServerMessage;
import pl.fratik.chinczyk.socket.messages.server.game.ServerGameMessage;

import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class ChinczykClient {
    private final EventLoopGroup workerGroup;
    private final Set<ChinczykListenerWrapper> listeners = new HashSet<>();
    private final SocketAddress socketAddress;
    private final Map<Integer, ClientChinczykImpl> games;
    private final PlayerDeserializer<ClientPlayerImpl> playerDeserializer;
    private final ClientSocketChannel channel;

    public ChinczykClient(SocketAddress socketAddress) throws SocketException {
        this.socketAddress = socketAddress;
        games = new HashMap<>();
        playerDeserializer = new ClientPlayerDeserializer(games);
        workerGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(ClientSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new MessageDecoder(games, playerDeserializer),
                        new MessageEncoder(), new MessageHandler(ChinczykClient.this));
            }
        });
        try {
            ChannelFuture f = b.connect(socketAddress).sync();
            channel = (ClientSocketChannel) f.channel();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SocketException("Nie udało się połączyć", e);
        } catch (Exception e) {
            throw new SocketException("Nie udało się połączyć", e);
        }
    }

    public void registerListener(ChinczykListener listener) {
        listeners.add(new ChinczykListenerWrapper(listener));
    }

    public boolean unregisterListener(ChinczykListener listener) {
        return listeners.removeIf(w -> Objects.equals(w.listener, listener));
    }

    public void handleRead(ServerMessage msg) {
        switch (msg.getOp()) {
            case CONNECT: {
                ConnectResponseMessage message = (ConnectResponseMessage) msg;
                if (message.getStatus() != ConnectResponseMessage.Status.SUCCESS) return;
                if (games.get(message.getGameCode()) != null)
                    throw new IllegalStateException("połączono do istniejacej gry " + message.getGameCode());
                ClientChinczykImpl chin;
                games.put(message.getGameCode(), chin = new ClientChinczykImpl(channel, message.getGameCode(), message.isPrivate(), false));
                listeners.forEach(listener -> listener.onConnect(chin));
                break;
            }
            case DISCONNECT: {
                DisconnectResponseMessage message = (DisconnectResponseMessage) msg;
                games.remove(message.getGameCode());
                break;
            }
            case GAME: {
                ServerGameMessage message = (ServerGameMessage) msg;
                ClientChinczykImpl chinczyk = games.get(message.getGameCode());
                if (chinczyk == null)
                    throw new IllegalStateException("otrzymano wiadomość dla nieistniejącej gry " + message.getGameCode());
                chinczyk.handleEvent(message);
            }
        }
    }

    public Future<ClientChinczyk> connectGame(int gameCode) {
        CompletableFuture<ClientChinczyk> future = new CompletableFuture<>();
        channel.createTask(new Task(m -> m.getOp() == OpCode.CONNECT &&
                ((ConnectResponseMessage) m).getGameCode() == gameCode, msg -> {
            ConnectResponseMessage m = (ConnectResponseMessage) msg;
            if (!m.getStatus().isSuccess()) future.completeExceptionally(new ConnectGameException(m.getStatus()));
            else future.complete(games.get(m.getGameCode()));
        }, () -> future.completeExceptionally(new DisconnectedSocketException())));
        channel.writeAndFlush(new ConnectMessage(gameCode));
        return future;
    }
}
