package com.duy.android.compiler.builder;

import android.support.annotation.NonNull;

import com.android.annotations.Nullable;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.internal.dsl.BuildType;
import com.android.build.gradle.internal.dsl.ProductFlavor;
import com.android.utils.ILogger;
import com.android.utils.StdLogger;
import com.duy.android.compiler.builder.parser.FastGradleParser;
import com.google.common.collect.Lists;

import junit.framework.TestCase;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.internal.reflect.DirectInstantiator;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.testfixtures.ProjectBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AppBuilderTest extends TestCase {

    private final FastGradleParser mGradleParser = new FastGradleParser();
    private final ILogger mLogger = new StdLogger(StdLogger.Level.VERBOSE);

    public void test1() throws IOException {
        final File appGradleFile = new File(getTestProjectDir(), Project.DEFAULT_BUILD_FILE);

        final Instantiator instantiator = DirectInstantiator.INSTANCE;

        final Project project = ProjectBuilder.builder().withProjectDir(getTestProjectDir()).build();
        AppPlugin androidApp = new AppPlugin(instantiator, null);

        //create android task
        androidApp.apply(project);

        //android {}
        AppExtension android = (AppExtension) androidApp.getExtension();

        //android { compileSdkVersion }, only support 27
        android.setCompileSdkVersion(mGradleParser.getCompileSdkVersion(appGradleFile));
        //android { buildToolsVersion }, no need
        android.setBuildToolsVersion(mGradleParser.getBuildToolsVersion(appGradleFile));

        //apply default config
        android.defaultConfig(new Action<ProductFlavor>() {
            @Override
            public void execute(@NonNull ProductFlavor productFlavor) {
                try {
                    mGradleParser.parseDefaultProductFavor(appGradleFile, productFlavor);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //android { buildTypes { debug, release } }
        android.buildTypes(new Action<NamedDomainObjectContainer<BuildType>>() {
            @Override
            public void execute(@NonNull NamedDomainObjectContainer<BuildType> buildTypes) {
                BuildType debug = new BuildType("debug", project, instantiator, getLogger());
                debug.setMinifyEnabled(false);
                debug.setProguardFiles(Lists.newArrayList(new File(getTestProjectDir(), "proguard-rules.pro")));

                buildTypes.add(debug);

                BuildType release = new BuildType("release", project, instantiator, getLogger());
                release.setMinifyEnabled(false);
                release.setProguardFiles(Lists.newArrayList(new File(getTestProjectDir(), "proguard-rules.pro")));
                ;
                buildTypes.add(release);
            }
        });

        project.getDependencies().add("", "com.android.support:support-v4:27.1.1");
        project.getDependencies().add("", "com.android.support:appcompat-v7:27.1.1");

    }

    @Nullable
    private Logger getLogger() {
        return null;
    }

    @NonNull
    private File getTestProjectDir() {
        return new File("D:\\github\\javaide\\aosp\\gradle\\testdata\\HelloWorld");
    }
}
