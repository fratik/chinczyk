package pl.fratik.chinczyk.socket.messages.server.game;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Language;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.socket.GameOpCode;
import pl.fratik.chinczyk.socket.messages.server.ResponseStatus;

import static pl.fratik.chinczyk.util.StreamUtil.readString;
import static pl.fratik.chinczyk.util.StreamUtil.writeString;

@Getter
public class SetLanguageGameResponseMessage extends ServerGameMessage {
    private final Place place;
    private final Status status;
    private final Language language;

    public SetLanguageGameResponseMessage(int gameCode, Place place, Status status, Language language) {
        super(gameCode, GameOpCode.SET_LANGUAGE);
        this.place = place;
        this.status = status;
        this.language = language;
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(place == null ? 0 : place.getOffset());
        buf.writeByte(status.getCode());
        if (status == Status.SUCCESS) writeString(buf, language.name());
    }

    public static SetLanguageGameResponseMessage deserialize(int gameCode, ByteBuf buf) throws Exception {
        byte offset = buf.readByte();
        Place place;
        if (offset == 0) place = null;
        else place = Place.getByOffset(offset);
        Status status = Status.getByCode(buf.readByte());
        if (status != Status.SUCCESS) return new SetLanguageGameResponseMessage(gameCode, place, status, null);
        Language lang = Language.valueOf(readString(buf));
        return new SetLanguageGameResponseMessage(gameCode, place, status, lang);
    }

    @Getter
    public enum Status implements ResponseStatus {
        SUCCESS(0),
        INVALID_GAME(1),
        INVALID_PLAYER(2),
        INVALID_LANGUAGE(3); // nieprawidłowy język

        private final byte code;

        Status(int code) {
            this.code = (byte) code;
        }

        public static Status getByCode(byte b) {
            for (Status s : values()) {
                if (s.code == b) return s;
            }
            throw new IllegalArgumentException("nieprawidłowy status");
        }
    }
}
