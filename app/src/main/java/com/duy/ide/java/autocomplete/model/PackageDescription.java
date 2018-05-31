package com.duy.ide.java.autocomplete.model;

import java.util.HashMap;

public class PackageDescription extends DescriptionImpl {
    private String name;
    private PackageDescription parent;
    private HashMap<String, PackageDescription> child = new HashMap<>();

    public PackageDescription(String name, PackageDescription parent) {
        this.name = name;
        this.parent = parent;
    }

    public static PackageDescription root() {
        return new PackageDescription("", null);
    }

    @Override
    public String toString() {
        return name;
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
        return null;
    }

    @Override
    public String getSnippet() {
        return name + (isLeaf() ? ";" : ".");
    }


    public HashMap<String, PackageDescription> getChild() {
        return child;
    }

    private boolean isRoot() {
        return name.isEmpty() || parent == null;
    }

    private boolean isLeaf() {
        return child.isEmpty();
    }

    public PackageDescription get(String key) {
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
            this.child.put(pkg, new PackageDescription(pkg, this));
        }
    }

    public PackageDescription remove(String child) {
        if (child.contains(".")) {
            return this.child.remove(child.substring(0, child.indexOf(".")));
        } else {
            return this.child.remove(child);
        }
    }
}
