/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.sdklib.internal.repository;

import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.ISdkLog;
import com.android.sdklib.ISystemImage;
import com.android.sdklib.SdkConstants;
import com.android.sdklib.SdkManager;
import com.android.sdklib.ISystemImage.LocationType;
import com.android.sdklib.internal.repository.Archive.Arch;
import com.android.sdklib.internal.repository.Archive.Os;
import com.android.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Scans a local SDK to find which packages are currently installed.
 */
public class LocalSdkParser {

    private Package[] mPackages;

    public LocalSdkParser() {
        // pass
    }

    /**
     * Returns the packages found by the last call to {@link #parseSdk}.
     * <p/>
     * This returns initially returns null.
     * Once the parseSdk() method has been called, this returns a possibly empty but non-null array.
     */
    public Package[] getPackages() {
        return mPackages;
    }

    /**
     * Clear the internal packages list. After this call, {@link #getPackages()} will return
     * null till {@link #parseSdk} is called.
     */
    public void clearPackages() {
        mPackages = null;
    }

    /**
     * Scan the give SDK to find all the packages already installed at this location.
     * <p/>
     * Store the packages internally. You can use {@link #getPackages()} to retrieve them
     * at any time later.
     *
     * @param osSdkRoot The path to the SDK folder, typically {@code sdkManager.getLocation()}.
     * @param sdkManager An existing SDK manager to list current platforms and addons.
     * @param monitor A monitor to track progress. Cannot be null.
     * @return The packages found. Can be retrieved later using {@link #getPackages()}.
     */
    public Package[] parseSdk(
            String osSdkRoot,
            SdkManager sdkManager,
            ITaskMonitor monitor) {
        ArrayList<Package> packages = new ArrayList<Package>();
        HashSet<File> visited = new HashSet<File>();

        monitor.setProgressMax(10);

        File dir = new File(osSdkRoot, SdkConstants.FD_DOCS);
        Package pkg = scanDoc(dir, monitor);
        if (pkg != null) {
            packages.add(pkg);
            visited.add(dir);
        }
        monitor.incProgress(1);

        dir = new File(osSdkRoot, SdkConstants.FD_TOOLS);
        pkg = scanTools(dir, monitor);
        if (pkg != null) {
            packages.add(pkg);
            visited.add(dir);
        }
        monitor.incProgress(1);

        dir = new File(osSdkRoot, SdkConstants.FD_PLATFORM_TOOLS);
        pkg = scanPlatformTools(dir, monitor);
        if (pkg != null) {
            packages.add(pkg);
            visited.add(dir);
        }
        monitor.incProgress(1);

        File samplesRoot = new File(osSdkRoot, SdkConstants.FD_SAMPLES);

        // for platforms, add-ons and samples, rely on the SdkManager parser
        for(IAndroidTarget target : sdkManager.getTargets()) {
            Properties props = parseProperties(new File(target.getLocation(),
                    SdkConstants.FN_SOURCE_PROP));

            try {
                if (target.isPlatform()) {
                    pkg = PlatformPackage.create(target, props);

                    if (samplesRoot.isDirectory()) {
                        // Get the samples dir for a platform if it is located in the new
                        // root /samples dir. We purposely ignore "old" samples that are
                        // located under the platform dir.
                        File samplesDir = new File(target.getPath(IAndroidTarget.SAMPLES));
                        if (samplesDir.exists() && samplesDir.getParentFile().equals(samplesRoot)) {
                            Properties samplesProps = parseProperties(
                                    new File(samplesDir, SdkConstants.FN_SOURCE_PROP));
                            if (samplesProps != null) {
                                Package pkg2 = SamplePackage.create(target, samplesProps);
                                packages.add(pkg2);
                            }
                            visited.add(samplesDir);
                        }
                    }
                } else {
                    pkg = AddonPackage.create(target, props);
                }

                for (ISystemImage systemImage : target.getSystemImages()) {
                    if (systemImage.getLocationType() == LocationType.IN_SYSTEM_IMAGE) {
                        File siDir = systemImage.getLocation();
                        if (siDir.isDirectory()) {
                            Properties siProps = parseProperties(
                                    new File(siDir, SdkConstants.FN_SOURCE_PROP));
                            Package pkg2 = new SystemImagePackage(
                                    target.getVersion(),
                                    0 /*revision*/,   // this will use the one from siProps if any
                                    systemImage.getAbiType(),
                                    siProps,
                                    siDir.getAbsolutePath());
                            packages.add(pkg2);
                            visited.add(siDir);
                        }
                    }
                }

            } catch (Exception e) {
                monitor.error(e, null);
            }

            if (pkg != null) {
                packages.add(pkg);
                visited.add(new File(target.getLocation()));
            }
        }
        monitor.incProgress(1);

        scanMissingSystemImages(sdkManager, visited, packages, monitor);
        monitor.incProgress(1);
        scanMissingAddons(sdkManager, visited, packages, monitor);
        monitor.incProgress(1);
        scanMissingSamples(sdkManager, visited, packages, monitor);
        monitor.incProgress(1);
        scanExtras(sdkManager, visited, packages, monitor);
        monitor.incProgress(1);
        scanExtrasDirectory(osSdkRoot, visited, packages, monitor);
        monitor.incProgress(1);
        scanSources(sdkManager, visited, packages, monitor);
        monitor.incProgress(1);

        Collections.sort(packages);

        mPackages = packages.toArray(new Package[packages.size()]);
        return mPackages;
    }

