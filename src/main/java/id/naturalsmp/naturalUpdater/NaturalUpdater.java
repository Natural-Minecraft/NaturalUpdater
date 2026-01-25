package id.naturalsmp.naturalUpdater;

import id.naturalsmp.naturalupdater.platform.BukkitPlatform;
import org.bukkit.plugin.java.JavaPlugin;

public final class NaturalUpdater extends JavaPlugin {

    private UpdaterPlugin core;

    @Override
    public void onEnable() {
        this.core = new UpdaterPlugin(new BukkitPlatform(this));
        this.core.onEnable();

        // Register commands (Bukkit specific)
        getCommand("updater").setExecutor(new UpdaterCommand(core));
        getCommand("updater").setTabCompleter(new UpdaterTabCompleter());
    }

    @Override
    public void onDisable() {
        getLogger().info("NaturalUpdater disabled.");
    }

    public UpdaterPlugin getCore() {
        return core;
    }
}
