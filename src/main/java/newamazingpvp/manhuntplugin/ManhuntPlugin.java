package newamazingpvp.manhuntplugin;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ManhuntPlugin extends JavaPlugin implements Listener {

    private ScheduledTask compassTask;
    private boolean gameInProgress = false;
    private List<Player> hunters = new ArrayList<>();
    private List<Player> runners = new ArrayList<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("manhunt").setExecutor(new ManhuntCommand(this));
        startCompassTask();
    }

    @Override
    public void onDisable() {
        if (compassTask != null) {
            compassTask.cancel();
        }
    }

    private void startCompassTask() {
        compassTask = getServer().getGlobalRegionScheduler().runAtFixedRate(this, (task) -> {
            if (gameInProgress) {
                for (Player hunter : hunters) {
                    updateCompass(hunter);
                }
            }
        }, 1, 20);
    }

    private void updateCompass(Player hunter) {
        ItemStack compass = hunter.getInventory().getItemInMainHand();
        if (compass.getType() == Material.COMPASS) {
            Player nearestRunner = getNearestRunner(hunter);
            if (nearestRunner != null) {
                hunter.setCompassTarget(nearestRunner.getLocation());
            }
        }
    }

    private Player getNearestRunner(Player hunter) {
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Player runner : runners) {
            if (runner.getWorld() == hunter.getWorld()) {
                double distance = hunter.getLocation().distance(runner.getLocation());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = runner;
                }
            }
        }

        return nearest;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (gameInProgress && event.getItem() != null && event.getItem().getType() == Material.COMPASS) {
            Player player = event.getPlayer();
            if (hunters.contains(player)) {
                World world = player.getWorld();
                Random random = new Random();
                if (random.nextDouble() < 0.1) {
                    world.spawnEntity(player.getLocation().add(random.nextInt(10) - 5, 0, random.nextInt(10) - 5), org.bukkit.entity.EntityType.PIGLIN);
                }
            }
        }
    }



    public void endGame(String winnerType) {
        gameInProgress = false;
        String message = (winnerType != null) ? winnerType + " have won the game!" : "The game has ended.";
        Bukkit.broadcastMessage(message);

        getServer().getGlobalRegionScheduler().runDelayed(this, (task) -> regenerateWorlds(), 100);
    }

    private void regenerateWorlds() {
        Bukkit.broadcastMessage("Regenerating worlds...");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("World is regenerating. Please rejoin in a few minutes.");
        }

        for (World world : Bukkit.getWorlds()) {
            //Bukkit.unloadWorld(world, false);
        }

        forceDeleteWorldFolder("world");
        forceDeleteWorldFolder("world_nether");
        forceDeleteWorldFolder("world_the_end");
        getServer().shutdown();

        /*WorldCreator overworld = new WorldCreator("world");
        overworld.environment(World.Environment.NORMAL);
        overworld.createWorld();

        WorldCreator nether = new WorldCreator("world_nether");
        nether.environment(World.Environment.NETHER);
        nether.createWorld();

        WorldCreator end = new WorldCreator("world_the_end");
        end.environment(World.Environment.THE_END);
        end.createWorld();*/

        Bukkit.broadcastMessage("Worlds have been regenerated. Players can now rejoin.");
    }

    private void forceDeleteWorldFolder(String worldName) {
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

    public void startGame(List<Player> newHunters, List<Player> newRunners) {
        if (gameInProgress) {
            endGame(null);
        }

        hunters = new ArrayList<>(newHunters);
        runners = new ArrayList<>(newRunners);
        gameInProgress = true;

        World overworld = Bukkit.getWorlds().get(0);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleportAsync(overworld.getSpawnLocation());
        }

        for (Player hunter : hunters) {
            hunter.getInventory().addItem(new ItemStack(Material.COMPASS));
        }

        Bukkit.broadcastMessage("Manhunt game has started!");
    }

    public boolean isGameInProgress() {
        return gameInProgress;
    }

    public List<Player> getHunters() {
        return hunters;
    }

    public List<Player> getRunners() {
        return runners;
    }
}