    /**
     * Find any directory in the /extras/vendors/path folders for extra packages.
     * This isn't a recursive search.
     */
    private void scanExtras(SdkManager sdkManager,
            HashSet<File> visited,
            ArrayList<Package> packages,
            ISdkLog log) {
        File root = new File(sdkManager.getLocation(), SdkConstants.FD_EXTRAS);

        if (!root.isDirectory()) {
            // This should not happen. It makes listFiles() return null so let's avoid it.
            return;
        }

        for (File vendor : root.listFiles()) {
            if (vendor.isDirectory()) {
                scanExtrasDirectory(vendor.getAbsolutePath(), visited, packages, log);
            }
        }
    }

    /**
     * Find any other directory in the given "root" directory that hasn't been visited yet
     * and assume they contain extra packages. This is <em>not</em> a recursive search.
     */
    private void scanExtrasDirectory(String extrasRoot,
            HashSet<File> visited,
            ArrayList<Package> packages,
            ISdkLog log) {
        File root = new File(extrasRoot);

        if (!root.isDirectory()) {
            // This should not happen. It makes listFiles() return null so let's avoid it.
            return;
        }

        for (File dir : root.listFiles()) {
            if (dir.isDirectory() && !visited.contains(dir)) {
                Properties props = parseProperties(new File(dir, SdkConstants.FN_SOURCE_PROP));
                if (props != null) {
                    try {
                        Package pkg = ExtraPackage.create(
                                null,                       //source
                                props,                      //properties
                                null,                       //vendor
                                dir.getName(),              //path
                                0,                          //revision
                                null,                       //license
                                "Tools",                    //description
                                null,                       //descUrl
                                Os.getCurrentOs(),          //archiveOs
                                Arch.getCurrentArch(),      //archiveArch
                                dir.getPath()               //archiveOsPath
                                );

                        packages.add(pkg);
                        visited.add(dir);
                    } catch (Exception e) {
                        log.error(e, null);
                    }
                }
            }
        }
    }

    /**
     * Find any other sub-directories under the /samples root that hasn't been visited yet
     * and assume they contain sample packages. This is <em>not</em> a recursive search.
     * <p/>
     * The use case is to find samples dirs under /samples when their target isn't loaded.
     */
    private void scanMissingSamples(SdkManager sdkManager,
            HashSet<File> visited,
            ArrayList<Package> packages,
            ISdkLog log) {
        File root = new File(sdkManager.getLocation());
        root = new File(root, SdkConstants.FD_SAMPLES);

        if (!root.isDirectory()) {
            // It makes listFiles() return null so let's avoid it.
            return;
        }

        for (File dir : root.listFiles()) {
            if (dir.isDirectory() && !visited.contains(dir)) {
                Properties props = parseProperties(new File(dir, SdkConstants.FN_SOURCE_PROP));
                if (props != null) {
                    try {
                        Package pkg = SamplePackage.create(dir.getAbsolutePath(), props);
                        packages.add(pkg);
                        visited.add(dir);
                    } catch (Exception e) {
                        log.error(e, null);
                    }
                }
            }
        }
    }

