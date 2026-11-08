package com.gmail.davideblade99.healthbar.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The capitalization methods of {@link Utils}, on mob type names written as the plugin passes them
 *
 * @since 2.0.7
 */
final class UtilsTest {

    /**
     * {@link Utils#capitalize} upper-cases only the first character, as for the key of a mob type ("iron_golem")
     *
     * @since 2.0.7
     */
    @Test
    @DisplayName("capitalize() upper-cases only the first character")
    void capitalize() {
        assertEquals("Iron_golem", Utils.capitalize("iron_golem"));
        assertEquals("Cow", Utils.capitalize("cow"));
        assertEquals("X", Utils.capitalize("x"));
        assertEquals("", Utils.capitalize(""));
    }

    /**
     * {@link Utils#capitalizeFully} turns a mob type name with spaces ("IRON GOLEM") into "Iron Golem", keeping the
     * spaces as they are
     *
     * @since 2.0.7
     */
    @Test
    @DisplayName("capitalizeFully() upper-cases the first character of every word and lower-cases the others")
    void capitalizeFully() {
        assertEquals("Iron Golem", Utils.capitalizeFully("IRON GOLEM"));
        assertEquals("Mushroom Cow", Utils.capitalizeFully("MUSHROOM COW"));
        assertEquals("Cow", Utils.capitalizeFully("COW"));
        assertEquals("Elder  Guardian", Utils.capitalizeFully("ELDER  GUARDIAN"));
        assertEquals("", Utils.capitalizeFully(""));
    }
}
