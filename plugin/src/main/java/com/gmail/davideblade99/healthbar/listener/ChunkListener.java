package com.gmail.davideblade99.healthbar.listener;

import com.gmail.davideblade99.healthbar.HealthBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jetbrains.annotations.NotNull;

public final class ChunkListener extends HealthBarListener {

    public ChunkListener(@NotNull final HealthBar plugin) {
        super(plugin);
    }

    /**
     * Show the health bar on the mobs that are in the loaded chunk
     *
     * @param event Event triggered when the server unloads a chunk
     */
    @EventHandler
    public void onChunkLoad(final ChunkLoadEvent event) {
        if (plugin.getSettings().mobBarHideDelay != 0)
            return; // If the bar does not always have to be shown

        for (Entity entity : event.getChunk().getEntities())
            if (entity instanceof LivingEntity && entity.getType() != EntityType.PLAYER)
                plugin.getEntityTrackerManager().registerMobHit((LivingEntity) entity, true);
    }

    /**
     * Remove the health bar from the mobs in the unloaded chunk
     *
     * @param event Event triggered when the server loads a chunk
     */
    @EventHandler
    public void onChunkUnload(final ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities())
            if (entity instanceof LivingEntity && entity.getType() != EntityType.PLAYER)
                plugin.getEntityTrackerManager().hideMobBar((LivingEntity) entity);
    }
}
