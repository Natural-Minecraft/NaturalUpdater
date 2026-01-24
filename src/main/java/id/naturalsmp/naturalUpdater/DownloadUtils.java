package id.naturalsmp.naturalupdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class DownloadUtils {

    private static final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    public static CompletableFuture<File> downloadFile(String url, String fileName, File targetDir) {
        if (!targetDir.exists())
            targetDir.mkdirs();
        File targetFile = new File(targetDir, fileName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try (InputStream is = response.body();
                                FileOutputStream fos = new FileOutputStream(targetFile)) {
                            byte[] buffer = new byte[8192];
                            int read;
                            while ((read = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, read);
                            }
                            System.out.println("[NaturalUpdater] Download complete: " + targetFile.getAbsolutePath());
                            return targetFile;
                        } catch (Exception e) {
                            System.err.println("[NaturalUpdater] Failed to write file: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("[NaturalUpdater] HTTP error downloading: " + response.statusCode());
                    }
                    return null;
                });
    }
}
