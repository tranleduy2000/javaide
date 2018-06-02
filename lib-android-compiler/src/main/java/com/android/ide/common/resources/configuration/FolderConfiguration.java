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

package com.android.ide.common.resources.configuration;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.resources.Density;
import com.android.resources.ResourceFolderType;
import com.android.resources.ScreenOrientation;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


/**
 * Represents the configuration for Resource Folders. All the properties have a default
 * value which means that the property is not set.
 */
public final class FolderConfiguration implements Comparable<FolderConfiguration> {

    @SuppressWarnings("NullableProblems") // done in static-block below
    @NonNull
    private static final ResourceQualifier[] DEFAULT_QUALIFIERS;

    static {
        // get the default qualifiers.
        FolderConfiguration defaultConfig = new FolderConfiguration();
        defaultConfig.createDefault();
        DEFAULT_QUALIFIERS = defaultConfig.getQualifiers();
    }

    /** Splitter which can be used to split qualifiers */
    public static final Splitter QUALIFIER_SPLITTER = Splitter.on('-');


    private final ResourceQualifier[] mQualifiers;

    private static final int INDEX_COUNTRY_CODE          = 0;
    private static final int INDEX_NETWORK_CODE          = 1;
    private static final int INDEX_LOCALE                = 2;
    private static final int INDEX_LAYOUT_DIR            = 3;
    private static final int INDEX_SMALLEST_SCREEN_WIDTH = 4;
    private static final int INDEX_SCREEN_WIDTH          = 5;
    private static final int INDEX_SCREEN_HEIGHT         = 6;
    private static final int INDEX_SCREEN_LAYOUT_SIZE    = 7;
    private static final int INDEX_SCREEN_RATIO          = 8;
    private static final int INDEX_SCREEN_ROUND          = 9;
    private static final int INDEX_SCREEN_ORIENTATION    = 10;
    private static final int INDEX_UI_MODE               = 11;
    private static final int INDEX_NIGHT_MODE            = 12;
    private static final int INDEX_PIXEL_DENSITY         = 13;
    private static final int INDEX_TOUCH_TYPE            = 14;
    private static final int INDEX_KEYBOARD_STATE        = 15;
    private static final int INDEX_TEXT_INPUT_METHOD     = 16;
    private static final int INDEX_NAVIGATION_STATE      = 17;
    private static final int INDEX_NAVIGATION_METHOD     = 18;
    private static final int INDEX_SCREEN_DIMENSION      = 19;
    private static final int INDEX_VERSION               = 20;
    private static final int INDEX_COUNT                 = 21;

    public FolderConfiguration() {
        mQualifiers = new ResourceQualifier[INDEX_COUNT];
    }

    private FolderConfiguration(ResourceQualifier[] qualifiers) {
        this();
        System.arraycopy(qualifiers, 0, mQualifiers, 0, INDEX_COUNT);
    }
    /**
     * Creates a {@link FolderConfiguration} matching the folder segments.
     * @param folderSegments The segments of the folder name. The first segments should contain
     * the name of the folder
     * @return a FolderConfiguration object, or null if the folder name isn't valid..
     */
    @Nullable
    public static FolderConfiguration getConfig(@NonNull String[] folderSegments) {
        Iterator<String> iterator = Iterators.forArray(folderSegments);
        if (iterator.hasNext()) {
            // Skip the first segment: it should be just the base folder, such as "values" or
            // "layout"
            iterator.next();
        }

        return getConfigFromQualifiers(iterator);
    }

    /**
     * Creates a {@link FolderConfiguration} matching the folder segments.
     * @param folderSegments The segments of the folder name. The first segments should contain
     * the name of the folder
     * @return a FolderConfiguration object, or null if the folder name isn't valid..
     * @see FolderConfiguration#getConfig(String[])
     */
    @Nullable
    public static FolderConfiguration getConfig(@NonNull Iterable<String> folderSegments) {
        Iterator<String> iterator = folderSegments.iterator();
        if (iterator.hasNext()) {
            // Skip the first segment: it should be just the base folder, such as "values" or
            // "layout"
            iterator.next();
        }

        return getConfigFromQualifiers(iterator);
    }

    /**
     * Creates a {@link FolderConfiguration} matching the qualifiers.
     *
     * @param qualifiers the qualifiers.
     *
     * @return a FolderConfiguration object, or null if the folder name isn't valid..
     */
    @Nullable
    public static FolderConfiguration getConfigFromQualifiers(
            @NonNull Iterable<String> qualifiers) {
        return getConfigFromQualifiers(qualifiers.iterator());
    }

