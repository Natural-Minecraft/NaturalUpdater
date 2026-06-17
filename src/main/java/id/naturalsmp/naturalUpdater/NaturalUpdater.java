package id.naturalsmp.naturalUpdater;

import id.naturalsmp.naturalUpdater.platform.BukkitPlatform;
import org.bukkit.plugin.java.JavaPlugin;

public final class NaturalUpdater extends JavaPlugin {

    private UpdaterPlugin core;

    @Override
    public void onEnable() {
        org.bukkit.Bukkit.getConsoleSender().sendMessage(
                org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    "\n&a===============\n" +
                    "&a _   _       _                  _     &e _   _           _       _            \n" +
                    "&a| \ | | __ _| |_ _   _ _ __ __ _| |   &e| | | |_ __   __| | __ _| |_ ___ _ __ \n" +
                    "&a|  \| |/ _` | __| | | | '__/ _` | |   &e| | | | '_ \ / _` |/ _` | __/ _ \\ '__|\n" +
                    "&a| |\  | (_| | |_| |_| | | | (_| | |   &e| |_| | |_) | (_| | (_| | ||  __/ |   \n" +
                    "&a|_| \_|\__,_|\__|\__,_|_|  \__,_|_|   &e \___/| .__/ \__,_|\__,_|\__\___|_|   \n" +
                    "                                             |_|                             \n" +
                    "       >> &eNaturalUpdater v" + getDescription().getVersion() + " Enabled! <<\n" +
                    "&a===============\n"
                )
        );
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
