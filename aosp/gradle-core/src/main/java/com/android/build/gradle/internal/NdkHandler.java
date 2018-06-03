/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.build.gradle.internal;

import static com.android.SdkConstants.FN_LOCAL_PROPERTIES;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.core.Abi;
import com.android.build.gradle.internal.core.Toolchain;
import com.android.sdklib.AndroidTargetHash;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.repository.PreciseRevision;
import com.android.utils.Pair;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

import org.gradle.api.InvalidUserDataException;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Handles NDK related information.
 */
public class NdkHandler {

    @Nullable
    private String compileSdkVersion;
    private boolean resolvedSdkVersion;
    private final Toolchain toolchain;
    private final String toolchainVersion;
    private final File ndkDirectory;

    private Map<Pair<Toolchain, Abi>, PreciseRevision> defaultToolchainVersions = Maps.newHashMap();


    public NdkHandler(
            @NonNull File projectDir,
            @Nullable String compileSdkVersion,
            @NonNull String toolchainName,
            @NonNull String toolchainVersion) {
        if (compileSdkVersion != null) {
            setCompileSdkVersion(compileSdkVersion);
        }
        this.toolchain = Toolchain.getByName(toolchainName);
        this.toolchainVersion = toolchainVersion;
        ndkDirectory = findNdkDirectory(projectDir);
    }

    @Nullable
    public String getCompileSdkVersion() {
        if (!resolvedSdkVersion) {
            resolveCompileSdkVersion();
        }
        return compileSdkVersion;
    }

