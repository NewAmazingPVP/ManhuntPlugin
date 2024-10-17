package newamazingpvp.manhuntplugin;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
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

import static newamazingpvp.manhuntplugin.WorldManager.regenerateWorlds;

public class ManhuntPlugin extends JavaPlugin implements Listener {

    private ScheduledTask compassTask;
    private boolean gameInProgress = false;
    private List<Player> hunters = new ArrayList<>();
    private Player runner = null;

    private Compass compass;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("manhunt").setExecutor(new ManhuntCommand(this));
        compass = new Compass(this);
    }

    public void endGame(String winnerType) {
        gameInProgress = false;
        String message = (winnerType != null) ? winnerType + " have won the game!" : "The game has ended.";
        Bukkit.broadcastMessage(ChatColor.GOLD + message);

        getServer().getGlobalRegionScheduler().runDelayed(this, (task) -> regenerateWorlds(), 100);
    }

    @EventHandler
    public void dragonDeath(EntityDeathEvent event){
        if (event.getEntity().getType() == EntityType.ENDER_DRAGON) {
            endGame("Runner " + runner.getName() + " has killed the dragon and");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        if (event.getEntity().equals(runner)) {
            endGame("Hunters");
        }
    }

    public void startGame(List<Player> newHunters, Player newRunner) {
        if (gameInProgress) {
            endGame(null);
        }

        hunters = new ArrayList<>(newHunters);
        runner = newRunner;
        gameInProgress = true;

        World overworld = Bukkit.getWorlds().get(0);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleportAsync(overworld.getSpawnLocation());
        }

        for (Player hunter : hunters) {
            hunter.getInventory().addItem(new ItemStack(Material.COMPASS));
            compass.setTrackingPlayers(hunter.getUniqueId(), runner.getUniqueId());
        }

        Bukkit.broadcastMessage("Manhunt game has started!");
    }

    public boolean isGameInProgress() {
        return gameInProgress;
    }

    public List<Player> getHunters() {
        return hunters;
    }

    public Player getRunner() {
        return runner;
    }
}