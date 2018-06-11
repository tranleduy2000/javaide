package com.duy.ide.javaide.editor.autocomplete.model;

import android.support.annotation.NonNull;
import android.text.Editable;

import com.duy.ide.editor.view.IEditAreaView;

import java.util.HashMap;

/**
 * @see com.duy.ide.javaide.editor.autocomplete.internal.CompletePackage
 */
public class PackageDescription extends JavaSuggestItemImpl {
    private String name;
    private PackageDescription parent;
    private HashMap<String, PackageDescription> child = new HashMap<>();

    private PackageDescription(String name, PackageDescription parent) {
        this.name = name;
        this.parent = parent;
    }

    public static PackageDescription root() {
        return new PackageDescription("", null);
    }

    @Override
    public void onSelectThis(@NonNull IEditAreaView editorView) {
        try {
            final int cursor = getEditor().getCursor();
            final int length = getIncomplete().length();
            final int start = cursor - length;

            Editable editable = editorView.getEditableText();

            if (isLeaf()) {
                //static access
                String text = name.replace("$", ".");
                // why not add semicolon (;) , in some case you need declared variable with full class
                // name, not import package
                editable.replace(start, cursor, text);
                editorView.setSelection(start + text.length());
            } else {
                String text = name + ".";
                editable.replace(start, cursor, text);
                editorView.setSelection(start + text.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public char getTypeHeader() {
        return 'p';
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
    public String getReturnType() {
        return null;
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

    @Override
    public String toString() {
        return "PackageDescription{" +
                "name='" + name + '\'' +
                ", parent=" + parent +
                ", child=" + child +
                ", lastUsed=" + lastUsed +
                "} " + super.toString();
    }
}
