package id.naturalsmp.naturalupdater;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final NaturalUpdater plugin;
    private String panelUrl;
    private String apiKey;
    private String serverUuid;
    private String githubOwner;
    private String githubToken;
    private int checkInterval;
    private Map<String, String> trackedPlugins = new HashMap<>();

    public ConfigManager(NaturalUpdater plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

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
