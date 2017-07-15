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

import com.android.sdklib.xml.ManifestData;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representing one apk (or more if there are soft variants) that needs to be generated.
 * This contains a link to the project used for the export, and which extra filters should be used.
 *
 * This class is meant to be sortable in a way that allows generation of the buildInfo
 * value that goes in the composite versionCode.
 */
public final class ApkData implements Comparable<ApkData> {

    private static final String PROP_PROJECT = "project";
    private static final String PROP_BUILDINFO = "buildInfo";
    private static final String PROP_MINOR = "minorCode";
    private static final String PROP_ABI = "abi";
    private static final String PROP_RESOURCES = "resources";

    /**
     * List of ABI order.
     * This is meant to be a list of CPU/CPU2 to indicate the order required by the build info.
     * If the ABIs being compared in {@link #compareTo(ApkData)} are in the same String array,
     * then the value returned must ensure that the {@link ApkData} will ordered the same as the
     * array.
     * If the ABIs are not in the same array, any order can be returned.
     */
    private static final String[][] ABI_SORTING = new String[][] {
        new String[] { "armeabi", "armeabi-v7a" }
    };

    private final ProjectConfig mProjectConfig;
    private final HashMap<String, String> mOutputNames = new HashMap<String, String>();
    private int mBuildInfo;
    private int mMinorCode;

    // the following are used to sort the export data and generate buildInfo
    private final String mAbi;
    private final Map<String, String> mSoftVariantMap = new HashMap<String, String>();

    ApkData(ProjectConfig projectConfig, String abi, Map<String, String> softVariants) {
        mProjectConfig = projectConfig;
        mAbi = abi;
        if (softVariants != null) {
            mSoftVariantMap.putAll(softVariants);
        }
    }

    ApkData(ProjectConfig projectConfig, String abi) {
        this(projectConfig, abi, null /*softVariants*/);
    }

    ApkData(ProjectConfig projectConfig, Map<String, String> softVariants) {
        this(projectConfig, null /*abi*/, softVariants);
    }

    ApkData(ProjectConfig projectConfig) {
        this(projectConfig, null /*abi*/, null /*softVariants*/);
    }

    public ProjectConfig getProjectConfig() {
        return mProjectConfig;
    }

    public String getOutputName(String key) {
        return mOutputNames.get(key);
    }

    public void setOutputName(String key, String outputName) {
        mOutputNames.put(key, outputName);
    }

    public int getBuildInfo() {
        return mBuildInfo;
    }

    void setBuildInfo(int buildInfo) {
        mBuildInfo = buildInfo;
    }

    public int getMinorCode() {
        return mMinorCode;
    }

    void setMinorCode(int minor) {
        mMinorCode = minor;
    }

    public String getAbi() {
        return mAbi;
    }

    public Map<String, String> getSoftVariantMap() {
        return mSoftVariantMap;
    }

    /**
     * Computes and returns the composite version code
     * @param versionCode the major version code.
     * @return the composite versionCode to be used in the manifest.
     */
    public int getCompositeVersionCode(int versionCode) {
        int trueVersionCode = versionCode * MultiApkExportHelper.OFFSET_VERSION_CODE;
        trueVersionCode += getBuildInfo() * MultiApkExportHelper.OFFSET_BUILD_INFO;
        trueVersionCode += getMinorCode();

        return trueVersionCode;
    }

    @Override
    public String toString() {
        return getLogLine(null);
    }

    public String getLogLine(String key) {
        StringBuilder sb = new StringBuilder();
        sb.append(getOutputName(key)).append(':');

        LogHelper.write(sb, PROP_BUILDINFO, mBuildInfo);
        LogHelper.write(sb, PROP_MINOR, mMinorCode);
        LogHelper.write(sb, PROP_PROJECT, mProjectConfig.getRelativePath());
        sb.append(mProjectConfig.getConfigString(true /*onlyManifestData*/));

        if (mAbi != null) {
            LogHelper.write(sb, PROP_ABI, mAbi);
        }

        String filter = mSoftVariantMap.get(key);
        if (filter != null) {
            LogHelper.write(sb, PROP_RESOURCES, filter);
        }

        return sb.toString();
    }

    public int compareTo(ApkData o) {
        // compare only the hard properties, and in a specific order:

        // 1. minSdkVersion
        int minSdkDiff = mProjectConfig.getMinSdkVersion() - o.mProjectConfig.getMinSdkVersion();
        if (minSdkDiff != 0) {
            return minSdkDiff;
        }

        // 2. <supports-screens>
        // only compare if they have don't have the same size support. This is because
        // this compare method throws an exception if the values cannot be compared.
        if (mProjectConfig.getSupportsScreens().hasSameScreenSupportAs(
                o.mProjectConfig.getSupportsScreens()) == false) {
            return mProjectConfig.getSupportsScreens().compareScreenSizesWith(
                    o.mProjectConfig.getSupportsScreens());
        }

        // 3. glEsVersion
        int comp;
        if (mProjectConfig.getGlEsVersion() != ManifestData.GL_ES_VERSION_NOT_SET) {
            if (o.mProjectConfig.getGlEsVersion() != ManifestData.GL_ES_VERSION_NOT_SET) {
                comp = mProjectConfig.getGlEsVersion() - o.mProjectConfig.getGlEsVersion();
                if (comp != 0) return comp;
            } else {
                return -1;
            }
        } else if (o.mProjectConfig.getGlEsVersion() != ManifestData.GL_ES_VERSION_NOT_SET) {
            return 1;
        }

        // 4. ABI
        // here the returned value is only important if both abi are non null.
        if (mAbi != null && o.mAbi != null) {
            comp = compareAbi(mAbi, o.mAbi);
            if (comp != 0) return comp;
        }

        return 0;
    }

    private int compareAbi(String abi, String abi2) {
        // look for the abis in each of the ABI sorting array
        for (String[] abiArray : ABI_SORTING) {
            int abiIndex = -1, abiIndex2 = -1;
            final int count = abiArray.length;
            for (int i = 0 ; i < count ; i++) {
                if (abiArray[i].equals(abi)) {
                    abiIndex = i;
                }
                if (abiArray[i].equals(abi2)) {
                    abiIndex2 = i;
                }
            }

            // if both were found
            if (abiIndex != -1 && abiIndex != -1) {
                return abiIndex - abiIndex2;
            }
        }

        return 0;
    }
}
