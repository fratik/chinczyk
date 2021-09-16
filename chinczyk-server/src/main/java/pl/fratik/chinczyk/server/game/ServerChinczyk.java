package pl.fratik.chinczyk.server.game;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.AccessLevel;
import lombok.Getter;
import pl.fratik.chinczyk.game.*;
import pl.fratik.chinczyk.server.ChinczykServer;
import pl.fratik.chinczyk.server.database.DatabaseConnection;
import pl.fratik.chinczyk.server.database.DatabaseManager;
import pl.fratik.chinczyk.server.database.PlayerData;
import pl.fratik.chinczyk.server.util.NameUtil;
import pl.fratik.chinczyk.server.util.NamedThreadFactory;
import pl.fratik.chinczyk.socket.messages.client.game.*;
import pl.fratik.chinczyk.socket.messages.server.ConnectResponseMessage;
import pl.fratik.chinczyk.socket.messages.server.DisconnectResponseMessage;
import pl.fratik.chinczyk.socket.messages.server.ServerMessage;
import pl.fratik.chinczyk.socket.messages.server.game.*;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ServerChinczyk implements Chinczyk {
    private static final ThreadLocal<Boolean> isTimeout = ThreadLocal.withInitial(() -> false);
    private final DatabaseManager dbm;
    @Getter private final long gameID;
    private final PlayerData hoster;
    @Getter private final boolean isPrivate;
    @Getter private Language language;
    private final Map<Integer, Chinczyk> games;
    private final Map<Place, ServerPlayer> players;
    @Getter private final Set<Rule> rules;
    private boolean cheats;
    private final Set<Channel> connections;
    private final ScheduledExecutorService executor;
    private final ReentrantLock lock;
    private final EventStorage eventStorage;
    private final Random random;
    private final long randomSeed;
    private long randomSeq;
    @Getter private GameStatus status;
    @Getter private int gameCode;
    private ScheduledFuture<?> timeoutFuture;
    @Getter private Instant start;
    @Getter private Instant end;
    private long gameDuration;
    private Place turn;
    private Player winner;
    private int rollCounter;
    @Getter(AccessLevel.PROTECTED) private Integer rolled;

    private ServerChinczyk(long gameID, PlayerData hoster, boolean isPrivate, Language language, Map<Integer, Chinczyk> games, DatabaseManager dbm) {
        this.gameID = gameID;
        this.hoster = Objects.requireNonNull(hoster);
        this.isPrivate = isPrivate;
        this.language = Objects.requireNonNull(language);
        this.games = games;
        this.dbm = dbm;
        connections = new HashSet<>();
        status = GameStatus.WAITING_FOR_PLAYERS;
        players = new EnumMap<>(Place.class);
        rules = EnumSet.noneOf(Rule.class);
        int i = 1;
        do {
            if (i++ >= 500) throw new IllegalStateException("Nie udało się wygenerować kodu gry po " + i + " próbach");
            gameCode = ChinczykServer.RANDOM.nextInt(1000000);
        } while (games.containsKey(gameCode));
        executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("ServerChinczyk-" + gameID));
        timeoutFuture = executor.schedule(this::timeout, 5, TimeUnit.MINUTES);
        lock = new ReentrantLock();
        games.put(gameCode, this);
        eventStorage = new EventStorage() {
            @Override
            protected void onEvent(Event event) {
                if (event.getType() == Event.Type.WON)
                    end = Instant.now();
                broadcast(new EventGameMessage(gameCode, event));
            }
        };
        random = new Random((randomSeed = ChinczykServer.RANDOM.nextLong()));
    }

    private void broadcastHeader() {
        broadcast(new HeaderGameMessage(gameCode, generateHeader()));
    }

    public Header generateHeader() {
        return new Header(Header.CURRENT_VERSION, gameID, hoster.getId(), language, getPlayers(), rules, cheats, gameDuration,
                start == null ? 0 : start.toEpochMilli(), end == null ? 0 : end.toEpochMilli(), Instant.now().toEpochMilli());
    }

    private void makeTurn() {
        lock.lock();
        try {
            if (checkWin()) {
                if (eventStorage.getLastEvent() == null)
                    throw new IllegalStateException("eventStorage.getLastEvent() jest null przy wygranej?");
                eventStorage.add(new Event(Event.Type.WON, winner, null, null, null, false));
                updateStatus(GameStatus.ENDED, false);
                return;
            }
            if (turn == null) turn = getHosterPlayer().getPlace();
            else if (!isPlaying(players.get(turn)) || (rules.contains(Rule.ONE_ROLL) || rollCounter++ >= 2 ||
                    Arrays.stream(players.get(turn).getPieces()).anyMatch(p -> p.getPosition() != 0)) &&
                    (rolled == null || rolled != 6)) {
                turn = Place.getNextPlace(turn, players.entrySet().stream()
                        .filter(p -> isPlaying(p.getValue())).map(Map.Entry::getKey).collect(Collectors.toSet()));
                rollCounter = 0;
            }
            rolled = null;
            if (isTimeout.get() == Boolean.FALSE && timeoutFuture != null && !timeoutFuture.isCancelled() && !timeoutFuture.cancel(true)) return;
            timeoutFuture = executor.schedule(this::timeout, rules.contains(Rule.LONGER_TIMEOUT) ? 15 : 1, TimeUnit.MINUTES);
            broadcast(new TurnGameMessage(gameCode, turn));
        } finally {
            lock.unlock();
        }
    }

    private boolean checkWin() {
        if (readyPlayerCount() == 1) {
            Optional<ServerPlayer> p = players.values().stream().filter(this::isPlaying).findAny();
            if (p.isPresent()) {
                winner = p.get();
                return true;
            }
            throw new IllegalStateException("ready == 1, ale nie ma wygranego");
        }
        for (Player p : players.values()) {
            if (rules.contains(Rule.QUICK_GAME)) {
                if (Arrays.stream(p.getPieces()).anyMatch(piece -> piece.getPosition() >= 41)) {
                    winner = p;
                    return true;
                }
            } else {
                if (Arrays.stream(p.getPieces()).allMatch(piece -> piece.getPosition() >= 41)) {
                    winner = p;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPlaying(ServerPlayer serverPlayer) {
        GameStatus gameStatus = status;
        if (gameStatus == GameStatus.WAITING || gameStatus == GameStatus.WAITING_FOR_PLAYERS)
            return serverPlayer.getStatus() != PlayerStatus.LEFT;
        return serverPlayer.getStatus() == PlayerStatus.PLAYING ||
                ((gameStatus == GameStatus.CANCELLED || gameStatus == GameStatus.ERRORED)
                        && serverPlayer.getStatus() == PlayerStatus.READY);
    }

    private void startGame() {
        lock.lock();
        try {
            start = Instant.now();
            status = GameStatus.IN_PROGRESS;
            for (ServerPlayer p : players.values()) p.setStatus(PlayerStatus.PLAYING);
            broadcastHeader();
            eventStorage.add(new Event(Event.Type.GAME_START, null, null, null, null, null));
            makeTurn();
        } finally {
            lock.unlock();
        }
    }

    private void timeout() {
        isTimeout.set(true);
        try {
            lock.lockInterruptibly(); //timeouty mogą być anulowane przed załozeniem locka
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        try {
            GameStatus status = this.status;
            if (status == GameStatus.WAITING_FOR_PLAYERS || status == GameStatus.WAITING) {
                updateStatus(GameStatus.CANCELLED, AbortReason.TIMEOUT, false);
                end();
                return;
            }
            if (status == GameStatus.IN_PROGRESS) {
                ServerPlayer player = players.get(turn);
                player.setStatus(PlayerStatus.LEFT);
            }
        } finally {
            lock.unlock();
        }
    }

    private void updateStatus(GameStatus status, boolean flush) {
        updateStatus(status, null, flush);
    }

    private void updateStatus(GameStatus status, AbortReason reason, boolean flush) {
        if (this.status == status) return;
        lock.lock();
        try {
            broadcast(new GameStatusUpdateMessage(gameCode, status, reason), flush);
            this.status = status;
            if (status == GameStatus.CANCELLED || status == GameStatus.ERRORED || status == GameStatus.ENDED) end();
        } finally {
            lock.unlock();
        }
    }

    private void end() {
        if (status == GameStatus.WAITING || status == GameStatus.WAITING_FOR_PLAYERS || status == GameStatus.IN_PROGRESS)
            throw new IllegalStateException("nieprawidłowy status gry");
        games.remove(getGameCode());
        for (Channel con : new HashSet<>(connections)) removeConnection(con, true);
        if (status == GameStatus.ENDED) {
            // todo policz statystyki
        }
    }

    private void broadcast(ServerMessage message) {
        broadcast(message, true);
    }

    private void broadcast(ServerMessage message, boolean flush) {
        for (Channel connection : connections) {
            connection.write(message);
            if (flush) connection.flush();
        }
    }

    public static ServerChinczyk createNewGame(DatabaseManager dbm, PlayerData hoster, boolean isPrivate, Language language, Map<Integer, Chinczyk> games) throws SQLException {
        try (DatabaseConnection con = dbm.getConnection()) {
            //todo generacja ID z bazy + staty
            return new ServerChinczyk(2137, hoster, isPrivate, language, games, dbm);
        }
    }

    public static ServerChinczyk loadGame() {
        //todo
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public long getHosterID() {
        return hoster.getId();
    }

    @Override
    public Map<Place, ServerPlayer> getPlayersMap() {
        return players;
    }

    public void addConnection(Channel channel, boolean sendMessage) {
        connections.add(channel);
        if (sendMessage) {
            channel.write(new ConnectResponseMessage(gameCode, ConnectResponseMessage.Status.SUCCESS, isPrivate));
            channel.write(new GameStatusUpdateMessage(gameCode, status, null));
            channel.write(new HeaderGameMessage(gameCode, generateHeader()));
            eventStorage.forEach(channel::write);
            channel.writeAndFlush(new SyncEndGameMessage(gameCode));
        }
    }

    public void removeConnection(Channel channel, boolean sendMessage) {
        connections.remove(channel);
        if (sendMessage) channel.writeAndFlush(new DisconnectResponseMessage(gameCode, DisconnectResponseMessage.Status.SUCCESS));
    }

    public boolean isConnected(Channel channel) {
        return connections.contains(channel);
    }

    public void handleEvent(ChannelHandlerContext ctx, ClientGameMessage msg) throws Exception {
        switch (msg.getGameOp()) {
            case JOIN: {
                JoinGameMessage message = (JoinGameMessage) msg;
                PlayerData pdata;
                if (message.getId() != null) {
                    try (DatabaseConnection con = dbm.getConnection()) {
                        pdata = con.getPlayerData(message.getId());
                    }
                    if (pdata == null) {
                        ctx.writeAndFlush(new JoinGameResponseMessage(gameCode, message.getPlace(),
                                JoinGameResponseMessage.Status.INVALID_USER, null));
                        return;
                    }
                } else pdata = null;
                lock.lock();
                try {
                    if (getHosterPlayer() == null) {
                        ctx.writeAndFlush(new JoinGameResponseMessage(gameCode, message.getPlace(),
                                JoinGameResponseMessage.Status.HOST_FIRST, null));
                        return;
                    }
                    if (players.get(message.getPlace()) != null) {
                        ctx.writeAndFlush(new JoinGameResponseMessage(gameCode, message.getPlace(),
                                JoinGameResponseMessage.Status.PLACE_OCCUPIED, null));
                        return;
                    }
                    boolean invalidName;
                    if (message.getName() == null) invalidName = pdata == null;
                    else invalidName = !NameUtil.checkName(message.getName());
                    if (invalidName) {
                        ctx.writeAndFlush(new JoinGameResponseMessage(gameCode, message.getPlace(),
                                JoinGameResponseMessage.Status.INVALID_NAME, null));
                        return;
                    }
                    ServerPlayer player = new ServerPlayer(this, message.getPlace(), pdata, message.getName());
                    players.put(message.getPlace(), player);
                    broadcast(new JoinGameResponseMessage(gameCode, message.getPlace(),
                            JoinGameResponseMessage.Status.SUCCESS, player));
                } finally {
                    lock.unlock();
                }
                break;
            }
            case SET_PLAYER_STATUS: {
                SetPlayerStatusGameMessage message = (SetPlayerStatusGameMessage) msg;
                lock.lock();
                try {
                    ServerPlayer p = players.get(message.getPlace());
                    if (p == null) {
                        ctx.writeAndFlush(new SetPlayerStatusGameResponseMessage(gameCode, message.getPlace(),
                                SetPlayerStatusGameResponseMessage.Status.INVALID_PLAYER, null));
                        return;
                    }
                    if (!p.canChangeStatus(message.getPlayerStatus())) {
                        ctx.writeAndFlush(new SetPlayerStatusGameResponseMessage(gameCode, message.getPlace(),
                                SetPlayerStatusGameResponseMessage.Status.UNEXPECTED_STATUS, null));
                        return;
                    }
                    p.setStatus(message.getPlayerStatus());
                    ServerGameMessage resp = new SetPlayerStatusGameResponseMessage(gameCode, p.getPlace(),
                            SetPlayerStatusGameResponseMessage.Status.SUCCESS, p.getStatus());
                    if (status == GameStatus.WAITING_FOR_PLAYERS || status == GameStatus.WAITING) {
                        broadcast(resp, false);
                        if (readyPlayerCount() == players.size()) updateStatus(GameStatus.WAITING, false);
                        else updateStatus(GameStatus.WAITING_FOR_PLAYERS, false);
                        connections.forEach(Channel::flush);
                    } else ctx.writeAndFlush(resp);
                } finally {
                    lock.unlock();
                }
                break;
            }
            case SET_NAME: {
                SetNameGameMessage message = (SetNameGameMessage) msg;
                lock.lock();
                try {
                    ServerPlayer p = players.get(message.getPlace());
                    if (p == null) {
                        ctx.writeAndFlush(new SetNameGameResponseMessage(gameCode, message.getPlace(),
                                SetNameGameResponseMessage.Status.INVALID_PLAYER, null));
                        return;
                    }
                    if (!NameUtil.checkName(message.getName())) {
                        ctx.writeAndFlush(new SetNameGameResponseMessage(gameCode, p.getPlace(),
                                SetNameGameResponseMessage.Status.INVALID_NAME, null));
                        return;
                    }
                    p.setName(message.getName());
                    broadcast(new SetNameGameResponseMessage(gameCode, p.getPlace(),
                            SetNameGameResponseMessage.Status.SUCCESS, p.getName()));
                } finally {
                    lock.unlock();
                }
                break;
            }
            case SET_LANGUAGE: {
                SetLanguageGameMessage message = (SetLanguageGameMessage) msg;
                if (message.getLanguage() == null) {
                    ctx.writeAndFlush(new SetLanguageGameResponseMessage(gameCode, message.getPlace(),
                            SetLanguageGameResponseMessage.Status.INVALID_LANGUAGE, null));
                    return;
                }
                if (message.getPlace() == null) { // zmiana języka dla gry
                    language = message.getLanguage();
                    ctx.writeAndFlush(new SetLanguageGameResponseMessage(gameCode, null,
                            SetLanguageGameResponseMessage.Status.SUCCESS, language));
                    broadcastHeader();
                    return;
                }
                ServerPlayer p = players.get(message.getPlace());
                if (p == null) {
                    ctx.writeAndFlush(new SetLanguageGameResponseMessage(gameCode, message.getPlace(),
                            SetLanguageGameResponseMessage.Status.INVALID_PLAYER, null));
                    return;
                }
                p.setLanguage(message.getLanguage());
                broadcast(new SetLanguageGameResponseMessage(gameCode, p.getPlace(),
                        SetLanguageGameResponseMessage.Status.SUCCESS, language));
                break;
            }
            case SET_RULES: {
                SetRulesGameMessage message = (SetRulesGameMessage) msg;
                rules.clear();
                rules.addAll(message.getRules());
                if (rules.stream().anyMatch(Rule::isCheat)) cheats = true;
                ctx.write(new SetRulesGameResponseMessage(gameCode, SetRulesGameResponseMessage.Status.SUCCESS));
                broadcastHeader();
                break;
            }
            case LEAVE: {
                LeaveGameMessage message = (LeaveGameMessage) msg;
                lock.lock();
                try {
                    ServerPlayer p = players.get(message.getPlace());
                    if (p == null) {
                        ctx.writeAndFlush(new LeaveGameResponseMessage(gameCode, message.getPlace(),
                                LeaveGameResponseMessage.Status.INVALID_PLAYER));
                        return;
                    }
                    if (status == GameStatus.IN_PROGRESS) {
                        p.setStatus(PlayerStatus.LEFT, true);
                        eventStorage.add(new Event(Event.Type.LEFT_GAME, p, null, null, null, null));
                    } else {
                        players.remove(p.getPlace());
                        broadcast(new LeaveGameResponseMessage(gameCode, p.getPlace(),
                                LeaveGameResponseMessage.Status.SUCCESS));
                    }
                } finally {
                    lock.unlock();
                }
                break;
            }
            case START_GAME: {
                StartGameMessage message = (StartGameMessage) msg;
                if (status == GameStatus.IN_PROGRESS) {
                    ctx.writeAndFlush(new StartGameResponseMessage(gameCode, StartGameResponseMessage.Status.ALREADY_STARTED));
                    return;
                }
                lock.lock();
                try {
                    if (!canStartGame()) {
                        ctx.writeAndFlush(new StartGameResponseMessage(gameCode, StartGameResponseMessage.Status.NOT_READY));
                        return;
                    }
                    ctx.writeAndFlush(new StartGameResponseMessage(gameCode, StartGameResponseMessage.Status.SUCCESS));
                    startGame();
                } finally {
                    lock.unlock();
                }
                break;
            }
            case ROLL_DICE: {
                RollDiceGameMessage message = (RollDiceGameMessage) msg;
                if (message.getPlace() != turn) {
                    ctx.writeAndFlush(new RollDiceGameResponseMessage(gameCode, message.getPlace(),
                            RollDiceGameResponseMessage.Status.NOT_TURN, null, null));
                    return;
                }
                if (rolled != null) {
                    ctx.writeAndFlush(new RollDiceGameResponseMessage(gameCode, message.getPlace(),
                            RollDiceGameResponseMessage.Status.ALREADY_ROLLED, null, null));
                    return;
                }
                ServerPlayer player = players.get(turn);
                if (rules.contains(Rule.DEV_MODE)) {
                    if (message.getRolled() == 0) rolled = roll();
                    else rolled = message.getRolled();
                }
                ServerPiece pieceToMove = null;
                byte canMove = 0;
                for (int i = 0; i < player.getPieces().length; i++) {
                    ServerPiece piece = (ServerPiece) player.getPieces()[i];
                    if (!piece.canMove()) continue;
                    if (canMove == 0) pieceToMove = piece;
                    else pieceToMove = null;
                    canMove |= 1 << (3 - i);
                }
                broadcast(new RollDiceGameResponseMessage(gameCode, turn,
                        RollDiceGameResponseMessage.Status.SUCCESS, rolled, canMove));
                if (rules.contains(Rule.FAST_ROLLS)) {
                    if (canMove == 0) movePiece(null, true);
                    else movePiece(pieceToMove, true);
                    makeTurn();
                }
                break;
            }
            case MOVE_PIECE: {
                MovePieceGameMessage message = (MovePieceGameMessage) msg;
                if (message.getPlace() != turn) {
                    ctx.writeAndFlush(new MovePieceGameResponseMessage(gameCode, message.getPlace(),
                            message.getPieceIndex(), MovePieceGameResponseMessage.Status.NOT_TURN));
                    return;
                }
                ServerPlayer player = players.get(turn);
                if (rolled == null) {
                    ctx.writeAndFlush(new MovePieceGameResponseMessage(gameCode, message.getPlace(),
                            message.getPieceIndex(), MovePieceGameResponseMessage.Status.NOT_ROLLED));
                    return;
                }
                ServerPiece p;
                if (message.getPieceIndex() == (byte) 0xFF) {
                    if (Arrays.stream(player.getPieces()).map(ServerPiece.class::cast).anyMatch(ServerPiece::canMove)) {
                        ctx.writeAndFlush(new MovePieceGameResponseMessage(gameCode, message.getPlace(),
                                message.getPieceIndex(), MovePieceGameResponseMessage.Status.INVALID_PIECE));
                        return;
                    }
                    p = null;
                } else {
                    if (message.getPieceIndex() < 0 || message.getPieceIndex() > 3) {
                        ctx.writeAndFlush(new MovePieceGameResponseMessage(gameCode, message.getPlace(),
                                message.getPieceIndex(), MovePieceGameResponseMessage.Status.INVALID_PIECE));
                        return;
                    }
                    p = (ServerPiece) player.getPieces()[message.getPieceIndex()];
                    if (!p.canMove()) {
                        ctx.writeAndFlush(new MovePieceGameResponseMessage(gameCode, message.getPlace(),
                                message.getPieceIndex(), MovePieceGameResponseMessage.Status.INVALID_PIECE));
                        return;
                    }
                }
                ctx.writeAndFlush(new MovePieceGameResponseMessage(gameCode, message.getPlace(),
                        message.getPieceIndex(), MovePieceGameResponseMessage.Status.SUCCESS));
                movePiece(p, false);
                break;
            }
            default:
                throw new IllegalArgumentException("otrzymano serwerowy OP");
        }
    }

    private void movePiece(ServerPiece pieceToMove, boolean fastRolled) {
        ServerPlayer player = players.get(turn);
        if (pieceToMove == null) {
            if (Arrays.stream(player.getPieces()).map(ServerPiece.class::cast).anyMatch(ServerPiece::canMove))
                throw new IllegalArgumentException("można ruszyć jakimś pionkiem");
            eventStorage.add(new Event(null, player, rolled, null, null, fastRolled));
            return;
        }
        if (player.getPieces()[pieceToMove.getIndex()] != pieceToMove) throw new IllegalArgumentException("niewłaściwy gracz");
        ServerPiece thrown = null;
        String nextPosition;
        int curPosition = pieceToMove.getPosition();
        if (curPosition == 0) nextPosition = pieceToMove.getBoardPosition(1);
        else nextPosition = pieceToMove.getBoardPosition(curPosition + rolled);
        for (Player p : players.values()) {
            for (Piece pi : p.getPieces()) {
                if (pi.getBoardPosition().equals(nextPosition) && !p.equals(pieceToMove.getPlayer())) {
                    ((ServerPiece) pi).setPosition(0);
                    thrown = (ServerPiece) pi;
                }
            }
        }
        if (pieceToMove.getPosition() == 0) pieceToMove.setPosition(1);
        else pieceToMove.setPosition(pieceToMove.getPosition() + rolled);
        if (thrown != null) eventStorage.add(new Event(Event.Type.THROW, player, rolled, pieceToMove, thrown, fastRolled));
        else {
            Event.Type type;
            if (pieceToMove.getBoardPosition()
                    .startsWith(String.valueOf(player.getPlace().name().toLowerCase().charAt(0))) &&
                    curPosition <= 40) type = Event.Type.ENTERED_HOME; //tylko jeżeli wejdzie na x5-x8 z <=40
            else if (curPosition == 0) type = Event.Type.LEFT_START;
            else type = Event.Type.MOVE;
            eventStorage.add(new Event(type, player, rolled, pieceToMove, null, fastRolled));
        }
    }

    private int roll() {
        randomSeq++;
        return random.nextInt(6) + 1;
    }

    private int readyPlayerCount() {
        return (int) players.values().stream().filter(Player::isReady).count();
    }

    private boolean canStartGame() {
        return status == GameStatus.WAITING;
    }

    private ServerPlayer getHosterPlayer() {
        return players.values().stream().filter(p -> p.getId() != null && hoster.getId() == p.getId()).findAny().orElse(null);
    }

    protected Piece getPieceAt(String boardPosition) {
        for (ServerPlayer p : players.values()) {
            if (!isPlaying(p)) continue;
            for (Piece piece : p.getPieces()) {
                if (piece.getBoardPosition().equals(boardPosition))
                    return piece;
            }
        }
        return null;
    }

    protected boolean hasRolledExit() { // 6 lub (1 lub 6) przy włączonej zasadzie wychodzenia przy 1
        if (rolled == null) return false;
        return rolled == 6 || (rolled == 1 && rules.contains(Rule.ONE_LEAVES_HOME));
    }
}
