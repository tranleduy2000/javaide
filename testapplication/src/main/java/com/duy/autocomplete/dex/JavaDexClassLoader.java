package com.duy.autocomplete.dex;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.duy.autocomplete.util.JavaUtil;
import com.duy.autocomplete.datastructure.Dictionary;
import com.duy.autocomplete.model.ClassDescription;
import com.duy.autocomplete.model.Description;
import com.duy.autocomplete.model.FieldDescription;
import com.duy.autocomplete.model.Member;
import com.duy.autocomplete.model.MethodDescription;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaDexClassLoader {
    private JavaClassReader mClassReader;
    private Dictionary mDictionary;

    public JavaDexClassLoader(File classpath, File outDir) {
        mDictionary = new Dictionary();
        mClassReader = new JavaClassReader(classpath.getPath(), outDir.getPath());
    }

    private static final String TAG = "JavaDexClassLoader";
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
        ClassDescription aClass = mClassReader.readClassByName(className, true);
        return addClass(aClass, System.currentTimeMillis());
    }

    public void loadAllClasses(boolean fullRefresh) {
        mClassReader.load();
        if (fullRefresh) {
            HashMap<String, Class> classes = mClassReader.getClasses();
            for (Map.Entry<String, Class> entry : classes.entrySet()) {
                loadClass(entry.getKey());
            }
        } else {
        }
    }

    private ClassDescription addClass(ClassDescription classDescription, long lastUsed) {
        String className = classDescription.getClassName();
        String inverseName = JavaUtil.getInverseName(className);

        mDictionary.remove("class", className);
        mDictionary.remove("class", inverseName);
        mDictionary.add("class", className, classDescription);
        mDictionary.add("class", inverseName, classDescription);

        if (classDescription.getFields().size() > 0) {
            mDictionary.removeCategory(classDescription.getClassName());
            for (FieldDescription member : classDescription.getFields()) {
                this.addClassMember(classDescription.getClassName(), member, member, lastUsed);
            }
            for (MethodDescription member : classDescription.getMethods()) {
                this.addClassMember(classDescription.getClassName(), member, member, lastUsed);
            }
        }

        return classDescription;
    }

    private void addClassMember(String category, Member member, Description description, long lastUsed) {
        mDictionary.add(category, member.getName(), description);
    }

    public void loadAll() {

    }
}
