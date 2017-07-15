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
import com.android.sdklib.internal.export.MultiApkExportHelper.ExportException;
import com.android.sdklib.internal.project.ApkSettings;
import com.android.sdklib.internal.project.ProjectProperties;
import com.android.sdklib.internal.project.ProjectProperties.PropertyType;
import com.android.sdklib.resources.Density;
import com.android.sdklib.xml.ManifestData;
import com.android.sdklib.xml.ManifestData.SupportsScreens;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class representing an Android project and its properties.
 *
 * Only the properties that pertain to the multi-apk export are present.
 */
public final class ProjectConfig {

    private static final String PROP_API = "api";
    private static final String PROP_SCREENS = "screens";
    private static final String PROP_GL = "gl";
    private static final String PROP_ABI = "splitByAbi";
    private static final String PROP_DENSITY = "splitByDensity";
    private static final String PROP_LOCALEFILTERS = "localeFilters";

    /**
     * List of densities and their associated aapt filter.
     */
    private static final String[][] DENSITY_LIST = new String[][] {
        new String[] { Density.HIGH.getResourceValue(),
                Density.HIGH.getResourceValue() + "," + Density.NODPI.getResourceValue() },
                new String[] { Density.MEDIUM.getResourceValue(),
                        Density.MEDIUM.getResourceValue() + "," +
                                Density.NODPI.getResourceValue() },
                        new String[] { Density.MEDIUM.getResourceValue(),
                Density.MEDIUM.getResourceValue() + "," + Density.NODPI.getResourceValue() },
    };

    private final File mProjectFolder;
    private final String mRelativePath;

    private final int mMinSdkVersion;
    private final int mGlEsVersion;
    private final SupportsScreens mSupportsScreens;
    private final boolean mSplitByAbi;
    private final boolean mSplitByDensity;
    private final Map<String, String> mLocaleFilters;
    /** List of ABIs not defined in the properties but actually existing in the project as valid
     * .so files */
    private final List<String> mAbis;

    static ProjectConfig create(File projectFolder, String relativePath,
            ManifestData manifestData) throws ExportException {
        // load the project properties
        ProjectProperties projectProp = ProjectProperties.load(projectFolder.getAbsolutePath(),
                PropertyType.DEFAULT);
        if (projectProp == null) {
            throw new ExportException(String.format("%1$s is missing for project %2$s",
                    PropertyType.DEFAULT.getFilename(), relativePath));
        }

        ApkSettings apkSettings = new ApkSettings(projectProp);

        return new ProjectConfig(projectFolder,
                relativePath,
                manifestData.getMinSdkVersion(),
                manifestData.getGlEsVersion(),
                manifestData.getSupportsScreensValues(),
                apkSettings.isSplitByAbi(),
                apkSettings.isSplitByDensity(),
                apkSettings.getLocaleFilters());
    }


    private ProjectConfig(File projectFolder, String relativePath,
            int minSdkVersion, int glEsVersion,
            SupportsScreens supportsScreens, boolean splitByAbi, boolean splitByDensity,
            Map<String, String> localeFilters) {
        mProjectFolder = projectFolder;
        mRelativePath = relativePath;
        mMinSdkVersion = minSdkVersion;
        mGlEsVersion = glEsVersion;
        mSupportsScreens = supportsScreens;
        mSplitByAbi = splitByAbi;
        mSplitByDensity = splitByDensity;
        mLocaleFilters = localeFilters;
        if (mSplitByAbi) {
            mAbis = findAbis();
        } else {
            mAbis = null;
        }
    }

    public File getProjectFolder() {
        return mProjectFolder;
    }


    public String getRelativePath() {
        return mRelativePath;
    }

    List<ApkData> getApkDataList() {
        // there are 3 cases:
        // 1. ABI split generate multiple apks with different build info, so they are different
        //    ApkData for all of them. Special case: split by abi but no native code => 1 ApkData.
        // 2. split by density or locale filters generate soft variant only, so they all go
        //    in the same ApkData.
        // 3. Both 1. and 2. means that more than one ApkData are created and they all get soft
        //    variants.

        ArrayList<ApkData> list = new ArrayList<ApkData>();

        Map<String, String> softVariants = computeSoftVariantMap();

        if (mSplitByAbi) {
            if (mAbis.size() > 0) {
                for (String abi : mAbis) {
                    list.add(new ApkData(this, abi, softVariants));
                }
            } else {
                // if there are no ABIs, then just generate a single ApkData with no specific ABI.
                list.add(new ApkData(this, softVariants));
            }
        } else {
            // create a single ApkData.
            list.add(new ApkData(this, softVariants));
        }

        return list;
    }

    int getMinSdkVersion() {
        return mMinSdkVersion;
    }

    SupportsScreens getSupportsScreens() {
        return mSupportsScreens;
    }

    int getGlEsVersion() {
        return mGlEsVersion;
    }

    boolean isSplitByDensity() {
        return mSplitByDensity;
    }

    boolean isSplitByAbi() {
        return mSplitByAbi;
    }

