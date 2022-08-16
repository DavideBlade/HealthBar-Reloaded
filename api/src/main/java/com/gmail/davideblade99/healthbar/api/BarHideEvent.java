package com.gmail.davideblade99.healthbar.api;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when the health bar of a player is hidden
 */
public class BarHideEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final OfflinePlayer player;

    public BarHideEvent(@NotNull final OfflinePlayer player) {
        super(false);

        this.player = player;
    }

    /**
     * @return The player whose health bar has been hidden.
     */
    @NotNull
    public OfflinePlayer getOfflinePlayer() {
        return player;
    }

    @NotNull
    @Override
    public final HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public final String getEventName() {
        return getClass().getSimpleName();
    }
}
