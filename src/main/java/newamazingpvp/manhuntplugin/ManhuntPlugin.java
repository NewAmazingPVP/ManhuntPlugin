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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static newamazingpvp.manhuntplugin.ManhuntCommand.hunterMaxHealth;
import static newamazingpvp.manhuntplugin.ManhuntCommand.runnerMaxHealth;
import static newamazingpvp.manhuntplugin.WorldManager.regenerateWorlds;

public class ManhuntPlugin extends JavaPlugin implements Listener {

    private boolean gameInProgress = false;
    private List<Player> hunters = new ArrayList<>();
    private Player runner = null;

    private Compass compass;

    public static ManhuntPlugin manhuntPlugin;

    @Override
    public void onEnable() {
        manhuntPlugin = this;
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("manhunt").setExecutor(new ManhuntCommand(this));
        getCommand("manhunt").setTabCompleter(new ManhuntTabCompleter());
        compass = new Compass(this);
        new Utils(this);
    }

    @Override
    public void onDisable() {
        if (gameInProgress) {
            endGame(null);
        }
    }

    public void endGame(String winnerType) {
        gameInProgress = false;
        String message = (winnerType != null) ? winnerType + " have won the game!" : "The game has ended.";
        Bukkit.broadcastMessage(ChatColor.GOLD + message);
        Bukkit.broadcastMessage(ChatColor.GREEN + "You can regenerate the world for the next game by doing " + ChatColor.GOLD + "/manhunt regen" + ChatColor.GREEN + " or it wait for it to automatically in 10 minutes");
        getServer().getGlobalRegionScheduler().runDelayed(this, (task) -> {
                regenerateWorlds();
        }, 20 * 60 * 10);
    }

    @EventHandler
    public void dragonDeath(EntityDeathEvent event){
        if (event.getEntity().getType() == EntityType.ENDER_DRAGON && gameInProgress) {
            endGame("Runner " + runner.getName() + " has killed the dragon and");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        if (gameInProgress && !hunters.contains(event.getPlayer()) && !event.getPlayer().equals(runner) && !Bukkit.getWhitelistedPlayers().contains(event.getPlayer().getName())) {
            event.getPlayer().kickPlayer(ChatColor.RED + "A manhunt game is currently in progress. Please wait for the next game or ask them to /manhunt add you.");
        } else {
            Bukkit.dispatchCommand(event.getPlayer(), "manhunt");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        if (event.getEntity().equals(runner)) {
            if(gameInProgress) {
                endGame("Hunters");
            }
        }
    }

    public void startGame(List<Player> newHunters, Player newRunner) {
        if (gameInProgress) {
            endGame(null);
        }

        hunters = new ArrayList<>(newHunters);
        runner = newRunner;
        runner.setMaxHealth(runnerMaxHealth*2.0);
        runner.setHealth(runner.getMaxHealth());
        gameInProgress = true;
        compass.playerLastLocation.addTrackingPlayer(runner);

        World overworld = Bukkit.getWorlds().get(0);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleportAsync(overworld.getSpawnLocation());
        }

        for (Player hunter : hunters) {
            hunter.getInventory().addItem(new ItemStack(Material.COMPASS));
            hunter.setMaxHealth(hunterMaxHealth*2.0);
            hunter.setHealth(hunter.getMaxHealth());
            compass.setTrackingPlayers(hunter.getUniqueId(), runner.getUniqueId());
        }

        for (Player p: Bukkit.getOnlinePlayers()) {
            if (!hunters.contains(event.getPlayer()) && !event.getPlayer().equals(runner) && !Bukkit.getWhitelistedPlayers().contains(event.getPlayer().getName())) {
                event.getPlayer().kickPlayer(ChatColor.RED + "A manhunt game is currently in progress. Please wait for the next game or ask them to /manhunt add you.");
            }
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