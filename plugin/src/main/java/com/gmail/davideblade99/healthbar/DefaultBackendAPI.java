package com.gmail.davideblade99.healthbar;

import com.gmail.davideblade99.healthbar.api.internal.BackendAPI;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Default API implementation
 */
public class DefaultBackendAPI extends BackendAPI {

    private final HealthBar plugin;

    public DefaultBackendAPI(@NotNull final HealthBar plugin) {
        this.plugin = plugin;
    }

    /**
     * @see BackendAPI#hasBar(LivingEntity)
     */
    @Override
    public boolean hasBar(@NotNull final LivingEntity entity) {
        return plugin.getEntityTrackerManager().hasBar(entity);
    }

    /**
     * @see BackendAPI#mobHideBar(LivingEntity)
     */
    @Override
    public void mobHideBar(@NotNull final LivingEntity mob) {
        plugin.getEntityTrackerManager().hideMobBar(mob);
    }

    /**
     * @see BackendAPI#getMobName(LivingEntity)
     */
    @Nullable
    @Override
    public String getMobName(@NotNull final LivingEntity mob) {
        return plugin.getEntityTrackerManager().getNameWhileHavingBar(mob);
    }
}
