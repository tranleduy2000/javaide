package com.duy.ide.javaide.editor.autocomplete.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by Duy on 20-Jul-17.
 */

public class FieldDescription extends DescriptionImpl implements Member {
    private String name, type;
    private int modifiers;
    private String value;

    public FieldDescription(String name, String type, int modifiers) {
        this.name = name;
        this.type = type;
        this.modifiers = modifiers;
    }

    public FieldDescription(Field field) {
        this.name = field.getName();
        this.type = field.getType().getName();
        this.modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            try {
                value = field.get(null).toString();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public char getTypeHeader() {
        return 'f'; //field
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return value;
    }

    @Override
    public String getReturnType() {
        return type;
    }

    @Override
    public String getInsertText() {
        return name;
    }

    @Override
    public int getSuggestionPriority() {
        return DescriptionImpl.FIELD_DESC;
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
