package id.naturalsmp.naturalupdater.platform;

import java.io.File;
import java.util.logging.Logger;

public interface UpdaterPlatform {
    Logger getLogger();

    File getDataFolder();

    File getUpdateFolder();

    void scheduleAsync(Runnable task, long delayTicks, long intervalTicks);

    void scheduleSync(Runnable task);

    void dispatchCommand(String command);

    String getPlatformName();
}
