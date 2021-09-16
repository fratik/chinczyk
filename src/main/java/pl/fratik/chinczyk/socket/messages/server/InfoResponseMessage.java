package pl.fratik.chinczyk.socket.messages.server;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.GameStatus;
import pl.fratik.chinczyk.game.Language;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.game.Rule;
import pl.fratik.chinczyk.socket.OpCode;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import static pl.fratik.chinczyk.util.StreamUtil.readString;
import static pl.fratik.chinczyk.util.StreamUtil.writeString;

@Getter
public class InfoResponseMessage extends ServerMessage {
    private final Status status;
    private final int gameCode;
    private final boolean privateMode;
    private final Set<Place> availablePlaces;
    private final int readyCount;
    private final GameStatus gameStatus;
    private final Language language;
    private final Set<Rule> rules;

    public InfoResponseMessage(Status status) {
        super(OpCode.INFO);
        if (status == Status.SUCCESS) throw new IllegalArgumentException("Dla sukcesu użyj pełnego konstruktora!");
        this.status = status;
        this.gameCode = 0;
        this.privateMode = false;
        this.availablePlaces = null;
        this.readyCount = 0;
        this.gameStatus = null;
        this.language = null;
        this.rules = null;
    }

    public InfoResponseMessage(Status status, int gameCode, boolean privateMode, Set<Place> availablePlaces, int readyCount, GameStatus gameStatus, Language language, Set<Rule> rules) {
        super(OpCode.INFO);
        this.status = status;
        if (status == Status.SUCCESS) {
            this.gameCode = gameCode;
            this.privateMode = privateMode;
            this.availablePlaces = Collections.unmodifiableSet(availablePlaces);
            this.readyCount = readyCount;
            this.gameStatus = Objects.requireNonNull(gameStatus);
            this.language = Objects.requireNonNull(language);
            this.rules = Collections.unmodifiableSet(rules);
        } else {
            this.gameCode = 0;
            this.privateMode = false;
            this.availablePlaces = null;
            this.readyCount = 0;
            this.gameStatus = null;
            this.language = null;
            this.rules = null;
        }
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeByte(status.getCode());
        if (status != Status.SUCCESS) return;
        buf.writeInt(gameCode);
        buf.writeBoolean(privateMode);
        buf.writeByte(availablePlaces.size());
        for (Place place : availablePlaces) buf.writeByte(place.getOffset());
        buf.writeByte(readyCount);
        writeString(buf, gameStatus.name());
        writeString(buf, language.name());
        buf.writeLong(Rule.toRaw(rules));
    }

    public static InfoResponseMessage deserialize(ByteBuf buf) throws Exception {
        Status status = Status.getByCode(buf.readByte());
        if (status != Status.SUCCESS) return new InfoResponseMessage(status);
        int gameCode = buf.readInt();
        boolean privateMode = buf.readBoolean();
        Set<Place> availablePlaces = EnumSet.noneOf(Place.class);
        byte placeCount = buf.readByte();
        for (int i = 0; i < placeCount; i++)
            availablePlaces.add(Place.getByOffset(buf.readByte()));
        int readyCount = buf.readByte();
        GameStatus gameStatus = GameStatus.valueOf(readString(buf));
        Language language = Language.valueOf(readString(buf));
        Set<Rule> rules = Rule.fromRaw(buf.readLong());
        return new InfoResponseMessage(status, gameCode, privateMode, availablePlaces, readyCount, gameStatus, language, rules);
    }

    @Getter
    public enum Status implements ResponseStatus {
        SUCCESS(0),
        INVALID_GAME(1);

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
