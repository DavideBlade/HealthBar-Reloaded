package com.gmail.davideblade99.healthbar.util;

import org.jetbrains.annotations.NotNull;

/**
 * Class used to store an entity's custom name settings (useful since the health bar overrides them)
 */
public final class CustomNameSetting {

    private final String customName;
    private final boolean shown;

    public CustomNameSetting(@NotNull final String customName, final boolean shown) {
        this.customName = customName;
        this.shown = shown;
    }

    @NotNull
    public String getName() {
        return customName;
    }

    public boolean isShown() {
        return shown;
    }
}
