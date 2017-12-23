/*
 * Copyright (C) 2014 The Android Open Source Project
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

/**
 * List of features describing the LayoutLib capabilities.
 */
public class Features {
    /** Ability to render at full size, as required by the layout, and unbound by the screen */
    public static final int UNBOUND_RENDERING = 0;
    /** Ability to override the background of the rendering with transparency using
     * {@link SessionParams#setOverrideBgColor(int)} */
    public static final int CUSTOM_BACKGROUND_COLOR = 1;
    /** Ability to call {@link RenderSession#render()} and {@link RenderSession#render(long)}. */
    public static final int RENDER = 2;
    /** Ability to ask for a layout only with no rendering through
     * {@link SessionParams#setLayoutOnly()}
     */
    public static final int LAYOUT_ONLY = 3;
    /**
     * Ability to control embedded layout parsers through {@link ILayoutPullParser#getParser(String)}
     */
    public static final int EMBEDDED_LAYOUT = 4;
    /** Ability to call<br>
     * {@link RenderSession#insertChild(Object, ILayoutPullParser, int, IAnimationListener)}<br>
     * {@link RenderSession#moveChild(Object, Object, int, java.util.Map, IAnimationListener)}<br>
     * {@link RenderSession#setProperty(Object, String, String)}<br>
     * The method that receives an animation listener can only use it if the
     * ANIMATED_VIEW_MANIPULATION, or FULL_ANIMATED_VIEW_MANIPULATION is also supported.
     */
    public static final int VIEW_MANIPULATION = 5;
    /** Ability to play animations with<br>
     * {@link RenderSession#animate(Object, String, boolean, IAnimationListener)}
     */
    public static final int PLAY_ANIMATION = 6;
    /**
     * Ability to manipulate views with animation, as long as the view does not change parent.
     * {@link RenderSession#insertChild(Object, ILayoutPullParser, int, IAnimationListener)}<br>
     * {@link RenderSession#moveChild(Object, Object, int, java.util.Map, IAnimationListener)}<br>
     * {@link RenderSession#removeChild(Object, IAnimationListener)}<br>
     */
    public static final int ANIMATED_VIEW_MANIPULATION = 7;
    /**
     * Ability to move views (even into a different ViewGroup) with animation.
     * see {@link RenderSession#moveChild(Object, Object, int, java.util.Map, IAnimationListener)}
     */
    public static final int FULL_ANIMATED_VIEW_MANIPULATION = 7;
    public static final int ADAPTER_BINDING = 8;
    public static final int EXTENDED_VIEWINFO = 9;
    /**
     * Ability to properly resize nine-patch assets.
     */
    public static final int FIXED_SCALABLE_NINE_PATCH = 10;
    /**
     * Ability to render RTL layouts.
     */
    public static final int RTL = 11;
    /**
     * Ability to render ActionBar.
     */
    public static final int ACTION_BAR = 12;
    /**
     * Ability to simulate older Platform Versions.
     * <p/>
     * This is the last feature supported by API 12.
     */
    public static final int SIMULATE_PLATFORM = 13;
    /**
     * Ability to render preferences.
     */
    public static final int PREFERENCES_RENDERING = 14;
}
