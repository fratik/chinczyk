package pl.fratik.chinczyk.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Config {
    public static Config instance;

    private String bindIp = "0.0.0.0";
    private int port = 2137;
    private String hikariUrl = "postgres://host:port/dbname";
    private String hikariUsername = "username";
    private String hikariPassword = "password";
}
