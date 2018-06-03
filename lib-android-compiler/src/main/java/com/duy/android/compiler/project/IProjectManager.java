package com.duy.android.compiler.project;

import java.io.File;
import java.io.IOException;

public interface IProjectManager<T extends JavaProject> {
    T loadProject(File rootDir, boolean tryToImport) throws IOException;
}
