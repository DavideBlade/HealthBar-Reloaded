package com.gmail.davideblade99.healthbar;

import com.gmail.davideblade99.healthbar.Updater.ResponseHandler;
import com.gmail.davideblade99.healthbar.api.internal.BackendAPI;
import com.gmail.davideblade99.healthbar.command.Commands;
import com.gmail.davideblade99.healthbar.listener.*;
import com.gmail.davideblade99.healthbar.manager.EntityTrackerManager;
import com.gmail.davideblade99.healthbar.manager.PlayerBarManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;


public final class HealthBar extends JavaPlugin {

    private final static String[] SUPPORTED_VERSIONS = {"1.14", "1.15", "1.16", "1.17", "1.18", "1.19"};
    public final static String CHAT_PREFIX = "§2[§aHealthBar§2] ";

    private static HealthBar instance;
    private Settings settings;
    private EntityTrackerManager entityTrackerManager;
    private PlayerBarManager playerBarManager;
    public NamespacedKey namespace;

    @Override
    public void onEnable() {
        if (!supportedVersion()) {
            final ConsoleCommandSender console = Bukkit.getConsoleSender();
            console.sendMessage("§cThis version of HealthBar is compatible with the following versions: " + String.join(", ", SUPPORTED_VERSIONS));
            console.sendMessage("§cThe server is on a different version and the plugin may not work properly");
        }

        instance = this;
        namespace = new NamespacedKey(this, "HealthBar");
        settings = new Settings(this);
        playerBarManager = new PlayerBarManager(this);
        entityTrackerManager = new EntityTrackerManager(this);

        registerListeners();
        registerCommands();

        showHealthBar();

        BackendAPI.setImplementation(new DefaultBackendAPI(this));

        // Check for update
        new Updater(this).checkForUpdate(new ResponseHandler() {
            @Override
            public void onUpdateFound(@NotNull final String newVersion) {
                final String pluginVersion = getDescription().getVersion();
                final String currentVersion = pluginVersion.contains(" ") ? pluginVersion.split(" ")[0] : pluginVersion;

                final ConsoleCommandSender console = Bukkit.getConsoleSender();
                console.sendMessage("§aFound a new version: " + newVersion + " (Yours: v" + currentVersion + ")");
                console.sendMessage("§aDownload it on spigot:");
                console.sendMessage("§aspigotmc.org/resources/104616");
            }
        });
    }

    @Override
    public void onDisable() {
        playerBarManager.clearHealthBars();
        entityTrackerManager.removeAllMobHealthBars();

        instance = null;
        settings = null;
        entityTrackerManager = null;
        playerBarManager = null;
        namespace = null;

        Bukkit.getConsoleSender().sendMessage("§aHealthBar disabled: all the health bars have been removed.");
    }

    /**
     * Reloads the different configuration files
     */
    public void reloadConfigFromDisk() {
        this.settings = new Settings(this);
        this.playerBarManager.reload();
        this.entityTrackerManager.removeAllMobHealthBars();

        showHealthBar();
    }

    /**
     * Show the health bar of all entities in the server
     */
    private void showHealthBar() {
        // Show health bar on all entities
        if (settings.mobBarHideDelay == 0) {
            for (World world : Bukkit.getWorlds()) {
                for (LivingEntity mob : world.getLivingEntities())
                    if (mob.getType() != EntityType.PLAYER)
                        entityTrackerManager.registerMobHit(mob, true);
            }
        }

        // Set health bar for players already connected (possible in case of /reload)
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerBarManager.updatePlayer(player);
            playerBarManager.updateScoreboard(player);
            playerBarManager.fixTabName(player);
        }
    }

    /**
     * Register all listeners of the plugin
     */
    private void registerListeners() {
        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new HealthListener(this), this);
        pm.registerEvents(new PlayerRenameEntityListener(this), this);
        pm.registerEvents(new InventoryListener(this), this);

        // Register listener only if the health bar of players is enabled
        if (settings.playerBarEnabled) {
            pm.registerEvents(new PlayerJoinListener(this), this);
            pm.registerEvents(new PlayerTeleportListener(this), this);
            pm.registerEvents(new PlayerRespawnListener(this), this);
        }

        // Register listener only if the health bar of mobs is enabled
        if (settings.mobBarEnabled) {
            pm.registerEvents(new ChunkListener(this), this);
            pm.registerEvents(new EntitySpawnListener(this), this);
        }

        if (!settings.barInDeathMessages) // Register listener only if the plugin needs to fix death messages
            pm.registerEvents(new PlayerDeathListener(this), this);
    }

    /**
     * Registers all plugin commands
     */
    private void registerCommands() {
        getCommand("healthbar").setExecutor(new Commands(this));
    }

    /**
     * @return true if the Minecraft server version is supported, otherwise false
     */
    private boolean supportedVersion() {
        final String serverVersion = Bukkit.getVersion();
        for (String version : SUPPORTED_VERSIONS)
            if (serverVersion.contains(version))
                return true;

        return false;
    }

    public Settings getSettings() {
        return settings;
    }

    public static HealthBar getInstance() {
        return instance;
    }

    public EntityTrackerManager getEntityTrackerManager() {
        return entityTrackerManager;
    }

    public PlayerBarManager getPlayerBarManager() {
        return playerBarManager;
    }
}
