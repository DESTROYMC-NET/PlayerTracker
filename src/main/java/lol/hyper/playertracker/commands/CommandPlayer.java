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

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class CommandPlayer implements CommandExecutor {

    final String pattern = "MM/dd/yyyy HH:mm:ss";
    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    private final PlayerTracker playerTracker;
    String joinDateString;
    Date joinDate;
    String lastPlayedString;
    Date lastPlayed;

    public CommandPlayer(PlayerTracker playerTracker) {
        this.playerTracker = playerTracker;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int argsLength = args.length;
        if (argsLength == 1) {
            UUID uuid = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
            if (!playerTracker.jsonController.doesPlayerExist(uuid)) {
                sender.sendMessage(ChatColor.RED + "Player was not found. Maybe they changed their username?");
                return true;
            } else {
                lastPlayed = Date.from(
                        Instant.ofEpochMilli(Long.parseLong(playerTracker.jsonController.getLastLogin(uuid))));
                lastPlayedString = simpleDateFormat.format(lastPlayed);
                joinDate = Date.from(
                        Instant.ofEpochMilli(Long.parseLong(playerTracker.jsonController.getFirstJoin(uuid))));
                joinDateString = simpleDateFormat.format(joinDate);
                sender.sendMessage(ChatColor.GOLD + "--------------------------------------------");
                sender.sendMessage(ChatColor.DARK_AQUA + args[0] + " was first seen on " + joinDateString + " EST.");
                if (Bukkit.getServer().getPlayerExact(args[0]) != null && !PlayerTracker.isVanished(args[0])) {
                    sender.sendMessage(ChatColor.DARK_AQUA + args[0] + " is currently online.\n");
                } else {
                    sender.sendMessage(
                            ChatColor.DARK_AQUA + args[0] + " was last seen on " + lastPlayedString + " EST.");
                }
                sender.sendMessage(ChatColor.GOLD + "--------------------------------------------");
            }
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Invalid option. Usage: /player <player> to find player info.");
        return true;
    }
}
