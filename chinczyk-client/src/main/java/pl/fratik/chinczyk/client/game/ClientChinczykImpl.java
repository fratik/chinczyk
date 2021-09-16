package pl.fratik.chinczyk.client.game;

import lombok.Getter;
import pl.fratik.chinczyk.client.exceptions.*;
import pl.fratik.chinczyk.client.socket.ClientSocketChannel;
import pl.fratik.chinczyk.game.*;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.OpCode;
import pl.fratik.chinczyk.socket.messages.client.game.JoinGameMessage;
import pl.fratik.chinczyk.socket.messages.client.game.SetLanguageGameMessage;
import pl.fratik.chinczyk.socket.messages.client.game.SetRulesGameMessage;
import pl.fratik.chinczyk.socket.messages.client.game.StartGameMessage;
import pl.fratik.chinczyk.socket.messages.server.ServerMessage;
import pl.fratik.chinczyk.socket.messages.server.game.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Predicate;

public class ClientChinczykImpl implements ClientChinczyk {
    final ClientSocketChannel channel;
    @Getter private final int gameCode;
    @Getter private final boolean isPrivate;
    private boolean syncing;
    @Getter private long gameID;
    @Getter private long hosterID;
    private final Map<Place, ClientPlayerImpl> players;
    @Getter private final Set<Rule> rules;
    @Getter private boolean cheats;
    @Getter private Language language;
    @Getter private GameStatus status;
    @Getter private long gameDuration;
    @Getter private Instant start;
    @Getter private Instant end;
    @Getter private Instant lastHeaderReceived;
    private final EventStorage eventStorage;
    private final Set<GameListenerWrapper> listeners;

    public ClientChinczykImpl(ClientSocketChannel channel, int gameCode, boolean isPrivate, boolean isHosting) {
        this.channel = channel;
        this.gameCode = gameCode;
        this.isPrivate = isPrivate;
        if (isHosting) status = GameStatus.WAITING_FOR_PLAYERS;
        players = new EnumMap<>(Place.class);
        rules = EnumSet.noneOf(Rule.class);
        eventStorage = new EventStorage() {
            @Override
            protected void onEvent(Event event) {
                listeners.forEach(l -> l.onEvent(event));
            }
        };
        listeners = new HashSet<>();
        syncing = true;
    }

    public void handleHeader(Header header) {
        if (syncing) {
            gameID = header.getId();
            hosterID = header.getHoster();
            language = header.getLanguage();
            for (Player player : header.getPlayers())
                players.put(player.getPlace(), (ClientPlayerImpl) player);
        }
        rules.clear();
        rules.addAll(header.getRules());
        cheats = header.isCheats();
        gameDuration = header.getGameDuration();
        start = header.getStart() == 0 ? null : Instant.ofEpochMilli(header.getStart());
        end = header.getEnd() == 0 ? null : Instant.ofEpochMilli(header.getEnd());
        lastHeaderReceived = Instant.ofEpochMilli(header.getTimestamp());
    }

    @Override
    public Map<Place, ClientPlayerImpl> getPlayersMap() {
        return players;
    }

    public void handleEvent(ServerGameMessage message) {
        if (message.getGameCode() != gameCode) throw new IllegalArgumentException("jak do tego doszło nie wiem");
        switch (message.getGameOp()) {
            case GAME_STATUS_UPDATE: {
                GameStatus oldStatus = status;
                status = ((GameStatusUpdateMessage) message).getGameStatus();
                if (!syncing) listeners.forEach(l -> l.onStatusUpdate(oldStatus));
                break;
            }
            case SYNC_END: {
                syncing = false;
                listeners.forEach(GameListenerWrapper::onReady);
                break;
            }
            case HEADER: {
                handleHeader(((HeaderGameMessage) message).getHeader());
                break;
            }
            case EVENT: {
                handleEvent(((EventGameMessage) message).getEvent());
                break;
            }
        }
    }

