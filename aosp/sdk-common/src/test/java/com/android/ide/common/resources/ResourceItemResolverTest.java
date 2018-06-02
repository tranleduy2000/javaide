/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.ide.common.resources;

import com.android.annotations.Nullable;
import com.android.ide.common.rendering.api.ArrayResourceValue;
import com.android.ide.common.rendering.api.LayoutLog;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.res2.ResourceRepository;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.resources.ResourceType;
import com.google.common.collect.Lists;

import junit.framework.TestCase;

import java.util.List;
import java.util.Map;

public class ResourceItemResolverTest extends TestCase {
    @SuppressWarnings("ConstantConditions")
    public void test() throws Exception {
        final TestResourceRepository frameworkResources = TestResourceRepository.create(true,
                new Object[]{
                        "values/strings.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources>\n"
                        + "    <string name=\"ok\">Ok</string>\n"
                        + "    <array name=\"my_fw_array\">\"\n"
                        + "        <item>  fw_value1</item>\n"   // also test trimming.
                        + "        <item>fw_value2\n</item>\n"
                        + "        <item>fw_value3</item>\n"
                        + "    </array>\n"
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
                });

        final ResourceRepository appResources = TestResourceRepository.createRes2(false,
                new Object[]{
                        "layout/layout1.xml", "<!--contents doesn't matter-->",

                        "layout/layout2.xml", "<!--contents doesn't matter-->",

                        "layout-land/layout1.xml", "<!--contents doesn't matter-->",

                        "layout-land/only_land.xml", "<!--contents doesn't matter-->",

                        "drawable/graphic.9.png", new byte[0],

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
                        + "    </style>"
                        + "    <style name=\"RandomStyle2\" parent=\"RandomStyle\">\n"
                        + "    </style>"
                        + "</resources>\n",

                        "values/strings.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">\n"
                        + "    <item type=\"id\" name=\"action_bar_refresh\" />\n"
                        + "    <item type=\"dimen\" name=\"dialog_min_width_major\">45%</item>\n"
                        + "    <string name=\"home_title\">Home Sample</string>\n"
                        + "    <string name=\"show_all_apps\">All</string>\n"
                        + "    <string name=\"menu_wallpaper\">Wallpaper</string>\n"
                        + "    <string name=\"menu_search\">Search</string>\n"
                        + "    <string name=\"menu_settings\">Settings</string>\n"
                        + "    <string name=\"dummy\" translatable=\"false\">Ignore Me</string>\n"
                        + "    <string name=\"wallpaper_instructions\">Tap picture to set portrait wallpaper</string>\n"
                        + "    <string name=\"xliff_string\">First: <xliff:g id=\"firstName\">%1$s</xliff:g> Last: <xliff:g id=\"lastName\">%2$s</xliff:g></string>\n"
                        + "    <array name=\"my_array\">\"\n"
                        + "        <item>  value1</item>\n"   // also test trimming.
                        + "        <item>value2\n</item>\n"
                        + "        <item>value3</item>\n"
                        + "    </array>\n"
                        + "</resources>\n",

                        "values-es/strings.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources>\n"
                        + "    <string name=\"show_all_apps\">Todo</string>\n"
                        + "</resources>\n",
                });

        final FolderConfiguration config = FolderConfiguration.getConfigForFolder("values-es-land");
        assertFalse(appResources.isFramework());
        assertNotNull(config);

        final LayoutLog logger = new LayoutLog() {
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

        ResourceItemResolver.ResourceProvider provider = new ResourceItemResolver.ResourceProvider() {
            private ResourceResolver mResolver;

            @Nullable
            @Override
            public ResourceResolver getResolver(boolean createIfNecessary) {
                if (mResolver == null && createIfNecessary) {
                    Map<ResourceType, Map<String, ResourceValue>> appResourceMap;
                    appResourceMap = appResources.getConfiguredResources(config);
                    Map<ResourceType, Map<String, ResourceValue>> frameworkResourcesMap =
                            frameworkResources.getConfiguredResources(config);
                    assertNotNull(appResourceMap);
                    mResolver = ResourceResolver.create(appResourceMap, frameworkResourcesMap,
                            "MyTheme", true);
                    assertNotNull(mResolver);
                    mResolver.setLogger(logger);
                }

                return mResolver;
            }

            @Nullable
            @Override
            public com.android.ide.common.resources.ResourceRepository getFrameworkResources() {
                return frameworkResources;
            }

            @Nullable
            @Override
            public ResourceRepository getAppResources() {
                return appResources;
            }
        };

        ResourceItemResolver resolver = new ResourceItemResolver(config, provider, logger);

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

        // resolveResValue
        //    android:color/bright_foreground_dark => @android:color/background_light => white
        assertEquals("Todo", resolver.resolveResValue(
                resolver.findResValue("@string/show_all_apps", false)).getValue());
        assertEquals("#ffffffff", resolver.resolveResValue(
                resolver.findResValue("@android:color/bright_foreground_dark", false)).getValue());

        // Test array values.
        ResourceValue resValue = resolver.findResValue("@array/my_array", false);
        assertTrue(resValue instanceof ArrayResourceValue);
        assertEquals(3, ((ArrayResourceValue) resValue).getElementCount());
        assertEquals("value1", ((ArrayResourceValue) resValue).getElement(0));
        assertEquals("value2", ((ArrayResourceValue) resValue).getElement(1));
        assertEquals("value3", ((ArrayResourceValue) resValue).getElement(2));
        resValue = resolver.findResValue("@android:array/my_fw_array", false);
        assertTrue(resValue instanceof ArrayResourceValue);
        assertEquals(3, ((ArrayResourceValue) resValue).getElementCount());
        assertEquals("fw_value1", ((ArrayResourceValue) resValue).getElement(0));
        assertEquals("fw_value2", ((ArrayResourceValue) resValue).getElement(1));
        assertEquals("fw_value3", ((ArrayResourceValue) resValue).getElement(2));


        // Now do everything over again, but this time without a resource resolver.
        // Also set a lookup chain.
        resolver = new ResourceItemResolver(config, frameworkResources, appResources,
                logger);
        List<ResourceValue> chain = Lists.newArrayList();
        resolver.setLookupChainList(chain);

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
        assertEquals("Todo", resolver.resolveResValue(
                resolver.findResValue("@string/show_all_apps", false)).getValue());

        chain.clear();
        ResourceValue v = resolver.findResValue("@android:color/bright_foreground_dark", false);
        assertEquals("@android:color/bright_foreground_dark => @android:color/background_light",
                ResourceItemResolver.getDisplayString(ResourceType.COLOR, "bright_foreground_dark",
                        true, chain));
        assertEquals("First: ${firstName} Last: ${lastName}",
                resolver.findResValue("@string/xliff_string", false).getValue());
        assertEquals("First: <xliff:g id=\"firstName\">%1$s</xliff:g> Last: <xliff:g id=\"lastName\">%2$s</xliff:g>",
                resolver.findResValue("@string/xliff_string", false).getRawXmlValue());

        chain.clear();
        assertEquals("#ffffffff", resolver.resolveResValue(v).getValue());
        assertEquals("@android:color/bright_foreground_dark => @android:color/background_light "
                + "=> #ffffffff",
                ResourceItemResolver.getDisplayString("@android:color/bright_foreground_dark",
                        chain));

        // Try to resolve style attributes
        resolver = new ResourceItemResolver(config, provider, logger);
        resolver.setLookupChainList(chain);
        chain.clear();
        ResourceValue target = new ResourceValue(ResourceType.STRING, "dummy", false);
        target.setValue("?foo");
        assertEquals("#ff000000", resolver.resolveResValue(target).getValue());
        assertEquals("?foo => ?android:colorForeground => @color/bright_foreground_light => " 
                + "@android:color/background_dark => #ff000000",
                ResourceItemResolver.getDisplayString("?foo", chain));

        // Test array values.
        resValue = resolver.findResValue("@array/my_array", false);
        assertTrue(resValue instanceof ArrayResourceValue);
        assertEquals(3, ((ArrayResourceValue) resValue).getElementCount());
        assertEquals("value1", ((ArrayResourceValue) resValue).getElement(0));
        assertEquals("value2", ((ArrayResourceValue) resValue).getElement(1));
        assertEquals("value3", ((ArrayResourceValue) resValue).getElement(2));
        resValue = resolver.findResValue("@android:array/my_fw_array", false);
        assertTrue(resValue instanceof ArrayResourceValue);
        assertEquals(3, ((ArrayResourceValue) resValue).getElementCount());
        assertEquals("fw_value1", ((ArrayResourceValue) resValue).getElement(0));
        assertEquals("fw_value2", ((ArrayResourceValue) resValue).getElement(1));
        assertEquals("fw_value3", ((ArrayResourceValue) resValue).getElement(2));

    }
}
