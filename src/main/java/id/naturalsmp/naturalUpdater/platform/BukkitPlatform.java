package id.naturalsmp.naturalupdater.platform;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public class BukkitPlatform implements UpdaterPlatform {
    private final JavaPlugin plugin;

    public BukkitPlatform(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public File getUpdateFolder() {
        return new File(plugin.getDataFolder().getParentFile(), "update");
    }

    @Override
    public void scheduleAsync(Runnable task, long delayTicks, long intervalTicks) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, intervalTicks);
    }

    @Override
    public void scheduleSync(Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public void dispatchCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public String getPlatformName() {
        return "Bukkit";
    }
}
