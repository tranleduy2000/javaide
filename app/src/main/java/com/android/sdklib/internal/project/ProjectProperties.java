/*
 * Copyright (C) 2008 The Android Open Source Project
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

import com.android.sdklib.ISdkLog;
import com.android.sdklib.SdkConstants;
import com.android.sdklib.io.FileWrapper;
import com.android.sdklib.io.FolderWrapper;
import com.android.sdklib.io.IAbstractFile;
import com.android.sdklib.io.IAbstractFolder;
import com.android.sdklib.io.StreamException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class representing project properties for both ADT and Ant-based build.
 * <p/>The class is associated to a {@link PropertyType} that indicate which of the project
 * property file is represented.
 * <p/>To load an existing file, use {@link #load(IAbstractFolder, PropertyType)}.
 * <p/>The class is meant to be always in sync (or at least not newer) than the file it represents.
 * Once created, it can only be updated through {@link #reload()}
 *
 * <p/>The make modification or make new file, use a {@link ProjectPropertiesWorkingCopy} instance,
 * either through {@link #create(IAbstractFolder, PropertyType)} or through
 * {@link #makeWorkingCopy()}.
 *
 */
public class ProjectProperties {
    protected final static Pattern PATTERN_PROP = Pattern.compile(
    "^([a-zA-Z0-9._-]+)\\s*=\\s*(.*)\\s*$");

    /** The property name for the project target */
    public final static String PROPERTY_TARGET = "target";

    public final static String PROPERTY_LIBRARY = "android.library";
    public final static String PROPERTY_LIB_REF = "android.library.reference.";
    private final static String PROPERTY_LIB_REF_REGEX = "android.library.reference.\\d+";

    public final static String PROPERTY_SDK = "sdk.dir";
    // LEGACY - compatibility with 1.6 and before
    public final static String PROPERTY_SDK_LEGACY = "sdk-location";

    @Deprecated //This is not needed by the new Ant rules
    public final static String PROPERTY_APP_PACKAGE = "application.package";

    public final static String PROPERTY_SPLIT_BY_DENSITY = "split.density";
    public final static String PROPERTY_SPLIT_BY_ABI = "split.abi";
    public final static String PROPERTY_SPLIT_BY_LOCALE = "split.locale";

    public final static String PROPERTY_TESTED_PROJECT = "tested.project.dir";

    public final static String PROPERTY_BUILD_SOURCE_DIR = "source.dir";
    public final static String PROPERTY_BUILD_OUT_DIR = "out.dir";

    public final static String PROPERTY_PACKAGE = "package";
    public final static String PROPERTY_VERSIONCODE = "versionCode";
    public final static String PROPERTY_PROJECTS = "projects";
    public final static String PROPERTY_KEY_STORE = "key.store";
    public final static String PROPERTY_KEY_ALIAS = "key.alias";

    public static enum PropertyType {
        BUILD(SdkConstants.FN_BUILD_PROPERTIES, BUILD_HEADER, new String[] {
                PROPERTY_BUILD_SOURCE_DIR, PROPERTY_BUILD_OUT_DIR
            }),
        DEFAULT(SdkConstants.FN_DEFAULT_PROPERTIES, DEFAULT_HEADER, new String[] {
                PROPERTY_TARGET, PROPERTY_LIBRARY, PROPERTY_LIB_REF_REGEX,
                PROPERTY_KEY_STORE, PROPERTY_KEY_ALIAS
            }),
        LOCAL(SdkConstants.FN_LOCAL_PROPERTIES, LOCAL_HEADER, new String[] {
                PROPERTY_SDK
            }),
        EXPORT(SdkConstants.FN_EXPORT_PROPERTIES, EXPORT_HEADER, new String[] {
                PROPERTY_PACKAGE, PROPERTY_VERSIONCODE, PROPERTY_PROJECTS,
                PROPERTY_KEY_STORE, PROPERTY_KEY_ALIAS
            });

        private final String mFilename;
        private final String mHeader;
        private final Set<String> mKnownProps;

        PropertyType(String filename, String header, String[] validProps) {
            mFilename = filename;
            mHeader = header;
            HashSet<String> s = new HashSet<String>();
            s.addAll(Arrays.asList(validProps));
            mKnownProps = Collections.unmodifiableSet(s);
        }

        public String getFilename() {
            return mFilename;
        }

        public String getHeader() {
            return mHeader;
        }

        /**
         * Returns whether a given property is known for the property type.
         */
        public boolean isKnownProperty(String name) {
            for (String propRegex : mKnownProps) {
                if (propRegex.equals(name) || Pattern.matches(propRegex, name)) {
                    return true;
                }
            }

            return false;
        }
    }