    /**
     * Creates a {@link FolderConfiguration} matching the qualifiers.
     *
     * @param qualifiers An iterator on the qualifiers.
     *
     * @return a FolderConfiguration object, or null if the folder name isn't valid..
     */
    @Nullable
    public static FolderConfiguration getConfigFromQualifiers(
            @NonNull Iterator<String> qualifiers) {
        FolderConfiguration config = new FolderConfiguration();

        // we are going to loop through the segments, and match them with the first
        // available qualifier. If the segment doesn't match we try with the next qualifier.
        // Because the order of the qualifier is fixed, we do not reset the first qualifier
        // after each successful segment.
        // If we run out of qualifier before processing all the segments, we fail.

        int qualifierIndex = 0;
        int qualifierCount = DEFAULT_QUALIFIERS.length;

        /*
            Process a series of qualifiers and parse them into a set of ResourceQualifiers
            in the new folder configuration.

            The basic loop is as follows:

                while (qualifiers.hasNext()) {
                    String seg = qualifiers.next();

                    while (qualifierIndex < qualifierCount &&
                            !DEFAULT_QUALIFIERS[qualifierIndex].checkAndSet(seg, config)) {
                        qualifierIndex++;
                    }

                    // if we reached the end of the qualifier we didn't find a matching qualifier.
                    if (qualifierIndex == qualifierCount) {
                        return null;
                    } else {
                        qualifierIndex++; // already processed this one
                    }
                }

             In other words, we process through the iterable, one segment at a time, and
             for that segment, we iterate up through the qualifiers and ask each one if
             they can handle it (via checkAndSet); if they can, we are done with that segment
             *and* that qualifier, so next time through the segment loop we won't keep retrying
             the same qualifiers. So, we are basically iterating through two lists (segments
             and qualifiers) at the same time).

             However, locales are a special exception to this: we want to combine *two* segments
             into a single qualifier when you specify both a language and a region.
             E.g. for "en-rUS-ldltr" we want a single LocaleQualifier holding both "en" and "rUS"
             and then a LayoutDirectionQualifier for ldltr.

             Therefore, we've unrolled the above loop: we process all identifiers up to
             the locale qualifier index.

             Then, at the locale qualifier index, IF we get a match, we don't increment
             the qualifierIndex: instead, we fetch the next segment, and if it matches
             as a region, we augment the locale qualifier and continue -- otherwise, we
             bail and process the next segment as usual.

             And then we finally iterate through the remaining qualifiers and segments; this
             is basically the first loop again, iterating from the post-locale qualifier
             up to the end.
         */

        if (!qualifiers.hasNext()) {
            return config;
        }

        while (qualifiers.hasNext()) {
            String seg = qualifiers.next();
            if (seg.isEmpty()) {
                return null; // Not a valid folder configuration
            }

            // TODO: Perform case normalization later (on a per qualifier basis)
            seg = seg.toLowerCase(Locale.US); // no-op if string is already in lower case

            while (qualifierIndex < INDEX_LOCALE &&
                    !DEFAULT_QUALIFIERS[qualifierIndex].checkAndSet(seg, config)) {
                qualifierIndex++;
            }

            // if we reached the end of the qualifier we didn't find a matching qualifier.
            if (qualifierIndex == INDEX_LOCALE) {
                // Ready for locale matching now; that requires some special
                // casing described below

                boolean handle = true;
                // Don't need to lowercase; qualifier will normalize case on its own
                if (DEFAULT_QUALIFIERS[qualifierIndex].checkAndSet(seg, config)) {
                    qualifierIndex++;
                    if (qualifiers.hasNext()) {
                        seg = qualifiers.next();
                        if (seg.isEmpty()) {
                            return null; // Not a valid folder configuration
                        }
                        // Is the next qualifier a region? If so, amend the existing
                        // LocaleQualifier
                        if (LocaleQualifier.isRegionSegment(seg)) {
                            LocaleQualifier localeQualifier = config.getLocaleQualifier();
                            assert localeQualifier != null; // because checkAndSet returned true above
                            localeQualifier.setRegionSegment(seg);
                            handle = false;
                        } else {
                            // No, not a region, so perform normal processing
                            seg = seg.toLowerCase(Locale.US); // no-op if string is already in lower case
                        }
                    } else {
                        return config;
                    }
                }

                if (handle) {
                    while (qualifierIndex < qualifierCount &&
                            !DEFAULT_QUALIFIERS[qualifierIndex].checkAndSet(seg, config)) {
                        qualifierIndex++;
                    }

                    // if we reached the end of the qualifier we didn't find a matching qualifier.
                    if (qualifierIndex == qualifierCount) {
                        // Ready for locale matching now; that requires some special
                        // casing described below
                        return null;
                    } else {
                        qualifierIndex++; // already processed this one
                    }
                }

                break;
            } else {
                qualifierIndex++; // already processed this one
            }
        }

        // Same loop as above, but we continue from after the locales
        while (qualifiers.hasNext()) {
            String seg = qualifiers.next();
            if (seg.isEmpty()) {
                return null; // Not a valid folder configuration
            }

            seg = seg.toLowerCase(Locale.US); // no-op if string is already in lower case

            while (qualifierIndex < qualifierCount &&
                    !DEFAULT_QUALIFIERS[qualifierIndex].checkAndSet(seg, config)) {
                qualifierIndex++;
            }

            // if we reached the end of the qualifier we didn't find a matching qualifier.
            if (qualifierIndex == qualifierCount) {
                return null;
            } else {
                qualifierIndex++; // already processed this one
            }
        }

        return config;
    }

