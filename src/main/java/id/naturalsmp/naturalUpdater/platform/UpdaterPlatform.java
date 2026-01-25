package id.naturalsmp.naturalUpdater.platform;

import java.io.File;
import java.util.logging.Logger;

public interface UpdaterPlatform {
    Logger getLogger();

    File getDataFolder();

    File getUpdateFolder();

    void scheduleAsync(Runnable task, long delayTicks, long intervalTicks);

    void scheduleSync(Runnable task);

    void dispatchCommand(String command);

    void sendMessage(Object sender, String message);

    String getPlatformName();

    // Agnostic Config/Storage Access
    String getConfigString(String path);

    int getConfigInt(String path, int defaultValue);

    java.util.Map<String, String> getTrackedPlugins();

    String getStoredVersion(String repo);

    void setStoredVersion(String repo, String hash);
}
