package id.naturalsmp.naturalupdater;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.json.JSONObject;

public class GitHubFetcher {

    private final NaturalUpdater plugin;
    private final HttpClient client;

    public GitHubFetcher(NaturalUpdater plugin) {
        this.plugin = plugin;
        this.client = HttpClient.newHttpClient();
    }

    public CompletableFuture<String> getLatestCommitHash(String repoName) {
        ConfigManager config = plugin.getConfigManager();
        String url;
        if (repoName.contains("/")) {
            url = String.format("https://api.github.com/repos/%s/commits/main", repoName);
        } else {
            url = String.format("https://api.github.com/repos/%s/%s/commits/main", config.getGithubOwner(), repoName);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", "token " + config.getGithubToken())
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        JSONObject json = new JSONObject(response.body());
                        return json.getString("sha");
                    }
                    return null;
                });
    }

    public CompletableFuture<String> getLatestReleaseDownloadUrl(String repoName) {
        ConfigManager config = plugin.getConfigManager();
        String url;
        if (repoName.contains("/")) {
            url = String.format("https://api.github.com/repos/%s/releases/latest", repoName);
        } else {
            url = String.format("https://api.github.com/repos/%s/%s/releases/latest", config.getGithubOwner(), repoName);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", "token " + config.getGithubToken())
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        JSONObject json = new JSONObject(response.body());
                        // Fetch the first asset URL
                        if (json.has("assets") && !json.getJSONArray("assets").isEmpty()) {
                            return json.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
                        }
                    }
                    return null;
                });
    }
}
