package com.duy.android.compiler.project;

import com.android.annotations.Nullable;
import com.android.ide.common.xml.AndroidManifestParser;
import com.android.ide.common.xml.ManifestData;
import com.android.io.StreamException;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * https://developer.android.com/studio/projects/android-library#aar-contents
 * <p>
 * AAR library
 */
public class AndroidLibraryProject extends JavaProject {

    private File xmlManifest;
    private File resDir;
    private File aidlDir;
    private File jniDir;
    private File assetsDir;
    @Nullable
    private File classesJar;
    private File classR;

    public AndroidLibraryProject(File libraryDir, String libraryName) throws IOException, SAXException, StreamException, ParserConfigurationException {
        super(libraryDir, null, null);
        parseAndroidManifest();

        resDir = new File(dirRoot, "res");
        aidlDir = new File(dirRoot, "aidl");
        jniDir = new File(dirRoot, "jni");
        assetsDir = new File(dirRoot, "assets");

        if (new File(dirRoot, "classes.jar").exists()) {
            classesJar = new File(dirRoot, "classes.jar");
        }
        classR = new File(dirRoot, getPackageName().replace(".", "/") + "/R.java");
    }

    public File getXmlManifest() {
        return xmlManifest;
    }

    public File getResDir() {
        return resDir;
    }

    public File getAidlDir() {
        return aidlDir;
    }

    public File getJniDir() {
        return jniDir;
    }

    public File getAssetsDir() {
        return assetsDir;
    }

    @Nullable
    public File getClassesJar() {
        return classesJar;
    }

    public File getClassR() {
        return classR;
    }

    private void parseAndroidManifest() throws IOException, SAXException, StreamException, ParserConfigurationException {
        xmlManifest = new File(dirRoot, "AndroidManifest.xml");
        if (!xmlManifest.exists()) {
            throw new FileNotFoundException(xmlManifest + " not exist");
        }

        ManifestData manifestData = AndroidManifestParser.parse(new FileInputStream(xmlManifest));
        String aPackage = manifestData.getPackage();
        setPackageName(aPackage);
    }

}
