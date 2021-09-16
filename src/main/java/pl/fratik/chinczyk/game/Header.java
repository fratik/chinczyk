package pl.fratik.chinczyk.game;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import pl.fratik.chinczyk.socket.PlayerDeserializer;
import pl.fratik.chinczyk.util.StreamUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Data
public class Header {
    private static final byte[] HEADER = new byte[] {0x21, 0x37};
    public static final byte CURRENT_VERSION = 5;
    /**
     * Wersja nagłówka
     */
    private final byte version;
    /**
     * ID gry
     */
    private final long id;
    /**
     * Osoba hostująca grę
     */
    private final long hoster;
    /**
     * Język gry
     */
    private final Language language;
    /**
     * Lista graczy
     */
    private final Set<? extends Player> players;
    /**
     * Lista zasad
     */
    private final Set<Rule> rules;
    /**
     * Włączone oszustwa?
     */
    private final boolean cheats;
    /**
     * Poprzednia długość gry w sekundach (aby wyliczyć długość, weź gameDuration*1000 + (end - start))
     */
    private final long gameDuration; //uint
    /**
     * Start gry od jej załadowania (UNIX timestamp w ms)
     */
    private final long start;
    /**
     * Data końca gry lub 0 (UNIX timestamp w ms)
     */
    private final long end;
    /**
     * UNIX timestamp w ms zrzutu nagłówka
     */
    private final long timestamp;

    public void serialize(ByteBuf buf) throws Exception {
        buf.writeBytes(HEADER);
        buf.writeByte(CURRENT_VERSION);
        buf.writeLong(id);
        buf.writeLong(hoster);
        StreamUtil.writeString(buf, language.name());
        buf.writeByte(players.size());
        for (Player player : players) player.serialize(buf);
        buf.writeLong(Rule.toRaw(rules));
        buf.writeBoolean(cheats);
        buf.writeInt((int) gameDuration);
        buf.writeLong(start);
        buf.writeLong(end);
        buf.writeLong(timestamp);
    }

    public static Header deserialize(int gameCode, ByteBuf buf, PlayerDeserializer<?> deserializer) throws Exception {
        byte[] header = new byte[HEADER.length];
        buf.readBytes(header);
        if (Arrays.equals(header, HEADER)) throw new IllegalArgumentException("nieprawidłowy nagłówek");
        byte version = buf.readByte();
        if (version != CURRENT_VERSION) throw new IllegalArgumentException("nieprawidłowa wersja");
        long id = buf.readLong();
        long hoster = buf.readLong();
        Language language = Language.valueOf(StreamUtil.readString(buf));
        Player[] players = new Player[buf.readUnsignedByte()];
        for (int i = 0; i < players.length; i++) players[i] = deserializer.deserialize(gameCode, buf);
        Set<Rule> rules = Rule.fromRaw(buf.readLong());
        boolean cheats = buf.readBoolean();
        long gameDuration = buf.readUnsignedInt();
        long start = buf.readLong();
        long end = buf.readLong();
        long save = buf.readLong();
        return new Header(version, id, hoster, language, new HashSet<>(Arrays.asList(players)),
                rules, cheats, gameDuration, start, end, save);
    }
}
