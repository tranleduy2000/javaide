package com.duy.testapplication.dex;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.testapplication.autocomplete.JavaUtil;
import com.duy.testapplication.datastructure.Dictionary;
import com.duy.testapplication.model.ClassDescription;

import java.io.File;
import java.util.ArrayList;

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


    @NonNull
    public ArrayList<ClassDescription> findClass(String namePrefix) {
        return mDictionary.find("class", namePrefix);
    }

    @Nullable
    public String findSuperClassName(String className) {
        ArrayList<ClassDescription> classes = this.findClass(className);
        ClassDescription currentClass = null;
        for (ClassDescription aClass : classes) {
            if (aClass.getClassName().equals(className)) {
                currentClass = aClass;
                break;
            }
        }
        return currentClass == null ? null : currentClass.getSuperClass();
    }

    public ArrayList<ClassDescription> findClassMember(String className, String namePrefix) {
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

    public ClassDescription loadClass(String className, String classpath, boolean loadClassMembers) {
        mClassReader.load();
        ClassDescription aClass = mClassReader.readClassByName(className, true);
        return this.addClass(aClass, System.currentTimeMillis());
    }

    private ClassDescription addClass(ClassDescription classDescription, long lastUsed) {
        String className = classDescription.getName();
        String inverseName = JavaUtil.getInverseName(className);

        mDictionary.remove("class", className);
        mDictionary.remove("class", inverseName);
        mDictionary.add("class", className, classDescription);
        mDictionary.add("class", inverseName, classDescription);

        if (classDescription.getFields().size() > 0) {
            mDictionary.removeCategory(classDescription.getClassName());
            for (String member : classDescription.getFields()) {
                this.addClassMember(classDescription, member, lastUsed);
            }
        }


        return classDescription;
    }

    private void addClassMember(ClassDescription classDesc, String member, long lastUsed) {
        try {
            String simpleName = classDesc.getSimpleName();
            String prototype = member.replaceAll("\\).*", ");").trim();


        } catch (Exception e) {

        }
    }

    public void loadAll() {

    }
}
