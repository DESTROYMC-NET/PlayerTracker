package lol.hyper.playertracker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandReload implements CommandExecutor {

    private final PlayerTracker playerTracker;
    private final MYSQLController mysqlController;

    public CommandReload(PlayerTracker playerTracker, MYSQLController mysqlController) {
        this.playerTracker = playerTracker;
        this.mysqlController = mysqlController;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender.isOp() || commandSender.hasPermission("playertracker.reload")) {
            playerTracker.loadConfig(playerTracker.configFile);
            commandSender.sendMessage(ChatColor.GREEN + "Config was reloaded!");
            mysqlController.disconnect();
            mysqlController.connect();
            Bukkit.getScheduler().runTaskLaterAsynchronously(playerTracker, mysqlController::databaseSetup, 100);
        } else {
            commandSender.sendMessage(ChatColor.RED + "You do not have permission to reload!");
        }
        return true;
    }
}