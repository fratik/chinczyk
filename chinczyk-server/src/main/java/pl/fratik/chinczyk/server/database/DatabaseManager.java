package pl.fratik.chinczyk.server.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import pl.fratik.chinczyk.server.Config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    private final HikariDataSource hikari;

    public DatabaseManager() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(Config.instance.getHikariUrl());
        config.setUsername(Config.instance.getHikariUsername());
        config.setPassword(Config.instance.getHikariPassword());
        config.setAutoCommit(false);
        hikari = new HikariDataSource(config);
        createTables();
    }

    public void createTables() throws SQLException {
        try (Connection con = hikari.getConnection()) {
            try (PreparedStatement stmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS players " +
                    "(id BIGSERIAL, name VARCHAR(13), connections JSONB)")) {
                stmt.execute();
            }
            try (PreparedStatement stmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS clients " +
                    "(id BIGSERIAL, token TEXT, name TEXT, connections TEXT)")) {
                stmt.execute();
            }
        }
    }

    public DatabaseConnection getConnection() throws SQLException {
        return new DatabaseConnection(hikari.getConnection());
    }

    public Client getClientDataByToken(String token) throws SQLException {
        try (DatabaseConnection connection = getConnection()) {
            return connection.getClientDataByToken(token);
        }
    }

    public PlayerData getPlayerData(long id) throws SQLException {
        try (DatabaseConnection connection = getConnection()) {
            return connection.getPlayerData(id);
        }
    }
}