    /**
     * Creates a {@link FolderConfiguration} matching the given folder name.
     *
     * @param folderName the folder name
     * @return a FolderConfiguration object, or null if the folder name isn't valid..
     */
    @Nullable
    public static FolderConfiguration getConfigForFolder(@NonNull String folderName) {
        return getConfig(QUALIFIER_SPLITTER.split(folderName));
    }

    /**
     * Creates a copy of the given {@link FolderConfiguration}, that can be modified without
     * affecting the original.
     */
    @NonNull
    public static FolderConfiguration copyOf(@NonNull FolderConfiguration original) {
        return new FolderConfiguration(original.mQualifiers);
    }

    /**
     * Creates a {@link FolderConfiguration} matching the given qualifier string
     * (just the qualifiers; e.g. for a folder like "values-en-rUS" this would be "en-rUS").
     *
     * @param qualifierString the qualifier string
     * @return a FolderConfiguration object, or null if the qualifier string isn't valid..
     */
    @Nullable
    public static FolderConfiguration getConfigForQualifierString(@NonNull String qualifierString) {
        if (qualifierString.isEmpty()) {
            return new FolderConfiguration();
        } else {
            return getConfigFromQualifiers(QUALIFIER_SPLITTER.split(qualifierString));
        }
    }

    /**
     * Returns the number of {@link ResourceQualifier} that make up a Folder configuration.
     */
    public static int getQualifierCount() {
        return INDEX_COUNT;
    }

    /**
     * Sets the config from the qualifiers of a given <var>config</var>.
     * <p/>This is equivalent to <code>set(config, false)</code>
     * @param config the configuration to set
     *
     * @see #set(FolderConfiguration, boolean)
     */
    public void set(@Nullable FolderConfiguration config) {
        set(config, false /*nonFakeValuesOnly*/);
    }

    /**
     * Sets the config from the qualifiers of a given <var>config</var>.
     * @param config the configuration to set
     * @param nonFakeValuesOnly if set to true this ignore qualifiers for which the
     * current value is a fake value.
     *
     * @see ResourceQualifier#hasFakeValue()
     */
    public void set(@Nullable FolderConfiguration config, boolean nonFakeValuesOnly) {
        if (config != null) {
            for (int i = 0 ; i < INDEX_COUNT ; i++) {
                ResourceQualifier q = config.mQualifiers[i];
                if (!nonFakeValuesOnly || q == null || !q.hasFakeValue()) {
                    mQualifiers[i] = q;
                }
            }
        }
    }

    /**
     * Reset the config.
     * <p/>This makes qualifiers at all indices <code>null</code>.
     */
    public void reset() {
        for (int i = 0 ; i < INDEX_COUNT ; i++) {
            mQualifiers[i] = null;
        }
    }

    /**
     * Removes the qualifiers from the receiver if they are present (and valid)
     * in the given configuration.
     */
    public void substract(@NonNull FolderConfiguration config) {
        for (int i = 0 ; i < INDEX_COUNT ; i++) {
            if (config.mQualifiers[i] != null && config.mQualifiers[i].isValid()) {
                mQualifiers[i] = null;
            }
        }
    }

    /**
     * Adds the non-qualifiers from the given config.
     * Qualifiers that are null in the given config do not change in the receiver.
     */
    public void add(@NonNull FolderConfiguration config) {
        for (int i = 0 ; i < INDEX_COUNT ; i++) {
            if (config.mQualifiers[i] != null) {
                mQualifiers[i] = config.mQualifiers[i];
            }
        }
    }

    /**
     * Returns the first invalid qualifier, or <code>null<code> if they are all valid (or if none
     * exists).
     */
    @Nullable
    public ResourceQualifier getInvalidQualifier() {
        for (int i = 0 ; i < INDEX_COUNT ; i++) {
            if (mQualifiers[i] != null && !mQualifiers[i].isValid()) {
                return mQualifiers[i];
            }
        }

        // all allocated qualifiers are valid, we return null.
        return null;
    }

