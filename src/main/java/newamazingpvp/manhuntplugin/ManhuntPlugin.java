package newamazingpvp.manhuntplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

import static newamazingpvp.manhuntplugin.ManhuntCommand.*;
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
    }

    public void endGame(String winnerType) {
        gameInProgress = false;
        String message = (winnerType != null) ? winnerType + " have won the game!" : "The game has ended.";
        Bukkit.broadcastMessage(ChatColor.GOLD + message);
        Bukkit.broadcastMessage(ChatColor.GREEN + "You can regenerate the world for the next game by doing " + ChatColor.GOLD + "/manhunt regen" + ChatColor.GREEN + " or it wait for it to automatically in 10 minutes");
        getServer().getGlobalRegionScheduler().runDelayed(this, (task) -> {
            regenerateWorlds();
        }, 20 * 60 * 10);
        getServer().getGlobalRegionScheduler().runDelayed(this, (task) -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(ChatColor.GREEN + "World will regenerate soon or do it now with " + ChatColor.GOLD + "/manhunt regen");
            }
        }, 20 * 30);
    }

    @EventHandler
    public void dragonDeath(EntityDeathEvent event) {
        if (event.getEntity().getType() == EntityType.ENDER_DRAGON && gameInProgress) {
            endGame("Runner " + runner.getName() + " has killed the dragon and");
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (gameInProgress && !hunters.contains(event.getPlayer()) && !event.getPlayer().equals(runner) && !Bukkit.getWhitelistedPlayers().contains(event.getPlayer())) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "A manhunt game is currently in progress. Please wait for the next game or ask them to /manhunt add you.");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (gameInProgress && !hunters.contains(event.getPlayer()) && !event.getPlayer().equals(runner) && !Bukkit.getWhitelistedPlayers().contains(event.getPlayer())) {
            //event.getPlayer().kickPlayer(ChatColor.RED + "A manhunt game is currently in progress. Please wait for the next game or ask them to /manhunt add you.");
        } else {
            Bukkit.dispatchCommand(event.getPlayer(), "manhunt");
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        getServer().getScheduler().runTaskLater(this, () -> {
            if (gameInProgress && Bukkit.getOnlinePlayers().isEmpty()) {
                getServer().getScheduler().runTaskLater(this, () -> {
                    if (Bukkit.getOnlinePlayers().isEmpty()) {
                        endGame(null);
                        regenerateWorlds();
                    }
                }, 20 * 60 * 5);
            }
        }, 20);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        getServer().getScheduler().runTaskLater(this, () -> {
            if (gameInProgress && Bukkit.getOnlinePlayers().isEmpty()) {
                getServer().getScheduler().runTaskLater(this, () -> {
                    if (Bukkit.getOnlinePlayers().isEmpty()) {
                        endGame(null);
                        regenerateWorlds();
                    }
                }, 20 * 60 * 5);
            }
        }, 20);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().equals(runner)) {
            if (gameInProgress) {
                endGame("Hunters");
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (gameInProgress && hunters.contains(event.getPlayer())) {
            addItemOrDrop(event.getPlayer(), new ItemStack(Material.COMPASS), "Your inventory is full. The compass has been dropped on the ground.");
        }
    }

    public void startGame(List<Player> newHunters, Player newRunner) {
        if (gameInProgress) {
            endGame(null);
        }

        hunters = new ArrayList<>(newHunters);
        runner = newRunner;
        runner.setMaxHealth(runnerMaxHealth * 2.0);
        runner.setHealth(runner.getMaxHealth());
        gameInProgress = true;
        compass.playerLastLocation.addTrackingPlayer(runner);

        World overworld = Bukkit.getWorlds().get(0);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleportAsync(overworld.getSpawnLocation());
        }

        for (Player hunter : hunters) {
            hunter.getInventory().addItem(new ItemStack(Material.COMPASS));
            hunter.setMaxHealth(hunterMaxHealth * 2.0);
            hunter.setHealth(hunter.getMaxHealth());
            compass.setTrackingPlayers(hunter.getUniqueId(), runner.getUniqueId());
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!hunters.contains(p) && !p.equals(runner) && !Bukkit.getWhitelistedPlayers().contains(p)) {
                p.kickPlayer(ChatColor.RED + "A manhunt game is currently in progress. Please wait for the next game or ask them to /manhunt add you.");
            }
        }
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("hunters");
        for (Player hunter : hunters) {
            team.addEntry(hunter.getName());
        }
        team.setAllowFriendlyFire(false);

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

    public Compass getCompass() {
        return compass;
    }
}