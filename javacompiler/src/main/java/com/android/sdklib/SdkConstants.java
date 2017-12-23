/*
 * Copyright (C) 2007 The Android Open Source Project
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

import com.android.AndroidConstants;

import java.io.File;

/**
 * Constant definition class.<br>
 * <br>
 * Most constants have a prefix defining the content.
 * <ul>
 * <li><code>OS_</code> OS path constant. These paths are different depending on the platform.</li>
 * <li><code>FN_</code> File name constant.</li>
 * <li><code>FD_</code> Folder name constant.</li>
 * </ul>
 *
 */
public final class SdkConstants {
    public final static int PLATFORM_UNKNOWN = 0;
    public final static int PLATFORM_LINUX = 1;
    public final static int PLATFORM_WINDOWS = 2;
    public final static int PLATFORM_DARWIN = 3;

    /**
     * Returns current platform, one of {@link #PLATFORM_WINDOWS}, {@link #PLATFORM_DARWIN},
     * {@link #PLATFORM_LINUX} or {@link #PLATFORM_UNKNOWN}.
     */
    public final static int CURRENT_PLATFORM = currentPlatform();

    /**
     * Charset for the ini file handled by the SDK.
     */
    public final static String INI_CHARSET = "UTF-8";                                 //$NON-NLS-1$

    /** An SDK Project's AndroidManifest.xml file */
    public static final String FN_ANDROID_MANIFEST_XML= "AndroidManifest.xml";        //$NON-NLS-1$
    /** pre-dex jar filename. i.e. "classes.jar" */
    public final static String FN_CLASSES_JAR = "classes.jar";                        //$NON-NLS-1$
    /** Dex filename inside the APK. i.e. "classes.dex" */
    public final static String FN_APK_CLASSES_DEX = "classes.dex";                    //$NON-NLS-1$

    /** An SDK Project's build.xml file */
    public final static String FN_BUILD_XML = "build.xml";                            //$NON-NLS-1$

    /** Name of the framework library, i.e. "android.jar" */
    public static final String FN_FRAMEWORK_LIBRARY = "android.jar";                  //$NON-NLS-1$
    /** Name of the layout attributes, i.e. "attrs.xml" */
    public static final String FN_ATTRS_XML = "attrs.xml";                            //$NON-NLS-1$
    /** Name of the layout attributes, i.e. "attrs_manifest.xml" */
    public static final String FN_ATTRS_MANIFEST_XML = "attrs_manifest.xml";          //$NON-NLS-1$
    /** framework aidl import file */
    public static final String FN_FRAMEWORK_AIDL = "framework.aidl";                  //$NON-NLS-1$
    /** framework renderscript folder */
    public static final String FN_FRAMEWORK_RENDERSCRIPT = "renderscript";            //$NON-NLS-1$
    /** framework include folder */
    public static final String FN_FRAMEWORK_INCLUDE = "include";                      //$NON-NLS-1$
    /** framework include (clang) folder */
    public static final String FN_FRAMEWORK_INCLUDE_CLANG = "clang-include";          //$NON-NLS-1$
    /** layoutlib.jar file */
    public static final String FN_LAYOUTLIB_JAR = "layoutlib.jar";                    //$NON-NLS-1$
    /** widget list file */
    public static final String FN_WIDGETS = "widgets.txt";                            //$NON-NLS-1$
    /** Intent activity actions list file */
    public static final String FN_INTENT_ACTIONS_ACTIVITY = "activity_actions.txt";   //$NON-NLS-1$
    /** Intent broadcast actions list file */
    public static final String FN_INTENT_ACTIONS_BROADCAST = "broadcast_actions.txt"; //$NON-NLS-1$
    /** Intent service actions list file */
    public static final String FN_INTENT_ACTIONS_SERVICE = "service_actions.txt";     //$NON-NLS-1$
    /** Intent category list file */
    public static final String FN_INTENT_CATEGORIES = "categories.txt";               //$NON-NLS-1$