    private final static String LOCAL_HEADER =
//           1-------10--------20--------30--------40--------50--------60--------70--------80
            "# This file is automatically generated by Android Tools.\n" +
            "# Do not modify this file -- YOUR CHANGES WILL BE ERASED!\n" +
            "# \n" +
            "# This file must *NOT* be checked in Version Control Systems,\n" +
            "# as it contains information specific to your local configuration.\n" +
            "\n";

    private final static String DEFAULT_HEADER =
//          1-------10--------20--------30--------40--------50--------60--------70--------80
           "# This file is automatically generated by Android Tools.\n" +
           "# Do not modify this file -- YOUR CHANGES WILL BE ERASED!\n" +
           "# \n" +
           "# This file must be checked in Version Control Systems.\n" +
           "# \n" +
           "# To customize properties used by the Ant build system use,\n" +
           "# \"build.properties\", and override values to adapt the script to your\n" +
           "# project structure.\n" +
           "\n";

    private final static String BUILD_HEADER =
//          1-------10--------20--------30--------40--------50--------60--------70--------80
           "# This file is used to override default values used by the Ant build system.\n" +
           "# \n" +
           "# This file must be checked in Version Control Systems, as it is\n" +
           "# integral to the build system of your project.\n" +
           "\n" +
           "# This file is only used by the Ant script.\n" +
           "\n" +
           "# You can use this to override default values such as\n" +
           "#  'source.dir' for the location of your java source folder and\n" +
           "#  'out.dir' for the location of your output folder.\n" +
           "\n" +
           "# You can also use it define how the release builds are signed by declaring\n" +
           "# the following properties:\n" +
           "#  'key.store' for the location of your keystore and\n" +
           "#  'key.alias' for the name of the key to use.\n" +
           "# The password will be asked during the build when you use the 'release' target.\n" +
           "\n";

    private final static String EXPORT_HEADER =
//          1-------10--------20--------30--------40--------50--------60--------70--------80
           "# Export properties\n" +
           "# \n" +
           "# This file must be checked in Version Control Systems.\n" +
           "\n" +
           "# The main content for this file is:\n" +
           "# - package name for the application being export\n" +
           "# - list of the projects being export\n" +
           "# - version code for the application\n" +
           "\n" +
           "# You can also use it define how the release builds are signed by declaring\n" +
           "# the following properties:\n" +
           "#  'key.store' for the location of your keystore and\n" +
           "#  'key.alias' for the name of the key alias to use.\n" +
           "# The password will be asked during the build when you use the 'release' target.\n" +
           "\n";

    protected final IAbstractFolder mProjectFolder;
    protected final Map<String, String> mProperties;
    protected final PropertyType mType;

    /**
     * Loads a project properties file and return a {@link ProjectProperties} object
     * containing the properties
     *
     * @param projectFolderOsPath the project folder.
     * @param type One the possible {@link PropertyType}s.
     */
    public static ProjectProperties load(String projectFolderOsPath, PropertyType type) {
        IAbstractFolder wrapper = new FolderWrapper(projectFolderOsPath);
        return load(wrapper, type);
    }

    /**
     * Loads a project properties file and return a {@link ProjectProperties} object
     * containing the properties
     *
     * @param projectFolder the project folder.
     * @param type One the possible {@link PropertyType}s.
     */
    public static ProjectProperties load(IAbstractFolder projectFolder, PropertyType type) {
        if (projectFolder.exists()) {
            IAbstractFile propFile = projectFolder.getFile(type.mFilename);
            if (propFile.exists()) {
                Map<String, String> map = parsePropertyFile(propFile, null /* log */);
                if (map != null) {
                    return new ProjectProperties(projectFolder, map, type);
                }
            }
        }
        return null;
    }

    /**
     * Creates a new project properties object, with no properties.
     * <p/>The file is not created until {@link ProjectPropertiesWorkingCopy#save()} is called.
     * @param projectFolderOsPath the project folder.
     * @param type the type of property file to create
     */
    public static ProjectPropertiesWorkingCopy create(String projectFolderOsPath,
            PropertyType type) {
        // create and return a ProjectProperties with an empty map.
        IAbstractFolder folder = new FolderWrapper(projectFolderOsPath);
        return create(folder, type);
    }

