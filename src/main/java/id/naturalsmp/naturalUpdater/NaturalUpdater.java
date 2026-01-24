package id.naturalsmp.naturalupdater;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;

public final class NaturalUpdater extends JavaPlugin {

    private static NaturalUpdater instance;
    private ConfigManager configManager;
    private VersionDatabase versionDatabase;
    private UpdateScheduler updateScheduler;
    private PterodactylClient pteroClient;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.versionDatabase = new VersionDatabase(this);
        this.pteroClient = new PterodactylClient(this);

        this.updateScheduler = new UpdateScheduler(this);
        this.updateScheduler.start();

        getLogger().info("NaturalUpdater enabled! Automated CI/CD is active.");

        // Register commands
        getCommand("updater").setExecutor(new UpdaterCommand(this));
        getCommand("updater").setTabCompleter(new UpdaterTabCompleter());
    }

    @Override
    public void onDisable() {
        getLogger().info("NaturalUpdater disabled.");
    }

    public static NaturalUpdater getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public VersionDatabase getVersionDatabase() {
        return versionDatabase;
    }

    public PterodactylClient getPteroClient() {
        return pteroClient;
    }
}