    /**
     * Adds a qualifier to the {@link FolderConfiguration}
     * @param qualifier the {@link ResourceQualifier} to add.
     */
    public void addQualifier(@Nullable ResourceQualifier qualifier) {
        if (qualifier instanceof CountryCodeQualifier) {
            mQualifiers[INDEX_COUNTRY_CODE] = qualifier;

        } else if (qualifier instanceof NetworkCodeQualifier) {
            mQualifiers[INDEX_NETWORK_CODE] = qualifier;

        } else if (qualifier instanceof LocaleQualifier) {
            mQualifiers[INDEX_LOCALE] = qualifier;

        } else if (qualifier instanceof LayoutDirectionQualifier) {
            mQualifiers[INDEX_LAYOUT_DIR] = qualifier;

        } else if (qualifier instanceof SmallestScreenWidthQualifier) {
            mQualifiers[INDEX_SMALLEST_SCREEN_WIDTH] = qualifier;

        } else if (qualifier instanceof ScreenWidthQualifier) {
            mQualifiers[INDEX_SCREEN_WIDTH] = qualifier;

        } else if (qualifier instanceof ScreenHeightQualifier) {
            mQualifiers[INDEX_SCREEN_HEIGHT] = qualifier;

        } else if (qualifier instanceof ScreenSizeQualifier) {
            mQualifiers[INDEX_SCREEN_LAYOUT_SIZE] = qualifier;

        } else if (qualifier instanceof ScreenRatioQualifier) {
            mQualifiers[INDEX_SCREEN_RATIO] = qualifier;

        } else if (qualifier instanceof ScreenRoundQualifier) {
            mQualifiers[INDEX_SCREEN_ROUND] = qualifier;

        } else if (qualifier instanceof ScreenOrientationQualifier) {
            mQualifiers[INDEX_SCREEN_ORIENTATION] = qualifier;

        } else if (qualifier instanceof UiModeQualifier) {
            mQualifiers[INDEX_UI_MODE] = qualifier;

        } else if (qualifier instanceof NightModeQualifier) {
            mQualifiers[INDEX_NIGHT_MODE] = qualifier;

        } else if (qualifier instanceof DensityQualifier) {
            mQualifiers[INDEX_PIXEL_DENSITY] = qualifier;

        } else if (qualifier instanceof TouchScreenQualifier) {
            mQualifiers[INDEX_TOUCH_TYPE] = qualifier;

        } else if (qualifier instanceof KeyboardStateQualifier) {
            mQualifiers[INDEX_KEYBOARD_STATE] = qualifier;

        } else if (qualifier instanceof TextInputMethodQualifier) {
            mQualifiers[INDEX_TEXT_INPUT_METHOD] = qualifier;

        } else if (qualifier instanceof NavigationStateQualifier) {
            mQualifiers[INDEX_NAVIGATION_STATE] = qualifier;

        } else if (qualifier instanceof NavigationMethodQualifier) {
            mQualifiers[INDEX_NAVIGATION_METHOD] = qualifier;

        } else if (qualifier instanceof ScreenDimensionQualifier) {
            mQualifiers[INDEX_SCREEN_DIMENSION] = qualifier;

        } else if (qualifier instanceof VersionQualifier) {
            mQualifiers[INDEX_VERSION] = qualifier;

        }
    }

    /**
     * Removes a given qualifier from the {@link FolderConfiguration}.
     * @param qualifier the {@link ResourceQualifier} to remove.
     */
    public void removeQualifier(@NonNull ResourceQualifier qualifier) {
        for (int i = 0 ; i < INDEX_COUNT ; i++) {
            if (mQualifiers[i] == qualifier) {
                mQualifiers[i] = null;
                return;
            }
        }
    }

    /**
     * Returns a qualifier by its index. The total number of qualifiers can be accessed by
     * {@link #getQualifierCount()}.
     * @param index the index of the qualifier to return.
     * @return the qualifier or null if there are none at the index.
     */
    @Nullable
    public ResourceQualifier getQualifier(int index) {
        return mQualifiers[index];
    }

    public void setCountryCodeQualifier(CountryCodeQualifier qualifier) {
        mQualifiers[INDEX_COUNTRY_CODE] = qualifier;
    }

    @Nullable
    public CountryCodeQualifier getCountryCodeQualifier() {
        return (CountryCodeQualifier)mQualifiers[INDEX_COUNTRY_CODE];
    }

    public void setNetworkCodeQualifier(NetworkCodeQualifier qualifier) {
        mQualifiers[INDEX_NETWORK_CODE] = qualifier;
    }

    @Nullable
    public NetworkCodeQualifier getNetworkCodeQualifier() {
        return (NetworkCodeQualifier)mQualifiers[INDEX_NETWORK_CODE];
    }

    public void setLocaleQualifier(LocaleQualifier qualifier) {
        mQualifiers[INDEX_LOCALE] = qualifier;
    }

    @Nullable
    public LocaleQualifier getLocaleQualifier() {
        return (LocaleQualifier)mQualifiers[INDEX_LOCALE];
    }

    public void setLayoutDirectionQualifier(LayoutDirectionQualifier qualifier) {
        mQualifiers[INDEX_LAYOUT_DIR] = qualifier;
    }

    @Nullable
    public LayoutDirectionQualifier getLayoutDirectionQualifier() {
        return (LayoutDirectionQualifier)mQualifiers[INDEX_LAYOUT_DIR];
    }

    public void setSmallestScreenWidthQualifier(SmallestScreenWidthQualifier qualifier) {
        mQualifiers[INDEX_SMALLEST_SCREEN_WIDTH] = qualifier;
    }

    @Nullable
    public SmallestScreenWidthQualifier getSmallestScreenWidthQualifier() {
        return (SmallestScreenWidthQualifier) mQualifiers[INDEX_SMALLEST_SCREEN_WIDTH];
    }

    public void setScreenWidthQualifier(ScreenWidthQualifier qualifier) {
        mQualifiers[INDEX_SCREEN_WIDTH] = qualifier;
    }

    @Nullable
    public ScreenWidthQualifier getScreenWidthQualifier() {
        return (ScreenWidthQualifier) mQualifiers[INDEX_SCREEN_WIDTH];
    }

    public void setScreenHeightQualifier(ScreenHeightQualifier qualifier) {
        mQualifiers[INDEX_SCREEN_HEIGHT] = qualifier;
    }

