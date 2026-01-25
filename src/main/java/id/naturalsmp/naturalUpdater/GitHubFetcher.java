package id.naturalsmp.naturalupdater;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class GitHubFetcher {

    private final UpdaterPlugin plugin;
    private final HttpClient client;

    public GitHubFetcher(UpdaterPlugin plugin) {
        this.plugin = plugin;
        this.client = HttpClient.newHttpClient();
    }

    public CompletableFuture<String> getLatestCommitHash(String repoName) {
        ConfigManager config = plugin.getConfigManager();
        String url;
        if (repoName.contains("/")) {
            url = String.format("https://api.github.com/repos/%s/commits", repoName);
        } else {
            url = String.format("https://api.github.com/repos/%s/%s/commits", config.getGithubOwner(), repoName);
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
                        org.json.JSONArray json = new org.json.JSONArray(response.body());
                        if (!json.isEmpty()) {
                            return json.getJSONObject(0).getString("sha");
                        }
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
            url = String.format("https://api.github.com/repos/%s/%s/releases/latest", config.getGithubOwner(),
                    repoName);
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
                        if (json.has("assets")) {
                            org.json.JSONArray assets = json.getJSONArray("assets");
                            for (int i = 0; i < assets.length(); i++) {
                                JSONObject asset = assets.getJSONObject(i);
                                String name = asset.getString("name");
                                if (name.endsWith(".jar")) {
                                    return asset.getString("browser_download_url");
                                }
                            }
                        }
                    }
                    return null;
                });
    }
}
