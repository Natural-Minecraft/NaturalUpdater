package id.naturalsmp.naturalUpdater;

import id.naturalsmp.naturalUpdater.platform.UpdaterPlatform;
import java.util.Map;

public class ConfigManager {

    private final UpdaterPlugin plugin;

    public ConfigManager(UpdaterPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        // No-op or trigger platform specific reload if needed
    }

    public String getPanelUrl() {
        return plugin.getPlatform().getConfigString("pterodactyl.panel-url");
    }

    public String getApiKey() {
        return plugin.getPlatform().getConfigString("pterodactyl.api-key");
    }

    public String getServerUuid() {
        return plugin.getPlatform().getConfigString("pterodactyl.server-uuid");
    }

    public String getGithubOwner() {
        return plugin.getPlatform().getConfigString("github.owner");
    }

    public String getGithubToken() {
        return plugin.getPlatform().getConfigString("github.token");
    }

    public int getCheckInterval() {
        return plugin.getPlatform().getConfigInt("github.check-interval-minutes", 10);
    }

    public Map<String, String> getTrackedPlugins() {
        return plugin.getPlatform().getTrackedPlugins();
    }
}
