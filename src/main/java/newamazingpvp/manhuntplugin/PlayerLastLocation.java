package newamazingpvp.manhuntplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

import static newamazingpvp.manhuntplugin.ManhuntPlugin.manhuntPlugin;

public class PlayerLastLocation {
    private final HashMap<UUID, Location> lastOverworldLocations = new HashMap<>();
    private final HashMap<UUID, Location> lastNetherLocations = new HashMap<>();
    private final HashMap<UUID, Location> lastEndLocations = new HashMap<>();

    public PlayerLastLocation(){
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID playerUUID : lastOverworldLocations.keySet()) {
                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player == null) continue;
                    if (player.getWorld().getName().equals("world")) {
                        lastOverworldLocations.put(playerUUID, player.getLocation());
                    }
                }
                for (UUID playerUUID : lastNetherLocations.keySet()) {
                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player == null) continue;
                    if (player.getWorld().getName().equals("world_nether")) {
                        lastNetherLocations.put(playerUUID, player.getLocation());
                    }
                }
                for (UUID playerUUID : lastEndLocations.keySet()) {
                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player == null) continue;
                    if (player.getWorld().getName().equals("world_the_end")) {
                        lastEndLocations.put(playerUUID, player.getLocation());
                    }
                }
            }
        }.runTaskTimerAsynchronously(manhuntPlugin, 0, 20);
    }

    public void addTrackingPlayer(Player player){
        lastOverworldLocations.put(player.getUniqueId(), null);
        lastNetherLocations.put(player.getUniqueId(), null);
        lastEndLocations.put(player.getUniqueId(), null);
    }

    public void setLastOverworldLocation(UUID playerUUID, Location location) {
        lastOverworldLocations.put(playerUUID, location);
    }

    public void setLastNetherLocation(UUID playerUUID, Location location) {
        lastNetherLocations.put(playerUUID, location);
    }

    public void setLastEndLocation(UUID playerUUID, Location location) {
        lastEndLocations.put(playerUUID, location);
    }

    public Location getLastOverworldLocation(UUID playerUUID){
        return lastOverworldLocations.get(playerUUID);
    }

    public Location getLastNetherLocation(UUID playerUUID){
        return lastNetherLocations.get(playerUUID);
    }

    public Location getLastEndLocation(UUID playerUUID){
        return lastEndLocations.get(playerUUID);
    }

    public Location getSameDimensionLocation(UUID targetUUID, Player player){
        if(player.getWorld().getName().equals("world")) return lastOverworldLocations.get(targetUUID);
        if(player.getWorld().getName().equals("world_nether")) return lastNetherLocations.get(targetUUID);
        if(player.getWorld().getName().equals("world_the_end")) return lastEndLocations.get(targetUUID);
        return null;
    }

    public void removeTrackingPlayer(UUID playerUUID){
        lastOverworldLocations.remove(playerUUID);
        lastNetherLocations.remove(playerUUID);
        lastEndLocations.remove(playerUUID);
    }


}
