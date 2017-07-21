package com.duy.ide.autocomplete.model;


import android.util.Log;

import com.duy.ide.autocomplete.util.JavaUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * Created by Duy on 20-Jul-17.
 */

public class ClassDescription implements Description {
    private String name, simpleName, className, extend, packageName;
    private long lastUsed = 0;

    private ArrayList<ConstructorDescription> constructors;
    private ArrayList<FieldDescription> fields;
    private ArrayList<MethodDescription> methods;

    public ClassDescription(String simpleName, String className, String extend, long lastUsed) {
        this.name = simpleName;
        this.simpleName = simpleName;
        this.className = className;
        this.extend = extend;
        packageName = JavaUtil.getPackageName(className);
        this.lastUsed = 0;

        constructors = new ArrayList<>();
        fields = new ArrayList<>();
        methods = new ArrayList<>();
    }

    public ClassDescription(Class value) {
        this.simpleName = value.getSimpleName();
        this.name = value.getSimpleName();
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

    public Class getType() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }



    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSimpleName() {
        return simpleName;
    }


    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSuperClass() {
        return extend;
    }

    public String getPackageName() {
        return packageName;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    public ArrayList<ConstructorDescription> getConstructors() {
        return constructors;
    }

    public void setConstructors(ArrayList<ConstructorDescription> constructors) {
        this.constructors = constructors;
    }

    public ArrayList<FieldDescription> getFields() {
        return fields;
    }

    private static final String TAG = "ClassDescription";

    public void addConstructor(ConstructorDescription constructorDescription) {
        Log.d(TAG, "addConstructor() called with: classConstructor = [" + constructorDescription + "]");

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
        return className;
    }

    public ArrayList<Description> getMember(String suffix) {
        ArrayList<Description> result = new ArrayList<>();
        for (ConstructorDescription constructor : constructors) {
            if (constructor.getName().startsWith(suffix)) {
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
