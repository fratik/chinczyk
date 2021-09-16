package pl.fratik.chinczyk.server.database;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class Client {
    private final long id;
    private final String token;
    private final String clientName;
    private final Set<String> connections;
}
