package com.gmail.davideblade99.healthbar.listener;

import com.gmail.davideblade99.healthbar.HealthBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.jetbrains.annotations.NotNull;

public final class HealthListener extends HealthBarListener {

    public HealthListener(@NotNull final HealthBar plugin) {
        super(plugin);
    }

    /**
     * Hides the health bar immediately to allow compatibility with other plugins that (incorrectly) use custom
     * entity names (e.g., to recognize certain bosses and give rewards)
     *
     * @param event Event triggered when a player dies
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDeath(final EntityDeathEvent event) {
        if (event.getEntity() instanceof Player)
            return;

        plugin.getEntityTrackerManager().hideMobBar(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageEvent(final EntityDamageEvent event) {
        final Entity entity = event.getEntity();
        if (!(entity instanceof final LivingEntity living))
            return;

        if (living.getNoDamageTicks() > living.getMaximumNoDamageTicks() / 2F)
            return;

        if (entity instanceof Player) {
            // Need to schedule delayed since then the event is terminated and the health is updated
            if (plugin.getSettings().playerBarEnabled)
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.getEntityTrackerManager().registerPlayerHit((Player) entity, event instanceof EntityDamageByEntityEvent));

            return;
        }

        // Need to schedule delayed since then the event is terminated and the health is updated
        if (plugin.getSettings().mobBarEnabled)
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.getEntityTrackerManager().registerMobHit(living, event instanceof EntityDamageByEntityEvent));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityRegain(final EntityRegainHealthEvent event) {
        final Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity))
            return;

        if (entity instanceof Player) {
            // Need to schedule delayed since then the event is terminated and the health is updated
            if (plugin.getSettings().playerBarEnabled)
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.getEntityTrackerManager().registerPlayerHit((Player) entity, event.getRegainReason() != RegainReason.SATIATED && event.getAmount() > 0.0));
            return;
        }

        // Need to schedule delayed since then the event is terminated and the health is updated
        if (plugin.getSettings().mobBarEnabled)
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.getEntityTrackerManager().registerMobHit((LivingEntity) entity, true));
    }
}
