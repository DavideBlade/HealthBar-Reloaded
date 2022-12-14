package com.gmail.davideblade99.healthbar.api;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when the health bar after the name of a player is hidden
 */
public class BarHideEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final OfflinePlayer player;

    /**
     * Creates a new instance of the event
     *
     * @param player Player whose health bar has been hidden
     */
    public BarHideEvent(@NotNull final OfflinePlayer player) {
        super(false);

        this.player = player;
    }

    /**
     * @return The player whose health bar has been hidden
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

    /**
     * @return List of event handlers
     *
     * @see Event
     * @see <a href="https://bukkit.fandom.com/wiki/Event_API_Reference#Creating_Custom_Events">wiki</a>
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public final String getEventName() {
        return getClass().getSimpleName();
    }
}
