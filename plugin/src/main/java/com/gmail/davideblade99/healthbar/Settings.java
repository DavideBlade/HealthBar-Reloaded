package com.gmail.davideblade99.healthbar;

import com.gmail.davideblade99.healthbar.util.MobBarsUtil;
import com.gmail.davideblade99.healthbar.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Class that contains all the settings in the config.yml
 */
public final class Settings {

    /*
     * Player settings - 2 health bars:
     * 1) After bar -> located after the name of the player
     * 2) Below bar -> located below the player's name
     */
    public final boolean playerBarEnabled;
    public final boolean afterBarEnabled;
    public final int barAfterStyle;
    public final boolean barAfterUseTextMode;
    public final int barAfterHideDelay;
    public final boolean useCustomBarAfter;
    public final BarType playerAfterBarType;
    public final boolean belowBarEnabled;
    public final String belowBarText;
    public final boolean belowBarUseHearts;
    public final boolean belowBarUseProportion;
    public final int belowBarProportion;
    public final List<String> playerDisabledWorlds; // Disabled worlds names

    /* Mob settings */
    public final boolean mobBarEnabled;
    public final boolean showMobBarOnCustomNames;
    public final int mobBarStyle;
    public final boolean mobBarUseTextMode;
    public final boolean mobBarUseCustomText;
    public final String mobBarCustomText;
    public final int mobBarHideDelay;
    public final boolean mobBarSemiHidden;
    public final boolean useCustomMobBar;
    public final BarType mobBarType;
    public final List<String> mobDisabledWorlds; // Disabled worlds names
    public final List<EntityType> mobDisabledTypes; // Disabled mobs
    public final Map<String, String> localeMap; // Name translation map
    public final List<String> mobBar;

    /* Generic settings */
    public final boolean barOnMythicMobs;
    public final boolean barInDeathMessages;
    public final boolean fixTabNames;
    public final boolean usePlayerPermissions;
    public final boolean overrideOtherScoreboards;

    public Settings(@NotNull final HealthBar plugin) {
        checkConfigYML(plugin);

        final FileConfiguration config = plugin.getConfig();

        /* Player settings */
        this.playerBarEnabled = config.getBoolean(Nodes.PLAYER_BAR_ENABLE.path);
        this.afterBarEnabled = config.getBoolean(Nodes.PLAYER_AFTERBAR_ENABLE.path);
        this.barAfterStyle = config.getInt(Nodes.PLAYER_AFTERBAR_STYLE.path);
        this.barAfterUseTextMode = config.getBoolean(Nodes.PLAYER_AFTERBAR_TEXT_MODE.path);
        this.barAfterHideDelay = config.getBoolean(Nodes.PLAYER_AFTERBAR_ALWAYS_SHOWN.path) ? 0 : config.getInt(Nodes.PLAYER_AFTERBAR_DELAY.path) * 20;
        this.useCustomBarAfter = config.getBoolean(Nodes.PLAYER_AFTERBAR_USE_CUSTOM.path);

        if (useCustomBarAfter)
            playerAfterBarType = BarType.BAR; // Custom bars - highest priority on configs
        else if (!barAfterUseTextMode)
            playerAfterBarType = BarType.CUSTOM_TEXT; // Text - maybe custom - medium priority on configs
        else
            playerAfterBarType = BarType.DEFAULT_TEXT; // Default bar - low priority on configs

        this.belowBarEnabled = config.getBoolean(Nodes.PLAYER_BELOWBAR_ENABLE.path);
        this.belowBarText = Utils.replaceSymbols(config.getString(Nodes.PLAYER_BELOWBAR_TEXT.path));
        this.belowBarUseHearts = config.getBoolean(Nodes.PLAYER_BELOWBAR_DISPLAY_RAW_HEARTS.path);
        this.belowBarUseProportion = config.getBoolean(Nodes.PLAYER_BELOWBAR_USE_PROPORTION.path);
        this.belowBarProportion = config.getInt(Nodes.PLAYER_BELOWBAR_PROPORTIONAL_TO.path);
        this.playerDisabledWorlds = ImmutableList.copyOf(config.getString(Nodes.PLAYERS_DISABLED_WORLDS.path).toLowerCase().replace(" ", "").split(","));

        /* Mob settings */
        this.mobBarEnabled = config.getBoolean(Nodes.MOB_ENABLE.path);
        this.showMobBarOnCustomNames = config.getBoolean(Nodes.MOB_SHOW_ON_NAMED.path);
        this.mobBarStyle = config.getInt(Nodes.MOB_STYLE.path);
        this.mobBarUseTextMode = config.getBoolean(Nodes.MOB_TEXT_MODE.path);
        this.mobBarUseCustomText = config.getBoolean(Nodes.MOB_CUSTOM_TEXT_ENABLE.path);
        this.mobBarHideDelay = config.getBoolean(Nodes.MOB_ALWAYS_SHOWN.path) ? 0 : config.getInt(Nodes.MOB_DELAY.path) * 20;
        this.mobBarSemiHidden = config.getBoolean(Nodes.MOB_SHOW_IF_LOOKING.path);
        this.useCustomMobBar = config.getBoolean(Nodes.MOB_USE_CUSTOM.path);

        String mobBarCustomText = Utils.replaceSymbols(config.getString(Nodes.MOB_CUSTOM_TEXT.path));
        if (useCustomMobBar) {
            // Custom bars - highest priority on configs
            this.mobBarType = BarType.BAR;
        } else if (mobBarUseTextMode) {
            // Text - maybe custom - medium priority on configs

            if (mobBarUseCustomText) {
                mobBarCustomText = mobBarCustomText.replace("{health}", "{h}").replace("{max}", "{m}").replace("{name}", "{n}");
                this.mobBarType = BarType.CUSTOM_TEXT;
            } else
                this.mobBarType = BarType.DEFAULT_TEXT;
        } else {
            // Default bar - low priority on configs
            this.mobBarType = BarType.BAR;
        }
        this.mobBarCustomText = mobBarCustomText;

        this.mobDisabledWorlds = ImmutableList.copyOf(config.getString(Nodes.MOB_DISABLED_WORLDS.path).toLowerCase().replace(" ", "").split(","));
        this.mobDisabledTypes = ImmutableList.copyOf(Utils.getTypesFromString(config.getString(Nodes.MOB_DISABLED_TYPES.path)));
        this.localeMap = mobBarUseCustomText ? ImmutableMap.copyOf(Utils.getTranslationMap(plugin)) : ImmutableMap.of();

        if (mobBarType == BarType.BAR) {
            if (useCustomMobBar)
                this.mobBar = ImmutableList.copyOf(MobBarsUtil.getCustomBars(Utils.loadYamlFile("custom-mob-bar.yml", plugin)));
            else
                this.mobBar = ImmutableList.copyOf(MobBarsUtil.getDefaultsBars(this.mobBarStyle));
        } else
            this.mobBar = ImmutableList.copyOf(Utils.initialiseEmptyStringArray(21)); // Setup for health array


        this.barOnMythicMobs = !config.getBoolean(Nodes.HOOK_MYTHICMOBS.path);
        this.barInDeathMessages = config.getBoolean(Nodes.BAR_IN_DEATH_MESSAGES.path);
        this.usePlayerPermissions = config.getBoolean(Nodes.USE_PLAYER_PERMISSIONS.path);
        this.fixTabNames = config.getBoolean(Nodes.FIX_TAB_NAMES.path);
        this.overrideOtherScoreboards = config.getBoolean(Nodes.OVERRIDE_OTHER_SCOREBOARD.path);
    }

