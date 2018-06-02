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

package com.android.sdklib.internal.avd;

import com.android.utils.ILogger;
import com.google.common.base.Charsets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HardwareProperties {
    /** AVD/config.ini key for whether hardware buttons are present. */
    public static final String HW_MAINKEYS = "hw.mainKeys";

    /** AVD/config.ini key indicating whether trackball is present. */
    public static final String HW_TRACKBALL = "hw.trackBall";

    /** AVD/config.ini key indicating whether qwerty keyboard is present. */
    public static final String HW_KEYBOARD = "hw.keyboard";

    /** AVD/config.ini key indicating whether dpad is present. */
    public static final String HW_DPAD = "hw.dPad";

    /** AVD/config.ini key indicating whether gps is present. */
    public static final String HW_GPS = "hw.gps";

    /** AVD/config.ini key indicating whether the device is running on battery. */
    public static final String HW_BATTERY = "hw.battery";

    /** AVD/config.ini key indicating whether accelerometer is present. */
    public static final String HW_ACCELEROMETER = "hw.accelerometer";

    /** AVD/config.ini key indicating whether gyroscope is present. */
    public static final String HW_ORIENTATION_SENSOR = "hw.sensors.orientation";

    /** AVD/config.ini key indicating whether h/w mic is present. */
    public static final String HW_AUDIO_INPUT = "hw.audioInput";

    /** AVD/config.ini key indicating whether sdcard is present. */
    public static final String HW_SDCARD = "hw.sdCard";

    /** AVD/config.ini key for LCD density. */
    public static final String HW_LCD_DENSITY = "hw.lcd.density";

    /** AVD/config.ini key indicating whether proximity sensor present. */
    public static final String HW_PROXIMITY_SENSOR = "hw.sensors.proximity";

    /** AVD/config.ini key for initial device orientation. */
    public static final String HW_INITIAL_ORIENTATION = "hw.initialOrientation";


    private static final Pattern PATTERN_PROP = Pattern.compile(
    "^([a-zA-Z0-9._-]+)\\s*=\\s*(.*)\\s*$");

    /** Property name in the generated avd config file; String; e.g. "hw.screen" */
    private static final String HW_PROP_NAME = "name";              //$NON-NLS-1$
    /** Property type, one of {@link HardwarePropertyType} */
    private static final String HW_PROP_TYPE = "type";              //$NON-NLS-1$
    /** Default value of the property. String matching the property type. */
    private static final String HW_PROP_DEFAULT = "default";        //$NON-NLS-1$
    /** User-visible name of the property. String. */
    private static final String HW_PROP_ABSTRACT = "abstract";      //$NON-NLS-1$
    /** User-visible description of the property. String. */
    private static final String HW_PROP_DESC = "description";       //$NON-NLS-1$
    /** Comma-separate values for a property of type "enum" */
    private static final String HW_PROP_ENUM = "enum";              //$NON-NLS-1$

    public static final String BOOLEAN_YES = "yes";
    public static final String BOOLEAN_NO = "no";
    public static final Pattern DISKSIZE_PATTERN = Pattern.compile("\\d+[MK]B"); //$NON-NLS-1$

    /** Represents the type of a hardware property value. */
    public enum HardwarePropertyType {
        INTEGER     ("integer",  false /*isEnum*/),     //$NON-NLS-1$
        BOOLEAN     ("boolean",  false /*isEnum*/),     //$NON-NLS-1$
        DISKSIZE    ("diskSize", false /*isEnum*/),     //$NON-NLS-1$
        STRING      ("string",   false /*isEnum*/),     //$NON-NLS-1$
        INTEGER_ENUM("integer",  true  /*isEnum*/),     //$NON-NLS-1$
        STRING_ENUM ("string",   true  /*isEnum*/);     //$NON-NLS-1$


        private String mName;
        private boolean mIsEnum;

        HardwarePropertyType(String name, boolean isEnum) {
            mName = name;
            mIsEnum = isEnum;
        }

        /** Returns the name of the type (e.g. "string", "boolean", etc.) */
        public String getName() {
            return mName;
        }

        /** Indicates whether this type is an enum (e.g. "enum of strings"). */
        public boolean isEnum() {
            return mIsEnum;
        }

        /** Returns the internal HardwarePropertyType object matching the given type name. */
        public static HardwarePropertyType getEnum(String name, boolean isEnum) {
            for (HardwarePropertyType type : values()) {
                if (type.mName.equals(name) && type.mIsEnum == isEnum) {
                    return type;
                }
            }

            return null;
        }
    }

    public static final class HardwareProperty {
        private String mName;
        private HardwarePropertyType mType;
        /** the string representation of the default value. can be null. */
        private String mDefault;
        /** the choices for an enum. Null if not an enum. */
        private String[] mEnum;
        private String mAbstract;
        private String mDescription;

        public HardwareProperty() {
            // initialize strings to sane defaults, as not all properties will be set from
            // the ini file
            mName = "";
            mDefault = "";
            mAbstract = "";
            mDescription = "";
        }

        /** Returns the hardware config name of the property, e.g. "hw.screen" */
        public String getName() {
            return mName;
        }

        /** Returns the property type, one of {@link HardwarePropertyType} */
        public HardwarePropertyType getType() {
            return mType;
        }

        /**
         * Returns the default value of the property.
         * String matching the property type.
         * Can be null.
         */
        public String getDefault() {
            return mDefault;
        }

        /** Returns the user-visible name of the property. */
        public String getAbstract() {
            return mAbstract;
        }

        /** Returns the user-visible description of the property. */
        public String getDescription() {
            return mDescription;
        }

        /** Returns the possible values for an enum property. Can be null. */
        public String[] getEnum() {
            return mEnum;
        }

        public boolean isValidForUi() {
            // don't display single string type for now.
            return mType != HardwarePropertyType.STRING || mType.isEnum();
        }
    }

    /**
     * Parses the hardware definition file.
     * @param file the property file to parse
     * @param log the ILogger object receiving warning/error from the parsing. Cannot be null.
     * @return the map of (key,value) pairs, or null if the parsing failed.
     */
    public static Map<String, HardwareProperty> parseHardwareDefinitions(File file, ILogger log) {
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis, Charsets.UTF_8));

            Map<String, HardwareProperty> map = new TreeMap<String, HardwareProperty>();

            String line = null;
            HardwareProperty prop = null;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty() && line.charAt(0) != '#') {
                    Matcher m = PATTERN_PROP.matcher(line);
                    if (m.matches()) {
                        String key = m.group(1);
                        String value = m.group(2);

                        if (HW_PROP_NAME.equals(key)) {
                            prop = new HardwareProperty();
                            prop.mName = value;
                            map.put(prop.mName, prop);
                        }

                        if (prop == null) {
                            log.warning("Error parsing '%1$s': missing '%2$s'",
                                    file.getAbsolutePath(), HW_PROP_NAME);
                            return null;
                        }

                        if (HW_PROP_TYPE.equals(key)) {
                            // Note: we don't know yet whether this type is an enum.
                            // This is indicated by the "enum = value" line that is parsed later.
                            prop.mType = HardwarePropertyType.getEnum(value, false);
                            assert (prop.mType != null);
                        } else if (HW_PROP_DEFAULT.equals(key)) {
                            prop.mDefault = value;
                        } else if (HW_PROP_ABSTRACT.equals(key)) {
                            prop.mAbstract = value;
                        } else if (HW_PROP_DESC.equals(key)) {
                            prop.mDescription = value;
                        } else if (HW_PROP_ENUM.equals(key)) {
                            if (!prop.mType.isEnum()) {
                                // Change the type to an enum, if valid.
                                prop.mType = HardwarePropertyType.getEnum(prop.mType.getName(),
                                                                          true);
                                assert (prop.mType != null);
                            }

                            // Sanitize input: trim spaces, ignore empty entries.
                            String[] v = value.split(",");
                            int n = 0;
                            for (int i = 0; i < v.length; i++) {
                                String s = v[i] = v[i].trim();
                                if (!s.isEmpty()) {
                                    n++;
                                }
                            }
                            prop.mEnum = new String[n];
                            n = 0;
                            for (int i = 0; i < v.length; i++) {
                                String s = v[i];
                                if (!s.isEmpty()) {
                                    prop.mEnum[n++] = s;
                                }
                            }
                        }
                    } else {
                        log.warning("Error parsing '%1$s': \"%2$s\" is not a valid syntax",
                                file.getAbsolutePath(), line);
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
            log.warning("Error parsing '%1$s': %2$s.", file.getAbsolutePath(),
                        e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        return null;
    }

    /**
     * Returns the boolean value matching the given index.
     * This is the reverse of {@link #getBooleanValueIndex(String)}.
     *
     * @param index 0 or 1.
     * @return {@link #BOOLEAN_YES} for 0 or {@link #BOOLEAN_NO} for 1.
     * @throws IndexOutOfBoundsException if index is neither 0 nor 1.
     */
    public static String getBooleanValue(int index) {
        if (index == 0) {
            return BOOLEAN_YES;
        } else if (index == 1) {
            return BOOLEAN_NO;
        }
        throw new IndexOutOfBoundsException("HardwareProperty boolean index must 0 (true) or 1 (false) but was " + index);
    }

    /**
     * Returns the index of a boolean <var>value</var>.
     * This if the reverse of {@link #getBooleanValue(int)}.
     *
     * @param value Either {@link #BOOLEAN_YES} or {@link #BOOLEAN_NO}.
     * @return 0 for {@link #BOOLEAN_YES}, 1 for {@link #BOOLEAN_NO} or -1 for anything else.
     */
    public static int getBooleanValueIndex(String value) {
        if (BOOLEAN_YES.equals(value)) {
            return 0;
        } else if (BOOLEAN_NO.equals(value)) {
            return 1;
        }

        return -1;
    }
}
