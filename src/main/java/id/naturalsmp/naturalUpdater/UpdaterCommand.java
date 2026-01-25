package id.naturalsmp.naturalUpdater;

import id.naturalsmp.naturalUpdater.platform.UpdaterPlatform;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.io.File;
import java.util.Map;

public class UpdaterCommand implements CommandExecutor {

    private final UpdaterPlugin plugin;

    public UpdaterCommand(UpdaterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("natural.updater.admin")) {
            plugin.getPlatform().sendMessage(sender, "§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            showStatus(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("sync")) {
            plugin.getPlatform().sendMessage(sender,
                    "§6§lNaturalUpdater §8» §fMemulai sinkronisasi plugin dari GitHub...");
            syncAll(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().reload();
            plugin.getPlatform().sendMessage(sender, "§6§lNaturalUpdater §8» §aKonfigurasi berhasil di-reload!");
            return true;
        }

        if (args[0].equalsIgnoreCase("restart")) {
            plugin.getPlatform().sendMessage(sender, "§6§lNaturalUpdater §8» §fMemicu restart via NaturalCore...");
            plugin.getPlatform().dispatchCommand("restartalert 30");
            return true;
        }

        return false;
    }

    private void showStatus(CommandSender sender) {
        sender.sendMessage("§6§lNaturalUpdater Status §8»");
        sender.sendMessage("§7Platform: §f" + plugin.getPlatform().getPlatformName());
        sender.sendMessage("§7Ptero URL: §f" + plugin.getConfigManager().getPanelUrl());
        sender.sendMessage("§7GitHub Owner: §f" + plugin.getConfigManager().getGithubOwner());
        sender.sendMessage(
                "§7Tracked Plugins: §f" + String.join(", ", plugin.getConfigManager().getTrackedPlugins().keySet()));
    }

    private void syncAll(CommandSender sender) {
        Map<String, String> plugins = plugin.getConfigManager().getTrackedPlugins();
        File updateDir = plugin.getPlatform().getUpdateFolder();

        for (Map.Entry<String, String> entry : plugins.entrySet()) {
            String repo = entry.getKey();
            String jarName = entry.getValue();

            plugin.getUpdateScheduler().getFetcher().getLatestReleaseDownloadUrl(repo).thenAccept(url -> {
                if (url != null) {
                    sender.sendMessage("§7Downloading §e" + repo + "§7...");
                    DownloadUtils.downloadFile(url, jarName, updateDir).thenAccept(file -> {
                        if (file != null) {
                            sender.sendMessage("§aSuccessfully staged §f" + jarName + " §ain /update folder.");
                        } else {
                            sender.sendMessage("§cFailed to download §f" + jarName);
                        }
                    });
                } else {
                    sender.sendMessage("§cNo release found for repo: §f" + repo);
                }
            });
        }
    }
}