    @Nullable
    public ScreenHeightQualifier getScreenHeightQualifier() {
        return (ScreenHeightQualifier) mQualifiers[INDEX_SCREEN_HEIGHT];
    }

    public void setScreenSizeQualifier(ScreenSizeQualifier qualifier) {
        mQualifiers[INDEX_SCREEN_LAYOUT_SIZE] = qualifier;
    }

    @Nullable
    public ScreenSizeQualifier getScreenSizeQualifier() {
        return (ScreenSizeQualifier)mQualifiers[INDEX_SCREEN_LAYOUT_SIZE];
    }

    public void setScreenRatioQualifier(ScreenRatioQualifier qualifier) {
        mQualifiers[INDEX_SCREEN_RATIO] = qualifier;
    }

    @Nullable
    public ScreenRatioQualifier getScreenRatioQualifier() {
        return (ScreenRatioQualifier)mQualifiers[INDEX_SCREEN_RATIO];
    }

    public void setScreenRoundQualifier(ScreenRoundQualifier qualifier) {
        mQualifiers[INDEX_SCREEN_ROUND] = qualifier;
    }

    @Nullable
    public ScreenRoundQualifier getScreenRoundQualifier() {
        return (ScreenRoundQualifier)mQualifiers[INDEX_SCREEN_ROUND];
    }

    public void setScreenOrientationQualifier(ScreenOrientationQualifier qualifier) {
        mQualifiers[INDEX_SCREEN_ORIENTATION] = qualifier;
    }

    @Nullable
    public ScreenOrientationQualifier getScreenOrientationQualifier() {
        return (ScreenOrientationQualifier)mQualifiers[INDEX_SCREEN_ORIENTATION];
    }

    public void setUiModeQualifier(UiModeQualifier qualifier) {
        mQualifiers[INDEX_UI_MODE] = qualifier;
    }

    @Nullable
    public UiModeQualifier getUiModeQualifier() {
        return (UiModeQualifier)mQualifiers[INDEX_UI_MODE];
    }

    public void setNightModeQualifier(NightModeQualifier qualifier) {
        mQualifiers[INDEX_NIGHT_MODE] = qualifier;
    }

    @Nullable
    public NightModeQualifier getNightModeQualifier() {
        return (NightModeQualifier)mQualifiers[INDEX_NIGHT_MODE];
    }

    public void setDensityQualifier(DensityQualifier qualifier) {
        mQualifiers[INDEX_PIXEL_DENSITY] = qualifier;
    }

    @Nullable
    public DensityQualifier getDensityQualifier() {
        return (DensityQualifier)mQualifiers[INDEX_PIXEL_DENSITY];
    }

    public void setTouchTypeQualifier(TouchScreenQualifier qualifier) {
        mQualifiers[INDEX_TOUCH_TYPE] = qualifier;
    }

    @Nullable
    public TouchScreenQualifier getTouchTypeQualifier() {
        return (TouchScreenQualifier)mQualifiers[INDEX_TOUCH_TYPE];
    }

    public void setKeyboardStateQualifier(KeyboardStateQualifier qualifier) {
        mQualifiers[INDEX_KEYBOARD_STATE] = qualifier;
    }

    @Nullable
    public KeyboardStateQualifier getKeyboardStateQualifier() {
        return (KeyboardStateQualifier)mQualifiers[INDEX_KEYBOARD_STATE];
    }

    public void setTextInputMethodQualifier(TextInputMethodQualifier qualifier) {
        mQualifiers[INDEX_TEXT_INPUT_METHOD] = qualifier;
    }

    @Nullable
    public TextInputMethodQualifier getTextInputMethodQualifier() {
        return (TextInputMethodQualifier)mQualifiers[INDEX_TEXT_INPUT_METHOD];
    }

    public void setNavigationStateQualifier(NavigationStateQualifier qualifier) {
        mQualifiers[INDEX_NAVIGATION_STATE] = qualifier;
    }

    @Nullable
    public NavigationStateQualifier getNavigationStateQualifier() {
        return (NavigationStateQualifier)mQualifiers[INDEX_NAVIGATION_STATE];
    }

    public void setNavigationMethodQualifier(NavigationMethodQualifier qualifier) {
        mQualifiers[INDEX_NAVIGATION_METHOD] = qualifier;
    }

    @Nullable
    public NavigationMethodQualifier getNavigationMethodQualifier() {
        return (NavigationMethodQualifier)mQualifiers[INDEX_NAVIGATION_METHOD];
    }

    public void setScreenDimensionQualifier(ScreenDimensionQualifier qualifier) {
        mQualifiers[INDEX_SCREEN_DIMENSION] = qualifier;
    }

    @Nullable
    public ScreenDimensionQualifier getScreenDimensionQualifier() {
        return (ScreenDimensionQualifier)mQualifiers[INDEX_SCREEN_DIMENSION];
    }

    public void setVersionQualifier(VersionQualifier qualifier) {
        mQualifiers[INDEX_VERSION] = qualifier;
    }

    @Nullable
    public VersionQualifier getVersionQualifier() {
        return (VersionQualifier)mQualifiers[INDEX_VERSION];
    }

