package newamazingpvp.manhuntplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import static org.bukkit.Bukkit.getServer;

public class WorldManager {
    static void regenerateWorlds() {
        Bukkit.broadcastMessage("Regenerating worlds...");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("World is regenerating. Please rejoin in a few minutes.");
        }

        try {
            File script = new File("/home/ubuntu/Folia/deleteFolders.sh");

            ProcessBuilder builder = new ProcessBuilder("/bin/bash", script.getAbsolutePath());
            builder.redirectErrorStream(true);

            builder.directory(new File("/home/ubuntu/Folia/"));

            Process process = builder.start();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        getServer().shutdown();
    }
}
