package newamazingpvp.manhuntplugin;

import org.bukkit.Material;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import static newamazingpvp.manhuntplugin.ManhuntCommand.*;

public class Utils implements Listener {

    private ManhuntPlugin plugin;
    public Utils(ManhuntPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onPiglinDrop(EntityDropItemEvent e) {
        if (e.getEntity() instanceof Piglin && plugin.isGameInProgress()) {
            double rand = Math.random() * 100.0;
            double chance = piglinBoost;
            if (rand < chance) {
                e.setCancelled(true);
                ItemStack ep = new ItemStack(Material.ENDER_PEARL, (int)(Math.random()*3)+2);
                e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), ep);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && plugin.isGameInProgress()) {
            if(plugin.getRunner().equals(p)) {
                if (runnerResistance > 0) {
                    e.setDamage(e.getDamage() * (1 - (runnerResistance/100.0)));
                }
            } else if (plugin.getHunters().contains(p)) {
                if (hunterResistance > 0) {
                    e.setDamage(e.getDamage() * (1 - (hunterResistance/100.0)));
                }
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (plugin.getHunters().contains(player)) {
            player.getInventory().addItem(new ItemStack(Material.COMPASS));
        }

    }

    @EventHandler
    public void onCompassDrop(PlayerDropItemEvent event) {
        if (plugin.getHunters().contains(event.getPlayer()) && event.getItemDrop().getItemStack().getType() == Material.COMPASS) {
            event.setCancelled(true);
        }

    }
}