    /** platform build property file */
    public final static String FN_BUILD_PROP = "build.prop";                          //$NON-NLS-1$
    /** plugin properties file */
    public final static String FN_PLUGIN_PROP = "plugin.prop";                        //$NON-NLS-1$
    /** add-on manifest file */
    public final static String FN_MANIFEST_INI = "manifest.ini";                      //$NON-NLS-1$
    /** add-on layout device XML file. */
    public final static String FN_DEVICES_XML = "devices.xml";                        //$NON-NLS-1$
    /** hardware properties definition file */
    public final static String FN_HARDWARE_INI = "hardware-properties.ini";           //$NON-NLS-1$

    /** project property file */
    public final static String FN_PROJECT_PROPERTIES = "project.properties";          //$NON-NLS-1$

    /** project local property file */
    public final static String FN_LOCAL_PROPERTIES = "local.properties";              //$NON-NLS-1$

    /** project ant property file */
    public final static String FN_ANT_PROPERTIES = "ant.properties";                  //$NON-NLS-1$

    /** Skin layout file */
    public final static String FN_SKIN_LAYOUT = "layout";                             //$NON-NLS-1$

    /** dx.jar file */
    public static final String FN_DX_JAR = "dx.jar";                                  //$NON-NLS-1$

    /** dx executable (with extension for the current OS)  */
    public final static String FN_DX = (CURRENT_PLATFORM == PLATFORM_WINDOWS) ?
            "dx.bat" : "dx";                                            //$NON-NLS-1$ //$NON-NLS-2$

    /** aapt executable (with extension for the current OS)  */
    public final static String FN_AAPT = (CURRENT_PLATFORM == PLATFORM_WINDOWS) ?
            "aapt.exe" : "aapt";                                        //$NON-NLS-1$ //$NON-NLS-2$

    /** aidl executable (with extension for the current OS)  */
    public final static String FN_AIDL = (CURRENT_PLATFORM == PLATFORM_WINDOWS) ?
            "aidl.exe" : "aidl";                                        //$NON-NLS-1$ //$NON-NLS-2$

    /** renderscript executable (with extension for the current OS)  */
    public final static String FN_RENDERSCRIPT = (CURRENT_PLATFORM == PLATFORM_WINDOWS) ?
            "llvm-rs-cc.exe" : "llvm-rs-cc";                            //$NON-NLS-1$ //$NON-NLS-2$

    /** adb executable (with extension for the current OS)  */
    public final static String FN_ADB = (CURRENT_PLATFORM == PLATFORM_WINDOWS) ?
            "adb.exe" : "adb";                                          //$NON-NLS-1$ //$NON-NLS-2$

    /** emulator executable for the current OS */
    public final static String FN_EMULATOR = (CURRENT_PLATFORM == PLATFORM_WINDOWS) ?
            "emulator.exe" : "emulator";                                //$NON-NLS-1$ //$NON-NLS-2$

    /** zipalign executable (with extension for the current OS)  */
    public final static String FN_ZIPALIGN = (CURRENT_PLATFORM == PLATFORM_WINDOWS) ?
            "zipalign.exe" : "zipalign";                                //$NON-NLS-1$ //$NON-NLS-2$

    /** dexdump executable (with extension for the current OS)  */
    public final static String FN_DEXDUMP = (CURRENT_PLATFORM == PLATFORM_WINDOWS) ?
            "dexdump.exe" : "dexdump";                                  //$NON-NLS-1$ //$NON-NLS-2$

    /** zipalign executable (with extension for the current OS)  */
    public final static String FN_PROGUARD = (CURRENT_PLATFORM == PLATFORM_WINDOWS) ?
            "proguard.bat" : "proguard.sh";                             //$NON-NLS-1$ //$NON-NLS-2$

    /** properties file for SDK Updater packages */
    public final static String FN_SOURCE_PROP = "source.properties";                  //$NON-NLS-1$
    /** properties file for content hash of installed packages */
    public final static String FN_CONTENT_HASH_PROP = "content_hash.properties";      //$NON-NLS-1$
    /** properties file for the SDK */
    public final static String FN_SDK_PROP = "sdk.properties";                        //$NON-NLS-1$

    /**
     * filename for gdbserver.
     */
    public final static String FN_GDBSERVER = "gdbserver";              //$NON-NLS-1$

    /** default proguard config file */
    public final static String FN_PROGUARD_CFG = "proguard.cfg";        //$NON-NLS-1$

    /* Folder Names for Android Projects . */

