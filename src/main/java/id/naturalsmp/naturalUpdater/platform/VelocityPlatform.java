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
}
