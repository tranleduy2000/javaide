/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.sdklib.internal.project;

import com.android.AndroidConstants;
import com.android.io.FileWrapper;
import com.android.io.FolderWrapper;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.ISdkLog;
import com.android.sdklib.SdkConstants;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.project.ProjectProperties.PropertyType;
import com.android.sdklib.xml.AndroidManifest;
import com.android.sdklib.xml.AndroidXPathFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Creates the basic files needed to get an Android project up and running.
 *
 * @hide
 */
public class ProjectCreator {

    /** Version of the build.xml. Stored in version-tag */
    private final static int MIN_BUILD_VERSION_TAG = 1;

    /** Package path substitution string used in template files, i.e. "PACKAGE_PATH" */
    private final static String PH_JAVA_FOLDER = "PACKAGE_PATH";
    /** Package name substitution string used in template files, i.e. "PACKAGE" */
    private final static String PH_PACKAGE = "PACKAGE";
    /** Activity name substitution string used in template files, i.e. "ACTIVITY_NAME".
     * @deprecated This is only used for older templates. For new ones see
     * {@link #PH_ACTIVITY_ENTRY_NAME}, and {@link #PH_ACTIVITY_CLASS_NAME}. */
    @Deprecated
    private final static String PH_ACTIVITY_NAME = "ACTIVITY_NAME";
    /** Activity name substitution string used in manifest templates, i.e. "ACTIVITY_ENTRY_NAME".*/
    private final static String PH_ACTIVITY_ENTRY_NAME = "ACTIVITY_ENTRY_NAME";
    /** Activity name substitution string used in class templates, i.e. "ACTIVITY_CLASS_NAME".*/
    private final static String PH_ACTIVITY_CLASS_NAME = "ACTIVITY_CLASS_NAME";
    /** Activity FQ-name substitution string used in class templates, i.e. "ACTIVITY_FQ_NAME".*/
    private final static String PH_ACTIVITY_FQ_NAME = "ACTIVITY_FQ_NAME";
    /** Original Activity class name substitution string used in class templates, i.e.
     * "ACTIVITY_TESTED_CLASS_NAME".*/
    private final static String PH_ACTIVITY_TESTED_CLASS_NAME = "ACTIVITY_TESTED_CLASS_NAME";
    /** Project name substitution string used in template files, i.e. "PROJECT_NAME". */
    private final static String PH_PROJECT_NAME = "PROJECT_NAME";
    /** Application icon substitution string used in the manifest template */
    private final static String PH_ICON = "ICON";
    /** Version tag name substitution string used in template files, i.e. "VERSION_TAG". */
    private final static String PH_VERSION_TAG = "VERSION_TAG";

    /** The xpath to find a project name in an Ant build file. */
    private static final String XPATH_PROJECT_NAME = "/project/@name";

    /** Pattern for characters accepted in a project name. Since this will be used as a
     * directory name, we're being a bit conservative on purpose: dot and space cannot be used. */
    public static final Pattern RE_PROJECT_NAME = Pattern.compile("[a-zA-Z0-9_]+");
    /** List of valid characters for a project name. Used for display purposes. */
    public final static String CHARS_PROJECT_NAME = "a-z A-Z 0-9 _";

