package com.duy.android.compiler.project;


import android.support.annotation.NonNull;

import java.io.File;

public interface IProjectManager<T extends JavaProject> {
    @NonNull
    T loadProject(File rootDir, boolean tryToImport) throws Exception;
}
