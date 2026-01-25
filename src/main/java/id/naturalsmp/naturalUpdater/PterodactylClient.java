package id.naturalsmp.naturalUpdater;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class PterodactylClient {

    private final UpdaterPlugin plugin;
    private final HttpClient client;

    public PterodactylClient(UpdaterPlugin plugin) {
        this.plugin = plugin;
        this.client = HttpClient.newHttpClient();
    }

    public CompletableFuture<Boolean> restartServer() {
        ConfigManager config = plugin.getConfigManager();
        String url = String.format("%s/api/client/servers/%s/power", config.getPanelUrl(), config.getServerUuid());

        String json = "{\"signal\": \"restart\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + config.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 204);
    }
}
