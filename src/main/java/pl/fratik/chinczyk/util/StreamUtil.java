package pl.fratik.chinczyk.util;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StreamUtil {
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private StreamUtil() {}

    public static void writeString(ByteBuf buf, String str) {
        if (str.length() >= 0xFFFF) throw new IllegalArgumentException("za długi tekst");
        buf.writeShort(str.length());
        buf.writeBytes(str.getBytes(CHARSET));
    }

    public static String readString(ByteBuf buf) {
        int len = buf.readUnsignedShort();
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return new String(bytes, CHARSET);
    }

    public static String setToString(Set<String> set) {
        StringBuilder string = new StringBuilder();
        for (String e : set) string.append(e.replace(",", "\\,")).append(',');
        string.setLength(string.length() - 1);
        return string.toString();
    }

    public static Set<String> setFromString(String str) {
        return new HashSet<>(Arrays.asList(str.split("(?<!\\\\)\\|")));
    }
}
