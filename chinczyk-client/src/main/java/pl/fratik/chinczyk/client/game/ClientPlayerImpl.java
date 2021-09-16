package pl.fratik.chinczyk.client.game;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import pl.fratik.chinczyk.client.exceptions.*;
import pl.fratik.chinczyk.game.Language;
import pl.fratik.chinczyk.game.Piece;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.game.PlayerStatus;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.client.game.*;
import pl.fratik.chinczyk.socket.messages.server.game.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Getter
@ToString(exclude = "pieces")
public class ClientPlayerImpl implements ClientPlayer {
    @Getter(AccessLevel.PROTECTED) private final ClientChinczykImpl game;
    private final Place place;
    private final Long id;
    private final Piece[] pieces = new Piece[4];
    private String playerName;
    private Language language;
    private PlayerStatus status;

    public ClientPlayerImpl(ClientChinczykImpl game, Place place, Long id, String playerName, Language language) {
        this(game, place, id, playerName, language, PlayerStatus.JOINED);
    }

    public ClientPlayerImpl(ClientChinczykImpl game, Place place, Long id, String playerName, Language language, PlayerStatus status) {
        this.game = game;
        this.place = place;
        this.id = id;
        this.playerName = playerName;
        this.language = language;
        this.status = status;
        initPieces();
    }

    public void setStatusInternal(PlayerStatus status) {
        this.status = status;
    }

    public void setPlayerNameInternal(String playerName) {
        this.playerName = playerName;
    }

    public void setLanguageInternal(Language language) {
        this.language = language;
    }

    @Override
    public Future<Void> leaveGame() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        game.channel.createTask(game.getFilter(GameOpCode.LEAVE, m -> ((LeaveGameResponseMessage) m).getPlace() == place), msg -> {
            LeaveGameResponseMessage m = (LeaveGameResponseMessage) msg;
            if (!m.getStatus().isSuccess()) future.completeExceptionally(new PlayerLeaveException(m.getStatus()));
            else future.complete(null);
        }, () -> future.completeExceptionally(new DisconnectedSocketException()));
        game.channel.writeAndFlush(new LeaveGameMessage(game.getGameCode(), place));
        return future;
    }

    @Override
    public Future<Void> movePiece(Piece piece) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        game.channel.createTask(game.getFilter(GameOpCode.MOVE_PIECE, msg -> {
            MovePieceGameResponseMessage m = (MovePieceGameResponseMessage) msg;
            return m.getPlace() == place && m.getPieceIndex() == piece.getIndex();
        }), msg -> {
            MovePieceGameResponseMessage m = (MovePieceGameResponseMessage) msg;
            if (!m.getStatus().isSuccess()) future.completeExceptionally(new MovePieceException(m.getStatus()));
            else future.complete(null);
        }, () -> future.completeExceptionally(new DisconnectedSocketException()));
        game.channel.writeAndFlush(new MovePieceGameMessage(game.getGameCode(), place, (byte) (piece == null ? 0xFF : piece.getIndex())));
        return future;
    }

    @Override
    public Future<RollResult> rollDice(Integer rolled) {
        CompletableFuture<RollResult> future = new CompletableFuture<>();
        game.channel.createTask(game.getFilter(GameOpCode.ROLL_DICE, m -> ((RollDiceGameResponseMessage) m).getPlace() == place), msg -> {
            RollDiceGameResponseMessage m = (RollDiceGameResponseMessage) msg;
            if (!m.getStatus().isSuccess()) future.completeExceptionally(new RollDiceException(m.getStatus()));
            else future.complete(new RollResult(m.getRolled(), m.getCanMoveBitmap()));
        }, () -> future.completeExceptionally(new DisconnectedSocketException()));
        game.channel.writeAndFlush(new RollDiceGameMessage(game.getGameCode(), place, rolled));
        return future;
    }

    @Override
    public Future<Void> setLanguage(Language language) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        game.channel.createTask(game.getFilter(GameOpCode.SET_LANGUAGE, m -> ((SetLanguageGameResponseMessage) m).getPlace() == place), msg -> {
            SetLanguageGameResponseMessage m = (SetLanguageGameResponseMessage) msg;
            if (!m.getStatus().isSuccess()) future.completeExceptionally(new ChangeLanguageException(m.getStatus()));
            else future.complete(null);
        }, () -> future.completeExceptionally(new DisconnectedSocketException()));
        game.channel.writeAndFlush(new SetLanguageGameMessage(game.getGameCode(), place, language));
        return future;
    }

    @Override
    public Future<Void> setName(String name) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        game.channel.createTask(game.getFilter(GameOpCode.SET_NAME, m -> ((SetNameGameResponseMessage) m).getPlace() == place), msg -> {
            SetNameGameResponseMessage m = (SetNameGameResponseMessage) msg;
            if (!m.getStatus().isSuccess()) future.completeExceptionally(new SetNameException(m.getStatus()));
            else future.complete(null);
        }, () -> future.completeExceptionally(new DisconnectedSocketException()));
        game.channel.writeAndFlush(new SetNameGameMessage(game.getGameCode(), place, name));
        return future;
    }

    @Override
    public Future<Void> setStatus(PlayerStatus status) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        game.channel.createTask(game.getFilter(GameOpCode.SET_PLAYER_STATUS, m -> ((SetPlayerStatusGameResponseMessage) m).getPlace() == place), msg -> {
            SetPlayerStatusGameResponseMessage m = (SetPlayerStatusGameResponseMessage) msg;
            if (!m.getStatus().isSuccess()) future.completeExceptionally(new SetStatusException(m.getStatus()));
            else future.complete(null);
        }, () -> future.completeExceptionally(new DisconnectedSocketException()));
        game.channel.writeAndFlush(new SetPlayerStatusGameMessage(game.getGameCode(), place, status));
        return future;
    }

    protected void initPieces() {
        for (int i = 0; i < pieces.length; i++) {
            pieces[i] = new ClientPiece(this, i, 0);
        }
    }
}
