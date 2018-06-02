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

package com.android.xml;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.io.IAbstractFile;
import com.android.io.IAbstractFolder;
import com.android.io.StreamException;
import com.google.common.io.Closeables;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

/**
 * Helper and Constants for the AndroidManifest.xml file.
 *
 */
public final class AndroidManifest {

    public static final String NODE_MANIFEST = "manifest";
    public static final String NODE_APPLICATION = "application";
    public static final String NODE_ACTIVITY = "activity";
    public static final String NODE_ACTIVITY_ALIAS = "activity-alias";
    public static final String NODE_SERVICE = "service";
    public static final String NODE_RECEIVER = "receiver";
    public static final String NODE_PROVIDER = "provider";
    public static final String NODE_INTENT = "intent-filter";
    public static final String NODE_ACTION = "action";
    public static final String NODE_CATEGORY = "category";
    public static final String NODE_USES_SDK = "uses-sdk";
    public static final String NODE_PERMISSION = "permission";
    public static final String NODE_PERMISSION_TREE = "permission-tree";
    public static final String NODE_PERMISSION_GROUP = "permission-group";
    public static final String NODE_USES_PERMISSION = "uses-permission";
    public static final String NODE_INSTRUMENTATION = "instrumentation";
    public static final String NODE_USES_LIBRARY = "uses-library";
    public static final String NODE_SUPPORTS_SCREENS = "supports-screens";
    public static final String NODE_COMPATIBLE_SCREENS = "compatible-screens";
    public static final String NODE_USES_CONFIGURATION = "uses-configuration";
    public static final String NODE_USES_FEATURE = "uses-feature";
    public static final String NODE_METADATA = "meta-data";
    public static final String NODE_DATA = "data";
    public static final String NODE_GRANT_URI_PERMISSION = "grant-uri-permission";
    public static final String NODE_PATH_PERMISSION = "path-permission";
    public static final String NODE_SUPPORTS_GL_TEXTURE = "supports-gl-texture";

    public static final String ATTRIBUTE_PACKAGE = "package";
    public static final String ATTRIBUTE_VERSIONCODE = "versionCode";
    public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_MIME_TYPE = "mimeType";
    public static final String ATTRIBUTE_PORT = "port";
    public static final String ATTRIBUTE_REQUIRED = "required";
    public static final String ATTRIBUTE_GLESVERSION = "glEsVersion";
    public static final String ATTRIBUTE_PROCESS = "process";
    public static final String ATTRIBUTE_DEBUGGABLE = "debuggable";
    public static final String ATTRIBUTE_LABEL = "label";
    public static final String ATTRIBUTE_ICON = "icon";
    public static final String ATTRIBUTE_MIN_SDK_VERSION = "minSdkVersion";
    public static final String ATTRIBUTE_TARGET_SDK_VERSION = "targetSdkVersion";
    public static final String ATTRIBUTE_TARGET_PACKAGE = "targetPackage";
    public static final String ATTRIBUTE_TARGET_ACTIVITY = "targetActivity";
    public static final String ATTRIBUTE_MANAGE_SPACE_ACTIVITY = "manageSpaceActivity";
    public static final String ATTRIBUTE_EXPORTED = "exported";
    public static final String ATTRIBUTE_RESIZEABLE = "resizeable";
    public static final String ATTRIBUTE_ANYDENSITY = "anyDensity";
    public static final String ATTRIBUTE_SMALLSCREENS = "smallScreens";
    public static final String ATTRIBUTE_NORMALSCREENS = "normalScreens";
    public static final String ATTRIBUTE_LARGESCREENS = "largeScreens";
    public static final String ATTRIBUTE_REQ_5WAYNAV = "reqFiveWayNav";
    public static final String ATTRIBUTE_REQ_NAVIGATION = "reqNavigation";
    public static final String ATTRIBUTE_REQ_HARDKEYBOARD = "reqHardKeyboard";
    public static final String ATTRIBUTE_REQ_KEYBOARDTYPE = "reqKeyboardType";
    public static final String ATTRIBUTE_REQ_TOUCHSCREEN = "reqTouchScreen";
    public static final String ATTRIBUTE_THEME = "theme";
    public static final String ATTRIBUTE_BACKUP_AGENT = "backupAgent";
    public static final String ATTRIBUTE_PARENT_ACTIVITY_NAME = "parentActivityName";
    public static final String ATTRIBUTE_SUPPORTS_RTL = "supportsRtl";
    public static final String ATTRIBUTE_UI_OPTIONS = "uiOptions";
    public static final String ATTRIBUTE_VALUE = "value";

