package com.duy.ide.autocomplete.autocomplete;

import android.util.Log;

import com.android.annotations.Nullable;
import com.duy.ide.autocomplete.dex.JavaClassReader;
import com.duy.ide.autocomplete.model.PackageDescription;

import java.util.ArrayList;

/**
 * Created by Duy on 20-Jul-17.
 */

public class AutoCompletePackage {
    private static final String TAG = "AutoCompletePackage";
    private PackageDescription root;

    public AutoCompletePackage() {
        root = PackageDescription.root();
    }

    public void init(JavaClassReader classReader) {
        Log.d(TAG, "init() called with: classReader = [" + classReader + "]");

        ArrayList<Class> classes = classReader.getAllClasses();
        for (Class aClass : classes) {
            root.put(aClass.getName());
        }
    }

    @Nullable
    public PackageDescription trace(String child) {
        return this.root.get(child);
    }

}
