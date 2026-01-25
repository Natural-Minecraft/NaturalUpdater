package id.naturalsmp.naturalUpdater.platform;

import com.velocitypowered.api.proxy.ProxyServer;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class VelocityPlatform implements UpdaterPlatform {
    private final Object plugin;
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    public VelocityPlatform(Object plugin, ProxyServer server, Logger logger, Path dataDirectory) {
        this.plugin = plugin;
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public File getDataFolder() {
        return dataDirectory.toFile();
    }

    @Override
    public File getUpdateFolder() {
        // Velocity update folder is usually the same as bukkit logic, one level up from
        // data folder if desired
        // but typically plugins are in root /plugins.
        return new File(dataDirectory.toFile().getParentFile(), "update");
    }

    @Override
    public void scheduleAsync(Runnable task, long delayTicks, long intervalTicks) {
        // Convert ticks (20 per sec) to milliseconds
        long delayMillis = delayTicks * 50;
        long intervalMillis = intervalTicks * 50;
        server.getScheduler()
                .buildTask(plugin, task)
                .delay(delayMillis, TimeUnit.MILLISECONDS)
                .repeat(intervalMillis, TimeUnit.MILLISECONDS)
                .schedule();
    }

    @Override
    public void scheduleSync(Runnable task) {
        // Velocity is mostly async, "sync" just means run it.
        server.getScheduler()
                .buildTask(plugin, task)
                .schedule();
    }

    @Override
    public void dispatchCommand(String command) {
        server.getCommandManager().executeAsync(server.getConsoleCommandSource(), command);
    }

    @Override
    public void sendMessage(Object sender, String message) {
        if (sender instanceof com.velocitypowered.api.command.CommandSource s) {
            s.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize(message.replace("§", "&"))); // Convert legacy to MiniMessage if needed or use
                                                              // Component
        }
    }

    @Override
    public String getPlatformName() {
        return "Velocity";
    }

    @Override
    public String getConfigString(String path) {
        // Flat TOML parsing logic previously in ConfigManager
        // This is a simplified version for common keys
        File configFile = new File(getDataFolder(), "velocity.toml");
        if (!configFile.exists())
            return null;

        String section = path.contains(".") ? path.split("\\.")[0] : "";
        String key = path.contains(".") ? path.split("\\.")[1] : path;

        try (java.util.Scanner scanner = new java.util.Scanner(configFile)) {
            String currentSection = "";
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1);
                    continue;
                }
                if (currentSection.equals(section) && line.startsWith(key)) {
                    return line.split("=", 2)[1].trim().replace("\"", "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getConfigInt(String path, int defaultValue) {
        String val = getConfigString(path);
        return val != null ? Integer.parseInt(val) : defaultValue;
    }

    @Override
    public java.util.Map<String, String> getTrackedPlugins() {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        File configFile = new File(getDataFolder(), "velocity.toml");
        if (!configFile.exists())
            return map;

        try (java.util.Scanner scanner = new java.util.Scanner(configFile)) {
            String currentSection = "";
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1);
                    continue;
                }
                if (currentSection.equals("plugins") && line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    map.put(parts[0].trim(), parts[1].trim().replace("\"", ""));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public String getStoredVersion(String repo) {
        File file = new File(getDataFolder(), "versions.properties");
        if (!file.exists())
            return "";
        java.util.Properties props = new java.util.Properties();
        try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
            props.load(in);
            return props.getProperty(repo, "");
        } catch (java.io.IOException e) {
            return "";
        }
    }

    @Override
    public void setStoredVersion(String repo, String hash) {
        File file = new File(getDataFolder(), "versions.properties");
        java.util.Properties props = new java.util.Properties();
        if (file.exists()) {
            try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
                props.load(in);
            } catch (java.io.IOException ignored) {
            }
        }
        props.setProperty(repo, hash);
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
            props.store(out, null);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
