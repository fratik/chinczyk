package pl.fratik.chinczyk.game;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@Data
public class Event {
    private final Type type;
    private final Player player;
    private final Integer rolled;
    private final Piece piece;
    private final Piece piece2;
    private final Boolean fastRolled;

    public Event(Type type, Player player, Integer rolled, Piece piece, Piece piece2, Boolean fastRolled) {
        this.type = type;
        this.player = player;
        this.rolled = rolled;
        this.piece = checkType(type, Type.LEFT_START, Type.MOVE, Type.ENTERED_HOME, Type.THROW) ?
                Objects.requireNonNull(piece) : null;
        this.piece2 = checkType(type, Type.THROW) ? Objects.requireNonNull(piece2) : null;
        this.fastRolled = checkType(true, type, Type.LEFT_START, Type.MOVE, Type.ENTERED_HOME, Type.THROW) ? fastRolled : null;
    }

    private static boolean checkType(Type type, Type... allowedTypes) {
        return checkType(false, type, allowedTypes);
    }

    private static boolean checkType(boolean allowNull, Type type, Type... allowedTypes) {
        if (allowNull && type == null) return true;
        return allowedTypes != null && Arrays.asList(allowedTypes).contains(type);
    }

    public void serialize(ByteBuf buf) throws Exception {
        buf.writeByte(type.getRaw());
        buf.writeByte(player == null ? 0 : player.getPlace().getOffset());
        buf.writeByte(rolled == null ? 0 : rolled);
        buf.writeByte(piece == null ? 0 : piece.getIndex());
        if (piece2 != null) {
            buf.writeByte(piece2.getPlayer().getPlace().getOffset());
            buf.writeByte(piece2.getIndex());
        } else buf.writeByte(0);
        buf.writeBoolean(fastRolled != null && fastRolled);
    }

    public static Event deserialize(ByteBuf buf, Map<Place, ? extends Player> playerMap) throws Exception {
        Type type = Type.getByRaw(buf.readUnsignedByte());
        short place = buf.readUnsignedByte();
        byte rolled = buf.readByte();
        byte pieceIndex = buf.readByte();
        byte player2Place = buf.readByte();
        byte piece2Index;
        if (player2Place != 0) piece2Index = buf.readByte();
        else piece2Index = 0;
        boolean fastRolled = buf.readBoolean();
        Player p, p2;
        if (place == 0) p = null;
        else p = playerMap.get(Place.getByOffset(place));
        Piece piece;
        if (p != null) piece = p.getPieces()[pieceIndex];
        else piece = null;
        Piece piece2;
        if (player2Place != 0) p2 = playerMap.get(Place.getByOffset(player2Place));
        else p2 = null;
        if (p2 != null) piece2 = p2.getPieces()[piece2Index];
        else piece2 = null;
        return new Event(type, p, rolled == 0 ? null : (int) rolled, piece, piece2, fastRolled);
    }

    public enum Type {
        GAME_START(1),
        LEFT_START(2),
        MOVE(3),
        THROW(4),
        ENTERED_HOME(5),
        WON(6),
        LEFT_GAME(7);

        @Getter private final int raw;

        Type(int raw) {
            this.raw = raw;
        }

        public static Type getByRaw(int raw) {
            for (Type t : Type.values()) {
                if (t.getRaw() == raw) return t;
            }
            return null;
        }
    }
}
