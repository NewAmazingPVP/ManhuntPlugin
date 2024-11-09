package newamazingpvp.manhuntplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static newamazingpvp.manhuntplugin.WorldManager.regenerateWorlds;

public class ManhuntCommand implements CommandExecutor {

    private final ManhuntPlugin plugin;
    public static double piglinBoost = 0.0;
    public static double runnerMaxHealth = 10.0;
    public static double hunterMaxHealth = 20.0;
    public static double runnerResistance = 0.0;
    public static double hunterResistance = 0.0;

    public ManhuntCommand(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                if (plugin.isGameInProgress()) {
                    player.sendMessage("A game is already in progress.");
                    return true;
                }
                startGame(player, args);
                player.sendMessage("Game started successfully.");
                break;
            case "end":
                if (!plugin.isGameInProgress()) {
                    player.sendMessage("No game is currently in progress.");
                    return true;
                }
                if (Bukkit.getOnlinePlayers().size() > 1) {
                    player.sendMessage("You can't end the game while there are still players online.");
                    return true;
                }
                plugin.endGame(null);
                player.sendMessage("Game ended successfully.");
                break;
            case "list":
                listPlayers(player);
                player.sendMessage("Listed players successfully.");
                break;
            case "compass":
                addItemOrDrop(player, new ItemStack(Material.COMPASS), "Your inventory is full. The compass has been dropped on the ground.");
                player.sendMessage("Compass given successfully.");
                break;
            case "regen":
                if (plugin.isGameInProgress()) {
                    player.sendMessage("You can't regenerate the world while a game is in progress.");
                    return true;
                }
                regenerateWorlds();
                //player.sendMessage("World regenerated successfully.");
                break;
            case "add":
                if (args.length != 2) {
                    player.sendMessage("Usage: /manhunt add <hunter>");
                    return true;
                }
                plugin.getHunters().add(Bukkit.getPlayer(args[1]));
                player.sendMessage("Hunter added successfully.");
                break;
            case "remove":
                if (args.length != 2) {
                    player.sendMessage("Usage: /manhunt remove <hunter>");
                    return true;
                }
                plugin.getHunters().remove(Bukkit.getPlayer(args[1]));
                player.sendMessage("Hunter removed successfully.");
                break;
            case "piglindrop":
                if (args.length != 2) {
                    player.sendMessage("Usage: /manhunt piglinBoost %");
                    return true;
                }
                piglinBoost = Double.parseDouble(args[1]);
                player.sendMessage("Piglin drop rate set successfully.");
                break;
            case "setmaxhealth":
                if (args.length != 3) {
                    player.sendMessage("Usage: /manhunt setMaxHealth <runner/hunter> <health>");
                    return true;
                }
                if (args[1].equalsIgnoreCase("runner")) {
                    runnerMaxHealth = Double.parseDouble(args[2]);
                } else if (args[1].equalsIgnoreCase("hunter")) {
                    hunterMaxHealth = Double.parseDouble(args[2]);
                } else {
                    player.sendMessage("Usage: /manhunt setMaxHealth <runner/hunter> <health>");
                    return true;
                }
                player.sendMessage("Max health set successfully.");
                break;
            case "setresistance":
                if (args.length != 3) {
                    player.sendMessage("Usage: /manhunt setResistance <runner/hunter> %");
                    return true;
                }
                if (args[1].equalsIgnoreCase("runner")) {
                    runnerResistance = Double.parseDouble(args[2]);
                } else if (args[1].equalsIgnoreCase("hunter")) {
                    hunterResistance = Double.parseDouble(args[2]);
                } else {
                    player.sendMessage("Usage: /manhunt setResistance <runner/hunter> %");
                    return true;
                }
                player.sendMessage("Resistance set successfully.");
                break;
            case "track":
                plugin.compass.playerLastLocation.addTrackingPlayer(plugin.getRunner());
                plugin.compass.setTrackingPlayers(player.getUniqueId(), player.getUniqueId());
                player.sendMessage("Tracking hunter successfully.");
                break;
            default:
                sendUsage(player);
                break;
        }

