package newamazingpvp.manhuntplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManhuntTabCompleter implements TabCompleter {

    private static final List<String> COMMANDS = Arrays.asList("start", "end", "list", "compass", "regen", "add", "remove", "piglindrop", "setmaxhealth", "setresistance", "help", "quicksettings");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            numbers.add(i);
        }
        if (args.length == 1) {
            return filterSuggestions(COMMANDS, args[0]);
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "add":
                case "remove":
                    return null;
                case "setmaxhealth":
                case "setresistance":
                    return filterSuggestions(Arrays.asList("runner", "hunter"), args[1]);
                case "piglindrop":
                    return numbers.stream().map(String::valueOf).toList();
                default:
                    return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("setmaxhealth") || args[0].equalsIgnoreCase("setresistance")) {
                return numbers.stream().map(String::valueOf).toList();
            } else {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        }
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

    private List<String> filterSuggestions(List<String> suggestions, String input) {
        List<String> filtered = new ArrayList<>();
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(suggestion);
            }
        }
        return filtered;
    }
}