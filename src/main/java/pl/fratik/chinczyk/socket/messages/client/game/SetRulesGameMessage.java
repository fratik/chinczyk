package pl.fratik.chinczyk.socket.messages.client.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Rule;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.server.game.ServerGameMessage;
import pl.fratik.chinczyk.socket.messages.server.game.SetRulesGameResponseMessage;

import java.util.Set;

@Getter
public class SetRulesGameMessage extends ClientGameMessage {
    private final Set<Rule> rules;

    public SetRulesGameMessage(int gameCode, Set<Rule> rules) {
        super(gameCode, GameOpCode.SET_RULES);
        this.rules = rules;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeLong(Rule.toRaw(rules));
    }

    public static SetRulesGameMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        return new SetRulesGameMessage(gameCode, Rule.fromRaw(buf.readLong()));
    }

    @Override
    public ServerGameMessage getInvalidGameMessage() {
        return new SetRulesGameResponseMessage(gameCode, SetRulesGameResponseMessage.Status.INVALID_GAME);
    }
}
