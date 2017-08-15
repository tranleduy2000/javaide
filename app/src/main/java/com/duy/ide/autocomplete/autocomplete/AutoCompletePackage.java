package com.duy.ide.autocomplete.autocomplete;

import com.duy.ide.autocomplete.dex.JavaClassReader;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Duy on 20-Jul-17.
 */

public class AutoCompletePackage {
    private PackageModel root;

    public AutoCompletePackage() {
        root = PackageModel.root();
    }

    public void init(JavaClassReader classReader) {
        ArrayList<Class> classes = classReader.getAllClasses();
        for (Class aClass : classes) {
            root.put(aClass.getPackage().getName());
        }
    }

    public PackageModel trace(String child) {
        return this.root.get(child);
    }

    public static class PackageModel {
        private String name;
        private HashMap<String, PackageModel> child = new HashMap<>();

        public PackageModel(String name) {
            this.name = name;
        }

        public static PackageModel root() {
            return new PackageModel("");
        }

        public String getName() {
            return name;
        }

        public HashMap<String, PackageModel> getChild() {
            return child;
        }

        private boolean isRoot() {
            return name.isEmpty();
        }

        private boolean isLeaf() {
            return child.isEmpty();
        }

        public PackageModel get(String key) {
            if (!key.contains(".")) {
                return child.get(key);
            } else {
                return get(key.substring(0, key.indexOf(".")))
                        .get(key.substring(key.indexOf(".") + 1));
            }
        }

        public void put(String pkg) {
            if (pkg.contains(".")) {
                String first = pkg.substring(0, pkg.indexOf("."));
                if (get(first) == null) {
                    put(first);
                }
                get(first).put(pkg.substring(pkg.indexOf(".") + 1));
            } else {
                this.child.put(pkg, new PackageModel(pkg));
            }
        }
    }
}
