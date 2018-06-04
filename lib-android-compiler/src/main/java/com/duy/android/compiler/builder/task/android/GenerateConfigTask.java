package com.duy.android.compiler.builder.task.android;

import com.android.builder.compiling.BuildConfigGenerator;
import com.android.builder.model.ClassField;
import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.task.Task;
import com.duy.android.compiler.project.AndroidAppProject;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

public class GenerateConfigTask extends Task<AndroidAppProject> {
    List<Object> items;
    private boolean debuggable = true;
    private int versionCode;

    public GenerateConfigTask(IBuilder<? extends AndroidAppProject> builder) {
        super(builder);
    }

    @Override
    public String getTaskName() {
        return "Generate BuildConfig.java";
    }

    @Override
    public boolean run() throws Exception {
        String packageName = project.getPackageName();
        File genFolder = project.getDirGeneratedSource();
        BuildConfigGenerator generator = new BuildConfigGenerator(genFolder, packageName);
        // Hack (see IDEA-100046): We want to avoid reporting "condition is always true"
        // from the data flow inspection, so use a non-constant value. However, that defeats
        // the purpose of this flag (when not in debug mode, if (BuildConfig.DEBUG && ...) will
        // be completely removed by the compiler), so as a hack we do it only for the case
        // where debug is true, which is the most likely scenario while the user is looking
        // at source code.
        //map.put(PH_DEBUG, Boolean.toString(mDebug));
        generator.addField("boolean", "DEBUG",
                getDebuggable() ? "Boolean.parseBoolean(\"true\")" : "false")
                .addField("String", "APPLICATION_ID", "\"${getAppPackageName()}\"")
                .addField("String", "BUILD_TYPE", "\"${getBuildTypeName()}\"")
                .addField("String", "FLAVOR", "\"${getFlavorName()}\"")
                .addField("int", "VERSION_CODE", Integer.toString(getVersionCode()))
                .addField("String", "VERSION_NAME", "\"${Strings.nullToEmpty(getVersionName())}\"")
                .addItems(getItems());

        generator.generate();
        return true;
    }

    public List<Object> getItems() {
        return items;
    }

    List<String> getItemValues() {
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

    public int getVersionCode() {
        return versionCode;
    }

    public boolean getDebuggable() {
        return debuggable;
    }

}
