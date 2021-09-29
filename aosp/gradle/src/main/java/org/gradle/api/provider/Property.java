package org.gradle.api.provider;

import com.android.annotations.Nullable;

import org.gradle.api.Incubating;

/**
 * A {@code Provider} representation for capturing the state of a property. The value can be provided by using the method {@link #set(Object)} or {@link #set(Provider)}.
 * <p>
 * <p><b>Note:</b> This interface is not intended for implementation by build script or plugin authors. An instance of this class can be created through the factory method {@link org.gradle.api.model.ObjectFactory#property(Class)}. There are also several specialized subtypes of this interface that can be created using various other factory methods.
 *
 * @param <T> Type of value represented by the property
 * @since 4.3
 */
@Incubating
public interface Property<T> extends Provider<T> {
    /**
     * Sets the value of the property the given value.
     * <p>
     * <p>This method can also be used to clear the value of the property, by passing {@code null} as the value.
     *
     * @param value The value, can be null.
     */
    void set(@Nullable T value);

    /**
     * Sets the property to have the same value of the given provider. This property will track the value of the provider and query its value each time the value of the property is queried. When the provider has no value, this property will also have no value.
     *
     * @param provider Provider
     */
    void set(Provider<? extends T> provider);
}