    /** Resources folder name, i.e. "res". */
    public final static String FD_RESOURCES = "res";                    //$NON-NLS-1$
    /** Assets folder name, i.e. "assets" */
    public final static String FD_ASSETS = "assets";                    //$NON-NLS-1$
    /** Default source folder name in an SDK project, i.e. "src".
     * <p/>
     * Note: this is not the same as {@link #FD_PKG_SOURCES}
     * which is an SDK sources folder for packages. */
    public final static String FD_SOURCES = "src";                      //$NON-NLS-1$
    /** Default generated source folder name, i.e. "gen" */
    public final static String FD_GEN_SOURCES = "gen";                  //$NON-NLS-1$
    /** Default native library folder name inside the project, i.e. "libs"
     * While the folder inside the .apk is "lib", we call that one libs because
     * that's what we use in ant for both .jar and .so and we need to make the 2 development ways
     * compatible. */
    public final static String FD_NATIVE_LIBS = "libs";                 //$NON-NLS-1$
    /** Native lib folder inside the APK: "lib" */
    public final static String FD_APK_NATIVE_LIBS = "lib";              //$NON-NLS-1$
    /** Default output folder name, i.e. "bin" */
    public final static String FD_OUTPUT = "bin";                       //$NON-NLS-1$
    /** Classes output folder name, i.e. "classes" */
    public final static String FD_CLASSES_OUTPUT = "classes";           //$NON-NLS-1$
    /** proguard output folder for mapping, etc.. files */
    public final static String FD_PROGUARD = "proguard";                //$NON-NLS-1$

    /* Folder Names for the Android SDK */

    /** Name of the SDK platforms folder. */
    public final static String FD_PLATFORMS = "platforms";              //$NON-NLS-1$
    /** Name of the SDK addons folder. */
    public final static String FD_ADDONS = "add-ons";                   //$NON-NLS-1$
    /** Name of the SDK system-images folder. */
    public final static String FD_SYSTEM_IMAGES = "system-images";      //$NON-NLS-1$
    /** Name of the SDK sources folder where source packages are installed.
     * <p/>
     * Note this is not the same as {@link #FD_SOURCES} which is the folder name where sources
     * are installed inside a project. */
    public final static String FD_PKG_SOURCES = "sources";              //$NON-NLS-1$
    /** Name of the SDK tools folder. */
    public final static String FD_TOOLS = "tools";                      //$NON-NLS-1$
    /** Name of the SDK platform tools folder. */
    public final static String FD_PLATFORM_TOOLS = "platform-tools";    //$NON-NLS-1$
    /** Name of the SDK tools/lib folder. */
    public final static String FD_LIB = "lib";                          //$NON-NLS-1$
    /** Name of the SDK docs folder. */
    public final static String FD_DOCS = "docs";                        //$NON-NLS-1$
    /** Name of the doc folder containing API reference doc (javadoc) */
    public static final String FD_DOCS_REFERENCE = "reference";         //$NON-NLS-1$
    /** Name of the SDK images folder. */
    public final static String FD_IMAGES = "images";                    //$NON-NLS-1$
    /** Name of the ABI to support. */
    public final static String ABI_ARMEABI = "armeabi";                 //$NON-NLS-1$
    public final static String ABI_ARMEABI_V7A = "armeabi-v7a";         //$NON-NLS-1$
    public final static String ABI_INTEL_ATOM = "x86";                  //$NON-NLS-1$
    /** Name of the CPU arch to support. */
    public final static String CPU_ARCH_ARM = "arm";                    //$NON-NLS-1$
    public final static String CPU_ARCH_INTEL_ATOM = "x86";             //$NON-NLS-1$
    /** Name of the CPU model to support. */
    public final static String CPU_MODEL_CORTEX_A8 = "cortex-a8";       //$NON-NLS-1$

