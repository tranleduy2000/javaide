/*
 * Copyright (C) 2011 The Android Open Source Project
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Rendering parameters for a {@link RenderSession}.
 */
public class SessionParams extends RenderParams {

    public enum RenderingMode {
        NORMAL(false, false),
        V_SCROLL(false, true),
        H_SCROLL(true, false),
        FULL_EXPAND(true, true);

        private final boolean mHorizExpand;
        private final boolean mVertExpand;

        RenderingMode(boolean horizExpand, boolean vertExpand) {
            mHorizExpand = horizExpand;
            mVertExpand = vertExpand;
        }

        public boolean isHorizExpand() {
            return mHorizExpand;
        }

        public boolean isVertExpand() {
            return mVertExpand;
        }
    }

    private final ILayoutPullParser mLayoutDescription;
    private final RenderingMode mRenderingMode;
    private boolean mLayoutOnly = false;
    private Map<ResourceReference, AdapterBinding> mAdapterBindingMap;
    private boolean mExtendedViewInfoMode = false;
    private final int mSimulatedPlatformVersion;

    /**
     *
     * @param layoutDescription the {@link ILayoutPullParser} letting the LayoutLib Bridge visit the
     * layout file.
     * @param renderingMode The rendering mode.
     * @param projectKey An Object identifying the project. This is used for the cache mechanism.
     * @param hardwareConfig the {@link HardwareConfig}.
     * @param renderResources a {@link RenderResources} object providing access to the resources.
     * @param layoutlibCallback The {@link LayoutlibCallback} object to get information from
     * the project.
     * @param minSdkVersion the minSdkVersion of the project
     * @param targetSdkVersion the targetSdkVersion of the project
     * @param log the object responsible for displaying warning/errors to the user.
     */
    public SessionParams(
            ILayoutPullParser layoutDescription,
            RenderingMode renderingMode,
            Object projectKey,
            HardwareConfig hardwareConfig,
            RenderResources renderResources,
            LayoutlibCallback layoutlibCallback,
            int minSdkVersion, int targetSdkVersion,
            LayoutLog log) {
        this(layoutDescription, renderingMode, projectKey, hardwareConfig,
                renderResources, layoutlibCallback, minSdkVersion, targetSdkVersion, log, 0);
    }

    /**
     *
     * @param layoutDescription the {@link ILayoutPullParser} letting the LayoutLib Bridge visit the
     * layout file.
     * @param renderingMode The rendering mode.
     * @param projectKey An Object identifying the project. This is used for the cache mechanism.
     * @param hardwareConfig the {@link HardwareConfig}.
     * @param renderResources a {@link RenderResources} object providing access to the resources.
     * @param projectCallback The {@link LayoutlibCallback} object to get information from
     * the project.
     * @param minSdkVersion the minSdkVersion of the project
     * @param targetSdkVersion the targetSdkVersion of the project
     * @param log the object responsible for displaying warning/errors to the user.
     * @param simulatedPlatformVersion try to simulate an old android platform. 0 means disabled.
     */
    public SessionParams(
            ILayoutPullParser layoutDescription,
            RenderingMode renderingMode,
            Object projectKey,
            HardwareConfig hardwareConfig,
            RenderResources renderResources,
            LayoutlibCallback projectCallback,
            int minSdkVersion, int targetSdkVersion,
            LayoutLog log, int simulatedPlatformVersion) {
        super(projectKey, hardwareConfig, renderResources, projectCallback,
                minSdkVersion, targetSdkVersion, log);

        mLayoutDescription = layoutDescription;
        mRenderingMode = renderingMode;
        mSimulatedPlatformVersion = simulatedPlatformVersion;
    }

    public SessionParams(SessionParams params) {
        super(params);
        mLayoutDescription = params.mLayoutDescription;
        mRenderingMode = params.mRenderingMode;
        mSimulatedPlatformVersion = params.mSimulatedPlatformVersion;
        if (params.mAdapterBindingMap != null) {
            mAdapterBindingMap = new HashMap<ResourceReference, AdapterBinding>(
                    params.mAdapterBindingMap);
        }
        mExtendedViewInfoMode = params.mExtendedViewInfoMode;
    }

    public ILayoutPullParser getLayoutDescription() {
        return mLayoutDescription;
    }

    public RenderingMode getRenderingMode() {
        return mRenderingMode;
    }

    public void setLayoutOnly() {
        mLayoutOnly = true;
    }

    public boolean isLayoutOnly() {
        return mLayoutOnly;
    }

    public void addAdapterBinding(ResourceReference reference, AdapterBinding data) {
        if (mAdapterBindingMap == null) {
            mAdapterBindingMap = new HashMap<ResourceReference, AdapterBinding>();
        }

        mAdapterBindingMap.put(reference, data);
    }

    public Map<ResourceReference, AdapterBinding> getAdapterBindings() {
        if (mAdapterBindingMap == null) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(mAdapterBindingMap);
    }

    public void setExtendedViewInfoMode(boolean mode) {
        mExtendedViewInfoMode = mode;
    }

    public boolean getExtendedViewInfoMode() {
        return mExtendedViewInfoMode;
    }

    public int getSimulatedPlatformVersion() {
        return mSimulatedPlatformVersion;
    }

    /**
     * The class should be in RenderParams, but is here because it was
     * originally here and we cannot change the API without breaking backwards
     * compatibility.
     */
    public static class Key<T> {
        public final Class<T> mExpectedClass;
        public final String mName;

        public Key(String name, Class<T> expectedClass) {
            assert name != null;
            assert expectedClass != null;

            mExpectedClass = expectedClass;
            mName = name;
        }

        @Override
        public int hashCode() {
            int result = mExpectedClass.hashCode();
            return 31 * result + mName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj != null && getClass() == obj.getClass()) {
                Key<?> k = (Key<?>) obj;
                return mExpectedClass.equals(k.mExpectedClass) && mName.equals(k.mName);
            }
            return false;
        }

        @Override
        public String toString() {
            return mName;
        }
    }
}
