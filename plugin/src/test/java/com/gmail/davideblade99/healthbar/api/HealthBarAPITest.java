package com.gmail.davideblade99.healthbar.api;

import com.gmail.davideblade99.healthbar.InitializationRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.gmail.davideblade99.healthbar.InitializationRunner.fakeEntity;
import static com.gmail.davideblade99.healthbar.InitializationRunner.healthBar;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(InitializationRunner.class)
final class HealthBarAPITest {

    /**
     * Resets the fake entity when each {@code @Test} is executed so that each one is isolated
     */
    @BeforeEach
    void setUp() {
        healthBar.getEntityTrackerManager().hideMobBar(fakeEntity);
    }

    @Test
    @DisplayName("Checking hasBar() API")
    void hasBar() {
        assertFalse(HealthBarAPI.hasBar(fakeEntity), "The entity does not have a bar");
        healthBar.getEntityTrackerManager().registerMobHit(fakeEntity, true);
        assertTrue(HealthBarAPI.hasBar(fakeEntity), "The entity has a bar");
    }

    @Test
    @DisplayName("Checking mobHideBar() API")
    void mobHideBar() {
        healthBar.getEntityTrackerManager().registerMobHit(fakeEntity, true);
        assertTrue(HealthBarAPI.hasBar(fakeEntity), "The entity should have a bar");
        HealthBarAPI.mobHideBar(fakeEntity);
        assertFalse(HealthBarAPI.hasBar(fakeEntity), "The entity still has the bar");
    }

    @Test
    @DisplayName("Checking getMobName() API")
    void getMobName() {
        assertFalse(HealthBarAPI.hasBar(fakeEntity));
        assertEquals(fakeEntity.getCustomName(), HealthBarAPI.getMobName(fakeEntity), "Mismatch with the original custom entity name (= null)");

        final String customName = "test";
        fakeEntity.setCustomName(customName);
        assertEquals(fakeEntity.getCustomName(), HealthBarAPI.getMobName(fakeEntity), "Mismatch with the custom entity name (= " + customName + ")");

        healthBar.getEntityTrackerManager().registerMobHit(fakeEntity, true);
        assertEquals(customName, HealthBarAPI.getMobName(fakeEntity), "Mismatch with the custom entity name (= " + customName + ")");
    }
}