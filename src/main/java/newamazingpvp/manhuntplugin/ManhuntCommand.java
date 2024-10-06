package newamazingpvp.manhuntplugin;

import newamazingpvp.manhuntplugin.ManhuntPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ManhuntCommand implements CommandExecutor {

    private final ManhuntPlugin plugin;

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
                plugin.endGame(null);
                break;
            case "list":
                listPlayers(player);
                break;
            default:
                sendUsage(player);
                break;
        }

        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage("Usage:");
        player.sendMessage("/manhunt start <hunter1,hunter2...> <runner1,runner2...>");
        player.sendMessage("/manhunt end");
        player.sendMessage("/manhunt list");
    }

    private void startGame(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage("Usage: /manhunt start <hunter1,hunter2...> <runner1,runner2...>");
            return;
        }

        List<Player> hunters = getPlayers(args[1]);
        List<Player> runners = getPlayers(args[2]);

        if (hunters.isEmpty() || runners.isEmpty()) {
            player.sendMessage("Invalid player names. Make sure all players are online.");
            return;
        }

        plugin.startGame(hunters, runners);
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
        player.sendMessage("Runners: " + getPlayerNames(plugin.getRunners()));
    }

    private String getPlayerNames(List<Player> players) {
        List<String> names = new ArrayList<>();
        for (Player p : players) {
            names.add(p.getName());
        }
        return String.join(", ", names);
    }
}