    /**
     * Returns a map of pair values (apk name suffix, aapt res filter) to be used to generate
     * multiple soft apk variants.
     */
    private Map<String, String> computeSoftVariantMap() {
        HashMap<String, String> map = new HashMap<String, String>();

        if (mSplitByDensity && mLocaleFilters.size() > 0) {
            for (String[] density : DENSITY_LIST) {
                for (Entry<String,String> entry : mLocaleFilters.entrySet()) {
                    map.put(density[0] + "-" + entry.getKey(),
                            density[1] + "," + entry.getValue());
                }
            }

        } else if (mSplitByDensity) {
            for (String[] density : DENSITY_LIST) {
                map.put(density[0], density[1]);
            }

        } else if (mLocaleFilters.size() > 0) {
            map.putAll(mLocaleFilters);

        }

        return map;
    }

    /**
     * Finds ABIs in a project folder. This is based on the presence of libs/<abi>/ folder.
     *
     * @param projectPath The OS path of the project.
     * @return A new non-null, possibly empty, list of ABI strings.
     */
    private List<String> findAbis() {
        ArrayList<String> abiList = new ArrayList<String>();
        File libs = new File(mProjectFolder, SdkConstants.FD_NATIVE_LIBS);
        if (libs.isDirectory()) {
            File[] abis = libs.listFiles();
            for (File abi : abis) {
                if (abi.isDirectory()) {
                    // only add the abi folder if there are .so files in it.
                    String[] content = abi.list(new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return name.toLowerCase().endsWith(".so");
                        }
                    });

                    if (content.length > 0) {
                        abiList.add(abi.getName());
                    }
                }
            }
        }

        return abiList;
    }

    String getConfigString(boolean onlyManifestData) {
        StringBuilder sb = new StringBuilder();
        LogHelper.write(sb, PROP_API, mMinSdkVersion);
        LogHelper.write(sb, PROP_SCREENS, mSupportsScreens.getEncodedValues());

        if (mGlEsVersion != ManifestData.GL_ES_VERSION_NOT_SET) {
            LogHelper.write(sb, PROP_GL, "0x" + Integer.toHexString(mGlEsVersion));
        }

        if (onlyManifestData == false) {
            if (mSplitByAbi) {
                // need to not only encode true, but also the list of ABIs that will be used when
                // the project is exported. This is because the hard property is not so much
                // whether an apk is generated per ABI, but *how many* of them (since they all take
                // a different build Info).
                StringBuilder value = new StringBuilder(Boolean.toString(true));
                for (String abi : mAbis) {
                    value.append('|').append(abi);
                }
                LogHelper.write(sb, PROP_ABI, value);
            } else {
                LogHelper.write(sb, PROP_ABI, false);
            }

            // in this case we're simply always going to make 3 versions (which may not make sense)
            // so the boolean is enough.
            LogHelper.write(sb, PROP_DENSITY, Boolean.toString(mSplitByDensity));

            if (mLocaleFilters.size() > 0) {
                LogHelper.write(sb, PROP_LOCALEFILTERS, ApkSettings.writeLocaleFilters(mLocaleFilters));
            }
        }

        return sb.toString();
    }

    /**
     * Compares the current project config to a list of properties.
     * These properties are in the format output by {@link #getConfigString()}.
     * @param values the properties to compare to.
     * @return null if the properties exactly match the current config, an error message otherwise
     */
    String compareToProperties(Map<String, String> values) {
        String tmp;
        // Note that most properties must always be present in the map.
        try {
            // api must always be there
            if (mMinSdkVersion != Integer.parseInt(values.get(PROP_API))) {
                return "Attribute minSdkVersion changed";
            }
        } catch (NumberFormatException e) {
            // failed to convert an integer? consider the configs not equal.
            return "Failed to convert attribute minSdkVersion to an Integer";
        }

        try {
            tmp = values.get(PROP_GL); // GL is optional in the config string.
            if (tmp != null) {
                if (mGlEsVersion != Integer.decode(tmp)) {
                    return "Attribute glEsVersion changed";
                }
            }
        } catch (NumberFormatException e) {
            // failed to convert an integer? consider the configs not equal.
            return "Failed to convert attribute glEsVersion to an Integer";
        }

        tmp = values.get(PROP_DENSITY);
        if (tmp == null || mSplitByDensity != Boolean.valueOf(tmp)) {
            return "Property split.density changed or is missing from config file";
        }

        // compare the ABI. If splitByAbi is true, then compares the ABIs present in the project
        // as they must match.
        tmp = values.get(PROP_ABI);
        if (tmp == null) {
            return "Property split.abi is missing from config file";
        }
        String[] abis = tmp.split("\\|");
        if (mSplitByAbi != Boolean.valueOf(abis[0])) { // first value is always the split boolean
            return "Property split.abi changed";
        }
        // now compare the rest if needed.
        if (mSplitByAbi) {
            if (abis.length - 1 != mAbis.size()) {
                return "The number of ABIs available in the project changed";
            }
            for (int i = 1 ; i < abis.length ; i++) {
                if (mAbis.indexOf(abis[i]) == -1) {
                    return "The list of ABIs available in the project changed";
                }
            }
        }

        tmp = values.get(PROP_SCREENS);
        if (tmp != null) {
            SupportsScreens supportsScreens = new SupportsScreens(tmp);
            if (supportsScreens.equals(mSupportsScreens) == false) {
                return "Supports-Screens value changed";
            }
        } else {
            return "Supports-screens value missing from config file";
        }

        tmp = values.get(PROP_LOCALEFILTERS);
        if (tmp != null) {
            if (mLocaleFilters.equals(ApkSettings.readLocaleFilters(tmp)) == false) {
                return "Locale resource filter changed";
            }
        } else {
            // do nothing. locale filter is optional in the config string.
        }

        return null;
    }
}
