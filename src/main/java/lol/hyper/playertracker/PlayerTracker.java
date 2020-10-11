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

    private static PlayerTracker instance;
    public FileConfiguration config;
    public boolean finishedSetup = false;
    public final File configFile = new File(getDataFolder(), "config.yml");

    public static PlayerTracker getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        loadConfig(configFile);
        if (config.getString("mysql.database").equalsIgnoreCase("database")) {
            Bukkit.getLogger().severe("[PlayerTracker] It looks like you have not configured your database settings. Please edit your config.yml file!");
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            MYSQLController.connect();
        }
        this.getCommand("player").setExecutor(new CommandPlayer());
        this.getCommand("ptreload").setExecutor(new CommandReload());
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        Bukkit.getScheduler().runTaskLater(this, MYSQLController::databaseSetup, 100);
    }

    @Override
    public void onDisable() {
        if (Bukkit.getOnlinePlayers().size() > 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                try {
                    MYSQLController.updateLastLogin(player.getUniqueId());
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        MYSQLController.disconnect();
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
                    MYSQLController.addNewPlayer(player.getUniqueId());
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
                MYSQLController.updateLastLogin(player.getUniqueId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