    /** Name of the SDK skins folder. */
    public final static String FD_SKINS = "skins";                      //$NON-NLS-1$
    /** Name of the SDK samples folder. */
    public final static String FD_SAMPLES = "samples";                  //$NON-NLS-1$
    /** Name of the SDK extras folder. */
    public final static String FD_EXTRAS = "extras";                    //$NON-NLS-1$
    /** Name of the SDK templates folder, i.e. "templates" */
    public final static String FD_TEMPLATES = "templates";              //$NON-NLS-1$
    /** Name of the SDK Ant folder, i.e. "ant" */
    public final static String FD_ANT = "ant";                          //$NON-NLS-1$
    /** Name of the SDK data folder, i.e. "data" */
    public final static String FD_DATA = "data";                        //$NON-NLS-1$
    /** Name of the SDK renderscript folder, i.e. "rs" */
    public final static String FD_RENDERSCRIPT = "rs";                  //$NON-NLS-1$
    /** Name of the SDK resources folder, i.e. "res" */
    public final static String FD_RES = "res";                          //$NON-NLS-1$
    /** Name of the SDK font folder, i.e. "fonts" */
    public final static String FD_FONTS = "fonts";                      //$NON-NLS-1$
    /** Name of the android sources directory */
    public static final String FD_ANDROID_SOURCES = "sources";          //$NON-NLS-1$
    /** Name of the addon libs folder. */
    public final static String FD_ADDON_LIBS = "libs";                  //$NON-NLS-1$

    /** Namespace for the resource XML, i.e. "http://schemas.android.com/apk/res/android" */
    public final static String NS_RESOURCES =
        "http://schemas.android.com/apk/res/android";                   //$NON-NLS-1$

    /** The name of the uses-library that provides "android.test.runner" */
    public final static String ANDROID_TEST_RUNNER_LIB =
        "android.test.runner";                                          //$NON-NLS-1$

    /* Folder path relative to the SDK root */
    /** Path of the documentation directory relative to the sdk folder.
     *  This is an OS path, ending with a separator. */
    public final static String OS_SDK_DOCS_FOLDER = FD_DOCS + File.separator;

    /** Path of the tools directory relative to the sdk folder, or to a platform folder.
     *  This is an OS path, ending with a separator. */
    public final static String OS_SDK_TOOLS_FOLDER = FD_TOOLS + File.separator;

    /** Path of the lib directory relative to the sdk folder, or to a platform folder.
     *  This is an OS path, ending with a separator. */
    public final static String OS_SDK_TOOLS_LIB_FOLDER =
            OS_SDK_TOOLS_FOLDER + FD_LIB + File.separator;

    /**
     * Path of the lib directory relative to the sdk folder, or to a platform
     * folder. This is an OS path, ending with a separator.
     */
    public final static String OS_SDK_TOOLS_LIB_EMULATOR_FOLDER = OS_SDK_TOOLS_LIB_FOLDER
            + "emulator" + File.separator;                              //$NON-NLS-1$

    /** Path of the platform tools directory relative to the sdk folder.
     *  This is an OS path, ending with a separator. */
    public final static String OS_SDK_PLATFORM_TOOLS_FOLDER = FD_PLATFORM_TOOLS + File.separator;

    /** Path of the Platform tools Lib directory relative to the sdk folder.
     *  This is an OS path, ending with a separator. */
    public final static String OS_SDK_PLATFORM_TOOLS_LIB_FOLDER =
            OS_SDK_PLATFORM_TOOLS_FOLDER + FD_LIB + File.separator;

    /** Path of the bin folder of proguard folder relative to the sdk folder.
     *  This is an OS path, ending with a separator. */
    public final static String OS_SDK_TOOLS_PROGUARD_BIN_FOLDER =
        SdkConstants.OS_SDK_TOOLS_FOLDER +
        "proguard" + File.separator +                                   //$NON-NLS-1$
        "bin" + File.separator;                                         //$NON-NLS-1$

    /* Folder paths relative to a platform or add-on folder */

    /** Path of the images directory relative to a platform or addon folder.
     *  This is an OS path, ending with a separator. */
    public final static String OS_IMAGES_FOLDER = FD_IMAGES + File.separator;

    /** Path of the skin directory relative to a platform or addon folder.
     *  This is an OS path, ending with a separator. */
    public final static String OS_SKINS_FOLDER = FD_SKINS + File.separator;

    /* Folder paths relative to a Platform folder */

    /** Path of the data directory relative to a platform folder.
     *  This is an OS path, ending with a separator. */
    public final static String OS_PLATFORM_DATA_FOLDER = FD_DATA + File.separator;

    /** Path of the renderscript directory relative to a platform folder.
     *  This is an OS path, ending with a separator. */
    public final static String OS_PLATFORM_RENDERSCRIPT_FOLDER = FD_RENDERSCRIPT + File.separator;


