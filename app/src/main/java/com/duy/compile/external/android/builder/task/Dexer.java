package com.duy.compile.external.android.builder.task;

import android.support.annotation.NonNull;
import android.util.Log;

import com.duy.compile.external.android.builder.AndroidBuilder2;
import com.duy.compile.external.android.builder.util.Util;
import com.duy.dex.Dex;
import com.duy.dx.merge.CollisionPolicy;
import com.duy.dx.merge.DexMerger;
import com.duy.ide.DLog;
import com.duy.ide.file.FileManager;
import com.duy.project.file.android.AndroidProject;
import com.duy.project.file.java.JavaProjectFolder;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class Dexer extends BuildTask {
    private static final String TAG = "Dexer";

    public Dexer(AndroidBuilder2 builder, AndroidProject project) {
        super(builder, project);
    }

    @Override
    public boolean run() throws Throwable {
        Log.d(TAG, "convertToDexFormat() called with: projectFile = [" + project + "]");

        builder.stdout("Android dx");

        if (!dexLibs(project)) {
            return false;
        }
        if (dexBuildClasses(project)) {
            return false;
        }
        dexMerge(project);

        return false;
    }

    private boolean dexLibs(@NonNull JavaProjectFolder project) throws Exception {
        if (DLog.DEBUG) DLog.d(TAG, "dexLibs() called with: project = [" + project + "]");
        File dirLibs = project.getDirLibs();
        File[] files = dirLibs.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".jar");
            }
        });
        for (File jarLib : files) {
            // compare hash of jar contents to name of dexed version
            String md5 = Util.getMD5Checksum(jarLib);

            File dexLib = new File(project.getDirBuildDexedLibs(), jarLib.getName().replace(".jar", "-" + md5 + ".dex"));
            if (dexLib.exists()) {
                continue;
            }
            String[] args = {"--verbose",
                    "--no-strict",
                    "--output=" + dexLib.getAbsolutePath(), //output
                    jarLib.getAbsolutePath() //input
            };
            int resultCode = com.duy.dx.command.dexer.Main.main(args);
            if (resultCode != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Merge all classed has been build by {@link JavaCompiler} to a single file .dex
     */
    private boolean dexBuildClasses(@NonNull JavaProjectFolder project) throws IOException {
        if (DLog.DEBUG) DLog.d(TAG, "dexBuildClasses() called with: project = [" + project + "]");

        String input = project.getDirBuildClasses().getPath();
        FileManager.ensureFileExist(new File(input));
        String[] args = new String[]{"--dex", "--verbose", "--no-strict",
                "--output=" + project.getDexedClassesFile().getPath(), //output dex file
                input}; //input file
        int resultCode = com.duy.dx.command.dexer.Main.main(args);
        return resultCode == 0;
    }

    private File dexMerge(@NonNull JavaProjectFolder projectFile) throws IOException {
        DLog.d(TAG, "dexMerge() called with: projectFile = [" + projectFile + "]");
        FileManager.ensureFileExist(projectFile.getDexedClassesFile());

        if (projectFile.getDirBuildDexedLibs().exists()) {
            File[] files = projectFile.getDirBuildDexedLibs().listFiles();
            if (files != null && files.length > 0) {
                for (File dexedLib : files) {
                    DexMerger dexMerger = new DexMerger(
                            new Dex[]{
                                    new Dex(projectFile.getDexedClassesFile()),
                                    new Dex(dexedLib)},
                            CollisionPolicy.FAIL);
                    Dex merged = dexMerger.merge();
                    merged.writeTo(projectFile.getDexedClassesFile());
                }
            }
        }
        return projectFile.getDexedClassesFile();
    }

}
