package com.gmail.davideblade99.healthbar.manager;

import com.gmail.davideblade99.healthbar.BarType;
import com.gmail.davideblade99.healthbar.HealthBar;
import com.gmail.davideblade99.healthbar.Permissions;
import com.gmail.davideblade99.healthbar.Settings;
import com.gmail.davideblade99.healthbar.api.BarHideEvent;
import com.gmail.davideblade99.healthbar.util.Utils;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

/**
 * Class that handles the bars next to the names of the players
 */
public final class PlayerBarManager {

    /** Scoreboard used to display health bars */
    private final static Scoreboard FAKE_SCOREBOARD = HealthBar.getInstance().getServer().getScoreboardManager().getNewScoreboard();

    private final HealthBar plugin;
    /** Scoreboard used to hide health bars from players without permission */
    private final Scoreboard mainScoreboard;
    private Objective belowObj = null;

    public PlayerBarManager(@NotNull final HealthBar plugin) {
        this.plugin = plugin;
        this.mainScoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();

        setupHealthBars();
    }

    /**
     * Update the player's health bar (both the one below the name and the one after it)
     *
     * @param player Player to update health bar to
     */
    public void updatePlayer(@NotNull final Player player) {
        // First off, update health below
        updateHealthBelow(player);

        // Check NPC of Citizens (and maybe other plugins)
        if (player.hasMetadata("NPC"))
            return;

        // If the plugin uses health bar after, and the delay is 0, set it
        if (plugin.getSettings().afterBarEnabled && plugin.getSettings().barAfterHideDelay == 0)
            plugin.getPlayerBarManager().setHealthSuffix(player);
    }

    /**
     * Show or hide health bars depending on whether the player is in a disabled world or has permission to see
     * health bars
     *
     * @param player Player whose permissions to check
     */
    public void updateScoreboard(@NotNull final Player player) {
        if (!player.isOnline()) // The method is (sometimes) called delayed: the player may be logged out
            return;

        // Permission check
        if (plugin.getSettings().usePlayerPermissions) {
            if (!player.hasPermission(Permissions.SEE_BAR)) {
                player.setScoreboard(FAKE_SCOREBOARD);
                return;
            }
        }

        // World check
        if (plugin.getSettings().playerDisabledWorlds.contains(player.getWorld().getName().toLowerCase())) {
            player.setScoreboard(FAKE_SCOREBOARD);
            return;
        }

        player.setScoreboard(mainScoreboard);
    }

    /**
     * Fix the name in the tab (so that the health bar is not shown)
     *
     * @param player Player whose name is to be fixed
     */
    public void fixTabName(@NotNull final Player player) {
        if (!plugin.getSettings().fixTabNames)
            return;
        if (player.getPlayerListName().startsWith("§"))
            return; // Is already colored!

        player.setPlayerListName("§f" + player.getName());
    }

    /**
     * Hide the bar after the name of the specified player
     *
     * @param player Player whose bar must be hidden
     */
    public void hideAfterHealthBar(@NotNull final Player player) {
        Team team = mainScoreboard.getTeam("hbr0");
        if (team == null) {
            team = mainScoreboard.registerNewTeam("hbr0"); // Empty team (without prefix & suffix)
            team.setCanSeeFriendlyInvisibles(false);
        }
        team.addEntry(player.getName());

        // Api - call the custom event after hiding the bar
        plugin.getServer().getPluginManager().callEvent(new BarHideEvent(player));
    }

