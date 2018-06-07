package com.android.build.gradle.internal.tasks.multidex;

import com.android.build.gradle.internal.PostCompilationData;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.DefaultAndroidTask;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.android.builder.model.AndroidProject.FD_INTERMEDIATES;

/**
 * Take a list of classes for the main dex (that was computed before obfuscation),
 * a proguard-generated mapping file and create a new list of classes with the new
 * obfuscated names.
 */
public class RetraceMainDexList extends DefaultAndroidTask {
    private File mainDexListFile;
    private File outputFile;
    private File mappingFile;

    public static Map<String, String> createDict(List<String> lines) {
        Map<String, String> map = Maps.newHashMap();

        for (String line : lines) {
            if (line.startsWith(" ")) {
                continue;
            }


            int pos = line.indexOf(" -> ");
            if (pos == -1) {
                throw new RuntimeException("unable to read mapping file.");
            }


            String fullName = line.substring(0, pos);
            String obfuscatedName = line.substring(pos + 4, line.length() - 1);

            ((HashMap<String, String>) map).put(obfuscatedName.replace(".", "/") + ".class", fullName.replace(".", "/") + ".class");
        }


        return map;
    }

    /**
     * Gradle doesn't really handle optional inputs as being a file that
     * doesn't exist. Optional means the task field is null. So we do some
     * custom logic to return null if the file doesn't exist since we cannot
     * know ahead of time without parsing the proguard config rule files.
     */
    @Optional
    public File getMappingFileInput() {
        File file = getMappingFile();
        if (file != null && file.isFile()) {
            return file;
        }


        return null;
    }

    @TaskAction
    public void retrace() throws IOException {

        File mapping = getMappingFile();
        // if there is no mapping file or if it doesn't exist, then we just copy from the main
        // dex list ot the output.
        if (mapping == null || !mapping.isFile()) {
            Files.copy(getMainDexListFile(), getOutputFile());
            return;

        }


        // load the main class names
        List<String> classes = Files.readLines(getMainDexListFile(), Charsets.UTF_8);

        // load the mapping file and create a dictionary
        List<String> mappingLines = Files.readLines(mapping, Charsets.UTF_8);
        Map<String, String> map = createDict(mappingLines);

        // create the deobfuscated class list. This is a set to detect dups
        // You can have the same class coming from the non-obfuscated list and from
        // the shrinked jar that have different names until we remap them.
        Set<String> deobfuscatedClasses = Sets.newHashSetWithExpectedSize(classes.size());

        for (String clazz : classes) {
            String fullName = map.get(clazz);

            ((HashSet<String>) deobfuscatedClasses).add(fullName != null ? fullName : clazz);
        }


        String fileContent = Joiner.on(System.getProperty("line.separator")).join(deobfuscatedClasses);
        Files.write(fileContent, getOutputFile(), Charsets.UTF_8);
    }

    public File getMainDexListFile() {
        return mainDexListFile;
    }

    public void setMainDexListFile(File mainDexListFile) {
        this.mainDexListFile = mainDexListFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public File getMappingFile() {
        return mappingFile;
    }

    public void setMappingFile(File mappingFile) {
        this.mappingFile = mappingFile;
    }

    public static class ConfigAction implements TaskConfigAction<RetraceMainDexList> {
        private VariantScope scope;
        private PostCompilationData pcData;

        public ConfigAction(VariantScope scope, PostCompilationData pcData) {
            this.scope = scope;
            this.pcData = pcData;
        }

        @Override
        public String getName() {
            return scope.getTaskName("retrace", "MainDexClassList");
        }

        @Override
        public Class<RetraceMainDexList> getType() {
            return ((Class<RetraceMainDexList>) (RetraceMainDexList.class));
        }

        @Override
        public void execute(RetraceMainDexList retraceTask) {
            retraceTask.setVariantName(scope.getVariantConfiguration().getFullName());
            retraceTask.setMainDexListFile(scope.getMainDexListFile());
            retraceTask.setMappingFile(scope.getVariantData().getMappingFile());
            retraceTask.setOutputFile(new File(String.valueOf(scope.getGlobalScope().getBuildDir()) +
                    "/" + FD_INTERMEDIATES + "/multi-dex/" +
                    scope.getVariantConfiguration().getDirName() +
                    "/maindexlist_deobfuscated.txt"));

        }

        public VariantScope getScope() {
            return scope;
        }

        public void setScope(VariantScope scope) {
            this.scope = scope;
        }

        public PostCompilationData getPcData() {
            return pcData;
        }

        public void setPcData(PostCompilationData pcData) {
            this.pcData = pcData;
        }
    }
}