    /** Path of the samples directory relative to a platform folder.
     *  This is an OS path, ending with a separator. */
    public final static String OS_PLATFORM_SAMPLES_FOLDER = FD_SAMPLES + File.separator;

    /** Path of the resources directory relative to a platform folder.
     *  This is an OS path, ending with a separator. */
    public final static String OS_PLATFORM_RESOURCES_FOLDER =
            OS_PLATFORM_DATA_FOLDER + FD_RES + File.separator;

    /** Path of the fonts directory relative to a platform folder.
     *  This is an OS path, ending with a separator. */
    public final static String OS_PLATFORM_FONTS_FOLDER =
            OS_PLATFORM_DATA_FOLDER + FD_FONTS + File.separator;

    /** Path of the android source directory relative to a platform folder.
     *  This is an OS path, ending with a separator. */
    public final static String OS_PLATFORM_SOURCES_FOLDER = FD_ANDROID_SOURCES + File.separator;

    /** Path of the android templates directory relative to a platform folder.
     *  This is an OS path, ending with a separator. */
    public final static String OS_PLATFORM_TEMPLATES_FOLDER = FD_TEMPLATES + File.separator;

    /** Path of the Ant build rules directory relative to a platform folder.
     *  This is an OS path, ending with a separator. */
    public final static String OS_PLATFORM_ANT_FOLDER = FD_ANT + File.separator;

    /** Path of the attrs.xml file relative to a platform folder. */
    public final static String OS_PLATFORM_ATTRS_XML =
            OS_PLATFORM_RESOURCES_FOLDER + AndroidConstants.FD_RES_VALUES + File.separator +
            FN_ATTRS_XML;

    /** Path of the attrs_manifest.xml file relative to a platform folder. */
    public final static String OS_PLATFORM_ATTRS_MANIFEST_XML =
            OS_PLATFORM_RESOURCES_FOLDER + AndroidConstants.FD_RES_VALUES + File.separator +
            FN_ATTRS_MANIFEST_XML;

    /** Path of the layoutlib.jar file relative to a platform folder. */
    public final static String OS_PLATFORM_LAYOUTLIB_JAR =
            OS_PLATFORM_DATA_FOLDER + FN_LAYOUTLIB_JAR;

    /** Path of the renderscript include folder relative to a platform folder. */
    public final static String OS_FRAMEWORK_RS =
            FN_FRAMEWORK_RENDERSCRIPT + File.separator + FN_FRAMEWORK_INCLUDE;
    /** Path of the renderscript (clang) include folder relative to a platform folder. */
    public final static String OS_FRAMEWORK_RS_CLANG =
            FN_FRAMEWORK_RENDERSCRIPT + File.separator + FN_FRAMEWORK_INCLUDE_CLANG;

    /* Folder paths relative to a addon folder */

    /** Path of the images directory relative to a folder folder.
     *  This is an OS path, ending with a separator. */
    public final static String OS_ADDON_LIBS_FOLDER = FD_ADDON_LIBS + File.separator;

    /** Skin default **/
    public final static String SKIN_DEFAULT = "default";                    //$NON-NLS-1$

    /** SDK property: ant templates revision */
    public final static String PROP_SDK_ANT_TEMPLATES_REVISION =
        "sdk.ant.templates.revision";                                       //$NON-NLS-1$

    /** SDK property: default skin */
    public final static String PROP_SDK_DEFAULT_SKIN = "sdk.skin.default"; //$NON-NLS-1$


