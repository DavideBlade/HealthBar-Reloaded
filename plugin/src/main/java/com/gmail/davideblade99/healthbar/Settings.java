package com.gmail.davideblade99.healthbar;

import com.gmail.davideblade99.healthbar.BarShowCondition.*;
import com.gmail.davideblade99.healthbar.util.MobBarsUtil;
import com.gmail.davideblade99.healthbar.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public final int afterBarStyle;
    public final boolean afterBarUseTextMode;
    public final BarShowCondition afterBarShowCondition;
    public final int afterBarHideDelay;
    public final boolean useCustomAfterBar;
    public final BarType playerAfterBarType;
    public final boolean belowBarEnabled;
    public final String belowBarText;
    public final boolean belowBarUseHearts;
    public final boolean belowBarUseProportion;
    public final int belowBarProportion;
    public final List<String> playerDisabledWorlds; // Disabled worlds names

    /* Mob settings */
    public final boolean mobBarEnabled;
    public final NamedMobPolicy barOnNamedMobPolicy;
    public final int mobBarStyle;
    public final boolean mobBarUseTextMode;
    public final boolean mobBarUseCustomText;
    public final String mobBarCustomText;
    public final BarShowCondition mobBarShowCondition;
    public final int mobBarHideDelay;
    public final boolean useCustomMobBar;
    public final BarType mobBarType;
    public final List<String> mobDisabledWorlds; // Disabled worlds names
    public final List<EntityType> mobDisabledTypes; // Disabled mobs
    public final Map<String, String> localeMap; // Name translation map
    public final List<String> mobBar; // In bars mode, mob bars for various health %

    /* Generic settings */
    public final boolean barOnMythicMobs;
    public final String barOnLevelledMobs;
    public final boolean barOnAuraMobs;
    public final boolean barOnStackedMobs; // WildStacker integration
    public final boolean barInDeathMessages;
    public final boolean fixTabNames;
    public final boolean usePlayerPermissions;
    public final boolean overrideOtherScoreboards;

    public Settings(@NotNull final HealthBar plugin) {
        this.checkConfigYML(plugin);

        final FileConfiguration config = plugin.getConfig();

        /* Player settings */
        this.playerBarEnabled = config.getBoolean(Nodes.PLAYER_BAR_ENABLE.path);
        this.afterBarEnabled = config.getBoolean(Nodes.PLAYER_AFTERBAR_ENABLE.path);
        this.afterBarStyle = config.getInt(Nodes.PLAYER_AFTERBAR_STYLE.path);
        this.afterBarUseTextMode = config.getBoolean(Nodes.PLAYER_AFTERBAR_TEXT_MODE.path);
        final BarShowRuleParser.ParseResult showConditionParsing = BarShowRuleParser.parse(
                config.getString(Nodes.PLAYER_AFTERBAR_SHOW_CONDITION.path),
                config.getInt(Nodes.PLAYER_AFTERBAR_HIDE_DELAY.path),
                Set.of(Always.class, OnDamage.class, BelowPercentage.class)
        );
        this.afterBarShowCondition = showConditionParsing.showCondition;
        this.afterBarHideDelay = showConditionParsing.hideDelay;
        this.useCustomAfterBar = config.getBoolean(Nodes.PLAYER_AFTERBAR_USE_CUSTOM.path);

        if (this.useCustomAfterBar)
            this.playerAfterBarType = BarType.BAR; // Custom bars - highest priority on configs
        else if (!afterBarUseTextMode)
            this.playerAfterBarType = BarType.CUSTOM_TEXT; // Text - maybe custom - medium priority on configs
        else
            this.playerAfterBarType = BarType.DEFAULT_TEXT; // Default bar - low priority on configs

        this.belowBarEnabled = config.getBoolean(Nodes.PLAYER_BELOWBAR_ENABLE.path);
        this.belowBarText = Utils.replaceSymbols(config.getString(Nodes.PLAYER_BELOWBAR_TEXT.path));
        this.belowBarUseHearts = config.getBoolean(Nodes.PLAYER_BELOWBAR_DISPLAY_RAW_HEARTS.path);
        this.belowBarUseProportion = config.getBoolean(Nodes.PLAYER_BELOWBAR_USE_PROPORTION.path);
        this.belowBarProportion = config.getInt(Nodes.PLAYER_BELOWBAR_PROPORTIONAL_TO.path);
        this.playerDisabledWorlds = ImmutableList.copyOf(
                config.getString(Nodes.PLAYERS_DISABLED_WORLDS.path).toLowerCase().replace(" ", "").split(","));

        /* Mob settings */
        this.mobBarEnabled = config.getBoolean(Nodes.MOB_ENABLE.path);
        switch (config.getString(Nodes.MOB_SHOW_ON_NAMED.path).toLowerCase()) {
            case "no" -> this.barOnNamedMobPolicy = NamedMobPolicy.IGNORE;
            case "append" -> this.barOnNamedMobPolicy = NamedMobPolicy.APPEND;
            default -> this.barOnNamedMobPolicy = NamedMobPolicy.OVERRIDE;
        }
        this.mobBarStyle = config.getInt(Nodes.MOB_STYLE.path);
        this.mobBarUseTextMode = config.getBoolean(Nodes.MOB_TEXT_MODE.path);
        this.mobBarUseCustomText = config.getBoolean(Nodes.MOB_CUSTOM_TEXT_ENABLE.path);
        final BarShowRuleParser.ParseResult barAfterShowRule = BarShowRuleParser.parse(
                config.getString(Nodes.MOB_SHOW_CONDITION.path),
                config.getInt(Nodes.MOB_HIDE_DELAY.path),
                Set.of(Always.class, AlwaysWhenLooking.class, OnDamage.class, OnDamageOrLooking.class,
                        OnDamageAndLooking.class, BelowPercentage.class)
        );
        this.mobBarShowCondition = barAfterShowRule.showCondition;
        this.mobBarHideDelay = barAfterShowRule.hideDelay;
        this.useCustomMobBar = config.getBoolean(Nodes.MOB_USE_CUSTOM.path);

        String mobBarCustomText = Utils.replaceSymbols(config.getString(Nodes.MOB_CUSTOM_TEXT.path));
        if (useCustomMobBar) {
            // Custom bars - highest priority on configs
            this.mobBarType = BarType.BAR;
        } else if (mobBarUseTextMode) {
            // Text - maybe custom - medium priority on configs

            if (mobBarUseCustomText) {
                mobBarCustomText =
                        mobBarCustomText.replace("{health}", "{h}").replace("{max}", "{m}").replace("{name}", "{n}");
                this.mobBarType = BarType.CUSTOM_TEXT;
            } else
                this.mobBarType = BarType.DEFAULT_TEXT;
        } else {
            // Default bar - low priority on configs
            this.mobBarType = BarType.BAR;
        }
        this.mobBarCustomText = mobBarCustomText;

        this.mobDisabledWorlds = ImmutableList.copyOf(
                config.getString(Nodes.MOB_DISABLED_WORLDS.path).toLowerCase().replace(" ", "").split(","));
        this.mobDisabledTypes =
                ImmutableList.copyOf(Utils.getTypesFromString(config.getString(Nodes.MOB_DISABLED_TYPES.path)));
        this.localeMap = mobBarUseCustomText ? ImmutableMap.copyOf(Utils.getTranslationMap(plugin)) : ImmutableMap.of();

        if (mobBarType == BarType.BAR) {
            if (useCustomMobBar)
                this.mobBar = ImmutableList.copyOf(
                        MobBarsUtil.getCustomBars(Utils.loadYamlFile("custom-mob-bar.yml", plugin)));
            else
                this.mobBar = ImmutableList.copyOf(MobBarsUtil.getDefaultsBars(this.mobBarStyle));
        } else
            this.mobBar = null;


        this.barOnMythicMobs = !config.getBoolean(Nodes.HOOK_MYTHICMOBS.path);
        this.barOnLevelledMobs = config.getString(Nodes.HOOK_LEVELLEDMOBS.path);
        this.barOnAuraMobs = !config.getBoolean(Nodes.HOOK_AURAMOBS.path);
        this.barOnStackedMobs = !config.getBoolean(Nodes.HOOK_WILDSTACKER.path);
        this.barInDeathMessages = config.getBoolean(Nodes.BAR_IN_DEATH_MESSAGES.path);
        this.usePlayerPermissions = config.getBoolean(Nodes.USE_PLAYER_PERMISSIONS.path);
        this.fixTabNames = config.getBoolean(Nodes.FIX_TAB_NAMES.path);
        this.overrideOtherScoreboards = config.getBoolean(Nodes.OVERRIDE_OTHER_SCOREBOARD.path);
    }

    /**
     * Check if the mob's health bar is always set.
     *
     * Note that being always set does not mean that it is always displayed/visible. For example, this method may return
     * true and the bar may actually be set, but the player can only see it when looking at the mob.
     *
     * @return True if mobs health bar is always set, otherwise false.
     *
     * @since 2.0.5
     */
    public boolean isMobBarAlwaysSet() {
        return this.mobBarHideDelay == 0 || this.mobBarShowCondition instanceof OnDamageOrLooking;
    }

    /**
     * If it does not exist, copy the config.yml from the .jar to the plugin folder and then check that it has all the
     * settings inside. If any are missing, they are added with the default value.
     *
     * @param plugin Plugin instance
     */
    private void checkConfigYML(@NotNull final Plugin plugin) {
        Utils.loadYamlFile("config.yml",
                plugin); // Copy config.yml from the .jar to the plugin folder if it doesn't exist

        plugin.reloadConfig();

        final FileConfiguration config = plugin.getConfig();
        for (Nodes node : Nodes.values())
            if (!config.isSet(node.path))
                config.set(node.path, node.defaultValue);
        plugin.saveConfig();
    }

    /**
     * Utility class for parsing {@link BarShowCondition} from configuration strings and associating it with the
     * configured time after which the bar is hidden.
     *
     * @since 2.0.5
     */
    private static class BarShowRuleParser {

        /**
         * Parses a {@link BarShowCondition} by interpreting the string value into a valid showCondition and pairs it
         * with a hide time (in ticks). Only conditions included in {@code allowedTypes} are considered valid;
         * otherwise, {@code defaultCondition} is returned.
         *
         * @param showCondition The show showCondition read from the configuration
         * @param hideDelay     Time in seconds after which the bar is hidden once the showCondition is triggered (<=0
         *                      means never)
         * @param allowedTypes  the set of {@link Class} representing the allowed showCondition types
         *
         * @return A {@link ParseResult} containing the parsed showCondition and the hide time in ticks
         *
         * @since 2.0.5
         */
        private static ParseResult parse(@Nullable final String showCondition, int hideDelay,
                                         @NotNull final Set<Class<? extends BarShowCondition>> allowedTypes) {
            hideDelay = hideDelay <= 0 ?
                        -1 :
                        hideDelay * 20; // Non-positive values keep the bar indefinitely once triggered

            if (showCondition != null && !allowedTypes.isEmpty()) {

                if (showCondition.equalsIgnoreCase("always") && allowedTypes.contains(Always.class))
                    return new ParseResult(new Always(), 0);

                if (showCondition.equalsIgnoreCase("always-when-look") &&
                        allowedTypes.contains(AlwaysWhenLooking.class))
                    return new ParseResult(new AlwaysWhenLooking(), 0);

                if (showCondition.equalsIgnoreCase("on-damage") && allowedTypes.contains(OnDamage.class))
                    return new ParseResult(new OnDamage(), hideDelay);

                if (showCondition.equalsIgnoreCase("on-damage-or-look") &&
                        allowedTypes.contains(OnDamageOrLooking.class))
                    return new ParseResult(new OnDamageOrLooking(), hideDelay);

                if (showCondition.equalsIgnoreCase("on-damage-and-look") &&
                        allowedTypes.contains(OnDamageAndLooking.class))
                    return new ParseResult(new OnDamageAndLooking(), hideDelay);

                try {
                    if (showCondition.matches("^below-[0-9]+%$") && allowedTypes.contains(BelowPercentage.class))
                        return new ParseResult(
                                new BelowPercentage(Integer.parseInt(showCondition.replaceAll("[^0-9]", ""))),
                                hideDelay == -1 ? 0 : hideDelay
                                // Set to 0 (as "always" conditions) because the bar must always be shown when the
                                // health is below the threshold
                        );
                } catch (final IllegalArgumentException ignored) {
                    // Fallback to default show condition
                }
            }

            return new ParseResult(new OnDamage(), hideDelay); // Default show condition
        }

        /**
         * Parsed {@link BarShowCondition} from configuration along with the time (in ticks) after which the bar is
         * hidden once the show showCondition is triggered.
         *
         * @param showCondition Parsed show showCondition
         * @param hideDelay     Time in ticks after which the bar is hidden
         *
         * @since 2.0.5
         */
        private record ParseResult(BarShowCondition showCondition, int hideDelay) {}
    }

    /** List of nodes (settings) in config.yml */
    private enum Nodes {

        PLAYER_BAR_ENABLE("player-bars.enable", true),

        PLAYER_AFTERBAR_ENABLE("player-bars.after-name.enable", true),
        PLAYER_AFTERBAR_STYLE("player-bars.after-name.display-style", 1),
        PLAYER_AFTERBAR_SHOW_CONDITION("player-bars.after-name.show-condition", "on-damage"),
        PLAYER_AFTERBAR_HIDE_DELAY("player-bars.after-name.hide-delay-seconds", 5),
        PLAYER_AFTERBAR_TEXT_MODE("player-bars.after-name.text-mode", false),
        PLAYER_AFTERBAR_USE_CUSTOM("player-bars.after-name.use-custom-file", false),

        PLAYER_BELOWBAR_ENABLE("player-bars.below-name.enable", true),
        PLAYER_BELOWBAR_TEXT("player-bars.below-name.text", "% &cHealth"),
        PLAYER_BELOWBAR_DISPLAY_RAW_HEARTS("player-bars.below-name.display-raw-hearts", false),
        PLAYER_BELOWBAR_USE_PROPORTION("player-bars.below-name.use-proportion", true),
        PLAYER_BELOWBAR_PROPORTIONAL_TO("player-bars.below-name.proportional-to", 100),

        PLAYERS_DISABLED_WORLDS("player-bars.disabled-worlds", "world_nether,world_the_end"),

        MOB_ENABLE("mob-bars.enable", true),
        MOB_SHOW_ON_NAMED("mob-bars.show-on-named-mobs", "override"),
        MOB_STYLE("mob-bars.display-style", 1),
        MOB_SHOW_CONDITION("mob-bars.show-condition", "on-damage"),
        MOB_HIDE_DELAY("mob-bars.hide-delay-seconds", 5),
        MOB_TEXT_MODE("mob-bars.text-mode", false),
        MOB_CUSTOM_TEXT_ENABLE("mob-bars.custom-text-enable", false),
        MOB_CUSTOM_TEXT("mob-bars.custom-text", "{name} - &a{health}/{max}"),
        MOB_USE_CUSTOM("mob-bars.use-custom-file", false),
        MOB_DISABLED_WORLDS("mob-bars.disabled-worlds", "world_nether,world_the_end"),
        MOB_DISABLED_TYPES("mob-bars.disabled-types", "creeper,zombie,skeleton,iron_golem"),

        HOOK_MYTHICMOBS("hooks.MythicMobs", false),
        HOOK_LEVELLEDMOBS("hooks.LevelledMobs", "LevelledMobs"),
        HOOK_AURAMOBS("hooks.AuraMobs", false),
        HOOK_WILDSTACKER("hooks.WildStacker", false),
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
