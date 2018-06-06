package com.android.build.gradle.tasks;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.BaseTask;
import com.android.builder.compiling.ResValueGenerator;
import com.android.builder.core.VariantConfiguration;
import com.android.builder.model.ClassField;
import com.android.utils.FileUtils;
import com.google.common.collect.Lists;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import groovy.lang.Closure;

@ParallelizableTask
public class GenerateResValues extends BaseTask {
    @OutputDirectory
    private File resOutputDir;
    private List<Object> items;

    @Input
    public List<String> getItemValues() {
        List<Object> resolvedItems = getItems();
        List<String> list = Lists.newArrayListWithCapacity(resolvedItems.size() * 3);

        for (Object object : resolvedItems) {
            if (object instanceof String) {
                list.add((String) object);
            } else if (object instanceof ClassField) {
                ClassField field = (ClassField) object;
                list.add(field.getType());
                list.add(field.getName());
                list.add(field.getValue());
            }

        }


        return list;
    }

    @TaskAction
    public void generate() throws IOException, ParserConfigurationException {
        File folder = getResOutputDir();
        List<Object> resolvedItems = getItems();

        if (resolvedItems.isEmpty()) {
            FileUtils.deleteFolder(folder);
        } else {
            ResValueGenerator generator = new ResValueGenerator(folder);
            generator.addItems(getItems());

            generator.generate();
        }

    }

    public File getResOutputDir() {
        return resOutputDir;
    }

    public void setResOutputDir(File resOutputDir) {
        this.resOutputDir = resOutputDir;
    }

    public List<Object> getItems() {
        return items;
    }

    public void setItems(List<Object> items) {
        this.items = items;
    }

    public static class ConfigAction implements TaskConfigAction<GenerateResValues> {
        @NonNull
        private VariantScope scope;

        public ConfigAction(@NonNull VariantScope scope) {
            this.scope = scope;
        }

        @Override
        public String getName() {
            return scope.getTaskName("generate", "ResValues");
        }

        @Override
        public Class getType() {
            return GenerateResValues.class;
        }

        @Override
        public void execute(GenerateResValues generateResValuesTask) {
            scope.getVariantData().generateResValuesTask = generateResValuesTask;

            final VariantConfiguration variantConfiguration = scope.getVariantData().getVariantConfiguration();

            generateResValuesTask.setAndroidBuilder(scope.getGlobalScope().getAndroidBuilder());
            generateResValuesTask.setVariantName(variantConfiguration.getFullName());

            ConventionMappingHelper.map(generateResValuesTask, "items", new Closure<List<Object>>(this, this) {
                public List<Object> doCall(Object it) {
                    return variantConfiguration.getResValues();
                }

                public List<Object> doCall() {
                    return doCall(null);
                }

            });

            ConventionMappingHelper.map(generateResValuesTask, "resOutputDir", new Closure<File>(this, this) {
                public File doCall(Object it) {
                    return new File(scope.getGlobalScope().getGeneratedDir(), "res/resValues/" + scope.getVariantData().getVariantConfiguration().getDirName());
                }

                public File doCall() {
                    return doCall(null);
                }

            });
        }

        public VariantScope getScope() {
            return scope;
        }

        public void setScope(VariantScope scope) {
            this.scope = scope;
        }
    }
}
