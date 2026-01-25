package id.naturalsmp.naturalUpdater;

import com.velocitypowered.api.command.SimpleCommand;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VelocityUpdaterTabCompleter implements SimpleCommand {

    private final List<String> subCommands = Arrays.asList("status", "sync", "reload", "restart");

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return subCommands.stream()
                    .filter(s -> s.startsWith(input))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public void execute(Invocation invocation) {
        // This is handled by VelocityUpdaterCommand, but SimpleCommand requires it.
    }
}