    /** Pattern for characters accepted in a package name. A package is list of Java identifier
     * separated by a dot. We need to have at least one dot (e.g. a two-level package name).
     * A Java identifier cannot start by a digit. */
    public static final Pattern RE_PACKAGE_NAME =
        Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z0-9_]*)+");
    /** List of valid characters for a project name. Used for display purposes. */
    public final static String CHARS_PACKAGE_NAME = "a-z A-Z 0-9 _";

    /** Pattern for characters accepted in an activity name, which is a Java identifier. */
    public static final Pattern RE_ACTIVITY_NAME =
        Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
    /** List of valid characters for a project name. Used for display purposes. */
    public final static String CHARS_ACTIVITY_NAME = "a-z A-Z 0-9 _";


    public enum OutputLevel {
        /** Silent mode. Project creation will only display errors. */
        SILENT,
        /** Normal mode. Project creation will display what's being done, display
         * error but not warnings. */
        NORMAL,
        /** Verbose mode. Project creation will display what's being done, errors and warnings. */
        VERBOSE;
    }

    /**
     * Exception thrown when a project creation fails, typically because a template
     * file cannot be written.
     */
    private static class ProjectCreateException extends Exception {
        /** default UID. This will not be serialized anyway. */
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unused")
        ProjectCreateException(String message) {
            super(message);
        }

        ProjectCreateException(Throwable t, String format, Object... args) {
            super(format != null ? String.format(format, args) : format, t);
        }

        ProjectCreateException(String format, Object... args) {
            super(String.format(format, args));
        }
    }

    /** The {@link OutputLevel} verbosity. */
    private final OutputLevel mLevel;
    /** Logger for errors and output. Cannot be null. */
    private final ISdkLog mLog;
    /** The OS path of the SDK folder. */
    private final String mSdkFolder;
    /** The {@link SdkManager} instance. */
    private final SdkManager mSdkManager;

    /**
     * Helper class to create android projects.
     *
     * @param sdkManager The {@link SdkManager} instance.
     * @param sdkFolder The OS path of the SDK folder.
     * @param level The {@link OutputLevel} verbosity.
     * @param log Logger for errors and output. Cannot be null.
     */
    public ProjectCreator(SdkManager sdkManager, String sdkFolder, OutputLevel level, ISdkLog log) {
        mSdkManager = sdkManager;
        mSdkFolder = sdkFolder;
        mLevel = level;
        mLog = log;
    }

    /**
     * Creates a new project.
     * <p/>
     * The caller should have already checked and sanitized the parameters.
     *
     * @param folderPath the folder of the project to create.
     * @param projectName the name of the project. The name must match the
     *          {@link #RE_PROJECT_NAME} regex.
     * @param packageName the package of the project. The name must match the
     *          {@link #RE_PACKAGE_NAME} regex.
     * @param activityEntry the activity of the project as it will appear in the manifest. Can be
     *          null if no activity should be created. The name must match the
     *          {@link #RE_ACTIVITY_NAME} regex.
     * @param target the project target.
     * @param library whether the project is a library.
     * @param pathToMainProject if non-null the project will be setup to test a main project
     * located at the given path.
     */
    public void createProject(String folderPath, String projectName,
            String packageName, String activityEntry, IAndroidTarget target, boolean library,
            String pathToMainProject) {

        // create project folder if it does not exist
        File projectFolder = checkNewProjectLocation(folderPath);
        if (projectFolder == null) {
            return;
        }

        try {
            boolean isTestProject = pathToMainProject != null;

            // first create the project properties.

            // location of the SDK goes in localProperty
            ProjectPropertiesWorkingCopy localProperties = ProjectProperties.create(folderPath,
                    PropertyType.LOCAL);
            localProperties.setProperty(ProjectProperties.PROPERTY_SDK, mSdkFolder);
            localProperties.save();

            // target goes in default properties
            ProjectPropertiesWorkingCopy defaultProperties = ProjectProperties.create(folderPath,
                    PropertyType.PROJECT);
            defaultProperties.setProperty(ProjectProperties.PROPERTY_TARGET, target.hashString());
            if (library) {
                defaultProperties.setProperty(ProjectProperties.PROPERTY_LIBRARY, "true");
            }
            defaultProperties.save();

            // create a build.properties file with just the application package
            ProjectPropertiesWorkingCopy buildProperties = ProjectProperties.create(folderPath,
                    PropertyType.ANT);

            if (isTestProject) {
                buildProperties.setProperty(ProjectProperties.PROPERTY_TESTED_PROJECT,
                        pathToMainProject);
            }

            buildProperties.save();

            // create the map for place-holders of values to replace in the templates
            final HashMap<String, String> keywords = new HashMap<String, String>();

            // create the required folders.
            // compute src folder path
            final String packagePath =
                stripString(packageName.replace(".", File.separator),
                        File.separatorChar);

            // put this path in the place-holder map for project files that needs to list
            // files manually.
            keywords.put(PH_JAVA_FOLDER, packagePath);
            keywords.put(PH_PACKAGE, packageName);
            keywords.put(PH_VERSION_TAG, Integer.toString(MIN_BUILD_VERSION_TAG));


            // compute some activity related information
            String fqActivityName = null, activityPath = null, activityClassName = null;
            String originalActivityEntry = activityEntry;
            String originalActivityClassName = null;
            if (activityEntry != null) {
                if (isTestProject) {
                    // append Test so that it doesn't collide with the main project activity.
                    activityEntry += "Test";

                    // get the classname from the original activity entry.
                    int pos = originalActivityEntry.lastIndexOf('.');
                    if (pos != -1) {
                        originalActivityClassName = originalActivityEntry.substring(pos + 1);
                    } else {
                        originalActivityClassName = originalActivityEntry;
                    }
                }

                // get the fully qualified name of the activity
                fqActivityName = AndroidManifest.combinePackageAndClassName(packageName,
                        activityEntry);

                // get the activity path (replace the . to /)
                activityPath = stripString(fqActivityName.replace(".", File.separator),
                        File.separatorChar);

                // remove the last segment, so that we only have the path to the activity, but
                // not the activity filename itself.
                activityPath = activityPath.substring(0,
                        activityPath.lastIndexOf(File.separatorChar));

                // finally, get the class name for the activity
                activityClassName = fqActivityName.substring(fqActivityName.lastIndexOf('.') + 1);
            }

            // at this point we have the following for the activity:
            // activityEntry: this is the manifest entry. For instance .MyActivity
            // fqActivityName: full-qualified class name: com.foo.MyActivity
            // activityClassName: only the classname: MyActivity
            // originalActivityClassName: the classname of the activity being tested (if applicable)

            // Add whatever activity info is needed in the place-holder map.
            // Older templates only expect ACTIVITY_NAME to be the same (and unmodified for tests).
            if (target.getVersion().getApiLevel() < 4) { // legacy
                if (originalActivityEntry != null) {
                    keywords.put(PH_ACTIVITY_NAME, originalActivityEntry);
                }
            } else {
                // newer templates make a difference between the manifest entries, classnames,
                // as well as the main and test classes.
                if (activityEntry != null) {
                    keywords.put(PH_ACTIVITY_ENTRY_NAME, activityEntry);
                    keywords.put(PH_ACTIVITY_CLASS_NAME, activityClassName);
                    keywords.put(PH_ACTIVITY_FQ_NAME, fqActivityName);
                    if (originalActivityClassName != null) {
                        keywords.put(PH_ACTIVITY_TESTED_CLASS_NAME, originalActivityClassName);
                    }
                }
            }

            // Take the project name from the command line if there's one
            if (projectName != null) {
                keywords.put(PH_PROJECT_NAME, projectName);
            } else {
                if (activityClassName != null) {
                    // Use the activity class name as project name
                    keywords.put(PH_PROJECT_NAME, activityClassName);
                } else {
                    // We need a project name. Just pick up the basename of the project
                    // directory.
                    projectName = projectFolder.getName();
                    keywords.put(PH_PROJECT_NAME, projectName);
                }
            }

            // create the source folder for the activity
            if (activityClassName != null) {
                String srcActivityFolderPath =
                        SdkConstants.FD_SOURCES + File.separator + activityPath;
                File sourceFolder = createDirs(projectFolder, srcActivityFolderPath);

                String javaTemplate = isTestProject ? "java_tests_file.template"
                        : "java_file.template";
                String activityFileName = activityClassName + ".java";

                installTargetTemplate(javaTemplate, new File(sourceFolder, activityFileName),
                        keywords, target);
            } else {
                // we should at least create 'src'
                createDirs(projectFolder, SdkConstants.FD_SOURCES);
            }

            // create other useful folders
            File resourceFolder = createDirs(projectFolder, SdkConstants.FD_RESOURCES);
            createDirs(projectFolder, SdkConstants.FD_OUTPUT);
            createDirs(projectFolder, SdkConstants.FD_NATIVE_LIBS);

            if (isTestProject == false) {
                /* Make res files only for non test projects */
                File valueFolder = createDirs(resourceFolder, AndroidConstants.FD_RES_VALUES);
                installTargetTemplate("strings.template", new File(valueFolder, "strings.xml"),
                        keywords, target);

                File layoutFolder = createDirs(resourceFolder, AndroidConstants.FD_RES_LAYOUT);
                installTargetTemplate("layout.template", new File(layoutFolder, "main.xml"),
                        keywords, target);

                // create the icons
                if (installIcons(resourceFolder, target)) {
                    keywords.put(PH_ICON, "android:icon=\"@drawable/ic_launcher\"");
                } else {
                    keywords.put(PH_ICON, "");
                }
            }

            /* Make AndroidManifest.xml and build.xml files */
            String manifestTemplate = "AndroidManifest.template";
            if (isTestProject) {
                manifestTemplate = "AndroidManifest.tests.template";
            }

            installTargetTemplate(manifestTemplate,
                    new File(projectFolder, SdkConstants.FN_ANDROID_MANIFEST_XML),
                    keywords, target);

            installTemplate("build.template",
                    new File(projectFolder, SdkConstants.FN_BUILD_XML),
                    keywords);

            // install the proguard config file.
            installTemplate(SdkConstants.FN_PROGUARD_CFG,
                    new File(projectFolder, SdkConstants.FN_PROGUARD_CFG),
                    null /*keywords*/);
        } catch (Exception e) {
            mLog.error(e, null);
        }
    }

    private File checkNewProjectLocation(String folderPath) {
        File projectFolder = new File(folderPath);
        if (!projectFolder.exists()) {

            boolean created = false;
            Throwable t = null;
            try {
                created = projectFolder.mkdirs();
            } catch (Exception e) {
                t = e;
            }

            if (created) {
                println("Created project directory: %1$s", projectFolder);
            } else {
                mLog.error(t, "Could not create directory: %1$s", projectFolder);
                return null;
            }
        } else {
            Exception e = null;
            String error = null;
            try {
                String[] content = projectFolder.list();
                if (content == null) {
                    error = "Project folder '%1$s' is not a directory.";
                } else if (content.length != 0) {
                    error = "Project folder '%1$s' is not empty. Please consider using '%2$s update' instead.";
                }
            } catch (Exception e1) {
                e = e1;
            }

            if (e != null || error != null) {
                mLog.error(e, error, projectFolder, SdkConstants.androidCmdName());
            }
        }
        return projectFolder;
    }

    /**
     * Updates an existing project.
     * <p/>
     * Workflow:
     * <ul>
     * <li> Check AndroidManifest.xml is present (required)
     * <li> Check if there's a legacy properties file and convert it
     * <li> Check there's a project.properties with a target *or* --target was specified
     * <li> Update default.prop if --target was specified
     * <li> Refresh/create "sdk" in local.properties
     * <li> Build.xml: create if not present or if version-tag is found or not. version-tag:custom
     * prevent any overwrite. version-tag:[integer] will override. missing version-tag will query
     * the dev.
     * </ul>
     *
     * @param folderPath the folder of the project to update. This folder must exist.
     * @param target the project target. Can be null.
     * @param projectName The project name from --name. Can be null.
     * @param libraryPath the path to a library to add to the references. Can be null.
     * @return true if the project was successfully updated.
     */
    @SuppressWarnings("deprecation")
    public boolean updateProject(String folderPath, IAndroidTarget target, String projectName,
            String libraryPath) {
        // since this is an update, check the folder does point to a project
        FileWrapper androidManifest = checkProjectFolder(folderPath,
                SdkConstants.FN_ANDROID_MANIFEST_XML);
        if (androidManifest == null) {
            return false;
        }

        // get the parent folder.
        FolderWrapper projectFolder = (FolderWrapper) androidManifest.getParentFolder();

        boolean hasProguard = false;

        // Check there's a project.properties with a target *or* --target was specified
        IAndroidTarget originalTarget = null;
        boolean writeProjectProp = false;
        ProjectProperties props = ProjectProperties.load(projectFolder, PropertyType.PROJECT);

        if (props == null) {
            // no project.properties, try to load default.properties
            props = ProjectProperties.load(projectFolder, PropertyType.LEGACY_DEFAULT);
            writeProjectProp = true;
        }

        if (props != null) {
            String targetHash = props.getProperty(ProjectProperties.PROPERTY_TARGET);
            originalTarget = mSdkManager.getTargetFromHashString(targetHash);

            // if the project is already setup with proguard, we won't copy the proguard config.
            hasProguard = props.getProperty(ProjectProperties.PROPERTY_PROGUARD_CONFIG) != null;
        }

        if (originalTarget == null && target == null) {
            mLog.error(null,
                "The project either has no target set or the target is invalid.\n" +
                "Please provide a --target to the '%1$s update' command.",
                SdkConstants.androidCmdName());
            return false;
        }

        boolean saveProjectProps = false;

        ProjectPropertiesWorkingCopy propsWC = null;

        // Update default.prop if --target was specified
        if (target != null || writeProjectProp) {
            // we already attempted to load the file earlier, if that failed, create it.
            if (props == null) {
                propsWC = ProjectProperties.create(projectFolder, PropertyType.PROJECT);
            } else {
                propsWC = props.makeWorkingCopy(PropertyType.PROJECT);
            }

            // set or replace the target
            if (target != null) {
                propsWC.setProperty(ProjectProperties.PROPERTY_TARGET, target.hashString());
            }
            saveProjectProps = true;
        }

        if (libraryPath != null) {
            // At this point, the default properties already exists, either because they were
            // already there or because they were created with a new target
            if (propsWC == null) {
                assert props != null;
                propsWC = props.makeWorkingCopy();
            }

            // check the reference is valid
            File libProject = new File(libraryPath);
            String resolvedPath;
            if (libProject.isAbsolute() == false) {
                libProject = new File(projectFolder, libraryPath);
                try {
                    resolvedPath = libProject.getCanonicalPath();
                } catch (IOException e) {
                    mLog.error(e, "Unable to resolve path to library project: %1$s", libraryPath);
                    return false;
                }
            } else {
                resolvedPath = libProject.getAbsolutePath();
            }

            println("Resolved location of library project to: %1$s", resolvedPath);

            // check the lib project exists
            if (checkProjectFolder(resolvedPath, SdkConstants.FN_ANDROID_MANIFEST_XML) == null) {
                mLog.error(null, "No Android Manifest at: %1$s", resolvedPath);
                return false;
            }

            // look for other references to figure out the index
            int index = 1;
            while (true) {
                String propName = ProjectProperties.PROPERTY_LIB_REF + Integer.toString(index);
                assert props != null;
                String ref = props.getProperty(propName);
                if (ref == null) {
                    break;
                } else {
                    index++;
                }
            }

            String propName = ProjectProperties.PROPERTY_LIB_REF + Integer.toString(index);
            propsWC.setProperty(propName, libraryPath);
            saveProjectProps = true;
        }

        // save the default props if needed.
        if (saveProjectProps) {
            try {
                assert propsWC != null;
                propsWC.save();
                if (writeProjectProp) {
                    println("Updated and renamed %1$s to %2$s",
                            PropertyType.LEGACY_DEFAULT.getFilename(),
                            PropertyType.PROJECT.getFilename());
                } else {
                    println("Updated %1$s", PropertyType.PROJECT.getFilename());
                }
            } catch (Exception e) {
                mLog.error(e, "Failed to write %1$s file in '%2$s'",
                        PropertyType.PROJECT.getFilename(),
                        folderPath);
                return false;
            }

            if (writeProjectProp) {
                // need to delete the default prop file.
                ProjectProperties.delete(projectFolder, PropertyType.LEGACY_DEFAULT);
            }
        }

        // Refresh/create "sdk" in local.properties
        // because the file may already exists and contain other values (like apk config),
        // we first try to load it.
        props = ProjectProperties.load(projectFolder, PropertyType.LOCAL);
        if (props == null) {
            propsWC = ProjectProperties.create(projectFolder, PropertyType.LOCAL);
        } else {
            propsWC = props.makeWorkingCopy();
        }

        // set or replace the sdk location.
        propsWC.setProperty(ProjectProperties.PROPERTY_SDK, mSdkFolder);
        try {
            propsWC.save();
            println("Updated %1$s", PropertyType.LOCAL.getFilename());
        } catch (Exception e) {
            mLog.error(e, "Failed to write %1$s file in '%2$s'",
                    PropertyType.LOCAL.getFilename(),
                    folderPath);
            return false;
        }

        // legacy: check if build.properties must be renamed to ant.properties.
        props = ProjectProperties.load(projectFolder, PropertyType.ANT);
        if (props == null) {
            props = ProjectProperties.load(projectFolder, PropertyType.LEGACY_BUILD);
            if (props != null) {
                try {
                    // get a working copy with the new property type
                    propsWC = props.makeWorkingCopy(PropertyType.ANT);
                    propsWC.save();

                    // delete the old file
                    ProjectProperties.delete(projectFolder, PropertyType.LEGACY_BUILD);

                    println("Renamed %1$s to %2$s",
                            PropertyType.LEGACY_BUILD.getFilename(),
                            PropertyType.ANT.getFilename());
                } catch (Exception e) {
                    mLog.error(e, "Failed to write %1$s file in '%2$s'",
                            PropertyType.ANT.getFilename(),
                            folderPath);
                    return false;
                }
            }
        }

        // Build.xml: create if not present or no <androidinit/> in it
        File buildXml = new File(projectFolder, SdkConstants.FN_BUILD_XML);
        boolean needsBuildXml = projectName != null || !buildXml.exists();

        // if it seems there's no need for a new build.xml, look for inside the file
        // to try to detect old ones that may need updating.
        if (!needsBuildXml) {
            // we are looking for version-tag: followed by either an integer or "custom".
            if (checkFileContainsRegexp(buildXml, "version-tag:\\s*custom") != null) { //$NON-NLS-1$
                println("%1$s: Found version-tag: custom. File will not be updated.",
                        SdkConstants.FN_BUILD_XML);
            } else {
                Matcher m = checkFileContainsRegexp(buildXml, "version-tag:\\s*(\\d+)"); //$NON-NLS-1$
                if (m == null) {
                    println("----------\n" +
                            "%1$s: Failed to find version-tag string. File must be updated.\n" +
                            "In order to not erase potential customizations, the file will not be automatically regenerated.\n" +
                            "If no changes have been made to the file, delete it manually and run the command again.\n" +
                            "If you have made customizations to the build process, the file must be manually updated.\n" +
                            "It is recommended to:\n" +
                            "\t* Copy current file to a safe location.\n" +
                            "\t* Delete original file.\n" +
                            "\t* Run command again to generate a new file.\n" +
                            "\t* Port customizations to the new file, by looking at the new rules file\n" +
                            "\t  located at <SDK>/tools/ant/build.xml\n" +
                            "\t* Update file to contain\n" +
                            "\t      version-tag: custom\n" +
                            "\t  to prevent file from being rewritten automatically by the SDK tools.\n" +
                            "----------\n",
                            SdkConstants.FN_BUILD_XML);
                } else {
                    String versionStr = m.group(1);
                    if (versionStr != null) {
                        // can't fail due to regexp above.
                        int version = Integer.parseInt(versionStr);
                        if (version < MIN_BUILD_VERSION_TAG) {
                            println("%1$s: Found version-tag: %2$d. Expected version-tag: %3$d: file must be updated.",
                                    SdkConstants.FN_BUILD_XML, version, MIN_BUILD_VERSION_TAG);
                            needsBuildXml = true;
                        }
                    }
                }
            }
        }

        if (needsBuildXml) {
            // create the map for place-holders of values to replace in the templates
            final HashMap<String, String> keywords = new HashMap<String, String>();

            // put the current version-tag value
            keywords.put(PH_VERSION_TAG, Integer.toString(MIN_BUILD_VERSION_TAG));

            // if there was no project name on the command line, figure one out.
            if (projectName == null) {
                // otherwise, take it from the existing build.xml if it exists already.
                if (buildXml.exists()) {
                    try {
                        XPathFactory factory = XPathFactory.newInstance();
                        XPath xpath = factory.newXPath();

                        projectName = xpath.evaluate(XPATH_PROJECT_NAME,
                                new InputSource(new FileInputStream(buildXml)));
                    } catch (XPathExpressionException e) {
                        // this is ok since we're going to recreate the file.
                        mLog.error(e, "Unable to find existing project name from %1$s",
                                SdkConstants.FN_BUILD_XML);
                    } catch (FileNotFoundException e) {
                        // can't happen since we check above.
                    }
                }

                // if the project is still null, then we find another way.
                if (projectName == null) {
                    extractPackageFromManifest(androidManifest, keywords);
                    if (keywords.containsKey(PH_ACTIVITY_ENTRY_NAME)) {
                        String activity = keywords.get(PH_ACTIVITY_ENTRY_NAME);
                        // keep only the last segment if applicable
                        int pos = activity.lastIndexOf('.');
                        if (pos != -1) {
                            activity = activity.substring(pos + 1);
                        }

                        // Use the activity as project name
                        projectName = activity;

                        println("No project name specified, using Activity name '%1$s'.\n" +
                                "If you wish to change it, edit the first line of %2$s.",
                                activity, SdkConstants.FN_BUILD_XML);
                    } else {
                        // We need a project name. Just pick up the basename of the project
                        // directory.
                        File projectCanonicalFolder = projectFolder;
                        try {
                            projectCanonicalFolder = projectCanonicalFolder.getCanonicalFile();
                        } catch (IOException e) {
                            // ignore, keep going
                        }

                        // Use the folder name as project name
                        projectName = projectCanonicalFolder.getName();

                        println("No project name specified, using project folder name '%1$s'.\n" +
                                "If you wish to change it, edit the first line of %2$s.",
                                projectName, SdkConstants.FN_BUILD_XML);
                    }
                }
            }

            // put the project name in the map for replacement during the template installation.
            keywords.put(PH_PROJECT_NAME, projectName);

            if (mLevel == OutputLevel.VERBOSE) {
                println("Regenerating %1$s with project name %2$s",
                        SdkConstants.FN_BUILD_XML,
                        keywords.get(PH_PROJECT_NAME));
            }

            try {
                installTemplate("build.template", buildXml, keywords);
            } catch (ProjectCreateException e) {
                mLog.error(e, null);
                return false;
            }
        }

        if (hasProguard == false) {
            try {
                installTemplate(SdkConstants.FN_PROGUARD_CFG,
                        new File(projectFolder, SdkConstants.FN_PROGUARD_CFG),
                        null /*placeholderMap*/);
            } catch (ProjectCreateException e) {
                mLog.error(e, null);
                return false;
            }
        }

        return true;
    }

    /**
     * Updates a test project with a new path to the main (tested) project.
     * @param folderPath the path of the test project.
     * @param pathToMainProject the path to the main project, relative to the test project.
     */
    @SuppressWarnings("deprecation")
    public void updateTestProject(final String folderPath, final String pathToMainProject,
            final SdkManager sdkManager) {
        // since this is an update, check the folder does point to a project
        if (checkProjectFolder(folderPath, SdkConstants.FN_ANDROID_MANIFEST_XML) == null) {
            return;
        }

        // check the path to the main project is valid.
        File mainProject = new File(pathToMainProject);
        String resolvedPath;
        if (mainProject.isAbsolute() == false) {
            mainProject = new File(folderPath, pathToMainProject);
            try {
                resolvedPath = mainProject.getCanonicalPath();
            } catch (IOException e) {
                mLog.error(e, "Unable to resolve path to main project: %1$s", pathToMainProject);
                return;
            }
        } else {
            resolvedPath = mainProject.getAbsolutePath();
        }

        println("Resolved location of main project to: %1$s", resolvedPath);

        // check the main project exists
        if (checkProjectFolder(resolvedPath, SdkConstants.FN_ANDROID_MANIFEST_XML) == null) {
            mLog.error(null, "No Android Manifest at: %1$s", resolvedPath);
            return;
        }

        // now get the target from the main project
        ProjectProperties projectProp = ProjectProperties.load(resolvedPath, PropertyType.PROJECT);
        if (projectProp == null) {
            // legacy support for older file name.
            projectProp = ProjectProperties.load(resolvedPath, PropertyType.LEGACY_DEFAULT);
            if (projectProp == null) {
                mLog.error(null, "No %1$s at: %2$s", PropertyType.PROJECT.getFilename(),
                        resolvedPath);
                return;
            }
        }

        String targetHash = projectProp.getProperty(ProjectProperties.PROPERTY_TARGET);
        if (targetHash == null) {
            mLog.error(null, "%1$s in the main project has no target property.",
                    PropertyType.PROJECT.getFilename());
            return;
        }

        IAndroidTarget target = sdkManager.getTargetFromHashString(targetHash);
        if (target == null) {
            mLog.error(null, "Main project target %1$s is not a valid target.", targetHash);
            return;
        }

        // update test-project does not support the --name parameter, therefore the project
        // name should generally not be passed to updateProject().
        // However if build.xml does not exist then updateProject() will recreate it. In this
        // case we will need the project name.
        // To do this, we look for the parent project name and add "test" to it.
        // If the main project does not have a project name (yet), then the default behavior
        // will be used (look for activity and then folder name)
        String projectName = null;
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        File testBuildXml = new File(folderPath, SdkConstants.FN_BUILD_XML);
        if (testBuildXml.isFile() == false) {
            File mainBuildXml = new File(resolvedPath, SdkConstants.FN_BUILD_XML);
            if (mainBuildXml.isFile()) {
                try {
                    // get the name of the main project and add Test to it.
                    String mainProjectName = xpath.evaluate(XPATH_PROJECT_NAME,
                            new InputSource(new FileInputStream(mainBuildXml)));
                    projectName = mainProjectName + "Test";
                } catch (XPathExpressionException e) {
                    // it's ok, updateProject() will figure out a name automatically.
                    // We do log the error though as the build.xml file may be broken.
                    mLog.warning("Failed to parse %1$s.\n" +
                            "File may not be valid. Consider running 'android update project' on the main project.",
                            mainBuildXml.getPath());
                } catch (FileNotFoundException e) {
                    // should not happen since we check first.
                }
            }
        }

        // now update the project as if it's a normal project
        if (updateProject(folderPath, target, projectName, null /*libraryPath*/) == false) {
            // error message has already been displayed.
            return;
        }

        // add the test project specific properties.
        // At this point, we know build.prop has been renamed ant.prop
        ProjectProperties antProps = ProjectProperties.load(folderPath, PropertyType.ANT);
        ProjectPropertiesWorkingCopy antWorkingCopy;
        if (antProps == null) {
            antWorkingCopy = ProjectProperties.create(folderPath, PropertyType.ANT);
        } else {
            antWorkingCopy = antProps.makeWorkingCopy();
        }

        // set or replace the path to the main project
        antWorkingCopy.setProperty(ProjectProperties.PROPERTY_TESTED_PROJECT, pathToMainProject);
        try {
            antWorkingCopy.save();
            println("Updated %1$s", PropertyType.ANT.getFilename());
        } catch (Exception e) {
            mLog.error(e, "Failed to write %1$s file in '%2$s'",
                    PropertyType.ANT.getFilename(),
                    folderPath);
            return;
        }
    }

    /**
     * Checks whether the give <var>folderPath</var> is a valid project folder, and returns
     * a {@link FileWrapper} to the required file.
     * <p/>This checks that the folder exists and contains an AndroidManifest.xml file in it.
     * <p/>Any error are output using {@link #mLog}.
     * @param folderPath the folder to check
     * @param requiredFilename the file name of the file that's required.
     * @return a {@link FileWrapper} to the AndroidManifest.xml file, or null otherwise.
     */
    private FileWrapper checkProjectFolder(String folderPath, String requiredFilename) {
        // project folder must exist and be a directory, since this is an update
        FolderWrapper projectFolder = new FolderWrapper(folderPath);
        if (!projectFolder.isDirectory()) {
            mLog.error(null, "Project folder '%1$s' is not a valid directory.",
                    projectFolder);
            return null;
        }

        // Check AndroidManifest.xml is present
        FileWrapper requireFile = new FileWrapper(projectFolder, requiredFilename);
        if (!requireFile.isFile()) {
            mLog.error(null,
                    "%1$s is not a valid project (%2$s not found).",
                    folderPath, requiredFilename);
            return null;
        }

        return requireFile;
    }

    /**
     * Looks for a given regex in a file and returns the matcher if any line of the input file
     * contains the requested regexp.
     *
     * @param file the file to search.
     * @param regexp the regexp to search for.
     *
     * @return a Matcher or null if the regexp is not found.
     */
    private Matcher checkFileContainsRegexp(File file, String regexp) {
        Pattern p = Pattern.compile(regexp);

        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line;

            while ((line = in.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.find()) {
                    return m;
                }
            }

            in.close();
        } catch (Exception e) {
            // ignore
        }

        return null;
    }

    /**
     * Extracts a "full" package & activity name from an AndroidManifest.xml.
     * <p/>
     * The keywords dictionary is always filed the package name under the key {@link #PH_PACKAGE}.
     * If an activity name can be found, it is filed under the key {@link #PH_ACTIVITY_ENTRY_NAME}.
     * When no activity is found, this key is not created.
     *
     * @param manifestFile The AndroidManifest.xml file
     * @param outKeywords  Place where to put the out parameters: package and activity names.
     * @return True if the package/activity was parsed and updated in the keyword dictionary.
     */
    private boolean extractPackageFromManifest(File manifestFile,
            Map<String, String> outKeywords) {
        try {
            XPath xpath = AndroidXPathFactory.newXPath();

            InputSource source = new InputSource(new FileReader(manifestFile));
            String packageName = xpath.evaluate("/manifest/@package", source);

            source = new InputSource(new FileReader(manifestFile));

            // Select the "android:name" attribute of all <activity> nodes but only if they
            // contain a sub-node <intent-filter><action> with an "android:name" attribute which
            // is 'android.intent.action.MAIN' and an <intent-filter><category> with an
            // "android:name" attribute which is 'android.intent.category.LAUNCHER'
            String expression = String.format("/manifest/application/activity" +
                    "[intent-filter/action/@%1$s:name='android.intent.action.MAIN' and " +
                    "intent-filter/category/@%1$s:name='android.intent.category.LAUNCHER']" +
                    "/@%1$s:name", AndroidXPathFactory.DEFAULT_NS_PREFIX);

            NodeList activityNames = (NodeList) xpath.evaluate(expression, source,
                    XPathConstants.NODESET);

            // If we get here, both XPath expressions were valid so we're most likely dealing
            // with an actual AndroidManifest.xml file. The nodes may not have the requested
            // attributes though, if which case we should warn.

            if (packageName == null || packageName.length() == 0) {
                mLog.error(null,
                        "Missing <manifest package=\"...\"> in '%1$s'",
                        manifestFile.getName());
                return false;
            }

            // Get the first activity that matched earlier. If there is no activity,
            // activityName is set to an empty string and the generated "combined" name
            // will be in the form "package." (with a dot at the end).
            String activityName = "";
            if (activityNames.getLength() > 0) {
                activityName = activityNames.item(0).getNodeValue();
            }

            if (mLevel == OutputLevel.VERBOSE && activityNames.getLength() > 1) {
                println("WARNING: There is more than one activity defined in '%1$s'.\n" +
                        "Only the first one will be used. If this is not appropriate, you need\n" +
                        "to specify one of these values manually instead:",
                        manifestFile.getName());

                for (int i = 0; i < activityNames.getLength(); i++) {
                    String name = activityNames.item(i).getNodeValue();
                    name = combinePackageActivityNames(packageName, name);
                    println("- %1$s", name);
                }
            }

            if (activityName.length() == 0) {
                mLog.warning("Missing <activity %1$s:name=\"...\"> in '%2$s'.\n" +
                        "No activity will be generated.",
                        AndroidXPathFactory.DEFAULT_NS_PREFIX, manifestFile.getName());
            } else {
                outKeywords.put(PH_ACTIVITY_ENTRY_NAME, activityName);
            }

            outKeywords.put(PH_PACKAGE, packageName);
            return true;

        } catch (IOException e) {
            mLog.error(e, "Failed to read %1$s", manifestFile.getName());
        } catch (XPathExpressionException e) {
            Throwable t = e.getCause();
            mLog.error(t == null ? e : t,
                    "Failed to parse %1$s",
                    manifestFile.getName());
        }

        return false;
    }

    private String combinePackageActivityNames(String packageName, String activityName) {
        // Activity Name can have 3 forms:
        // - ".Name" means this is a class name in the given package name.
        //    The full FQCN is thus packageName + ".Name"
        // - "Name" is an older variant of the former. Full FQCN is packageName + "." + "Name"
        // - "com.blah.Name" is a full FQCN. Ignore packageName and use activityName as-is.
        //   To be valid, the package name should have at least two components. This is checked
        //   later during the creation of the build.xml file, so we just need to detect there's
        //   a dot but not at pos==0.

        int pos = activityName.indexOf('.');
        if (pos == 0) {
            return packageName + activityName;
        } else if (pos > 0) {
            return activityName;
        } else {
            return packageName + "." + activityName;
        }
    }

    /**
     * Installs a new file that is based on a template file provided by a given target.
     * Each match of each key from the place-holder map in the template will be replaced with its
     * corresponding value in the created file.
     *
     * @param templateName the name of to the template file
     * @param destFile the path to the destination file, relative to the project
     * @param placeholderMap a map of (place-holder, value) to create the file from the template.
     * @param target the Target of the project that will be providing the template.
     * @throws ProjectCreateException
     */
    private void installTargetTemplate(String templateName, File destFile,
            Map<String, String> placeholderMap, IAndroidTarget target)
            throws ProjectCreateException {
        // query the target for its template directory
        String templateFolder = target.getPath(IAndroidTarget.TEMPLATES);
        final String sourcePath = templateFolder + File.separator + templateName;

        installFullPathTemplate(sourcePath, destFile, placeholderMap);
    }

    /**
     * Installs a new file that is based on a template file provided by the tools folder.
     * Each match of each key from the place-holder map in the template will be replaced with its
     * corresponding value in the created file.
     *
     * @param templateName the name of to the template file
     * @param destFile the path to the destination file, relative to the project
     * @param placeholderMap a map of (place-holder, value) to create the file from the template.
     * @throws ProjectCreateException
     */
    private void installTemplate(String templateName, File destFile,
            Map<String, String> placeholderMap)
            throws ProjectCreateException {
        // query the target for its template directory
        String templateFolder = mSdkFolder + File.separator + SdkConstants.OS_SDK_TOOLS_LIB_FOLDER;
        final String sourcePath = templateFolder + File.separator + templateName;

        installFullPathTemplate(sourcePath, destFile, placeholderMap);
    }

    /**
     * Installs a new file that is based on a template.
     * Each match of each key from the place-holder map in the template will be replaced with its
     * corresponding value in the created file.
     *
     * @param sourcePath the full path to the source template file
     * @param destFile the destination file
     * @param placeholderMap a map of (place-holder, value) to create the file from the template.
     * @throws ProjectCreateException
     */
    private void installFullPathTemplate(String sourcePath, File destFile,
            Map<String, String> placeholderMap) throws ProjectCreateException {

        boolean existed = destFile.exists();

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(destFile));
            BufferedReader in = new BufferedReader(new FileReader(sourcePath));
            String line;

            while ((line = in.readLine()) != null) {
                if (placeholderMap != null) {
                    for (Map.Entry<String, String> entry : placeholderMap.entrySet()) {
                        line = line.replace(entry.getKey(), entry.getValue());
                    }
                }

                out.write(line);
                out.newLine();
            }

            out.close();
            in.close();
        } catch (Exception e) {
            throw new ProjectCreateException(e, "Could not access %1$s: %2$s",
                    destFile, e.getMessage());
        }

        println("%1$s file %2$s",
                existed ? "Updated" : "Added",
                destFile);
    }

    /**
     * Installs the project icons.
     * @param resourceFolder the resource folder
     * @param target the target of the project.
     * @return true if any icon was installed.
     */
    private boolean installIcons(File resourceFolder, IAndroidTarget target)
            throws ProjectCreateException {
        // query the target for its template directory
        String templateFolder = target.getPath(IAndroidTarget.TEMPLATES);

        boolean installedIcon = false;

        installedIcon |= installIcon(templateFolder, "ic_launcher_hdpi.png", resourceFolder,
                "drawable-hdpi");
        installedIcon |= installIcon(templateFolder, "ic_launcher_mdpi.png", resourceFolder,
                "drawable-mdpi");
        installedIcon |= installIcon(templateFolder, "ic_launcher_ldpi.png", resourceFolder,
                "drawable-ldpi");

        return installedIcon;
    }

    /**
     * Installs an Icon in the project.
     * @return true if the icon was installed.
     */
    private boolean installIcon(String templateFolder, String iconName, File resourceFolder,
            String folderName) throws ProjectCreateException {
        File icon = new File(templateFolder, iconName);
        if (icon.exists()) {
            File drawable = createDirs(resourceFolder, folderName);
            installBinaryFile(icon, new File(drawable, "ic_launcher.png"));
            return true;
        }

        return false;
    }

    /**
     * Installs a binary file
     * @param source the source file to copy
     * @param destination the destination file to write
     * @throws ProjectCreateException
     */
    private void installBinaryFile(File source, File destination) throws ProjectCreateException {
        byte[] buffer = new byte[8192];

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(source);
            fos = new FileOutputStream(destination);

            int read;
            while ((read = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }

        } catch (FileNotFoundException e) {
            // shouldn't happen since we check before.
        } catch (IOException e) {
            throw new ProjectCreateException(e, "Failed to read binary file: %1$s",
                    source.getAbsolutePath());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

    }

    /**
     * Prints a message unless silence is enabled.
     * <p/>
     * This is just a convenience wrapper around {@link ISdkLog#printf(String, Object...)} from
     * {@link #mLog} after testing if ouput level is {@link OutputLevel#VERBOSE}.
     *
     * @param format Format for String.format
     * @param args Arguments for String.format
     */
    private void println(String format, Object... args) {
        if (mLevel != OutputLevel.SILENT) {
            if (!format.endsWith("\n")) {
                format += "\n";
            }
            mLog.printf(format, args);
        }
    }

    /**
     * Creates a new folder, along with any parent folders that do not exists.
     *
     * @param parent the parent folder
     * @param name the name of the directory to create.
     * @throws ProjectCreateException
     */
    private File createDirs(File parent, String name) throws ProjectCreateException {
        final File newFolder = new File(parent, name);
        boolean existedBefore = true;

        if (!newFolder.exists()) {
            if (!newFolder.mkdirs()) {
                throw new ProjectCreateException("Could not create directory: %1$s", newFolder);
            }
            existedBefore = false;
        }

        if (newFolder.isDirectory()) {
            if (!newFolder.canWrite()) {
                throw new ProjectCreateException("Path is not writable: %1$s", newFolder);
            }
        } else {
            throw new ProjectCreateException("Path is not a directory: %1$s", newFolder);
        }

        if (!existedBefore) {
            try {
                println("Created directory %1$s", newFolder.getCanonicalPath());
            } catch (IOException e) {
                throw new ProjectCreateException(
                        "Could not determine canonical path of created directory", e);
            }
        }

        return newFolder;
    }

    /**
     * Strips the string of beginning and trailing characters (multiple
     * characters will be stripped, example stripString("..test...", '.')
     * results in "test";
     *
     * @param s the string to strip
     * @param strip the character to strip from beginning and end
     * @return the stripped string or the empty string if everything is stripped.
     */
    private static String stripString(String s, char strip) {
        final int sLen = s.length();
        int newStart = 0, newEnd = sLen - 1;

        while (newStart < sLen && s.charAt(newStart) == strip) {
          newStart++;
        }
        while (newEnd >= 0 && s.charAt(newEnd) == strip) {
          newEnd--;
        }

        /*
         * newEnd contains a char we want, and substring takes end as being
         * exclusive
         */
        newEnd++;

        if (newStart >= sLen || newEnd < 0) {
            return "";
        }

        return s.substring(newStart, newEnd);
    }
}
