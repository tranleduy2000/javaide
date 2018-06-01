package com.duy.android.compiler.file;

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
 * AAR library
 */
public class AndroidLibraryProject extends JavaProject {

    private File xmlManifest;
    private File resDir;
    private File aidlDir;
    private File jniDir;
    private File assetsDir;
    private File classesJar;

    private File classR;

    public AndroidLibraryProject(File root, String libraryName) throws IOException, SAXException, StreamException, ParserConfigurationException {
        super(root, null, null);
        parseAndroidManifest();
        resDir = new File(dirRoot, "res");
        aidlDir = new File(dirRoot, "aidl");
        jniDir = new File(dirRoot, "jni");
        assetsDir = new File(dirRoot, "assets");
        classesJar = new File(dirBuildOutput, "classes.jar");
        classR = new File(dirRoot, getPackageName().replace(".", "/") + "/R.java");
    }

    public AndroidLibraryProject(File root, String mainClassName, String packageName, String projectName) {
        super(root, mainClassName, packageName);
    }


    private void parseAndroidManifest() throws IOException, SAXException, StreamException, ParserConfigurationException {
        xmlManifest = new File(dirRoot, "AndroidManifest.xml");
        if (!xmlManifest.exists()) {
            throw new FileNotFoundException("AndroidManifest.xml");
        }

        ManifestData manifestData = AndroidManifestParser.parse(new FileInputStream(xmlManifest));
        String aPackage = manifestData.getPackage();
        setPackageName(aPackage);
    }

}