    private void handleEvent(Event event) {
        if (status != GameStatus.IN_PROGRESS) throw new IllegalStateException("nieprawidłowy stan gry");
        if (event.getType() != null) {
            switch (event.getType()) {
                case LEFT_START: {
                    ((ClientPiece) event.getPiece()).setPosition(1);
                    break;
                }
                case MOVE:
                case ENTERED_HOME: {
                    ((ClientPiece) event.getPiece()).setPosition(event.getPiece().getPosition() + event.getRolled());
                    break;
                }
                case THROW: {
                    ((ClientPiece) event.getPiece()).setPosition(event.getPiece().getPosition() + event.getRolled());
                    ((ClientPiece) event.getPiece2()).setPosition(0);
                    break;
                }
                case LEFT_GAME: {
                    ((ClientPlayerImpl) event.getPlayer()).setStatusInternal(PlayerStatus.LEFT);
                    ((ClientPlayerImpl) event.getPlayer()).initPieces();
                    break;
                }
            }
        }
        eventStorage.add(event);
    }

    private Predicate<ServerMessage> getFilter(GameOpCode op) {
        return getFilter(op, null);
    }

    Predicate<ServerMessage> getFilter(GameOpCode op, Predicate<ServerMessage> additional) {
        if (additional == null) additional = unused -> true;
        Predicate<ServerMessage> finalAdditional = additional;
        return m -> m.getOp() == OpCode.GAME && ((ServerGameMessage) m).getGameCode() == gameCode &&
                ((ServerGameMessage) m).getGameOp() == op && finalAdditional.test(m);
    }

    @Override
    public Future<ClientPlayer> joinGame(Place place, Long id, String name) {
        CompletableFuture<ClientPlayer> future = new CompletableFuture<>();
        channel.createTask(getFilter(GameOpCode.JOIN, m -> ((JoinGameResponseMessage) m).getPlace() == place), msg -> {
            JoinGameResponseMessage m = (JoinGameResponseMessage) msg;
            if (!m.getStatus().isSuccess()) future.completeExceptionally(new PlayerJoinException(m.getStatus()));
            else future.complete((ClientPlayer) m.getPlayer());
        }, () -> future.completeExceptionally(new DisconnectedSocketException()));
        channel.writeAndFlush(new JoinGameMessage(gameCode, place, id, name));
        return future;
    }

    @Override
    public Future<Void> changeLanguage(Language language) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        channel.createTask(getFilter(GameOpCode.SET_LANGUAGE, m -> ((SetLanguageGameResponseMessage) m).getPlace() == null), msg -> {
            SetLanguageGameResponseMessage m = (SetLanguageGameResponseMessage) msg;
            if (!m.getStatus().isSuccess()) future.completeExceptionally(new ChangeLanguageException(m.getStatus()));
            else future.complete(null);
        }, () -> future.completeExceptionally(new DisconnectedSocketException()));
        channel.writeAndFlush(new SetLanguageGameMessage(gameCode, null, language));
        return future;
    }

    @Override
    public Future<Void> setRules(Set<Rule> rules) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        channel.createTask(getFilter(GameOpCode.SET_RULES), msg -> {
            SetRulesGameResponseMessage m = (SetRulesGameResponseMessage) msg;
            if (!m.getStatus().isSuccess()) future.completeExceptionally(new SetRulesException(m.getStatus()));
            else future.complete(null);
        }, () -> future.completeExceptionally(new DisconnectedSocketException()));
        channel.writeAndFlush(new SetRulesGameMessage(gameCode, rules));
        return future;
    }

    @Override
    public Future<Void> startGame() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        channel.createTask(getFilter(GameOpCode.START_GAME), msg -> {
            StartGameResponseMessage m = (StartGameResponseMessage) msg;
            if (!m.getStatus().isSuccess()) future.completeExceptionally(new StartGameException(m.getStatus()));
            else future.complete(null);
        }, () -> future.completeExceptionally(new DisconnectedSocketException()));
        channel.writeAndFlush(new StartGameMessage(gameCode));
        return future;
    }
}