        return true;
    }

    public static void addItemOrDrop(Player player, ItemStack itemStack, String fullInventoryMessage) {
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(itemStack);
        } else {
            World world = player.getWorld();
            world.dropItem(player.getLocation(), itemStack);
            player.sendMessage(ChatColor.GRAY + fullInventoryMessage);
        }
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.GOLD + "Usage:");
        player.sendMessage(ChatColor.YELLOW + "/manhunt " + ChatColor.AQUA + "start <hunter1,hunter2...> <runner>" + ChatColor.WHITE + " - " + ChatColor.GREEN + "Starts a new manhunt game.");
        player.sendMessage(ChatColor.YELLOW + "/manhunt " + ChatColor.AQUA + "end" + ChatColor.WHITE + " - " + ChatColor.GREEN + "Ends the current manhunt game.");
        player.sendMessage(ChatColor.YELLOW + "/manhunt " + ChatColor.AQUA + "list" + ChatColor.WHITE + " - " + ChatColor.GREEN + "Lists all players in the current game.");
        player.sendMessage(ChatColor.YELLOW + "/manhunt " + ChatColor.AQUA + "compass" + ChatColor.WHITE + " - " + ChatColor.GREEN + "Gives a compass to the player.");
        player.sendMessage(ChatColor.YELLOW + "/manhunt " + ChatColor.AQUA + "add <hunter>" + ChatColor.WHITE + " - " + ChatColor.GREEN + "Adds a player as a hunter.");
        player.sendMessage(ChatColor.YELLOW + "/manhunt " + ChatColor.AQUA + "remove <hunter>" + ChatColor.WHITE + " - " + ChatColor.GREEN + "Removes a player from hunters.");
        player.sendMessage(ChatColor.YELLOW + "/manhunt " + ChatColor.AQUA + "piglinDrop %" + ChatColor.WHITE + " - " + ChatColor.GREEN + "Sets the piglin drop rate for enderpearls, in percentage. 0 is normal rate, and 100 is always drops enderpearls.");
        player.sendMessage(ChatColor.YELLOW + "/manhunt " + ChatColor.AQUA + "setMaxHealth runner <health>" + ChatColor.WHITE + " - " + ChatColor.GREEN + "Sets the max health for the runner.");
        player.sendMessage(ChatColor.YELLOW + "/manhunt " + ChatColor.AQUA + "setMaxHealth hunter <health>" + ChatColor.WHITE + " - " + ChatColor.GREEN + "Sets the max health for the hunters.");
        player.sendMessage(ChatColor.YELLOW + "/manhunt " + ChatColor.AQUA + "setResistance runner %" + ChatColor.WHITE + " - " + ChatColor.GREEN + "Sets the resistance % from damage for the runner.");
        player.sendMessage(ChatColor.YELLOW + "/manhunt " + ChatColor.AQUA + "setResistance hunter %" + ChatColor.WHITE + " - " + ChatColor.GREEN + "Sets the resistance % from damage for the hunters.");
    }

    private void startGame(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage("Usage: /manhunt start <hunter1,hunter2...> <runner>");
            return;
        }

        List<Player> hunters = getPlayers(args[1]);
        Player runner = Bukkit.getPlayer(args[2]);

        if (hunters.isEmpty() || runner == null) {
            player.sendMessage("Invalid player names. Make sure all players are online.");
            return;
        }

        plugin.startGame(hunters, runner);
    }

    private List<Player> getPlayers(String names) {
        List<Player> players = new ArrayList<>();
        for (String name : names.split(",")) {
            Player player = Bukkit.getPlayer(name);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    private void listPlayers(Player player) {
        if (!plugin.isGameInProgress()) {
            player.sendMessage("No game is currently in progress.");
            return;
        }

        player.sendMessage("Hunters: " + getPlayerNames(plugin.getHunters()));
        player.sendMessage("Runner: " + plugin.getRunner());
    }

    private String getPlayerNames(List<Player> players) {
        List<String> names = new ArrayList<>();
        for (Player p : players) {
            names.add(p.getName());
        }
        return String.join(", ", names);
    }
}