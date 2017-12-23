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
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.SdkConstants;
import com.android.sdklib.SdkManager;
import com.android.sdklib.IAndroidTarget.IOptionalLibrary;
import com.android.sdklib.internal.repository.Archive.Arch;
import com.android.sdklib.internal.repository.Archive.Os;
import com.android.sdklib.repository.PkgProps;
import com.android.sdklib.repository.SdkRepoConstants;
import com.android.util.Pair;

import org.w3c.dom.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

/**
 * Represents an add-on XML node in an SDK repository.
 */
public class AddonPackage extends Package
    implements IPackageVersion, IPlatformDependency, IExactApiLevelDependency, ILayoutlibVersion {

    private final String mVendor;
    private final String mName;
    private final AndroidVersion mVersion;

    /**
     * The helper handling the layoutlib version.
     */
    private final LayoutlibVersionMixin mLayoutlibVersion;

    /** An add-on library. */
    public static class Lib {
        private final String mName;
        private final String mDescription;

        public Lib(String name, String description) {
            mName = name;
            mDescription = description;
        }

        public String getName() {
            return mName;
        }

        public String getDescription() {
            return mDescription;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((mDescription == null) ? 0 : mDescription.hashCode());
            result = prime * result + ((mName == null) ? 0 : mName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Lib)) {
                return false;
            }
            Lib other = (Lib) obj;
            if (mDescription == null) {
                if (other.mDescription != null) {
                    return false;
                }
            } else if (!mDescription.equals(other.mDescription)) {
                return false;
            }
            if (mName == null) {
                if (other.mName != null) {
                    return false;
                }
            } else if (!mName.equals(other.mName)) {
                return false;
            }
            return true;
        }
    }

    private final Lib[] mLibs;

    /**
     * Creates a new add-on package from the attributes and elements of the given XML node.
     * This constructor should throw an exception if the package cannot be created.
     *
     * @param source The {@link SdkSource} where this is loaded from.
     * @param packageNode The XML element being parsed.
     * @param nsUri The namespace URI of the originating XML document, to be able to deal with
     *          parameters that vary according to the originating XML schema.
     * @param licenses The licenses loaded from the XML originating document.
     */
    AddonPackage(SdkSource source, Node packageNode, String nsUri, Map<String,String> licenses) {
        super(source, packageNode, nsUri, licenses);
        mVendor   = XmlParserUtils.getXmlString(packageNode, SdkRepoConstants.NODE_VENDOR);
        mName     = XmlParserUtils.getXmlString(packageNode, SdkRepoConstants.NODE_NAME);
        int apiLevel = XmlParserUtils.getXmlInt   (packageNode, SdkRepoConstants.NODE_API_LEVEL, 0);
        mVersion = new AndroidVersion(apiLevel, null /*codeName*/);

        mLibs = parseLibs(XmlParserUtils.getFirstChild(packageNode, SdkRepoConstants.NODE_LIBS));

        mLayoutlibVersion = new LayoutlibVersionMixin(packageNode);
    }

    /**
     * Creates a new platform package based on an actual {@link IAndroidTarget} (which
     * {@link IAndroidTarget#isPlatform()} false) from the {@link SdkManager}.
     * This is used to list local SDK folders in which case there is one archive which
     * URL is the actual target location.
     * <p/>
     * By design, this creates a package with one and only one archive.
     */
    static Package create(IAndroidTarget target, Properties props) {
        return new AddonPackage(target, props);
    }

    @VisibleForTesting(visibility=Visibility.PRIVATE)
    protected AddonPackage(IAndroidTarget target, Properties props) {
        this(null /*source*/, target, props);
    }

    @VisibleForTesting(visibility=Visibility.PRIVATE)
    protected AddonPackage(SdkSource source, IAndroidTarget target, Properties props) {
        super(  source,                     //source
                props,                      //properties
                target.getRevision(),       //revision
                null,                       //license
                target.getDescription(),    //description
                null,                       //descUrl
                Os.getCurrentOs(),          //archiveOs
                Arch.getCurrentArch(),      //archiveArch
                target.getLocation()        //archiveOsPath
                );

        mVersion = target.getVersion();
        mName     = target.getName();
        mVendor   = target.getVendor();
        mLayoutlibVersion = new LayoutlibVersionMixin(props);

        IOptionalLibrary[] optLibs = target.getOptionalLibraries();
        if (optLibs == null || optLibs.length == 0) {
            mLibs = new Lib[0];
        } else {
            mLibs = new Lib[optLibs.length];
            for (int i = 0; i < optLibs.length; i++) {
                mLibs[i] = new Lib(optLibs[i].getName(), optLibs[i].getDescription());
            }
        }
    }

    /**
     * Creates a broken addon which we know failed to load properly.
     *
     * @param archiveOsPath The absolute OS path of the addon folder.
     * @param props The properties parsed from the addon manifest (not the source.properties).
     * @param error The error indicating why this addon failed to be loaded.
     */
    static Package createBroken(String archiveOsPath, Map<String, String> props, String error) {
        String name     = props.get(SdkManager.ADDON_NAME);
        String vendor   = props.get(SdkManager.ADDON_VENDOR);
        String api      = props.get(SdkManager.ADDON_API);
        String revision = props.get(SdkManager.ADDON_REVISION);

        String shortDesc = String.format("%1$s by %2$s, Android API %3$s, revision %4$s [*]",
                name,
                vendor,
                api,
                revision);

        String longDesc = String.format(
                "%1$s\n" +
                "[*] Addon failed to load: %2$s",
                shortDesc,
                error);

        int apiLevel = IExactApiLevelDependency.API_LEVEL_INVALID;

        try {
            apiLevel = Integer.parseInt(api);
        } catch(NumberFormatException e) {
            // ignore
        }

        return new BrokenPackage(null/*props*/, shortDesc, longDesc,
                IMinApiLevelDependency.MIN_API_LEVEL_NOT_SPECIFIED,
                apiLevel,
                archiveOsPath);
    }

    public int getExactApiLevel() {
        return mVersion.getApiLevel();
    }

    /**
     * Save the properties of the current packages in the given {@link Properties} object.
     * These properties will later be given to a constructor that takes a {@link Properties} object.
     */
    @Override
    void saveProperties(Properties props) {
        super.saveProperties(props);

        mVersion.saveProperties(props);
        mLayoutlibVersion.saveProperties(props);

        if (mName != null) {
            props.setProperty(PkgProps.ADDON_NAME, mName);
        }
        if (mVendor != null) {
            props.setProperty(PkgProps.ADDON_VENDOR, mVendor);
        }
    }

    /**
     * Parses a <libs> element.
     */
    private Lib[] parseLibs(Node libsNode) {
        ArrayList<Lib> libs = new ArrayList<Lib>();

        if (libsNode != null) {
            String nsUri = libsNode.getNamespaceURI();
            for(Node child = libsNode.getFirstChild();
                child != null;
                child = child.getNextSibling()) {

                if (child.getNodeType() == Node.ELEMENT_NODE &&
                        nsUri.equals(child.getNamespaceURI()) &&
                        SdkRepoConstants.NODE_LIB.equals(child.getLocalName())) {
                    libs.add(parseLib(child));
                }
            }
        }

        return libs.toArray(new Lib[libs.size()]);
    }

    /**
     * Parses a <lib> element from a <libs> container.
     */
    private Lib parseLib(Node libNode) {
        return new Lib(XmlParserUtils.getXmlString(libNode, SdkRepoConstants.NODE_NAME),
                       XmlParserUtils.getXmlString(libNode, SdkRepoConstants.NODE_DESCRIPTION));
    }

    /** Returns the vendor, a string, for add-on packages. */
    public String getVendor() {
        return mVendor;
    }

    /** Returns the name, a string, for add-on packages or for libraries. */
    public String getName() {
        return mName;
    }

    /**
     * Returns the version of the platform dependency of this package.
     * <p/>
     * An add-on has the same {@link AndroidVersion} as the platform it depends on.
     */
    public AndroidVersion getVersion() {
        return mVersion;
    }

    /** Returns the libs defined in this add-on. Can be an empty array but not null. */
    public Lib[] getLibs() {
        return mLibs;
    }

    /**
     * Returns the layoutlib version.
     * <p/>
     * The first integer is the API of layoublib, which should be > 0.
     * It will be equal to {@link ILayoutlibVersion#LAYOUTLIB_API_NOT_SPECIFIED} (0)
     * if the layoutlib version isn't specified.
     * <p/>
     * The second integer is the revision for that given API. It is >= 0
     * and works as a minor revision number, incremented for the same API level.
     *
     * @since sdk-addon-2.xsd
     */
    public Pair<Integer, Integer> getLayoutlibVersion() {
        return mLayoutlibVersion.getLayoutlibVersion();
    }

    /**
     * Returns a string identifier to install this package from the command line.
     * For add-ons, we use "addon-vendor-name-N" where N is the base platform API.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public String installId() {
        return encodeAddonName();
    }

    /**
     * Returns a description of this package that is suitable for a list display.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public String getListDescription() {
        return String.format("%1$s by %2$s%3$s",
                getName(),
                getVendor(),
                isObsolete() ? " (Obsolete)" : "");
    }

    /**
     * Returns a short description for an {@link IDescription}.
     */
    @Override
    public String getShortDescription() {
        return String.format("%1$s by %2$s, Android API %3$s, revision %4$s%5$s",
                getName(),
                getVendor(),
                mVersion.getApiString(),
                getRevision(),
                isObsolete() ? " (Obsolete)" : "");
    }

    /**
     * Returns a long description for an {@link IDescription}.
     *
     * The long description is whatever the XML contains for the &lt;description&gt; field,
     * or the short description if the former is empty.
     */
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

        s += String.format("\nRequires SDK Platform Android API %1$s",
                mVersion.getApiString());
        return s;
    }

    /**
     * Computes a potential installation folder if an archive of this package were
     * to be installed right away in the given SDK root.
     * <p/>
     * An add-on package is typically installed in SDK/add-ons/"addon-name"-"api-level".
     * The name needs to be sanitized to be acceptable as a directory name.
     * However if we can find a different directory under SDK/add-ons that already
     * has this add-ons installed, we'll use that one.
     *
     * @param osSdkRoot The OS path of the SDK root folder.
     * @param sdkManager An existing SDK manager to list current platforms and addons.
     * @return A new {@link File} corresponding to the directory to use to install this package.
     */
    @Override
    public File getInstallFolder(String osSdkRoot, SdkManager sdkManager) {
        File addons = new File(osSdkRoot, SdkConstants.FD_ADDONS);

        // First find if this add-on is already installed. If so, reuse the same directory.
        for (IAndroidTarget target : sdkManager.getTargets()) {
            if (!target.isPlatform() &&
                    target.getVersion().equals(mVersion) &&
                    target.getName().equals(getName()) &&
                    target.getVendor().equals(getVendor())) {
                return new File(target.getLocation());
            }
        }

        // Compute a folder directory using the addon declared name and vendor strings.
        String name = encodeAddonName();

        for (int i = 0; i < 100; i++) {
            String name2 = i == 0 ? name : String.format("%s-%d", name, i); //$NON-NLS-1$
            File folder = new File(addons, name2);
            if (!folder.exists()) {
                return folder;
            }
        }

        // We shouldn't really get here. I mean, seriously, we tried hard enough.
        return null;
    }

    private String encodeAddonName() {
        String name = String.format("addon-%s-%s-%s",     //$NON-NLS-1$
                                    getName(), getVendor(), mVersion.getApiString());
        name = name.toLowerCase();
        name = name.replaceAll("[^a-z0-9_-]+", "_");      //$NON-NLS-1$ //$NON-NLS-2$
        name = name.replaceAll("_+", "_");                //$NON-NLS-1$ //$NON-NLS-2$
        return name;
    }

    @Override
    public boolean sameItemAs(Package pkg) {
        if (pkg instanceof AddonPackage) {
            AddonPackage newPkg = (AddonPackage)pkg;

            // check they are the same add-on.
            return getName().equals(newPkg.getName()) &&
                    getVendor().equals(newPkg.getVendor()) &&
                    getVersion().equals(newPkg.getVersion());
        }

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((mLayoutlibVersion == null) ? 0 : mLayoutlibVersion.hashCode());
        result = prime * result + Arrays.hashCode(mLibs);
        result = prime * result + ((mName == null) ? 0 : mName.hashCode());
        result = prime * result + ((mVendor == null) ? 0 : mVendor.hashCode());
        result = prime * result + ((mVersion == null) ? 0 : mVersion.hashCode());
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
        if (!(obj instanceof AddonPackage)) {
            return false;
        }
        AddonPackage other = (AddonPackage) obj;
        if (mLayoutlibVersion == null) {
            if (other.mLayoutlibVersion != null) {
                return false;
            }
        } else if (!mLayoutlibVersion.equals(other.mLayoutlibVersion)) {
            return false;
        }
        if (!Arrays.equals(mLibs, other.mLibs)) {
            return false;
        }
        if (mName == null) {
            if (other.mName != null) {
                return false;
            }
        } else if (!mName.equals(other.mName)) {
            return false;
        }
        if (mVendor == null) {
            if (other.mVendor != null) {
                return false;
            }
        } else if (!mVendor.equals(other.mVendor)) {
            return false;
        }
        if (mVersion == null) {
            if (other.mVersion != null) {
                return false;
            }
        } else if (!mVersion.equals(other.mVersion)) {
            return false;
        }
        return true;
    }
}
