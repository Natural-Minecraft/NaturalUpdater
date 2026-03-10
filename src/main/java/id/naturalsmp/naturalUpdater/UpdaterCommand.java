package id.naturalsmp.naturalUpdater;

import id.naturalsmp.naturalUpdater.platform.UpdaterPlatform;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.JSONObject;
import java.io.File;
import java.util.Map;

public class UpdaterCommand implements CommandExecutor {

    private final UpdaterPlugin plugin;

    public UpdaterCommand(UpdaterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("natural.updater.admin")) {
            plugin.getPlatform().sendMessage(sender, "§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            showStatus(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("sync")) {
            plugin.getPlatform().sendMessage(sender,
                    "§6§lNaturalUpdater §8» §fMemulai sinkronisasi plugin dari GitHub...");
            syncAll(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().reload();
            plugin.getPlatform().sendMessage(sender, "§6§lNaturalUpdater §8» §aKonfigurasi berhasil di-reload!");
            return true;
        }

        if (args[0].equalsIgnoreCase("restart")) {
            plugin.getPlatform().sendMessage(sender, "§6§lNaturalUpdater §8» §fMemicu restart via NaturalCore...");
            plugin.getPlatform().dispatchCommand("restartalert 30");
            return true;
        }

        if (args[0].equalsIgnoreCase("geyser")) {
            handleGeyserExport(sender);
            return true;
        }

        return false;
    }

    private void handleGeyserExport(CommandSender sender) {
        plugin.getPlatform().sendMessage(sender, "§6§lGeyserPack §8» §fMencari ItemsAdder resource pack...");

        // ItemsAdder Output Path
        File iaFolder = new File(plugin.getPlatform().getDataFolder().getParentFile(), "ItemsAdder");
        File packFile = new File(iaFolder, "output/generated.zip");

        if (!packFile.exists()) {
            plugin.getPlatform().sendMessage(sender,
                    "§cError: ItemsAdder generated.zip tidak ditemukan di " + packFile.getAbsolutePath());
            return;
        }

        plugin.getPlatform().sendMessage(sender, "§6§lGeyserPack §8» §fMengupload ke GitHub NaturalPacks...");

        String timeTag = new java.text.SimpleDateFormat("yyyyMMdd-HHmm").format(new java.util.Date());
        String tagName = "pack-" + timeTag;

        GitHubFetcher fetcher = plugin.getUpdateScheduler().getFetcher();
        fetcher.createRelease("NaturalPacks", tagName, "Resource Pack Update " + timeTag).thenAccept(releaseJson -> {
            if (releaseJson == null) {
                plugin.getPlatform().sendMessage(sender,
                        "§cError: Gagal membuat rilis di GitHub. Pastikan Repo 'NaturalPacks' ada.");
                return;
            }

            String uploadUrl = releaseJson.getString("upload_url").split("\\{")[0];

            fetcher.uploadAsset(uploadUrl, "generated.zip", packFile).thenAccept(success -> {
                if (success) {
                    plugin.getPlatform().sendMessage(sender,
                            "§6§lGeyserPack §8» §fUpload selesai. Mentrigger konversi...");

                    JSONObject inputs = new JSONObject();
                    inputs.put("tag", tagName);

                    fetcher.triggerWorkflow("NaturalPacks", "convert.yml", inputs).thenAccept(triggered -> {
                        if (triggered) {
                            plugin.getPlatform().sendMessage(sender,
                                    "§6§lGeyserPack §8» §aBerhasil! Konversi sedang berjalan di GitHub.");
                            plugin.getPlatform().sendMessage(sender,
                                    "§7Silakan tunggu beberapa menit hingga pack tersedia di Velocity.");
                        } else {
                            plugin.getPlatform().sendMessage(sender,
                                    "§cError: Gagal mentrigger Action GitHub. Cek izin Token (harus ada 'workflow').");
                        }
                    });
                } else {
                    plugin.getPlatform().sendMessage(sender,
                            "§cError: Gagal mengupload file ke GitHub (Draft rilis dibuat tanpa asset).");
                }
            });
        });
    }

    private void showStatus(CommandSender sender) {
        sender.sendMessage("§6§lNaturalUpdater Status §8»");
        sender.sendMessage("§7Platform: §f" + plugin.getPlatform().getPlatformName());
        sender.sendMessage("§7Ptero URL: §f" + plugin.getConfigManager().getPanelUrl());
        sender.sendMessage("§7GitHub Owner: §f" + plugin.getConfigManager().getGithubOwner());
        sender.sendMessage(
                "§7Tracked Plugins: §f" + String.join(", ", plugin.getConfigManager().getTrackedPlugins().keySet()));
    }

    private void syncAll(CommandSender sender) {
        Map<String, String> plugins = plugin.getConfigManager().getTrackedPlugins();
        File updateDir = plugin.getPlatform().getUpdateFolder();

        for (Map.Entry<String, String> entry : plugins.entrySet()) {
            String repo = entry.getKey();
            String jarName = entry.getValue();

            if (repo.startsWith("http://") || repo.startsWith("https://")) {
                sender.sendMessage("§7Downloading §e" + jarName + "§7 from URL...");
                DownloadUtils.downloadFile(repo, jarName, updateDir).thenAccept(file -> {
                    if (file != null) {
                        sender.sendMessage("§aSuccessfully staged §f" + jarName + " §ain /update folder.");
                    } else {
                        sender.sendMessage("§cFailed to download §f" + jarName);
                    }
                });
                continue;
            }

            plugin.getUpdateScheduler().getFetcher().getLatestReleaseDownloadUrl(repo, ".jar").thenAccept(url -> {
                if (url != null) {
                    sender.sendMessage("§7Downloading §e" + repo + "§7...");
                    DownloadUtils.downloadFile(url, jarName, updateDir).thenAccept(file -> {
                        if (file != null) {
                            sender.sendMessage("§aSuccessfully staged §f" + jarName + " §ain /update folder.");
                        } else {
                            sender.sendMessage("§cFailed to download §f" + jarName);
                        }
                    });
                } else {
                    sender.sendMessage("§cNo release found for repo: §f" + repo);
                }
            });
        }
    }
}