    /**
     * The sdk manager only lists valid addons. However here we also want to find "broken"
     * addons, i.e. addons that failed to load for some reason.
     * <p/>
     * Find any other sub-directories under the /add-ons root that hasn't been visited yet
     * and assume they contain broken addons.
     */
    private void scanMissingAddons(SdkManager sdkManager,
            HashSet<File> visited,
            ArrayList<Package> packages,
            ISdkLog log) {
        File addons = new File(new File(sdkManager.getLocation()), SdkConstants.FD_ADDONS);

        File[] files = addons.listFiles();
        if (files == null) {
            return;
        }

        for (File dir : files) {
            if (dir.isDirectory() && !visited.contains(dir)) {
                Pair<Map<String, String>, String> infos =
                    SdkManager.parseAddonProperties(dir, sdkManager.getTargets(), log);

                Map<String, String> props = infos.getFirst();
                String error = infos.getSecond();
                try {
                    Package pkg = AddonPackage.createBroken(dir.getAbsolutePath(), props, error);
                    packages.add(pkg);
                    visited.add(dir);
                } catch (Exception e) {
                    log.error(e, null);
                }
            }
        }
    }

    /**
     * The sdk manager only lists valid system image via its addons or platform targets.
     * However here we also want to find "broken" system images, that is system images
     * that are located in the sdk/system-images folder but somehow not loaded properly.
     */
    private void scanMissingSystemImages(SdkManager sdkManager,
            HashSet<File> visited,
            ArrayList<Package> packages,
            ISdkLog log) {
        File siRoot = new File(sdkManager.getLocation(), SdkConstants.FD_SYSTEM_IMAGES);

        File[] files = siRoot.listFiles();
        if (files == null) {
            return;
        }

        // The system-images folder contains a list of platform folders.
        for (File platformDir : files) {
            if (platformDir.isDirectory() && !visited.contains(platformDir)) {
                visited.add(platformDir);

                // In the platform directory, we expect a list of abi folders
                File[] platformFiles = platformDir.listFiles();
                if (platformFiles != null) {
                    for (File abiDir : platformFiles) {
                        if (abiDir.isDirectory() && !visited.contains(abiDir)) {
                            visited.add(abiDir);

                            // Ignore empty directories
                            File[] abiFiles = abiDir.listFiles();
                            if (abiFiles != null && abiFiles.length > 0) {
                                Properties props =
                                    parseProperties(new File(abiDir, SdkConstants.FN_SOURCE_PROP));

                                try {
                                    Package pkg = SystemImagePackage.createBroken(abiDir, props);
                                    packages.add(pkg);
                                } catch (Exception e) {
                                    log.error(e, null);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Scan the sources/folders and register valid as well as broken source packages.
     */
    private void scanSources(SdkManager sdkManager,
            HashSet<File> visited,
            ArrayList<Package> packages,
            ISdkLog log) {
        File srcRoot = new File(sdkManager.getLocation(), SdkConstants.FD_PKG_SOURCES);

        File[] subDirs = srcRoot.listFiles();
        if (subDirs == null) {
            return;
        }

        // The sources folder contains a list of platform folders.
        for (File platformDir : subDirs) {
            if (platformDir.isDirectory() && !visited.contains(platformDir)) {
                visited.add(platformDir);

                // Ignore empty directories
                File[] srcFiles = platformDir.listFiles();
                if (srcFiles != null && srcFiles.length > 0) {
                    Properties props =
                        parseProperties(new File(platformDir, SdkConstants.FN_SOURCE_PROP));

                    try {
                        Package pkg = SourcePackage.create(platformDir, props);
                        packages.add(pkg);
                    } catch (Exception e) {
                        log.error(e, null);
                    }
                }
            }
        }
    }

    /**
     * Try to find a tools package at the given location.
     * Returns null if not found.
     */
    private Package scanTools(File toolFolder, ISdkLog log) {
        // Can we find some properties?
        Properties props = parseProperties(new File(toolFolder, SdkConstants.FN_SOURCE_PROP));

        // We're not going to check that all tools are present. At the very least
        // we should expect to find android and an emulator adapted to the current OS.
        Set<String> names = new HashSet<String>();
        File[] files = toolFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                names.add(file.getName());
            }
        }

        if (!names.contains(SdkConstants.androidCmdName()) ||
                !names.contains(SdkConstants.FN_EMULATOR)) {
            return null;
        }

        // Create our package. use the properties if we found any.
        try {
            Package pkg = ToolPackage.create(
                    null,                       //source
                    props,                      //properties
                    0,                          //revision
                    null,                       //license
                    "Tools",                    //description
                    null,                       //descUrl
                    Os.getCurrentOs(),          //archiveOs
                    Arch.getCurrentArch(),      //archiveArch
                    toolFolder.getPath()        //archiveOsPath
                    );

            return pkg;
        } catch (Exception e) {
            log.error(e, null);
        }
        return null;
    }

    /**
     * Try to find a platform-tools package at the given location.
     * Returns null if not found.
     */
    private Package scanPlatformTools(File platformToolsFolder, ISdkLog log) {
        // Can we find some properties?
        Properties props = parseProperties(new File(platformToolsFolder,
                SdkConstants.FN_SOURCE_PROP));

        // We're not going to check that all tools are present. At the very least
        // we should expect to find adb, aidl, aapt and dx (adapted to the current OS).

        if (platformToolsFolder.listFiles() == null) {
            // ListFiles is null if the directory doesn't even exist.
            // Not going to find anything in there...
            return null;
        }

        // Create our package. use the properties if we found any.
        try {
            Package pkg = PlatformToolPackage.create(
                    null,                           //source
                    props,                          //properties
                    0,                              //revision
                    null,                           //license
                    "Platform Tools",               //description
                    null,                           //descUrl
                    Os.getCurrentOs(),              //archiveOs
                    Arch.getCurrentArch(),          //archiveArch
                    platformToolsFolder.getPath()   //archiveOsPath
                    );

            return pkg;
        } catch (Exception e) {
            log.error(e, null);
        }
        return null;
    }

    /**
     * Try to find a docs package at the given location.
     * Returns null if not found.
     */
    private Package scanDoc(File docFolder, ISdkLog log) {
        // Can we find some properties?
        Properties props = parseProperties(new File(docFolder, SdkConstants.FN_SOURCE_PROP));

        // To start with, a doc folder should have an "index.html" to be acceptable.
        // We don't actually check the content of the file.
        if (new File(docFolder, "index.html").isFile()) {
            try {
                Package pkg = DocPackage.create(
                        null,                       //source
                        props,                      //properties
                        0,                          //apiLevel
                        null,                       //codename
                        0,                          //revision
                        null,                       //license
                        null,                       //description
                        null,                       //descUrl
                        Os.getCurrentOs(),          //archiveOs
                        Arch.getCurrentArch(),      //archiveArch
                        docFolder.getPath()         //archiveOsPath
                        );

                return pkg;
            } catch (Exception e) {
                log.error(e, null);
            }
        }

        return null;
    }

    /**
     * Parses the given file as properties file if it exists.
     * Returns null if the file does not exist, cannot be parsed or has no properties.
     */
    private Properties parseProperties(File propsFile) {
        FileInputStream fis = null;
        try {
            if (propsFile.exists()) {
                fis = new FileInputStream(propsFile);

                Properties props = new Properties();
                props.load(fis);

                // To be valid, there must be at least one property in it.
                if (props.size() > 0) {
                    return props;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
}
