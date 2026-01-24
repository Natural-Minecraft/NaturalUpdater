package id.naturalsmp.naturalupdater;

import org.bukkit.Bukkit;
import java.util.Map;

public class UpdateScheduler {

    private final NaturalUpdater plugin;
    private final GitHubFetcher fetcher;

    public UpdateScheduler(NaturalUpdater plugin) {
        this.plugin = plugin;
        this.fetcher = new GitHubFetcher(plugin);
    }

    public void start() {
        int interval = plugin.getConfigManager().getCheckInterval() * 1200; // minutes to ticks
        if (interval <= 0)
            return;

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            plugin.getLogger().info("Checking for plugin updates on GitHub...");
            performAutoCheck();
        }, 600L, interval);
    }

    private void performAutoCheck() {
        Map<String, String> plugins = plugin.getConfigManager().getTrackedPlugins();
        java.io.File updateDir = new java.io.File(plugin.getDataFolder().getParentFile(), "update");

        for (Map.Entry<String, String> entry : plugins.entrySet()) {
            String repo = entry.getKey();
            String jarName = entry.getValue();
            String currentHash = plugin.getVersionDatabase().getLastHash(repo);

            fetcher.getLatestCommitHash(repo).thenAccept(newHash -> {
                if (newHash != null && !newHash.equals(currentHash)) {
                    plugin.getLogger().info("New build detected for " + repo + "! Hash: " + newHash);

                    fetcher.getLatestReleaseDownloadUrl(repo).thenAccept(url -> {
                        if (url != null) {
                            DownloadUtils.downloadFile(url, jarName, updateDir).thenAccept(file -> {
                                if (file != null) {
                                    plugin.getVersionDatabase().setLastHash(repo, newHash);
                                    plugin.getLogger().info("Successfully staged update for " + repo);

                                    // Trigger restart if globally enabled
                                    if (plugin.getConfig().getBoolean("auto-restart", true)) {
                                        plugin.getLogger().warning("RESTARTING SERVER in 10s for updates...");
                                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                            plugin.getPteroClient().restartServer();
                                        }, 200L);
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
    }
}
