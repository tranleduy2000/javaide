/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.sdklib;

import static com.android.SdkConstants.FD_LIB;
import static com.android.SdkConstants.FN_AAPT;
import static com.android.SdkConstants.FN_AIDL;
import static com.android.SdkConstants.FN_BCC_COMPAT;
import static com.android.SdkConstants.FN_DEXDUMP;
import static com.android.SdkConstants.FN_DX;
import static com.android.SdkConstants.FN_DX_JAR;
import static com.android.SdkConstants.FN_JACK;
import static com.android.SdkConstants.FN_JILL;
import static com.android.SdkConstants.FN_LD_ARM;
import static com.android.SdkConstants.FN_LD_MIPS;
import static com.android.SdkConstants.FN_LD_X86;
import static com.android.SdkConstants.FN_RENDERSCRIPT;
import static com.android.SdkConstants.FN_SPLIT_SELECT;
import static com.android.SdkConstants.FN_ZIPALIGN;
import static com.android.SdkConstants.OS_FRAMEWORK_RS;
import static com.android.SdkConstants.OS_FRAMEWORK_RS_CLANG;
import static com.android.sdklib.BuildToolInfo.PathId.AAPT;
import static com.android.sdklib.BuildToolInfo.PathId.AIDL;
import static com.android.sdklib.BuildToolInfo.PathId.ANDROID_RS;
import static com.android.sdklib.BuildToolInfo.PathId.ANDROID_RS_CLANG;
import static com.android.sdklib.BuildToolInfo.PathId.BCC_COMPAT;
import static com.android.sdklib.BuildToolInfo.PathId.DEXDUMP;
import static com.android.sdklib.BuildToolInfo.PathId.DX;
import static com.android.sdklib.BuildToolInfo.PathId.DX_JAR;
import static com.android.sdklib.BuildToolInfo.PathId.JACK;
import static com.android.sdklib.BuildToolInfo.PathId.JILL;
import static com.android.sdklib.BuildToolInfo.PathId.LD_ARM;
import static com.android.sdklib.BuildToolInfo.PathId.LD_MIPS;
import static com.android.sdklib.BuildToolInfo.PathId.LD_X86;
import static com.android.sdklib.BuildToolInfo.PathId.LLVM_RS_CC;
import static com.android.sdklib.BuildToolInfo.PathId.SPLIT_SELECT;
import static com.android.sdklib.BuildToolInfo.PathId.ZIP_ALIGN;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.android.annotations.VisibleForTesting.Visibility;
import com.android.sdklib.io.FileOp;
import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.NoPreviewRevision;
import com.android.utils.ILogger;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Information on a specific build-tool folder.
 * <p/>
 * For unit tests, see:
 * - sdklib/src/test/.../LocalSdkTest
 * - sdklib/src/test/.../SdkManagerTest
 * - sdklib/src/test/.../BuildToolInfoTest
 */
public class BuildToolInfo {

    /** Name of the file read by {@link #getRuntimeProps()} */
    private static final String FN_RUNTIME_PROPS = "runtime.properties";

    /**
     * Property in {@link #FN_RUNTIME_PROPS} indicating the desired runtime JVM.
     * Type: {@link NoPreviewRevision#toShortString()}, e.g. "1.7.0"
     */
    private static final String PROP_RUNTIME_JVM = "Runtime.Jvm";

    /**
     * First version with native multi-dex support.
     */
    public static final int SDK_LEVEL_FOR_MULTIDEX_NATIVE_SUPPORT = 21;

    public enum PathId {
        /** OS Path to the target's version of the aapt tool. */
        AAPT("1.0.0"),
        /** OS Path to the target's version of the aidl tool. */
        AIDL("1.0.0"),
        /** OS Path to the target's version of the dx tool. */
        DX("1.0.0"),
        /** OS Path to the target's version of the dx.jar file. */
        DX_JAR("1.0.0"),
        /** OS Path to the llvm-rs-cc binary for Renderscript. */
        LLVM_RS_CC("1.0.0"),
        /** OS Path to the Renderscript include folder. */
        ANDROID_RS("1.0.0"),
        /** OS Path to the Renderscript(clang) include folder. */
        ANDROID_RS_CLANG("1.0.0"),

        DEXDUMP("1.0.0"),

        // --- NEW IN 18.1.0 ---

        /** OS Path to the bcc_compat tool. */
        BCC_COMPAT("18.1.0"),
        /** OS Path to the ARM linker. */
        LD_ARM("18.1.0"),
        /** OS Path to the X86 linker. */
        LD_X86("18.1.0"),
        /** OS Path to the MIPS linker. */
        LD_MIPS("18.1.0"),

