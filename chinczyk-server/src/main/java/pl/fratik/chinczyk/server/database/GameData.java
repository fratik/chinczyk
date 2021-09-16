package pl.fratik.chinczyk.server.database;

import lombok.Data;

@Data
public class GameData {
    private final long id;
    private final byte[] header;
    private final byte[] content;
    private final boolean inProgress;
    private final PlayerData hoster;
}
