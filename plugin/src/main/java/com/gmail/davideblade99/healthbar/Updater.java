package com.gmail.davideblade99.healthbar;

import org.bukkit.Bukkit;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class Updater {

    private final Plugin plugin;

    public Updater(@NotNull final Plugin plugin) {
        this.plugin = plugin;
    }

    public interface ResponseHandler {

        /**
         * Called when the updater finds a new version.
         *
         * @param newVersion - the new version
         */
        void onUpdateFound(@NotNull final String newVersion);
    }

    public void checkForUpdate(@NotNull final ResponseHandler responseHandler) {
        new Thread(() -> {
            try {
                final HttpURLConnection con = (HttpURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=104616").openConnection();
                final String newVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();

                if (isNewerVersion(newVersion)) {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        if (newVersion.contains(" "))
                            responseHandler.onUpdateFound(newVersion.split(" ")[0]);
                        else
                            responseHandler.onUpdateFound(newVersion);
                    });
                }
            } catch (final IOException e) {
                Bukkit.getConsoleSender().sendMessage("§cCould not contact Spigot to check for updates.");
            } catch (final IllegalPluginAccessException ignored) {
                // Plugin not enabled
            } catch (final Exception e) {
                e.printStackTrace();
                Bukkit.getConsoleSender().sendMessage("§cUnable to check for updates: unhandled exception.");
            }
        }).start();
    }

    /**
     * Compare the version found with the plugin's version
     *
     * @param versionOfSpigot Version found on SpigotMC
     *
     * @return True if the passed version does not match the plugin version, otherwise false
     */
    private boolean isNewerVersion(@Nullable final String versionOfSpigot) {
        return !plugin.getDescription().getVersion().equals(versionOfSpigot);
    }
}