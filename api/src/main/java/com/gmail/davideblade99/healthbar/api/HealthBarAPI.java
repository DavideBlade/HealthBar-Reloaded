package com.gmail.davideblade99.healthbar.api;

import com.gmail.davideblade99.healthbar.api.internal.BackendAPI;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HealthBarAPI {

    private HealthBarAPI() {
        throw new IllegalAccessError();
    }

    /**
     * Checks if an entity has its health bar shown
     *
     * @param entity Entity to check
     *
     * @return True if the entity has a health bar, otherwise false
     */
    public static boolean hasBar(@NotNull final LivingEntity entity) {
        return BackendAPI.getImplementation().hasBar(entity);
    }

    /**
     * Hides the health bar restoring the custom name
     *
     * @param mob Mob whose health bar must be hidden
     */
    public static void mobHideBar(@NotNull final LivingEntity mob) {
        BackendAPI.getImplementation().mobHideBar(mob);
    }

    /**
     * Gets the real name of the mob, even if it doesn't have the health bar
     *
     * @param mob Mob whose name is being searched
     *
     * @return The name of the mob or {@code null} if it has no name
     */
    @Nullable
    public static String getMobName(@NotNull final LivingEntity mob) {
        return BackendAPI.getImplementation().getMobName(mob);
    }
}
