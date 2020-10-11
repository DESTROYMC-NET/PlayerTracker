package lol.hyper.playertracker;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandReload implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender.isOp() || commandSender.hasPermission("playertracker.reload")) {
            PlayerTracker.getInstance().loadConfig(PlayerTracker.getInstance().configFile);
            commandSender.sendMessage(ChatColor.GREEN + "Config was reloaded!");
            MYSQLController.disconnect();
            MYSQLController.connect();
        } else {
            commandSender.sendMessage(ChatColor.RED + "You do not have permission to reload!");
        }
        return true;
    }
}