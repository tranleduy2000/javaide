/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sdklib.internal.export;

import com.android.sdklib.SdkConstants;
import com.android.sdklib.io.FileWrapper;
import com.android.sdklib.io.IAbstractFile;
import com.android.sdklib.io.StreamException;
import com.android.sdklib.xml.AndroidManifestParser;
import com.android.sdklib.xml.ManifestData;
import com.android.sdklib.xml.ManifestData.SupportsScreens;

import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Helper to export multiple APKs from 1 or or more projects.
 * <strong>This class is not meant to be accessed from multiple threads</strong>
 */
public class MultiApkExportHelper {

    private final static String PROP_VERSIONCODE = "versionCode";
    private final static String PROP_PACKAGE = "package";

    private final String mExportProjectRoot;
    private final String mAppPackage;
    private final int mVersionCode;
    private final Target mTarget;

    private ArrayList<ProjectConfig> mProjectList;
    private ArrayList<ApkData> mApkDataList;

    final static int MAX_MINOR = 100;
    final static int MAX_BUILDINFO = 100;
    final static int OFFSET_BUILD_INFO = MAX_MINOR;
    final static int OFFSET_VERSION_CODE = OFFSET_BUILD_INFO * MAX_BUILDINFO;

    private final static String FILE_CONFIG = "projects.config";
    private final static String FILE_MINOR_CODE = "minor.codes";
    private final static String FOLDER_LOG = "logs";
    private final PrintStream mStdio;

    public static final class ExportException extends Exception {
        private static final long serialVersionUID = 1L;

        public ExportException(String message) {
            super(message);
        }

        public ExportException(String format, Object... args) {
            super(String.format(format, args));
        }

        public ExportException(Throwable cause, String format, Object... args) {
            super(String.format(format, args), cause);
        }

        public ExportException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static enum Target {
        RELEASE("release"), CLEAN("clean");

        private final String mName;

        Target(String name) {
            mName = name;
        }

        public String getTarget() {
            return mName;
        }

        public static Target getTarget(String value) {
            for (Target t : values()) {
                if (t.mName.equals(value)) {
                    return t;
                }

            }

            return null;
        }
    }

    public MultiApkExportHelper(String exportProjectRoot, String appPackage,
            int versionCode, Target target, PrintStream stdio) {
        mExportProjectRoot = exportProjectRoot;
        mAppPackage = appPackage;
        mVersionCode = versionCode;
        mTarget = target;
        mStdio = stdio;
    }

    public List<ApkData> getApkData(String projectList) throws ExportException {
        if (mTarget != Target.RELEASE) {
            throw new IllegalArgumentException("getApkData must only be called for Target.RELEASE");
        }

        // get the list of apk to export and their configuration.
        List<ProjectConfig> projects = getProjects(projectList);

        // look to see if there's a config file from a previous export
        File configProp = new File(mExportProjectRoot, FILE_CONFIG);
        if (configProp.isFile()) {
            compareProjectsToConfigFile(projects, configProp);
        }

        // look to see if there's a minor properties file
        File minorCodeProp = new File(mExportProjectRoot, FILE_MINOR_CODE);
        Map<Integer, Integer> minorCodeMap = null;
        if (minorCodeProp.isFile()) {
            minorCodeMap = getMinorCodeMap(minorCodeProp);
        }

        // get the apk from the projects.
        return getApkData(projects, minorCodeMap);
    }

    /**
     * Returns the list of projects defined by the <var>projectList</var> string.
     * The projects are checked to be valid Android project and to represent a valid set
     * of projects for multi-apk export.
     * If a project does not exist or is not valid, the method will throw a {@link BuildException}.
     * The string must be a list of paths, relative to the export project path (given to
     * {@link #MultiApkExportHelper(String, String, int, Target)}), separated by the colon (':')
     * character. The path separator is expected to be forward-slash ('/') on all platforms.
     * @param projects the string containing all the relative paths to the projects. This is
     * usually read from export.properties.
     * @throws ExportException
     */
    public List<ProjectConfig> getProjects(String projectList) throws ExportException {
        String[] paths = projectList.split("\\:");

        mProjectList = new ArrayList<ProjectConfig>();

        for (String path : paths) {
            path = path.replaceAll("\\/", File.separator);
            processProject(path, mProjectList);
        }

        return mProjectList;
    }

