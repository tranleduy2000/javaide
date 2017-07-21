package com.duy.ide.autocomplete.model;

import java.lang.reflect.Field;

/**
 * Created by Duy on 20-Jul-17.
 */

public class FieldDescription implements Member , Description {
    private Field field;

    public FieldDescription(Field field) {
        this.field = field;
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public long getLastUsed() {
        return 0;
    }

    @Override
    public void setLastUsed(long time) {

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
    public String getPrototype() {
        return field.getName();
    }

    @Override
    public Class getReturnType() {
        return null;
    }

    @Override
    public String toString() {
        return field.getName();
    }
}
