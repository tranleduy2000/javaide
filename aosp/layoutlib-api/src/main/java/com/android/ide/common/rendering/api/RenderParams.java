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

package com.android.ide.common.rendering.api;

import com.android.ide.common.rendering.api.SessionParams.Key;
import com.android.resources.Density;
import com.android.resources.ScreenSize;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for rendering parameters. This include the generic parameters but not what needs
 * to be rendered or additional parameters.
 */
public abstract class RenderParams {

    public static final long DEFAULT_TIMEOUT = 250; //ms

    private final Object mProjectKey;
    private final HardwareConfig mHardwareConfig;
    private final RenderResources mRenderResources;
    private final LayoutlibCallback mLayoutlibCallback;
    private final int mMinSdkVersion;
    private final int mTargetSdkVersion;
    private final LayoutLog mLog;

    private boolean mCustomBackgroundEnabled;
    private int mCustomBackgroundColor;
    private long mTimeout;

    private AssetRepository mAssetRepository;
    private IImageFactory mImageFactory;

    private String mAppIcon;
    private String mAppLabel;
    private String mLocale;
    private String mActivityName;
    private boolean mForceNoDecor;
    private boolean mSupportsRtl;

    /**
     * A flexible map to pass additional flags to LayoutLib. LayoutLib will ignore flags that it
     * doesn't recognize.
     */
    private Map<Key, Object> mFlags;

    /**
     * @param projectKey An Object identifying the project. This is used for the cache mechanism.
     * @param hardwareConfig the {@link HardwareConfig}.
     * @param renderResources a {@link RenderResources} object providing access to the resources.
     * @param layoutlibCallback The {@link LayoutlibCallback} object to get information from
     * the project.
     * @param minSdkVersion the minSdkVersion of the project
     * @param targetSdkVersion the targetSdkVersion of the project
     * @param log the object responsible for displaying warning/errors to the user.
     */
    public RenderParams(
            Object projectKey,
            HardwareConfig hardwareConfig,
            RenderResources renderResources,
            LayoutlibCallback layoutlibCallback,
            int minSdkVersion, int targetSdkVersion,
            LayoutLog log) {
        mProjectKey = projectKey;
        mHardwareConfig = hardwareConfig;
        mRenderResources = renderResources;
        mLayoutlibCallback = layoutlibCallback;
        mMinSdkVersion = minSdkVersion;
        mTargetSdkVersion = targetSdkVersion;
        mLog = log;
        mCustomBackgroundEnabled = false;
        mTimeout = DEFAULT_TIMEOUT;
    }

    /**
     * Copy constructor.
     */
    public RenderParams(RenderParams params) {
        mProjectKey = params.mProjectKey;
        mHardwareConfig = params.mHardwareConfig;
        mRenderResources = params.mRenderResources;
        mAssetRepository = params.mAssetRepository;
        mLayoutlibCallback = params.mLayoutlibCallback;
        mMinSdkVersion = params.mMinSdkVersion;
        mTargetSdkVersion = params.mTargetSdkVersion;
        mLog = params.mLog;
        mCustomBackgroundEnabled = params.mCustomBackgroundEnabled;
        mCustomBackgroundColor = params.mCustomBackgroundColor;
        mTimeout = params.mTimeout;
        mImageFactory = params.mImageFactory;
        mAppIcon = params.mAppIcon;
        mAppLabel = params.mAppLabel;
        mLocale = params.mLocale;
        mActivityName = params.mActivityName;
        mForceNoDecor = params.mForceNoDecor;
        mSupportsRtl = params.mSupportsRtl;
        if (params.mFlags != null) {
            mFlags = new HashMap<Key, Object>(params.mFlags);
        }
    }

    public void setOverrideBgColor(int color) {
        mCustomBackgroundEnabled = true;
        mCustomBackgroundColor = color;
    }

    public void setTimeout(long timeout) {
        mTimeout = timeout;
    }

    public void setImageFactory(IImageFactory imageFactory) {
        mImageFactory = imageFactory;
    }

    public void setAppIcon(String appIcon) {
        mAppIcon = appIcon;
    }

    public void setAppLabel(String appLabel) {
        mAppLabel = appLabel;
    }

    public void setLocale(String locale) {
        mLocale = locale;
    }

    public void setActivityName(String activityName) {
        mActivityName = activityName;
    }

    public void setForceNoDecor() {
        mForceNoDecor = true;
    }

    public void setRtlSupport(boolean supportsRtl) {
        mSupportsRtl = supportsRtl;
    }

    public void setAssetRepository(AssetRepository assetRepository) {
        mAssetRepository = assetRepository;
    }

    public Object getProjectKey() {
        return mProjectKey;
    }

    public HardwareConfig getHardwareConfig() {
        return mHardwareConfig;
    }

    public int getMinSdkVersion() {
        return mMinSdkVersion;
    }

    public int getTargetSdkVersion() {
        return mTargetSdkVersion;
    }

    /**
     * @deprecated Use {@link #getHardwareConfig()}
     */
    @Deprecated
    public int getScreenWidth() {
        return mHardwareConfig.getScreenWidth();
    }

    /**
     * @deprecated Use {@link #getHardwareConfig()}
     */
    @Deprecated
    public int getScreenHeight() {
        return mHardwareConfig.getScreenHeight();
    }

    /**
     * @deprecated Use {@link #getHardwareConfig()}
     */
    @Deprecated
    public Density getDensity() {
        return mHardwareConfig.getDensity();
    }

    /**
     * @deprecated Use {@link #getHardwareConfig()}
     */
    @Deprecated
    public float getXdpi() {
        return mHardwareConfig.getXdpi();
    }

    /**
     * @deprecated Use {@link #getHardwareConfig()}
     */
    @Deprecated
    public float getYdpi() {
        return mHardwareConfig.getYdpi();
    }

    public RenderResources getResources() {
        return mRenderResources;
    }

    public AssetRepository getAssets() {
        return mAssetRepository;
    }

    /** @deprecated use {@link #getLayoutlibCallback()} */
    @Deprecated
    public IProjectCallback getProjectCallback() {
        return getLayoutlibCallback();
    }

    public LayoutlibCallback getLayoutlibCallback() {
        return mLayoutlibCallback;
    }

    public LayoutLog getLog() {
        return mLog;
    }

    public boolean isBgColorOverridden() {
        return mCustomBackgroundEnabled;
    }

    public int getOverrideBgColor() {
        return mCustomBackgroundColor;
    }

    public long getTimeout() {
        return mTimeout;
    }

    public IImageFactory getImageFactory() {
        return mImageFactory;
    }

    /**
     * @deprecated Use {@link #getHardwareConfig()}
     */
    @Deprecated
    public ScreenSize getConfigScreenSize() {
        return mHardwareConfig.getScreenSize();
    }

    public String getAppIcon() {
        return mAppIcon;
    }

    public String getAppLabel() {
        return mAppLabel;
    }

    public String getLocale() {
        return mLocale;
    }

    public String getActivityName() {
        return mActivityName;
    }

    public boolean isForceNoDecor() {
        return mForceNoDecor;
    }

    public boolean isRtlSupported() {
        return mSupportsRtl;
    }

    public <T> void setFlag(Key<T> key, T value) {
        if (mFlags == null) {
            mFlags = new HashMap<Key, Object>();
        }
        mFlags.put(key, value);
    }

    public <T> T getFlag(Key<T> key) {

        // noinspection since the values in the map can be added only by setFlag which ensures that
        // the types match.
        //noinspection unchecked
        return mFlags == null ? null : (T) mFlags.get(key);
    }
}
