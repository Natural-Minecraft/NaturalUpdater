package id.naturalsmp.naturalupdater;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UpdaterTabCompleter implements TabCompleter {

    private final List<String> subCommands = Arrays.asList("status", "sync", "reload", "restart");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("natural.updater.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return subCommands.stream()
                    .filter(s -> s.startsWith(input))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
