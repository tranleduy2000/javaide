package com.duy.testapplication.dex;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.testapplication.autocomplete.JavaUtil;
import com.duy.testapplication.datastructure.Dictionary;
import com.duy.testapplication.model.ClassDescription;
import com.duy.testapplication.model.Description;
import com.duy.testapplication.model.FieldDescription;
import com.duy.testapplication.model.Member;
import com.duy.testapplication.model.MethodDescription;

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

    public void loadAllClasses(boolean fullRefresh) {
        if (fullRefresh) {
            mClassReader.dispose();
            mClassReader.load();
        } else {
            mClassReader.load();
        }
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
            for (FieldDescription member : classDescription.getFields()) {
                this.addClassMember(classDescription, member, member, lastUsed);
            }
            for (MethodDescription member : classDescription.getMethods()) {
                this.addClassMember(classDescription, member, member, lastUsed);
            }
        }


        return classDescription;
    }

    private void addClassMember(ClassDescription classDesc, Member member, Description description, long lastUsed) {
        mDictionary.add(classDesc.getClassName(), member.getPrototype(), description);
    }

    public void loadAll() {

    }
}
