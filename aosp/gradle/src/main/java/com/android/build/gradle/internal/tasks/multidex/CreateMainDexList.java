package com.android.build.gradle.internal.tasks.multidex;

import android.support.annotation.NonNull;

import com.android.build.gradle.internal.PostCompilationData;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.BaseTask;
import com.android.ide.common.process.ProcessException;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;

import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Task to create the main (non-obfuscated) list of classes to keep.
 * It uses a jar containing all the classes, as well as a shrinked jar file created by proguard.
 * <p>
 * Optionally, it can use a manual list of classes/jars to keep.
 */
public class CreateMainDexList extends BaseTask {
    private File allClassesJarFile;
    private File componentsJarFile;
    private File outputFile;
    private File includeInMainDexJarFile;
    private File mainDexListFile;

    public File getDxJar() {
        return getBuilder().getDxJar();
    }

    @TaskAction
    public void output() throws IOException, ProcessException {
        if (getAllClassesJarFile() == null) {
            throw new NullPointerException("No input file");
        }


        // manifest components plus immediate dependencies must be in the main dex.
        File _allClassesJarFile = getAllClassesJarFile();
        Set<String> mainDexClasses = callDx(_allClassesJarFile, getComponentsJarFile());

        // add additional classes specified via a jar file.
        File _includeInMainDexJarFile = getIncludeInMainDexJarFile();
        if (_includeInMainDexJarFile != null) {
            // proguard shrinking is overly aggressive when it comes to removing
            // interface classes: even if an interface is implemented by a concrete
            // class, if no code actually references the interface class directly
            // (i.e., code always references the concrete class), proguard will
            // remove the interface class when shrinking.  This is problematic,
            // as the runtime verifier still needs the interface class to be
            // present, or the concrete class won't be valid.  Use a
            // ClassReferenceListBuilder here (only) to pull in these missing
            // interface classes.  Note that doing so brings in other unnecessary
            // stuff, too; next time we're low on main dex space, revisit this!
            mainDexClasses.addAll(callDx(_allClassesJarFile, _includeInMainDexJarFile));
        }


        if (mainDexListFile != null) {
            Set<String> mainDexList = new HashSet<String>(Files.readLines(mainDexListFile, Charsets.UTF_8));
            mainDexClasses.addAll(mainDexList);
        }


        String fileContent = Joiner.on(System.getProperty("line.separator")).join(mainDexClasses);

        Files.write(fileContent, getOutputFile(), Charsets.UTF_8);
    }

    private Set<String> callDx(File allClassesJarFile, File jarOfRoots) throws ProcessException {
        return getBuilder().createMainDexList(allClassesJarFile, jarOfRoots);
    }

    public File getAllClassesJarFile() {
        return allClassesJarFile;
    }

    public void setAllClassesJarFile(File allClassesJarFile) {
        this.allClassesJarFile = allClassesJarFile;
    }

    public File getComponentsJarFile() {
        return componentsJarFile;
    }

    public void setComponentsJarFile(File componentsJarFile) {
        this.componentsJarFile = componentsJarFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public File getIncludeInMainDexJarFile() {
        return includeInMainDexJarFile;
    }

    public void setIncludeInMainDexJarFile(File includeInMainDexJarFile) {
        this.includeInMainDexJarFile = includeInMainDexJarFile;
    }

    public File getMainDexListFile() {
        return mainDexListFile;
    }

    public void setMainDexListFile(File mainDexListFile) {
        this.mainDexListFile = mainDexListFile;
    }

    public static class ConfigAction implements TaskConfigAction<CreateMainDexList> {
        private VariantScope scope;
        private Callable<List<File>> inputFiles;

        public ConfigAction(VariantScope scope, PostCompilationData pcData) {
            this.scope = scope;
            inputFiles = pcData.getInputFilesCallable();
        }

        @Override
        public String getName() {
            return scope.getTaskName("create", "MainDexClassList");
        }

        @Override
        public Class<CreateMainDexList> getType() {
            return CreateMainDexList.class;
        }

        @Override
        public void execute(@NonNull CreateMainDexList createMainDexList) {
            createMainDexList.setAndroidBuilder(scope.getGlobalScope().getAndroidBuilder());
            createMainDexList.setVariantName(scope.getVariantConfiguration().getFullName());

            Callable<List<File>> files = inputFiles;
            try {
                createMainDexList.allClassesJarFile = files.call().get(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            createMainDexList.setComponentsJarFile(scope.getProguardComponentsJarFile());
            createMainDexList.setMainDexListFile(scope.getManifestKeepListFile());
            createMainDexList.setOutputFile(scope.getMainDexListFile());
        }

        public VariantScope getScope() {
            return scope;
        }

        public void setScope(VariantScope scope) {
            this.scope = scope;
        }
    }
}