        // --- NEW IN 19.1.0 ---
        ZIP_ALIGN("19.1.0"),

        // --- NEW IN 21.x.y ---
        JACK("21.1.0"),
        JILL("21.1.0"),

        SPLIT_SELECT("22.0.0");

        /**
         * min revision this element was introduced.
         * Controls {@link BuildToolInfo#isValid(ILogger)}
         */
        private final FullRevision mMinRevision;

        /**
         * Creates the enum with a min revision in which this
         * tools appeared in the build tools.
         *
         * @param minRevision the min revision.
         */
        PathId(@NonNull String minRevision) {
            mMinRevision = FullRevision.parseRevision(minRevision);
        }

        /**
         * Returns whether the enum of present in a given rev of the build tools.
         *
         * @param fullRevision the build tools revision.
         * @return true if the tool is present.
         */
        boolean isPresentIn(@NonNull FullRevision fullRevision) {
            return fullRevision.compareTo(mMinRevision) >= 0;
        }
    }

    /** The build-tool revision. */
    @NonNull
    private final FullRevision mRevision;

    /** The path to the build-tool folder specific to this revision. */
    @NonNull
    private final File mPath;

    private final Map<PathId, String> mPaths = Maps.newEnumMap(PathId.class);

    public BuildToolInfo(@NonNull FullRevision revision, @NonNull File path) {
        mRevision = revision;
        mPath = path;

        add(AAPT, FN_AAPT);
        add(AIDL, FN_AIDL);
        add(DX, FN_DX);
        add(DX_JAR, FD_LIB + File.separator + FN_DX_JAR);
        add(LLVM_RS_CC, FN_RENDERSCRIPT);
        add(ANDROID_RS, OS_FRAMEWORK_RS);
        add(ANDROID_RS_CLANG, OS_FRAMEWORK_RS_CLANG);
        add(DEXDUMP, FN_DEXDUMP);
        add(BCC_COMPAT, FN_BCC_COMPAT);
        add(LD_ARM, FN_LD_ARM);
        add(LD_X86, FN_LD_X86);
        add(LD_MIPS, FN_LD_MIPS);
        add(ZIP_ALIGN, FN_ZIPALIGN);
        add(JACK, FN_JACK);
        add(JILL, FN_JILL);
        add(SPLIT_SELECT, FN_SPLIT_SELECT);
    }

    public BuildToolInfo(
            @NonNull FullRevision revision,
            @NonNull File mainPath,
            @NonNull File aapt,
            @NonNull File aidl,
            @NonNull File dx,
            @NonNull File dxJar,
            @NonNull File llmvRsCc,
            @NonNull File androidRs,
            @NonNull File androidRsClang,
            @Nullable File bccCompat,
            @Nullable File ldArm,
            @Nullable File ldX86,
            @Nullable File ldMips,
            @NonNull File zipAlign) {
        mRevision = revision;
        mPath = mainPath;
        add(AAPT, aapt);
        add(AIDL, aidl);
        add(DX, dx);
        add(DX_JAR, dxJar);
        add(LLVM_RS_CC, llmvRsCc);
        add(ANDROID_RS, androidRs);
        add(ANDROID_RS_CLANG, androidRsClang);
        add(ZIP_ALIGN, zipAlign);

        if (bccCompat != null) {
            add(BCC_COMPAT, bccCompat);
        } else if (BCC_COMPAT.isPresentIn(revision)) {
            throw new IllegalArgumentException("BCC_COMPAT required in " + revision.toString());
        }
        if (ldArm != null) {
            add(LD_ARM, ldArm);
        } else if (LD_ARM.isPresentIn(revision)) {
            throw new IllegalArgumentException("LD_ARM required in " + revision.toString());
        }

        if (ldX86 != null) {
            add(LD_X86, ldX86);
        } else if (LD_X86.isPresentIn(revision)) {
            throw new IllegalArgumentException("LD_X86 required in " + revision.toString());
        }

        if (ldMips != null) {
            add(LD_MIPS, ldMips);
        } else if (LD_MIPS.isPresentIn(revision)) {
            throw new IllegalArgumentException("LD_MIPS required in " + revision.toString());
        }
    }

    private void add(PathId id, String leaf) {
        add(id, new File(mPath, leaf));
    }

    private void add(PathId id, File path) {
        String str = path.getAbsolutePath();
        if (path.isDirectory() && str.charAt(str.length() - 1) != File.separatorChar) {
            str += File.separatorChar;
        }
        mPaths.put(id, str);
    }

