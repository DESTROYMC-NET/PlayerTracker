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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class Events implements Listener {

    private final PlayerTracker playerTracker;

    public Events(PlayerTracker playerTracker) {
        this.playerTracker = playerTracker;
    }

    @EventHandler
    public void loginEvent(PlayerLoginEvent event) {
        if (!playerTracker.mysqlController.finishedSetup) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "PlayerTracker has not finished setting up! Please wait a few.");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            playerTracker.mysqlController.joinTasks.put(player, System.currentTimeMillis());
            Bukkit.getLogger().info("Adding " + player.getName() + " to player database.");
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!PlayerTracker.isVanished(player.getName())) {
            playerTracker.mysqlController.quitTasks.put(player, System.currentTimeMillis());
        }
    }
}
