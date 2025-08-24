package com.gmail.davideblade99.healthbar;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;

import static com.gmail.davideblade99.healthbar.BarShowCondition.*;

/**
 * It represents the trigger that causes the health bar to be shown.
 *
 * @since 2.0.5
 */
public sealed interface BarShowCondition permits Always, AlwaysWhenLooking, OnDamage, OnDamageOrLooking, OnDamageAndLooking, BelowPercentage {

    /**
     * Always show the health bar.
     *
     * @since 2.0.5
     */
    record Always() implements BarShowCondition {}

    /**
     * Health bar always shown when the player is looking at the mob.
     *
     * @since 2.0.5
     */
    record AlwaysWhenLooking() implements BarShowCondition {}

    /**
     * Show the health bar when the entity takes damage.
     *
     * @since 2.0.5
     */
    record OnDamage() implements BarShowCondition {}

    /**
     * Show the health bar either when the entity takes damage or when the player is looking at it.
     *
     * @since 2.0.5
     */
    record OnDamageOrLooking() implements BarShowCondition {}

    /**
     * Show the health bar when both the entity takes damage and the player is looking at it.
     *
     * @since 2.0.5
     */
    record OnDamageAndLooking() implements BarShowCondition {}

    /**
     * Show the health bar when the entity's {@link Damageable#getHealth() health} drops below a specified percentage
     * compared to the {@link Attribute#GENERIC_MAX_HEALTH maximum health}.
     *
     * @param percentage The health percentage threshold (1–99).
     *
     * @since 2.0.5
     */
    record BelowPercentage(int percentage) implements BarShowCondition {

        /**
         * Creates a new {@link BelowPercentage} condition with a specific threshold.
         *
         * @throws IllegalArgumentException if percentage is not between 1 and 99.
         * @since 2.0.5
         */
        public BelowPercentage {
            if (percentage < 1 || percentage > 99)
                throw new IllegalArgumentException("Percentage must be between 1 and 99.");
        }
    }
}
