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

package com.android.tools.lint.checks;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.FilterData;
import com.android.build.OutputFile;
import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.AndroidArtifactOutput;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.ProductFlavor;
import com.android.builder.model.ProductFlavorContainer;
import com.android.builder.model.Variant;
import com.android.tools.lint.client.api.LintClient;
import com.android.tools.lint.client.api.LintDriver;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Project;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.mockito.stubbing.OngoingStubbing;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("javadoc")
public class IconDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new IconDetector();
    }

    private Set<Issue> mEnabled = new HashSet<Issue>();
    private boolean mAbbreviate;

    private static final Set<Issue> ALL = new HashSet<Issue>();
    static {
        ALL.add(IconDetector.DUPLICATES_CONFIGURATIONS);
        ALL.add(IconDetector.DUPLICATES_NAMES);
        ALL.add(IconDetector.GIF_USAGE);
        ALL.add(IconDetector.ICON_DENSITIES);
        ALL.add(IconDetector.ICON_DIP_SIZE);
        ALL.add(IconDetector.ICON_EXTENSION);
        ALL.add(IconDetector.ICON_LOCATION);
        ALL.add(IconDetector.ICON_MISSING_FOLDER);
        ALL.add(IconDetector.ICON_NODPI);
        ALL.add(IconDetector.ICON_COLORS);
        ALL.add(IconDetector.ICON_XML_AND_PNG);
        ALL.add(IconDetector.ICON_LAUNCHER_SHAPE);
        ALL.add(IconDetector.ICON_MIX_9PNG);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAbbreviate = true;
    }

    @Override
    protected void configureDriver(LintDriver driver) {
        driver.setAbbreviating(mAbbreviate);
    }

    @Override
    protected TestConfiguration getConfiguration(LintClient client, Project project) {
        return new TestConfiguration(client, project, null) {
            @Override
            public boolean isEnabled(@NonNull Issue issue) {
                return super.isEnabled(issue) && mEnabled.contains(issue);
            }
        };
    }

    public void test() throws Exception {
        mEnabled = ALL;
        assertEquals(
            "res/drawable-mdpi/sample_icon.gif: Warning: Using the .gif format for bitmaps is discouraged [GifUsage]\n" +
            "res/drawable/ic_launcher.png: Warning: The ic_launcher.png icon has identical contents in the following configuration folders: drawable-mdpi, drawable [IconDuplicatesConfig]\n" +
            "    res/drawable-mdpi/ic_launcher.png: <No location-specific message\n" +
            "res/drawable/ic_launcher.png: Warning: Found bitmap drawable res/drawable/ic_launcher.png in densityless folder [IconLocation]\n" +
            "res/drawable-hdpi: Warning: Missing the following drawables in drawable-hdpi: sample_icon.gif (found in drawable-mdpi) [IconDensities]\n" +
            "res: Warning: Missing density variation folders in res: drawable-xhdpi, drawable-xxhdpi, drawable-xxxhdpi [IconMissingDensityFolder]\n" +
            "0 errors, 5 warnings\n" +
            "",

            lintProject(
                    // Use minSDK4 to ensure that we get warnings about missing drawables
                    "apicheck/minsdk4.xml=>AndroidManifest.xml",
                    "res/drawable/ic_launcher.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher.png",
                    "res/drawable-mdpi/sample_icon.gif",
                    // Make a dummy file named .svn to make sure it doesn't get seen as
                    // an icon name
                    "res/drawable-mdpi/sample_icon.gif=>res/drawable-hdpi/.svn",
                    "res/drawable-hdpi/ic_launcher.png"));
    }

    public void testMixed() throws Exception {
        mEnabled = Collections.singleton(IconDetector.ICON_XML_AND_PNG);
        assertEquals(
            "res/drawable/background.xml: Warning: The following images appear both as density independent .xml files and as bitmap files: res/drawable-mdpi/background.png, res/drawable/background.xml [IconXmlAndPng]\n" +
            "    res/drawable-mdpi/background.png: <No location-specific message\n" +
            "0 errors, 1 warnings\n",

            lintProject(
                    "apicheck/minsdk4.xml=>AndroidManifest.xml",
                    "apicheck/minsdk4.xml=>res/drawable/background.xml",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/background.png"));
    }

    public void testApi1() throws Exception {
        mEnabled = ALL;
        assertEquals(
            "No warnings.",

            lintProject(
                    // manifest file which specifies uses sdk = 2
                    "apicheck/minsdk2.xml=>AndroidManifest.xml",
                    "res/drawable/ic_launcher.png"));
    }

    public void test2() throws Exception {
        mEnabled = ALL;
        assertEquals(
            "res/drawable-hdpi/other.9.png: Warning: The following unrelated icon files have identical contents: appwidget_bg.9.png, other.9.png [IconDuplicates]\n" +
            "    res/drawable-hdpi/appwidget_bg.9.png: <No location-specific message\n" +
            "res/drawable-hdpi/unrelated.png: Warning: The following unrelated icon files have identical contents: ic_launcher.png, unrelated.png [IconDuplicates]\n" +
            "    res/drawable-hdpi/ic_launcher.png: <No location-specific message\n" +
            "res: Warning: Missing density variation folders in res: drawable-mdpi, drawable-xhdpi, drawable-xxhdpi, drawable-xxxhdpi [IconMissingDensityFolder]\n" +
            "0 errors, 3 warnings\n",

            lintProject(
                    "res/drawable-hdpi/unrelated.png",
                    "res/drawable-hdpi/appwidget_bg.9.png",
                    "res/drawable-hdpi/appwidget_bg_focus.9.png",
                    "res/drawable-hdpi/other.9.png",
                    "res/drawable-hdpi/ic_launcher.png"
                    ));
    }

    public void testNoDpi() throws Exception {
        mEnabled = ALL;
        assertEquals(
            "res/drawable-mdpi/frame.png: Warning: The following images appear in both -nodpi and in a density folder: frame.png [IconNoDpi]\n" +
            "res/drawable-xlarge-nodpi-v11/frame.png: Warning: The frame.png icon has identical contents in the following configuration folders: drawable-mdpi, drawable-nodpi, drawable-xlarge-nodpi-v11 [IconDuplicatesConfig]\n" +
            "    res/drawable-nodpi/frame.png: <No location-specific message\n" +
            "    res/drawable-mdpi/frame.png: <No location-specific message\n" +
            "res: Warning: Missing density variation folders in res: drawable-hdpi, drawable-xhdpi, drawable-xxhdpi, drawable-xxxhdpi [IconMissingDensityFolder]\n" +
            "0 errors, 3 warnings\n" +
            "",

            lintProject(
                "res/drawable-mdpi/frame.png",
                "res/drawable-nodpi/frame.png",
                "res/drawable-xlarge-nodpi-v11/frame.png"));
    }

    public void testNoDpi2() throws Exception {
        mEnabled = ALL;
        // Having additional icon names in the no-dpi folder should not cause any complaints
        assertEquals(
            "res/drawable-xxxhdpi/frame.png: Warning: The image frame.png varies significantly in its density-independent (dip) size across the various density versions: drawable-ldpi/frame.png: 629x387 dp (472x290 px), drawable-mdpi/frame.png: 472x290 dp (472x290 px), drawable-hdpi/frame.png: 315x193 dp (472x290 px), drawable-xhdpi/frame.png: 236x145 dp (472x290 px), drawable-xxhdpi/frame.png: 157x97 dp (472x290 px), drawable-xxxhdpi/frame.png: 118x73 dp (472x290 px) [IconDipSize]\n" +
            "    res/drawable-xxhdpi/frame.png: <No location-specific message\n" +
            "    res/drawable-xhdpi/frame.png: <No location-specific message\n" +
            "    res/drawable-hdpi/frame.png: <No location-specific message\n" +
            "    res/drawable-mdpi/frame.png: <No location-specific message\n" +
            "    res/drawable-ldpi/frame.png: <No location-specific message\n" +
            "res/drawable-xxxhdpi/frame.png: Warning: The following unrelated icon files have identical contents: frame.png, frame.png, frame.png, file1.png, file2.png, frame.png, frame.png, frame.png [IconDuplicates]\n" +
            "    res/drawable-xxhdpi/frame.png: <No location-specific message\n" +
            "    res/drawable-xhdpi/frame.png: <No location-specific message\n" +
            "    res/drawable-nodpi/file2.png: <No location-specific message\n" +
            "    res/drawable-nodpi/file1.png: <No location-specific message\n" +
            "    res/drawable-mdpi/frame.png: <No location-specific message\n" +
            "    res/drawable-ldpi/frame.png: <No location-specific message\n" +
            "    res/drawable-hdpi/frame.png: <No location-specific message\n" +
            "0 errors, 2 warnings\n" +
            "",

            lintProject(
                    "res/drawable-mdpi/frame.png=>res/drawable-mdpi/frame.png",
                    "res/drawable-mdpi/frame.png=>res/drawable-hdpi/frame.png",
                    "res/drawable-mdpi/frame.png=>res/drawable-ldpi/frame.png",
                    "res/drawable-mdpi/frame.png=>res/drawable-xhdpi/frame.png",
                    "res/drawable-mdpi/frame.png=>res/drawable-xxhdpi/frame.png",
                    "res/drawable-mdpi/frame.png=>res/drawable-xxxhdpi/frame.png",
                    "res/drawable-mdpi/frame.png=>res/drawable-nodpi/file1.png",
                    "res/drawable-mdpi/frame.png=>res/drawable-nodpi/file2.png"));
    }

    public void testNoDpiMix() throws Exception {
        mEnabled = ALL;
        assertEquals(
            "res/drawable-mdpi/frame.xml: Warning: The following images appear in both -nodpi and in a density folder: frame.png, frame.xml [IconNoDpi]\n" +
            "    res/drawable-mdpi/frame.png: <No location-specific message\n" +
            "res/drawable-nodpi/frame.xml: Warning: The following images appear both as density independent .xml files and as bitmap files: res/drawable-mdpi/frame.png, res/drawable-nodpi/frame.xml [IconXmlAndPng]\n" +
            "    res/drawable-mdpi/frame.png: <No location-specific message\n" +
            "res: Warning: Missing density variation folders in res: drawable-hdpi, drawable-xhdpi, drawable-xxhdpi, drawable-xxxhdpi [IconMissingDensityFolder]\n" +
            "0 errors, 3 warnings\n",

            lintProject(
                "res/drawable-mdpi/frame.png",
                "res/drawable/states.xml=>res/drawable-nodpi/frame.xml"));
    }


    public void testMixedFormat() throws Exception {
        mEnabled = ALL;
        // Test having a mixture of .xml and .png resources for the same name
        // Make sure we don't get:
        // drawable-hdpi: Warning: Missing the following drawables in drawable-hdpi: f.png (found in drawable-mdpi)
        // drawable-xhdpi: Warning: Missing the following drawables in drawable-xhdpi: f.png (found in drawable-mdpi)
        assertEquals(
            "res/drawable-xxxhdpi/f.xml: Warning: The following images appear both as density independent .xml files and as bitmap files: res/drawable-hdpi/f.xml, res/drawable-mdpi/f.png [IconXmlAndPng]\n" +
            "    res/drawable-xxhdpi/f.xml: <No location-specific message\n" +
            "    res/drawable-xhdpi/f.xml: <No location-specific message\n" +
            "    res/drawable-mdpi/f.png: <No location-specific message\n" +
            "    res/drawable-hdpi/f.xml: <No location-specific message\n" +
            "0 errors, 1 warnings\n",

            lintProject(
                    "res/drawable-mdpi/frame.png=>res/drawable-mdpi/f.png",
                    "res/drawable/states.xml=>res/drawable-hdpi/f.xml",
                    "res/drawable/states.xml=>res/drawable-xhdpi/f.xml",
                    "res/drawable/states.xml=>res/drawable-xxhdpi/f.xml",
                    "res/drawable/states.xml=>res/drawable-xxxhdpi/f.xml"));
    }

    public void testMisleadingFileName() throws Exception {
        mEnabled = Collections.singleton(IconDetector.ICON_EXTENSION);
        assertEquals(
            "res/drawable-mdpi/frame.gif: Warning: Misleading file extension; named .gif but the file format is png [IconExtension]\n" +
            "res/drawable-mdpi/frame.jpg: Warning: Misleading file extension; named .jpg but the file format is png [IconExtension]\n" +
            "res/drawable-mdpi/myjpg.png: Warning: Misleading file extension; named .png but the file format is JPEG [IconExtension]\n" +
            "res/drawable-mdpi/sample_icon.jpeg: Warning: Misleading file extension; named .jpeg but the file format is gif [IconExtension]\n" +
            "res/drawable-mdpi/sample_icon.jpg: Warning: Misleading file extension; named .jpg but the file format is gif [IconExtension]\n" +
            "res/drawable-mdpi/sample_icon.png: Warning: Misleading file extension; named .png but the file format is gif [IconExtension]\n" +
            "0 errors, 6 warnings\n",

            lintProject(
                "res/drawable-mdpi/sample_icon.jpg=>res/drawable-mdpi/myjpg.jpg", // VALID
                "res/drawable-mdpi/sample_icon.jpg=>res/drawable-mdpi/myjpg.jpeg", // VALID
                "res/drawable-mdpi/frame.png=>res/drawable-mdpi/frame.gif",
                "res/drawable-mdpi/frame.png=>res/drawable-mdpi/frame.jpg",
                "res/drawable-mdpi/sample_icon.jpg=>res/drawable-mdpi/myjpg.png",
                "res/drawable-mdpi/sample_icon.gif=>res/drawable-mdpi/sample_icon.jpg",
                "res/drawable-mdpi/sample_icon.gif=>res/drawable-mdpi/sample_icon.jpeg",
                "res/drawable-mdpi/sample_icon.gif=>res/drawable-mdpi/sample_icon.png"));
    }

    public void testColors() throws Exception {
        mEnabled = Collections.singleton(IconDetector.ICON_COLORS);
        assertEquals(
            "res/drawable-mdpi/ic_menu_my_action.png: Warning: Action Bar icons should use a single gray color (#333333 for light themes (with 60%/30% opacity for enabled/disabled), and #FFFFFF with opacity 80%/30% for dark themes [IconColors]\n" +
            "res/drawable-mdpi-v11/ic_stat_my_notification.png: Warning: Notification icons must be entirely white [IconColors]\n" +
            "res/drawable-mdpi-v9/ic_stat_my_notification2.png: Warning: Notification icons must be entirely white [IconColors]\n" +
            "0 errors, 3 warnings\n",

            lintProject(
                "apicheck/minsdk14.xml=>AndroidManifest.xml",
                "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_menu_my_action.png",
                "res/drawable/ic_launcher.png=>res/drawable-mdpi-v11/ic_stat_my_notification.png",
                "res/drawable/ic_launcher.png=>res/drawable-mdpi-v9/ic_stat_my_notification2.png",
                "res/drawable-mdpi/ic_menu_add_clip_normal.png")); // OK
    }

    public void testNotActionBarIcons() throws Exception {
        mEnabled = Collections.singleton(IconDetector.ICON_COLORS);
        assertEquals(
            "No warnings.",

            // No Java code designates the menu as an action bar menu
            lintProject(
                "apicheck/minsdk14.xml=>AndroidManifest.xml",
                "res/menu/menu.xml",
                "res/drawable/ic_launcher.png=>res/drawable-mdpi/icon1.png",
                "res/drawable/ic_launcher.png=>res/drawable-mdpi/icon2.png",
                "res/drawable/ic_launcher.png=>res/drawable-mdpi/icon3.png", // Not action bar
                "res/drawable-mdpi/ic_menu_add_clip_normal.png")); // OK
    }

    public void testActionBarIcons() throws Exception {
        mEnabled = Collections.singleton(IconDetector.ICON_COLORS);
        assertEquals(
            "res/drawable-mdpi/icon1.png: Warning: Action Bar icons should use a single gray color (#333333 for light themes (with 60%/30% opacity for enabled/disabled), and #FFFFFF with opacity 80%/30% for dark themes [IconColors]\n" +
            "res/drawable-mdpi/icon2.png: Warning: Action Bar icons should use a single gray color (#333333 for light themes (with 60%/30% opacity for enabled/disabled), and #FFFFFF with opacity 80%/30% for dark themes [IconColors]\n" +
            "0 errors, 2 warnings\n",

            lintProject(
                "apicheck/minsdk14.xml=>AndroidManifest.xml",
                "res/menu/menu.xml",
                "src/test/pkg/ActionBarTest.java.txt=>src/test/pkg/ActionBarTest.java",
                "res/drawable/ic_launcher.png=>res/drawable-mdpi/icon1.png",
                "res/drawable/ic_launcher.png=>res/drawable-mdpi/icon2.png",
                "res/drawable/ic_launcher.png=>res/drawable-mdpi/icon3.png", // Not action bar
                "res/drawable-mdpi/ic_menu_add_clip_normal.png")); // OK
    }

    public void testOkActionBarIcons() throws Exception {
        mEnabled = Collections.singleton(IconDetector.ICON_COLORS);
        assertEquals(
            "No warnings.",

            lintProject(
                "apicheck/minsdk14.xml=>AndroidManifest.xml",
                "res/menu/menu.xml",
                "res/drawable-mdpi/ic_menu_add_clip_normal.png=>res/drawable-mdpi/icon1.png",
                "res/drawable-mdpi/ic_menu_add_clip_normal.png=>res/drawable-mdpi/icon2.png"));
    }

    public void testNotificationIcons() throws Exception {
        mEnabled = Collections.singleton(IconDetector.ICON_COLORS);
        assertEquals(
            "res/drawable-mdpi/icon1.png: Warning: Notification icons must be entirely white [IconColors]\n" +
            "res/drawable-mdpi/icon2.png: Warning: Notification icons must be entirely white [IconColors]\n" +
            "res/drawable-mdpi/icon3.png: Warning: Notification icons must be entirely white [IconColors]\n" +
            "res/drawable-mdpi/icon4.png: Warning: Notification icons must be entirely white [IconColors]\n" +
            "res/drawable-mdpi/icon5.png: Warning: Notification icons must be entirely white [IconColors]\n" +
            "0 errors, 5 warnings\n",

            lintProject(
                "apicheck/minsdk14.xml=>AndroidManifest.xml",
                "src/test/pkg/NotificationTest.java.txt=>src/test/pkg/NotificationTest.java",
                "res/drawable/ic_launcher.png=>res/drawable-mdpi/icon1.png",
                "res/drawable/ic_launcher.png=>res/drawable-mdpi/icon2.png",
                "res/drawable/ic_launcher.png=>res/drawable-mdpi/icon3.png",
                "res/drawable/ic_launcher.png=>res/drawable-mdpi/icon4.png",
                "res/drawable/ic_launcher.png=>res/drawable-mdpi/icon5.png",
                "res/drawable/ic_launcher.png=>res/drawable-mdpi/icon6.png", // not a notification
                "res/drawable/ic_launcher.png=>res/drawable-mdpi/icon7.png", // ditto
                "res/drawable-mdpi/ic_menu_add_clip_normal.png")); // OK
    }

    public void testOkNotificationIcons() throws Exception {
        mEnabled = Collections.singleton(IconDetector.ICON_COLORS);
        assertEquals(
            "No warnings.",

            lintProject(
                "apicheck/minsdk14.xml=>AndroidManifest.xml",
                "src/test/pkg/NotificationTest.java.txt=>src/test/pkg/NotificationTest.java",
                "res/drawable-mdpi/ic_menu_add_clip_normal.png=>res/drawable-mdpi/icon1.png",
                "res/drawable-mdpi/ic_menu_add_clip_normal.png=>res/drawable-mdpi/icon2.png",
                "res/drawable-mdpi/ic_menu_add_clip_normal.png=>res/drawable-mdpi/icon3.png",
                "res/drawable-mdpi/ic_menu_add_clip_normal.png=>res/drawable-mdpi/icon4.png",
                "res/drawable-mdpi/ic_menu_add_clip_normal.png=>res/drawable-mdpi/icon5.png"));
    }

    public void testExpectedSize() throws Exception {
        mEnabled = Collections.singleton(IconDetector.ICON_EXPECTED_SIZE);
        assertEquals(
            "res/drawable-mdpi/ic_launcher.png: Warning: Incorrect icon size for drawable-mdpi/ic_launcher.png: expected 48x48, but was 24x24 [IconExpectedSize]\n" +
            "res/drawable-mdpi/icon1.png: Warning: Incorrect icon size for drawable-mdpi/icon1.png: expected 32x32, but was 48x48 [IconExpectedSize]\n" +
            "res/drawable-mdpi/icon3.png: Warning: Incorrect icon size for drawable-mdpi/icon3.png: expected 24x24, but was 48x48 [IconExpectedSize]\n" +
            "0 errors, 3 warnings\n",

            lintProject(
                "apicheck/minsdk14.xml=>AndroidManifest.xml",
                "src/test/pkg/NotificationTest.java.txt=>src/test/pkg/NotificationTest.java",
                "res/menu/menu.xml",
                "src/test/pkg/ActionBarTest.java.txt=>src/test/pkg/ActionBarTest.java",

                // 3 wrong-sized icons:
                "res/drawable/ic_launcher.png=>res/drawable-mdpi/icon1.png",
                "res/drawable/ic_launcher.png=>res/drawable-mdpi/icon3.png",
                "res/drawable-mdpi/stat_notify_alarm.png=>res/drawable-mdpi/ic_launcher.png",

                // OK sizes
                "res/drawable-mdpi/ic_menu_add_clip_normal.png=>res/drawable-mdpi/icon2.png",
                "res/drawable-mdpi/stat_notify_alarm.png=>res/drawable-mdpi/icon4.png",
                "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher2.png"
            ));
    }

    public void testAbbreviate() throws Exception {
        mEnabled = Collections.singleton(IconDetector.ICON_DENSITIES);
        assertEquals(
            "res/drawable-hdpi: Warning: Missing the following drawables in drawable-hdpi: " +
            "ic_launcher10.png, ic_launcher11.png, ic_launcher12.png, ic_launcher2.png, " +
            "ic_launcher3.png... (6 more) [IconDensities]\n" +
            "res/drawable-xhdpi: Warning: Missing the following drawables in drawable-xhdpi: " +
            "ic_launcher10.png, ic_launcher11.png, ic_launcher12.png, ic_launcher2.png, " +
            "ic_launcher3.png... (6 more) [IconDensities]\n" +
            "0 errors, 2 warnings\n",

            lintProject(
                    // Use minSDK4 to ensure that we get warnings about missing drawables
                    "apicheck/minsdk4.xml=>AndroidManifest.xml",
                    "res/drawable/ic_launcher.png=>res/drawable-hdpi/ic_launcher1.png",
                    "res/drawable/ic_launcher.png=>res/drawable-xhdpi/ic_launcher1.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher1.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher2.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher3.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher4.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher5.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher6.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher7.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher8.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher9.webp",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher10.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher11.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher12.png"
            ));
    }

    public void testShowAll() throws Exception {
        mEnabled = Collections.singleton(IconDetector.ICON_DENSITIES);
        mAbbreviate = false;
        assertEquals(
            "res/drawable-hdpi: Warning: Missing the following drawables in drawable-hdpi: " +
            "ic_launcher10.png, ic_launcher11.png, ic_launcher12.png, ic_launcher2.png, " +
            "ic_launcher3.png, ic_launcher4.png, ic_launcher5.png, ic_launcher6.png, " +
            "ic_launcher7.png, ic_launcher8.png, ic_launcher9.png [IconDensities]\n" +
            "res/drawable-xhdpi: Warning: Missing the following drawables in drawable-xhdpi: " +
            "ic_launcher10.png, ic_launcher11.png, ic_launcher12.png, ic_launcher2.png," +
            " ic_launcher3.png, ic_launcher4.png, ic_launcher5.png, ic_launcher6.png, " +
            "ic_launcher7.png, ic_launcher8.png, ic_launcher9.png [IconDensities]\n" +
            "0 errors, 2 warnings\n",

            lintProject(
                    // Use minSDK4 to ensure that we get warnings about missing drawables
                    "apicheck/minsdk4.xml=>AndroidManifest.xml",
                    "res/drawable/ic_launcher.png=>res/drawable-hdpi/ic_launcher1.png",
                    "res/drawable/ic_launcher.png=>res/drawable-xhdpi/ic_launcher1.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher1.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher2.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher3.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher4.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher5.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher6.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher7.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher8.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher9.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher10.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher11.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher12.png"
            ));
    }

    public void testIgnoreMissingFolders() throws Exception {
        mEnabled = Collections.singleton(IconDetector.ICON_DENSITIES);
        assertEquals(
            "No warnings.",

            lintProject(
                    // Use minSDK4 to ensure that we get warnings about missing drawables
                    "apicheck/minsdk4.xml=>AndroidManifest.xml",
                    "ignoremissing.xml=>lint.xml",
                    "res/drawable/ic_launcher.png=>res/drawable-hdpi/ic_launcher1.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher1.png",
                    "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher2.png"
            ));
    }

    public void testSquareLauncher() throws Exception {
        mEnabled = Collections.singleton(IconDetector.ICON_LAUNCHER_SHAPE);
        assertEquals(
            "res/drawable-hdpi/ic_launcher_filled.png: Warning: Launcher icons should not fill every pixel of their square region; see the design guide for details [IconLauncherShape]\n" +
            "0 errors, 1 warnings\n",

            lintProject(
                    "apicheck/minsdk4.xml=>AndroidManifest.xml",
                    "res/drawable-hdpi/filled.png=>res/drawable-hdpi/ic_launcher_filled.png",
                    "res/drawable-mdpi/sample_icon.gif=>res/drawable-mdpi/ic_launcher_2.gif"
            ));
    }

    public void testMixNinePatch() throws Exception {
        // https://code.google.com/p/android/issues/detail?id=43075
        mEnabled = Collections.singleton(IconDetector.ICON_MIX_9PNG);
        assertEquals(""
                + "res/drawable-mdpi/ic_launcher_filled.png: Warning: The files ic_launcher_filled.png and ic_launcher_filled.9.png clash; both will map to @drawable/ic_launcher_filled [IconMixedNinePatch]\n"
                + "    res/drawable-hdpi/ic_launcher_filled.png: <No location-specific message\n"
                + "    res/drawable-hdpi/ic_launcher_filled.9.png: <No location-specific message\n"
                + "0 errors, 1 warnings\n",

                lintProject(
                        "apicheck/minsdk4.xml=>AndroidManifest.xml",
                        "res/drawable-hdpi/filled.png=>res/drawable-mdpi/ic_launcher_filled.png",
                        "res/drawable-hdpi/filled.png=>res/drawable-hdpi/ic_launcher_filled.png",
                        "res/drawable-hdpi/filled.png=>res/drawable-hdpi/ic_launcher_filled.9.png",
                        "res/drawable-mdpi/sample_icon.gif=>res/drawable-mdpi/ic_launcher_2.gif"
                ));
    }

    public void test67486() throws Exception {
        // Regression test for https://code.google.com/p/android/issues/detail?id=67486
        mEnabled = Collections.singleton(IconDetector.ICON_COLORS);
        assertEquals("No warnings.",

                lintProject(
                        "apicheck/minsdk14.xml=>AndroidManifest.xml",
                        "res/drawable-xhdpi/ic_stat_notify.png=>res/drawable-xhdpi/ic_stat_notify.png"
                ));
    }

    public void testDuplicatesWithDpNames() throws Exception {
        // Regression test for https://code.google.com/p/android/issues/detail?id=74584
        mEnabled = Collections.singleton(IconDetector.DUPLICATES_NAMES);
        assertEquals("No warnings.",

                lintProject(
                        "res/drawable-hdpi/unrelated.png=>res/drawable-mdpi/foo_72dp.png",
                        "res/drawable-hdpi/unrelated.png=>res/drawable-xhdpi/foo_36dp.png"
                ));
    }

    public void testClaimedSize() throws Exception {
        // Check that icons which declare a dp size actually correspond to that dp size
        mEnabled = Collections.singleton(IconDetector.ICON_DIP_SIZE);
        assertEquals(""
                + "res/drawable-xhdpi/foo_30dp.png: Warning: Suspicious file name foo_30dp.png: The implied 30 dp size does not match the actual dp size (pixel size 72\u00d772 in a drawable-xhdpi folder computes to 36\u00d736 dp) [IconDipSize]\n"
                + "res/drawable-mdpi/foo_80dp.png: Warning: Suspicious file name foo_80dp.png: The implied 80 dp size does not match the actual dp size (pixel size 72\u00d772 in a drawable-mdpi folder computes to 72\u00d772 dp) [IconDipSize]\n"
                + "0 errors, 2 warnings\n",

                lintProject(
                        "res/drawable-hdpi/unrelated.png=>res/drawable-mdpi/foo_72dp.png", // ok
                        "res/drawable-hdpi/unrelated.png=>res/drawable-mdpi/foo_80dp.png", // wrong
                        "res/drawable-hdpi/unrelated.png=>res/drawable-xhdpi/foo_36dp.png",  // ok
                        "res/drawable-hdpi/unrelated.png=>res/drawable-xhdpi/foo_35dp.png",  // ~ok
                        "res/drawable-hdpi/unrelated.png=>res/drawable-xhdpi/foo_30dp.png"  // wrong
                ));
    }

    public void testResConfigs1() throws Exception {
        // resConfigs in the Gradle model sets up the specific set of resource configs
        // that are included in the packaging: we use this to limit the set of required
        // densities
        mEnabled = Sets.newHashSet(IconDetector.ICON_DENSITIES, IconDetector.ICON_MISSING_FOLDER);
        assertEquals(""
                + "res: Warning: Missing density variation folders in res: drawable-hdpi [IconMissingDensityFolder]\n"
                + "0 errors, 1 warnings\n",

                lintProject(
                        "res/drawable-mdpi/frame.png",
                        "res/drawable-nodpi/frame.png",
                        "res/drawable-xlarge-nodpi-v11/frame.png"));
    }

    public void testResConfigs2() throws Exception {
        mEnabled = Sets.newHashSet(IconDetector.ICON_DENSITIES, IconDetector.ICON_MISSING_FOLDER);
        assertEquals(""
                + "res/drawable-hdpi: Warning: Missing the following drawables in drawable-hdpi: sample_icon.gif (found in drawable-mdpi) [IconDensities]\n"
                + "0 errors, 1 warnings\n",

                lintProject(
                        // Use minSDK4 to ensure that we get warnings about missing drawables
                        "apicheck/minsdk4.xml=>AndroidManifest.xml",
                        "res/drawable/ic_launcher.png",
                        "res/drawable/ic_launcher.png=>res/drawable-mdpi/ic_launcher.png",
                        "res/drawable/ic_launcher.png=>res/drawable-xhdpi/ic_launcher.png",
                        "res/drawable-mdpi/sample_icon.gif",
                        "res/drawable-hdpi/ic_launcher.png"));
    }

    public void testSplits1() throws Exception {
        // splits in the Gradle model sets up the specific set of resource configs
        // that are included in the packaging: we use this to limit the set of required
        // densities
        mEnabled = Sets.newHashSet(IconDetector.ICON_DENSITIES, IconDetector.ICON_MISSING_FOLDER);
        assertEquals(""
                        + "res: Warning: Missing density variation folders in res: drawable-hdpi [IconMissingDensityFolder]\n"
                        + "0 errors, 1 warnings\n",

                lintProject(
                        "res/drawable-mdpi/frame.png",
                        "res/drawable-nodpi/frame.png",
                        "res/drawable-xlarge-nodpi-v11/frame.png"));
    }

    @Override
    protected TestLintClient createClient() {
        String testName = getName();
        if (testName.startsWith("testResConfigs")) {
            return createClientForTestResConfigs();
        } else if (testName.startsWith("testSplits")) {
            return createClientForTestSplits();
        } else {
            return super.createClient();
        }
    }

    private TestLintClient createClientForTestResConfigs() {

        // Set up a mock project model for the resource configuration test(s)
        // where we provide a subset of densities to be included

        return new TestLintClient() {
            @NonNull
            @Override
            protected Project createProject(@NonNull File dir, @NonNull File referenceDir) {
                return new Project(this, dir, referenceDir) {
                    @Override
                    public boolean isGradleProject() {
                        return true;
                    }

                    @Nullable
                    @Override
                    public AndroidProject getGradleProjectModel() {
                        /*
                        Simulate variant freeBetaDebug in this setup:
                            defaultConfig {
                                ...
                                resConfigs "mdpi"
                            }
                            flavorDimensions  "pricing", "releaseType"
                            productFlavors {
                                beta {
                                    flavorDimension "releaseType"
                                    resConfig "en"
                                    resConfigs "nodpi", "hdpi"
                                }
                                normal { flavorDimension "releaseType" }
                                free { flavorDimension "pricing" }
                                paid { flavorDimension "pricing" }
                            }
                         */
                        ProductFlavor flavorFree = mock(ProductFlavor.class);
                        when(flavorFree.getName()).thenReturn("free");
                        when(flavorFree.getResourceConfigurations())
                                .thenReturn(Collections.<String>emptyList());

                        ProductFlavor flavorNormal = mock(ProductFlavor.class);
                        when(flavorNormal.getName()).thenReturn("normal");
                        when(flavorNormal.getResourceConfigurations())
                                .thenReturn(Collections.<String>emptyList());

                        ProductFlavor flavorPaid = mock(ProductFlavor.class);
                        when(flavorPaid.getName()).thenReturn("paid");
                        when(flavorPaid.getResourceConfigurations())
                                .thenReturn(Collections.<String>emptyList());

                        ProductFlavor flavorBeta = mock(ProductFlavor.class);
                        when(flavorBeta.getName()).thenReturn("beta");
                        List<String> resConfigs = Arrays.asList("hdpi", "en", "nodpi");
                        when(flavorBeta.getResourceConfigurations()).thenReturn(resConfigs);

                        ProductFlavor defaultFlavor = mock(ProductFlavor.class);
                        when(defaultFlavor.getName()).thenReturn("main");
                        when(defaultFlavor.getResourceConfigurations()).thenReturn(
                                Collections.singleton("mdpi"));

                        ProductFlavorContainer containerBeta =
                                mock(ProductFlavorContainer.class);
                        when(containerBeta.getProductFlavor()).thenReturn(flavorBeta);

                        ProductFlavorContainer containerFree =
                                mock(ProductFlavorContainer.class);
                        when(containerFree.getProductFlavor()).thenReturn(flavorFree);

                        ProductFlavorContainer containerPaid =
                                mock(ProductFlavorContainer.class);
                        when(containerPaid.getProductFlavor()).thenReturn(flavorPaid);

                        ProductFlavorContainer containerNormal =
                                mock(ProductFlavorContainer.class);
                        when(containerNormal.getProductFlavor()).thenReturn(flavorNormal);

                        ProductFlavorContainer defaultContainer =
                                mock(ProductFlavorContainer.class);
                        when(defaultContainer.getProductFlavor()).thenReturn(defaultFlavor);

                        List<ProductFlavorContainer> containers = Arrays.asList(
                                containerPaid, containerFree, containerNormal, containerBeta
                        );

                        AndroidProject project = mock(AndroidProject.class);
                        when(project.getProductFlavors()).thenReturn(containers);
                        when(project.getDefaultConfig()).thenReturn(defaultContainer);
                        return project;
                    }

                    @Nullable
                    @Override
                    public Variant getCurrentVariant() {
                        List<String> productFlavorNames = Arrays.asList("free", "beta");
                        Variant mock = mock(Variant.class);
                        when(mock.getProductFlavors()).thenReturn(productFlavorNames);
                        return mock;
                    }
                };
            }
        };
    }

    private TestLintClient createClientForTestSplits() {

        // Set up a mock project model for the resource configuration test(s)
        // where we provide a subset of densities to be included

        return new TestLintClient() {
            @NonNull
            @Override
            protected Project createProject(@NonNull File dir, @NonNull File referenceDir) {
                return new Project(this, dir, referenceDir) {
                    @Override
                    public boolean isGradleProject() {
                        return true;
                    }

                    @Nullable
                    @Override
                    public AndroidProject getGradleProjectModel() {
                        /*
                            Simulate variant debug in this setup:
                            splits {
                                density {
                                    enable true
                                    reset()
                                    include "mdpi", "hdpi"
                                }
                            }
                         */

                        ProductFlavor defaultFlavor = mock(ProductFlavor.class);
                        when(defaultFlavor.getName()).thenReturn("main");
                        when(defaultFlavor.getResourceConfigurations()).thenReturn(
                                Collections.<String>emptyList());

                        ProductFlavorContainer defaultContainer =
                                mock(ProductFlavorContainer.class);
                        when(defaultContainer.getProductFlavor()).thenReturn(defaultFlavor);

                        AndroidProject project = mock(AndroidProject.class);
                        when(project.getProductFlavors()).thenReturn(
                                Collections.<ProductFlavorContainer>emptyList());
                        when(project.getDefaultConfig()).thenReturn(defaultContainer);
                        return project;
                    }

                    @Nullable
                    @Override
                    public Variant getCurrentVariant() {
                        Collection<AndroidArtifactOutput> outputs = Lists.newArrayList();

                        outputs.add(createAndroidArtifactOutput("", ""));
                        outputs.add(createAndroidArtifactOutput("DENSITY", "mdpi"));
                        outputs.add(createAndroidArtifactOutput("DENSITY", "hdpi"));

                        AndroidArtifact mainArtifact = mock(AndroidArtifact.class);
                        when(mainArtifact.getOutputs()).thenReturn(outputs);

                        List<String> productFlavorNames = Collections.emptyList();
                        Variant mock = mock(Variant.class);
                        when(mock.getProductFlavors()).thenReturn(productFlavorNames);
                        when(mock.getMainArtifact()).thenReturn(mainArtifact);
                        return mock;
                    }

                    private AndroidArtifactOutput createAndroidArtifactOutput(
                            @NonNull String filterType,
                            @NonNull String identifier) {
                        AndroidArtifactOutput artifactOutput = mock(
                                AndroidArtifactOutput.class);

                        OutputFile outputFile = mock(OutputFile.class);
                        if (filterType.isEmpty()) {
                            when(outputFile.getFilterTypes())
                                    .thenReturn(Collections.<String>emptyList());
                            when(outputFile.getFilters())
                                    .thenReturn(Collections.<FilterData>emptyList());
                        } else {
                            when(outputFile.getFilterTypes())
                                    .thenReturn(Collections.singletonList(filterType));
                            List<FilterData> filters = Lists.newArrayList();
                            FilterData filter = mock(FilterData.class);
                            when(filter.getFilterType()).thenReturn(filterType);
                            when(filter.getIdentifier()).thenReturn(identifier);
                            filters.add(filter);
                            when(outputFile.getFilters()).thenReturn(filters);
                        }

                        // Work around wildcard capture
                        //when(artifactOutput.getOutputs()).thenReturn(outputFiles);
                        List<OutputFile> outputFiles = Collections.singletonList(outputFile);
                        OngoingStubbing<? extends Collection<? extends OutputFile>> when = when(
                                artifactOutput.getOutputs());
                        //noinspection unchecked,RedundantCast
                        ((OngoingStubbing<Collection<? extends OutputFile>>) (OngoingStubbing<?>) when)
                                .thenReturn(outputFiles);

                        return artifactOutput;
                    }
                };
            }
        };
    }
}
