package pl.fratik.chinczyk.server.socket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import pl.fratik.chinczyk.game.Chinczyk;
import pl.fratik.chinczyk.game.Rule;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.OpCode;
import pl.fratik.chinczyk.socket.messages.client.*;
import pl.fratik.chinczyk.socket.messages.client.game.*;

import java.util.List;
import java.util.Map;

public class MessageDecoder extends ReplayingDecoder<MessageDecoder.State> {
    private final Map<Integer, Chinczyk> games;
    private OpCode opCode;
    private int gameCode;
    private GameOpCode gameOpCode;

    public MessageDecoder(Map<Integer, Chinczyk> games) {
        super(State.READ_OPCODE);
        this.games = games;
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
        ClientMessage message;
        switch (opCode) {
            case IDENTIFY:
                message = IdentifyMessage.deserialize(in);
                break;
            case HOST:
                message = HostMessage.deserialize(in);
                break;
            case CONNECT:
                message = ConnectMessage.deserialize(in);
                break;
            case INFO:
                message = InfoMessage.deserialize(in);
                break;
            case DISCONNECT:
                message = DisconnectMessage.deserialize(in);
                break;
            case GAME:
                Chinczyk game = games.get(gameCode);
                if (game == null) throw new IllegalArgumentException("Nie znaleziono gry!");
                switch (gameOpCode) {
                    case JOIN:
                        message = JoinGameMessage.deserialize(gameCode, in);
                        break;
                    case SET_PLAYER_STATUS:
                        message = SetPlayerStatusGameMessage.deserialize(gameCode, in);
                        break;
                    case SET_NAME:
                        message = SetNameGameMessage.deserialize(gameCode, in);
                        break;
                    case SET_LANGUAGE:
                        message = SetLanguageGameMessage.deserialize(gameCode, in);
                        break;
                    case LEAVE:
                        message = LeaveGameMessage.deserialize(gameCode, in);
                        break;
                    case SET_RULES:
                        message = SetRulesGameMessage.deserialize(gameCode, in);
                        break;
                    case START_GAME:
                        message = StartGameMessage.deserialize(gameCode, in);
                        break;
                    case ROLL_DICE:
                        message = RollDiceGameMessage.deserialize(gameCode, game.getRules().contains(Rule.DEV_MODE), in);
                        break;
                    case MOVE_PIECE:
                        message = MovePieceGameMessage.deserialize(gameCode, in);
                        break;
                    default:
                        throw new IllegalArgumentException("otrzymano serwerowy OpCode");
                }
                break;
            default:
                throw new IllegalArgumentException("otrzymano serwerowy OpCode");
        }
        out.add(message);
        opCode = null;
        gameOpCode = null;
        gameCode = -1;
        checkpoint(State.READ_OPCODE);
    }

    protected enum State {
        READ_OPCODE,
        READ_GAMECODE,
        READ_GAME_OPCODE,
        READ_CONTENT
    }
}