    /**
     * Writes post-export logs and other files.
     * @throws ExportException if writing the files failed.
     */
    public void writeLogs() throws ExportException {
        writeConfigProperties();
        writeMinorVersionProperties();
        writeApkLog();
    }

    private void writeConfigProperties() throws ExportException {
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(
                    new FileOutputStream(new File(mExportProjectRoot, FILE_CONFIG)));

            writer.append("# PROJECT CONFIG -- DO NOT DELETE.\n");
            writeValue(writer, PROP_VERSIONCODE, mVersionCode);

            for (ProjectConfig project : mProjectList) {
                writeValue(writer,project.getRelativePath(),
                        project.getConfigString(false /*onlyManifestData*/));
            }

            writer.flush();
        } catch (Exception e) {
            throw new ExportException("Failed to write config log", e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                throw new ExportException("Failed to write config log", e);
            }
        }
    }

    private void writeMinorVersionProperties() throws ExportException {
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(
                    new FileOutputStream(new File(mExportProjectRoot, FILE_MINOR_CODE)));

            writer.append(
                    "# Minor version codes.\n" +
                    "# To create update to select APKs without updating the main versionCode\n" +
                    "# edit this file and manually increase the minor version for the select\n" +
                    "# build info.\n" +
                    "# Format of the file is <buildinfo>:<minor>\n");
            writeValue(writer, PROP_VERSIONCODE, mVersionCode);

            for (ApkData apk : mApkDataList) {
                writeValue(writer, Integer.toString(apk.getBuildInfo()), apk.getMinorCode());
            }

            writer.flush();
        } catch (Exception e) {
            throw new ExportException("Failed to write minor log", e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                throw new ExportException("Failed to write minor log", e);
            }
        }
    }

    private void writeApkLog() throws ExportException {
        OutputStreamWriter writer = null;
        try {
            File logFolder = new File(mExportProjectRoot, FOLDER_LOG);
            if (logFolder.isFile()) {
                throw new ExportException("Cannot create folder '%1$s', file is in the way!",
                        FOLDER_LOG);
            } else if (logFolder.exists() == false) {
                logFolder.mkdir();
            }

            Formatter formatter = new Formatter();
            formatter.format("%1$s.%2$d-%3$tY%3$tm%3$td-%3$tH%3$tM.log",
                    mAppPackage, mVersionCode,
                    Calendar.getInstance().getTime());

            writer = new OutputStreamWriter(
                    new FileOutputStream(new File(logFolder, formatter.toString())));

            writer.append("# Multi-APK BUILD LOG.\n");
            writeValue(writer, PROP_PACKAGE, mAppPackage);
            writeValue(writer, PROP_VERSIONCODE, mVersionCode);

            for (ApkData apk : mApkDataList) {
                // if there are soft variant, do not display the main log line, as it's not actually
                // exported.
                Map<String, String> softVariants = apk.getSoftVariantMap();
                if (softVariants.size() > 0) {
                    for (String softVariant : softVariants.keySet()) {
                        writer.append(apk.getLogLine(softVariant));
                        writer.append('\n');
                    }
                } else {
                    writer.append(apk.getLogLine(null));
                    writer.append('\n');
                }
            }

            writer.flush();
        } catch (Exception e) {
            throw new ExportException("Failed to write build log", e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                throw new ExportException("Failed to write build log", e);
            }
        }
    }

    private void writeValue(OutputStreamWriter writer, String name, String value)
            throws IOException {
        writer.append(name).append(':').append(value).append('\n');
    }

    private void writeValue(OutputStreamWriter writer, String name, int value) throws IOException {
        writeValue(writer, name, Integer.toString(value));
    }

    private List<ApkData> getApkData(List<ProjectConfig> projects,
            Map<Integer, Integer> minorCodes) {
        mApkDataList = new ArrayList<ApkData>();

        // get all the apkdata from all the projects
        for (ProjectConfig config : projects) {
            mApkDataList.addAll(config.getApkDataList());
        }

        // sort the projects and assign buildInfo
        Collections.sort(mApkDataList);
        int buildInfo = 0;
        for (ApkData data : mApkDataList) {
            data.setBuildInfo(buildInfo);
            if (minorCodes != null) {
                Integer minorCode = minorCodes.get(buildInfo);
                if (minorCode != null) {
                    data.setMinorCode(minorCode);
                }
            }

            buildInfo++;
        }

        return mApkDataList;
    }

    /**
     * Checks a project for inclusion in the list of exported APK.
     * <p/>This performs a check on the manifest, as well as gathers more information about
     * mutli-apk from the project's default.properties file.
     * If the manifest is correct, a list of apk to export is created and returned.
     *
     * @param projectFolder the folder of the project to check
     * @param projects the list of project to file with the project if it passes validation.
     * @throws ExportException in case of error.
     */
    private void processProject(String relativePath,
            ArrayList<ProjectConfig> projects) throws ExportException {

        // resolve the relative path
        File projectFolder;
        try {
            File path = new File(mExportProjectRoot, relativePath);

            projectFolder = path.getCanonicalFile();

            // project folder must exist and be a directory
            if (projectFolder.isDirectory() == false) {
                throw new ExportException(
                        "Project folder '%1$s' is not a valid directory.",
                        projectFolder.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new ExportException(
                    e, "Failed to resolve path %1$s", relativePath);
        }

        try {
            // Check AndroidManifest.xml is present
            IAbstractFile androidManifest = new FileWrapper(projectFolder,
                    SdkConstants.FN_ANDROID_MANIFEST_XML);

            if (androidManifest.exists() == false) {
                throw new ExportException(String.format(
                        "%1$s is not a valid project (%2$s not found).",
                        relativePath, androidManifest.getOsLocation()));
            }

            // output the relative path resolution.
            mStdio.println(String.format("%1$s => %2$s", relativePath,
                    projectFolder.getAbsolutePath()));

            // parse the manifest of the project.
            ManifestData manifestData = AndroidManifestParser.parse(androidManifest);

            // validate the application package name
            String manifestPackage = manifestData.getPackage();
            if (mAppPackage.equals(manifestPackage) == false) {
                throw new ExportException(
                        "%1$s package value is not valid. Found '%2$s', expected '%3$s'.",
                        androidManifest.getOsLocation(), manifestPackage, mAppPackage);
            }

            // validate that the manifest has no versionCode set.
            if (manifestData.getVersionCode() != null) {
                throw new ExportException(
                        "%1$s is not valid: versionCode must not be set for multi-apk export.",
                        androidManifest.getOsLocation());
            }

            // validate that the minSdkVersion is not a codename
            int minSdkVersion = manifestData.getMinSdkVersion();
            if (minSdkVersion == ManifestData.MIN_SDK_CODENAME) {
                throw new ExportException(
                        "Codename in minSdkVersion is not supported by multi-apk export.");
            }

            // compare to other projects already processed to make sure that they are not
            // identical.
            for (ProjectConfig otherProject : projects) {
                // Multiple apk export support difference in:
                // - min SDK Version
                // - Screen version
                // - GL version
                // - ABI (not managed at the Manifest level).
                // if those values are the same between 2 manifest, then it's an error.


                // first the minSdkVersion.
                if (minSdkVersion == otherProject.getMinSdkVersion()) {
                    // if it's the same compare the rest.
                    SupportsScreens currentSS = manifestData.getSupportsScreensValues();
                    SupportsScreens previousSS = otherProject.getSupportsScreens();
                    boolean sameSupportsScreens = currentSS.hasSameScreenSupportAs(previousSS);

                    // if it's the same, then it's an error. Can't export 2 projects that have the
                    // same approved (for multi-apk export) hard-properties.
                    if (manifestData.getGlEsVersion() == otherProject.getGlEsVersion() &&
                            sameSupportsScreens) {

                        throw new ExportException(
                                "Android manifests must differ in at least one of the following values:\n" +
                                "- minSdkVersion\n" +
                                "- SupportsScreen (screen sizes only)\n" +
                                "- GL ES version.\n" +
                                "%1$s and %2$s are considered identical for multi-apk export.",
                                relativePath,
                                otherProject.getRelativePath());
                    }

                    // At this point, either supports-screens or GL are different.
                    // Because supports-screens is the highest priority properties to be
                    // (potentially) different, we must do some extra checks on it.
                    // It must either be the same in both projects (difference is only on GL value),
                    // or follow theses rules:
                    // - Property in each projects must be strictly different, ie both projects
                    //   cannot support the same screen size(s).
                    // - Property in each projects cannot overlap, ie a projects cannot support
                    //   both a lower and a higher screen size than the other project.
                    //   (ie APK1 supports small/large and APK2 supports normal).
                    if (sameSupportsScreens == false) {
                        if (currentSS.hasStrictlyDifferentScreenSupportAs(previousSS) == false) {
                            throw new ExportException(
                                    "APK differentiation by Supports-Screens cannot support different APKs supporting the same screen size.\n" +
                                    "%1$s supports %2$s\n" +
                                    "%3$s supports %4$s\n",
                                    relativePath, currentSS.toString(),
                                    otherProject.getRelativePath(), previousSS.toString());
                        }

                        if (currentSS.overlapWith(previousSS)) {
                            throw new ExportException(
                                    "Unable to compute APK priority due to incompatible difference in Supports-Screens values.\n" +
                                    "%1$s supports %2$s\n" +
                                    "%3$s supports %4$s\n",
                                    relativePath, currentSS.toString(),
                                    otherProject.getRelativePath(), previousSS.toString());
                        }
                    }
                }
            }

            // project passes first validation. Attempt to create a ProjectConfig object.

            ProjectConfig config = ProjectConfig.create(projectFolder, relativePath, manifestData);
            projects.add(config);
        } catch (SAXException e) {
            throw new ExportException(e, "Failed to validate %1$s", relativePath);
        } catch (IOException e) {
            throw new ExportException(e, "Failed to validate %1$s", relativePath);
        } catch (StreamException e) {
            throw new ExportException(e, "Failed to validate %1$s", relativePath);
        } catch (ParserConfigurationException e) {
            throw new ExportException(e, "Failed to validate %1$s", relativePath);
        }
    }

    /**
     * Checks an existing list of {@link ProjectConfig} versus a config file.
     * @param projects the list of projects to check
     * @param configProp the config file (must have been generated from a previous export)
     * @return true if the projects and config file match
     * @throws ExportException in case of error
     */
    private void compareProjectsToConfigFile(List<ProjectConfig> projects, File configProp)
            throws ExportException {
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(configProp));
            bufferedReader = new BufferedReader(reader);
            String line;

            // List of the ProjectConfig that need to be checked. This is to detect
            // new Projects added to the setup.
            // removed projects are detected when an entry in the config file doesn't match
            // any ProjectConfig in the list.
            ArrayList<ProjectConfig> projectsToCheck = new ArrayList<ProjectConfig>();
            projectsToCheck.addAll(projects);

            // store the project that doesn't match.
            ProjectConfig badMatch = null;
            String errorMsg = null;

            // recorded whether we checked the version code. this is for when we compare
            // a project config
            boolean checkedVersion = false;

            int lineNumber = 0;
            while ((line = bufferedReader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.length() == 0 || line.startsWith("#")) {
                    continue;
                }

                // read the name of the property
                int colonPos = line.indexOf(':');
                if (colonPos == -1) {
                    // looks like there's an invalid line!
                    throw new ExportException(
                            "Failed to read existing build log. Line %d is not a property line.",
                            lineNumber);
                }

                String name = line.substring(0, colonPos);
                String value = line.substring(colonPos + 1);

                if (PROP_VERSIONCODE.equals(name)) {
                    try {
                        int versionCode = Integer.parseInt(value);
                        if (versionCode < mVersionCode) {
                            // this means this config file is obsolete and we can ignore it.
                            return;
                        } else if (versionCode > mVersionCode) {
                            // we're exporting at a lower versionCode level than the config file?
                            throw new ExportException(
                                    "Incompatible versionCode: Exporting at versionCode %1$d but %2$s file indicate previous export with versionCode %3$d.",
                                    mVersionCode, FILE_CONFIG, versionCode);
                        } else if (badMatch != null) {
                            // looks like versionCode is a match, but a project
                            // isn't compatible.
                            break;
                        } else {
                            // record that we did check the versionCode
                            checkedVersion = true;
                        }
                    } catch (NumberFormatException e) {
                        throw new ExportException(
                                "Failed to read integer property %1$s at line %2$d.",
                                PROP_VERSIONCODE, lineNumber);
                    }
                } else {
                    // looks like this is (or should be) a project line.
                    // name of the property is the relative path.
                    // look for a matching project.
                    ProjectConfig found = null;
                    for (int i = 0 ; i < projectsToCheck.size() ; i++) {
                        ProjectConfig p = projectsToCheck.get(i);
                        if (p.getRelativePath().equals(name)) {
                            found = p;
                            projectsToCheck.remove(i);
                            break;
                        }
                    }

                    if (found == null) {
                        // deleted project!
                        throw new ExportException(
                                "Project %1$s has been removed from the list of projects to export.\n" +
                                "Any change in the multi-apk configuration requires an increment of the versionCode in export.properties.",
                                name);
                    } else {
                        // make a map of properties
                        HashMap<String, String> map = new HashMap<String, String>();
                        String[] properties = value.split(";");
                        for (String prop : properties) {
                            int equalPos = prop.indexOf('=');
                            map.put(prop.substring(0, equalPos), prop.substring(equalPos + 1));
                        }

                        errorMsg = found.compareToProperties(map);
                        if (errorMsg != null) {
                            // bad project config, record the project
                            badMatch = found;

                            // if we've already checked that the versionCode didn't already change
                            // we stop right away.
                            if (checkedVersion) {
                                break;
                            }
                        }
                    }

                }

            }

            if (badMatch != null) {
                throw new ExportException(
                        "Config for project %1$s has changed from previous export with versionCode %2$d:\n" +
                        "\t%3$s\n" +
                        "Any change in the multi-apk configuration requires an increment of the versionCode in export.properties.",
                        badMatch.getRelativePath(), mVersionCode, errorMsg);
            } else if (projectsToCheck.size() > 0) {
                throw new ExportException(
                        "Project %1$s was not part of the previous export with versionCode %2$d.\n" +
                        "Any change in the multi-apk configuration requires an increment of the versionCode in export.properties.",
                        projectsToCheck.get(0).getRelativePath(), mVersionCode);
            }

        } catch (IOException e) {
            throw new ExportException(e, "Failed to read existing config log: %s", FILE_CONFIG);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                throw new ExportException(e, "Failed to read existing config log: %s", FILE_CONFIG);
            }
        }
    }

    private Map<Integer, Integer> getMinorCodeMap(File minorProp) throws ExportException {
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(minorProp));
            bufferedReader = new BufferedReader(reader);
            String line;

            boolean foundVersionCode = false;
            Map<Integer, Integer> map = new HashMap<Integer, Integer>();

            int lineNumber = 0;
            while ((line = bufferedReader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.length() == 0 || line.startsWith("#")) {
                    continue;
                }

                // read the name of the property
                int colonPos = line.indexOf(':');
                if (colonPos == -1) {
                    // looks like there's an invalid line!
                    throw new ExportException(
                            "Failed to read existing build log. Line %d is not a property line.",
                            lineNumber);
                }

                String name = line.substring(0, colonPos);
                String value = line.substring(colonPos + 1);

                if (PROP_VERSIONCODE.equals(name)) {
                    try {
                        int versionCode = Integer.parseInt(value);
                        if (versionCode < mVersionCode) {
                            // this means this minor file is obsolete and we can ignore it.
                            return null;
                        } else if (versionCode > mVersionCode) {
                            // we're exporting at a lower versionCode level than the minor file?
                            throw new ExportException(
                                    "Incompatible versionCode: Exporting at versionCode %1$d but %2$s file indicate previous export with versionCode %3$d.",
                                    mVersionCode, FILE_MINOR_CODE, versionCode);
                        }
                        foundVersionCode = true;
                    } catch (NumberFormatException e) {
                        throw new ExportException(
                                "Failed to read integer property %1$s at line %2$d.",
                                PROP_VERSIONCODE, lineNumber);
                    }
                } else {
                    try {
                        map.put(Integer.valueOf(name), Integer.valueOf(value));
                    } catch (NumberFormatException e) {
                        throw new ExportException(
                                "Failed to read buildInfo property '%1$s' at line %2$d.",
                                line, lineNumber);
                    }
                }
            }

            // if there was no versionCode found, we can't garantee that the minor version
            // found are for this versionCode
            if (foundVersionCode == false) {
                throw new ExportException(
                        "%1$s property missing from file %2$s.", PROP_VERSIONCODE, FILE_MINOR_CODE);
            }

            return map;
        } catch (IOException e) {
            throw new ExportException(e, "Failed to read existing minor log: %s", FILE_MINOR_CODE);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                throw new ExportException(e, "Failed to read existing minor log: %s",
                        FILE_MINOR_CODE);
            }
        }
    }
}
