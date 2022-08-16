package com.gmail.davideblade99.healthbar.listener;

import com.gmail.davideblade99.healthbar.HealthBar;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public abstract class HealthBarListener implements Listener {

    final HealthBar plugin;

    protected HealthBarListener(@NotNull final HealthBar plugin) {
        super();

        this.plugin = plugin;
    }
}
