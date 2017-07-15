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

package com.android.sdklib.internal.project;

import com.android.sdklib.SdkConstants;
import com.android.sdklib.internal.project.ProjectProperties.PropertyType;
import com.android.sdklib.io.IAbstractFile;
import com.android.sdklib.io.IAbstractFolder;
import com.android.sdklib.io.StreamException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

/**
 * A modifyable and saveable copy of a {@link ProjectProperties}.
 * <p/>This copy gives access to modification method such as {@link #setProperty(String, String)}
 * and {@link #removeProperty(String)}.
 *
 * To get access to an instance, use {@link ProjectProperties#makeWorkingCopy()} or
 * {@link ProjectProperties#create(IAbstractFolder, PropertyType)}.
 */
@SuppressWarnings("deprecation")
public class ProjectPropertiesWorkingCopy extends ProjectProperties {

    private final static Map<String, String> COMMENT_MAP = new HashMap<String, String>();
    static {
//               1-------10--------20--------30--------40--------50--------60--------70--------80
        COMMENT_MAP.put(PROPERTY_TARGET,
                "# Project target.\n");
        COMMENT_MAP.put(PROPERTY_SPLIT_BY_DENSITY,
                "# Indicates whether an apk should be generated for each density.\n");
        COMMENT_MAP.put(PROPERTY_SDK,
                "# location of the SDK. This is only used by Ant\n" +
                "# For customization when using a Version Control System, please read the\n" +
                "# header note.\n");
        COMMENT_MAP.put(PROPERTY_APP_PACKAGE,
                "# The name of your application package as defined in the manifest.\n" +
                "# Used by the 'uninstall' rule.\n");
        COMMENT_MAP.put(PROPERTY_PACKAGE,
                "# Package of the application being exported\n");
        COMMENT_MAP.put(PROPERTY_VERSIONCODE,
                "# Major version code\n");
        COMMENT_MAP.put(PROPERTY_PROJECTS,
                "# List of the Android projects being used for the export.\n" +
                "# The list is made of paths that are relative to this project,\n" +
                "# using forward-slash (/) as separator, and are separated by colons (:).\n");
    }


    /**
     * Sets a new properties. If a property with the same name already exists, it is replaced.
     * @param name the name of the property.
     * @param value the value of the property.
     */
    public synchronized void setProperty(String name, String value) {
        mProperties.put(name, value);
    }

    /**
     * Removes a property and returns its previous value (or null if the property did not exist).
     * @param name the name of the property to remove.
     */
    public synchronized String removeProperty(String name) {
        return mProperties.remove(name);
    }

    /**
     * Merges all properties from the given file into the current properties.
     * <p/>
     * This emulates the Ant behavior: existing properties are <em>not</em> overridden.
     * Only new undefined properties become defined.
     * <p/>
     * Typical usage:
     * <ul>
     * <li>Create a ProjectProperties with {@link PropertyType#BUILD}
     * <li>Merge in values using {@link PropertyType#DEFAULT}
     * <li>The result is that this contains all the properties from default plus those
     *     overridden by the build.properties file.
     * </ul>
     *
     * @param type One the possible {@link PropertyType}s.
     * @return this object, for chaining.
     */
    public synchronized ProjectPropertiesWorkingCopy merge(PropertyType type) {
        if (mProjectFolder.exists() && mType != type) {
            IAbstractFile propFile = mProjectFolder.getFile(type.getFilename());
            if (propFile.exists()) {
                Map<String, String> map = parsePropertyFile(propFile, null /* log */);
                if (map != null) {
                    for (Entry<String, String> entry : map.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        if (!mProperties.containsKey(key) && value != null) {
                            mProperties.put(key, value);
                        }
                    }
                }
            }
        }
        return this;
    }


    /**
     * Saves the property file, using UTF-8 encoding.
     * @throws IOException
     * @throws StreamException
     */
    public synchronized void save() throws IOException, StreamException {
        IAbstractFile toSave = mProjectFolder.getFile(mType.getFilename());

        // write the whole file in a byte array before dumping it in the file. This
        // This is so that if the file already existing
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, SdkConstants.INI_CHARSET);

        if (toSave.exists()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(toSave.getContents(),
                    SdkConstants.INI_CHARSET));

            // since we're reading the existing file and replacing values with new ones, or skipping
            // removed values, we need to record what properties have been visited, so that
            // we can figure later what new properties need to be added at the end of the file.
            HashSet<String> visitedProps = new HashSet<String>();

            String line = null;
            while ((line = reader.readLine()) != null) {
                // check if this is a line containing a property.
                if (line.length() > 0 && line.charAt(0) != '#') {

                    Matcher m = PATTERN_PROP.matcher(line);
                    if (m.matches()) {
                        String key = m.group(1);
                        String value = m.group(2);

                        // record the prop
                        visitedProps.add(key);

                        // check if the property still exists.
                        if (mProperties.containsKey(key)) {
                            // put the new value.
                            value = mProperties.get(key);
                        } else {
                            // property doesn't exist. Check if it's a known property.
                            // if it's a known one, we'll remove it, otherwise, leave it untouched.
                            if (mType.isKnownProperty(key)) {
                                value = null;
                            }
                        }

                        // if the value is still valid, write it down.
                        if (value != null) {
                            writeValue(writer, key, value, false /*addComment*/);
                        }
                    } else  {
                        // the line was wrong, let's just ignore it so that it's removed from the
                        // file.
                    }
                } else {
                    // non-property line: just write the line in the output as-is.
                    writer.append(line).append('\n');
                }
            }

            // now add the new properties.
            for (Entry<String, String> entry : mProperties.entrySet()) {
                if (visitedProps.contains(entry.getKey()) == false) {
                    String value = entry.getValue();
                    if (value != null) {
                        writeValue(writer, entry.getKey(), value, true /*addComment*/);
                    }
                }
            }

        } else {
            // new file, just write it all
            // write the header
            writer.write(mType.getHeader());

            // write the properties.
            for (Entry<String, String> entry : mProperties.entrySet()) {
                String value = entry.getValue();
                if (value != null) {
                    writeValue(writer, entry.getKey(), value, true /*addComment*/);
                }
            }
        }

        writer.flush();

        // now put the content in the file.
        OutputStream filestream = toSave.getOutputStream();
        filestream.write(baos.toByteArray());
        filestream.flush();
    }

    private void writeValue(OutputStreamWriter writer, String key, String value,
            boolean addComment) throws IOException {
        if (addComment) {
            String comment = COMMENT_MAP.get(key);
            if (comment != null) {
                writer.write(comment);
            }
        }

        value = value.replaceAll("\\\\", "\\\\\\\\");
        writer.write(String.format("%s=%s\n", key, value));
    }

    /**
     * Private constructor.
     * <p/>
     * Use {@link #load(String, PropertyType)} or {@link #create(String, PropertyType)}
     * to instantiate.
     */
    ProjectPropertiesWorkingCopy(IAbstractFolder projectFolder, Map<String, String> map,
            PropertyType type) {
        super(projectFolder, map, type);
    }

}
