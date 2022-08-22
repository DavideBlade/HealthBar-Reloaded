package com.gmail.davideblade99.healthbar;

import com.gmail.davideblade99.healthbar.manager.EntityTrackerManager;
import com.gmail.davideblade99.healthbar.manager.PlayerBarManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Initializes Bukkit components and plugin for all tests
 */
public final class InitializationRunner implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    public static HealthBar healthBar;
    public static LivingEntity fakeEntity;

    private static boolean started = false;

    private static Path tempFolder;

    /**
     * Code executed before any test
     */
    @Override
    public void beforeAll(final ExtensionContext context) throws IOException, InvalidDescriptionException, NoSuchFieldException, IllegalAccessException {
        if (!started) {
            started = true;
            // The following line registers a callback hook when the root test context is shut down
            context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).put("HealthBar-Test", this);

            // Create temp config.yml
            tempFolder = Files.createTempDirectory("junit");
            Files.copy(InitializationRunner.class.getResourceAsStream("/config.test.yml"), tempFolder.resolve("config.yml"));

            // Mock various Bukkit components
            final Server server = mock(Server.class);
            final PluginManager pluginManager = mock(PluginManager.class);
            given(server.getPluginManager()).willReturn(pluginManager);
            given(server.getLogger()).willReturn(Logger.getAnonymousLogger());
            given(server.getVersion()).willReturn("1.19");
            given(server.getScheduler()).willReturn(mock(BukkitScheduler.class));
            Bukkit.setServer(server);

            final ScoreboardManager scoreboardManager = mock(ScoreboardManager.class);
            given(server.getScoreboardManager()).willReturn(scoreboardManager);
            given(scoreboardManager.getMainScoreboard()).willReturn(mock(Scoreboard.class));
            given(scoreboardManager.getMainScoreboard().registerNewObjective(notNull(), notNull(), notNull())).willReturn(mock(Objective.class));
            given(scoreboardManager.getMainScoreboard().registerNewTeam(notNull())).willReturn(mock(Team.class));

            // Initialize fake world
            final World world = mock(World.class);
            given(world.getName()).willReturn("test-world");

            // Initialize fake entity
            fakeEntity = mock(LivingEntity.class);
            final AttributeInstance attribute = mock(AttributeInstance.class);
            final PersistentDataContainer persistentDataContainer = mock(PersistentDataContainer.class);
            final DataContainer dataContainer = new DataContainer();
            given(fakeEntity.getType()).willReturn(EntityType.SPIDER);
            given(fakeEntity.getEntityId()).willReturn(5);
            given(fakeEntity.getWorld()).willReturn(world);
            given(fakeEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).willReturn(attribute);
            given(attribute.getValue()).willReturn(20D);
            given(fakeEntity.getHealth()).willReturn(20D);
            given(fakeEntity.getPersistentDataContainer()).willReturn(persistentDataContainer);
            /*
             * Connect custom name getter & setter.
             * In other words, when the setCustomName(...) method is called,
             * it sets the value that should return getCustomName()
             */
            doAnswer(answer -> when(fakeEntity.getCustomName()).thenReturn((String) answer.getArguments()[0])).when(fakeEntity).setCustomName(notNull());
            /*
             * Connect persistent data container set(...), remove(...) & has(...).
             *
             * Specifically, when the dataContainer.set(...) method is called,
             * it sets the return of dataContainer.has(...) to true;
             * when the dataContainer.remove(...) method is called,
             * it sets the return of dataContainer.has(...) to false.
             */
            doAnswer(answer -> {
                dataContainer.registerData((NamespacedKey) answer.getArguments()[0], (PersistentDataType<?, ?>) answer.getArguments()[1]);

                return Void.TYPE;
            }).when(persistentDataContainer).set(notNull(), notNull(), notNull());
            doAnswer(answer -> {
                dataContainer.removeData((NamespacedKey) answer.getArguments()[0]);

                return Void.TYPE;
            }).when(persistentDataContainer).remove(notNull());
            doAnswer(answer -> dataContainer.hasData((NamespacedKey) answer.getArguments()[0], (PersistentDataType<?, ?>) answer.getArguments()[1])).when(persistentDataContainer).has(notNull(), notNull());

            // Initialize plugin
            final JavaPluginLoader pluginLoader = new JavaPluginLoader(server);
            final PluginDescriptionFile descriptionFile = new PluginDescriptionFile(InitializationRunner.class.getResourceAsStream("/plugin.yml"));
            healthBar = new HealthBar(pluginLoader, descriptionFile, tempFolder.toFile(), tempFolder.toFile());
            ReflectionTestUtils.setField(healthBar, "instance", healthBar);
            ReflectionTestUtils.setField(healthBar, "settings", new Settings(healthBar));
            ReflectionTestUtils.setField(healthBar, "entityTrackerManager", new EntityTrackerManager(healthBar));
            ReflectionTestUtils.setField(healthBar, "playerBarManager", new PlayerBarManager(healthBar));
            ReflectionTestUtils.setField(healthBar, "namespace", new NamespacedKey(healthBar, "HealthBar"));

            // Initialize API
            DefaultBackendAPI.setImplementation(new DefaultBackendAPI(healthBar));

            assertAll(
                    () -> assertTrue(Files.exists(tempFolder.resolve("config.yml")), "Config.yml not created"),
                    () -> assertNotNull(Bukkit.getServer(), "Server is null"),
                    () -> assertNotNull(fakeEntity, "Fake entity is null"),
                    () -> assertNotNull(healthBar, "Plugin is null"),
                    () -> assertNotNull(HealthBar.getInstance(), "Plugin instance is null"),
                    () -> assertNotNull(healthBar.getEntityTrackerManager(), "Tracker manager is null"),
                    () -> assertNotNull(healthBar.getNamespace(), "Namespace is null"),
                    () -> assertNotNull(healthBar.getSettings(), "Settings are null"),
                    () -> assertNotNull(healthBar.getPlayerBarManager(), "Player bar manager is null"),
                    () -> assertNotNull(DefaultBackendAPI.getImplementation(), "API implementation is null"));
        }
    }

    /**
     * Code executed at the end of any test (even in cases of failure)
     */
    @Override
    public void close() {
        // Delete files inside temporary folder
        if (tempFolder.toFile().isDirectory())
            for (File c : tempFolder.toFile().listFiles())
                c.delete();
        tempFolder.toFile().delete(); // Delete folder
    }

    /**
     * Contains the data added and removed to the {@link PersistentDataContainer} of the {@code fakeEntity}
     */
    private static class DataContainer {
        private final Map<NamespacedKey, PersistentDataType<?, ?>> dataContainer = new HashMap<>();

        private void registerData(@NotNull final NamespacedKey namespacedKey, @NotNull final PersistentDataType<?, ?> persistentDataType) {
            dataContainer.put(namespacedKey, persistentDataType);
        }

        /**
         * @return True if the map contains the namespace-datatype pair, otherwise false
         */
        private boolean hasData(@NotNull final NamespacedKey namespacedKey, @NotNull final PersistentDataType<?, ?> persistentDataType) {
            final PersistentDataType<?, ?> persistentData = dataContainer.get(namespacedKey);

            return persistentData != null && persistentData.equals(persistentDataType);
        }

        void removeData(@NotNull final NamespacedKey namespacedKey) {
            dataContainer.remove(namespacedKey);
        }
    }
}
