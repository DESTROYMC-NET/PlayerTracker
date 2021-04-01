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

package lol.hyper.playertracker;

import lol.hyper.playertracker.commands.CommandPlayer;
import lol.hyper.playertracker.commands.CommandReload;
import lol.hyper.playertracker.tools.MYSQLController;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.logging.Logger;

public final class PlayerTracker extends JavaPlugin implements Listener {

    public FileConfiguration config;
    public boolean finishedSetup = false;
    public final File configFile = new File(getDataFolder(), "config.yml");
    public final Logger logger = this.getLogger();

    public CommandReload commandReload;
    public CommandPlayer commandPlayer;
    public MYSQLController mysqlController;

    @Override
    public void onEnable() {
        mysqlController = new MYSQLController(this);
        commandReload = new CommandReload(this);
        commandPlayer = new CommandPlayer(mysqlController);
        loadConfig(configFile);
        if (config.getString("mysql.database").equalsIgnoreCase("database")) {
            logger.severe("It looks like you have not configured your database settings. Please edit your config.yml file!");
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            mysqlController.connect();
        }
        this.getCommand("player").setExecutor(commandPlayer);
        this.getCommand("ptreload").setExecutor(commandReload);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        Bukkit.getScheduler().runTaskLaterAsynchronously(this, mysqlController::databaseSetup, 100);
    }

    @Override
    public void onDisable() {
        if (Bukkit.getOnlinePlayers().size() > 0) {
            for (Player player: Bukkit.getOnlinePlayers()) {
                try {
                    mysqlController.updateLastLogin(player.getUniqueId());
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        mysqlController.disconnect();
    }

    public void loadConfig(File file) {
        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    @EventHandler
    public void loginEvent(PlayerLoginEvent event) {
        if (!finishedSetup) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "PlayerTracker has not finished setting up! Please wait a few.");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                mysqlController.addNewPlayer(player.getUniqueId());
                Bukkit.getLogger().info("Adding " + player.getName() + " to player database.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            });
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!isVanished(player.getName())) {
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    mysqlController.updateLastLogin(player.getUniqueId());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * @param player player to check if vanished
     * @return returns if player is vanished or not
     */
    public static boolean isVanished(String player) {
        if (Bukkit.getPlayerExact(player) == null) {
            return false;
        } else {
            Player player2 = Bukkit.getPlayerExact(player);
            assert player2 != null;
            for (MetadataValue meta : player2.getMetadata("vanished")) {
                if (meta.asBoolean()) return true;
            }
        }
        return false;
    }
}