    /**
     * Creates a new project properties object, with no properties.
     * <p/>The file is not created until {@link ProjectPropertiesWorkingCopy#save()} is called.
     * @param projectFolder the project folder.
     * @param type the type of property file to create
     */
    public static ProjectPropertiesWorkingCopy create(IAbstractFolder projectFolder,
            PropertyType type) {
        // create and return a ProjectProperties with an empty map.
        return new ProjectPropertiesWorkingCopy(projectFolder, new HashMap<String, String>(), type);
    }

    /**
     * Creates and returns a copy of the current properties as a
     * {@link ProjectPropertiesWorkingCopy} that can be modified and saved.
     * @return a new instance of {@link ProjectPropertiesWorkingCopy}
     */
    public ProjectPropertiesWorkingCopy makeWorkingCopy() {
        // copy the current properties in a new map
        HashMap<String, String> propList = new HashMap<String, String>();
        propList.putAll(mProperties);

        return new ProjectPropertiesWorkingCopy(mProjectFolder, propList, mType);
    }

    /**
     * Returns the type of the property file.
     *
     * @see PropertyType
     */
    public PropertyType getType() {
        return mType;
    }

    /**
     * Returns the value of a property.
     * @param name the name of the property.
     * @return the property value or null if the property is not set.
     */
    public synchronized String getProperty(String name) {
        return mProperties.get(name);
    }

    /**
     * Returns a set of the property keys. Unlike {@link Map#keySet()} this is not a view of the
     * map keys. Modifying the returned {@link Set} will not impact the underlying {@link Map}.
     */
    public synchronized Set<String> keySet() {
        return new HashSet<String>(mProperties.keySet());
    }

    /**
     * Reloads the properties from the underlying file.
     */
    public synchronized void reload() {
        if (mProjectFolder.exists()) {
            IAbstractFile propFile = mProjectFolder.getFile(mType.mFilename);
            if (propFile.exists()) {
                Map<String, String> map = parsePropertyFile(propFile, null /* log */);
                if (map != null) {
                    mProperties.clear();
                    mProperties.putAll(map);
                }
            }
        }
    }

    /**
     * Parses a property file (using UTF-8 encoding) and returns a map of the content.
     * <p/>If the file is not present, null is returned with no error messages sent to the log.
     *
     * @param propFile the property file to parse
     * @param log the ISdkLog object receiving warning/error from the parsing. Cannot be null.
     * @return the map of (key,value) pairs, or null if the parsing failed.
     * @deprecated Use {@link #parsePropertyFile(IAbstractFile, ISdkLog)}
     */
    public static Map<String, String> parsePropertyFile(File propFile, ISdkLog log) {
        IAbstractFile wrapper = new FileWrapper(propFile);
        return parsePropertyFile(wrapper, log);
    }

    /**
     * Parses a property file (using UTF-8 encoding) and returns a map of the content.
     * <p/>If the file is not present, null is returned with no error messages sent to the log.
     *
     * @param propFile the property file to parse
     * @param log the ISdkLog object receiving warning/error from the parsing. Cannot be null.
     * @return the map of (key,value) pairs, or null if the parsing failed.
     */
    public static Map<String, String> parsePropertyFile(IAbstractFile propFile, ISdkLog log) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(propFile.getContents(),
                    SdkConstants.INI_CHARSET));

            String line = null;
            Map<String, String> map = new HashMap<String, String>();
            while ((line = reader.readLine()) != null) {
                if (line.length() > 0 && line.charAt(0) != '#') {

                    Matcher m = PATTERN_PROP.matcher(line);
                    if (m.matches()) {
                        map.put(m.group(1), m.group(2));
                    } else {
                        log.warning("Error parsing '%1$s': \"%2$s\" is not a valid syntax",
                                propFile.getOsLocation(),
                                line);
                        return null;
                    }
                }
            }

            return map;
        } catch (FileNotFoundException e) {
            // this should not happen since we usually test the file existence before
            // calling the method.
            // Return null below.
        } catch (IOException e) {
            log.warning("Error parsing '%1$s': %2$s.",
                    propFile.getOsLocation(),
                    e.getMessage());
        } catch (StreamException e) {
            log.warning("Error parsing '%1$s': %2$s.",
                    propFile.getOsLocation(),
                    e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // pass
                }
            }
        }

        return null;
    }


    /**
     * Private constructor.
     * <p/>
     * Use {@link #load(String, PropertyType)} or {@link #create(String, PropertyType)}
     * to instantiate.
     */
    protected ProjectProperties(IAbstractFolder projectFolder, Map<String, String> map,
            PropertyType type) {
        mProjectFolder = projectFolder;
        mProperties = map;
        mType = type;
    }
}