    public static final String VALUE_PARENT_ACTIVITY = SdkConstants.ANDROID_SUPPORT_PKG_PREFIX + "PARENT_ACTIVITY";

    /**
     * Returns an {@link IAbstractFile} object representing the manifest for the given project.
     *
     * @param projectFolder The project containing the manifest file.
     * @return An IAbstractFile object pointing to the manifest or null if the manifest
     *         is missing.
     */
    public static IAbstractFile getManifest(IAbstractFolder projectFolder) {
        IAbstractFile file = projectFolder.getFile(SdkConstants.FN_ANDROID_MANIFEST_XML);
        if (file != null && file.exists()) {
            return file;
        }

        return null;
    }

    /**
     * Returns the package for a given project.
     * @param projectFolder the folder of the project.
     * @return the package info or null (or empty) if not found.
     * @throws XPathExpressionException
     * @throws StreamException If any error happens when reading the manifest.
     */
    public static String getPackage(IAbstractFolder projectFolder)
            throws XPathExpressionException, StreamException {
        IAbstractFile file = getManifest(projectFolder);
        if (file != null) {
            return getPackage(file);
        }

        return null;
    }

    /**
     * Returns the package for a given manifest.
     * @param manifestFile the manifest to parse.
     * @return the package info or null (or empty) if not found.
     * @throws XPathExpressionException
     * @throws StreamException If any error happens when reading the manifest.
     */
    public static String getPackage(IAbstractFile manifestFile)
            throws XPathExpressionException, StreamException {
        return getStringValue(manifestFile,
                "/" + NODE_MANIFEST +
                "/@" + ATTRIBUTE_PACKAGE);
    }

    /**
     * Returns whether the manifest is set to make the application debuggable.
     *
     * If the give manifest does not contain the debuggable attribute then the application
     * is considered to not be debuggable.
     *
     * @param manifestFile the manifest to parse.
     * @return true if the application is debuggable.
     * @throws XPathExpressionException
     * @throws StreamException If any error happens when reading the manifest.
     */
    public static boolean getDebuggable(IAbstractFile manifestFile)
            throws XPathExpressionException, StreamException {
        String value = getStringValue(manifestFile,
                "/"  + NODE_MANIFEST +
                "/"  + NODE_APPLICATION +
                "/@" + AndroidXPathFactory.DEFAULT_NS_PREFIX +
                ":"  + ATTRIBUTE_DEBUGGABLE);

        // default is not debuggable, which is the same behavior as parseBoolean
        return Boolean.parseBoolean(value);
    }

