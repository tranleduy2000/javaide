package com.duy.ide.javaide.editor.autocomplete.model;


import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.javaide.editor.autocomplete.util.JavaUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * Created by Duy on 20-Jul-17.
 */

public class ClassDescription extends DescriptionImpl {
    private static final String TAG = "ClassDescription";
    private final String simpleName;
    private final String className;
    private final String packageName;
    private final ArrayList<ConstructorDescription> constructors;
    private final ArrayList<FieldDescription> fields;
    private final ArrayList<MethodDescription> methods;

    private String extend;

    public ClassDescription(String simpleName, String className, String extend, long lastUsed) {
        this.simpleName = simpleName;
        this.className = className;
        this.extend = extend;
        packageName = JavaUtil.getPackageName(className);
        this.lastUsed = 0;

        constructors = new ArrayList<>();
        fields = new ArrayList<>();
        methods = new ArrayList<>();
    }

    @SuppressWarnings("unused")
    public ClassDescription(Class value) {
        this.simpleName = value.getSimpleName();
        this.className = value.getName();
        if (value.getSuperclass() != null) {
            this.extend = value.getSuperclass().getName();
        }
        this.packageName = JavaUtil.getPackageName(className);
        this.lastUsed = 0;


        constructors = new ArrayList<>();
        fields = new ArrayList<>();
        methods = new ArrayList<>();

        for (Constructor constructor : value.getConstructors()) {
            if (Modifier.isPublic(constructor.getModifiers())) {
                addConstructor(new ConstructorDescription(constructor));
            }
        }
        for (Field field : value.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers())) {
                if (!field.getName().equals(field.getDeclaringClass().getName())) {
                    addField(new FieldDescription(field));
                }
            }
        }
        for (Method method : value.getMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                addMethod(new MethodDescription(method));
            }
        }
    }

    @Override
    public String getName() {
        return simpleName;
    }

    @Override
    public final String getDescription() {
        return packageName;
    }

    @Override
    public String getReturnType() {
        return "";
    }

    @Override
    public char getTypeHeader() {
        return DescriptionImpl.CLASS_DESC;
    }

    @Override
    public String getInsertText() {
        return getSimpleName() + " ";
    }

    @Override
    public int getSuggestionPriority() {
        return DescriptionImpl.CLASS_DESC;
    }


    public final String getSimpleName() {
        return simpleName;
    }

    public final String getClassName() {
        return className;
    }

    public final String getSuperClass() {
        return extend;
    }

    public final String getPackageName() {
        return packageName;
    }

    public ArrayList<ConstructorDescription> getConstructors() {
        return constructors;
    }

    public ArrayList<FieldDescription> getFields() {
        return fields;
    }

    public void addConstructor(ConstructorDescription constructorDescription) {
        this.constructors.add(constructorDescription);
    }

    public void addField(FieldDescription fieldDescription) {
        fields.add(fieldDescription);
    }

    public void addMethod(MethodDescription methodDescription) {
        methods.add(methodDescription);
    }

    public ArrayList<MethodDescription> getMethods() {
        return methods;
    }

    @Override
    public String toString() {
        return simpleName + "(" + packageName + ")";
    }

    public ArrayList<SuggestItem> getMember(String suffix) {
        ArrayList<SuggestItem> result = new ArrayList<>();
        for (ConstructorDescription constructor : constructors) {
            if (!suffix.isEmpty() && constructor.getName().startsWith(suffix)) {
                result.add(constructor);
            }
        }
        for (FieldDescription field : fields) {
            if (suffix.isEmpty() || field.getName().startsWith(suffix)) {
                result.add(field);
            }
        }
        for (MethodDescription method : methods) {
            if (suffix.isEmpty() || method.getName().startsWith(suffix)) {
                result.add(method);
            }
        }
        return result;
    }
}
