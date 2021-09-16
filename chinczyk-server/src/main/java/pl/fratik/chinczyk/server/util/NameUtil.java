package pl.fratik.chinczyk.server.util;

public class NameUtil {
    public static boolean checkName(String name) {
        return name.length() <= 13;
    }
}
