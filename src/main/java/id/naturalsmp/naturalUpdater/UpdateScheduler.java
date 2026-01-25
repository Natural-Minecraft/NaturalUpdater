package id.naturalsmp.naturalUpdater;

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
                    plugin.getLogger().info("New update detected for " + repo + "! Remote HASH: " + displayNewHash);

                    if (repo.equalsIgnoreCase("NaturalPacks") || repo.endsWith("/NaturalPacks")) {
                        handleGeyserAutoSync(repo, newHash);
                    } else {
                        fetcher.getLatestReleaseDownloadUrl(repo, ".jar").thenAccept(url -> {
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
                                }
                            });
                        });
                    }
                } else {
                    plugin.getLogger().info(repo + " is already up to date.");
                }
            });
        }
    }

    private void handleGeyserAutoSync(String repo, String newHash) {
        plugin.getLogger().info("Menciptakan sinkronisasi Geyser Pack otomatis...");

        File geyserBase = new File(plugin.getPlatform().getDataFolder().getParentFile(), "Geyser-Velocity");
        File mappingDir = new File(geyserBase, "custom_mappings");
        File packDir = new File(geyserBase, "packs");

        if (!mappingDir.exists())
            mappingDir.mkdirs();
        if (!packDir.exists())
            packDir.mkdirs();

        // Download Mappings
        fetcher.getLatestReleaseDownloadUrl(repo, ".mappings").thenAccept(url -> {
            if (url != null) {
                DownloadUtils.downloadFile(url, "generated.mappings", mappingDir).thenAccept(file -> {
                    if (file != null) {
                        plugin.getLogger().info("Geyser Mappings updated via Auto-Sync.");
                        plugin.getVersionDatabase().setLastHash(repo, newHash);
                    }
                });
            }
        });

        // Download MCPack
        fetcher.getLatestReleaseDownloadUrl(repo, ".mcpack").thenAccept(url -> {
            if (url != null) {
                DownloadUtils.downloadFile(url, "generated.mcpack", packDir).thenAccept(file -> {
                    if (file != null)
                        plugin.getLogger().info("Geyser MCPack updated via Auto-Sync.");
                });
            }
        });
    }
}
