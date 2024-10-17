package newamazingpvp.manhuntplugin;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public class Compass implements Listener {

    private final HashMap<UUID, UUID> trackingPlayers = new HashMap<>();
    private final HashMap<UUID, Location> lastPortalLocations = new HashMap<>();
    public BukkitTask compassTask;

    private final ManhuntPlugin plugin;

    public Compass(ManhuntPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        compassUpdate();
    }

    @EventHandler
    public void onPlayerPortalEvent(PlayerPortalEvent event) {
        lastPortalLocations.put(event.getPlayer().getUniqueId(), event.getFrom());
    }
    public void compassUpdate() {
        compassTask = new BukkitRunnable() {
            public void run() {
                for (UUID playerUUID : trackingPlayers.keySet()) {
                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player == null) continue;

                    UUID targetUUID = trackingPlayers.get(playerUUID);
                    Player target = Bukkit.getPlayer(targetUUID);
                    if (target == null) continue;

                    ItemStack compass = getCompassFromInventory(player);
                    if (compass == null){
                        String warning = ChatColor.RED + "You need a compass to track runner. Do /manhunt compass to get one";
                        TextComponent textComponent = new TextComponent(warning);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent);
                        continue;
                    }

                    String msg = ChatColor.GREEN + "Tracking " + ChatColor.BOLD + target.getName();

                    if (player.getWorld().getEnvironment() == World.Environment.NORMAL && target.getWorld().getEnvironment() == World.Environment.NORMAL) {
                        setNormalCompass(compass);
                        player.setCompassTarget(target.getLocation());
                    } else if (player.getWorld().getEnvironment() == target.getWorld().getEnvironment()) {
                        setLodestoneCompass(compass, target.getLocation());
                    } else {
                        Location targetLocation = lastPortalLocations.get(target.getUniqueId());
                        if (targetLocation != null && player.getWorld() == targetLocation.getWorld()) {
                            setLodestoneCompass(compass, targetLocation);
                        } else {
                            msg = ChatColor.RED + "Cannot track player because they are in a different dimension and haven't used a portal yet";
                        }
                    }
                    TextComponent textComponent = new TextComponent(msg);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void setTrackingPlayers(UUID playerUUID, UUID targetUUID) {
        trackingPlayers.put(playerUUID, targetUUID);
    }

    private ItemStack getCompassFromInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.COMPASS) {
                return item;
            }
        }
        return null;
    }

    private void setNormalCompass(ItemStack compass) {
        CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();
        assert compassMeta != null;
        if (compassMeta.isLodestoneTracked()) {
            compassMeta.setLodestone(null);
            compassMeta.setLodestoneTracked(false);
            compass.setItemMeta(compassMeta);
        }
    }

    private void setLodestoneCompass(ItemStack compass, Location location) {
        CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();
        assert compassMeta != null;
        compassMeta.setLodestone(location);
        compassMeta.setLodestoneTracked(true);
        compass.setItemMeta(compassMeta);
    }
}