    /**
     * Normalize a folder configuration based on the API level of its qualifiers
     */
    public void normalize() {
        int minSdk = 1;
        for (ResourceQualifier qualifier : mQualifiers) {
            if (qualifier != null) {
                int min = qualifier.since();
                if (min > minSdk) {
                    minSdk = min;
                }
            }
        }

        if (minSdk == 1) {
            return;
        }

        if (mQualifiers[INDEX_VERSION] == null ||
                ((VersionQualifier)mQualifiers[INDEX_VERSION]).getVersion() < minSdk) {
            mQualifiers[INDEX_VERSION] = new VersionQualifier(minSdk);
        }
    }

    /**
     * Updates the {@link SmallestScreenWidthQualifier}, {@link ScreenWidthQualifier}, and
     * {@link ScreenHeightQualifier} based on the (required) values of
     * {@link ScreenDimensionQualifier} {@link DensityQualifier}, and
     * {@link ScreenOrientationQualifier}.
     *
     * Also the density cannot be {@link Density#NODPI} as it's not valid on a device.
     */
    public void updateScreenWidthAndHeight() {

        ResourceQualifier sizeQ = mQualifiers[INDEX_SCREEN_DIMENSION];
        ResourceQualifier densityQ = mQualifiers[INDEX_PIXEL_DENSITY];
        ResourceQualifier orientQ = mQualifiers[INDEX_SCREEN_ORIENTATION];

        if (sizeQ != null && densityQ != null && orientQ != null) {
            Density density = ((DensityQualifier) densityQ).getValue();
            if (density == Density.NODPI || density == Density.ANYDPI) {
                return;
            }

            ScreenOrientation orientation = ((ScreenOrientationQualifier) orientQ).getValue();

            int size1 = ((ScreenDimensionQualifier) sizeQ).getValue1();
            int size2 = ((ScreenDimensionQualifier) sizeQ).getValue2();

            // make sure size1 is the biggest (should be the case, but make sure)
            if (size1 < size2) {
                int a = size1;
                size1 = size2;
                size2 = a;
            }

            // compute the dp. round them up since we want -w480dp to match a 480.5dp screen
            int dp1 = (int) Math.ceil(size1 * Density.DEFAULT_DENSITY / density.getDpiValue());
            int dp2 = (int) Math.ceil(size2 * Density.DEFAULT_DENSITY / density.getDpiValue());

            setSmallestScreenWidthQualifier(new SmallestScreenWidthQualifier(dp2));

            switch (orientation) {
                case PORTRAIT:
                    setScreenWidthQualifier(new ScreenWidthQualifier(dp2));
                    setScreenHeightQualifier(new ScreenHeightQualifier(dp1));
                    break;
                case LANDSCAPE:
                    setScreenWidthQualifier(new ScreenWidthQualifier(dp1));
                    setScreenHeightQualifier(new ScreenHeightQualifier(dp2));
                    break;
                case SQUARE:
                    setScreenWidthQualifier(new ScreenWidthQualifier(dp2));
                    setScreenHeightQualifier(new ScreenHeightQualifier(dp2));
                    break;
            }
        }
    }

