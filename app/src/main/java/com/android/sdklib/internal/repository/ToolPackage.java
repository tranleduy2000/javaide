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

import com.android.annotations.VisibleForTesting;
import com.android.annotations.VisibleForTesting.Visibility;
import com.android.sdklib.SdkConstants;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.repository.Archive.Arch;
import com.android.sdklib.internal.repository.Archive.Os;
import com.android.sdklib.repository.SdkRepoConstants;

import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a tool XML node in an SDK repository.
 */
public class ToolPackage extends Package implements IMinPlatformToolsDependency {

    /** The value returned by {@link ToolPackage#installId()}. */
    public static final String INSTALL_ID = "tools";                             //$NON-NLS-1$

    protected static final String PROP_MIN_PLATFORM_TOOLS_REV =
                                                "Platform.MinPlatformToolsRev";  //$NON-NLS-1$

    /**
     * The minimal revision of the platform-tools package required by this package
     * or {@link #MIN_PLATFORM_TOOLS_REV_INVALID} if the value was missing.
     */
    private final int mMinPlatformToolsRevision;

    /**
     * Creates a new tool package from the attributes and elements of the given XML node.
     * This constructor should throw an exception if the package cannot be created.
     *
     * @param source The {@link SdkSource} where this is loaded from.
     * @param packageNode The XML element being parsed.
     * @param nsUri The namespace URI of the originating XML document, to be able to deal with
     *          parameters that vary according to the originating XML schema.
     * @param licenses The licenses loaded from the XML originating document.
     */
    ToolPackage(SdkSource source, Node packageNode, String nsUri, Map<String,String> licenses) {
        super(source, packageNode, nsUri, licenses);

        mMinPlatformToolsRevision = XmlParserUtils.getXmlInt(
                packageNode,
                SdkRepoConstants.NODE_MIN_PLATFORM_TOOLS_REV,
                MIN_PLATFORM_TOOLS_REV_INVALID);
        if (mMinPlatformToolsRevision == MIN_PLATFORM_TOOLS_REV_INVALID) {
            // This revision number is mandatory starting with sdk-repository-3.xsd
            // and did not exist before. Complain if the URI has level >= 3.

            boolean needRevision = false;

            Pattern nsPattern = Pattern.compile(SdkRepoConstants.NS_PATTERN);
            Matcher m = nsPattern.matcher(nsUri);
            if (m.matches()) {
                String version = m.group(1);
                try {
                    needRevision = Integer.parseInt(version) >= 3;
                } catch (NumberFormatException e) {
                    // ignore. needRevision defaults to false
                }
            }

            if (needRevision) {
                throw new IllegalArgumentException(
                        String.format("Missing %1$s element in %2$s package",
                                SdkRepoConstants.NODE_MIN_PLATFORM_TOOLS_REV,
                                SdkRepoConstants.NODE_PLATFORM_TOOL));
            }
        }
    }

    /**
     * Manually create a new package with one archive and the given attributes or properties.
     * This is used to create packages from local directories in which case there must be
     * one archive which URL is the actual target location.
     * <p/>
     * By design, this creates a package with one and only one archive.
     */
    static Package create(
            SdkSource source,
            Properties props,
            int revision,
            String license,
            String description,
            String descUrl,
            Os archiveOs,
            Arch archiveArch,
            String archiveOsPath) {
        return new ToolPackage(source, props, revision, license, description,
                descUrl, archiveOs, archiveArch, archiveOsPath);
    }

    @VisibleForTesting(visibility=Visibility.PRIVATE)
    protected ToolPackage(
                SdkSource source,
                Properties props,
                int revision,
                String license,
                String description,
                String descUrl,
                Os archiveOs,
                Arch archiveArch,
                String archiveOsPath) {
        super(source,
                props,
                revision,
                license,
                description,
                descUrl,
                archiveOs,
                archiveArch,
                archiveOsPath);

        mMinPlatformToolsRevision = Integer.parseInt(
                getProperty(
                        props,
                        PROP_MIN_PLATFORM_TOOLS_REV,
                        Integer.toString(MIN_PLATFORM_TOOLS_REV_INVALID)));
    }

    /**
    * The minimal revision of the tools package required by this package if > 0,
    * or {@link #MIN_PLATFORM_TOOLS_REV_INVALID} if the value was missing.
    * <p/>
    * This attribute is mandatory and should not be normally missing.
     */
    public int getMinPlatformToolsRevision() {
        return mMinPlatformToolsRevision;
    }

    /**
     * Returns a string identifier to install this package from the command line.
     * For tools, we use "tools" since this package is unique.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public String installId() {
        return INSTALL_ID;
    }

    /**
     * Returns a description of this package that is suitable for a list display.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public String getListDescription() {
        return String.format("Android SDK Tools%1$s",
                isObsolete() ? " (Obsolete)" : "");
    }

    /**
     * Returns a short description for an {@link IDescription}.
     */
    @Override
    public String getShortDescription() {
        return String.format("Android SDK Tools, revision %1$d%2$s",
                getRevision(),
                isObsolete() ? " (Obsolete)" : "");
    }

