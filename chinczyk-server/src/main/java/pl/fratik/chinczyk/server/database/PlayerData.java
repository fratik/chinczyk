package pl.fratik.chinczyk.server.database;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerData {
    private final long id;
    private String name;
    private JsonObject connections;
}
