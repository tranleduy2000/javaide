package com.duy.android.compiler.io;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.duy.android.compiler.builder.AndroidAppBuilder;
import com.duy.android.compiler.builder.model.BuildType;
import com.duy.android.compiler.env.Environment;
import com.duy.android.compiler.library.AndroidLibraryExtractor;
import com.duy.android.compiler.project.AndroidApplicationProject;
import com.duy.android.compiler.project.AndroidProjectManager;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AndroidLibraryProjectExtractorTest {

    @Test
    public void testExtractAar() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();

        String[] list = context.getAssets().list("");
        System.out.println("list = " + Arrays.toString(list));

        File file = new File(context.getCacheDir(), "appcompat-v7-25.2.0.aar");
        FileOutputStream output = new FileOutputStream(file);
        IOUtils.copy(context.getAssets().open("appcompat-v7-25.2.0.aar"), output);
        output.close();

        String libraryName = "appcompat-v7-25.2.0";
        AndroidLibraryExtractor extractor = new AndroidLibraryExtractor(context);
        boolean result = extractor.extract(file, libraryName);

        assertTrue(result);

        File libDir = new File(Environment.getSdCardLibraryCachedDir(context), libraryName);
    }

    @Test
    public void testBuild() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        AndroidProjectManager projectManager = new AndroidProjectManager(context);
        File dir = (Environment.getSdkAppDir());
        AndroidApplicationProject project = projectManager.createNewProject(context, dir, "AndroidLibraryProjectExtractorTest"
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

    @Test
    public void testReadPom() throws IOException {
        Context context = InstrumentationRegistry.getTargetContext();

        File pomFile = new File(Environment.getSdkAppDir(), "appcompat-v7-27.1.1.pom");
        FileOutputStream output = new FileOutputStream(pomFile);
        IOUtils.copy(context.getAssets().open("libs/appcompat-v7-27.1.1.pom"), output);
        output.close();

        Model model = null;
        FileReader reader = null;
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        try {
            reader = new FileReader(pomFile);
            model = mavenReader.read(reader);
            model.setPomFile(pomFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        MavenProject mavenProject = new MavenProject(model);
        System.out.println("mavenProject = " + mavenProject);
    }
}
