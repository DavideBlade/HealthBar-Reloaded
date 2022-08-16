package com.gmail.davideblade99.healthbar.listener;

import com.gmail.davideblade99.healthbar.HealthBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;

public final class EntitySpawnListener extends HealthBarListener {

    public EntitySpawnListener(@NotNull final HealthBar plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(final CreatureSpawnEvent event) {
        if (plugin.getSettings().mobBarHideDelay != 0)
            return; // If the bar does not always have to be shown

        plugin.getEntityTrackerManager().registerMobHit(event.getEntity(), true);
    }
}
