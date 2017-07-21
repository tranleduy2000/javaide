package com.duy.ide.autocomplete.dex;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.duy.ide.autocomplete.datastructure.Dictionary;
import com.duy.ide.autocomplete.model.ClassDescription;
import com.duy.ide.autocomplete.model.Description;
import com.duy.ide.autocomplete.model.FieldDescription;
import com.duy.ide.autocomplete.model.MethodDescription;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaDexClassLoader {
    private static final String TAG = "JavaDexClassLoader";
    private JavaClassReader mClassReader;
    private Dictionary mDictionary;

    public JavaClassReader getClassReader() {
        return mClassReader;
    }

    public JavaDexClassLoader(File classpath, File outDir) {
        mDictionary = new Dictionary();
        mClassReader = new JavaClassReader(classpath.getPath(), outDir.getPath());
    }

    @NonNull
    public ArrayList<ClassDescription> findClass(String namePrefix) {
        Log.d(TAG, "findClass() called with: namePrefix = [" + namePrefix + "]");
        return mDictionary.find(ClassDescription.class, "class", namePrefix);
    }

    @Nullable
    public String findSuperClassName(String className) {
        ArrayList<ClassDescription> classes = this.findClass(className);
        ClassDescription currentClass = null;
        for (Description aClass : classes) {
            if (aClass instanceof ClassDescription) {
                if (((ClassDescription) aClass).getClassName().equals(className)) {
                    currentClass = (ClassDescription) aClass;
                    break;
                }
            }
        }
        return currentClass == null ? null : currentClass.getSuperClass();
    }

    public ArrayList<Description> findClassMember(String className, String namePrefix) {
        Log.d(TAG, "findClassMember() called with: className = [" + className + "], namePrefix = [" + namePrefix + "]");
        return mDictionary.find(className, namePrefix);
    }

    public void touchClass(String className) {
        ArrayList<ClassDescription> classDescriptions = findClass(className);
        if (classDescriptions.size() > 0) {
            this.touch(classDescriptions.get(0));
        }
    }

    private void touch(ClassDescription classDescription) {
        mDictionary.touch(classDescription);
    }

    public ClassDescription loadClass(String className) {
        Log.d(TAG, "loadClass() called with: className = [" + className + "]");

        ClassDescription aClass = mClassReader.readClassByName(className);
        return addClass(aClass, System.currentTimeMillis());
    }

    public void loadAllClasses(boolean fullRefresh) {
        mClassReader.load();
        if (fullRefresh) {
            HashMap<String, Class> classes = mClassReader.getClasses();
            for (Map.Entry<String, Class> entry : classes.entrySet()) {
                loadClass(entry.getKey());
            }
        }
    }

    private ClassDescription addClass(ClassDescription classDescription, long lastUsed) {
        String className = classDescription.getClassName();
        mDictionary.put("class", className, classDescription);

        if (classDescription.getFields().size() > 0) {
            HashMap<String, Description> value = new HashMap<>();
            for (FieldDescription member : classDescription.getFields()) {
                Log.d(TAG, "addClass member = " + member);
                value.put(className, member);
            }
            for (MethodDescription member : classDescription.getMethods()) {
                Log.d(TAG, "addClass member = " + member);
                value.put(className, member);
            }
            mDictionary.putAll(className, value);
        }

        return classDescription;
    }
}
