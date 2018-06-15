/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.editor.autocomplete.parser;

import android.support.annotation.Nullable;

import com.android.annotations.NonNull;
import com.duy.android.compiler.project.JavaProject;
import com.duy.common.data.Pair;
import com.duy.common.interfaces.Filter;
import com.duy.ide.javaide.editor.autocomplete.model.ClassDescription;
import com.duy.ide.javaide.utils.DLog;
import com.sun.tools.javac.tree.JCTree;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaClassManager implements IClassManager {

    private static final String TAG = "JavaClassReader";
    private static final String JAVA_DOT_LANG_DOT = "java.lang.";
    private static JavaClassManager INSTANCE;

    /**
     * All classes sorted by simple class name, fastest find list of classes start with prefix
     */
    private final ArrayList<Pair<String, IClass>> mSimpleNames = new ArrayList<>();
    /**
     * All classes sorted by full class name, use for faster find class name by using binary search
     */
    private final ArrayList<IClass> mFullNames = new ArrayList<>();
    /**
     * Map contains parsed class, parsed class is java class in jar file or from user
     */
    private final HashMap<String, IClass> mLoaded = new HashMap<>();

    private File mBootClasspath;
    private File mTempDir;

    private JavaClassManager(File bootClassPath, File tempDir) {
        mBootClasspath = bootClassPath;
        mTempDir = tempDir;
    }

    public static JavaClassManager getInstance(File classpath, File tempDir) {
        if (INSTANCE == null) {
            INSTANCE = new JavaClassManager(classpath, tempDir);
        }
        return INSTANCE;
    }

    public static JavaClassManager getInstance() {
        if (INSTANCE == null) {
            throw new RuntimeException("JavaClassManager not init");
        }
        return INSTANCE;
    }

    @NonNull
    public ArrayList<IClass> getAllClasses() {
        return mFullNames;
    }

    public void loadFromProject(JavaProject project) {
        long time = System.currentTimeMillis();
        try {

            CompiledClassLoader classLoader = new CompiledClassLoader(mBootClasspath, mTempDir);
            ArrayList<Class> classes = classLoader.getCompiledClassesFromProject(project);
            for (Class clazz : classes) {
                getClassWrapper(clazz);
            }

            JavaParser parser = new JavaParser();
            ArrayList<File> javaSrcDirs = project.getJavaSrcDirs();
            for (File javaSrcDir : javaSrcDirs) {
                Collection<File> javaFiles = FileUtils.listFiles(
                        javaSrcDir,
                        new String[]{"java"},
                        true);
                for (File javaFile : javaFiles) {
                    if (DLog.DEBUG) DLog.d(TAG, "loadFromProject: parsing class " + javaFile);
                    String content = com.duy.android.compiler.utils.IOUtils.toString(javaFile);
                    JCTree.JCCompilationUnit ast = parser.parse(content);
                    List<IClass> parseClasses = parser.parseClasses(ast);
                    for (IClass aClass : parseClasses) {
                        update(aClass);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        System.out.println("Loaded classes " + (System.currentTimeMillis() - time));
    }

    /**
     * @param fullName - full class name
     */
    @Nullable
    public IClass getParsedClass(String fullName) {
        IClass cache = mLoaded.get(fullName);
        if (cache != null) {
            return cache;
        }
        return mLoaded.get(JAVA_DOT_LANG_DOT + fullName);
    }

    @NonNull
    public IClass getClassWrapper(@NonNull Class clazz) {
        IClass cache = mLoaded.get(clazz.getName());
        if (cache != null) {
            return cache;
        }
        ClassDescription wrapper = new ClassDescription(clazz);
        update(wrapper);
        wrapper.initMembers(clazz);
        return wrapper;
    }

    @Override
    public void update(IClass value) {
        if (DLog.DEBUG) DLog.d(TAG, "update() called with: value = [" + value + "]");
        String fullClassName = value.getFullClassName();
        if (mLoaded.containsKey(fullClassName)) {
            mLoaded.put(fullClassName, value);
            return;
        }
        mLoaded.put(fullClassName, value);

        if (mFullNames.size() == 0) {
            mFullNames.add(value);
        } else {
            boolean found = false;
            for (int i = 0; i < mFullNames.size(); i++) {
                IClass iClass = mFullNames.get(i);
                if (fullClassName.compareTo(iClass.getFullClassName()) <= 0) {
                    mFullNames.add(i, value);
                    found = true;
                    break;
                }
            }
            if (!found) {
                mFullNames.add(value);
            }
        }
        String simpleName = value.getSimpleName();
        Pair<String, IClass> simplePair = new Pair<>(simpleName, value);
        if (mSimpleNames.size() == 0) {
            mSimpleNames.add(simplePair);
        } else {
            boolean found = false;
            for (int i = 0; i < mSimpleNames.size(); i++) {
                Pair<String, IClass> pair = mSimpleNames.get(i);
                if (simpleName.compareTo(pair.first) <= 0) {
                    mSimpleNames.add(i, simplePair);
                    found = true;
                    break;
                }
            }
            if (!found) {
                mSimpleNames.add(simplePair);
            }
        }
    }

    @Override
    public void remove(String fullClassName) {
        IClass remove = mLoaded.remove(fullClassName);
        int index = Collections.binarySearch(mFullNames, remove,
                new Comparator<IClass>() {
                    @Override
                    public int compare(IClass o1, IClass o2) {
                        return o1.getFullClassName().compareTo(o2.getFullClassName());
                    }
                });
        if (index >= 0) {
            mFullNames.remove(index);
        }
        Pair<String, IClass> key = new Pair<>(remove.getSimpleName(), remove);
        index = Collections.binarySearch(mSimpleNames, key,
                new Comparator<Pair<String, IClass>>() {
                    @Override
                    public int compare(Pair<String, IClass> o1,
                                       Pair<String, IClass> o2) {
                        return o1.first.compareTo(o2.first);
                    }
                });
        if (index >= 0) {
            mSimpleNames.remove(index);
        }
    }

    /**
     * search class with binary search
     *
     * @param classes - sorted classes by name
     * @param prefix  - prefix if class name
     */
    @Nullable
    private List<Pair<String, IClass>> binarySearch(
            @NonNull List<Pair<String, IClass>> classes,
            @NonNull String prefix) {
        //find left index
        int start = -1, end = -1;
        int left = 0;
        int right = classes.size() - 1;
        //search left most
        while (left <= right) {
            int mid = (left + right) / 2;
            String name = classes.get(mid).first;
            String midValue = name.substring(0, Math.min(name.length(), prefix.length()));
            if (midValue.compareTo(prefix) < 0) { //mid < key
                left = mid + 1;
            } else if (midValue.compareTo(prefix) > 0) {
                right = mid - 1;
            } else {
                //now mid = key, find upper and lower bound with linear search
                start = mid;
                end = mid;
                //exit here
                while (start >= 0 &&
                        classes.get(start)
                                .first
                                .substring(0, Math.min(classes.get(start).first.length(), prefix.length()))
                                .equals(prefix)) {
                    start--;
                }
                //exit here
                while (end < classes.size() &&
                        classes.get(end)
                                .first
                                .substring(0, Math.min(classes.get(end).first.length(), prefix.length()))
                                .equals(prefix)) {
                    end++;
                }
                start++;
                end--;
                break;
            }
        }
        if (end >= 0 && start >= 0 && end - start + 1 /*length*/ >= 1) {
            return classes.subList(start, end + 1);
        }
        return null;
    }

    @Override
    @NonNull
    public List<IClass> find(@NonNull String simpleNamePrefix,
                             @Nullable Filter<IClass> filter) {
        ArrayList<IClass> result = new ArrayList<>();

        //find with simple name
        List<Pair<String, IClass>> simple = binarySearch(mSimpleNames, simpleNamePrefix);
        if (simple != null) {
            for (Pair<String, IClass> c : simple) {
                if (filter != null) {
                    if (!filter.accept(c.second)) {
                        continue;
                    }
                }
                result.add(c.second);
            }
        }
        return result;
    }

}
