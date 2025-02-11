package cn.ningmo.pnn.storage;

import cn.ningmo.pnn.PlayerNickname;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.*;
import java.util.UUID;

public class MySQLStorage implements Storage {
    private final Connection connection;

    public MySQLStorage(PlayerNickname plugin) throws SQLException {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("mysql");
        String host = config.getString("host");
        int port = config.getInt("port");
        String database = config.getString("database");
        String username = config.getString("username");
        String password = config.getString("password");

        String url = String.format("jdbc:mysql://%s:%d/%s", host, port, database);
        connection = DriverManager.getConnection(url, username, password);

        // 创建表
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS nicknames ("
                + "uuid VARCHAR(36) PRIMARY KEY,"
                + "nickname VARCHAR(32) NOT NULL"
                + ")"
            );
        }
    }

    @Override
    public void setNickname(UUID uuid, String nickname) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "REPLACE INTO nicknames (uuid, nickname) VALUES (?, ?)")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, nickname);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNickname(UUID uuid) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT nickname FROM nicknames WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void removeNickname(UUID uuid) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM nicknames WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasNickname(UUID uuid) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT 1 FROM nicknames WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public UUID getUUIDByNickname(String nickname) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT uuid FROM nicknames WHERE LOWER(nickname) = LOWER(?)")) {
            stmt.setString(1, nickname);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString("uuid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 