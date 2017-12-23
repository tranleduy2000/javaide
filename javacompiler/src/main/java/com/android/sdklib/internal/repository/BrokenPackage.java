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

package com.android.sdklib.internal.repository;

import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.repository.Archive.Arch;
import com.android.sdklib.internal.repository.Archive.Os;

import java.io.File;
import java.util.Properties;

/**
 * Represents an SDK repository package that is incomplete.
 * It has a distinct icon and a specific error that is supposed to help the user on how to fix it.
 */
public class BrokenPackage extends Package
        implements IExactApiLevelDependency, IMinApiLevelDependency {

    /**
     * The minimal API level required by this package, if > 0,
     * or {@link #MIN_API_LEVEL_NOT_SPECIFIED} if there is no such requirement.
     */
    private final int mMinApiLevel;

    /**
     * The exact API level required by this package, if > 0,
     * or {@link #API_LEVEL_INVALID} if there is no such requirement.
     */
    private final int mExactApiLevel;

    private final String mShortDescription;
    private final String mLongDescription;

    /**
     * Creates a new "broken" package that represents a package that we failed to load,
     * for whatever error indicated in {@code longDescription}.
     * There is also an <em>optional</em> API level dependency that can be specified.
     * <p/>
     * By design, this creates a package with one and only one archive.
     */
    BrokenPackage(Properties props,
            String shortDescription,
            String longDescription,
            int minApiLevel,
            int exactApiLevel,
            String archiveOsPath) {
        super(  null,                                   //source
                props,                                  //properties
                0,                                      //revision will be taken from props
                null,                                   //license
                longDescription,                        //description
                null,                                   //descUrl
                Os.ANY,                                 //archiveOs
                Arch.ANY,                               //archiveArch
                archiveOsPath                           //archiveOsPath
                );
        mShortDescription = shortDescription;
        mLongDescription = longDescription;
        mMinApiLevel = minApiLevel;
        mExactApiLevel = exactApiLevel;
    }

    /**
     * Save the properties of the current packages in the given {@link Properties} object.
     * These properties will later be given to a constructor that takes a {@link Properties} object.
     * <p/>
     * Base implementation override: We don't actually save properties for a broken package.
     */
    @Override
    void saveProperties(Properties props) {
        // Nop. We don't actually save properties for a broken package.
    }

    /**
     * Returns the minimal API level required by this package, if > 0,
     * or {@link #MIN_API_LEVEL_NOT_SPECIFIED} if there is no such requirement.
     */
    public int getMinApiLevel() {
        return mMinApiLevel;
    }

    /**
     * Returns the exact API level required by this package, if > 0,
     * or {@link #API_LEVEL_INVALID} if the value was missing.
     */
    public int getExactApiLevel() {
        return mExactApiLevel;
    }

    /**
     * Returns a string identifier to install this package from the command line.
     * For broken packages, we return an empty string. These are not installable.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public String installId() {
        return "";    //$NON-NLS-1$
    }

    /**
     * Returns a description of this package that is suitable for a list display.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public String getListDescription() {
        return mShortDescription;
    }

    /**
     * Returns a short description for an {@link IDescription}.
     */
    @Override
    public String getShortDescription() {
        return mShortDescription;
    }

    /**
     * Returns a long description for an {@link IDescription}.
     *
     * The long description uses what was given to the constructor.
     * If it's missing, it will use whatever the XML contains for the &lt;description&gt; field,
     * or the short description if the former is empty.
     */
    @Override
    public String getLongDescription() {

        String s = mLongDescription;
        if (s != null && s.length() != 0) {
            return s;
        }

        s = getDescription();
        if (s != null && s.length() != 0) {
            return s;
        }
        return getShortDescription();
    }

    /**
     * We should not be attempting to install a broken package.
     *
     * {@inheritDoc}
     */
    @Override
    public File getInstallFolder(String osSdkRoot, SdkManager sdkManager) {
        // We should not be attempting to install a broken package.
        return null;
    }

    @Override
    public boolean sameItemAs(Package pkg) {
        if (pkg instanceof BrokenPackage) {
            return mShortDescription.equals(((BrokenPackage) pkg).mShortDescription) &&
                getDescription().equals(pkg.getDescription()) &&
                getMinApiLevel() == ((BrokenPackage) pkg).getMinApiLevel();
        }

        return false;
    }

    @Override
    public boolean preInstallHook(Archive archive,
            ITaskMonitor monitor,
            String osSdkRoot,
            File installFolder) {
        // Nothing specific to do.
        return super.preInstallHook(archive, monitor, osSdkRoot, installFolder);
    }

    /**
     * Computes a hash of the installed content (in case of successful install.)
     *
     * {@inheritDoc}
     */
    @Override
    public void postInstallHook(Archive archive, ITaskMonitor monitor, File installFolder) {
        // Nothing specific to do.
        super.postInstallHook(archive, monitor, installFolder);
    }
}
