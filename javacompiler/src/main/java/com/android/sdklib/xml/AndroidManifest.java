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

package com.android.sdklib.xml;

import com.android.io.IAbstractFile;
import com.android.io.IAbstractFolder;
import com.android.io.StreamException;
import com.android.sdklib.SdkConstants;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

/**
 * Helper and Constants for the AndroidManifest.xml file.
 *
 */
public final class AndroidManifest {

    public final static String NODE_MANIFEST = "manifest";
    public final static String NODE_APPLICATION = "application";
    public final static String NODE_ACTIVITY = "activity";
    public final static String NODE_ACTIVITY_ALIAS = "activity-alias";
    public final static String NODE_SERVICE = "service";
    public final static String NODE_RECEIVER = "receiver";
    public final static String NODE_PROVIDER = "provider";
    public final static String NODE_INTENT = "intent-filter";
    public final static String NODE_ACTION = "action";
    public final static String NODE_CATEGORY = "category";
    public final static String NODE_USES_SDK = "uses-sdk";
    public final static String NODE_INSTRUMENTATION = "instrumentation";
    public final static String NODE_USES_LIBRARY = "uses-library";
    public final static String NODE_SUPPORTS_SCREENS = "supports-screens";
    public final static String NODE_USES_CONFIGURATION = "uses-configuration";
    public final static String NODE_USES_FEATURE = "uses-feature";

    public final static String ATTRIBUTE_PACKAGE = "package";
    public final static String ATTRIBUTE_VERSIONCODE = "versionCode";
    public final static String ATTRIBUTE_NAME = "name";
    public final static String ATTRIBUTE_REQUIRED = "required";
    public final static String ATTRIBUTE_GLESVERSION = "glEsVersion";
    public final static String ATTRIBUTE_PROCESS = "process";
    public final static String ATTRIBUTE_DEBUGGABLE = "debuggable";
    public final static String ATTRIBUTE_LABEL = "label";
    public final static String ATTRIBUTE_ICON = "icon";
    public final static String ATTRIBUTE_MIN_SDK_VERSION = "minSdkVersion";
    public final static String ATTRIBUTE_TARGET_SDK_VERSION = "targetSdkVersion";
    public final static String ATTRIBUTE_TARGET_PACKAGE = "targetPackage";
    public final static String ATTRIBUTE_TARGET_ACTIVITY = "targetActivity";
    public final static String ATTRIBUTE_MANAGE_SPACE_ACTIVITY = "manageSpaceActivity";
    public final static String ATTRIBUTE_EXPORTED = "exported";
    public final static String ATTRIBUTE_RESIZEABLE = "resizeable";
    public final static String ATTRIBUTE_ANYDENSITY = "anyDensity";
    public final static String ATTRIBUTE_SMALLSCREENS = "smallScreens";
    public final static String ATTRIBUTE_NORMALSCREENS = "normalScreens";
    public final static String ATTRIBUTE_LARGESCREENS = "largeScreens";
    public final static String ATTRIBUTE_REQ_5WAYNAV = "reqFiveWayNav";
    public final static String ATTRIBUTE_REQ_NAVIGATION = "reqNavigation";
    public final static String ATTRIBUTE_REQ_HARDKEYBOARD = "reqHardKeyboard";
    public final static String ATTRIBUTE_REQ_KEYBOARDTYPE = "reqKeyboardType";
    public final static String ATTRIBUTE_REQ_TOUCHSCREEN = "reqTouchScreen";
    public static final String ATTRIBUTE_THEME = "theme";

