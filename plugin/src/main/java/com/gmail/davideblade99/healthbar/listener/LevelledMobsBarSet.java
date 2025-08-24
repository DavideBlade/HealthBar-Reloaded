package com.gmail.davideblade99.healthbar.listener;

import com.gmail.davideblade99.healthbar.HealthBar;
import io.github.arcaneplugins.levelledmobs.events.MobPreLevelEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

/**
 * Listener used to remove the native LevelledMobs bar on mobs with a level
 *
 * @since 2.0.3.6
 */
public final class LevelledMobsBarSet extends HealthBarListener {

    public LevelledMobsBarSet(@NotNull final HealthBar plugin) {
        super(plugin);
    }

    /**
     * Removes LevelledMobs bar from mobs with a level
     *
     * @param event Event invoked when the level of a mob is changed
     *
     * @since 2.0.3.6
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobLeveling(final MobPreLevelEvent event) {
        event.setShowLMNametag(false); // Cancelling LevelledMobs bar

        if (!plugin.getSettings().isMobBarAlwaysSet())
            return; // If the bar does not always have to be shown

        plugin.getEntityTrackerManager().registerMobHit(event.getEntity(), false, true);
    }
}
