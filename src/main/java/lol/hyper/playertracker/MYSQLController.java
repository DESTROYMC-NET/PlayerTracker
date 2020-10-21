package lol.hyper.playertracker;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.UUID;

public class MYSQLController {

    private final PlayerTracker playerTracker;

    public MYSQLController(PlayerTracker playerTracker) {
        this.playerTracker = playerTracker;
    }

    static String url;
    private static Connection con;

    private void buildURL() {
        String database = playerTracker.config.getString("mysql.database");
        String host = playerTracker.config.getString("mysql.host");
        int port = playerTracker.config.getInt("mysql.port");
        String flags = playerTracker.config.getString("mysql.flags");
        url = "jdbc:mysql://" + host + ":" + port + "/" + database + flags;
    }

    public void connect() {
        String username = playerTracker.config.getString("mysql.username");
        String password = playerTracker.config.getString("mysql.password");
        buildURL();
        Bukkit.getScheduler().runTaskAsynchronously(playerTracker, () -> {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            try {
                con = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void disconnect() {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void databaseSetup() {
        try {
            DatabaseMetaData dbm = con.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "playerhistory", null);
            if (tables.next()) {
                Bukkit.getLogger().info("[PlayerTracker] We found the correct table. Everything is good!");
            } else {
                Bukkit.getLogger().info("[PlayerTracker] Creating table for players...");
                String CREATE_TABLE = "CREATE TABLE `playerhistory` (" +
                        "  `uuid` text NOT NULL," +
                        "  `first_join` text NOT NULL," +
                        "  `last_login` text NOT NULL" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
                Statement stmt = con.createStatement();
                stmt.executeUpdate(CREATE_TABLE);
                Bukkit.getLogger().info("[PlayerTracker] I created the tables! However, I'm going to import the player data!");
                Bukkit.getLogger().info("[PlayerTracker] I will import from player data, which tracks this information. It shouldn't take long.");
                int imported = 0;
                for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    importPlayer(uuid);
                    imported++;
                }
                Bukkit.getLogger().info("[PlayerTracker] I have imported " + imported + " players!");
            }
            playerTracker.finishedSetup = true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public String lookUpFirstJoin(UUID uuid) throws SQLException {
        String SQL_SORT = "SELECT first_join FROM playerhistory WHERE uuid=" + "'" + uuid.toString() + "'";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(SQL_SORT);
        String em;
        if (rs.next()) {
            em = rs.getString("first_join");
        } else {
            em = null;
        }
        return em;
    }

    public String lookUpLastLogin(UUID uuid) throws SQLException {
        String SQL_SORT = "SELECT last_login FROM playerhistory WHERE uuid=" + "'" + uuid.toString() + "'";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(SQL_SORT);
        String em;
        if (rs.next()) {
            em = rs.getString("last_login");
        } else {
            em = null;
        }
        return em;
    }

    public void updateLastLogin(UUID uuid) throws SQLException {
        String SQL_UPDATE = "UPDATE playerhistory SET last_login=? WHERE uuid=?";
        PreparedStatement preparedStatement = con.prepareStatement(SQL_UPDATE);
        preparedStatement.setString(1, Long.toString(System.currentTimeMillis()));
        preparedStatement.setString(2, uuid.toString());
        preparedStatement.executeUpdate();
    }

    public void addNewPlayer(UUID uuid) throws SQLException {
        String SQL_UPDATE = "INSERT INTO playerhistory (uuid, first_join, last_login)" + "VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = con.prepareStatement(SQL_UPDATE);
        preparedStatement.setString(1, uuid.toString());
        preparedStatement.setString(2, Long.toString(System.currentTimeMillis()));
        preparedStatement.setString(3, Long.toString(System.currentTimeMillis()));
        preparedStatement.executeUpdate();
    }

    public void importPlayer(UUID uuid) throws SQLException {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        String SQL_UPDATE = "INSERT INTO playerhistory (uuid, first_join, last_login)" + "VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = con.prepareStatement(SQL_UPDATE);
        preparedStatement.setString(1, uuid.toString());
        preparedStatement.setString(2, Long.toString(player.getFirstPlayed()));
        preparedStatement.setString(3, Long.toString(player.getLastPlayed()));
        preparedStatement.executeUpdate();
    }
}
