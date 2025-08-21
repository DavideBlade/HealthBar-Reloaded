package com.gmail.davideblade99.healthbar.listener;

import com.gmail.davideblade99.healthbar.HealthBar;
import com.gmail.davideblade99.healthbar.api.HealthBarAPI;
import org.apache.commons.lang.WordUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

public final class PlayerDeathListener extends HealthBarListener {

    public PlayerDeathListener(@NotNull final HealthBar plugin) {
        super(plugin);
    }

    /**
     * Fixes death messages so that the health bar is not shown
     *
     * @param event Event triggered when a player dies
     *
     * @see <a href="https://minecraft.fandom.com/wiki/Death_messages">Wiki with the list of death messages</a>
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeathEvent(final PlayerDeathEvent event) {
        final String deathMessage = event.getDeathMessage();
        if (deathMessage == null)
            return;

        final Player victim = event.getEntity();
        final EntityDamageEvent damageEvent = victim.getLastDamageCause();

        // If the last damage was inflicted by an entity
        if (damageEvent instanceof EntityDamageByEntityEvent) {
            final Entity damager = ((EntityDamageByEntityEvent) damageEvent).getDamager();

            if (containsAny(deathMessage, DeathMessages.KILLED_BY_ENTITY.identifiers)) {
                if (damager instanceof Player) {
                    final ItemMeta itemMeta = ((Player) damager).getInventory().getItemInMainHand().getItemMeta();
                    if (itemMeta == null || !itemMeta.hasDisplayName())
                        event.setDeathMessage(victim.getName() + " was slain by " + damager.getName());
                    else
                        event.setDeathMessage(victim.getName() + " was slain by " + damager.getName() + " using " + itemMeta.getDisplayName());
                    return;
                } else if (damager instanceof LivingEntity) {
                    event.setDeathMessage(victim.getName() + " was slain by " + getName((LivingEntity) damager));
                    return;
                }
            } else if (containsAny(deathMessage, DeathMessages.EXPLOSION_FROM_ENTITY.identifiers)) {
                if (damager instanceof Player) {
                    event.setDeathMessage(victim.getName() + " was blown up by " + damager.getName());
                    return;
                }
                if (damager instanceof LivingEntity) {
                    event.setDeathMessage(victim.getName() + " was blown up by " + getName((LivingEntity) damager));
                    return;
                }
            } else if (containsAny(deathMessage, DeathMessages.SHOTTED.identifiers)) {
                if (damager instanceof Projectile) {
                    final ProjectileSource projectileSource = ((Projectile) damager).getShooter();
                    if (projectileSource instanceof final LivingEntity shooter) {

                        if (shooter instanceof Player) {
                            final ItemMeta itemMeta = ((Player) shooter).getInventory().getItemInMainHand().getItemMeta();
                            if (itemMeta == null || !itemMeta.hasDisplayName())
                                event.setDeathMessage(victim.getName() + " was shot by " + shooter.getName());
                            else
                                event.setDeathMessage(victim.getName() + " was shot by " + shooter.getName() + " using " + itemMeta.getDisplayName());
                        } else
                            event.setDeathMessage(victim.getName() + " was shot by " + getName(shooter));

                        return;
                    }
                }
            } else if (containsAny(deathMessage, DeathMessages.FIREBALLED_BY_ENTITY.identifiers)) {
                if (damager instanceof Projectile) {
                    final ProjectileSource projectileSource = ((Projectile) damager).getShooter();
                    if (projectileSource instanceof final LivingEntity shooter) {

                        if (projectileSource instanceof Player)
                            event.setDeathMessage(victim.getName() + " was fireballed by " + shooter.getName());
                        else
                            event.setDeathMessage(victim.getName() + "was fireballed by " + getName(shooter));

                        return;
                    }
                }
            }
        }

        if (containsAny(deathMessage, DeathMessages.FALLING.identifiers)) {
            event.setDeathMessage(victim.getName() + " fell from a high place");
            return;
        }
        if (containsAny(deathMessage, DeathMessages.LAVA.identifiers)) {
            event.setDeathMessage(victim.getName() + " tried to swim in lava");
            return;
        }
        if (containsAny(deathMessage, DeathMessages.EXPLOSION.identifiers)) {
            event.setDeathMessage(victim.getName() + " blew up");
            return;
        }
        if (containsAny(deathMessage, DeathMessages.BURNED.identifiers)) {
            event.setDeathMessage(victim.getName() + " was burned to death");
            return;
        }
        if (containsAny(deathMessage, DeathMessages.ON_FIRED_BLOCK.identifiers)) {
            event.setDeathMessage(victim.getName() + " went up in flames");
            return;
        }
        if (containsAny(deathMessage, DeathMessages.DROWNED.identifiers)) {
            event.setDeathMessage(victim.getName() + " drowned");
            return;
        }
        if (containsAny(deathMessage, DeathMessages.SHOTTED.identifiers)) {
            event.setDeathMessage(victim.getName() + " was shot by an arrow");
            return;
        }
        if (containsAny(deathMessage, DeathMessages.SUFFOCATED.identifiers)) {
            event.setDeathMessage(victim.getName() + " suffocated in a wall");
            return;
        }
        if (containsAny(deathMessage, DeathMessages.STARVED.identifiers)) {
            event.setDeathMessage(victim.getName() + " starved to death");
            return;
        }
        if (containsAny(deathMessage, DeathMessages.MAGIC.identifiers)) {
            event.setDeathMessage(victim.getName() + " was killed by magic");
            return;
        }
        if (containsAny(deathMessage, DeathMessages.FIREBALL.identifiers)) {
            event.setDeathMessage(victim.getName() + " was fireballed");
            return;
        }
        if (containsAny(deathMessage, DeathMessages.POKED.identifiers)) {
            event.setDeathMessage(victim.getName() + " was pricked to death");
            return;
        }
        if (containsAny(deathMessage, DeathMessages.OUT_OF_WORLD.identifiers)) {
            event.setDeathMessage(victim.getName() + " fell out of the world");
            return;
        }
        if (containsAny(deathMessage, DeathMessages.SQUASHED.identifiers)) {
            event.setDeathMessage(victim.getName() + " was squashed by a falling anvil");
            return;
        }

        event.setDeathMessage(victim.getName() + " died"); // Default message
    }

    /**
     * @param mob Mob whose name has to be searched
     *
     * @return The mob's custom name or its original Minecraft vanilla name
     */
    @NotNull
    private String getName(@NotNull final LivingEntity mob) {
        final String customName = HealthBarAPI.getMobName(mob);
        if (customName != null)
            return customName; // Custom name (without bar)

        return WordUtils.capitalizeFully(mob.getType().toString().replace("_", " ")); // Vanilla name
    }

