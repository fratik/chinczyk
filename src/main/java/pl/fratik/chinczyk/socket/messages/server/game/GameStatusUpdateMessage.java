package pl.fratik.chinczyk.socket.messages.server.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.AbortReason;
import pl.fratik.chinczyk.game.GameStatus;
import pl.fratik.chinczyk.socket.GameOpCode;

import java.util.Objects;

import static pl.fratik.chinczyk.util.StreamUtil.readString;
import static pl.fratik.chinczyk.util.StreamUtil.writeString;

@Getter
public class GameStatusUpdateMessage extends ServerGameMessage {
    private final GameStatus gameStatus;
    private final AbortReason abortReason;

    public GameStatusUpdateMessage(int gameCode, GameStatus gameStatus, AbortReason abortReason) {
        super(gameCode, GameOpCode.GAME_STATUS_UPDATE);
        this.gameStatus = gameStatus;
        this.abortReason = gameStatus == GameStatus.CANCELLED ? Objects.requireNonNull(abortReason) : null;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        writeString(buf, gameStatus.name());
        if (abortReason != null) writeString(buf, abortReason.name());
    }

    public static GameStatusUpdateMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        GameStatus gameStatus = GameStatus.valueOf(readString(buf));
        AbortReason abortReason;
        if (gameStatus == GameStatus.CANCELLED) abortReason = AbortReason.valueOf(readString(buf));
        else abortReason = null;
        return new GameStatusUpdateMessage(gameCode, gameStatus, abortReason);
    }
}
