/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.ide.common.resources.configuration;

import com.android.annotations.Nullable;
import com.android.resources.NightMode;
import com.android.resources.ScreenRound;
import com.android.resources.UiMode;
import com.android.sdklib.devices.Device;
import com.android.sdklib.devices.Hardware;
import com.android.sdklib.devices.Screen;
import com.android.sdklib.devices.State;

public class DeviceConfigHelper {
    /**
     * Returns a {@link FolderConfiguration} based on the given state
     *
     * @param state
     *            The {@link State} of the {@link Device} to base the
     *            {@link FolderConfiguration} on. Can be null.
     * @return A {@link FolderConfiguration} based on the given {@link State}.
     *         If the given {@link State} is null, the result is also null;
     */
    @Nullable
    public static FolderConfiguration getFolderConfig(@Nullable State state) {
        if (state == null) {
            return null;
        }

        Hardware hw = state.getHardware();

        FolderConfiguration config = new FolderConfiguration();
        config.createDefault();
        Screen screen = hw.getScreen();
        config.setDensityQualifier(new DensityQualifier(screen.getPixelDensity()));
        config.setNavigationMethodQualifier(new NavigationMethodQualifier(hw.getNav()));
        ScreenDimensionQualifier sdq;
        if (screen.getXDimension() > screen.getYDimension()) {
            sdq = new ScreenDimensionQualifier(screen.getXDimension(), screen.getYDimension());
        } else {
            sdq = new ScreenDimensionQualifier(screen.getYDimension(), screen.getXDimension());
        }
        config.setScreenDimensionQualifier(sdq);
        config.setScreenRatioQualifier(new ScreenRatioQualifier(screen.getRatio()));
        config.setScreenSizeQualifier(new ScreenSizeQualifier(screen.getSize()));
        config.setTextInputMethodQualifier(new TextInputMethodQualifier(hw.getKeyboard()));
        config.setTouchTypeQualifier(new TouchScreenQualifier(screen.getMechanism()));
        ScreenRound screenRound = screen.getScreenRound();
        if (screenRound == null) {
            // The default is not round.
            screenRound = ScreenRound.NOTROUND;
        }
        config.setScreenRoundQualifier(new ScreenRoundQualifier(screenRound));

        config.setKeyboardStateQualifier(new KeyboardStateQualifier(state.getKeyState()));
        config.setNavigationStateQualifier(new NavigationStateQualifier(state.getNavState()));
        config.setScreenOrientationQualifier(
            new ScreenOrientationQualifier(state.getOrientation()));

        config.updateScreenWidthAndHeight();

        // Setup some default qualifiers
        config.setUiModeQualifier(new UiModeQualifier(UiMode.NORMAL));
        config.setNightModeQualifier(new NightModeQualifier(NightMode.NOTNIGHT));
        config.setCountryCodeQualifier(new CountryCodeQualifier());
        config.setLocaleQualifier(new LocaleQualifier());
        config.setLayoutDirectionQualifier(new LayoutDirectionQualifier());
        config.setNetworkCodeQualifier(new NetworkCodeQualifier());
        config.setVersionQualifier(new VersionQualifier());

        return config;
    }

    /**
     * Returns a {@link FolderConfiguration} based on the {@link State} given by
     * the {@link Device} and the state name.
     *
     * @param d
     *            The {@link Device} to base the {@link FolderConfiguration} on.
     * @param stateName
     *            The name of the state to base the {@link FolderConfiguration}
     *            on.
     * @return The {@link FolderConfiguration} based on the determined
     *         {@link State}. If there is no {@link State} with the given state
     *         name for the given device, null is returned.
     */
    @Nullable
    public static FolderConfiguration getFolderConfig(Device d, String stateName) {
        return getFolderConfig(d.getState(stateName));
    }

    /**
     * Returns a {@link FolderConfiguration} based on the default {@link State}
     * for the given {@link Device}.
     *
     * @param d
     *            The {@link Device} to generate the {@link FolderConfiguration}
     *            from.
     * @return A {@link FolderConfiguration} based on the default {@link State}
     *         for the given {@link Device}
     */
    public static FolderConfiguration getFolderConfig(Device d) {
        return getFolderConfig(d.getDefaultState());
    }
}