    /**
     * Update the number of hearts in the health bar below the player's name
     *
     * @param player Player to refresh the bar to
     */
    public void updateHealthBelow(final Player player) {
        final Settings settings = plugin.getSettings();
        if (!settings.playerBarEnabled || !settings.belowBarEnabled)
            return;

        final int score;

        // Higher priority
        if (settings.belowBarUseHearts)
            score = getRawAmountOfHearts(player);
        else if (settings.belowBarUseProportion)
            score = Utils.roundUpPositive(player.getHealth() * (double) settings.belowBarProportion / player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        else
            score = Utils.roundUpPositive(player.getHealth());

        belowObj.getScore(player.getName()).setScore(score);
    }

    /**
     * Set the health bar after the player's name
     *
     * @param player Player to set health bar to
     */
    public void setHealthSuffix(@NotNull final Player player) {
        final double health = player.getHealth();
        final double max = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        if (plugin.getSettings().useCustomBarAfter || !plugin.getSettings().barAfterUseTextMode) {
            final int healthOn10 = Utils.roundUpPositiveWithMax(((health * 10.0) / max), 10);
            mainScoreboard.getTeam("hbr" + healthOn10).addEntry(player.getName());
        } else {
            final int intHealth = Utils.roundUpPositive(health);
            final int intMax = Utils.roundUpPositive(max);

            final String color = getColor(health, max);
            Team team = mainScoreboard.getTeam("hbr" + intHealth + "-" + intMax);
            if (team == null) {
                team = mainScoreboard.registerNewTeam("hbr" + intHealth + "-" + intMax);
                team.setSuffix(" - " + color + intHealth + "§7/§a" + intMax);
                team.setCanSeeFriendlyInvisibles(false);
            }
            team.addEntry(player.getName());
        }
    }

    /**
     * Reload configuration and update bars
     */
    public void reload() {
        clearHealthBars();

        setupHealthBars();
    }

    /**
     * Delete all {@link Team} and {@link Objective} in the {@link Scoreboard} used to show the health bar
     */
    public void clearHealthBars() {
        // Remove all teams used for the bar after the player's name
        removeAllHealthBarTeams(mainScoreboard);

        // Remove Objective used for the bar below the player's name
        final Objective barBelow = plugin.getServer().getScoreboardManager().getMainScoreboard().getObjective("healthbarbelow");
        if (barBelow != null) {
            barBelow.unregister();
            belowObj = null;
        }
    }

    /**
     * @param health Entity health
     * @param max    Maximum entity health
     *
     * @return The color of the health bar based on the percentage of health remaining
     */
    private String getColor(final double health, final double max) {
        final double ratio = health / max;
        if (ratio > 0.5)
            return "§a"; // More than half health -> green
        if (ratio > 0.25)
            return "§e"; // More than quarter health -> yellow
        return "§c"; // Critical health -> red
    }

    /**
     * Set up the {@link Scoreboard} so that it contains the health bar below and after the player's name
     */
    private void setupHealthBars() {
        clearHealthBars(); // Clean up Teams and Objectives stored persistently (between restarts)

        // Setup below bar
        setupBelowBar();

        // Setup after bar
        if (plugin.getSettings().playerAfterBarType == BarType.BAR)
            createCustomPlayerBar(mainScoreboard, Utils.loadYamlFile("custom-player-bar.yml", plugin));
        else if (plugin.getSettings().playerAfterBarType == BarType.CUSTOM_TEXT)
            createDefaultPlayerBar(mainScoreboard, plugin.getSettings().barAfterStyle);
        // else creates the teams at the moment

        setAllTeamsInvisibility(mainScoreboard);
    }

    /**
     * Method responsible for creating the {@link Objective} needed for the bar below the players' names
     */
    private void setupBelowBar() {
        // Create the objective
        if (plugin.getSettings().playerBarEnabled && plugin.getSettings().belowBarEnabled) {
            belowObj = mainScoreboard.registerNewObjective("healthbarbelow", "dummy", plugin.getSettings().belowBarText);
            belowObj.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }
    }

    /**
     * @param player Player to get the hearts of
     *
     * @return The number of player's hearts
     */
    private static int getRawAmountOfHearts(@NotNull final Player player) {
        if (player.isHealthScaled())
            return (int) Math.round(player.getHealth() * 10.0 / player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        else
            return (int) Math.round(player.getHealth() / 2);
    }

    /**
     * Remove all {@link Team} created by HealthBar
     *
     * @param sb Scoreboard to apply cleaning to
     */
    public static void removeAllHealthBarTeams(@NotNull final Scoreboard sb) {
        for (Team team : sb.getTeams())
            if (team.getName().contains("hbr")) // Teams used by healthbar: they contains hbr
                team.unregister();
    }

    /**
     * By default, players on the same team can see each other while invisible. This method hides teams to anyone.
     *
     * @param sb Target scoreboard
     */
    private static void setAllTeamsInvisibility(@NotNull final Scoreboard sb) {
        for (Team team : sb.getTeams())
            if (team.getName().contains("hbr")) // Teams used by healthbar: they contains hbr
                team.setCanSeeFriendlyInvisibles(false);
    }

    /**
     * Create the custom bar by reading it from the configuration
     *
     * @param sb     Scoreboard in which to place the bar
     * @param config Configuration from which to read custom bar settings
     */
    private static void createCustomPlayerBar(@NotNull final Scoreboard sb, @NotNull final FileConfiguration config) {
        for (int i = 1; i < 11; i++) {
            try {
                final Team t = sb.registerNewTeam("hbr" + i);
                if (!config.isSet(i + "0" + "-percent.prefix"))
                    config.set(i + "0" + "-percent.prefix", "");
                if (!config.isSet(i + "0" + "-percent.suffix"))
                    config.set(i + "0" + "-percent.suffix", "");
                final String prefix = config.getString(i + "0" + "-percent.prefix");
                final String suffix = config.getString(i + "0" + "-percent.suffix");

                if (prefix != null && !prefix.equals(""))
                    t.setPrefix(Utils.replaceSymbols(prefix));
                if (suffix != null && !suffix.equals(""))
                    t.setSuffix(Utils.replaceSymbols(suffix));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create the bar based on the selected style
     *
     * @param sb    Scoreboard in which to place the bar
     * @param style Bar style
     */
    private static void createDefaultPlayerBar(@NotNull final Scoreboard sb, final int style) {
        switch (style) {
            case 2:
                sb.registerNewTeam("hbr1").setSuffix(" §c▌");
                sb.registerNewTeam("hbr2").setSuffix(" §c█");
                sb.registerNewTeam("hbr3").setSuffix(" §e█▌");
                sb.registerNewTeam("hbr4").setSuffix(" §e██");
                sb.registerNewTeam("hbr5").setSuffix(" §e██▌");
                sb.registerNewTeam("hbr6").setSuffix(" §a███");
                sb.registerNewTeam("hbr7").setSuffix(" §a███▌");
                sb.registerNewTeam("hbr8").setSuffix(" §a████");
                sb.registerNewTeam("hbr9").setSuffix(" §a████▌");
                sb.registerNewTeam("hbr10").setSuffix(" §a█████");
                break;
            case 3:
                sb.registerNewTeam("hbr1").setSuffix(" §cI§8IIIIIIIII");
                sb.registerNewTeam("hbr2").setSuffix(" §cII§8IIIIIIII");
                sb.registerNewTeam("hbr3").setSuffix(" §eIII§8IIIIIII");
                sb.registerNewTeam("hbr4").setSuffix(" §eIIII§8IIIIII");
                sb.registerNewTeam("hbr5").setSuffix(" §eIIIII§8IIIII");
                sb.registerNewTeam("hbr6").setSuffix(" §aIIIIII§8IIII");
                sb.registerNewTeam("hbr7").setSuffix(" §aIIIIIII§8III");
                sb.registerNewTeam("hbr8").setSuffix(" §aIIIIIIII§8II");
                sb.registerNewTeam("hbr9").setSuffix(" §aIIIIIIIII§8I");
                sb.registerNewTeam("hbr10").setSuffix(" §aIIIIIIIIII");
                break;
            case 4:
                sb.registerNewTeam("hbr1").setSuffix(" §c1❤");
                sb.registerNewTeam("hbr2").setSuffix(" §c2❤");
                sb.registerNewTeam("hbr3").setSuffix(" §e3❤");
                sb.registerNewTeam("hbr4").setSuffix(" §e4❤");
                sb.registerNewTeam("hbr5").setSuffix(" §e5❤");
                sb.registerNewTeam("hbr6").setSuffix(" §a6❤");
                sb.registerNewTeam("hbr7").setSuffix(" §a7❤");
                sb.registerNewTeam("hbr8").setSuffix(" §a8❤");
                sb.registerNewTeam("hbr9").setSuffix(" §a9❤");
                sb.registerNewTeam("hbr10").setSuffix(" §a10❤");
                break;
            case 5:
                sb.registerNewTeam("hbr1").setSuffix(" §c♦§7♦♦♦♦ ");
                sb.registerNewTeam("hbr2").setSuffix(" §c♦§7♦♦♦♦ ");
                sb.registerNewTeam("hbr3").setSuffix(" §e♦♦§7♦♦♦ ");
                sb.registerNewTeam("hbr4").setSuffix(" §e♦♦§7♦♦♦ ");
                sb.registerNewTeam("hbr5").setSuffix(" §a♦♦♦§7♦♦ ");
                sb.registerNewTeam("hbr6").setSuffix(" §a♦♦♦§7♦♦ ");
                sb.registerNewTeam("hbr7").setSuffix(" §a♦♦♦♦§7♦ ");
                sb.registerNewTeam("hbr8").setSuffix(" §a♦♦♦♦§7♦ ");
                sb.registerNewTeam("hbr9").setSuffix(" §a♦♦♦♦♦ ");
                sb.registerNewTeam("hbr10").setSuffix(" §a♦♦♦♦♦ ");
                break;
            case 6:
                sb.registerNewTeam("hbr1").setSuffix(" §c❤§7❤❤❤❤");
                sb.registerNewTeam("hbr2").setSuffix(" §c❤§7❤❤❤❤");
                sb.registerNewTeam("hbr3").setSuffix(" §c❤❤§7❤❤❤");
                sb.registerNewTeam("hbr4").setSuffix(" §c❤❤§7❤❤❤");
                sb.registerNewTeam("hbr5").setSuffix(" §c❤❤❤§7❤❤");
                sb.registerNewTeam("hbr6").setSuffix(" §c❤❤❤§7❤❤");
                sb.registerNewTeam("hbr7").setSuffix(" §c❤❤❤❤§7❤");
                sb.registerNewTeam("hbr8").setSuffix(" §c❤❤❤❤§7❤");
                sb.registerNewTeam("hbr9").setSuffix(" §c❤❤❤❤❤");
                sb.registerNewTeam("hbr10").setSuffix(" §c❤❤❤❤❤");
                break;
            case 7:
                sb.registerNewTeam("hbr1").setSuffix(" §c▌§8▌▌▌▌▌▌▌▌▌");
                sb.registerNewTeam("hbr2").setSuffix(" §c▌▌§8▌▌▌▌▌▌▌▌");
                sb.registerNewTeam("hbr3").setSuffix(" §e▌▌▌§8▌▌▌▌▌▌▌");
                sb.registerNewTeam("hbr4").setSuffix(" §e▌▌▌▌§8▌▌▌▌▌▌");
                sb.registerNewTeam("hbr5").setSuffix(" §e▌▌▌▌▌§8▌▌▌▌▌");
                sb.registerNewTeam("hbr6").setSuffix(" §a▌▌▌▌▌▌§8▌▌▌▌");
                sb.registerNewTeam("hbr7").setSuffix(" §a▌▌▌▌▌▌▌§8▌▌▌");
                sb.registerNewTeam("hbr8").setSuffix(" §a▌▌▌▌▌▌▌▌§8▌▌");
                sb.registerNewTeam("hbr9").setSuffix(" §a▌▌▌▌▌▌▌▌▌§8▌");
                sb.registerNewTeam("hbr10").setSuffix(" §a▌▌▌▌▌▌▌▌▌▌");
                break;
            default: // Style == 1 or > 7
                sb.registerNewTeam("hbr1").setSuffix(" §c|§8|||||||||");
                sb.registerNewTeam("hbr2").setSuffix(" §c||§8||||||||");
                sb.registerNewTeam("hbr3").setSuffix(" §e|||§8|||||||");
                sb.registerNewTeam("hbr4").setSuffix(" §e||||§8||||||");
                sb.registerNewTeam("hbr5").setSuffix(" §e|||||§8|||||");
                sb.registerNewTeam("hbr6").setSuffix(" §a||||||§8||||");
                sb.registerNewTeam("hbr7").setSuffix(" §a|||||||§8|||");
                sb.registerNewTeam("hbr8").setSuffix(" §a||||||||§8||");
                sb.registerNewTeam("hbr9").setSuffix(" §a|||||||||§8|");
                sb.registerNewTeam("hbr10").setSuffix(" §a||||||||||");
                break;
        }
    }
}
