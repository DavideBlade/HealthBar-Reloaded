package com.gmail.davideblade99.healthbar.util;

import com.gmail.davideblade99.healthbar.manager.EntityTrackerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class used to store an entity's custom name settings (useful since HealthBar changes them)
 *
 * @see EntityTrackerManager#namesTable
 */
public final class CustomNameSetting {

    private final String customName;
    private final boolean shown;

    /**
     * Creates a new instance to store original entity name settings (before they are changed by HealthBar)
     *
     * @param customName Original custom name of the entity
     * @param shown      Whether the entity name is originally visible
     *
     * @apiNote Usage example: {@code new CustomNameSetting(entity.getCustomName(), entity.isCustomNameVisible())}
     */
    public CustomNameSetting(@Nullable final String customName, final boolean shown) {
        this.customName = customName;
        this.shown = shown;
    }

    @Nullable
    public String getName() {
        return customName;
    }

    public boolean isShown() {
        return shown;
    }
}
