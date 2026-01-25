package id.naturalsmp.naturalUpdater;

public class VersionDatabase {

    private final UpdaterPlugin plugin;

    public VersionDatabase(UpdaterPlugin plugin) {
        this.plugin = plugin;
    }

    public String getLastHash(String repoName) {
        return plugin.getPlatform().getStoredVersion(repoName);
    }

    public void setLastHash(String repoName, String hash) {
        plugin.getPlatform().setStoredVersion(repoName, hash);
    }
}
