package no.hammers.anarchycore.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private Connection connection;
    private final String dbName = "anarchycore.db";

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            File databaseFile = new File(dataFolder, dbName);
            String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();

            connection = DriverManager.getConnection(url);
            plugin.getLogger().info("Successfully connected to SQLite database.");

            createTables();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to SQLite database", e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("SQLite database connection closed.");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to close SQLite connection", e);
        }
    }

    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS players (
                    uuid VARCHAR(36) PRIMARY KEY,
                    last_known_name VARCHAR(16),
                    join_number INTEGER
                );
            """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS punishments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uuid VARCHAR(36) NOT NULL,
                    type VARCHAR(16) NOT NULL,
                    reason TEXT,
                    timestamp BIGINT NOT NULL,
                    expiration BIGINT,
                    active BOOLEAN DEFAULT 1
                );
            """);
        }
    }

    public synchronized void registerPlayerJoin(UUID uuid, String name, int joinNumber) {
        String query = "INSERT INTO players (uuid, last_known_name, join_number) VALUES (?, ?, ?) " +
                       "ON CONFLICT(uuid) DO UPDATE SET last_known_name = excluded.last_known_name";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, name);
            pstmt.setInt(3, joinNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not register player join for " + uuid, e);
        }
    }

    public int getJoinNumber(UUID uuid) {
        String query = "SELECT join_number FROM players WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, uuid.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("join_number");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not get join number for " + uuid, e);
        }
        return -1;
    }

    public int getMaxJoinNumber() {
        String query = "SELECT MAX(join_number) FROM players";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not get max join number", e);
        }
        return 0;
    }

    // --- Punishments ---

    public void addPunishment(UUID uuid, String type, String reason, long expiration) {
        String query = "INSERT INTO punishments (uuid, type, reason, timestamp, expiration) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, type);
            pstmt.setString(3, reason);
            pstmt.setLong(4, System.currentTimeMillis());
            pstmt.setLong(5, expiration);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not add punishment for " + uuid, e);
        }
    }

    public void removeActivePunishments(UUID uuid, String type) {
        String query = "UPDATE punishments SET active = 0 WHERE uuid = ? AND type = ? AND active = 1";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, type);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not remove punishments for " + uuid, e);
        }
    }

    public boolean hasActivePunishment(UUID uuid, String type) {
        String query = "SELECT expiration FROM punishments WHERE uuid = ? AND type = ? AND active = 1";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    long expiration = rs.getLong("expiration");
                    if (expiration == -1 || expiration > System.currentTimeMillis()) {
                        return true;
                    } else {
                        // Automatically mark as expired
                        markPunishmentExpired(uuid, type, expiration);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not check punishment for " + uuid, e);
        }
        return false;
    }

    private void markPunishmentExpired(UUID uuid, String type, long expirationTime) {
        String query = "UPDATE punishments SET active = 0 WHERE uuid = ? AND type = ? AND expiration = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, type);
            pstmt.setLong(3, expirationTime);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not mark punishment expired for " + uuid, e);
        }
    }
    
    public String getActivePunishmentReason(UUID uuid, String type) {
        String query = "SELECT reason, expiration FROM punishments WHERE uuid = ? AND type = ? AND active = 1 ORDER BY id DESC LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    long expiration = rs.getLong("expiration");
                    if (expiration == -1 || expiration > System.currentTimeMillis()) {
                         return rs.getString("reason");
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not check punishment reason for " + uuid, e);
        }
        return "No reason provided.";
    }

}