    /**
     * Returns the value of the versionCode attribute or -1 if the value is not set.
     * @param manifestFile the manifest file to read the attribute from.
     * @return the integer value or -1 if not set.
     * @throws XPathExpressionException
     * @throws StreamException If any error happens when reading the manifest.
     */
    public static int getVersionCode(IAbstractFile manifestFile)
            throws XPathExpressionException, StreamException {
        String result = getStringValue(manifestFile,
                "/" + NODE_MANIFEST +
                "/@" + AndroidXPathFactory.DEFAULT_NS_PREFIX +
                ":" + ATTRIBUTE_VERSIONCODE);

        try {
            return Integer.parseInt(result);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Returns whether the version Code attribute is set in a given manifest.
     * @param manifestFile the manifest to check
     * @return true if the versionCode attribute is present and its value is not empty.
     * @throws XPathExpressionException
     * @throws StreamException If any error happens when reading the manifest.
     */
    public static boolean hasVersionCode(IAbstractFile manifestFile)
            throws XPathExpressionException, StreamException {
        XPath xPath = AndroidXPathFactory.newXPath();

        InputStream is = null;
        try {
            is = manifestFile.getContents();
            Object result = xPath.evaluate(
                    "/"  + NODE_MANIFEST +
                    "/@" + AndroidXPathFactory.DEFAULT_NS_PREFIX +
                    ":"  + ATTRIBUTE_VERSIONCODE,
                    new InputSource(is),
                    XPathConstants.NODE);

            if (result != null) {
                Node node  = (Node)result;
                if (!node.getNodeValue().isEmpty()) {
                    return true;
                }
            }
        } finally {
            try {
                Closeables.close(is, true /* swallowIOException */);
            } catch (IOException e) {
                // cannot happen
            }
        }

        return false;
    }

    /**
     * Returns the value of the minSdkVersion attribute.
     * <p/>
     * If the attribute is set with an int value, the method returns an Integer object.
     * <p/>
     * If the attribute is set with a codename, it returns the codename as a String object.
     * <p/>
     * If the attribute is not set, it returns null.
     *
     * @param manifestFile the manifest file to read the attribute from.
     * @return the attribute value.
     * @throws XPathExpressionException
     * @throws StreamException If any error happens when reading the manifest.
     */
    @Nullable
    public static Object getMinSdkVersion(IAbstractFile manifestFile)
            throws XPathExpressionException, StreamException {
        String result = getStringValue(manifestFile,
                "/" + NODE_MANIFEST +
                "/" + NODE_USES_SDK +
                "/@" + AndroidXPathFactory.DEFAULT_NS_PREFIX +
                ":" + ATTRIBUTE_MIN_SDK_VERSION);

        try {
            return Integer.valueOf(result);
        } catch (NumberFormatException e) {
            return !result.isEmpty() ? result : null;
        }
    }

    /**
     * Returns the value of the targetSdkVersion attribute.
     * <p/>
     * If the attribute is set with an int value, the method returns an Integer object.
     * <p/>
     * If the attribute is set with a codename, it returns the codename as a String object.
     * <p/>
     * If the attribute is not set, it returns null.
     *
     * @param manifestFile the manifest file to read the attribute from.
     * @return the integer value or -1 if not set.
     * @throws XPathExpressionException
     * @throws StreamException If any error happens when reading the manifest.
     */
    @Nullable
    public static Object getTargetSdkVersion(IAbstractFile manifestFile)
            throws XPathExpressionException, StreamException {
        String result = getStringValue(manifestFile,
                "/" + NODE_MANIFEST +
                "/" + NODE_USES_SDK +
                "/@" + AndroidXPathFactory.DEFAULT_NS_PREFIX +
                ":" + ATTRIBUTE_TARGET_SDK_VERSION);
        try {
            return Integer.valueOf(result);
        } catch (NumberFormatException e) {
            return !result.isEmpty() ? result : null;
        }
    }

    /**
     * Returns the application icon  for a given manifest.
     * @param manifestFile the manifest to parse.
     * @return the icon or null (or empty) if not found.
     * @throws XPathExpressionException
     * @throws StreamException If any error happens when reading the manifest.
     */
    public static String getApplicationIcon(IAbstractFile manifestFile)
            throws XPathExpressionException, StreamException {
        return getStringValue(manifestFile,
                "/" + NODE_MANIFEST +
                "/" + NODE_APPLICATION +
                "/@" + AndroidXPathFactory.DEFAULT_NS_PREFIX +
                ":" + ATTRIBUTE_ICON);
    }

    /**
     * Returns the application label  for a given manifest.
     * @param manifestFile the manifest to parse.
     * @return the label or null (or empty) if not found.
     * @throws XPathExpressionException
     * @throws StreamException If any error happens when reading the manifest.
     */
    public static String getApplicationLabel(IAbstractFile manifestFile)
            throws XPathExpressionException, StreamException {
        return getStringValue(manifestFile,
                "/" + NODE_MANIFEST +
                "/" + NODE_APPLICATION +
                "/@" + AndroidXPathFactory.DEFAULT_NS_PREFIX +
                ":" + ATTRIBUTE_LABEL);
    }

  /**
   * Returns whether the manifest is set to make the application RTL aware.
   *
   * If the give manifest does not contain the supportsRtl attribute then the application
   * is considered to not be not supporting RTL (there will be no layout mirroring).
   *
   * @param manifestFile the manifest to parse.
   * @return true if the application is supporting RTL.
   * @throws XPathExpressionException
   * @throws StreamException If any error happens when reading the manifest.
   */
  public static boolean getSupportsRtl(IAbstractFile manifestFile)
      throws XPathExpressionException, StreamException {

      String value = getStringValue(manifestFile,
              "/"  + NODE_MANIFEST +
              "/"  + NODE_APPLICATION +
              "/@" + AndroidXPathFactory.DEFAULT_NS_PREFIX +
              ":"  + ATTRIBUTE_SUPPORTS_RTL);

      // default is not debuggable, which is the same behavior as parseBoolean
      return Boolean.parseBoolean(value);
  }

    /**
     * Combines a java package, with a class value from the manifest to make a fully qualified
     * class name
     * @param javaPackage the java package from the manifest.
     * @param className the class name from the manifest.
     * @return the fully qualified class name.
     */
    public static String combinePackageAndClassName(String javaPackage, String className) {
        if (className == null || className.isEmpty()) {
            return javaPackage;
        }
        if (javaPackage == null || javaPackage.isEmpty()) {
            return className;
        }

        // the class name can be a subpackage (starts with a '.'
        // char), a simple class name (no dot), or a full java package
        boolean startWithDot = (className.charAt(0) == '.');
        boolean hasDot = (className.indexOf('.') != -1);
        if (startWithDot || !hasDot) {

            // add the concatenation of the package and class name
            if (startWithDot) {
                return javaPackage + className;
            } else {
                return javaPackage + '.' + className;
            }
        } else {
            // just add the class as it should be a fully qualified java name.
            return className;
        }
    }

    /**
     * Given a fully qualified activity name (e.g. com.foo.test.MyClass) and given a project
     * package base name (e.g. com.foo), returns the relative activity name that would be used
     * the "name" attribute of an "activity" element.
     *
     * @param fullActivityName a fully qualified activity class name, e.g. "com.foo.test.MyClass"
     * @param packageName The project base package name, e.g. "com.foo"
     * @return The relative activity name if it can be computed or the original fullActivityName.
     */
    public static String extractActivityName(String fullActivityName, String packageName) {
        if (packageName != null && fullActivityName != null) {
            if (!packageName.isEmpty() && fullActivityName.startsWith(packageName)) {
                String name = fullActivityName.substring(packageName.length());
                if (!name.isEmpty() && name.charAt(0) == '.') {
                    return name;
                }
            }
        }

        return fullActivityName;
    }


    private static String getStringValue(@NonNull IAbstractFile file, @NonNull String xPath)
            throws StreamException, XPathExpressionException {
        XPath xpath = AndroidXPathFactory.newXPath();

        InputStream is = null;
        try {
            is = file.getContents();
            return xpath.evaluate(xPath, new InputSource(is));
        } finally {
            try {
                Closeables.close(is, true /* swallowIOException */);
            } catch (IOException e) {
                // cannot happen
            }
        }
    }
}
