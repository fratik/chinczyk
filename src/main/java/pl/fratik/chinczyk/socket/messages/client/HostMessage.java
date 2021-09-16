package pl.fratik.chinczyk.socket.messages.client;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import pl.fratik.chinczyk.game.Language;
import pl.fratik.chinczyk.socket.OpCode;

import java.util.Objects;

import static pl.fratik.chinczyk.util.StreamUtil.readString;
import static pl.fratik.chinczyk.util.StreamUtil.writeString;

@Getter
public class HostMessage extends ClientMessage {
    private final boolean privateMode;
    private final Long id;
    private final Language language;

    public HostMessage(boolean privateMode, Long id, Language language) {
        super(OpCode.HOST);
        this.privateMode = privateMode;
        this.id = id;
        this.language = Objects.requireNonNull(language);
    }

    @Override
    protected void serializeContent(ByteBuf buf) throws Exception {
        buf.writeBoolean(privateMode);
        buf.writeLong(id == null ? 0 : id);
        writeString(buf, language.name());
    }

    public static HostMessage deserialize(ByteBuf buf) throws Exception {
        boolean privateMode = buf.readBoolean();
        long id = buf.readLong();
        Language language = Language.valueOf(readString(buf));
        return new HostMessage(privateMode, id == 0 ? null : id, language);
    }
}
