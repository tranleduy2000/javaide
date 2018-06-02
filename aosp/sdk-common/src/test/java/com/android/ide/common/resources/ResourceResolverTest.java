package com.android.ide.common.resources;

import com.android.ide.common.rendering.api.DensityBasedResourceValue;
import com.android.ide.common.rendering.api.LayoutLog;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.rendering.api.StyleResourceValue;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.resources.Density;
import com.android.resources.ResourceType;
import com.google.common.collect.Lists;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResourceResolverTest extends TestCase {
    public void test() throws Exception {
        TestResourceRepository frameworkRepository = TestResourceRepository.create(true,
                new Object[]{
                        "values/strings.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources>\n"
                        + "    <string name=\"ok\">Ok</string>\n"
                        + "</resources>\n",

                        "values/themes.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources>\n"
                        + "    <style name=\"Theme\">\n"
                        + "        <item name=\"colorForeground\">@android:color/bright_foreground_dark</item>\n"
                        + "        <item name=\"colorBackground\">@android:color/background_dark</item>\n"
                        + "    </style>\n"
                        + "    <style name=\"Theme.Light\">\n"
                        + "        <item name=\"colorBackground\">@android:color/background_light</item>\n"
                        + "        <item name=\"colorForeground\">@color/bright_foreground_light</item>\n"
                        + "    </style>\n"
                        + "</resources>\n",

                        "values/colors.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources>\n"
                        + "    <color name=\"background_dark\">#ff000000</color>\n"
                        + "    <color name=\"background_light\">#ffffffff</color>\n"
                        + "    <color name=\"bright_foreground_dark\">@android:color/background_light</color>\n"
                        + "    <color name=\"bright_foreground_light\">@android:color/background_dark</color>\n"
                        + "</resources>\n",

                        "values/ids.xml",  ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources>\n"
                        + "    <item name=\"some_framework_id\" type=\"id\" />\n"
                        + "</resources>\n",
                });

        TestResourceRepository projectRepository = TestResourceRepository.create(false,
                new Object[]{
                        "layout/layout1.xml", "<!--contents doesn't matter-->",

                        "layout/layout2.xml", "<!--contents doesn't matter-->",

                        "layout-land/layout1.xml", "<!--contents doesn't matter-->",

                        "layout-land/onlyLand.xml", "<!--contents doesn't matter-->",

                        "drawable/graphic.9.png", new byte[0],

                        "mipmap-xhdpi/ic_launcher.png", new byte[0],

                        "values/styles.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources>\n"
                        + "    <style name=\"MyTheme\" parent=\"android:Theme.Light\">\n"
                        + "        <item name=\"android:textColor\">#999999</item>\n"
                        + "        <item name=\"foo\">?android:colorForeground</item>\n"
                        + "    </style>\n"
                        + "    <style name=\"MyTheme.Dotted1\" parent=\"\">\n"
                        + "    </style>"
                        + "    <style name=\"MyTheme.Dotted2\">\n"
                        + "    </style>"
                        + "    <style name=\"RandomStyle\">\n"
                        + "        <item name=\"android:text\">&#169; Copyright</item>\n"
                        + "    </style>"
                        + "    <style name=\"RandomStyle2\" parent=\"RandomStyle\">\n"
                        + "    </style>"
                        + "    <style name=\"Theme.FakeTheme\" parent=\"\">\n"
                        + "    </style>"
                        + "    <style name=\"Theme\" parent=\"RandomStyle\">\n"
                        + "    </style>"
                        + "</resources>\n",

                        "values/strings.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources>\n"
                        + "    <item type=\"id\" name=\"action_bar_refresh\" />\n"
                        + "    <item type=\"dimen\" name=\"dialog_min_width_major\">45%</item>\n"
                        + "    <string name=\"home_title\">Home Sample</string>\n"
                        + "    <string name=\"show_all_apps\">All</string>\n"
                        + "    <string name=\"menu_wallpaper\">Wallpaper</string>\n"
                        + "    <string name=\"menu_search\">Search</string>\n"
                        + "    <string name=\"menu_settings\">Settings</string>\n"
                        + "    <string name=\"dummy\" translatable=\"false\">Ignore Me</string>\n"
                        + "    <string name=\"wallpaper_instructions\">Tap picture to set portrait wallpaper</string>\n"
                        + "</resources>\n",

                        "values-es/strings.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources>\n"
                        + "    <string name=\"show_all_apps\">Todo</string>\n"
                        + "</resources>\n",
                });

        assertFalse(projectRepository.isFrameworkRepository());
        FolderConfiguration config = FolderConfiguration.getConfigForFolder("values-es-land");
        assertNotNull(config);
        Map<ResourceType, Map<String, ResourceValue>> projectResources =
                projectRepository.getConfiguredResources(config);
        Map<ResourceType, Map<String, ResourceValue>> frameworkResources =
                frameworkRepository.getConfiguredResources(config);
        assertNotNull(projectResources);
        ResourceResolver resolver = ResourceResolver.create(projectResources, frameworkResources,
                "MyTheme", true);
        assertNotNull(resolver);

        LayoutLog logger = new LayoutLog() {
            @Override
            public void warning(String tag, String message, Object data) {
                fail(message);
            }

            @Override
            public void fidelityWarning(String tag, String message, Throwable throwable,
                    Object data) {
                fail(message);
            }

            @Override
            public void error(String tag, String message, Object data) {
                fail(message);
            }

            @Override
            public void error(String tag, String message, Throwable throwable, Object data) {
                fail(message);
            }
        };
        resolver.setLogger(logger);

        assertEquals("MyTheme", resolver.getThemeName());
        assertTrue(resolver.isProjectTheme());

        // findResValue
        assertNotNull(resolver.findResValue("@string/show_all_apps", false));
        assertNotNull(resolver.findResValue("@android:string/ok", false));
        assertNotNull(resolver.findResValue("@android:string/ok", true));
        assertEquals("Todo", resolver.findResValue("@string/show_all_apps", false).getValue());
        assertEquals("Home Sample", resolver.findResValue("@string/home_title", false).getValue());
        assertEquals("45%", resolver.findResValue("@dimen/dialog_min_width_major",
                false).getValue());
        assertNotNull(resolver.findResValue("@android:color/bright_foreground_dark", true));
        assertEquals("@android:color/background_light",
                resolver.findResValue("@android:color/bright_foreground_dark", true).getValue());
        assertEquals("#ffffffff",
                resolver.findResValue("@android:color/background_light", true).getValue());
        assertNull(resolver.findResValue("?attr/non_existent_style", false)); // shouldn't log an error.
        assertEquals(Density.XHIGH,
                ((DensityBasedResourceValue) resolver.findResValue("@mipmap/ic_launcher", false))
                        .getResourceDensity());  // also ensures that returned value is instance of DensityBasedResourceValue

        // getTheme
        StyleResourceValue myTheme = resolver.getTheme("MyTheme", false);
        assertNotNull(myTheme);
        assertSame(resolver.findResValue("@style/MyTheme", false), myTheme);
        assertNull(resolver.getTheme("MyTheme", true));
        assertNull(resolver.getTheme("MyNonexistentTheme", true));
        StyleResourceValue themeLight = resolver.getTheme("Theme.Light", true);
        assertNotNull(themeLight);
        StyleResourceValue theme = resolver.getTheme("Theme", true);
        assertNotNull(theme);

        // getParent
        StyleResourceValue parent = resolver.getParent(myTheme);
        assertNotNull(parent);
        assertEquals("Theme.Light", parent.getName());

        // themeIsParentOf
        assertTrue(resolver.themeIsParentOf(themeLight, myTheme));
        assertFalse(resolver.themeIsParentOf(myTheme, themeLight));
        assertTrue(resolver.themeIsParentOf(theme, themeLight));
        assertFalse(resolver.themeIsParentOf(themeLight, theme));
        assertTrue(resolver.themeIsParentOf(theme, myTheme));
        assertFalse(resolver.themeIsParentOf(myTheme, theme));
        StyleResourceValue dotted1 = resolver.getTheme("MyTheme.Dotted1", false);
        assertNotNull(dotted1);
        StyleResourceValue dotted2 = resolver.getTheme("MyTheme.Dotted2", false);
        assertNotNull(dotted2);
        assertTrue(resolver.themeIsParentOf(myTheme, dotted2));
        assertFalse(resolver.themeIsParentOf(myTheme, dotted1)); // because parent=""

        // isTheme
        assertFalse(resolver.isTheme(resolver.findResValue("@style/RandomStyle", false), null));
        assertFalse(resolver.isTheme(resolver.findResValue("@style/RandomStyle2", false), null));
        assertFalse(resolver.isTheme(resolver.findResValue("@style/Theme.FakeTheme", false), null));
        assertFalse(resolver.isTheme(resolver.findResValue("@style/Theme", false), null));
        //    check XML escaping in value resources
        StyleResourceValue randomStyle = (StyleResourceValue) resolver.findResValue(
                "@style/RandomStyle", false);
        assertEquals("\u00a9 Copyright", randomStyle.getItem("text", true).getValue());
        assertTrue(resolver.isTheme(resolver.findResValue("@style/MyTheme.Dotted2", false), null));
        assertFalse(resolver.isTheme(resolver.findResValue("@style/MyTheme.Dotted1", false),
                null));
        assertTrue(resolver.isTheme(resolver.findResValue("@style/MyTheme", false), null));
        assertTrue(resolver.isTheme(resolver.findResValue("@android:style/Theme.Light", false),
                null));
        assertTrue(resolver.isTheme(resolver.findResValue("@android:style/Theme", false), null));

        // findItemInStyle
        assertNotNull(resolver.findItemInStyle(myTheme, "colorForeground", true));
        assertEquals("@color/bright_foreground_light",
                resolver.findItemInStyle(myTheme, "colorForeground", true).getValue());
        assertNotNull(resolver.findItemInStyle(dotted2, "colorForeground", true));
        assertNull(resolver.findItemInStyle(dotted1, "colorForeground", true));

        // findItemInTheme
        assertNotNull(resolver.findItemInTheme("colorForeground", true));
        assertEquals("@color/bright_foreground_light",
                resolver.findItemInTheme("colorForeground", true).getValue());
        assertEquals("@color/bright_foreground_light",
                resolver.findResValue("?colorForeground", true).getValue());
        ResourceValue target = new ResourceValue(ResourceType.STRING, "dummy", false);
        target.setValue("?foo");
        assertEquals("#ff000000", resolver.resolveResValue(target).getValue());

        // getFrameworkResource
        assertNull(resolver.getFrameworkResource(ResourceType.STRING, "show_all_apps"));
        assertNotNull(resolver.getFrameworkResource(ResourceType.STRING, "ok"));
        assertEquals("Ok", resolver.getFrameworkResource(ResourceType.STRING, "ok").getValue());

        // getProjectResource
        assertNull(resolver.getProjectResource(ResourceType.STRING, "ok"));
        assertNotNull(resolver.getProjectResource(ResourceType.STRING, "show_all_apps"));
        assertEquals("Todo", resolver.getProjectResource(ResourceType.STRING,
                "show_all_apps").getValue());


        // resolveResValue
        //    android:color/bright_foreground_dark => @android:color/background_light => white
        assertEquals("Todo", resolver.resolveResValue(
                resolver.findResValue("@string/show_all_apps", false)).getValue());
        assertEquals("#ffffffff", resolver.resolveResValue(
                resolver.findResValue("@android:color/bright_foreground_dark", false)).getValue());

        // resolveValue
        assertEquals("#ffffffff",
                resolver.resolveValue(ResourceType.STRING, "bright_foreground_dark",
                        "@android:color/background_light", true).getValue());
        assertFalse(resolver.resolveValue(null, "id", "@+id/some_framework_id", false)
                .isFramework());
        // error expected.
        boolean failed = false;
        ResourceValue val = null;
        try {
            val = resolver.resolveValue(ResourceType.STRING, "bright_foreground_dark",
                    "@color/background_light", false);
        } catch (AssertionError expected) {
            failed = true;
        }
        assertTrue("incorrect resource returned: " + val, failed);

        // themeExtends
        assertTrue(resolver.themeExtends("@android:style/Theme", "@android:style/Theme"));
        assertTrue(resolver.themeExtends("@android:style/Theme", "@android:style/Theme.Light"));
        assertFalse(resolver.themeExtends("@android:style/Theme.Light", "@android:style/Theme"));
        assertTrue(resolver.themeExtends("@style/MyTheme.Dotted2", "@style/MyTheme.Dotted2"));
        assertTrue(resolver.themeExtends("@style/MyTheme", "@style/MyTheme.Dotted2"));
        assertTrue(resolver.themeExtends("@android:style/Theme.Light", "@style/MyTheme.Dotted2"));
        assertTrue(resolver.themeExtends("@android:style/Theme", "@style/MyTheme.Dotted2"));
        assertFalse(resolver.themeExtends("@style/MyTheme.Dotted1", "@style/MyTheme.Dotted2"));

        // Switch to MyTheme.Dotted1 (to make sure the parent="" inheritance works properly.)
        // To do that we need to create a new resource resolver.
        resolver = ResourceResolver.create(projectResources, frameworkResources,
                "MyTheme.Dotted1", true);
        resolver.setLogger(logger);
        assertNotNull(resolver);
        assertEquals("MyTheme.Dotted1", resolver.getThemeName());
        assertTrue(resolver.isProjectTheme());
        assertNull(resolver.findItemInTheme("colorForeground", true));

        resolver = ResourceResolver.create(projectResources, frameworkResources,
                "MyTheme.Dotted2", true);
        resolver.setLogger(logger);
        assertNotNull(resolver);
        assertEquals("MyTheme.Dotted2", resolver.getThemeName());
        assertTrue(resolver.isProjectTheme());
        assertNotNull(resolver.findItemInTheme("colorForeground", true));

        // Test recording resolver
        List<ResourceValue> chain = Lists.newArrayList();
        resolver = ResourceResolver.create(projectResources, frameworkResources, "MyTheme", true);
        resolver = resolver.createRecorder(chain);
        assertNotNull(resolver.findResValue("@android:color/bright_foreground_dark", true));
        ResourceValue v = resolver.findResValue("@android:color/bright_foreground_dark", false);
        chain.clear();
        assertEquals("#ffffffff", resolver.resolveResValue(v).getValue());
        assertEquals("@android:color/bright_foreground_dark => "
                + "@android:color/background_light => #ffffffff",
                ResourceItemResolver.getDisplayString("@android:color/bright_foreground_dark",
                        chain));

        frameworkRepository.dispose();
        projectRepository.dispose();
    }

    public void testMissingMessage() throws Exception {
        TestResourceRepository projectRepository = TestResourceRepository.create(false,
                new Object[]{
                        "values/colors.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources>\n"
                        + "    <color name=\"loop1\">@color/loop1</color>\n"
                        + "    <color name=\"loop2a\">@color/loop2b</color>\n"
                        + "    <color name=\"loop2b\">@color/loop2a</color>\n"
                        + "</resources>\n",

                });

        assertFalse(projectRepository.isFrameworkRepository());
        FolderConfiguration config = FolderConfiguration.getConfigForFolder("values-es-land");
        assertNotNull(config);
        Map<ResourceType, Map<String, ResourceValue>> projectResources =
                projectRepository.getConfiguredResources(config);
        assertNotNull(projectResources);
        ResourceResolver resolver = ResourceResolver.create(projectResources, projectResources,
                "MyTheme", true);
        final AtomicBoolean wasWarned = new AtomicBoolean(false);
        LayoutLog logger = new LayoutLog() {
            @Override
            public void warning(String tag, String message, Object data) {
                if ("Couldn't resolve resource @android:string/show_all_apps".equals(message)) {
                    wasWarned.set(true);
                } else {
                    fail(message);
                }
            }
        };
        resolver.setLogger(logger);
        assertNull(resolver.findResValue("@string/show_all_apps", true));
        assertTrue(wasWarned.get());
        projectRepository.dispose();
    }

    public void testLoop() throws Exception {
        TestResourceRepository projectRepository = TestResourceRepository.create(false,
                new Object[]{
                        "values/colors.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources>\n"
                        + "    <color name=\"loop1\">@color/loop1</color>\n"
                        + "    <color name=\"loop2a\">@color/loop2b</color>\n"
                        + "    <color name=\"loop2b\">@color/loop2a</color>\n"
                        + "</resources>\n",

                });

        assertFalse(projectRepository.isFrameworkRepository());
        FolderConfiguration config = FolderConfiguration.getConfigForFolder("values-es-land");
        assertNotNull(config);
        Map<ResourceType, Map<String, ResourceValue>> projectResources =
                projectRepository.getConfiguredResources(config);
        assertNotNull(projectResources);
        ResourceResolver resolver = ResourceResolver.create(projectResources, projectResources,
                "MyTheme", true);
        assertNotNull(resolver);

        final AtomicBoolean wasWarned = new AtomicBoolean(false);
        LayoutLog logger = new LayoutLog() {
            @Override
            public void error(String tag, String message, Object data) {
                if (("Potential stack overflow trying to resolve "
                        + "'@color/loop1': cyclic resource definitions?"
                        + " Render may not be accurate.").equals(message)) {
                    wasWarned.set(true);
                } else if (("Potential stack overflow trying to resolve "
                        + "'@color/loop2b': cyclic resource definitions? "
                        + "Render may not be accurate.").equals(message)) {
                    wasWarned.set(true);
                } else {
                    fail(message);
                }
            }
        };
        resolver.setLogger(logger);

        assertNotNull(resolver.findResValue("@color/loop1", false));
        resolver.resolveResValue(resolver.findResValue("@color/loop1", false));
        assertTrue(wasWarned.get());

        wasWarned.set(false);
        assertNotNull(resolver.findResValue("@color/loop2a", false));
        resolver.resolveResValue(resolver.findResValue("@color/loop2a", false));
        assertTrue(wasWarned.get());

        projectRepository.dispose();
    }

    public void testParentCycle() throws IOException {
        TestResourceRepository projectRepository = TestResourceRepository.create(false,
                new Object[]{
                        "values/styles.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources>\n"
                        + "    <style name=\"ButtonStyle.Base\">\n"
                        + "        <item name=\"android:textColor\">#ff0000</item>\n"
                        + "    </style>\n"
                        + "    <style name=\"ButtonStyle\" parent=\"ButtonStyle.Base\">\n"
                        + "        <item name=\"android:layout_height\">40dp</item>\n"
                        + "    </style>\n"
                        + "</resources>\n",

                        "layouts/layout.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<RelativeLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                        + "    android:layout_width=\"match_parent\"\n"
                        + "    android:layout_height=\"match_parent\">\n"
                        + "\n"
                        + "    <TextView\n"
                        + "        style=\"@style/ButtonStyle\"\n"
                        + "        android:layout_width=\"wrap_content\"\n"
                        + "        android:layout_height=\"wrap_content\" />\n"
                        + "\n"
                        + "</RelativeLayout>\n",

                });
        assertFalse(projectRepository.isFrameworkRepository());
        FolderConfiguration config = FolderConfiguration.getConfigForFolder("values-es-land");
        assertNotNull(config);
        Map<ResourceType, Map<String, ResourceValue>> projectResources =
                projectRepository.getConfiguredResources(config);
        assertNotNull(projectResources);
        ResourceResolver resolver = ResourceResolver.create(projectResources, projectResources,
                "ButtonStyle", true);
        assertNotNull(resolver);

        final AtomicBoolean wasWarned = new AtomicBoolean(false);
        LayoutLog logger = new LayoutLog() {
            @Override
            public void error(String tag, String message, Object data) {
                assertEquals("Cyclic style parent definitions: \"ButtonStyle\" specifies "
                        + "parent \"ButtonStyle.Base\" implies parent \"ButtonStyle\"", message);
                assertEquals(LayoutLog.TAG_BROKEN, tag);
                wasWarned.set(true);
            }
        };
        resolver.setLogger(logger);

        StyleResourceValue buttonStyle = (StyleResourceValue) resolver.findResValue(
                "@style/ButtonStyle", false);
        ResourceValue textColor = resolver.findItemInStyle(buttonStyle, "textColor", true);
        assertNotNull(textColor);
        assertEquals("#ff0000", textColor.getValue());
        assertFalse(wasWarned.get());
        ResourceValue missing = resolver.findItemInStyle(buttonStyle, "missing", true);
        assertNull(missing);
        assertTrue(wasWarned.get());

        projectRepository.dispose();
    }

    public void testSetDeviceDefaults() throws Exception {
        TestResourceRepository frameworkRepository = TestResourceRepository.create(true,
            new Object[] {
                "values/themes.xml", ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources>\n"
                + "    <style name=\"Theme.Light\" parent=\"\">\n"
                + "         <item name=\"android:textColor\">#ff0000</item>\n"
                + "    </style>\n"
                + "    <style name=\"Theme.Holo.Light\" parent=\"Theme.Light\">\n"
                + "         <item name=\"android:textColor\">#00ff00</item>\n"
                + "    </style>\n"
                + "    <style name=\"Theme.DeviceDefault.Light\" parent=\"Theme.Holo.Light\"/>\n"
                + "    <style name=\"Theme\" parent=\"\">\n"
                + "         <item name=\"android:textColor\">#000000</item>\n"
                + "    </style>\n"
                + "    <style name=\"Theme.Holo\" parent=\"Theme\">\n"
                + "         <item name=\"android:textColor\">#0000ff</item>\n"
                + "    </style>\n"
                + "    <style name=\"Theme.DeviceDefault\" parent=\"Theme.Holo\"/>\n"
                + "</resources>\n",
        });

        TestResourceRepository projectRepository = TestResourceRepository.create(false,
            new Object[] {
                "values/themes.xml", ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources>\n"
                + "    <style name=\"AppTheme\" parent=\"android:Theme.DeviceDefault.Light\"/>\n"
                + "    <style name=\"AppTheme.Dark\" parent=\"android:Theme.DeviceDefault\"/>\n"
                + "</resources>\n"
        });

        assertFalse(projectRepository.isFrameworkRepository());
        FolderConfiguration config = FolderConfiguration.getConfigForFolder("values-es-land");
        assertNotNull(config);
        Map<ResourceType, Map<String, ResourceValue>> projectResources = projectRepository
                .getConfiguredResources(config);
        Map<ResourceType, Map<String, ResourceValue>> frameworkResources = frameworkRepository
                .getConfiguredResources(config);
        assertNotNull(projectResources);
        ResourceResolver lightResolver = ResourceResolver.create(projectResources,
                frameworkResources, "AppTheme", true);
        assertNotNull(lightResolver);
        ResourceValue textColor = lightResolver.findItemInTheme("textColor", true);
        assertNotNull(textColor);
        assertEquals("#00ff00", textColor.getValue());

        lightResolver.setDeviceDefaults("Theme.Light", null);
        textColor = lightResolver.findItemInTheme("textColor", true);
        assertNotNull(textColor);
        assertEquals("#ff0000", textColor.getValue());

        ResourceResolver darkResolver = ResourceResolver.create(projectResources,
                frameworkResources, "AppTheme.Dark", true);
        assertNotNull(darkResolver);
        textColor = darkResolver.findItemInTheme("textColor", true);
        assertNotNull(textColor);
        assertEquals("#0000ff", textColor.getValue());

        darkResolver.setDeviceDefaults("Theme.Light", "Theme");
        textColor = darkResolver.findItemInTheme("textColor", true);
        assertNotNull(textColor);
        assertEquals("#000000", textColor.getValue());
    }

    public void testCycle() throws Exception {
        TestResourceRepository frameworkRepository = TestResourceRepository.create(true,
                new Object[] {
                        "values/themes.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources>\n"
                        + "    <style name=\"Theme.DeviceDefault.Light\"/>\n"
                        + "</resources>\n",
                });

        TestResourceRepository projectRepository = TestResourceRepository.create(false,
                new Object[] {
                        "values/themes.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources>\n"
                        + "    <style name=\"AppTheme\" parent=\"android:Theme.DeviceDefault.Light\"/>\n"
                        + "    <style name=\"AppTheme.Dark\" parent=\"android:Theme.DeviceDefault\"/>\n"
                        + "    <style name=\"foo\" parent=\"bar\"/>\n"
                        + "    <style name=\"bar\" parent=\"foo\"/>\n"
                        + "</resources>\n"
                });

        assertFalse(projectRepository.isFrameworkRepository());
        FolderConfiguration config = FolderConfiguration.getConfigForFolder("values");
        assertNotNull(config);
        Map<ResourceType, Map<String, ResourceValue>> projectResources = projectRepository
                .getConfiguredResources(config);
        Map<ResourceType, Map<String, ResourceValue>> frameworkResources = frameworkRepository
                .getConfiguredResources(config);
        assertNotNull(projectResources);
        ResourceResolver resolver = ResourceResolver.create(projectResources,
                frameworkResources, "AppTheme", true);

        final AtomicBoolean wasWarned = new AtomicBoolean(false);
        LayoutLog logger = new LayoutLog() {
            @Override
            public void error(String tag, String message, Object data) {
                if ("Cyclic style parent definitions: \"foo\" specifies parent \"bar\" specifies parent \"foo\"".equals(message)) {
                    wasWarned.set(true);
                } else {
                    fail(message);
                }
            }
        };
        resolver.setLogger(logger);
        assertFalse(resolver.isTheme(resolver.findResValue("@style/foo", false), null));
        assertTrue(wasWarned.get());

        projectRepository.dispose();

    }
}
