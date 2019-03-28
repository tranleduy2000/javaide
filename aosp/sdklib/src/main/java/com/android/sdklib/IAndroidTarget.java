/*
 * Copyright (C) 2008 The Android Open Source Project
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

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.sdklib.repository.descriptors.IdDisplay;

import java.io.File;
import java.util.List;
import java.util.Map;



/**
 * A version of Android that applications can target when building.
 */
public interface IAndroidTarget extends Comparable<IAndroidTarget> {

    /** OS Path to the "android.jar" file. */
    int ANDROID_JAR         = 1;
    /** OS Path to the "framework.aidl" file. */
    int ANDROID_AIDL        = 2;
    /** OS Path to the "samples" folder which contains sample projects. */
    int SAMPLES             = 4;
    /** OS Path to the "skins" folder which contains the emulator skins. */
    int SKINS               = 5;
    /** OS Path to the "templates" folder which contains the templates for new projects. */
    int TEMPLATES           = 6;
    /** OS Path to the "data" folder which contains data & libraries for the SDK tools. */
    int DATA                = 7;
    /** OS Path to the "attrs.xml" file. */
    int ATTRIBUTES          = 8;
    /** OS Path to the "attrs_manifest.xml" file. */
    int MANIFEST_ATTRIBUTES = 9;
    /** OS Path to the "data/layoutlib.jar" library. */
    int LAYOUT_LIB          = 10;
    /** OS Path to the "data/res" folder. */
    int RESOURCES           = 11;
    /** OS Path to the "data/fonts" folder. */
    int FONTS               = 12;
    /** OS Path to the "data/widgets.txt" file. */
    int WIDGETS             = 13;
    /** OS Path to the "data/activity_actions.txt" file. */
    int ACTIONS_ACTIVITY    = 14;
    /** OS Path to the "data/broadcast_actions.txt" file. */
    int ACTIONS_BROADCAST   = 15;
    /** OS Path to the "data/service_actions.txt" file. */
    int ACTIONS_SERVICE     = 16;
    /** OS Path to the "data/categories.txt" file. */
    int CATEGORIES          = 17;
    /** OS Path to the "sources" folder. */
    int SOURCES             = 18;
    /** OS Path to the target specific docs */
    int DOCS                = 19;
    /** OS Path to the "ant" folder which contains the ant build rules (ver 2 and above) */
    int ANT                 = 24;
    /** OS Path to the "uiautomator.jar" file. */
    int UI_AUTOMATOR_JAR    = 27;


    /**
     * Return value for {@link #getUsbVendorId()} meaning no USB vendor IDs are defined by the
     * Android target.
     */
    int NO_USB_ID = 0;

    /** An optional library provided by an Android Target */
    interface OptionalLibrary {
        /** The name of the library, as used in the manifest (&lt;uses-library&gt;). */
        @NonNull
        String getName();
        /** Location of the jar file. */
        @NonNull
        File getJar();
        /** Description of the library. */
        @NonNull
        String getDescription();
        /** Whether the library requires a manifest entry */
        boolean isManifestEntryRequired();
    }

    /**
     * Returns the target location.
     */
    String getLocation();

    /**
     * Returns the name of the vendor of the target.
     */
    String getVendor();

    /**
     * Returns the name of the target.
     */
    String getName();

    /**
     * Returns the full name of the target, possibly including vendor name.
     */
    String getFullName();

    /**
     * Returns the name to be displayed when representing all the libraries this target contains.
     */
    String getClasspathName();

    /**
     * Returns the name to be displayed when representing all the libraries this target contains.
     */
    String getShortClasspathName();

    /**
     * Returns the description of the target.
     */
    String getDescription();

    /**
     * Returns the version of the target. This is guaranteed to be non-null.
     */
    @NonNull
    AndroidVersion getVersion();

    /**
     * Returns the platform version as a readable string.
     */
    String getVersionName();

    /** Returns the revision number for the target. */
    int getRevision();

    /**
     * Returns true if the target is a standard Android platform.
     */
    boolean isPlatform();

    /**
     * Returns the parent target. This is likely to only be non <code>null</code> if
     * {@link #isPlatform()} returns <code>false</code>
     */
    IAndroidTarget getParent();

    /**
     * Returns the path of a platform component.
     * @param pathId the id representing the path to return.
     *        Any of the constants defined in the {@link IAndroidTarget} interface can be used.
     */
    String getPath(int pathId);

    /**
     * Returns the path of a platform component.
     * <p/>
     * This is like the legacy {@link #getPath(int)} method except it returns a {@link File}.
     *
     * @param pathId the id representing the path to return.
     *        Any of the constants defined in the {@link IAndroidTarget} interface can be used.
     */
    File getFile(int pathId);