    /**
     * Returns an {@link IAbstractFile} object representing the manifest for the given project.
     *
     * @param projectFolder The project containing the manifest file.
     * @return An IAbstractFile object pointing to the manifest or null if the manifest
     *         is missing.
     */
    public static IAbstractFile getManifest(IAbstractFolder projectFolder) {
        IAbstractFile file = projectFolder.getFile(SdkConstants.FN_ANDROID_MANIFEST_XML);
        if (file.exists()) {
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
        XPath xPath = AndroidXPathFactory.newXPath();

        return xPath.evaluate(
                "/"  + NODE_MANIFEST +
                "/@" + ATTRIBUTE_PACKAGE,
                new InputSource(manifestFile.getContents()));
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
        XPath xPath = AndroidXPathFactory.newXPath();

        String value = xPath.evaluate(
                "/"  + NODE_MANIFEST +
                "/"  + NODE_APPLICATION +
                "/@" + AndroidXPathFactory.DEFAULT_NS_PREFIX +
                ":"  + ATTRIBUTE_DEBUGGABLE,
                new InputSource(manifestFile.getContents()));

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
        XPath xPath = AndroidXPathFactory.newXPath();

        String result = xPath.evaluate(
                "/"  + NODE_MANIFEST +
                "/@" + AndroidXPathFactory.DEFAULT_NS_PREFIX +
                ":"  + ATTRIBUTE_VERSIONCODE,
                new InputSource(manifestFile.getContents()));

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

        Object result = xPath.evaluate(
                "/"  + NODE_MANIFEST +
                "/@" + AndroidXPathFactory.DEFAULT_NS_PREFIX +
                ":"  + ATTRIBUTE_VERSIONCODE,
                new InputSource(manifestFile.getContents()),
                XPathConstants.NODE);

        if (result != null) {
            Node node  = (Node)result;
            if (node.getNodeValue().length() > 0) {
                return true;
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
    public static Object getMinSdkVersion(IAbstractFile manifestFile)
            throws XPathExpressionException, StreamException {
        XPath xPath = AndroidXPathFactory.newXPath();

        String result = xPath.evaluate(
                "/"  + NODE_MANIFEST +
                "/"  + NODE_USES_SDK +
                "/@" + AndroidXPathFactory.DEFAULT_NS_PREFIX +
                ":"  + ATTRIBUTE_MIN_SDK_VERSION,
                new InputSource(manifestFile.getContents()));

        try {
            return Integer.valueOf(result);
        } catch (NumberFormatException e) {
            return result.length() > 0 ? result : null;
        }
    }

    /**
     * Returns the value of the targetSdkVersion attribute (defaults to 1 if the attribute is
     * not set), or -1 if the value is a codename.
     * @param manifestFile the manifest file to read the attribute from.
     * @return the integer value or -1 if not set.
     * @throws XPathExpressionException
     * @throws StreamException If any error happens when reading the manifest.
     */
    public static Integer getTargetSdkVersion(IAbstractFile manifestFile)
            throws XPathExpressionException, StreamException {
        XPath xPath = AndroidXPathFactory.newXPath();

        String result = xPath.evaluate(
                "/"  + NODE_MANIFEST +
                "/"  + NODE_USES_SDK +
                "/@" + AndroidXPathFactory.DEFAULT_NS_PREFIX +
                ":"  + ATTRIBUTE_TARGET_SDK_VERSION,
                new InputSource(manifestFile.getContents()));

        try {
            return Integer.valueOf(result);
        } catch (NumberFormatException e) {
            return result.length() > 0 ? -1 : null;
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
        XPath xPath = AndroidXPathFactory.newXPath();

        return xPath.evaluate(
                "/"  + NODE_MANIFEST +
                "/"  + NODE_APPLICATION +
                "/@" + AndroidXPathFactory.DEFAULT_NS_PREFIX +
                ":"  + ATTRIBUTE_ICON,
                new InputSource(manifestFile.getContents()));
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
        XPath xPath = AndroidXPathFactory.newXPath();

        return xPath.evaluate(
                "/"  + NODE_MANIFEST +
                "/"  + NODE_APPLICATION +
                "/@" + AndroidXPathFactory.DEFAULT_NS_PREFIX +
                ":"  + ATTRIBUTE_LABEL,
                new InputSource(manifestFile.getContents()));
    }

    /**
     * Combines a java package, with a class value from the manifest to make a fully qualified
     * class name
     * @param javaPackage the java package from the manifest.
     * @param className the class name from the manifest.
     * @return the fully qualified class name.
     */
    public static String combinePackageAndClassName(String javaPackage, String className) {
        if (className == null || className.length() == 0) {
            return javaPackage;
        }
        if (javaPackage == null || javaPackage.length() == 0) {
            return className;
        }

        // the class name can be a subpackage (starts with a '.'
        // char), a simple class name (no dot), or a full java package
        boolean startWithDot = (className.charAt(0) == '.');
        boolean hasDot = (className.indexOf('.') != -1);
        if (startWithDot || hasDot == false) {

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
            if (packageName.length() > 0 && fullActivityName.startsWith(packageName)) {
                String name = fullActivityName.substring(packageName.length());
                if (name.length() > 0 && name.charAt(0) == '.') {
                    return name;
                }
            }
        }

        return fullActivityName;
    }
}
