package com.duy.android.compiler.library;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileReader;

public class AndroidLibraryManager {

    public AndroidLibrary getLibrary(File pomFile) {
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

        return null;
    }
}
