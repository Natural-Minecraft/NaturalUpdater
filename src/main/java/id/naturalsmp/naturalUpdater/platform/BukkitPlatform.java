package id.naturalsmp.naturalUpdater.platform;

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
    public void sendMessage(Object sender, String message) {
        if (sender instanceof org.bukkit.command.CommandSender s) {
            s.sendMessage(message);
        }
    }

    @Override
    public String getPlatformName() {
        return "Bukkit";
    }

    @Override
    public String getConfigString(String path) {
        File configFile = new File(getDataFolder(), "config.yml");
        return org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(configFile).getString(path);
    }

    @Override
    public int getConfigInt(String path, int defaultValue) {
        File configFile = new File(getDataFolder(), "config.yml");
        return org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(configFile).getInt(path, defaultValue);
    }

    @Override
    public java.util.Map<String, String> getTrackedPlugins() {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        File configFile = new File(getDataFolder(), "config.yml");
        org.bukkit.configuration.ConfigurationSection section = org.bukkit.configuration.file.YamlConfiguration
                .loadConfiguration(configFile)
                .getConfigurationSection("plugins");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                map.put(key, section.getString(key));
            }
        }
        return map;
    }

    @Override
    public String getStoredVersion(String repo) {
        File file = new File(getDataFolder(), "versions.yml");
        if (!file.exists())
            return "";
        return org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file).getString(repo, "");
    }

    @Override
    public void setStoredVersion(String repo, String hash) {
        File file = new File(getDataFolder(), "versions.yml");
        org.bukkit.configuration.file.YamlConfiguration config = org.bukkit.configuration.file.YamlConfiguration
                .loadConfiguration(file);
        config.set(repo, hash);
        try {
            config.save(file);
        } catch (java.io.IOException e) {
            getLogger().severe("Failed to save versions.yml: " + e.getMessage());
        }
    }
}
