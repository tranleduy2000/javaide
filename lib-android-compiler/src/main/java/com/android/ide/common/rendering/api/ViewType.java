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
 * Lists various types of a view. Used as a possible return value of {@link ViewInfo#getViewType()}
 */
public enum ViewType {
    /**
     * A view added by the framework. No additional info about the view is
     * available.
     */
    SYSTEM_UNKNOWN,
    /**
     * A view that is part of the user's layout.
     */
    USER,
    /**
     * The overflow menu button in the action bar.
     */
    ACTION_BAR_OVERFLOW,
    /**
     * A menu item in the action bar.
     */
    ACTION_BAR_MENU,
    /**
     * A menu item in the action bar overflow popup.
     */
    ACTION_BAR_OVERFLOW_MENU,
    /**
     *  The back button in the Navigation Bar.
     */
    NAVIGATION_BAR_BACK,
    /**
     *  The home button in the Navigation Bar.
     */
    NAVIGATION_BAR_HOME,
    /**
     *  The recents button in the Navigation Bar.
     */
    NAVIGATION_BAR_RECENTS
}
