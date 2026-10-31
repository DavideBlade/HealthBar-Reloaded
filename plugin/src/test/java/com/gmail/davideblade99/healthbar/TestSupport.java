package com.gmail.davideblade99.healthbar;

import com.gmail.davideblade99.healthbar.api.internal.BackendAPI;
import org.jetbrains.annotations.NotNull;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.plugin.PluginManagerMock;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Objects;

/**
 * Starts and stops HealthBar on a MockBukkit server
 *
 * @since 2.0.7
 */
public final class TestSupport {

    /**
     * HealthBar's configuration in the tests, in the test resources
     *
     * @since 2.0.7
     */
    public static final String CONFIG = "config.test.yml";

    /** Enforce non-instantiability with a private constructor */
    private TestSupport() {
        throw new IllegalAccessError();
    }

    /**
     * Loads and enables HealthBar with {@value #CONFIG} as its config.yml, so that the tests do not depend on the
     * default settings. The file is put in the plugin's folder before HealthBar is enabled, which reads it from there
     * as on a real server.
     *
     * @param server MockBukkit server
     *
     * @return The enabled plugin
     *
     * @see MockBukkit#load(Class)
     *
     * @since 2.0.7
     */
    @NotNull
    public static HealthBar loadWithTestConfig(@NotNull final ServerMock server) {
        final PluginManagerMock pluginManager = server.getPluginManager();
        final HealthBar plugin = (HealthBar) pluginManager.loadPlugin(HealthBar.class, new Object[0]);

        try (InputStream config = TestSupport.class.getResourceAsStream("/" + CONFIG)) {
            Files.copy(Objects.requireNonNull(config, CONFIG + " not found"),
                    plugin.getDataFolder().toPath().resolve("config.yml"));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        plugin.getDescription().getPermissions().forEach(pluginManager::addPermission);
        pluginManager.enablePlugin(plugin);
        return plugin;
    }

    /**
     * Removes the API implementation that HealthBar sets when enabled, so that HealthBar can be enabled again:
     * {@link BackendAPI} accepts only one implementation
     *
     * @see BackendAPI#setImplementation(BackendAPI)
     *
     * @since 2.0.7
     */
    public static void resetBackendAPI() {
        try {
            final Field implementation = BackendAPI.class.getDeclaredField("implementation");
            implementation.setAccessible(true);
            implementation.set(null, null);
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