    /* Android Class Constants */
    public final static String CLASS_ACTIVITY = "android.app.Activity"; //$NON-NLS-1$
    public final static String CLASS_APPLICATION = "android.app.Application"; //$NON-NLS-1$
    public final static String CLASS_SERVICE = "android.app.Service"; //$NON-NLS-1$
    public final static String CLASS_BROADCASTRECEIVER = "android.content.BroadcastReceiver"; //$NON-NLS-1$
    public final static String CLASS_CONTENTPROVIDER = "android.content.ContentProvider"; //$NON-NLS-1$
    public final static String CLASS_INSTRUMENTATION = "android.app.Instrumentation"; //$NON-NLS-1$
    public final static String CLASS_INSTRUMENTATION_RUNNER =
        "android.test.InstrumentationTestRunner"; //$NON-NLS-1$
    public final static String CLASS_BUNDLE = "android.os.Bundle"; //$NON-NLS-1$
    public final static String CLASS_R = "android.R"; //$NON-NLS-1$
    public final static String CLASS_MANIFEST_PERMISSION = "android.Manifest$permission"; //$NON-NLS-1$
    public final static String CLASS_INTENT = "android.content.Intent"; //$NON-NLS-1$
    public final static String CLASS_CONTEXT = "android.content.Context"; //$NON-NLS-1$
    public final static String CLASS_VIEW = "android.view.View"; //$NON-NLS-1$
    public final static String CLASS_VIEWGROUP = "android.view.ViewGroup"; //$NON-NLS-1$
    public final static String CLASS_NAME_LAYOUTPARAMS = "LayoutParams"; //$NON-NLS-1$
    public final static String CLASS_VIEWGROUP_LAYOUTPARAMS =
        CLASS_VIEWGROUP + "$" + CLASS_NAME_LAYOUTPARAMS; //$NON-NLS-1$
    public final static String CLASS_NAME_FRAMELAYOUT = "FrameLayout"; //$NON-NLS-1$
    public final static String CLASS_FRAMELAYOUT =
        "android.widget." + CLASS_NAME_FRAMELAYOUT; //$NON-NLS-1$
    public final static String CLASS_PREFERENCE = "android.preference.Preference"; //$NON-NLS-1$
    public final static String CLASS_NAME_PREFERENCE_SCREEN = "PreferenceScreen"; //$NON-NLS-1$
    public final static String CLASS_PREFERENCES =
        "android.preference." + CLASS_NAME_PREFERENCE_SCREEN; //$NON-NLS-1$
    public final static String CLASS_PREFERENCEGROUP = "android.preference.PreferenceGroup"; //$NON-NLS-1$
    public final static String CLASS_PARCELABLE = "android.os.Parcelable"; //$NON-NLS-1$
    public static final String CLASS_FRAGMENT = "android.app.Fragment"; //$NON-NLS-1$
    public static final String CLASS_V4_FRAGMENT = "android.support.v4.app.Fragment"; //$NON-NLS-1$
    /** MockView is part of the layoutlib bridge and used to display classes that have
     * no rendering in the graphical layout editor. */
    public final static String CLASS_MOCK_VIEW = "com.android.layoutlib.bridge.MockView"; //$NON-NLS-1$



    /** Returns the appropriate name for the 'android' command, which is 'android.bat' for
     * Windows and 'android' for all other platforms. */
    public static String androidCmdName() {
        String os = System.getProperty("os.name");          //$NON-NLS-1$
        String cmd = "android";                             //$NON-NLS-1$
        if (os.startsWith("Windows")) {                     //$NON-NLS-1$
            cmd += ".bat";                                  //$NON-NLS-1$
        }
        return cmd;
    }

    /** Returns the appropriate name for the 'mksdcard' command, which is 'mksdcard.exe' for
     * Windows and 'mkdsdcard' for all other platforms. */
    public static String mkSdCardCmdName() {
        String os = System.getProperty("os.name");          //$NON-NLS-1$
        String cmd = "mksdcard";                            //$NON-NLS-1$
        if (os.startsWith("Windows")) {                     //$NON-NLS-1$
            cmd += ".exe";                                  //$NON-NLS-1$
        }
        return cmd;
    }

    /**
     * Returns current platform
     *
     * @return one of {@link #PLATFORM_WINDOWS}, {@link #PLATFORM_DARWIN},
     * {@link #PLATFORM_LINUX} or {@link #PLATFORM_UNKNOWN}.
     */
    public static int currentPlatform() {
        String os = System.getProperty("os.name");          //$NON-NLS-1$
        if (os.startsWith("Mac OS")) {                      //$NON-NLS-1$
            return PLATFORM_DARWIN;
        } else if (os.startsWith("Windows")) {              //$NON-NLS-1$
            return PLATFORM_WINDOWS;
        } else if (os.startsWith("Linux")) {                //$NON-NLS-1$
            return PLATFORM_LINUX;
        }

        return PLATFORM_UNKNOWN;
    }
}
