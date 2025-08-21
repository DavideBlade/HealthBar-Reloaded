package com.gmail.davideblade99.healthbar.listener;

import com.gmail.davideblade99.healthbar.HealthBar;
import com.gmail.davideblade99.healthbar.api.HealthBarAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;

public final class PlayerRenameEntityListener extends HealthBarListener {

    public PlayerRenameEntityListener(@NotNull final HealthBar plugin) {
        super(plugin);
    }

    /**
     * Hide the health bar to show the name given with the name tag
     *
     * @param event Event triggered when a player interacts with an entity
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRenameEntity(final PlayerInteractEntityEvent event) {
        final Entity target = event.getRightClicked();
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.NAME_TAG)
            return;
        if (!(target instanceof final LivingEntity entity))
            return;

        // If the bar must always be shown, remove the name after 1 second
        if (plugin.getSettings().mobBarHideDelay == 0 && HealthBarAPI.hasBar(entity))
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.getEntityTrackerManager().registerMobHit(entity, true), 20L);

        plugin.getEntityTrackerManager().hideMobBar(entity);
        entity.setCustomNameVisible(false);
    }
}