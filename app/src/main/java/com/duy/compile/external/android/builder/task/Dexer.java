package com.duy.compile.external.android.builder.task;

import android.support.annotation.NonNull;
import android.util.Log;

import com.duy.compile.external.android.builder.AndroidBuilder2;
import com.duy.compile.external.android.builder.util.Util;
import com.duy.dex.Dex;
import com.duy.dx.merge.CollisionPolicy;
import com.duy.dx.merge.DexMerger;
import com.duy.ide.DLog;
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
    public boolean run() throws Exception {
        Log.d(TAG, "convertToDexFormat() called with: projectFile = [" + project + "]");

        builder.stdout("Android dx");

        if (!dexLibs(project)) {
            return false;
        }
        if (!dexBuildClasses(project)) {
            return false;
        }
        if (!dexMerge(project)) {
            return false;
        }
        return true;
    }

    private boolean dexLibs(@NonNull JavaProjectFolder project) throws Exception {
        builder.stdout("Dex libs");

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
                builder.stdout("Lib " + jarLib.getName() + " has been dexed with cached file " + dexLib.getName());
                continue;
            }

            String[] args = {"--verbose",
                    "--no-strict",
                    "--output=" + dexLib.getAbsolutePath(), //output
                    jarLib.getAbsolutePath() //input
            };
            builder.stdout("Dexing lib " + dexLib.getAbsolutePath());
            int resultCode = com.duy.dx.command.dexer.Main.main(args);
            if (resultCode != 0) {
                return false;
            }
            builder.stdout("Dexed lib " + dexLib.getAbsolutePath());
        }

        builder.stdout("Dex libs completed");
        return true;
    }

    /**
     * Merge all classed has been build by {@link JavaCompiler} to a single file .dex
     */
    private boolean dexBuildClasses(@NonNull JavaProjectFolder project) throws IOException {
        builder.stdout("Merge build classes");

        if (DLog.DEBUG) DLog.d(TAG, "dexBuildClasses() called with: project = [" + project + "]");

        File buildClasseDir = project.getDirBuildClasses();
        String[] args = new String[]{
                "--verbose", "--no-strict",
                "--output=" + project.getDexFile().getAbsolutePath(), //output dex file
                buildClasseDir.getAbsolutePath() //input files
        };
        int resultCode = com.duy.dx.command.dexer.Main.main(args);
        builder.stdout("Merged build classes " + project.getDexFile().getName());
        return resultCode == 0;
    }

    private boolean dexMerge(@NonNull JavaProjectFolder projectFile) throws IOException {
        builder.stdout("Merge dex files");
        File[] dexedLibs = projectFile.getDirBuildDexedLibs().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(".dex");
            }
        });
        if (dexedLibs.length >= 1) {
            for (File dexedLib : dexedLibs) {
                Dex[] toBeMerge = {new Dex(projectFile.getDexFile()), new Dex(dexedLib)};
                DexMerger dexMerger = new DexMerger(toBeMerge, CollisionPolicy.FAIL);
                Dex merged = dexMerger.merge();
                merged.writeTo(projectFile.getDexFile());
            }
        }
        builder.stdout("Merge complete");
        return true;
    }

}
