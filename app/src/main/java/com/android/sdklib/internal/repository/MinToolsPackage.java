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

import com.android.sdklib.internal.repository.Archive.Arch;
import com.android.sdklib.internal.repository.Archive.Os;
import com.android.sdklib.repository.SdkRepository;

import org.w3c.dom.Node;

import java.util.Map;
import java.util.Properties;

/**
 * Represents an XML node in an SDK repository that has a min-tools-rev requirement.
 */
public abstract class MinToolsPackage extends Package
    implements IMinToolsDependency {

    protected static final String PROP_MIN_TOOLS_REV = "Platform.MinToolsRev";  //$NON-NLS-1$

    /**
     * The minimal revision of the tools package required by this extra package, if > 0,
     * or {@link #MIN_TOOLS_REV_NOT_SPECIFIED} if there is no such requirement.
     */
    private final int mMinToolsRevision;

    /**
     * Creates a new package from the attributes and elements of the given XML node.
     * <p/>
     * This constructor should throw an exception if the package cannot be created.
     */
    MinToolsPackage(RepoSource source, Node packageNode, Map<String,String> licenses) {
        super(source, packageNode, licenses);

        mMinToolsRevision = XmlParserUtils.getXmlInt(packageNode, SdkRepository.NODE_MIN_TOOLS_REV,
                MIN_TOOLS_REV_NOT_SPECIFIED);
    }

    /**
     * Manually create a new package with one archive and the given attributes.
     * This is used to create packages from local directories in which case there must be
     * one archive which URL is the actual target location.
     * <p/>
     * Properties from props are used first when possible, e.g. if props is non null.
     * <p/>
     * By design, this creates a package with one and only one archive.
     */
    public MinToolsPackage(
            RepoSource source,
            Properties props,
            int revision,
            String license,
            String description,
            String descUrl,
            Os archiveOs,
            Arch archiveArch,
            String archiveOsPath) {
        super(source, props, revision, license, description, descUrl,
                archiveOs, archiveArch, archiveOsPath);

        mMinToolsRevision = Integer.parseInt(
            getProperty(props, PROP_MIN_TOOLS_REV, Integer.toString(MIN_TOOLS_REV_NOT_SPECIFIED)));
    }

    /**
     * The minimal revision of the tools package required by this extra package, if > 0,
     * or {@link #MIN_TOOLS_REV_NOT_SPECIFIED} if there is no such requirement.
     */
    public int getMinToolsRevision() {
        return mMinToolsRevision;
    }

    @Override
    void saveProperties(Properties props) {
        super.saveProperties(props);

        if (getMinToolsRevision() != MIN_TOOLS_REV_NOT_SPECIFIED) {
            props.setProperty(PROP_MIN_TOOLS_REV, Integer.toString(getMinToolsRevision()));
        }
    }
}
