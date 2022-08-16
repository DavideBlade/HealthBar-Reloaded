package com.gmail.davideblade99.healthbar.listener;

import com.gmail.davideblade99.healthbar.HealthBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public final class PlayerTeleportListener extends HealthBarListener {

    public PlayerTeleportListener(@NotNull final HealthBar plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerTeleport(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();

        /*
         * Need to schedule delayed since then the event is terminated and the world is updated
         * (it is unchanged if the event is cancelled and is updated if the event is not cancelled)
         */
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (plugin.getSettings().overrideOtherScoreboards)
                plugin.getPlayerBarManager().updateScoreboard(player);

            plugin.getPlayerBarManager().updatePlayer(player);
        }, 1L);
    }
}
