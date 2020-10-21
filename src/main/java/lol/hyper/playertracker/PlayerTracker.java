package lol.hyper.playertracker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

public final class PlayerTracker extends JavaPlugin implements Listener {

    public FileConfiguration config;
    public boolean finishedSetup = false;
    public final File configFile = new File(getDataFolder(), "config.yml");

    public CommandReload commandReload;
    public MYSQLController mysqlController;

    @Override
    public void onEnable() {
        mysqlController = new MYSQLController(this);
        commandReload = new CommandReload(this, mysqlController);
        loadConfig(configFile);
        if (config.getString("mysql.database").equalsIgnoreCase("database")) {
            Bukkit.getLogger().severe("[PlayerTracker] It looks like you have not configured your database settings. Please edit your config.yml file!");
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            mysqlController.connect();
        }
        this.getCommand("player").setExecutor(new CommandPlayer(mysqlController));
        this.getCommand("ptreload").setExecutor(commandReload);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        Bukkit.getScheduler().runTaskLaterAsynchronously(this, mysqlController::databaseSetup, 100);
    }

    @Override
    public void onDisable() {
        if (Bukkit.getOnlinePlayers().size() > 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
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
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!finishedSetup) {
            player.kickPlayer(ChatColor.RED + "PlayerTracker has not finished setting up! Please wait a few.");
            return;
        }
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
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                mysqlController.updateLastLogin(player.getUniqueId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
