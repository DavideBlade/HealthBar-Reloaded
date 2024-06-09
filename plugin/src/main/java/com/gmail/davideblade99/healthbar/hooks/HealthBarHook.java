package com.gmail.davideblade99.healthbar.hooks;

/**
 * Interface representing a hook between HealthBar and the API of another plugin
 *
 * @param <T> Third-party plugin API class
 *
 * @since 2.0.3.6
 */
public interface HealthBarHook<T> {

    /**
     * @return The instance of the API class to which HealthBar is to be hooked
     *
     * @since 2.0.3.6
     */
    T getAPI();
}