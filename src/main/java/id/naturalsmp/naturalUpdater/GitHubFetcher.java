package id.naturalsmp.naturalUpdater;

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

    public CompletableFuture<String> getLatestReleaseDownloadUrl(String repoName, String extension) {
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
                                if (name.endsWith(extension)) {
                                    return asset.getString("browser_download_url");
                                }
                            }
                        }
                    }
                    return null;
                });
    }

    public CompletableFuture<JSONObject> createRelease(String repoName, String tagName, String name) {
        ConfigManager config = plugin.getConfigManager();
        String owner = config.getGithubOwner().trim();
        String token = config.getGithubToken().trim();
        String url = String.format("https://api.github.com/repos/%s/%s/releases", owner, repoName.trim());

        JSONObject body = new JSONObject();
        body.put("tag_name", tagName);
        body.put("name", name);
        body.put("body", "Automated pack upload via NaturalUpdater.");
        body.put("draft", true); // Create as draft to allow asset uploads before publishing
        body.put("prerelease", false);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", "token " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 201) {
                        return new JSONObject(response.body());
                    } else {
                        plugin.getLogger().severe(
                                "GitHub Release Error: Status " + response.statusCode() + " - " + response.body());
                        return null;
                    }
                });
    }

    public CompletableFuture<Boolean> publishRelease(String repoName, int releaseId) {
        ConfigManager config = plugin.getConfigManager();
        String owner = config.getGithubOwner().trim();
        String token = config.getGithubToken().trim();
        String url = String.format("https://api.github.com/repos/%s/%s/releases/%d", owner, repoName.trim(), releaseId);

        JSONObject body = new JSONObject();
        body.put("draft", false); // Publish the release

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", "token " + token)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 200);
    }

    public CompletableFuture<Boolean> uploadAsset(String uploadUrl, String fileName, java.io.File file) {
        ConfigManager config = plugin.getConfigManager();
        String url = String.format("%s?name=%s", uploadUrl, fileName);

        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("Authorization", "token " + config.getGithubToken())
                    .header("Content-Type", "application/octet-stream")
                    .POST(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                    .build();
        } catch (Exception e) {
            plugin.getLogger().severe("GitHub Upload Exception: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 201) {
                        return true;
                    } else {
                        plugin.getLogger().severe(
                                "GitHub Upload Error: Status " + response.statusCode() + " - " + response.body());
                        return false;
                    }
                });
    }

    public CompletableFuture<Boolean> triggerWorkflow(String repoName, String workflowFileName, JSONObject inputs) {
        ConfigManager config = plugin.getConfigManager();
        String owner = config.getGithubOwner().trim();
        String token = config.getGithubToken().trim();
        String url = String.format("https://api.github.com/repos/%s/%s/actions/workflows/%s/dispatches",
                owner, repoName.trim(), workflowFileName);

        JSONObject body = new JSONObject();
        body.put("ref", "master"); // Adjust branch if needed
        body.put("inputs", inputs);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", "token " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 204);
    }
}
