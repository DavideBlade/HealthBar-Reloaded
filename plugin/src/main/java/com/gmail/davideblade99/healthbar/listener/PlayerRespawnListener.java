package com.gmail.davideblade99.healthbar.listener;

import com.gmail.davideblade99.healthbar.HealthBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

public final class PlayerRespawnListener extends HealthBarListener {

    public PlayerRespawnListener(@NotNull final HealthBar plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerRespawn(final PlayerRespawnEvent event) {
        final Player player = event.getPlayer();

        // Need to schedule delayed since then the event is terminated and the location of the player is updated
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            plugin.getPlayerBarManager().updateScoreboard(player);
            plugin.getPlayerBarManager().updatePlayer(player);
        }, 1L);
    }
}
