package com.gmail.davideblade99.healthbar.manager;

import com.gmail.davideblade99.healthbar.HealthBar;
import com.gmail.davideblade99.healthbar.Settings;
import com.gmail.davideblade99.healthbar.util.CustomNameSetting;
import com.gmail.davideblade99.healthbar.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for tracking entities with a health bar
 */
public final class EntityTrackerManager {

    /** Entity whose name will never be changed (and therefore will not have a health bar) */
    private final static List<EntityType> DISABLED_ENTITIES = Arrays.asList(EntityType.WITHER, EntityType.ENDER_DRAGON);

    /** Map the name of the player with the task (ID) that hides its health bar once the delay has passed */
    private final Map<String, Integer> playerTable = new HashMap<>();

    /** Map the entity (ID) with the task (ID) that hides its health bar once the delay has passed */
    private final Map<Integer, Integer> mobTable = new HashMap<>();

    /** Map the entity (ID) with its original custom name (before the health bar is applied) */
    private final Map<Integer, CustomNameSetting> namesTable = new HashMap<>();

    private final HealthBar plugin;

    public EntityTrackerManager(@NotNull final HealthBar plugin) {
        this.plugin = plugin;
    }

    /**
     * Method that deals with performing all necessary actions at the time an entity is hit.
     *
     * Specifically, if the entity is among those that should have a health bar and if it is in a world where the
     * plugin is not disabled, the method shows or updates the health bar and hides the bar after the delay (if
     * set)
     *
     * @param attacked        Entity hit
     * @param damagedByEntity Whether the entity has been hit by another entity
     */
    public void registerMobHit(@NotNull final LivingEntity attacked, final boolean damagedByEntity) {
        // Entity type check
        final Settings settings = plugin.getSettings();
        final EntityType type = attacked.getType();
        if (DISABLED_ENTITIES.contains(type))
            return;
        if (settings.mobDisabledTypes.contains(type))
            return;

        // World check
        if (settings.mobDisabledWorlds.contains(attacked.getWorld().getName().toLowerCase()))
            return;

        // Check NPC of Citizens (and maybe other plugins)
        if (attacked.hasMetadata("NPC"))
            return;

        // Check mobs of MythicMobs
        if (!settings.barOnMythicMobs && plugin.getMythicMobsAPI() != null && plugin.getMythicMobsAPI().isMythicMob(attacked))
            return;

        // Custom name check
        if (isNamed(attacked)) {
            if (!settings.showMobBarOnCustomNames)
                return; // The bar must not be shown on renamed entities

            namesTable.put(attacked.getEntityId(), new CustomNameSetting(attacked.getCustomName(), attacked.isCustomNameVisible()));
        }


        // If the bar should remain visible
        if (settings.mobBarHideDelay == 0) {
            showMobHealthBar(attacked);
            return;
        }


        // Display always if hit by entity
        if (damagedByEntity) {
            final Integer taskID = mobTable.remove(attacked.getEntityId());
            if (taskID != null)
                Bukkit.getScheduler().cancelTask(taskID); // Eventually cancel previous task

            showMobHealthBar(attacked);
            hideMobBarLater(attacked);
        } else {
            // It's not damaged by entity, if the health was displayed only update it
            if (mobTable.containsKey(attacked.getEntityId()))
                showMobHealthBar(attacked);
        }
    }

    public void registerPlayerHit(@NotNull final Player player, final boolean damagedByEntity) {
        final String pname = player.getName();

        // Update the health bar under the name
        plugin.getPlayerBarManager().updateHealthBelow(player);

        // If the bar next to the name is not enabled return
        if (!plugin.getSettings().afterBarEnabled)
            return;

        // Check whether the bar should always be shown
        if (plugin.getSettings().barAfterHideDelay == 0) {
            updatePlayerHealthBar(player);
            return;
        }

        // Display always if hit by entity
        if (damagedByEntity) {
            final Integer taskID = playerTable.remove(pname);
            if (taskID != null)
                Bukkit.getScheduler().cancelTask(taskID); // Eventually cancel previous task

            updatePlayerHealthBar(player);
            hidePlayerBarLater(player);
        } else {
            // Not damaged by entity
            if (playerTable.containsKey(pname))
                updatePlayerHealthBar(player);
        }
    }

    /**
     * Hides the health bar and restores the custom name, if there was one
     *
     * @param mob Mob whose bar is to be hidden
     */
    public void hideMobBar(@NotNull final LivingEntity mob) {
        if (!mob.getPersistentDataContainer().has(plugin.getNamespace(), PersistentDataType.BYTE))
            return; // It's a real name! Don't touch it

        // Cancel eventual task
        final Integer id = mobTable.remove(mob.getEntityId());
        if (id != null)
            Bukkit.getScheduler().cancelTask(id);

        if (plugin.getSettings().showMobBarOnCustomNames) {
            final CustomNameSetting sb = namesTable.remove(mob.getEntityId());
            if (sb != null) {
                // Return only if found, else hide normally
                mob.setCustomName(sb.getName());
                mob.setCustomNameVisible(sb.isShown());
                mob.getPersistentDataContainer().remove(plugin.getNamespace());
                return;
            }
        }

        // Not a custom named mob, use default method (hide the name)
        mob.setCustomName("");
        mob.setCustomNameVisible(false);
        mob.getPersistentDataContainer().remove(plugin.getNamespace());
    }

