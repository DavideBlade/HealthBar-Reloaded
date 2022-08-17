package com.gmail.davideblade99.healthbar.listener;

import com.gmail.davideblade99.healthbar.HealthBar;
import com.gmail.davideblade99.healthbar.api.HealthBarAPI;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class InventoryListener extends HealthBarListener {

    public InventoryListener(@NotNull final HealthBar plugin) {
        super(plugin);
    }

    /**
     * Restore the name of an entity when a player tries to open his inventory
     *
     * @param event Event triggered when a player opens an inventory
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityInventoryOpen(final InventoryOpenEvent event) {
        final InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof LivingEntity))
            return;

        final LivingEntity entity = (LivingEntity) holder;
        if (HealthBarAPI.hasBar(entity))
            plugin.getEntityTrackerManager().hideMobBar(entity);
    }

    /**
     * Restore the health bar when the player close the inventory
     *
     * @param event Event triggered when a player closes an inventory
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityInventoryClose(final InventoryCloseEvent event) {
        final InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof LivingEntity))
            return;

        if (plugin.getSettings().mobBarHideDelay == 0)
            plugin.getEntityTrackerManager().registerMobHit((LivingEntity) holder, true);
    }
}