    /**
     * If it does not exist, copy the config.yml from the .jar to the plugin folder and then check that it has all
     * the settings inside. If any are missing, they are added with the default value.
     *
     * @param plugin Plugin instance
     */
    private void checkConfigYML(@NotNull final Plugin plugin) {
        Utils.loadYamlFile("config.yml", plugin); // Copy config.yml from the .jar to the plugin folder if it doesn't exist

        plugin.reloadConfig();

        final FileConfiguration config = plugin.getConfig();
        for (Nodes node : Nodes.values())
            if (!config.isSet(node.path))
                config.set(node.path, node.defaultValue);
        plugin.saveConfig();
    }

    /**
     * List of nodes (settings) in config.yml
     */
    private enum Nodes {

        PLAYER_BAR_ENABLE("player-bars.enable", true),

        PLAYER_AFTERBAR_ENABLE("player-bars.after-name.enable", true),
        PLAYER_AFTERBAR_STYLE("player-bars.after-name.display-style", 1),
        PLAYER_AFTERBAR_ALWAYS_SHOWN("player-bars.after-name.always-shown", false),
        PLAYER_AFTERBAR_TEXT_MODE("player-bars.after-name.text-mode", false),
        PLAYER_AFTERBAR_DELAY("player-bars.after-name.hide-delay-seconds", 5),
        PLAYER_AFTERBAR_USE_CUSTOM("player-bars.after-name.use-custom-file", false),

        PLAYER_BELOWBAR_ENABLE("player-bars.below-name.enable", true),
        PLAYER_BELOWBAR_TEXT("player-bars.below-name.text", "% &cHealth"),
        PLAYER_BELOWBAR_DISPLAY_RAW_HEARTS("player-bars.below-name.display-raw-hearts", false),
        PLAYER_BELOWBAR_USE_PROPORTION("player-bars.below-name.use-proportion", true),
        PLAYER_BELOWBAR_PROPORTIONAL_TO("player-bars.below-name.proportional-to", 100),

        PLAYERS_DISABLED_WORLDS("player-bars.disabled-worlds", "world_nether,world_the_end"),

        MOB_ENABLE("mob-bars.enable", true),
        MOB_SHOW_ON_NAMED("mob-bars.show-on-named-mobs", true),
        MOB_STYLE("mob-bars.display-style", 1),
        MOB_ALWAYS_SHOWN("mob-bars.always-shown", false),
        MOB_TEXT_MODE("mob-bars.text-mode", false),
        MOB_CUSTOM_TEXT_ENABLE("mob-bars.custom-text-enable", false),
        MOB_CUSTOM_TEXT("mob-bars.custom-text", "{name} - &a{health}/{max}"),
        MOB_DELAY("mob-bars.hide-delay-seconds", 5),
        MOB_SHOW_IF_LOOKING("mob-bars.show-only-if-looking", false),
        MOB_USE_CUSTOM("mob-bars.use-custom-file", false),
        MOB_DISABLED_WORLDS("mob-bars.disabled-worlds", "world_nether,world_the_end"),
        MOB_DISABLED_TYPES("mob-bars.disabled-types", "creeper,zombie,skeleton,iron_golem"),

        HOOK_MYTHICMOBS("hooks.MythicMobs", false),
        FIX_TAB_NAMES("fix-tab-names", true),
        BAR_IN_DEATH_MESSAGES("bar-in-death-messages", false),
        USE_PLAYER_PERMISSIONS("use-player-bar-permissions", false),
        OVERRIDE_OTHER_SCOREBOARD("override-other-scoreboard", false);

        private final String path;
        private final Object defaultValue;

        Nodes(@NotNull final String path, @NotNull final Object defaultValue) {
            this.path = path;
            this.defaultValue = defaultValue;
        }
    }
}
