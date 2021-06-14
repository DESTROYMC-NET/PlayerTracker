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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

public class JSONController {

    private static FileWriter writer;
    private static FileReader reader;
    private final PlayerTracker playerTracker;

    public JSONController(PlayerTracker playerTracker) {
        this.playerTracker = playerTracker;
    }

    private File getPlayerFile(UUID player) {
        return Paths.get(playerTracker.dataFolder.toString(), player.toString() + ".json")
                .toFile();
    }

    /**
     * Read data from JSON file.
     * @param file File to read data from.
     * @return JSONObject with JSON data.
     */
    private JSONObject readFile(File file) {
        if (!file.exists()) {
            return null;
        }
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            reader = new FileReader(file);
            obj = parser.parse(reader);
            reader.close();
        } catch (IOException | ParseException e) {
            playerTracker.logger.severe("Unable to read file " + file.getAbsolutePath());
            playerTracker.logger.severe("This is bad, really bad.");
            e.printStackTrace();
        }
        return (JSONObject) obj;
    }

    /**
     * Write data to JSON file.
     * @param file File to write data to.
     * @param jsonToWrite Data to write to file. This much be a JSON string.
     */
    private void writeFile(File file, String jsonToWrite) {
        try {
            writer = new FileWriter(file);
            writer.write(jsonToWrite);
            writer.close();
        } catch (IOException e) {
            playerTracker.logger.severe("Unable to write file " + file.getAbsolutePath());
            playerTracker.logger.severe("This is bad, really bad.");
            e.printStackTrace();
        }
    }

    public void convertBukkitToStorage() {
        playerTracker.logger.info("Converting bukkit data to json storage...");
        int players = 0;
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            String firstJoin = Long.toString(player.getFirstPlayed());
            String lastJoin = Long.toString(player.getLastPlayed());
            JSONObject file = new JSONObject();
            file.put("firstlogin", firstJoin);
            file.put("lastlogin", lastJoin);
            writeFile(getPlayerFile(player.getUniqueId()), file.toJSONString());
            players++;
        }
        playerTracker.logger.info(players + " were converted.");
    }

    public String getFirstJoin(UUID player) {
        File playerFile = getPlayerFile(player);
        JSONObject playerJSON = readFile(playerFile);
        return playerJSON.get("firstlogin").toString();
    }

    public String getLastLogin(UUID player) {
        File playerFile = getPlayerFile(player);
        JSONObject playerJSON = readFile(playerFile);
        return playerJSON.get("lastlogin").toString();
    }

    public void updateLastLogin(UUID player) {
        File playerFile = getPlayerFile(player);
        JSONObject playerJSON = readFile(playerFile);
        playerJSON.remove("lastlogin");
        playerJSON.put("lastlogin", Long.toString(System.currentTimeMillis()));
        writeFile(playerFile, playerJSON.toJSONString());
    }

    public void setFirstJoin(UUID player) {
        File playerFile = getPlayerFile(player);
        JSONObject playerJSON = new JSONObject();
        playerJSON.put("firstlogin", Long.toString(System.currentTimeMillis()));
        playerJSON.put("lastlogin", Long.toString(System.currentTimeMillis()));
        writeFile(playerFile, playerJSON.toJSONString());
    }

    public boolean doesPlayerExist(UUID player) {
        File[] listOfFiles = playerTracker.dataFolder.toFile().listFiles();
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                if (player.toString().equals(listOfFile.getName().replace(".json", ""))) {
                    return true;
                }
            }
        }
        return false;
    }
}
