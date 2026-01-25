package id.naturalsmp.naturalUpdater;

import com.velocitypowered.api.command.SimpleCommand;
import java.io.File;
import java.util.Map;

public class VelocityUpdaterCommand implements SimpleCommand {

    private final UpdaterPlugin core;

    public VelocityUpdaterCommand(UpdaterPlugin core) {
        this.core = core;
    }

    @Override
    public void execute(Invocation invocation) {
        Object sender = invocation.source();
        String[] args = invocation.arguments();

        if (!invocation.source().hasPermission("natural.updater.admin")) {
            core.getPlatform().sendMessage(sender, "&cYou do not have permission to use this command.");
            return;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            showStatus(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("sync")) {
            core.getPlatform().sendMessage(sender,
                    "&6&lNaturalUpdater &8» &fMemulai sinkronisasi plugin dari GitHub...");
            syncAll(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            core.getConfigManager().reload();
            core.getPlatform().sendMessage(sender, "&6&lNaturalUpdater &8» &aKonfigurasi berhasil di-reload!");
            return;
        }

        if (args[0].equalsIgnoreCase("restart")) {
            core.getPlatform().sendMessage(sender, "&6&lNaturalUpdater &8» &fMemicu restart panel...");
            core.getPteroClient().restartServer();
            return;
        }
    }

    @Override
    public java.util.List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return java.util.stream.Stream.of("status", "sync", "reload", "restart")
                    .filter(s -> s.startsWith(input))
                    .collect(java.util.stream.Collectors.toList());
        }
        return java.util.List.of();
    }

    private void showStatus(Object sender) {
        core.getPlatform().sendMessage(sender, "&6&lNaturalUpdater Status &8»");
        core.getPlatform().sendMessage(sender, "&7Platform: &f" + core.getPlatform().getPlatformName());
        core.getPlatform().sendMessage(sender, "&7Ptero URL: &f" + core.getConfigManager().getPanelUrl());
        core.getPlatform().sendMessage(sender, "&7GitHub Owner: &f" + core.getConfigManager().getGithubOwner());
        core.getPlatform().sendMessage(sender,
                "&7Tracked Plugins: &f" + String.join(", ", core.getConfigManager().getTrackedPlugins().keySet()));
    }

    private void syncAll(Object sender) {
        Map<String, String> plugins = core.getConfigManager().getTrackedPlugins();
        File updateDir = core.getPlatform().getUpdateFolder();

        if (!updateDir.exists())
            updateDir.mkdirs();

        for (Map.Entry<String, String> entry : plugins.entrySet()) {
            String repo = entry.getKey();
            String jarName = entry.getValue();

            core.getUpdateScheduler().getFetcher().getLatestReleaseDownloadUrl(repo).thenAccept(url -> {
                if (url != null) {
                    core.getPlatform().sendMessage(sender, "&7Downloading &e" + repo + "&7...");
                    DownloadUtils.downloadFile(url, jarName, updateDir).thenAccept(file -> {
                        if (file != null) {
                            core.getPlatform().sendMessage(sender,
                                    "&aSuccessfully staged &f" + jarName + " &ain /update folder.");
                        } else {
                            core.getPlatform().sendMessage(sender, "&cFailed to download &f" + jarName);
                        }
                    });
                } else {
                    core.getPlatform().sendMessage(sender, "&cNo release found for repo: &f" + repo);
                }
            });
        }
    }
}
