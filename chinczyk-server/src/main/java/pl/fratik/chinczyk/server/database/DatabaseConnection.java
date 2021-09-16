package pl.fratik.chinczyk.server.database;

import com.google.gson.JsonObject;
import pl.fratik.chinczyk.server.util.JsonUtil;
import pl.fratik.chinczyk.util.StreamUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class DatabaseConnection implements AutoCloseable {
    private final Connection connection;

    public DatabaseConnection(Connection connection) {
        this.connection = connection;
    }

    public PlayerData getPlayerData(long id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM players WHERE id = ?")) {
            stmt.setLong(1, id);
            ResultSet resultSet = stmt.executeQuery();
            if (!resultSet.isBeforeFirst()) return null;
            resultSet.next();
            JsonObject connections = JsonUtil.GSON.fromJson(resultSet.getString("connections"), JsonObject.class);
            return new PlayerData(resultSet.getLong("id"), resultSet.getString("name"), connections);
        }
    }

    public void updatePlayerData(long id, PlayerData newData) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE players SET name = ?, connections = ?::jsonb WHERE id = ?")) {
            stmt.setString(1, newData.getName());
            stmt.setString(2, newData.getConnections().toString());
            stmt.setLong(3, id);
            if (stmt.executeUpdate() == 0) throw new SQLException("nie uaktualniono żadnych danych");
        }
    }

    public PlayerData insertPlayerData(PlayerData data) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO players (name, connections) VALUES (?, ?::jsonb) RETURNING id")) {
            stmt.setString(1, data.getName());
            stmt.setString(2, data.getConnections().toString());
            ResultSet set = stmt.executeQuery();
            if (!set.isBeforeFirst()) throw new SQLException("brak zwróconego ID?");
            set.next();
            return new PlayerData(set.getLong("id"), data.getName(), data.getConnections());
        }
    }

    public Client getClientDataByToken(String token) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM clients WHERE token = ?")) {
            stmt.setString(1, token);
            ResultSet resultSet = stmt.executeQuery();
            if (!resultSet.isBeforeFirst()) return null;
            resultSet.next();
            Set<String> connections = StreamUtil.setFromString(resultSet.getString("connections"));
            return new Client(resultSet.getLong("id"), resultSet.getString("token"), resultSet.getString("clientName"), connections);
        }
    }

    public Client insertClientData(Client data) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO clients (token, clientName, connections) VALUES (?, ?, ?) RETURNING id")) {
            stmt.setString(1, data.getToken());
            stmt.setString(2, data.getClientName());
            stmt.setString(3, StreamUtil.setToString(data.getConnections()));
            ResultSet set = stmt.executeQuery();
            if (!set.isBeforeFirst()) throw new SQLException("brak zwróconego ID?");
            set.next();
            return new Client(set.getLong("id"), data.getToken(), data.getClientName(), data.getConnections());
        }
    }

    public void updateClientData(long id, Client data) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE clients SET token = ?, clientName = ?, connections = ? WHERE id = ?")) {
            stmt.setString(1, data.getToken());
            stmt.setString(2, data.getClientName());
            stmt.setString(3, StreamUtil.setToString(data.getConnections()));
            stmt.setLong(4, id);
            if (stmt.executeUpdate() == 0) throw new SQLException("nie uaktualniono żadnych danych");
        }
    }

    public void commit() throws SQLException {
        connection.commit();
    }

    public void rollback() throws SQLException {
        connection.rollback();
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
