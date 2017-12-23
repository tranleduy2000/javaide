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

import java.util.Collections;
import java.util.List;

/**
 * Callback for Action Bar information needed by the Layout Library.
 */
public class ActionBarCallback {

    // The Navigation mode constants correspond to their counterparts in android.app.ActionBar
    public static final int NAVIGATION_MODE_STANDARD = 0;
    public static final int NAVIGATION_MODE_LIST = 1;
    public static final int NAVIGATION_MODE_TABS = 2;

    // The protected constants must not be used on the LayoutLib side without incrementing
    // the LayoutLib API version number.
    protected static final String ATTR_MENU = "menu";                  //$NON-NLS-1$
    protected static final String ATTR_NAV_MODE = "actionBarNavMode";  //$NON-NLS-1$

    // The attribute values for ATTR_NAV_MODE.
    protected static final String VALUE_NAV_MODE_TABS = "tabs";        //$NON-NLS-1$
    protected static final String VALUE_NAV_MODE_LIST = "list";        //$NON-NLS-1$

    /**
     * Types of navigation for home button.
     */
    public enum HomeButtonStyle {
        NONE, SHOW_HOME_AS_UP
    }

    /**
     * Returns a list of names of the IDs for menus to add to the action bar.
     *
     * @return the list of menu ids. The list is never null, but may be empty.
     */
    public List<String> getMenuIdNames() {
        return Collections.emptyList();
    }

    /**
     * Returns whether the Action Bar should be split for narrow screens.
     */
    public boolean getSplitActionBarWhenNarrow() {
        return false;
    }

    /**
     * Returns which navigation mode the action bar should use.
     *
     * @return one of {@link #NAVIGATION_MODE_STANDARD}, {@link #NAVIGATION_MODE_LIST} or
     * {@link #NAVIGATION_MODE_TABS}
     */
    public int getNavigationMode() {
        return NAVIGATION_MODE_STANDARD;
    }

    /**
     * Returns the subtitle to be used with the action bar or null if there is no subtitle.
     */
    public String getSubTitle() {
        return null;
    }

    /**
     * Returns the type of navigation for home button to be used in the action bar.
     * <p/>
     * For example, for showHomeAsUp, an arrow is shown alongside the "home" icon.
     *
     * @return navigation type for home button. Never null.
     */
    public HomeButtonStyle getHomeButtonStyle() {
        return HomeButtonStyle.NONE;
    }

    /**
     * Returns whether to draw the overflow menu popup.
     */
    public boolean isOverflowPopupNeeded() {
        return false;
    }
}
