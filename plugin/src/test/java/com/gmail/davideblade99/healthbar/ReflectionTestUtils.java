package com.gmail.davideblade99.healthbar;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;


/**
 * Provides reflection utility methods for testing. Use only if strictly necessary!
 */
final class ReflectionTestUtils {

    private ReflectionTestUtils() { }

    /**
     * Sets the value of a private field
     *
     * @param target    Object instance in which to set the field
     * @param fieldName Field name
     * @param value     Value to which to set the field
     *
     * @throws NoSuchFieldException   If a field with the specified name is not found
     * @throws IllegalAccessException If the {@code Field} object is enforcing Java language access control and the
     *                                underlying field is inaccessible or final; or if this {@code Field} object
     *                                has no write access
     */
    public static void setField(@NotNull final Object target, @NotNull final String fieldName, @NotNull final Object value) throws NoSuchFieldException, IllegalAccessException {
        final Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
