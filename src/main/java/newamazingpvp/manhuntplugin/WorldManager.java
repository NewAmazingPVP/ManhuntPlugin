package newamazingpvp.manhuntplugin;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;

import static org.bukkit.Bukkit.getServer;

public class WorldManager {
    static void regenerateWorlds() {
        //Bukkit.broadcastMessage("Regenerating worlds...");

        for (Player player : Bukkit.getOnlinePlayers()) {
            //player.kickPlayer("World is regenerating. Please rejoin in a few seconds.");
        }

        for (World world : Bukkit.getWorlds()) {
            Bukkit.savePlayers();
            world.save();
            world.setAutoSave(false);
        }

        //TODO: Don't use a script, directly do it in the code itself to make it work anywhere
        try {
            File script = new File("/home/ubuntu/Manhunt/deleteFolders.sh");

            ProcessBuilder builder = new ProcessBuilder("/bin/bash", script.getAbsolutePath());
            builder.redirectErrorStream(true);

            builder.directory(new File("/home/ubuntu/Manhunt/"));

            Process process = builder.start();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        getServer().shutdown();
    }
}
