package com.duy.android2.builder.core;

import com.android.annotations.NonNull;
import com.android.builder.compiling.DependencyFileProcessor;
import com.android.builder.internal.incremental.DependencyData;

import java.io.File;

public class JAndroidBuilder {

    private static final DependencyFileProcessor sNoOpDependencyFileProcessor = new DependencyFileProcessor() {
        @Override
        public DependencyData processFile(@NonNull File dependencyFile) {
            return null;
        }
    };


    public JAndroidBuilder() {

    }
}
