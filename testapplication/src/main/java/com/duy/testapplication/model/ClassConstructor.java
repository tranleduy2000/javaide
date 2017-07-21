package com.duy.testapplication.model;

import java.lang.reflect.Constructor;

/**
 * Created by Duy on 20-Jul-17.
 */

public class ClassConstructor extends DescriptionImpl {
    private Constructor constructor;

    public ClassConstructor(Constructor constructor) {
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
        return null;
    }

    @Override
    public String toString() {
        return constructor.toString();
    }
}
