package com.gmail.davideblade99.healthbar.util;

import org.bukkit.attribute.Attribute;
import org.jetbrains.annotations.NotNull;

/**
 * {@link Attribute}s whose name changed between server versions, looked up so that the same plugin jar works on every
 * supported version
 *
 * @since 2.0.7
 */
public final class AttributeUtil {

    /**
     * Maximum health attribute. Called {@code GENERIC_MAX_HEALTH} up to 1.21.1 and {@code MAX_HEALTH} from 1.21.3,
     * when {@link Attribute} stopped being an enum: looked up by name once, so it works on every server version
     *
     * @since 2.0.7
     */
    public static final Attribute MAX_HEALTH = find("MAX_HEALTH", "GENERIC_MAX_HEALTH");

    /** Enforce non-instantiability with a private constructor */
    private AttributeUtil() {
        throw new IllegalAccessError();
    }

    /**
     * Looks up an attribute by the name of its constant in {@link Attribute}, trying each name in turn: looked up by
     * name, a constant that does not exist on this server version is skipped instead of breaking the plugin
     *
     * @param names The names the attribute had in the different server versions
     *
     * @return The attribute with the first name that exists on this server
     *
     * @throws IllegalStateException If none of the names exists on this server
     *
     * @since 2.0.7
     */
    @NotNull
    private static Attribute find(@NotNull final String... names) {
        for (String name : names) {
            try {
                return (Attribute) Attribute.class.getField(name).get(null);
            } catch (final NoSuchFieldException ignored) {
                // Not on this server version: try the next name
            } catch (final IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        throw new IllegalStateException("No attribute named " + String.join(" or ", names));
    }
}
