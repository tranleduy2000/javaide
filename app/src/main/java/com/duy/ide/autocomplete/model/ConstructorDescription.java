package com.duy.ide.autocomplete.model;

import java.lang.reflect.Constructor;

/**
 * Created by Duy on 20-Jul-17.
 */

public class ConstructorDescription extends DescriptionImpl {
    private Constructor constructor;

    public ConstructorDescription(Constructor constructor) {
        this.constructor = constructor;
    }

    @Override
    public String getName() {
        return constructor.getName();
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Class getType() {
        return null;
    }

    @Override
    public String getSnippet() {
        return constructor.getName();
    }

    @Override
    public String toString() {
        return constructor.toString();
    }
}
