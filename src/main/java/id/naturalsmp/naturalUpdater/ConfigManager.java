package id.naturalsmp.naturalUpdater;

import id.naturalsmp.naturalUpdater.platform.UpdaterPlatform;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigManager {

    private final UpdaterPlugin plugin;

    public ConfigManager(UpdaterPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        UpdaterPlatform platform = plugin.getPlatform();
        if (platform.getPlatformName().equalsIgnoreCase("Velocity")) {
            loadVelocityConfig();
        } else {
            loadBukkitConfig();
        }
    }

    private void loadBukkitConfig() {
        // This still uses Bukkit's FileConfiguration for comfort on Paper
        File configFile = new File(plugin.getPlatform().getDataFolder(), "config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        this.panelUrl = config.getString("pterodactyl.panel-url");
        this.apiKey = config.getString("pterodactyl.api-key");
        this.serverUuid = config.getString("pterodactyl.server-uuid");

        this.githubOwner = config.getString("github.owner");
        this.githubToken = config.getString("github.token");
        this.checkInterval = config.getInt("github.check-interval-minutes", 10);

        trackedPlugins.clear();
        if (config.getConfigurationSection("plugins") != null) {
            for (String key : config.getConfigurationSection("plugins").getKeys(false)) {
                trackedPlugins.put(key, config.getString("plugins." + key));
            }
        }
    }

    private void loadVelocityConfig() {
        // Simple manual TOML parsing for Velocity to avoid adding more dependencies
        File configFile = new File(plugin.getPlatform().getDataFolder(), "velocity.toml");
        if (!configFile.exists())
            return;

        try (Scanner scanner = new Scanner(configFile)) {
            String currentSection = "";
            trackedPlugins.clear();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1);
                    continue;
                }

                if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    String key = parts[0].trim();
                    String value = parts[1].trim().replace("\"", "");

                    if (currentSection.equals("pterodactyl")) {
                        if (key.equals("panel-url"))
                            this.panelUrl = value;
                        if (key.equals("api-key"))
                            this.apiKey = value;
                        if (key.equals("server-uuid"))
                            this.serverUuid = value;
                    } else if (currentSection.equals("github")) {
                        if (key.equals("owner"))
                            this.githubOwner = value;
                        if (key.equals("token"))
                            this.githubToken = value;
                        if (key.equals("check-interval-minutes"))
                            this.checkInterval = Integer.parseInt(value);
                    } else if (currentSection.equals("plugins")) {
                        trackedPlugins.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getPlatform().getLogger().severe("Failed to load velocity.toml: " + e.getMessage());
        }
    }

    public String getPanelUrl() {
        return panelUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getServerUuid() {
        return serverUuid;
    }

    public String getGithubOwner() {
        return githubOwner;
    }

    public String getGithubToken() {
        return githubToken;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    public Map<String, String> getTrackedPlugins() {
        return trackedPlugins;
    }
}
