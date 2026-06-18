package id.naturalsmp.naturalUpdater;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import id.naturalsmp.naturalUpdater.platform.VelocityPlatform;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "naturalupdater", name = "NaturalUpdater", version = "1.0-SNAPSHOT", authors = { "NaturalSMP" })
public class NaturalVelocityUpdater {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private UpdaterPlugin core;

    @Inject
    public NaturalVelocityUpdater(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getConsoleCommandSource().sendMessage(
                net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(
                    "\n&a================================================================================\n" +
                    "&a _   _       _                               _      &e _   _           _       _\n" +
                    "&a| \\ | | __ _| |_ _   _ _ __ __ _  | |   &e| | | |_ __   __| | __ _| |_ ___ _ __\n" +
                    "&a|  \\| |/ _` | __| | | | '__/ _` | | |   &e| | | | '_ \\ / _` |/ _` | __/ _ \\ '__|\n" +
                    "&a| |\\  | (_| | |_| |_| | | | (_| | | |   &e| |_| | |_) | (_| | (_| | ||  __/ |\n" +
                    "&a|_| \\_|\\__,_|\\__|\\__,_|_|  \\__,_|_|_|   &e \\___/| .__/ \\__,_|\\__,_|\\__\\___|_|\n" +
                    "                                                 &e|_|\n" +
                    "          &f>> &eNaturalUpdater v1.0-SNAPSHOT Enabled! <<\n" +
                    "&a================================================================================\n"
                )
        );
        // Convert SLF4J logger to java.util.logging.Logger for the core
        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger("NaturalUpdater");

        this.core = new UpdaterPlugin(new VelocityPlatform(this, server, julLogger, dataDirectory));
        this.core.onEnable();

        // Register Command
        com.velocitypowered.api.command.CommandManager cmdManager = server.getCommandManager();
        com.velocitypowered.api.command.CommandMeta meta = cmdManager.metaBuilder("vupdater")
                .aliases("vup")
                .plugin(this)
                .build();

        cmdManager.register(meta, new VelocityUpdaterCommand(core));
    }

    public UpdaterPlugin getCore() {
        return core;
    }
}
