package id.naturalsmp.naturalUpdater;

import id.naturalsmp.naturalUpdater.platform.UpdaterPlatform;
import java.util.Map;

public class UpdateScheduler {

    private final UpdaterPlugin plugin;
    private final GitHubFetcher fetcher;

    public UpdateScheduler(UpdaterPlugin plugin) {
        this.plugin = plugin;
        this.fetcher = new GitHubFetcher(plugin);
    }

    public GitHubFetcher getFetcher() {
        return fetcher;
    }

    public void start() {
        int interval = plugin.getConfigManager().getCheckInterval() * 1200; // minutes to ticks
        if (interval <= 0)
            return;

        plugin.getPlatform().scheduleAsync(() -> {
            plugin.getPlatform().getLogger().info("Checking for plugin updates on GitHub...");
            performAutoCheck();
        }, 600L, interval);
    }

    private void performAutoCheck() {
        Map<String, String> plugins = plugin.getConfigManager().getTrackedPlugins();
        java.io.File updateDir = plugin.getPlatform().getUpdateFolder();

        for (Map.Entry<String, String> entry : plugins.entrySet()) {
            String repo = entry.getKey();
            String jarName = entry.getValue();
            String currentHash = plugin.getVersionDatabase().getLastHash(repo);

            String displayHash = (currentHash != null && currentHash.length() >= 7) ? currentHash.substring(0, 7)
                    : (currentHash != null ? currentHash : "None");
            plugin.getLogger().info("Checking " + repo + "... (Current Local HASH: " + displayHash + ")");

            fetcher.getLatestCommitHash(repo).thenAccept(newHash -> {
                if (newHash == null) {
                    plugin.getPlatform().getLogger()
                            .warning("Failed to fetch latest hash for " + repo + ". Check GitHub Token/Repo spelling.");
                    return;
                }

                if (!newHash.equals(currentHash)) {
                    String displayNewHash = (newHash.length() >= 7) ? newHash.substring(0, 7) : newHash;
                    plugin.getLogger().info("New build detected for " + repo + "! Remote HASH: " + displayNewHash);

                    fetcher.getLatestReleaseDownloadUrl(repo).thenAccept(url -> {
                        if (url == null) {
                            plugin.getLogger().warning("No .jar asset found in the latest release of " + repo);
                            return;
                        }

                        plugin.getPlatform().getLogger().info("Downloading update for " + repo + " from: " + url);
                        DownloadUtils.downloadFile(url, jarName, updateDir).thenAccept(file -> {
                            if (file != null) {
                                plugin.getVersionDatabase().setLastHash(repo, newHash);
                                plugin.getPlatform().getLogger()
                                        .info("Successfully staged update for " + repo + " in /update folder.");

                                // Trigger restart if globally enabled: THIS NEEDS PLATFORM AGNOSTIC LOGIC
                                // But for now we just use the platform's dispatch command
                                if (true) { // TODO: Add check to config
                                    // Use standard SYSOUT or just return and let caller log
                                }
                            }
                        });
                    });
                } else {
                    plugin.getLogger().info(repo + " is already up to date.");
                }
            });
        }
    }
}
