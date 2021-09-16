package pl.fratik.chinczyk.socket.messages.client.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Language;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.server.game.ServerGameMessage;
import pl.fratik.chinczyk.socket.messages.server.game.SetLanguageGameResponseMessage;

import static pl.fratik.chinczyk.util.StreamUtil.readString;
import static pl.fratik.chinczyk.util.StreamUtil.writeString;

@Getter
public class SetLanguageGameMessage extends ClientGameMessage {
    private final Place place;
    private final Language language;

    public SetLanguageGameMessage(int gameCode, Place place, Language language) {
        super(gameCode, GameOpCode.SET_LANGUAGE);
        this.place = place;
        this.language = language;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(place == null ? 0 : place.getOffset());
        writeString(buf, language.name());
    }

    public static SetLanguageGameMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        byte offset = buf.readByte();
        Place place;
        if (offset == 0) place = null;
        else place = Place.getByOffset(offset);
        Language lang = null;
        try {
            lang = Language.valueOf(readString(buf));
        } catch (IllegalArgumentException ignored) {}
        return new SetLanguageGameMessage(gameCode, place, lang);
    }

    @Override
    public ServerGameMessage getInvalidGameMessage() {
        return new SetLanguageGameResponseMessage(gameCode, place, SetLanguageGameResponseMessage.Status.INVALID_GAME, null);
    }
}
