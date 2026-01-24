package id.naturalsmp.naturalupdater;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.io.File;
import java.util.Map;

public class UpdaterCommand implements CommandExecutor {

    private final NaturalUpdater plugin;
    private final GitHubFetcher fetcher;
    private final PterodactylClient ptero;

    public UpdaterCommand(NaturalUpdater plugin) {
        this.plugin = plugin;
        this.fetcher = new GitHubFetcher(plugin);
        this.ptero = new PterodactylClient(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("natural.updater.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            showStatus(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("sync")) {
            sender.sendMessage("§6§lNaturalUpdater §8» §fMemulai sinkronisasi plugin dari GitHub...");
            syncAll(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().reload();
            sender.sendMessage("§6§lNaturalUpdater §8» §aKonfigurasi berhasil di-reload!");
            return true;
        }

        if (args[0].equalsIgnoreCase("restart")) {
            sender.sendMessage("§6§lNaturalUpdater §8» §fMemicu restart via NaturalCore...");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restartalert 30"); // Trigger 30s countdown from NaturalCore
            return true;
        }

        return false;
    }

    private void showStatus(CommandSender sender) {
        sender.sendMessage("§6§lNaturalUpdater Status §8»");
        sender.sendMessage("§7Ptero URL: §f" + plugin.getConfigManager().getPanelUrl());
        sender.sendMessage("§7GitHub Owner: §f" + plugin.getConfigManager().getGithubOwner());
        sender.sendMessage(
                "§7Tracked Plugins: §f" + String.join(", ", plugin.getConfigManager().getTrackedPlugins().keySet()));
    }

    private void syncAll(CommandSender sender) {
        Map<String, String> plugins = plugin.getConfigManager().getTrackedPlugins();
        File updateDir = new File(plugin.getDataFolder().getParentFile(), "update");

        for (Map.Entry<String, String> entry : plugins.entrySet()) {
            String repo = entry.getKey();
            String jarName = entry.getValue();

            fetcher.getLatestReleaseDownloadUrl(repo).thenAccept(url -> {
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