    /**
     * Returns the revision.
     */
    @NonNull
    public FullRevision getRevision() {
        return mRevision;
    }

    /**
     * Returns the build-tool revision-specific folder.
     * <p/>
     * For compatibility reasons, use {@link #getPath(PathId)} if you need the path to a
     * specific tool.
     */
    @NonNull
    public File getLocation() {
        return mPath;
    }

    /**
     * Returns the path of a build-tool component.
     *
     * @param pathId the id representing the path to return.
     * @return The absolute path for that tool, with a / separator if it's a folder.
     *         Null if the path-id is unknown.
     */
    public String getPath(PathId pathId) {
        assert pathId.isPresentIn(mRevision);

        return mPaths.get(pathId);
    }

    /**
     * Checks whether the build-tool is valid by verifying that the expected binaries
     * are actually present. This checks that all known paths point to a valid file
     * or directory.
     *
     * @param log An optional logger. If non-null, errors will be printed there.
     * @return True if the build-tool folder contains all the expected tools.
     */
    public boolean isValid(@Nullable ILogger log) {
        for (Map.Entry<PathId, String> entry : mPaths.entrySet()) {
            File f = new File(entry.getValue());
            // check if file is missing. It's only ok if the revision of the build-tools
            // is lower than the min rev of the element.
            if (!f.exists() && entry.getKey().isPresentIn(mRevision)) {
                if (log != null) {
                    log.warning("Build-tool %1$s is missing %2$s at %3$s",  //$NON-NLS-1$
                            mRevision.toString(),
                            entry.getKey(), f.getAbsolutePath());
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Parses the build-tools runtime.props file, if present.
     *
     * @return The properties from runtime.props if present, otherwise an empty properties set.
     */
    @NonNull
    public Properties getRuntimeProps() {
        FileOp fop = new FileOp();
        return fop.loadProperties(new File(mPath, FN_RUNTIME_PROPS));
    }

    /**
     * Checks whether this build-tools package can run on the current JVM.
     *
     * @return True if the build-tools package has a Runtime.Jvm property and it is lesser or
     *  equal to the current JVM version.
     *         False if the property is present and the requirement is not met.
     *         True if there's an error parsing either versions and the comparison cannot be made.
     */
    public boolean canRunOnJvm() {
        Properties props = getRuntimeProps();
        String required = props.getProperty(PROP_RUNTIME_JVM);
        if (required == null) {
            // No requirement ==> accepts.
            return true;
        }
        try {
            NoPreviewRevision requiredVersion = NoPreviewRevision.parseRevision(required);
            NoPreviewRevision currentVersion = getCurrentJvmVersion();
            return currentVersion.compareTo(requiredVersion) >= 0;

        } catch (NumberFormatException ignore) {
            // Either we failed to parse the property version or the running JVM version.
            // Right now take the relaxed policy of accepting it if we can't compare.
            return true;
        }
    }

    @VisibleForTesting(visibility=Visibility.PRIVATE)
    @Nullable
    protected NoPreviewRevision getCurrentJvmVersion() throws NumberFormatException {
        String javav = System.getProperty("java.version");              //$NON-NLS-1$
        // java Version is typically in the form "1.2.3_45" and we just need to keep up to "1.2.3"
        // since our revision numbers are in 3-parts form (1.2.3).
        Pattern p = Pattern.compile("((\\d+)(\\.\\d+)?(\\.\\d+)?).*");  //$NON-NLS-1$
        Matcher m = p.matcher(javav);
        if (m.matches()) {
            return NoPreviewRevision.parseRevision(m.group(1));
        }
        return null;
    }

    /**
     * Returns a debug representation suitable for unit-tests.
     * Note that unit-tests need to clean up the paths to avoid inconsistent results.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<BuildToolInfo rev=").append(mRevision);    //$NON-NLS-1$
        builder.append(", mPath=").append(mPath);                   //$NON-NLS-1$
        builder.append(", mPaths=").append(getPathString());        //$NON-NLS-1$
        builder.append(">");                                        //$NON-NLS-1$
        return builder.toString();
    }

    private String getPathString() {
        StringBuilder sb = new StringBuilder("{");

        for (Map.Entry<PathId, String> entry : mPaths.entrySet()) {
            if (entry.getKey().isPresentIn(mRevision)) {
                if (sb.length() > 1) {
                    sb.append(", ");
                }
                sb.append(entry.getKey()).append('=').append(entry.getValue());
            }
        }

        sb.append('}');

        return sb.toString();
    }
}