    /** Returns a long description for an {@link IDescription}. */
    @Override
    public String getLongDescription() {
        String s = getDescription();
        if (s == null || s.length() == 0) {
            s = getShortDescription();
        }

        if (s.indexOf("revision") == -1) {
            s += String.format("\nRevision %1$d%2$s",
                    getRevision(),
                    isObsolete() ? " (Obsolete)" : "");
        }

        return s;
    }

    /**
     * Computes a potential installation folder if an archive of this package were
     * to be installed right away in the given SDK root.
     * <p/>
     * A "tool" package should always be located in SDK/tools.
     *
     * @param osSdkRoot The OS path of the SDK root folder.
     * @param sdkManager An existing SDK manager to list current platforms and addons.
     * @return A new {@link File} corresponding to the directory to use to install this package.
     */
    @Override
    public File getInstallFolder(String osSdkRoot, SdkManager sdkManager) {
        return new File(osSdkRoot, SdkConstants.FD_TOOLS);
    }

    @Override
    public boolean sameItemAs(Package pkg) {
        // only one tool package so any tool package is the same item.
        return pkg instanceof ToolPackage;
    }

    @Override
    void saveProperties(Properties props) {
        super.saveProperties(props);

        if (getMinPlatformToolsRevision() != MIN_PLATFORM_TOOLS_REV_INVALID) {
            props.setProperty(PROP_MIN_PLATFORM_TOOLS_REV,
                              Integer.toString(getMinPlatformToolsRevision()));
        }
    }

    /**
     * The tool package executes tools/lib/post_tools_install[.bat|.sh]
     * {@inheritDoc}
     */
    @Override
    public void postInstallHook(Archive archive, ITaskMonitor monitor, File installFolder) {
        super.postInstallHook(archive, monitor, installFolder);

        if (installFolder == null) {
            return;
        }

        File libDir = new File(installFolder, SdkConstants.FD_LIB);
        if (!libDir.isDirectory()) {
            return;
        }

        String scriptName = "post_tools_install";   //$NON-NLS-1$
        String shell = "";                          //$NON-NLS-1$
        if (SdkConstants.currentPlatform() == SdkConstants.PLATFORM_WINDOWS) {
            shell = "cmd.exe /c ";                  //$NON-NLS-1$
            scriptName += ".bat";                   //$NON-NLS-1$
        } else {
            scriptName += ".sh";                    //$NON-NLS-1$
        }

        File scriptFile = new File(libDir, scriptName);
        if (!scriptFile.isFile()) {
            return;
        }

        Process proc;
        int status = -1;

        try {
            proc = Runtime.getRuntime().exec(
                    shell + scriptName, // command
                    null,       // environment
                    libDir);    // working dir

            status = grabProcessOutput(proc, monitor, scriptName);

        } catch (Exception e) {
            monitor.logError("Exception: %s", e.toString());
        }

        if (status != 0) {
            monitor.logError("Failed to execute %s", scriptName);
            return;
        }
    }

    /**
     * Gets the stderr/stdout outputs of a process and returns when the process is done.
     * Both <b>must</b> be read or the process will block on windows.
     * @param process The process to get the ouput from.
     * @param monitor The monitor where to output errors.
     * @param scriptName The name of script being executed.
     * @return the process return code.
     * @throws InterruptedException
     */
    private int grabProcessOutput(final Process process,
            final ITaskMonitor monitor,
            final String scriptName)
                throws InterruptedException {
        // read the lines as they come. if null is returned, it's
        // because the process finished
        Thread t1 = new Thread("") { //$NON-NLS-1$
            @Override
            public void run() {
                // create a buffer to read the stderr output
                InputStreamReader is = new InputStreamReader(process.getErrorStream());
                BufferedReader errReader = new BufferedReader(is);

                try {
                    while (true) {
                        String line = errReader.readLine();
                        if (line != null) {
                            monitor.logError("[%1$s] Error: %2$s", scriptName, line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                    // do nothing.
                }
            }
        };

        Thread t2 = new Thread("") { //$NON-NLS-1$
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getInputStream());
                BufferedReader outReader = new BufferedReader(is);

                try {
                    while (true) {
                        String line = outReader.readLine();
                        if (line != null) {
                            monitor.log("[%1$s] %2$s", scriptName, line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                    // do nothing.
                }
            }
        };

        t1.start();
        t2.start();

        // it looks like on windows process#waitFor() can return
        // before the thread have filled the arrays, so we wait for both threads and the
        // process itself.
        /* Disabled since not used. Do we really need this?
        if (waitforReaders) {
            try {
                t1.join();
            } catch (InterruptedException e) {
            }
            try {
                t2.join();
            } catch (InterruptedException e) {
            }
        }
        */

        // get the return code from the process
        return process.waitFor();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + mMinPlatformToolsRevision;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof ToolPackage)) {
            return false;
        }
        ToolPackage other = (ToolPackage) obj;
        if (mMinPlatformToolsRevision != other.mMinPlatformToolsRevision) {
            return false;
        }
        return true;
    }
}
