package com.android.build.gradle.internal.tasks.multidex;

import com.android.build.gradle.internal.PostCompilationData;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.DefaultAndroidTask;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Custom Jar task that can merge other jars.
 * This ignores all non .class files since this is strictly to
 * handle code.
 */
public class JarMergingTask extends DefaultAndroidTask {
    private Collection<File> inputJars;
    private File inputDir;
    private File jarFile;

    private static void processJarFile(JarOutputStream jos, File file, byte[] buffer) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ZipInputStream zis = new ZipInputStream(fis);

        try {
            // loop on the entries of the jar file package and put them in the final jar
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // do not take directories or anything inside a potential META-INF folder.
                if (entry.isDirectory()) {
                    continue;
                }


                String name = entry.getName();
                if (!name.endsWith(".class")) {
                    continue;
                }


                JarEntry newEntry;

                // Preserve the STORED method of the input entry.
                if (entry.getMethod() == JarEntry.STORED) {
                    newEntry = new JarEntry(entry);
                } else {
                    // Create a new entry so that the compressed len is recomputed.
                    newEntry = new JarEntry(name);
                }


                // add the entry to the jar archive
                jos.putNextEntry(newEntry);

                // read the content of the entry from the input stream, and write it into the archive.
                int count;
                while ((count = zis.read(buffer)) != -1) {
                    jos.write(buffer, 0, count);
                }


                // close the entries for this file
                jos.closeEntry();
                zis.closeEntry();
            }

        } finally {
            zis.close();
        }


        fis.close();
    }

    @TaskAction
    public void createJar() throws IOException {
        jarFile.delete();

        FileOutputStream fos = new FileOutputStream(jarFile);
        JarOutputStream jos = new JarOutputStream(fos);

        final byte[] buffer = new byte[8192];
        Collection<File> jars = getInputJars();

        final Set<String> hashs = Sets.newHashSetWithExpectedSize(jars.size());

        for (File file : jars) {

            // TODO remove once we can properly add a library as a dependency of its test.
            String hash = Files.hash(file, Hashing.sha1()).toString();
            if (((HashSet<String>) hashs).contains(hash)) {
                continue;
            }

            ((HashSet<String>) hashs).add(hash);

            getLogger().info("INPUT: " + file);
            processJarFile(jos, file, buffer);
        }


        File _inputDir = getInputDir();
        if (_inputDir != null) {
            processFolder(jos, "", _inputDir, buffer);
        }


        jos.close();
    }

    private void processFolder(JarOutputStream jos, String path, File folder, byte[] buffer) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                // new entry
                jos.putNextEntry(new JarEntry(path + file.getName()));

                // put the file content
                FileInputStream fis = new FileInputStream(file);
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    jos.write(buffer, 0, count);
                }

                fis.close();

                // close the entry
                jos.closeEntry();
            } else if (file.isDirectory()) {
                processFolder(jos, path + file.getName() + "/", file, buffer);
            }

        }

    }

    public Collection<File> getInputJars() {
        return inputJars;
    }

    public void setInputJars(Collection<File> inputJars) {
        this.inputJars = inputJars;
    }

    public File getInputDir() {
        return inputDir;
    }

    public void setInputDir(File inputDir) {
        this.inputDir = inputDir;
    }

    public File getJarFile() {
        return jarFile;
    }

    public void setJarFile(File jarFile) {
        this.jarFile = jarFile;
    }

    public static class ConfigAction implements TaskConfigAction<JarMergingTask> {
        private VariantScope scope;
        private Callable<File> inputDir;
        private Callable<List<File>> inputLibraries;

        public ConfigAction(VariantScope scope, PostCompilationData pcData) {
            this.scope = scope;
            inputDir = pcData.getInputDirCallable();
            inputLibraries = pcData.getInputLibrariesCallable();
        }

        @Override
        public String getName() {
            return scope.getTaskName("packageAll", "ClassesForMultiDex");
        }

        @Override
        public Class<JarMergingTask> getType() {
            return JarMergingTask.class;
        }

        @Override
        public void execute(JarMergingTask jarMergingTask) {
            jarMergingTask.setVariantName(scope.getVariantConfiguration().getFullName());
            ConventionMappingHelper.map(jarMergingTask, "inputJars", inputLibraries);
            ConventionMappingHelper.map(jarMergingTask, "inputDir", inputDir);

            jarMergingTask.setJarFile(scope.getJarMergingOutputFile());
        }
    }
}
