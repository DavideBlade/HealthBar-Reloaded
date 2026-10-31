package com.gmail.davideblade99.healthbar;

import com.gmail.davideblade99.healthbar.api.HealthBarAPI;
import org.bukkit.entity.Cow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mob bars as a player sees them, with the settings of {@value TestSupport#CONFIG}
 *
 * @since 2.0.7
 */
final class MobBarTest {

    /**
     * {@code mob-bars.hide-delay-seconds} of {@value TestSupport#CONFIG}, in ticks
     *
     * @since 2.0.7
     */
    private static final int HIDE_DELAY_TICKS = 3 * 20;

    private ServerMock server;
    private WorldMock world;
    private PlayerMock player;

    /**
     * Starts a server with HealthBar, a world where mob bars are enabled and a player
     *
     * @since 2.0.7
     */
    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        TestSupport.loadWithTestConfig(server);
        world = server.addSimpleWorld("world");
        player = server.addPlayer();
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
     * Simulates a melee hit like the server does: the damage event, the health update, then the next tick (where
     * HealthBar reads the new health)
     *
     * @param mob Mob hit by the player
     *
     * @since 2.0.7
     */
    private void hit(@NotNull final LivingEntity mob) {
        server.getPluginManager().callEvent(new EntityDamageByEntityEvent(player, mob, DamageCause.ENTITY_ATTACK, 2));
        mob.damage(2, player);
        server.getScheduler().performOneTick();
    }

    /**
     * With {@code show-condition: on-damage}, a cow has no bar until a player hits it, then a visible one
     *
     * @since 2.0.7
     */
    @Test
    @DisplayName("A mob hit by a player shows a visible bar")
    void barAppearsOnHit() {
        final Cow cow = world.spawn(world.getSpawnLocation(), Cow.class);
        assertFalse(HealthBarAPI.hasBar(cow), "The cow has a bar before being hit");

        hit(cow);

        assertTrue(HealthBarAPI.hasBar(cow), "The cow has no bar after being hit");
        assertNotNull(cow.getCustomName(), "The bar is not in the custom name");
        assertTrue(cow.isCustomNameVisible(), "The bar is not visible");
    }

    /**
     * The bar disappears {@code hide-delay-seconds} after the hit
     *
     * @since 2.0.7
     */
    @Test
    @DisplayName("The bar disappears after the hide delay")
    void barDisappearsAfterDelay() {
        final Cow cow = world.spawn(world.getSpawnLocation(), Cow.class);
        hit(cow);

        server.getScheduler().performTicks(HIDE_DELAY_TICKS + 20);

        assertFalse(HealthBarAPI.hasBar(cow), "The bar is still there after the hide delay");
        assertFalse(cow.isCustomNameVisible(), "The name is still visible after the hide delay");
    }

    /**
     * With {@code show-on-named-mobs: override}, the bar takes the place of a mob's name while shown, and the name comes
     * back when the bar disappears
     *
     * @since 2.0.7
     */
    @Test
    @DisplayName("A named mob gets its name back when the bar disappears")
    void customNameComesBack() {
        final Cow cow = world.spawn(world.getSpawnLocation(), Cow.class);
        cow.setCustomName("Bessie");
        cow.setCustomNameVisible(true);

        hit(cow);
        assertEquals("Bessie", HealthBarAPI.getMobName(cow), "The original name is lost while the bar is shown");

        server.getScheduler().performTicks(HIDE_DELAY_TICKS + 20);
        assertEquals("Bessie", cow.getCustomName(), "The original name did not come back");
        assertTrue(cow.isCustomNameVisible(), "The original name is no longer visible");
    }

    /**
     * No bar on zombies, which are in {@code disabled-types}
     *
     * @since 2.0.7
     */
    @Test
    @DisplayName("No bar on the mob types disabled in the configuration")
    void noBarOnDisabledTypes() {
        final Zombie zombie = world.spawn(world.getSpawnLocation(), Zombie.class);

        hit(zombie);

        assertFalse(HealthBarAPI.hasBar(zombie), "Zombies are in disabled-types but got a bar");
    }

    /**
     * No bar in world_nether, which is in {@code disabled-worlds}
     *
     * @since 2.0.7
     */
    @Test
    @DisplayName("No bar in the worlds disabled in the configuration")
    void noBarInDisabledWorlds() {
        final WorldMock nether = server.addSimpleWorld("world_nether");
        final Cow cow = nether.spawn(nether.getSpawnLocation(), Cow.class);

        hit(cow);

        assertFalse(HealthBarAPI.hasBar(cow), "world_nether is in disabled-worlds but the cow got a bar");
    }
}