    /**
     * Returns whether an object is equals to the receiver.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof FolderConfiguration) {
            FolderConfiguration fc = (FolderConfiguration)obj;
            for (int i = 0 ; i < INDEX_COUNT ; i++) {
                ResourceQualifier qualifier = mQualifiers[i];
                ResourceQualifier fcQualifier = fc.mQualifiers[i];
                if (qualifier != null) {
                    if (!qualifier.equals(fcQualifier)) {
                        return false;
                    }
                } else if (fcQualifier != null) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Returns whether the Configuration has only default values.
     */
    public boolean isDefault() {
        for (ResourceQualifier irq : mQualifiers) {
            if (irq != null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the name of a folder with the configuration.
     */
    @NonNull
    public String getFolderName(@NonNull ResourceFolderType folder) {
        StringBuilder result = new StringBuilder(folder.getName());

        for (ResourceQualifier qualifier : mQualifiers) {
            if (qualifier != null) {
                String segment = qualifier.getFolderSegment();
                if (segment != null && !segment.isEmpty()) {
                    result.append(SdkConstants.RES_QUALIFIER_SEP);
                    result.append(segment);
                }
            }
        }

        return result.toString();
    }

  /**
   * Returns the folder configuration as a unique key
   */
  @NonNull
  public String getUniqueKey() {
    StringBuilder result = new StringBuilder(100);

    for (ResourceQualifier qualifier : mQualifiers) {
      if (qualifier != null) {
        String segment = qualifier.getFolderSegment();
        if (segment != null && !segment.isEmpty()) {
          result.append(SdkConstants.RES_QUALIFIER_SEP);
          result.append(segment);
        }
      }
    }

    return result.toString();
  }

  /**
     * Returns {@link #toDisplayString()}.
     */
    @NonNull
    @Override
    public String toString() {
        return toDisplayString();
    }

    /**
     * Returns a string valid for display purpose.
     */
    @NonNull
    public String toDisplayString() {
        if (isDefault()) {
            return "default";
        }

        StringBuilder result = null;
        int index = 0;
        ResourceQualifier qualifier;

        while (index < INDEX_COUNT) {
            qualifier = mQualifiers[index++];
            if (qualifier != null) {
                if (result == null) {
                    result = new StringBuilder();
                } else {
                    result.append(", "); //$NON-NLS-1$
                }
                result.append(qualifier.getLongDisplayValue());

            }
        }

        return result == null ? "" : result.toString();
    }

    /**
     * Returns a string for display purposes which uses only the short names of the qualifiers
     */
    @NonNull
    public String toShortDisplayString() {
        if (isDefault()) {
            return "default";
        }

        StringBuilder result = new StringBuilder(100);
        int index = 0;

        while (index < INDEX_COUNT) {
            ResourceQualifier qualifier = mQualifiers[index++];
            if (qualifier != null) {
                if (result.length() > 0) {
                    result.append(',');
                }
                result.append(qualifier.getShortDisplayValue());
            }
        }

        return result.toString();
    }

    @Override
    public int compareTo(@NonNull FolderConfiguration folderConfig) {
        // default are always at the top.
        if (isDefault()) {
            if (folderConfig.isDefault()) {
                return 0;
            }
            return -1;
        }

        // now we compare the qualifiers
        for (int i = 0 ; i < INDEX_COUNT; i++) {
            ResourceQualifier qualifier1 = mQualifiers[i];
            ResourceQualifier qualifier2 = folderConfig.mQualifiers[i];

            if (qualifier1 == null) {
                if (qualifier2 != null) {
                    return -1;
                }
            } else {
                if (qualifier2 == null) {
                    return 1;
                } else {
                    int result = qualifier1.compareTo(qualifier2);

                    if (result == 0) {
                        continue;
                    }

                    return result;
                }
            }
        }

        // if we arrive here, all the qualifier matches
        return 0;
    }

    /**
     * Returns the best matching {@link Configurable} for this configuration.
     *
     * @param configurables the list of {@link Configurable} to choose from.
     *
     * @return an item from the given list of {@link Configurable} or null.
     *
     * See http://d.android.com/guide/topics/resources/resources-i18n.html#best-match
     */
    @Nullable
    public Configurable findMatchingConfigurable(@Nullable List<? extends Configurable> configurables) {
        // Because we skip qualifiers where reference configuration doesn't have a valid qualifier,
        // we can end up with more than one match. In this case, we just take the first one.
        List<Configurable> matches = findMatchingConfigurables(configurables);
        return matches.isEmpty() ? null : matches.get(0);
    }

    /**
     * Tries to eliminate as many {@link Configurable}s as possible. It skips the
     * {@link ResourceQualifier} if it's not valid and assumes that all resources match it.
     *
     * @param configurables the list of {@code Configurable} to choose from.
     *
     * @return a list of items from the above list. This may be empty.
     */
    @NonNull
    public List<Configurable> findMatchingConfigurables(
            @Nullable List<? extends Configurable> configurables) {
        if (configurables == null) {
            return Collections.emptyList();
        }

        //
        // 1: eliminate resources that contradict the reference configuration
        // 2: pick next qualifier type
        // 3: check if any resources use this qualifier, if no, back to 2, else move on to 4.
        // 4: eliminate resources that don't use this qualifier.
        // 5: if more than one resource left, go back to 2.
        //
        // The precedence of the qualifiers is more important than the number of qualifiers that
        // exactly match the device.

        // 1: eliminate resources that contradict
        ArrayList<Configurable> matchingConfigurables = new ArrayList<Configurable>();
        for (Configurable res : configurables) {
            final FolderConfiguration configuration = res.getConfiguration();
            if (configuration != null && configuration.isMatchFor(this)) {
                matchingConfigurables.add(res);
            }
        }

        // if there is at most one match, just take it
        if (matchingConfigurables.size() < 2) {
            return matchingConfigurables;
        }

        // 2. Loop on the qualifiers, and eliminate matches
        final int count = getQualifierCount();
        for (int q = 0 ; q < count ; q++) {
            // look to see if one configurable has this qualifier.
            // At the same time also record the best match value for the qualifier (if applicable).

            // The reference value, to find the best match.
            // Note that this qualifier could be null. In which case any qualifier found in the
            // possible match, will all be considered best match.
            ResourceQualifier referenceQualifier = getQualifier(q);

            // If referenceQualifier is null, we don't eliminate resources based on it.
            if (referenceQualifier == null) {
                continue;
            }

            boolean found = false;
            ResourceQualifier bestMatch = null; // this is to store the best match.
            for (Configurable configurable : matchingConfigurables) {
                ResourceQualifier qualifier = configurable.getConfiguration().getQualifier(q);
                if (qualifier != null) {
                    // set the flag.
                    found = true;

                    // Now check for a best match. If the reference qualifier is null ,
                    // any qualifier is a "best" match (we don't need to record all of them.
                    // Instead the non compatible ones are removed below)
                    if (qualifier.isBetterMatchThan(bestMatch, referenceQualifier)) {
                        bestMatch = qualifier;
                    }
                }
            }

            // 4. If a configurable has a qualifier at the current index, remove all the ones that
            // do not have one, or whose qualifier value does not equal the best match found above
            // unless there's no reference qualifier, in which case they are all considered
            // "best" match.
            if (found) {
                for (int i = 0 ; i < matchingConfigurables.size(); ) {
                    Configurable configurable = matchingConfigurables.get(i);
                    FolderConfiguration configuration = configurable.getConfiguration();
                    ResourceQualifier qualifier = configuration.getQualifier(q);

                    if (qualifier == null) {
                        // this resources has no qualifier of this type: rejected.
                        matchingConfigurables.remove(configurable);
                    } else if (bestMatch != null && !bestMatch.equals(qualifier)) {
                        // there's a reference qualifier and there is a better match for it than
                        // this resource, so we reject it.
                        matchingConfigurables.remove(configurable);
                    } else {
                        // looks like we keep this resource, move on to the next one.
                        //noinspection AssignmentToForLoopParameter
                        i++;
                    }
                }

                // at this point we may have run out of matching resources before going
                // through all the qualifiers.
                if (matchingConfigurables.size() < 2) {
                    break;
                }
            }
        }

        // We've exhausted all the qualifiers. If we still have matching ones left, return all.
        return matchingConfigurables;
    }

    /**
     * Returns whether the configuration is a match for the given reference config.
     * <p/>A match means that, for each qualifier of this config
     * <ul>
     * <li>The reference config has no value set
     * <li>or, the qualifier of the reference config is a match. Depending on the qualifier type
     * this does not mean the same exact value.</li>
     * </ul>
     * @param referenceConfig The reference configuration to test against.
     * @return true if the configuration matches.
     */
    public boolean isMatchFor(@Nullable FolderConfiguration referenceConfig) {
        if (referenceConfig == null) {
            return false;
        }

        for (int i = 0 ; i < INDEX_COUNT ; i++) {
            ResourceQualifier testQualifier = mQualifiers[i];
            ResourceQualifier referenceQualifier = referenceConfig.mQualifiers[i];

            // it's only a non match if both qualifiers are non-null, and they don't match.
            if (testQualifier != null && referenceQualifier != null &&
                    !testQualifier.isMatchFor(referenceQualifier)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the index of the first non null {@link ResourceQualifier} starting at index
     * <var>startIndex</var>
     * @param startIndex
     * @return -1 if no qualifier was found.
     */
    public int getHighestPriorityQualifier(int startIndex) {
        for (int i = startIndex ; i < INDEX_COUNT ; i++) {
            if (mQualifiers[i] != null) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Create default qualifiers.
     * <p/>This creates qualifiers with no values for all indices.
     */
    public void createDefault() {
        mQualifiers[INDEX_COUNTRY_CODE] = new CountryCodeQualifier();
        mQualifiers[INDEX_NETWORK_CODE] = new NetworkCodeQualifier();
        mQualifiers[INDEX_LOCALE] = new LocaleQualifier();
        mQualifiers[INDEX_LAYOUT_DIR] = new LayoutDirectionQualifier();
        mQualifiers[INDEX_SMALLEST_SCREEN_WIDTH] = new SmallestScreenWidthQualifier();
        mQualifiers[INDEX_SCREEN_WIDTH] = new ScreenWidthQualifier();
        mQualifiers[INDEX_SCREEN_HEIGHT] = new ScreenHeightQualifier();
        mQualifiers[INDEX_SCREEN_LAYOUT_SIZE] = new ScreenSizeQualifier();
        mQualifiers[INDEX_SCREEN_RATIO] = new ScreenRatioQualifier();
        mQualifiers[INDEX_SCREEN_ROUND] = new ScreenRoundQualifier();
        mQualifiers[INDEX_SCREEN_ORIENTATION] = new ScreenOrientationQualifier();
        mQualifiers[INDEX_UI_MODE] = new UiModeQualifier();
        mQualifiers[INDEX_NIGHT_MODE] = new NightModeQualifier();
        mQualifiers[INDEX_PIXEL_DENSITY] = new DensityQualifier();
        mQualifiers[INDEX_TOUCH_TYPE] = new TouchScreenQualifier();
        mQualifiers[INDEX_KEYBOARD_STATE] = new KeyboardStateQualifier();
        mQualifiers[INDEX_TEXT_INPUT_METHOD] = new TextInputMethodQualifier();
        mQualifiers[INDEX_NAVIGATION_STATE] = new NavigationStateQualifier();
        mQualifiers[INDEX_NAVIGATION_METHOD] = new NavigationMethodQualifier();
        mQualifiers[INDEX_SCREEN_DIMENSION] = new ScreenDimensionQualifier();
        mQualifiers[INDEX_VERSION] = new VersionQualifier();
    }

    /**
     * Returns an array of all the non null qualifiers.
     */
    @NonNull
    public ResourceQualifier[] getQualifiers() {
        int count = 0;
        for (int i = 0 ; i < INDEX_COUNT ; i++) {
            if (mQualifiers[i] != null) {
                count++;
            }
        }

        ResourceQualifier[] array = new ResourceQualifier[count];
        int index = 0;
        for (int i = 0 ; i < INDEX_COUNT ; i++) {
            if (mQualifiers[i] != null) {
                array[index++] = mQualifiers[i];
            }
        }

        return array;
    }
}
