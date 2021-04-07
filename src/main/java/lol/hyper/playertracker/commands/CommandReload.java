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

package lol.hyper.playertracker.commands;

import lol.hyper.playertracker.PlayerTracker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandReload implements CommandExecutor {

    private final PlayerTracker playerTracker;

    public CommandReload(PlayerTracker playerTracker) {
        this.playerTracker = playerTracker;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender.isOp() || commandSender.hasPermission("playertracker.reload")) {
            playerTracker.loadConfig(playerTracker.configFile);
            commandSender.sendMessage(ChatColor.GREEN + "Configuration was reloaded.");
            playerTracker.mysqlController.disconnect();
            playerTracker.mysqlController.connect();
            Bukkit.getScheduler().runTaskLaterAsynchronously(playerTracker, playerTracker.mysqlController::databaseSetup, 100);
        } else {
            commandSender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
        }
        return true;
    }
}