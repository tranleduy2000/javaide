package com.duy.ide.autocomplete.model;

/**
 * Created by Duy on 20-Jul-17.
 */

public interface Member {
    String getName();

    String getPrototype();

    Class getReturnType();

    int getModifiers();
}
