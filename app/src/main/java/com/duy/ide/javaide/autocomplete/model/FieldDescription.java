package com.duy.ide.javaide.autocomplete.model;

import java.lang.reflect.Field;

/**
 * Created by Duy on 20-Jul-17.
 */

public class FieldDescription extends DescriptionImpl implements Member {
    private String name, type;
    private int modifiers;

    public FieldDescription(String name, String type, int modifiers) {
        this.name = name;
        this.type = type;
        this.modifiers = modifiers;
    }

    public FieldDescription(Field field) {
        this.name = field.getName();
        this.type = field.getType().getName();
        this.modifiers = field.getModifiers();
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getSnippet() {
        return name;
    }

    @Override
    public int getDescriptionType() {
        return DescriptionImpl.FIELD_DESC;
    }

    @Override
    public String getPrototype() {
        return name;
    }

    @Override
    public int getModifiers() {
        return modifiers;
    }

    @Override
    public String toString() {
        return name;
    }
}
