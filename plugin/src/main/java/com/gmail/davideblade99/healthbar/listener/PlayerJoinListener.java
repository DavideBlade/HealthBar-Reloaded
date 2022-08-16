package com.gmail.davideblade99.healthbar.listener;

import com.gmail.davideblade99.healthbar.HealthBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public final class PlayerJoinListener extends HealthBarListener {

    public PlayerJoinListener(@NotNull final HealthBar plugin) {
        super(plugin);
    }

    /**
     * Remove immediately the health bar from the tab in order to not cause compatibility problems with other
     * plugins that change the tab
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void joinLowest(final PlayerJoinEvent event) {
        plugin.getPlayerBarManager().fixTabName(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void joinHighest(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        // Eventually update the scoreboard
        plugin.getPlayerBarManager().updateScoreboard(player);

        // Update the health bars
        plugin.getPlayerBarManager().updatePlayer(player);
    }
}
