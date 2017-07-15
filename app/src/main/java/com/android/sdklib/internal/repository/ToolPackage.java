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

import com.android.sdklib.SdkConstants;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.repository.Archive.Arch;
import com.android.sdklib.internal.repository.Archive.Os;

import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

/**
 * Represents a tool XML node in an SDK repository.
 */
public class ToolPackage extends Package {

    /**
     * Creates a new tool package from the attributes and elements of the given XML node.
     * <p/>
     * This constructor should throw an exception if the package cannot be created.
     */
    ToolPackage(RepoSource source, Node packageNode, Map<String,String> licenses) {
        super(source, packageNode, licenses);
    }

    /**
     * Manually create a new package with one archive and the given attributes or properties.
     * This is used to create packages from local directories in which case there must be
     * one archive which URL is the actual target location.
     * <p/>
     * By design, this creates a package with one and only one archive.
     */
    ToolPackage(
            RepoSource source,
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
    }

    /** Returns a short description for an {@link IDescription}. */
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
     * @param suggestedDir A suggestion for the installation folder name, based on the root
     *                     folder used in the zip archive.
     * @param sdkManager An existing SDK manager to list current platforms and addons.
     * @return A new {@link File} corresponding to the directory to use to install this package.
     */
    @Override
    public File getInstallFolder(String osSdkRoot, String suggestedDir, SdkManager sdkManager) {
        return new File(osSdkRoot, SdkConstants.FD_TOOLS);
    }

    @Override
    public boolean sameItemAs(Package pkg) {
        // only one tool package so any tool package is the same item.
        return pkg instanceof ToolPackage;
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
        String shell = "";
        if (SdkConstants.currentPlatform() == SdkConstants.PLATFORM_WINDOWS) {
            shell = "cmd.exe /c ";
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
            monitor.setResult("Exception: %s", e.toString());
        }

        if (status != 0) {
            monitor.setResult("Failed to execute %s", scriptName);
            return;
        }
    }

    /**
     * Get the stderr/stdout outputs of a process and return when the process is done.
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
                            monitor.setResult("[%1$s] Error: %2$s", scriptName, line);
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
                            monitor.setResult("[%1$s] %2$s", scriptName, line);
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
}
