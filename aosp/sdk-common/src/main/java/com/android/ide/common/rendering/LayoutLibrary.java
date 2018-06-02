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

package com.android.ide.common.rendering;

import static com.android.ide.common.rendering.api.Result.Status.ERROR_REFLECTION;

import com.android.annotations.VisibleForTesting;
import com.android.ide.common.rendering.api.Bridge;
import com.android.ide.common.rendering.api.Capability;
import com.android.ide.common.rendering.api.DrawableParams;
import com.android.ide.common.rendering.api.Features;
import com.android.ide.common.rendering.api.ILayoutPullParser;
import com.android.ide.common.rendering.api.LayoutLog;
import com.android.ide.common.rendering.api.LayoutlibCallback;
import com.android.ide.common.rendering.api.RenderSession;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.rendering.api.Result;
import com.android.ide.common.rendering.api.Result.Status;
import com.android.ide.common.rendering.api.SessionParams;
import com.android.ide.common.rendering.api.SessionParams.RenderingMode;
import com.android.ide.common.rendering.api.ViewInfo;
import com.android.ide.common.rendering.legacy.ILegacyPullParser;
import com.android.ide.common.resources.ResourceResolver;
import com.android.ide.common.sdk.LoadStatus;
import com.android.layoutlib.api.ILayoutBridge;
import com.android.layoutlib.api.ILayoutLog;
import com.android.layoutlib.api.ILayoutResult;
import com.android.layoutlib.api.ILayoutResult.ILayoutViewInfo;
import com.android.layoutlib.api.IProjectCallback;
import com.android.layoutlib.api.IResourceValue;
import com.android.layoutlib.api.IXmlPullParser;
import com.android.resources.ResourceType;
import com.android.utils.ILogger;
import com.android.utils.SdkUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class to use the Layout library.
 * <p/>
 * Use {@link #load(String, ILogger, String)} to load the jar file.
 * <p/>
 * Use the layout library with:
 * {@link #init}, {@link #supports(int)}, {@link #createSession(SessionParams)},
 * {@link #dispose()}, {@link #clearCaches(Object)}.
 * <p/>
 * Layout libraries before API level 5 used {@link IProjectCallback}. Layout libraries from
 * API level 5 to 14 used {@link com.android.ide.common.rendering.api.IProjectCallback}.
 * Layout libraries since API 15 use {@link LayoutlibCallback}. To target all Layout libraries,
 * use {@code LayoutlibCallback}, which implements the other interfaces. Also, use
 * {@link ILegacyPullParser} instead of {@link ILayoutPullParser}.
 * <p/>
 * These interfaces will ensure that both new and older Layout libraries can be accessed.
 */
@SuppressWarnings("deprecation")
public class LayoutLibrary {

    public static final String CLASS_BRIDGE = "com.android.layoutlib.bridge.Bridge"; //$NON-NLS-1$
    public static final String FN_ICU_JAR = "icu4j.jar"; //$NON-NLS-1$

    /** Link to the layout bridge */
    private final Bridge mBridge;
    /** Link to a ILayoutBridge in case loaded an older library */
    private final ILayoutBridge mLegacyBridge;
    /** Status of the layoutlib.jar loading */
    private final LoadStatus mStatus;
    /** Message associated with the {@link LoadStatus}. This is mostly used when
     * {@link #getStatus()} returns {@link LoadStatus#FAILED}.
     */
    private final String mLoadMessage;
    /** classloader used to load the jar file */
    private final ClassLoader mClassLoader;

    // Reflection data for older Layout Libraries.
    private Method mViewGetParentMethod;
    private Method mViewGetBaselineMethod;
    private Method mViewParentIndexOfChildMethod;
    private Class<?> mMarginLayoutParamClass;
    private Field mLeftMarginField;
    private Field mTopMarginField;
    private Field mRightMarginField;
    private Field mBottomMarginField;

    /**
     * Returns the {@link LoadStatus} of the loading of the layoutlib jar file.
     */
    public LoadStatus getStatus() {
        return mStatus;
    }

    /** Returns the message associated with the {@link LoadStatus}. This is mostly used when
     * {@link #getStatus()} returns {@link LoadStatus#FAILED}.
     */
    public String getLoadMessage() {
        return mLoadMessage;
    }

    /**
     * Returns the classloader used to load the classes in the layoutlib jar file.
     */
    public ClassLoader getClassLoader() {
        return mClassLoader;
    }

    /**
     * Loads the layoutlib.jar file located at the given path and returns a {@link LayoutLibrary}
     * object representing the result.
     * <p/>
     * If loading failed {@link #getStatus()} will reflect this, and {@link #mBridge} will
     * be null.
     *
     * @param layoutLibJarOsPath the path of the jar file
     * @param log an optional log file.
     * @return a {@link LayoutLibrary} object always.
     */
    public static LayoutLibrary load(String layoutLibJarOsPath, ILogger log, String toolName) {

        LoadStatus status = LoadStatus.LOADING;
        String message = null;
        Bridge bridge = null;
        ILayoutBridge legacyBridge = null;
        ClassLoader classLoader = null;

        try {
            // get the URL for the file.
            File f = new File(layoutLibJarOsPath);
            if (f.isFile() == false) {
                if (log != null) {
                    log.error(null, "layoutlib.jar is missing!"); //$NON-NLS-1$
                }
            } else {
                URL[] urls;
                // TODO: The icu jar has to be in the same location as layoutlib.jar. Get rid of
                // this dependency.
                File icu4j = new File(f.getParent(), FN_ICU_JAR);
                if (icu4j.isFile()) {
                    urls = new URL[2];
                    urls[1] = SdkUtils.fileToUrl(icu4j);
                } else {
                    urls = new URL[1];
                }
                urls[0] = SdkUtils.fileToUrl(f);

                // create a class loader. Because this jar reference interfaces
                // that are in the editors plugin, it's important to provide
                // a parent class loader.
                classLoader = new URLClassLoader(urls,
                        LayoutLibrary.class.getClassLoader());

                // load the class
                Class<?> clazz = classLoader.loadClass(CLASS_BRIDGE);
                if (clazz != null) {
                    // instantiate an object of the class.
                    Constructor<?> constructor = clazz.getConstructor();
                    if (constructor != null) {
                        Object bridgeObject = constructor.newInstance();
                        if (bridgeObject instanceof Bridge) {
                            bridge = (Bridge)bridgeObject;
                        } else if (bridgeObject instanceof ILayoutBridge) {
                            legacyBridge = (ILayoutBridge) bridgeObject;
                        }
                    }
                }

                if (bridge == null && legacyBridge == null) {
                    status = LoadStatus.FAILED;
                    message = "Failed to load " + CLASS_BRIDGE; //$NON-NLS-1$
                    if (log != null) {
                        log.error(null,
                                "Failed to load " + //$NON-NLS-1$
                                CLASS_BRIDGE +
                                " from " +          //$NON-NLS-1$
                                layoutLibJarOsPath);
                    }
                } else {
                    // mark the lib as loaded, unless it's overridden below.
                    status = LoadStatus.LOADED;

                    // check the API, only if it's not a legacy bridge
                    if (bridge != null) {
                        int api = bridge.getApiLevel();
                        if (api > Bridge.API_CURRENT) {
                            status = LoadStatus.FAILED;
                            message = String.format(
                                    "This version of the rendering library is more recent than your version of %1$s. Please update %1$s", toolName);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            status = LoadStatus.FAILED;
            Throwable cause = t;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            message = "Failed to load the LayoutLib: " + cause.getMessage();
            // log the error.
            if (log != null) {
                log.error(t, message);
            }
        }

        return new LayoutLibrary(bridge, legacyBridge, classLoader, status, message);
    }

    // ------ Layout Lib API proxy

    /**
     * Returns the API level of the layout library.
     */
    public int getApiLevel() {
        if (mBridge != null) {
            return mBridge.getApiLevel();
        }

        if (mLegacyBridge != null) {
            return getLegacyApiLevel();
        }

        return 0;
    }

    /**
     * Returns the revision of the library inside a given (layoutlib) API level.
     * The true version number of the library is {@link #getApiLevel()}.{@link #getRevision()}
     */
    public int getRevision() {
        if (mBridge != null) {
            return mBridge.getRevision();
        }

        return 0;
    }

    /**
     * Returns whether the LayoutLibrary supports a given {@link Capability}.
     * @return true if it supports it.
     *
     * @see Bridge#getCapabilities()
     *
     * @deprecated use {@link #supports(int)}
     */
    @Deprecated
    public boolean supports(Capability capability) {
        return supports(capability.ordinal());
    }

    /**
     * Returns whether the LayoutLibrary supports a given {@link Features}.
     *
     * @see Bridge#supports(int)
     */
    public boolean supports(int capability) {
        if (mBridge != null) {
            if (mBridge.getApiLevel() > 12) {
                // Features were introduced in API level 13.
                return mBridge.supports(capability);
            } else {
                return capability <= Features.LAST_CAPABILITY
                        && mBridge.getCapabilities().contains(Capability.values()[capability]);
            }
        }

        //noinspection VariableNotUsedInsideIf
        if (mLegacyBridge != null) {
            if (capability == Features.UNBOUND_RENDERING) {
                return getLegacyApiLevel() == 4;
            }
        }

        return false;
    }

    /**
     * Initializes the Layout Library object. This must be called before any other action is taken
     * on the instance.
     *
     * @param platformProperties The build properties for the platform.
     * @param fontLocation the location of the fonts in the SDK target.
     * @param enumValueMap map attrName => { map enumFlagName => Integer value }. This is typically
     *          read from attrs.xml in the SDK target.
     * @param log a {@link LayoutLog} object. Can be null.
     * @return true if success.
     */
    public boolean init(Map<String, String> platformProperties,
            File fontLocation,
            Map<String, Map<String, Integer>> enumValueMap,
            LayoutLog log) {
        if (mBridge != null) {
            return mBridge.init(platformProperties, fontLocation, enumValueMap, log);
        } else if (mLegacyBridge != null) {
            return mLegacyBridge.init(fontLocation.getAbsolutePath(), enumValueMap);
        }

        return false;
    }

    /**
     * Prepares the layoutlib to unloaded.
     *
     * @see Bridge#dispose()
     */
    public boolean dispose() {
        if (mBridge != null) {
            return mBridge.dispose();
        }

        return true;
    }

    /**
     * Starts a layout session by inflating and rendering it. The method returns a
     * {@link RenderSession} on which further actions can be taken.
     * <p/>
     * Before taking further actions on the scene, it is recommended to use
     * {@link #supports(int)} to check what the scene can do.
     *
     * @return a new {@link RenderSession} object that contains the result of the scene creation and
     * first rendering or null if {@link #getStatus()} doesn't return {@link LoadStatus#LOADED}.
     *
     * @see Bridge#createSession(SessionParams)
     */
    public RenderSession createSession(SessionParams params) {
        if (mBridge != null) {
            RenderSession session = mBridge.createSession(params);
            if (params.getExtendedViewInfoMode() && !supports(Features.EXTENDED_VIEWINFO)) {
                // Extended view info was requested but the layoutlib does not support it.
                // Add it manually.
                List<ViewInfo> infoList = session.getRootViews();
                if (infoList != null) {
                    for (ViewInfo info : infoList) {
                        addExtendedViewInfo(info);
                    }
                }
            }

            return session;
        } else if (mLegacyBridge != null) {
            return createLegacySession(params);
        }

        return null;
    }

    /**
     * Renders a Drawable. If the rendering is successful, the result image is accessible through
     * {@link Result#getData()}. It is of type {@link BufferedImage}
     * @param params the rendering parameters.
     * @return the result of the action.
     */
    public Result renderDrawable(DrawableParams params) {
        if (mBridge != null) {
            return mBridge.renderDrawable(params);
        }

        return Status.NOT_IMPLEMENTED.createResult();
    }

    /**
     * Clears the resource cache for a specific project.
     * <p/>This cache contains bitmaps and nine patches that are loaded from the disk and reused
     * until this method is called.
     * <p/>The cache is not configuration dependent and should only be cleared when a
     * resource changes (at this time only bitmaps and 9 patches go into the cache).
     *
     * @param projectKey the key for the project.
     *
     * @see Bridge#clearCaches(Object)
     */
    public void clearCaches(Object projectKey) {
        if (mBridge != null) {
            mBridge.clearCaches(projectKey);
        } else if (mLegacyBridge != null) {
            mLegacyBridge.clearCaches(projectKey);
        }
    }

    /**
     * Utility method returning the parent of a given view object.
     *
     * @param viewObject the object for which to return the parent.
     *
     * @return a {@link Result} indicating the status of the action, and if success, the parent
     *      object in {@link Result#getData()}
     */
    public Result getViewParent(Object viewObject) {
        if (mBridge != null) {
            Result r = mBridge.getViewParent(viewObject);
            if (r.isSuccess()) {
                return r;
            }
        }

        return getViewParentWithReflection(viewObject);
    }

    /**
     * Utility method returning the index of a given view in its parent.
     * @param viewObject the object for which to return the index.
     *
     * @return a {@link Result} indicating the status of the action, and if success, the index in
     *      the parent in {@link Result#getData()}
     */
    public Result getViewIndex(Object viewObject) {
        if (mBridge != null) {
            Result r = mBridge.getViewIndex(viewObject);
            if (r.isSuccess()) {
                return r;
            }
        }

        return getViewIndexReflection(viewObject);
    }

    /**
     * Returns true if the character orientation of the locale is right to left.
     * @param locale The locale formatted as language-region
     * @return true if the locale is right to left.
     */
    public boolean isRtl(String locale) {
        return supports(Features.RTL) && mBridge.isRtl(locale);
    }

    // ------ Implementation

    private LayoutLibrary(Bridge bridge, ILayoutBridge legacyBridge, ClassLoader classLoader,
            LoadStatus status, String message) {
        mBridge = bridge;
        mLegacyBridge = legacyBridge;
        mClassLoader = classLoader;
        mStatus = status;
        mLoadMessage = message;
    }

    /**
     * Returns the API level of the legacy bridge.
     * <p/>
     * This handles the case where ILayoutBridge does not have a {@link ILayoutBridge#getApiLevel()}
     * (at API level 1).
     * <p/>
     * {@link ILayoutBridge#getApiLevel()} should never called directly.
     *
     * @return the api level of {@link #mLegacyBridge}.
     */
    private int getLegacyApiLevel() {
        int apiLevel = 1;
        try {
            apiLevel = mLegacyBridge.getApiLevel();
        } catch (AbstractMethodError e) {
            // the first version of the api did not have this method
            // so this is 1
        }

        return apiLevel;
    }

    private RenderSession createLegacySession(SessionParams params) {
        if (params.getLayoutDescription() instanceof IXmlPullParser == false) {
            throw new IllegalArgumentException("Parser must be of type ILegacyPullParser");
        }
        if (params.getProjectCallback() instanceof
                com.android.layoutlib.api.IProjectCallback == false) {
            throw new IllegalArgumentException("Project callback must be of type ILegacyCallback");
        }

        if (params.getResources() instanceof ResourceResolver == false) {
            throw new IllegalArgumentException("RenderResources object must be of type ResourceResolver");
        }

        ResourceResolver resources = (ResourceResolver) params.getResources();

        int apiLevel = getLegacyApiLevel();

        // create a log wrapper since the older api requires a ILayoutLog
        final LayoutLog log = params.getLog();
        ILayoutLog logWrapper = new ILayoutLog() {

            @Override
            public void warning(String message) {
                log.warning(null, message, null /*data*/);
            }

            @Override
            public void error(Throwable t) {
                log.error(null, "error!", t, null /*data*/);
            }

            @Override
            public void error(String message) {
                log.error(null, message, null /*data*/);
            }
        };


        // convert the map of ResourceValue into IResourceValue. Super ugly but works.

        Map<String, Map<String, IResourceValue>> projectMap = convertMap(
                resources.getProjectResources());
        Map<String, Map<String, IResourceValue>> frameworkMap = convertMap(
                resources.getFrameworkResources());

        ILayoutResult result = null;

        if (apiLevel == 4) {
            // Final ILayoutBridge API added support for "render full height"
            result = mLegacyBridge.computeLayout(
                    (IXmlPullParser) params.getLayoutDescription(),
                    params.getProjectKey(),
                    params.getScreenWidth(), params.getScreenHeight(),
                    params.getRenderingMode() == RenderingMode.FULL_EXPAND ? true : false,
                    params.getDensity().getDpiValue(), params.getXdpi(), params.getYdpi(),
                    resources.getThemeName(), resources.isProjectTheme(),
                    projectMap, frameworkMap,
                    (IProjectCallback) params.getProjectCallback(),
                    logWrapper);
        } else if (apiLevel == 3) {
            // api 3 add density support.
            result = mLegacyBridge.computeLayout(
                    (IXmlPullParser) params.getLayoutDescription(), params.getProjectKey(),
                    params.getScreenWidth(), params.getScreenHeight(),
                    params.getDensity().getDpiValue(), params.getXdpi(), params.getYdpi(),
                    resources.getThemeName(), resources.isProjectTheme(),
                    projectMap, frameworkMap,
                    (IProjectCallback) params.getProjectCallback(), logWrapper);
        } else if (apiLevel == 2) {
            // api 2 added boolean for separation of project/framework theme
            result = mLegacyBridge.computeLayout(
                    (IXmlPullParser) params.getLayoutDescription(), params.getProjectKey(),
                    params.getScreenWidth(), params.getScreenHeight(),
                    resources.getThemeName(), resources.isProjectTheme(),
                    projectMap, frameworkMap,
                    (IProjectCallback) params.getProjectCallback(), logWrapper);
        } else {
            // First api with no density/dpi, and project theme boolean mixed
            // into the theme name.

            // change the string if it's a custom theme to make sure we can
            // differentiate them
            String themeName = resources.getThemeName();
            if (resources.isProjectTheme()) {
                themeName = "*" + themeName; //$NON-NLS-1$
            }

            result = mLegacyBridge.computeLayout(
                    (IXmlPullParser) params.getLayoutDescription(), params.getProjectKey(),
                    params.getScreenWidth(), params.getScreenHeight(),
                    themeName,
                    projectMap, frameworkMap,
                    (IProjectCallback) params.getProjectCallback(), logWrapper);
        }

        // clean up that is not done by the ILayoutBridge itself
        legacyCleanUp();

        return convertToScene(result);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, IResourceValue>> convertMap(
            Map<ResourceType, Map<String, ResourceValue>> map) {
        Map<String, Map<String, IResourceValue>> result =
            new HashMap<String, Map<String, IResourceValue>>();

        for (Entry<ResourceType, Map<String, ResourceValue>> entry : map.entrySet()) {
            // ugly case but works.
            result.put(entry.getKey().getName(),
                    (Map) entry.getValue());
        }

        return result;
    }

    /**
     * Converts a {@link ILayoutResult} to a {@link RenderSession}.
     */
    private RenderSession convertToScene(ILayoutResult result) {

        Result sceneResult;
        ViewInfo rootViewInfo = null;

        if (result.getSuccess() == ILayoutResult.SUCCESS) {
            sceneResult = Status.SUCCESS.createResult();
            ILayoutViewInfo oldRootView = result.getRootView();
            if (oldRootView != null) {
                rootViewInfo = convertToViewInfo(oldRootView);
            }
        } else {
            sceneResult = Status.ERROR_UNKNOWN.createResult(result.getErrorMessage());
        }

        // create a BasicLayoutScene. This will return the given values but return the default
        // implementation for all method.
        // ADT should gracefully handle the default implementations of LayoutScene
        return new StaticRenderSession(sceneResult, rootViewInfo, result.getImage());
    }

    /**
     * Converts a {@link ILayoutViewInfo} (and its children) to a {@link ViewInfo}.
     */
    private ViewInfo convertToViewInfo(ILayoutViewInfo view) {
        // create the view info.
        ViewInfo viewInfo = new ViewInfo(view.getName(), view.getViewKey(),
                view.getLeft(), view.getTop(), view.getRight(), view.getBottom());

        // then convert the children
        ILayoutViewInfo[] children = view.getChildren();
        if (children != null) {
            ArrayList<ViewInfo> convertedChildren = new ArrayList<ViewInfo>(children.length);
            for (ILayoutViewInfo child : children) {
                convertedChildren.add(convertToViewInfo(child));
            }
            viewInfo.setChildren(convertedChildren);
        }

        return viewInfo;
    }

    /**
     * Post rendering clean-up that must be done here because it's not done in any layoutlib using
     * {@link ILayoutBridge}.
     */
    private void legacyCleanUp() {
        try {
            Class<?> looperClass = mClassLoader.loadClass("android.os.Looper"); //$NON-NLS-1$
            Field threadLocalField = looperClass.getField("sThreadLocal"); //$NON-NLS-1$
            if (threadLocalField != null) {
                threadLocalField.setAccessible(true);
                // get object. Field is static so no need to pass an object
                ThreadLocal<?> threadLocal = (ThreadLocal<?>) threadLocalField.get(null);
                if (threadLocal != null) {
                    threadLocal.remove();
                }
            }
        } catch (Exception e) {
            // do nothing.
        }
    }

    private Result getViewParentWithReflection(Object viewObject) {
        // default implementation using reflection.
        try {
            if (mViewGetParentMethod == null) {
                Class<?> viewClass = Class.forName("android.view.View");
                mViewGetParentMethod = viewClass.getMethod("getParent");
            }

            return Status.SUCCESS.createResult(mViewGetParentMethod.invoke(viewObject));
        } catch (Exception e) {
            // Catch all for the reflection calls.
            return ERROR_REFLECTION.createResult(null, e);
        }
    }

    /**
     * Utility method returning the index of a given view in its parent.
     * @param viewObject the object for which to return the index.
     *
     * @return a {@link Result} indicating the status of the action, and if success, the index in
     *      the parent in {@link Result#getData()}
     */
    private Result getViewIndexReflection(Object viewObject) {
        // default implementation using reflection.
        try {
            Class<?> viewClass = Class.forName("android.view.View");

            if (mViewGetParentMethod == null) {
                mViewGetParentMethod = viewClass.getMethod("getParent");
            }

            Object parentObject = mViewGetParentMethod.invoke(viewObject);

            if (mViewParentIndexOfChildMethod == null) {
                Class<?> viewParentClass = Class.forName("android.view.ViewParent");
                mViewParentIndexOfChildMethod = viewParentClass.getMethod("indexOfChild",
                        viewClass);
            }

            return Status.SUCCESS.createResult(
                    mViewParentIndexOfChildMethod.invoke(parentObject, viewObject));
        } catch (Exception e) {
            // Catch all for the reflection calls.
            return ERROR_REFLECTION.createResult(null, e);
        }
    }

    private void addExtendedViewInfo(ViewInfo info) {
        computeExtendedViewInfo(info);

        List<ViewInfo> children = info.getChildren();
        for (ViewInfo child : children) {
            addExtendedViewInfo(child);
        }
    }

    private void computeExtendedViewInfo(ViewInfo info) {
        Object viewObject = info.getViewObject();
        Object params = info.getLayoutParamsObject();

        int baseLine = getViewBaselineReflection(viewObject);
        int leftMargin = 0;
        int topMargin = 0;
        int rightMargin = 0;
        int bottomMargin = 0;

        try {
            if (mMarginLayoutParamClass == null) {
                mMarginLayoutParamClass = Class.forName(
                        "android.view.ViewGroup$MarginLayoutParams");

                mLeftMarginField = mMarginLayoutParamClass.getField("leftMargin");
                mTopMarginField = mMarginLayoutParamClass.getField("topMargin");
                mRightMarginField = mMarginLayoutParamClass.getField("rightMargin");
                mBottomMarginField = mMarginLayoutParamClass.getField("bottomMargin");
            }

            if (mMarginLayoutParamClass.isAssignableFrom(params.getClass())) {

                leftMargin = (Integer)mLeftMarginField.get(params);
                topMargin = (Integer)mTopMarginField.get(params);
                rightMargin = (Integer)mRightMarginField.get(params);
                bottomMargin = (Integer)mBottomMarginField.get(params);
            }

        } catch (Exception e) {
            // just use 'unknown' value.
            leftMargin = Integer.MIN_VALUE;
            topMargin = Integer.MIN_VALUE;
            rightMargin = Integer.MIN_VALUE;
            bottomMargin = Integer.MIN_VALUE;
        }

        info.setExtendedInfo(baseLine, leftMargin, topMargin, rightMargin, bottomMargin);
    }

    /**
     * Utility method returning the baseline value for a given view object. This basically returns
     * View.getBaseline().
     *
     * @param viewObject the object for which to return the index.
     *
     * @return the baseline value or -1 if not applicable to the view object or if this layout
     *     library does not implement this method.
     */
    private int getViewBaselineReflection(Object viewObject) {
        // default implementation using reflection.
        try {
            if (mViewGetBaselineMethod == null) {
                Class<?> viewClass = Class.forName("android.view.View");
                mViewGetBaselineMethod = viewClass.getMethod("getBaseline");
            }

            Object result = mViewGetBaselineMethod.invoke(viewObject);
            if (result instanceof Integer) {
                return ((Integer)result).intValue();
            }

        } catch (Exception e) {
            // Catch all for the reflection calls.
        }

        return Integer.MIN_VALUE;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    protected LayoutLibrary() {
        mBridge = null;
        mLegacyBridge = null;
        mClassLoader = null;
        mStatus = null;
        mLoadMessage = null;
    }
}
