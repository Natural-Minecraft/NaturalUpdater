package id.naturalsmp.naturalupdater;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VersionDatabase {

    private final NaturalUpdater plugin;
    private final File file;
    private YamlConfiguration config;

    public VersionDatabase(NaturalUpdater plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "versions.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public String getLastHash(String repoName) {
        return config.getString(repoName, "");
    }

    public void setLastHash(String repoName, String hash) {
        config.set(repoName, hash);
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
