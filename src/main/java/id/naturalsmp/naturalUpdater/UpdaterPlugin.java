package id.naturalsmp.naturalUpdater;

import id.naturalsmp.naturalUpdater.platform.UpdaterPlatform;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

public class UpdaterPlugin {
    private final UpdaterPlatform platform;
    private ConfigManager configManager;
    private VersionDatabase versionDatabase;
    private UpdateScheduler updateScheduler;
    private PterodactylClient pteroClient;

    public UpdaterPlugin(UpdaterPlatform platform) {
        this.platform = platform;
    }

    public void onEnable() {
        // Platform specific default config saving
        saveDefaultConfig();

        // Apply any pending updates from /update folder
        applyPendingUpdates();

        this.configManager = new ConfigManager(this);
        this.versionDatabase = new VersionDatabase(this);
        this.pteroClient = new PterodactylClient(this);

        this.updateScheduler = new UpdateScheduler(this);
        this.updateScheduler.start();

        platform.getLogger()
                .info("NaturalUpdater (" + platform.getPlatformName() + ") enabled! Automated CI/CD is active.");
    }

    private void applyPendingUpdates() {
        File updateDir = platform.getUpdateFolder();
        if (!updateDir.exists() || !updateDir.isDirectory())
            return;

        File pluginsDir = updateDir.getParentFile();
        File[] pendingFiles = updateDir.listFiles((dir, name) -> name.endsWith(".jar"));

        if (pendingFiles == null || pendingFiles.length == 0)
            return;

        platform.getLogger().info("Found " + pendingFiles.length + " pending update(s) in /update folder...");

        for (File file : pendingFiles) {
            File target = new File(pluginsDir, file.getName());
            try {
                // Delete old version if exists
                if (target.exists()) {
                    target.delete();
                }
                // Move new version
                Files.move(file.toPath(), target.toPath());
                platform.getLogger().info("✔ Applied update: " + file.getName());
            } catch (Exception e) {
                platform.getLogger().warning("Failed to apply update " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    private void saveDefaultConfig() {
        File dataFolder = platform.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        String configName = platform.getPlatformName().equalsIgnoreCase("Velocity") ? "velocity.toml" : "config.yml";
        File file = new File(dataFolder, configName);

        if (!file.exists()) {
            try (InputStream in = getClass().getResourceAsStream("/" + configName)) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                    platform.getLogger().info("Generated default config: " + configName);
                } else {
                    platform.getLogger().warning("Could not find internal resource: " + configName);
                }
            } catch (Exception e) {
                platform.getLogger().severe("Could not save default config: " + e.getMessage());
            }
        }
    }

    public void reload() {
        if (configManager != null) {
            configManager.reload();
        }
    }

    public java.util.logging.Logger getLogger() {
        return platform.getLogger();
    }

    public UpdaterPlatform getPlatform() {
        return platform;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public VersionDatabase getVersionDatabase() {
        return versionDatabase;
    }

    public UpdateScheduler getUpdateScheduler() {
        return updateScheduler;
    }

    public PterodactylClient getPteroClient() {
        return pteroClient;
    }
}