    /**
     * Remove health bars from all mobs
     */
    public void removeAllMobHealthBars() {
        Bukkit.getScheduler().cancelTasks(plugin);

        mobTable.clear();

        for (World world : plugin.getServer().getWorlds())
            for (LivingEntity entity : world.getLivingEntities())
                if (entity.getType() != EntityType.PLAYER)
                    hideMobBar(entity);
    }

    /**
     * Gets the real name of the mob, even if it doesn't have the health bar
     *
     * @param mob Mob whose name is being searched
     *
     * @return The name of the mob or {@code null} if it has no name
     */
    @Nullable
    public String getNameWhileHavingBar(@NotNull final LivingEntity mob) {
        final String cname = mob.getCustomName();
        if (cname == null)
            return null;

        if (mob.getPersistentDataContainer().has(plugin.getNamespace(), PersistentDataType.BYTE)) {
            if (plugin.getSettings().showMobBarOnCustomNames) {
                final CustomNameSetting sb = namesTable.get(mob.getEntityId());
                if (sb != null)
                    return sb.getName();
            }
            return null;
        } else
            return cname; // Real name, return it
    }

    /**
     * Checks if an entity has a health bar
     *
     * @param entity Entity to check
     *
     * @return True if the entity has the health bar, otherwise false
     */
    public boolean hasBar(@NotNull final LivingEntity entity) {
        if (entity instanceof Player) {
            final Team team = plugin.getServer().getScoreboardManager().getMainScoreboard().getEntryTeam(entity.getName());

            return team != null && team.getName().contains("hbr");
        } else
            return entity.getPersistentDataContainer().has(plugin.getNamespace(), PersistentDataType.BYTE);
    }

    /**
     * Build and display the health bar on the entity
     *
     * @param entity Entity on which to show the bar
     */
    private void showMobHealthBar(@NotNull final LivingEntity entity) {
        final Settings settings = plugin.getSettings();
        final double health = entity.getHealth();
        final double max = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        // If the entity is dead
        if (health <= 0.0)
            return;

        // What type of health should be displayed?
        final String displayString;
        switch (settings.mobBarType) {
            case BAR:
                displayString = settings.mobBar.get(Utils.roundUpPositiveWithMax(health / max * 20.0, 20));
                break;

            case CUSTOM_TEXT:
                displayString = settings.mobBarCustomText
                        .replace("{h}", String.valueOf(Utils.roundUpPositive(health)))
                        .replace("{m}", String.valueOf(Utils.roundUpPositive(max)))
                        .replace("{n}", getName(entity));
                break;

            case DEFAULT_TEXT:
                displayString = "Health: " + Utils.roundUpPositive(health) + "/" + Utils.roundUpPositive(max);
                break;

            default:
                displayString = null;
                break;
        }
        entity.setCustomName(displayString);
        entity.getPersistentDataContainer().set(plugin.getNamespace(), PersistentDataType.BYTE, (byte) 1);

        if (!settings.mobBarSemiHidden && displayString != null) // Check for visibility
            entity.setCustomNameVisible(true);
    }

    /**
     * Shows or hides the player's health bar depending on his health
     *
     * @param player Player whose bar to refresh
     */
    private void updatePlayerHealthBar(@NotNull final Player player) {
        // If the health is 0 remove the bar
        if (player.getHealth() == 0)
            plugin.getPlayerBarManager().hideAfterHealthBar(player);
        else
            plugin.getPlayerBarManager().setHealthSuffix(player);
    }

    /**
     * Hides the health bar once the delay has passed
     *
     * @param entity Entity whose bar is to be hidden
     */
    private void hideMobBarLater(@NotNull final LivingEntity entity) {
        mobTable.put(entity.getEntityId(), Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            mobTable.remove(entity.getEntityId());
            hideMobBar(entity);
        }, plugin.getSettings().mobBarHideDelay));
    }

    /**
     * Hides the health bar once the delay has passed
     *
     * @param player Player whose bar is to be hidden
     */
    private void hidePlayerBarLater(@NotNull final Player player) {
        playerTable.put(player.getName(), Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            playerTable.remove(player.getName());
            plugin.getPlayerBarManager().hideAfterHealthBar(player);
        }, plugin.getSettings().barAfterHideDelay));
    }

    /**
     * Gets the custom name of the mob. If it is not set it fetches the translated name in the local.yml file.
     *
     * @param mob Entity of which get the custom name
     *
     * @return The name to be placed in the health bar
     */
    @NotNull
    private String getName(@NotNull final LivingEntity mob) {
        if (!mob.getPersistentDataContainer().has(plugin.getNamespace(), PersistentDataType.BYTE))
            return mob.getCustomName(); // Return real name

        final CustomNameSetting sb = namesTable.get(mob.getEntityId());
        if (sb != null)
            return sb.getName();

        final String translatedName = plugin.getSettings().localeMap.get(mob.getType().toString());
        return translatedName != null ? translatedName : "";
    }

    /**
     * Checks if the entity already has a custom name (set by another plugin or with a name tag)
     *
     * @param entity Entity to check
     *
     * @return True if the entity has a custom name (not set by HealthBar), otherwise false
     */
    private boolean isNamed(@NotNull final LivingEntity entity) {
        return entity.getCustomName() != null && !entity.getPersistentDataContainer().has(plugin.getNamespace(), PersistentDataType.BYTE);
    }
}
