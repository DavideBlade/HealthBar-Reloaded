package com.gmail.davideblade99.healthbar.manager;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.gmail.davideblade99.healthbar.BarShowCondition.AlwaysWhenLooking;
import com.gmail.davideblade99.healthbar.BarShowCondition.BelowPercentage;
import com.gmail.davideblade99.healthbar.BarShowCondition.OnDamageAndLooking;
import com.gmail.davideblade99.healthbar.BarShowCondition.OnDamageOrLooking;
import com.gmail.davideblade99.healthbar.HealthBar;
import com.gmail.davideblade99.healthbar.NamedMobPolicy;
import com.gmail.davideblade99.healthbar.Settings;
import com.gmail.davideblade99.healthbar.util.AppendedBar;
import com.gmail.davideblade99.healthbar.util.CustomNameSetting;
import com.gmail.davideblade99.healthbar.util.Utils;
import org.apache.commons.lang.StringUtils;
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

    /** Map the entity (ID) with the health bar appended to the existing mob custom name (if any) */
    private final Map<Integer, AppendedBar> appendTable = new HashMap<>();

    private final HealthBar plugin;

    public EntityTrackerManager(@NotNull final HealthBar plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles all actions required when a living entity is hit.
     *
     * If the entity qualifies for a health bar and the plugin is enabled in its world, this method will display or
     * update the health bar and schedule its removal after the configured delay (if any).
     *
     * @param attacked        The entity that was hit
     * @param damagedByEntity Whether the entity has been hit by another entity
     */
    public void registerMobHit(@NotNull final LivingEntity attacked, final boolean damagedByEntity) {
        this.registerMobHit(attacked, damagedByEntity, false);
    }

    /**
     * Handles all actions required when a living entity is hit.
     *
     * If the entity qualifies for a health bar and the plugin is enabled in its world, this method will display or
     * update the health bar and schedule its removal after the configured delay (if any).
     *
     * @param attacked        The entity that was hit
     * @param damagedByEntity Whether the entity has been hit by another entity
     * @param forceUpdate           Whether the health bar must be displayed even if it is not currently visible (as opposed
     *                        to only updating an existing bar)
     *
     * @since 2.0.5
     */
    public void registerMobHit(@NotNull final LivingEntity attacked, final boolean damagedByEntity,
                               final boolean forceUpdate) {
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
        if (!settings.barOnMythicMobs && plugin.getMythicMobsAPI() != null && plugin.getMythicMobsAPI()
                .isMythicMob(attacked))
            return;

        // Check mobs of LevelledMobs
        if (!"HealthBar".equalsIgnoreCase(settings.barOnLevelledMobs) && plugin.getLevelledMobsAPI() != null &&
                plugin.getLevelledMobsAPI().isLevelled(attacked))
            return;

        // Check mobs of AuraMobs
        if (!settings.barOnAuraMobs && plugin.getAuraMobsMobs() != null && plugin.getAuraMobsMobs().isAuraMob(attacked))
            return;

        // Check stacked mobs (with WildStacker)
        if (!settings.barOnStackedMobs && plugin.isWildStackerEnabled() && WildStackerAPI.getEntityAmount(attacked) > 1)
            return;

        // Custom name check
        if (isNamed(attacked)) {
            switch (settings.barOnNamedMobPolicy) {
                case IGNORE:
                    return; // The bar must not be shown on renamed entities

                case OVERRIDE:
                    namesTable.put(attacked.getEntityId(),
                            new CustomNameSetting(attacked.getCustomName(), attacked.isCustomNameVisible()));
                    break;

                case APPEND:
                    break;
            }
        }

        // Check show condition threshold
        if (settings.mobBarShowCondition instanceof BelowPercentage showCondition
                && showCondition.percentage() <=
                attacked.getHealth() / attacked.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * 100)
            return;

        // If the bar should remain visible
        if (settings.mobBarHideDelay <= 0) {
            showMobHealthBar(attacked, damagedByEntity);
            return;
        }


        // Always display if hit by entity
        if (damagedByEntity) {
            final Integer taskID = mobTable.remove(attacked.getEntityId());
            if (taskID != null)
                Bukkit.getScheduler().cancelTask(taskID); // Eventually cancel previous task

            showMobHealthBar(attacked, damagedByEntity);
            hideMobBarLater(attacked);
        } else {
            // It's not damaged by entity, if the health was displayed only update it
            if (mobTable.containsKey(attacked.getEntityId()) || forceUpdate)
                showMobHealthBar(attacked, damagedByEntity);
        }
    }

    public void registerPlayerHit(@NotNull final Player player, final boolean damagedByEntity) {
        final Settings settings = plugin.getSettings();
        final String pname = player.getName();

        // Update the health bar under the name
        plugin.getPlayerBarManager().updateHealthBelow(player);

        // If the bar next to the name is not enabled return
        if (!settings.afterBarEnabled)
            return;

        // Check show condition threshold
        if (settings.afterBarShowCondition instanceof BelowPercentage showCondition
                && showCondition.percentage() <=
                player.getHealth() / player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * 100)
            return;


        // Check whether the bar should always be shown
        if (settings.afterBarHideDelay <= 0) {
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
        if (!hasBar(mob))
            return; // It's a real name! Don't touch it

        // Cancel eventual task
        final Integer id = mobTable.remove(mob.getEntityId());
        if (id != null)
            Bukkit.getScheduler().cancelTask(id);

        // Leave the bar set, just show it when the player looks at it
        if(plugin.getSettings().mobBarShowCondition instanceof OnDamageOrLooking) {
            mob.setCustomNameVisible(false);
            return;
        }

        if (plugin.getSettings().barOnNamedMobPolicy == NamedMobPolicy.OVERRIDE) {
            final CustomNameSetting nameSetting = namesTable.remove(mob.getEntityId());

            if (nameSetting != null) { // Return only if found, else hide normally
                mob.setCustomName(nameSetting.getName());
                mob.setCustomNameVisible(nameSetting.isShown());
                mob.getPersistentDataContainer().remove(plugin.getNamespace());
                return;
            }
        } else if (plugin.getSettings().barOnNamedMobPolicy == NamedMobPolicy.APPEND) {
            final AppendedBar appendedBar = appendTable.get(mob.getEntityId());

            if (appendedBar != null) { // Return only if found, else hide normally
                final String customName = mob.getCustomName();

                if (customName != null) { // If the custom name has not been removed in the meantime by a third party
                    mob.setCustomName(stripAppendedBar(mob));
                    mob.setCustomNameVisible(appendedBar.isShown());
                }

                appendTable.remove(mob.getEntityId());
                mob.getPersistentDataContainer().remove(plugin.getNamespace());
                return;
            }
        }

        // Mob without a custom name (it only has the health bar): use default method (hide the name)
        mob.setCustomName(null);
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
            return null; // No bar and no (original) custom name

        // If the mob has the bar
        if (hasBar(mob)) {
            final CustomNameSetting sb = namesTable.get(mob.getEntityId());

            return sb != null ? sb.getName() : null;
        }
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
            final Team team =
                    plugin.getServer().getScoreboardManager().getMainScoreboard().getEntryTeam(entity.getName());

            return team != null && team.getName().contains("hbr");
        } else
            return entity.getPersistentDataContainer().has(plugin.getNamespace(), PersistentDataType.BYTE);
    }

    /**
     * Build and display the health bar on the entity
     *
     * @param entity      Entity on which to show the bar
     * @param dueToAttack Whether the bar is displayed as a result of an attack sustained by the entity
     *
     * @since 2.0.5
     */
    private void showMobHealthBar(@NotNull final LivingEntity entity, final boolean dueToAttack) {
        final Settings settings = plugin.getSettings();
        final double health = entity.getHealth();
        final double max = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        // If the entity is dead
        if (health <= 0.0)
            return;

         // What type of health should be displayed?
        String displayString = switch (settings.mobBarType) {
            case BAR -> settings.mobBar.get(Utils.roundUpPositiveWithMax(health / max * 20.0, 20) - 1);
            case CUSTOM_TEXT -> settings.mobBarCustomText
                    .replace("{h}", String.valueOf(Utils.roundUpPositive(health)))
                    .replace("{m}", String.valueOf(Utils.roundUpPositive(max)))
                    .replace("{n}", getName(entity));
            case DEFAULT_TEXT -> StringUtils.capitalize(entity.getType().getKey().getKey())
                    .replaceAll("_", " ") + " " + Utils.roundUpPositive(health) + "§c❤";
        };

          if (settings.barOnNamedMobPolicy == NamedMobPolicy.APPEND) {
            final String currentCustomName = entity.getCustomName();
            final AppendedBar oldBar = appendTable.get(entity.getEntityId());

            appendTable.put(entity.getEntityId(), new AppendedBar(displayString,
                    /*
                     * If there is an oldBar, it means that HealthBar-Reloaded has already set a bar
                     * and the custom name will surely be visible (otherwise the health bar won't be visible):
                     * fetch the original boolean value of isCustomNameVisible() set by 3rd party or by default.
                     */
                    oldBar != null ? oldBar.isShown() : entity.isCustomNameVisible())
            );

            // Entity with a custom name set by third party: append the new bar, eventually replacing the old one
            if (currentCustomName != null &&
                    ((oldBar != null && !currentCustomName.equals(oldBar.getBar())) || !hasBar(entity)))
                displayString = (oldBar == null ?
                                 currentCustomName :
                                 currentCustomName.replace(" " + oldBar.getBar(), "")) + " " + displayString;
        }

        entity.setCustomName(displayString);
        entity.getPersistentDataContainer().set(plugin.getNamespace(), PersistentDataType.BYTE, (byte) 1);

        // Check for visibility
        if (displayString != null &&
                !(settings.mobBarShowCondition instanceof AlwaysWhenLooking) &&
                !(settings.mobBarShowCondition instanceof OnDamageAndLooking) &&
                (!(settings.mobBarShowCondition instanceof OnDamageOrLooking) || dueToAttack)
        )
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
        }, plugin.getSettings().afterBarHideDelay));
    }

    /**
     * Gets the custom name of the mob. If it is not set it fetches the translated name in the locale.yml file.
     *
     * @param mob Entity of which get the custom name
     *
     * @return The name to be placed in the health bar
     */
    @NotNull
    private String getName(@NotNull final LivingEntity mob) {
        String customName = mob.getCustomName();

        if (hasBar(mob)) { // The custom name contains the bar

            // Retrieves the original custom name before the bar was applied
            switch (plugin.getSettings().barOnNamedMobPolicy) {
                case OVERRIDE -> {
                    final CustomNameSetting sb = namesTable.get(mob.getEntityId());

                    customName = sb != null && sb.getName() != null ? sb.getName() : null;
                }
                case APPEND -> customName = stripAppendedBar(mob);
            }
        }

        final String translatedName = plugin.getSettings().localeMap.get(mob.getType().toString());

        return (customName != null && !customName.isEmpty() ?
                customName : // Return the original custom name before the bar was applied
                (
                        translatedName != null ?
                        translatedName : // Return the translated name
                        mob.getName() // Return real (vanilla) name
                )
        );
    }

    /**
     * Checks if the entity already has a custom name (set by another plugin or with a name tag)
     *
     * @param entity Entity to check
     *
     * @return True if the entity has a custom name (not set by HealthBar), otherwise false
     */
    private boolean isNamed(@NotNull final LivingEntity entity) {
        return entity.getCustomName() != null && !hasBar(entity);
    }

    /**
     * Retrieves the original custom name that the entity had before the bar was appended
     *
     * @return The entity's custom name or {@code null} if it had none
     *
     * @since 2.0.4.1
     */
    @Nullable
    private String stripAppendedBar(@NotNull final LivingEntity mob) {
        final String customName = mob.getCustomName();
        final AppendedBar appendedBar = appendTable.get(mob.getEntityId());

        if (plugin.getSettings().barOnNamedMobPolicy != NamedMobPolicy.APPEND || !hasBar(mob) || customName == null ||
                appendedBar == null)
            return customName;

        // The bar is appended to the right, so only the last occurrence has to be replaced: the rest is not the bar
        final String originalName = customName.replaceAll(" ?" + appendedBar.getBar() + "$", "");
        return originalName.isEmpty() ? null : originalName;
    }
}