    /**
     * Checks whether the string contains at least one of the words passed as a parameter
     *
     * @param str   String to scan
     * @param words Words to search in the string
     *
     * @return True if the string contains at least one of the words, false otherwise
     */
    private static boolean containsAny(@NotNull final String str, @NotNull final String[] words) {
        for (String word : words)
            if (str.contains(word))
                return true;
        return false;
    }

    /**
     * Enum representing all possible player death messages supported
     */
    private enum DeathMessages {
        /** Killed by an entity (player or monster) */
        KILLED_BY_ENTITY("killed", "slain", "got finished"),

        /** Killed by an explosion triggered by an entity */
        EXPLOSION_FROM_ENTITY("blown up"),

        /** Killed by an explosion */
        EXPLOSION("blew up"),

        /** Killed by a projectile (e.g. arrow) */
        SHOTTED("shot"),

        /** Killed by a fireball from an entity */
        FIREBALLED_BY_ENTITY("fireballed"),

        /** Killed by a fireball */
        FIREBALL("fireball"),

        /** Dead by falling */
        FALLING("high place", "doomed to fall", "fell off", "fell out of the water"),

        /** Died swimming in lava */
        LAVA("lava"),

        /** Burned to death */
        BURNED("burned", "crisp"),

        /** Killed while standing above a burning block */
        ON_FIRED_BLOCK("flames", "fire"),

        /** Drowned to death */
        DROWNED("drowned"),

        /** Suffocated to death */
        SUFFOCATED("wall"),

        /** Starved to death */
        STARVED("starved"),

        /** Killed by a potion effect */
        MAGIC("magic"),

        /** Killed by prickles */
        POKED("pricked", "cactus"),

        /** Killed by the void or the /kill command */
        OUT_OF_WORLD("world"),

        /** Squashed to death */
        SQUASHED("squashed");


        /** Words that identify the type of death message */
        private final String[] identifiers;

        DeathMessages(@NotNull final String... identifiers) {
            this.identifiers = identifiers;
        }
    }
}