    /**
     * Returns a BuildToolInfo for backward compatibility. If an older SDK is used this will return
     * paths located in the platform-tools, otherwise it'll return paths located in the latest
     * build-tools.
     * @return a BuildToolInfo or null if none are available.
     */
    BuildToolInfo getBuildToolInfo();

    /**
     * Returns the boot classpath for this target.
     * In most case, this is similar to calling {@link #getPath(int)} with
     * {@link IAndroidTarget#ANDROID_JAR}.
     *
     * @return a non null list of the boot classpath.
     */
    @NonNull
    List<String> getBootClasspath();

    /**
     * Returns a list of optional libraries for this target.
     *
     * These libraries are not automatically added to the classpath.
     * Using them requires adding a <code>uses-library</code> entry in the manifest.
     *
     * @return a list of libraries.
     *
     * @see OptionalLibrary#getName()
     */
    @NonNull
    List<OptionalLibrary> getOptionalLibraries();

    /**
     * Returns the additional libraries for this target.
     *
     * These libraries are automatically added to the classpath, but using them requires
     * adding a <code>uses-library</code> entry in the manifest.
     *
     * @return a list of libraries.
     *
     * @see OptionalLibrary#getName()
     */
    @NonNull
    List<OptionalLibrary> getAdditionalLibraries();

    /**
     * Returns whether the target is able to render layouts.
     */
    boolean hasRenderingLibrary();

    /**
     * Returns the available skin folders for this target.
     * <p/>
     * To get the skin names, use {@link File#getName()}. <br/>
     * Skins come either from:
     * <ul>
     * <li>a platform ({@code sdk/platforms/N/skins/name})</li>
     * <li>an add-on ({@code sdk/addons/name/skins/name})</li>
     * <li>a tagged system-image ({@code sdk/system-images/platform-N/tag/abi/skins/name}.)</li>
     * </ul>
     * The array can be empty but not null.
     */
    @NonNull
    File[] getSkins();

    /**
     * Returns the default skin folder for this target.
     * <p/>
     * To get the skin name, use {@link File#getName()}.
     */
    @Nullable
    File getDefaultSkin();

    /**
     * Returns the list of libraries available for a given platform.
     *
     * @return an array of libraries provided by the platform or <code>null</code> if there is none.
     */
    String[] getPlatformLibraries();

    /**
     * Return the value of a given property for this target.
     * @return the property value or <code>null</code> if it was not found.
     */
    String getProperty(String name);

    /**
     * Returns the value of a given property for this target as an Integer value.
     * <p/> If the value is missing or is not an integer, the method will return the given default
     * value.
     * @param name the name of the property to return
     * @param defaultValue the default value to return.
     *
     * @see Integer#decode(String)
     */
    Integer getProperty(String name, Integer defaultValue);

    /**
     * Returns the value of a given property for this target as a Boolean value.
     * <p/> If the value is missing or is not an boolean, the method will return the given default
     * value.
     *
     * @param name the name of the property to return
     * @param defaultValue the default value to return.
     *
     * @see Boolean#valueOf(String)
     */

    Boolean getProperty(String name, Boolean defaultValue);

    /**
     * Returns all the properties associated with this target. This can be null if the target has
     * no properties.
     */
    Map<String, String> getProperties();

    /**
     * Returns the USB Vendor ID for the vendor of this target.
     * <p/>If the target defines no USB Vendor ID, then the method return 0.
     */
    int getUsbVendorId();

    /**
     * Returns an array of system images for this target.
     * The array can be empty but not null.
     */
    ISystemImage[] getSystemImages();

    /**
     * Returns the system image information for the given {@code tag} and {@code abiType}.
     *
     * @param tag A tag id-display.
     * @param abiType An ABI type string.
     * @return An existing {@link ISystemImage} for the requested {@code abiType}
     *         or null if none exists for this type.
     */
    @Nullable
    ISystemImage getSystemImage(@NonNull IdDisplay tag, @NonNull String abiType);

    /**
     * Returns whether the given target is compatible with the receiver.
     * <p/>
     * This means that a project using the receiver's target can run on the given target.
     * <br/>
     * Example:
     * <pre>
     * CupcakeTarget.canRunOn(DonutTarget) == true
     * </pre>.
     *
     * @param target the IAndroidTarget to test.
     */
    boolean canRunOn(IAndroidTarget target);

    /**
     * Returns a string able to uniquely identify a target.
     * Typically the target will encode information such as api level, whether it's a platform
     * or add-on, and if it's an add-on vendor and add-on name.
     * <p/>
     * See {@link AndroidTargetHash} for helper methods to manipulate hash strings.
     */
    String hashString();
}
