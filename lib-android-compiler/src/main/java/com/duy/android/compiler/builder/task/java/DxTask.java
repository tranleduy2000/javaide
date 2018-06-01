package com.duy.android.compiler.builder.task.java;

import android.support.annotation.NonNull;
import android.util.Log;

import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.util.MD5Hash;
import com.duy.android.compiler.project.JavaProject;
import com.duy.android.compiler.builder.task.ABuildTask;
import com.duy.dex.Dex;
import com.duy.dx.merge.CollisionPolicy;
import com.duy.dx.merge.DexMerger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class DxTask extends ABuildTask<JavaProject> {
    private static final String TAG = "Dexer";

    public DxTask(IBuilder<? extends JavaProject> builder) {
        super(builder);
    }

    @Override
    public String getTaskName() {
        return "Dx";
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

    private boolean dexLibs(@NonNull JavaProject project) throws Exception {
        builder.stdout("Dex libs");

        File dirLibs = project.getDirLibs();
        File[] files = dirLibs.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".jar");
            }
        });
        for (File jarLib : files) {
            // compare hash of jar contents to name of dexed version
            String md5 = MD5Hash.getMD5Checksum(jarLib);

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
     * Merge all classed has been build by {@link CompileJavaTask} to a single file .dex
     */
    private boolean dexBuildClasses(@NonNull JavaProject project) throws IOException {
        builder.stdout("Merge build classes");

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

    private boolean dexMerge(@NonNull JavaProject projectFile) throws IOException {
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
        builder.stdout("Merge all dexed files completed");
        return true;
    }

}
