/*
 * This file is part of PlayerTracker.
 *
 * PlayerTracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlayerTracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PlayerTracker.  If not, see <https://www.gnu.org/licenses/>.
 */

package lol.hyper.playertracker.tools;

import lol.hyper.playertracker.PlayerTracker;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

public class MYSQLController {

    static String url;
    public final HashMap<Player, Long> quitTasks = new HashMap<>();
    public final HashMap<Player, Long> joinTasks = new HashMap<>();
    private final PlayerTracker playerTracker;
    public Connection con;
    public boolean finishedSetup = false;
    public MYSQLController(PlayerTracker playerTracker) {
        this.playerTracker = playerTracker;
    }

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
                connect();
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
                String CREATE_TABLE = "CREATE TABLE `playerhistory` (" + "  `uuid` text NOT NULL,"
                        + "  `first_join` text NOT NULL,"
                        + "  `last_login` text NOT NULL"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
                Statement stmt = con.createStatement();
                stmt.executeUpdate(CREATE_TABLE);
                Bukkit.getLogger()
                        .info("[PlayerTracker] I created the tables! However, I'm going to import the player data!");
                Bukkit.getLogger()
                        .info(
                                "[PlayerTracker] I will import from player data, which tracks this information. It shouldn't take long.");
                int imported = 0;
                for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    importPlayer(uuid);
                    imported++;
                }
                Bukkit.getLogger().info("[PlayerTracker] I have imported " + imported + " players!");
            }
            finishedSetup = true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void doTasks() {
        Bukkit.getScheduler().runTaskAsynchronously(playerTracker, () -> {
            connect(); // just force a reconnect
            if (joinTasks.size() != 0) {
                for (Player player : joinTasks.keySet()) {
                    long time = joinTasks.get(player);
                    addNewPlayer(player.getUniqueId(), time);
                    joinTasks.remove(player);
                }
            }
            if (quitTasks.size() != 0) {
                for (Player player : quitTasks.keySet()) {
                    updateLastLogin(player.getUniqueId());
                    quitTasks.remove(player);
                }
            }
        });
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

    public void updateLastLogin(UUID uuid) {
        String SQL_UPDATE = "UPDATE playerhistory SET last_login=? WHERE uuid=?";
        PreparedStatement preparedStatement;
        try {
            preparedStatement = con.prepareStatement(SQL_UPDATE);
            preparedStatement.setString(1, Long.toString(System.currentTimeMillis()));
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void addNewPlayer(UUID uuid, long time) {
        String SQL_UPDATE = "INSERT INTO playerhistory (uuid, first_join, last_login)" + "VALUES (?, ?, ?)";
        PreparedStatement preparedStatement;
        try {
            preparedStatement = con.prepareStatement(SQL_UPDATE);
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, Long.toString(time));
            preparedStatement.setString(3, Long.toString(time));
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
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
