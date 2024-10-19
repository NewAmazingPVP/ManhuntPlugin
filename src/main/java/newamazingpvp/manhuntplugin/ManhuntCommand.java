package newamazingpvp.manhuntplugin;

import newamazingpvp.manhuntplugin.ManhuntPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static newamazingpvp.manhuntplugin.WorldManager.regenerateWorlds;

public class ManhuntCommand implements CommandExecutor {

    private final ManhuntPlugin plugin;
    public static double piglinBoost;
    public static double runnerMaxHealth;
    public static double hunterMaxHealth;
    public static double runnerResistance;
    public static double hunterResistance;

    public ManhuntCommand(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

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
                break;
            case "end":
                if (!plugin.isGameInProgress()) {
                    player.sendMessage("No game is currently in progress.");
                    return true;
                }
                if(Bukkit.getOnlinePlayers().size() > 1) {
                    player.sendMessage("You can't end the game while there are still players online.");
                    return true;
                }
                plugin.endGame(null);
                break;
            case "list":
                listPlayers(player);
                break;
            case "compass":
                addItemOrDrop(player, new ItemStack(Material.COMPASS), "Your inventory is full. The compass has been dropped on the ground.");
                break;
            case "regen":
                if (plugin.isGameInProgress()) {
                    player.sendMessage("You can't regenerate the world while a game is in progress.");
                    return true;
                }
                regenerateWorlds();
                break;
            case "add":
                if (args.length != 2) {
                    player.sendMessage("Usage: /manhunt add <hunter>");
                    return true;
                }
                plugin.getHunters().add(Bukkit.getPlayer(args[1]));
                break;
            case "remove":
                if (args.length != 2) {
                    player.sendMessage("Usage: /manhunt remove <hunter>");
                    return true;
                }
                plugin.getHunters().remove(Bukkit.getPlayer(args[1]));
                break;
            case "piglinboost":
                if (args.length != 2) {
                    player.sendMessage("Usage: /manhunt piglinBoost %");
                    return true;
                }
                piglinBoost = Double.parseDouble(args[1]);
                break;
            case "setmaxhealth":
                if (args.length != 3) {
                    player.sendMessage("Usage: /manhunt setMaxHealth <runner/hunter> <health>");
                    return true;
                }
                if(args[1].equalsIgnoreCase("runner")) {
                    runnerMaxHealth = Double.parseDouble(args[2]);
                } else if(args[1].equalsIgnoreCase("hunter")) {
                    hunterMaxHealth = Double.parseDouble(args[2]);
                } else {
                    player.sendMessage("Usage: /manhunt setMaxHealth <runner/hunter> <health>");
                }
                break;
            case "setresistance":
                if (args.length != 3) {
                    player.sendMessage("Usage: /manhunt setResistance <runner/hunter> %");
                    return true;
                }
                if(args[1].equalsIgnoreCase("runner")) {
                    runnerResistance = Double.parseDouble(args[2]);
                } else if(args[1].equalsIgnoreCase("hunter")) {
                    hunterResistance = Double.parseDouble(args[2]);
                } else {
                    player.sendMessage("Usage: /manhunt setResistance <runner/hunter> %");
                }
                break;
            default:
                sendUsage(player);
                break;
        }

        return true;
    }

    public void addItemOrDrop(Player player, ItemStack itemStack, String fullInventoryMessage) {
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(itemStack);
        } else {
            World world = player.getWorld();
            world.dropItem(player.getLocation(), itemStack);
            player.sendMessage(ChatColor.GRAY + fullInventoryMessage);
        }
    }

    private void sendUsage(Player player) {
        player.sendMessage("Usage:");
        player.sendMessage("/manhunt start <hunter1,hunter2...> <runner>");
        player.sendMessage("/manhunt end");
        player.sendMessage("/manhunt list");
        player.sendMessage("/manhunt compass");
        player.sendMessage("/manhunt add <hunter>");
        player.sendMessage("/manhunt remove <hunter>");
        player.sendMessage("/manhunt piglinDrop %");
        player.sendMessage("/manhunt setMaxHealth runner <health>");
        player.sendMessage("/manhunt setMaxHealth hunter <health>");
        player.sendMessage("/manhunt setResistance runner %");
        player.sendMessage("/manhunt setResistance hunter %");
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