package com.gmail.davideblade99.healthbar.api;

import com.gmail.davideblade99.healthbar.HealthBar;
import com.gmail.davideblade99.healthbar.TestSupport;
import org.bukkit.entity.Cow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The methods of {@link HealthBarAPI}, on a cow, with the settings of {@value TestSupport#CONFIG}
 *
 * @since 2.0.3
 */
final class HealthBarAPITest {

    private HealthBar healthBar;
    private Cow cow;

    /**
     * Starts a server with HealthBar and spawns a cow, in a world where mob bars are enabled
     *
     * @since 2.0.3
     */
    @BeforeEach
    void setUp() {
        final ServerMock server = MockBukkit.mock();
        healthBar = TestSupport.loadWithTestConfig(server);
        final WorldMock world = server.addSimpleWorld("world");
        cow = world.spawn(world.getSpawnLocation(), Cow.class);
    }

    /**
     * Stops the server and removes HealthBar's API implementation, so that the next test starts from scratch
     *
     * @since 2.0.7
     */
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
        TestSupport.resetBackendAPI();
    }

    /**
     * {@link HealthBarAPI#hasBar} is false before the cow is hit and true after
     *
     * @since 2.0.3
     */
    @Test
    @DisplayName("Checking hasBar() API")
    void hasBar() {
        assertFalse(HealthBarAPI.hasBar(cow), "The entity does not have a bar");
        healthBar.getEntityTrackerManager().registerMobHit(cow, true);
        assertTrue(HealthBarAPI.hasBar(cow), "The entity has a bar");
    }

    /**
     * {@link HealthBarAPI#mobHideBar} removes the bar of a hit cow
     *
     * @since 2.0.3
     */
    @Test
    @DisplayName("Checking mobHideBar() API")
    void mobHideBar() {
        healthBar.getEntityTrackerManager().registerMobHit(cow, true);
        assertTrue(HealthBarAPI.hasBar(cow), "The entity should have a bar");
        HealthBarAPI.mobHideBar(cow);
        assertFalse(HealthBarAPI.hasBar(cow), "The entity still has the bar");
    }

    /**
     * {@link HealthBarAPI#getMobName} returns the cow's custom name (none, then one set), also while the bar is shown
     * in its place
     *
     * @since 2.0.3
     */
    @Test
    @DisplayName("Checking getMobName() API")
    void getMobName() {
        assertFalse(HealthBarAPI.hasBar(cow));
        assertEquals(cow.getCustomName(), HealthBarAPI.getMobName(cow), "Mismatch with the original custom entity name (= null)");

        final String customName = "test";
        cow.setCustomName(customName);
        assertEquals(cow.getCustomName(), HealthBarAPI.getMobName(cow), "Mismatch with the custom entity name (= " + customName + ")");

        healthBar.getEntityTrackerManager().registerMobHit(cow, true);
        assertEquals(customName, HealthBarAPI.getMobName(cow), "Mismatch with the custom entity name (= " + customName + ")");
    }
}
