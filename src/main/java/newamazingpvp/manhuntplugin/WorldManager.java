package newamazingpvp.manhuntplugin;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

public class WorldManager {
    static void regenerateWorlds() {
        Bukkit.broadcastMessage("Regenerating worlds...");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("World is regenerating. Please rejoin in a few minutes.");
        }

        forceDeleteWorldFolder("world");
        forceDeleteWorldFolder("world_nether");
        forceDeleteWorldFolder("world_the_end");
        getServer().shutdown();

        Bukkit.broadcastMessage("Worlds have been regenerated. Players can now rejoin.");
    }

    private static void forceDeleteWorldFolder(String worldName) {
        Path worldPath = Paths.get(Bukkit.getWorldContainer().getAbsolutePath(), worldName);
        try {
            Files.walkFileTree(worldPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            getLogger().severe("Failed to delete world folder: " + worldName);
            e.printStackTrace();
        }
    }
}
