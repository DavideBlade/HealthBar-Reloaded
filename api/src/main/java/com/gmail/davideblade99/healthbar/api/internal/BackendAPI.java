package com.gmail.davideblade99.healthbar.api.internal;

import com.gmail.davideblade99.healthbar.api.HealthBarAPI;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BackendAPI {
    private static BackendAPI implementation;

    public static void setImplementation(@NotNull final BackendAPI implementation) {
        if (BackendAPI.implementation != null)
            throw new IllegalStateException("Implementation already set");

        BackendAPI.implementation = implementation;
    }

    @NotNull
    public static BackendAPI getImplementation() {
        if (BackendAPI.implementation == null)
            throw new IllegalStateException("No implementation set");

        return implementation;
    }

    /**
     * @see HealthBarAPI#hasBar(LivingEntity)
     */
    public abstract boolean hasBar(@NotNull final LivingEntity entity);

    /**
     * @see HealthBarAPI#mobHideBar(LivingEntity)
     */
    public abstract void mobHideBar(@NotNull final LivingEntity mob);

    /**
     * @see HealthBarAPI#getMobName(LivingEntity)
     */
    @Nullable
    public abstract String getMobName(@NotNull final LivingEntity mob);
}