    /**
     * Retrieve the newest supported version if it is not the specified version is not supported.
     *
     * An older NDK may not support the specified compiledSdkVersion.  In that case, determine what
     * is the newest supported version and modify compileSdkVersion.
     */
    private void resolveCompileSdkVersion() {
        if (compileSdkVersion == null) {
            return;
        }
        File platformFolder = new File(ndkDirectory, "/platforms/" + compileSdkVersion);
        if (!platformFolder.exists()) {
            int targetVersion;
            try {
                targetVersion = Integer.parseInt(compileSdkVersion.substring("android-".length()));
            } catch (NumberFormatException ignore) {
                // If the targetVerison is not a number, most likely it is a preview version.
                // In that case, assume we are using the highest available version.
                targetVersion = Integer.MAX_VALUE;
            }

            File[] platformFolders = new File(ndkDirectory, "/platforms/").listFiles(
                    new FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.isDirectory();
                        }
                    });
            int highestVersion = 0;
            for(File platform :platformFolders) {
                if (platform.getName().startsWith("android-")) {
                    try {
                        int version = Integer.parseInt(
                                platform.getName().substring("android-".length()));
                        if (version > highestVersion && version < targetVersion) {
                            highestVersion = version;
                            compileSdkVersion = "android-" + version;
                        }
                    } catch(NumberFormatException ignore) {
                    }
                }
            }
        }
        resolvedSdkVersion = true;
    }

    public void setCompileSdkVersion(@NonNull String compileSdkVersion) {
        // Ensure compileSdkVersion is in platform hash string format (e.g. "android-21").
        AndroidVersion androidVersion = AndroidTargetHash.getVersionFromHash(compileSdkVersion);
        if (androidVersion == null) {
            this.compileSdkVersion = null;
        } else {
            this.compileSdkVersion = AndroidTargetHash.getPlatformHashString(androidVersion);
        }
        resolvedSdkVersion = false;
    }

    public Toolchain getToolchain() {
        return toolchain;
    }

    public String getToolchainVersion() {
        return toolchainVersion;
    }

    /**
     * Determine the location of the NDK directory.
     *
     * The NDK directory can be set in the local.properties file or using the ANDROID_NDK_HOME
     * environment variable.
     */
    private static File findNdkDirectory(File projectDir) {
        File localProperties = new File(projectDir, FN_LOCAL_PROPERTIES);

        if (localProperties.isFile()) {

            Properties properties = new Properties();
            InputStreamReader reader = null;
            try {
                //noinspection IOResourceOpenedButNotSafelyClosed
                FileInputStream fis = new FileInputStream(localProperties);
                reader = new InputStreamReader(fis, Charsets.UTF_8);
                properties.load(reader);
            } catch (FileNotFoundException ignored) {
                // ignore since we check up front and we don't want to fail on it anyway
                // in case there's an env var.
            } catch (IOException e) {
                throw new RuntimeException(String.format("Unable to read %1$s.", localProperties), e);
            } finally {
                try {
                    Closeables.close(reader, true /* swallowIOException */);
                } catch (IOException e) {
                    // ignore.
                }
            }

            String ndkDirProp = properties.getProperty("ndk.dir");
            if (ndkDirProp != null) {
                return new File(ndkDirProp);
            }

        } else {
            String envVar = System.getenv("ANDROID_NDK_HOME");
            if (envVar != null) {
                return new File(envVar);
            }
        }
        return null;
    }

    /**
     * Returns the directory of the NDK.
     */
    @Nullable
    public File getNdkDirectory() {
        return ndkDirectory;
    }

    /**
     * Return true if NDK directory is configured.
     */
    public boolean isNdkDirConfigured() {
        return ndkDirectory != null;
    }

    private static String getToolchainPrefix(Toolchain toolchain, Abi abi) {
        if (toolchain == Toolchain.GCC) {
            return abi.getGccToolchainPrefix();
        } else {
            return "llvm";
        }
    }

    /**
     * Return the directory containing the toolchain.
     *
     * @param toolchain toolchain to use.
     * @param toolchainVersion toolchain version to use.
     * @param abi target ABI of the toolchaina
     * @return a directory that contains the executables.
     */
    private File getToolchainPath(
            Toolchain toolchain,
            String toolchainVersion,
            Abi abi) {
        String version = toolchainVersion.isEmpty()
                ? getDefaultToolchainVersion(toolchain, abi).toString()
                : toolchainVersion;

        File prebuiltFolder = new File(
                ndkDirectory,
                "toolchains/" + getToolchainPrefix(toolchain, abi) + "-" + version + "/prebuilt");

        String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        String hostOs;
        if (osName.contains("windows")) {
            hostOs = "windows";
        } else if (osName.contains("mac")) {
            hostOs = "darwin";
        } else {
            hostOs = "linux";
        }

        // There should only be one directory in the prebuilt folder.  If there are more than one
        // attempt to determine the right one based on the operating system.
        File[] toolchainPaths = prebuiltFolder.listFiles(
                new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }
                });

        if (toolchainPaths == null) {
            throw new InvalidUserDataException("Unable to find toolchain: " + prebuiltFolder);
        }
        if (toolchainPaths.length == 1) {
            return toolchainPaths[0];
        }

        // Use 64-bit toolchain if available.
        File toolchainPath = new File(prebuiltFolder, hostOs + "-x86_64");
        if (toolchainPath.isDirectory()) {
            return toolchainPath;
        }

        // Fallback to 32-bit if we can't find the 64-bit toolchain.
        String osString = (osName.equals("windows")) ? hostOs : hostOs + "-x86";
        toolchainPath = new File(prebuiltFolder, osString);
        if (toolchainPath.isDirectory()) {
            return toolchainPath;
        } else {
            throw new InvalidUserDataException("Unable to find toolchain prebuilt folder in: "
                    + prebuiltFolder);
        }
    }

    /**
     * Returns the sysroot directory for the toolchain.
     */
    public String getSysroot(Abi abi) {
        if (getCompileSdkVersion() == null) {
            return "";
        } else {
            return ndkDirectory + "/platforms/" + getCompileSdkVersion() + "/arch-"
                    + abi.getArchitecture();
        }
    }

    /**
     * Return the directory containing prebuilt binaries such as gdbserver.
     */
    public File getPrebuiltDirectory(Abi abi) {
        return new File(ndkDirectory, "prebuilt/android-" + abi.getArchitecture());
    }

    /**
     * Return true if compiledSdkVersion supports 64 bits ABI.
     */
    public boolean supports64Bits() {
        if (getCompileSdkVersion() == null) {
            return false;
        }
        String targetString = getCompileSdkVersion().replace("android-", "");
        try {
            return Integer.parseInt(targetString) >= 20;
        } catch (NumberFormatException ignored) {
            // "android-L" supports 64-bits.
            return true;
        }
    }

    /**
     * Return the default version of the specified toolchain for a target abi.
     *
     * The default version is the highest version found in the NDK for the specified toolchain and
     * ABI.  The result is cached for performance.
     */
    private PreciseRevision getDefaultToolchainVersion(Toolchain toolchain, final Abi abi) {
        PreciseRevision defaultVersion = defaultToolchainVersions.get(Pair.of(toolchain, abi));
        if (defaultVersion != null) {
            return defaultVersion;
        }

        final String toolchainPrefix = getToolchainPrefix(toolchain, abi);
        File toolchains = new File(ndkDirectory, "toolchains");
        File[] toolchainsForAbi = toolchains.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(toolchainPrefix);
            }
        });
        if (toolchainsForAbi == null || toolchainsForAbi.length == 0) {
            throw new RuntimeException(
                    "No toolchains found in the NDK toolchains folder for ABI with prefix: "
                            + toolchainPrefix);
        }

        // Once we have a list of toolchains, we look the highest version
        PreciseRevision bestRevision = null;
        for (File toolchainFolder : toolchainsForAbi) {
            String folderName = toolchainFolder.getName();
            String version = folderName.substring(toolchainPrefix.length() + 1);
            try {
                PreciseRevision revision = PreciseRevision.parseRevision(version);
                if (bestRevision == null || revision.compareTo(bestRevision) > 0) {
                    bestRevision = revision;
                }
            } catch (NumberFormatException ignore) {
            }
        }
        defaultToolchainVersions.put(Pair.of(toolchain, abi), bestRevision);
        if (bestRevision == null) {
            throw new RuntimeException("Unable to find a valid toolchain in " + toolchains);
        }
        return bestRevision;
    }

    /**
     * Return the version of gcc that will be used by the NDK.
     *
     * Gcc is used by clang for linking.  It also contains gnu-libstdc++.
     *
     * If the gcc toolchain is used, then it's simply the toolchain version requested by the user.
     * If clang is used, then it depends the target abi.
     */
    public String getGccToolchainVersion(Abi abi) {
        return (toolchain == Toolchain.GCC && !toolchainVersion.isEmpty())
                ? toolchainVersion
                : getDefaultToolchainVersion(Toolchain.GCC, abi).toString();
    }

    /**
     * Return the folder containing gcc that will be used by the NDK.
     */
    public File getDefaultGccToolchainPath(Abi abi) {
        return getToolchainPath(Toolchain.GCC, getGccToolchainVersion(abi), abi);
    }

    /**
     * Returns a list of all ABI.
     */
    public static Collection<Abi> getAbiList() {
        return ImmutableList.copyOf(Abi.values());
    }

    /**
     * Returns a list of 32-bits ABI.
     */
    public static Collection<Abi> getAbiList32() {
        ImmutableList.Builder<Abi> builder = ImmutableList.builder();
        for (Abi abi : Abi.values()) {
            if (!abi.supports64Bits()) {
                builder.add(abi);
            }
        }
        return builder.build();
    }

    /**
     * Returns a list of supported ABI.
     */
    public Collection<Abi> getSupportedAbis() {
        return supports64Bits() ? getAbiList() : getAbiList32();
    }

    /**
     * Return the executable for compiling C code.
     */
    public File getCCompiler(Abi abi) {
        String compiler = toolchain == Toolchain.CLANG ? "clang" : abi.getGccExecutablePrefix() + "-gcc";
        return new File(getToolchainPath(toolchain, toolchainVersion, abi), "bin/" + compiler);
    }

    /**
     * Return the executable for compiling C++ code.
     */
    public File getCppCompiler(Abi abi) {
        String compiler = toolchain == Toolchain.CLANG ? "clang++" : abi.getGccExecutablePrefix() + "-g++";
        return new File(getToolchainPath(toolchain, toolchainVersion, abi), "bin/" + compiler);
    }

    /**
     * Return the executable for removing debug symbols from a shared object.
     */
    public File getStripCommand(Abi abi) {
        String strip = toolchain == Toolchain.CLANG ? "ndk-strip" : abi.getGccExecutablePrefix() + "-strip";
        return new File(getToolchainPath(toolchain, toolchainVersion, abi), "bin/" + strip);
    }

    /**
     * Return a list of include directories for an STl.
     */
    public List<File> getStlIncludes(@Nullable String stlName, @NonNull Abi abi) {
        File stlBaseDir = new File(ndkDirectory, "sources/cxx-stl/");
        if (stlName == null || stlName.isEmpty()) {
            stlName = "system";
        } else if (stlName.contains("_")) {
            stlName = stlName.substring(0, stlName.indexOf('_'));
        }

        List<File> includeDirs = Lists.newArrayList();
        if (stlName.equals("system")) {
            includeDirs.add(new File(stlBaseDir, "system/include"));
        } else if (stlName.equals("stlport")) {
            includeDirs.add(new File(stlBaseDir, "stlport/stlport"));
            includeDirs.add(new File(stlBaseDir, "gabi++/include"));
        } else if (stlName.equals("gnustl")) {
            String gccToolchainVersion = getGccToolchainVersion(abi);
            includeDirs.add(new File(stlBaseDir, "gnu-libstdc++/" + gccToolchainVersion + "/include"));
            includeDirs.add(new File(stlBaseDir, "gnu-libstdc++/" + gccToolchainVersion +
                    "/libs/" + abi.getName() + "/include"));
            includeDirs.add(new File(stlBaseDir, "gnu-libstdc++/" + gccToolchainVersion +
                    "/include/backward"));
        } else if (stlName.equals("gabi++")) {
            includeDirs.add(new File(stlBaseDir, "gabi++/include"));
        } else if (stlName.equals("c++")) {
            includeDirs.add(new File(stlBaseDir, "llvm-libc++/libcxx/include"));
            includeDirs.add(new File(stlBaseDir, "gabi++/include"));
            includeDirs.add(new File(stlBaseDir, "../android/support/include"));
        }

        return includeDirs;
    }
}
