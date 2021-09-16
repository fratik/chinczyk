package pl.fratik.chinczyk.client.socket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import pl.fratik.chinczyk.client.game.ClientChinczykImpl;
import pl.fratik.chinczyk.client.game.ClientPlayerImpl;
import pl.fratik.chinczyk.game.Chinczyk;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.OpCode;
import pl.fratik.chinczyk.socket.PlayerDeserializer;
import pl.fratik.chinczyk.socket.messages.server.*;
import pl.fratik.chinczyk.socket.messages.server.game.*;

import java.util.List;
import java.util.Map;

public class MessageDecoder extends ReplayingDecoder<MessageDecoder.State> {
    private final Map<Integer, ClientChinczykImpl> games;
    private final PlayerDeserializer<ClientPlayerImpl> playerDeserializer;
    private OpCode opCode;
    private int gameCode;
    private GameOpCode gameOpCode;

    public MessageDecoder(Map<Integer, ClientChinczykImpl> games, PlayerDeserializer<ClientPlayerImpl> playerDeserializer) {
        super(State.READ_OPCODE);
        this.games = games;
        this.playerDeserializer = playerDeserializer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case READ_OPCODE:
                readOpCode(ctx, in, out);
                break;
            case READ_GAMECODE:
                readGameCode(ctx, in, out);
                break;
            case READ_GAME_OPCODE:
                readGameOpCode(ctx, in, out);
                break;
            case READ_CONTENT:
                readContent(ctx, in, out);
                break;
        }
    }

    private void readOpCode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if ((opCode = OpCode.decode(in.readByte())) == OpCode.GAME) checkpoint(State.READ_GAMECODE);
        else checkpoint(State.READ_CONTENT);
    }

    private void readGameCode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        gameCode = in.readInt();
        checkpoint(State.READ_GAME_OPCODE);
    }

    private void readGameOpCode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        gameOpCode = GameOpCode.decode(in.readByte());
        checkpoint(State.READ_CONTENT);
    }

    private void readContent(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ServerMessage message;
        switch (opCode) {
            case IDENTIFY:
                message = IdentifyResponseMessage.deserialize(in);
                break;
            case HOST:
                message = HostResponseMessage.deserialize(in);
                break;
            case CONNECT:
                message = ConnectResponseMessage.deserialize(in);
                break;
            case INFO:
                message = InfoResponseMessage.deserialize(in);
                break;
            case DISCONNECT:
                message = DisconnectResponseMessage.deserialize(in);
                break;
            case GAME:
                switch (gameOpCode) {
                    case JOIN:
                        message = JoinGameResponseMessage.deserialize(gameCode, playerDeserializer, in);
                        break;
                    case LEAVE:
                        message = LeaveGameResponseMessage.deserialize(gameCode, in);
                        break;
                    case SET_PLAYER_STATUS:
                        message = SetPlayerStatusGameResponseMessage.deserialize(gameCode, in);
                        break;
                    case SET_NAME:
                        message = SetNameGameResponseMessage.deserialize(gameCode, in);
                        break;
                    case SET_LANGUAGE:
                        message = SetLanguageGameResponseMessage.deserialize(gameCode, in);
                        break;
                    case SET_RULES:
                        message = SetRulesGameResponseMessage.deserialize(gameCode, in);
                        break;
                    case START_GAME:
                        message = StartGameResponseMessage.deserialize(gameCode, in);
                        break;
                    case ROLL_DICE:
                        message = RollDiceGameResponseMessage.deserialize(gameCode, in);
                        break;
                    case MOVE_PIECE:
                        message = MovePieceGameResponseMessage.deserialize(gameCode, in);
                        break;
                    case TURN:
                        message = TurnGameMessage.deserialize(gameCode, in);
                        break;
                    case SYNC_END:
                        message = SyncEndGameMessage.deserialize(gameCode, in);
                        break;
                    case GAME_STATUS_UPDATE:
                        message = GameStatusUpdateMessage.deserialize(gameCode, in);
                        break;
                    case HEADER:
                        message = HeaderGameMessage.deserialize(gameCode, in, playerDeserializer);
                        break;
                    case EVENT:
                        Chinczyk chinczyk = games.get(gameCode);
                        if (chinczyk == null) throw new IllegalArgumentException("otrzymano event dla nieznanej gry?");
                        message = EventGameMessage.deserialize(gameCode, in, chinczyk.getPlayersMap());
                        break;
                    default:
                        throw new IllegalArgumentException("otrzymano nieprawidłowy OP gry");
                }
                break;
            default:
                throw new IllegalArgumentException("otrzymano nieprawidłowy OP");
        }
        out.add(message);
        opCode = null;
        gameOpCode = null;
        gameCode = -1;
        checkpoint(State.READ_OPCODE);
    }

    public enum State {
        READ_OPCODE,
        READ_GAMECODE,
        READ_GAME_OPCODE,
        READ_CONTENT
    }
}
