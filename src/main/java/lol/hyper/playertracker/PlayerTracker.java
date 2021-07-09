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
import lol.hyper.playertracker.tools.JSONController;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public final class PlayerTracker extends JavaPlugin implements Listener {

    public final Path dataFolder = Paths.get(getDataFolder() + File.separator + "data");
    public final Logger logger = this.getLogger();

    public CommandPlayer commandPlayer;
    public Events events;
    public JSONController jsonController;

    @Override
    public void onEnable() {
        commandPlayer = new CommandPlayer(this);
        events = new Events(this);
        jsonController = new JSONController(this);

        this.getCommand("player").setExecutor(commandPlayer);

        Bukkit.getServer().getPluginManager().registerEvents(events, this);

        if (!dataFolder.toFile().exists()) {
            try {
                Files.createDirectory(dataFolder);
            } catch (IOException e) {
                logger.severe("Unable to create " + dataFolder.toAbsolutePath());
                e.printStackTrace();
            }
            // convert bukkit -> json
            jsonController.convertBukkitToStorage();
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
