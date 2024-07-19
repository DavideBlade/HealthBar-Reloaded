package com.gmail.davideblade99.healthbar.util;

import com.gmail.davideblade99.healthbar.NamedMobPolicy;
import com.gmail.davideblade99.healthbar.Settings;
import com.gmail.davideblade99.healthbar.manager.EntityTrackerManager;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to store the appended bar (in case {@link Settings#barOnNamedMobPolicy} {@code ==}
 * {@link NamedMobPolicy#APPEND}) to the custom name of an entity so that it can be removed when needed and if the custom
 * name was originally visible (useful because HealthBar changes it)
 *
 * @see EntityTrackerManager#appendTable
 * @since 2.0.3.9
 */
public final class AppendedBar {

    private final String appendedBar;
    private final boolean shown;

    /**
     * Create a new instance to store the appended bar and the original custom name visibility (before it is modified by
     * HealthBar)
     *
     * @param appendedBar Bar added to the right of the entity name
     * @param shown       Whether the entity name is originally visible
     *
     * @apiNote Usage example: {@code new AppendedBar(barToAppend, entity.isCustomNameVisible())}
     * @since 2.0.3.9
     */
    public AppendedBar(@NotNull final String appendedBar, final boolean shown) {
        this.appendedBar = appendedBar;
        this.shown = shown;
    }

    /**
     * @return The bar appended to the entity's custom name
     * @since 2.0.3.9
     */
    @NotNull
    public String getBar() {
        return appendedBar;
    }

    /**
     * @return If the entity name was originally visible
     * @since 2.0.3.9
     */
    public boolean isShown() {
        return shown;
    }
}