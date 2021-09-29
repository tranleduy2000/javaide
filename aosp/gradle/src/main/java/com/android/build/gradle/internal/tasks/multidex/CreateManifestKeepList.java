package com.android.build.gradle.internal.tasks.multidex;

import com.android.build.gradle.internal.PostCompilationData;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.DefaultAndroidTask;
import com.android.build.gradle.internal.variant.BaseVariantOutputData;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.gradle.api.tasks.TaskAction;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import groovy.lang.Closure;

public class CreateManifestKeepList extends DefaultAndroidTask {
    private static String DEFAULT_KEEP_SPEC = "{ <init>(); }";
    private static Map<String, String> KEEP_SPECS;
    private File manifest;
    private File outputFile;
    private File proguardFile;
    private boolean filter;

    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(6);
        map.put("application", "{\n    <init>();\n    void attachBaseContext(android.content.Context);\n}");
        map.put("activity", DEFAULT_KEEP_SPEC);
        map.put("service", DEFAULT_KEEP_SPEC);
        map.put("receiver", DEFAULT_KEEP_SPEC);
        map.put("provider", DEFAULT_KEEP_SPEC);
        map.put("instrumentation", DEFAULT_KEEP_SPEC);
        KEEP_SPECS = map;
    }

    @TaskAction
    public void generateKeepListFromManifest() throws ParserConfigurationException, SAXException, IOException {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

        Writer out = new BufferedWriter(new FileWriter(getOutputFile()));
        try {
            parser.parse(getManifest(), new ManifestHandler(out));

            // add a couple of rules that cannot be easily parsed from the manifest.
            out.write("-keep public class * extends android.app.backup.BackupAgent {\n    <init>();\n}\n-keep public class * extends java.lang.annotation.Annotation {\n    *;\n}\n");

            if (proguardFile != null) {
                out.write(Files.toString(proguardFile, Charsets.UTF_8));
            }

        } finally {
            out.close();
        }

    }

    public File getManifest() {
        return manifest;
    }

    public void setManifest(File manifest) {
        this.manifest = manifest;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public File getProguardFile() {
        return proguardFile;
    }

    public void setProguardFile(File proguardFile) {
        this.proguardFile = proguardFile;
    }

    public boolean getFilter() {
        return filter;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    public static class ConfigAction implements TaskConfigAction<CreateManifestKeepList> {
        private VariantScope scope;
        private PostCompilationData pcData;

        public ConfigAction(VariantScope scope, PostCompilationData pcData) {
            this.scope = scope;
            this.pcData = pcData;
        }

        @Override
        public String getName() {
            return scope.getTaskName("collect", "MultiDexComponents");
        }

        @Override
        public Class<CreateManifestKeepList> getType() {
            return CreateManifestKeepList.class;
        }

        @Override
        public void execute(CreateManifestKeepList manifestKeepListTask) {
            manifestKeepListTask.setVariantName(scope.getVariantConfiguration().getFullName());

            // since all the output have the same manifest, besides the versionCode,
            // we can take any of the output and use that.
            final BaseVariantOutputData output = scope.getVariantData().getOutputs().get(0);
            ConventionMappingHelper.map(manifestKeepListTask, "manifest", new Closure<File>(this, this) {
                public File doCall(Object it) {
                    return output.getScope().getManifestOutputFile();
                }

                public File doCall() {
                    return doCall(null);
                }

            });

            manifestKeepListTask.setProguardFile(scope.getVariantConfiguration().getMultiDexKeepProguard());
            manifestKeepListTask.setOutputFile(scope.getManifestKeepListFile());

            //variant.ext.collectMultiDexComponents = manifestKeepListTask
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

    private class ManifestHandler extends DefaultHandler {
        private Writer out;

        public ManifestHandler(Writer out) {
            this.out = out;
        }

        @Override
        public void startElement(String uri, String localName, String qName, final Attributes attr) {
            String keepSpec = CreateManifestKeepList.KEEP_SPECS.get(qName);
            if (Boolean.parseBoolean(keepSpec)) {

                boolean keepIt = true;
                if (CreateManifestKeepList.this.getFilter()) {
                    // for ease of use, turn 'attr' into a simple map
                    Map<String, String> attrMap = new LinkedHashMap<>();
                    for (int i = 0; i < attr.getLength(); i++) {
                        attrMap.put(attr.getQName(i), attr.getValue(i));
                    }

                    keepIt = CreateManifestKeepList.this.getFilter();
                }


                if (keepIt) {
                    try {
                        out.write("-keep class " + attr.getValue("android:name") + " " + keepSpec + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        }
    }
}
