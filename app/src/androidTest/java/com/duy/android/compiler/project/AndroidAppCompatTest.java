package com.duy.android.compiler.project;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.duy.android.compiler.builder.AndroidAppBuilder;
import com.duy.android.compiler.builder.model.BuildType;
import com.duy.android.compiler.env.Environment;

import org.junit.runner.RunWith;

import java.io.File;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;

@RunWith(AndroidJUnit4.class)
public class AndroidAppCompatTest {
    public void test() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        AndroidProjectManager projectManager = new AndroidProjectManager(context);
        File dir = (Environment.getSdkAppDir());
        AndroidAppProject project = projectManager.createNewProject(context, dir, "AndroidLibraryProjectExtractorTest"
                , "com.duy.example", "MainActivity", "activity_main.xml",
                "TestLibrary", true);

        AndroidAppBuilder builder = new AndroidAppBuilder(context, project, new DiagnosticListener() {
            @Override
            public void report(Diagnostic diagnostic) {
                System.out.println("diagnostic = " + diagnostic);
            }
        });
        builder.build(BuildType.DEBUG);
    }
}
