package id.naturalsmp.naturalupdater;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import id.naturalsmp.naturalupdater.platform.VelocityPlatform;
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
        // Convert SLF4J logger to java.util.logging.Logger for the core
        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger("NaturalUpdater");

        this.core = new UpdaterPlugin(new VelocityPlatform(this, server, julLogger, dataDirectory));
        this.core.onEnable();
    }

    public UpdaterPlugin getCore() {
        return core;
    }
}
