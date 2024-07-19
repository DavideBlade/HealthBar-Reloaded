package com.gmail.davideblade99.healthbar;

import org.bukkit.entity.Entity;

/**
 * Enumerates possible policies to apply the bar to mobs with already a custom name ({@link Entity#getCustomName()}
 * {@code != null}), set by third parties
 *
 * @since 2.0.3.9
 */
public enum NamedMobPolicy {

    /**
     * Applies the bar by entirely overwriting the previously set name
     *
     * @since 2.0.3.9
     */
    OVERRIDE,

    /**
     * Adds bar to right of existing name
     *
     * @since 2.0.3.9
     */
    APPEND,

    /**
     * Ignore the mob and, therefore, do not set the bar
     *
     * @since 2.0.3.9
     */
    IGNORE
}