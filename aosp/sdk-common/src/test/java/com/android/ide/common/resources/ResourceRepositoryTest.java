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
package com.android.ide.common.resources;

import static com.android.SdkConstants.FD_RES;
import static com.android.SdkConstants.FD_RES_DRAWABLE;
import static com.android.SdkConstants.FD_RES_LAYOUT;

import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.ide.common.resources.configuration.LocaleQualifier;
import com.android.ide.common.resources.configuration.ScreenOrientationQualifier;
import com.android.io.FileWrapper;
import com.android.io.IAbstractFile;
import com.android.io.IAbstractFolder;
import com.android.resources.ResourceFolderType;
import com.android.resources.ResourceType;
import com.android.resources.ScreenOrientation;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import junit.framework.TestCase;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

@SuppressWarnings("javadoc")
public class ResourceRepositoryTest extends TestCase {
    private TestResourceRepository mRepository;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mRepository = TestResourceRepository.create(false, new Object[]{
                "layout/layout1.xml", "<!--contents doesn't matter-->",
                "layout/layout2.xml", "<!--contents doesn't matter-->",
                "layout-land/layout1.xml", "<!--contents doesn't matter-->",
                "layout-land/onlyLand.xml", "<!--contents doesn't matter-->",
                "drawable/graphic.9.png", new byte[0],
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
        assertFalse(mRepository.isFrameworkRepository());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mRepository.dispose();
    }

    public void testBasic() throws Exception {
        assertFalse(mRepository.hasResourceItem("@layout/layout0"));
        assertTrue(mRepository.hasResourceItem("@layout/layout1"));
        assertFalse(mRepository.hasResourceItem(ResourceType.LAYOUT, "layout0"));
        assertTrue(mRepository.hasResourceItem(ResourceType.LAYOUT, "layout1"));
        assertFalse(mRepository.hasResourceItem(ResourceType.STRING, "layout1"));
        assertTrue(mRepository.hasResourceItem(ResourceType.STRING, "home_title"));
        assertFalse(mRepository.hasResourceItem(ResourceType.STRING, "home_title2"));
        assertFalse(mRepository.hasResourceItem(ResourceType.DRAWABLE, "graph"));
        assertTrue(mRepository.hasResourceItem(ResourceType.DRAWABLE, "graphic"));
        assertTrue(mRepository.hasResourceItem("@id/action_bar_refresh"));
        assertTrue(mRepository.hasResourceItem("@drawable/graphic"));
        assertTrue(mRepository.hasResourcesOfType(ResourceType.DRAWABLE));
        assertFalse(mRepository.hasResourcesOfType(ResourceType.ANIM));

        List<ResourceType> availableResourceTypes = mRepository.getAvailableResourceTypes();
        assertEquals(5, availableResourceTypes.size()); // layout, string, drawable, id, dimen

        Collection<ResourceItem> allStrings =
                mRepository.getResourceItemsOfType(ResourceType.STRING);
        assertEquals(7, allStrings.size());
        ResourceItem item = mRepository.getResourceItem(ResourceType.STRING, "menu_settings");
        assertNotNull(item);
        assertEquals("menu_settings", item.getName());
        assertEquals("@string/menu_settings", item.getXmlString(ResourceType.STRING, false));
        assertTrue(item.hasDefault());
        assertFalse(item.hasAlternates());

        item = mRepository.getResourceItem(ResourceType.STRING, "show_all_apps");
        assertNotNull(item);
        assertEquals("show_all_apps", item.getName());
        assertEquals("@string/show_all_apps", item.getXmlString(ResourceType.STRING, false));
        assertTrue(item.hasDefault());
        assertTrue(item.hasAlternates());
        FolderConfiguration folderConfig = new FolderConfiguration();
        folderConfig.setLocaleQualifier(LocaleQualifier.getQualifier("en"));
        ResourceValue value =
                item.getResourceValue(ResourceType.STRING, folderConfig, false);
        assertNotNull(value);
        assertEquals("All", value.getValue());
        assertSame(ResourceType.STRING, value.getResourceType());

        folderConfig = new FolderConfiguration();
        folderConfig.setLocaleQualifier(LocaleQualifier.getQualifier("es"));
        value = item.getResourceValue(ResourceType.STRING, folderConfig, false);
        assertNotNull(value);
        assertEquals("Todo", value.getValue());
        assertSame(ResourceType.STRING, value.getResourceType());

        item = mRepository.getResourceItem(ResourceType.LAYOUT, "onlyLand");
        assertNotNull(item);
        assertFalse(item.hasDefault());
        assertEquals(1, item.getSourceFileList().size());
        ResourceFile resourceFile = item.getSourceFileList().get(0);
        assertEquals("onlyLand.xml", resourceFile.getFile().getName());
        assertEquals(ScreenOrientation.LANDSCAPE,
                resourceFile.getConfiguration().getScreenOrientationQualifier().getValue());

        item = mRepository.getResourceItem(ResourceType.LAYOUT, "layout1");
        assertNotNull(item);
        assertTrue(item.hasDefault());
        assertTrue(item.hasAlternates());
        assertFalse(item.hasNoSourceFile());
        assertEquals(2, item.getSourceFileList().size());
        assertEquals(1, item.getAlternateCount());
        assertFalse(item.isDeclaredInline());

        SortedSet<String> languages = mRepository.getLanguages();
        assertEquals(1, languages.size());
        assertTrue(languages.contains("es"));
        assertTrue(mRepository.getRegions("es").isEmpty());

        List<ResourceFile> layouts = mRepository.getSourceFiles(ResourceType.LAYOUT, "layout1",
                folderConfig);
        assertNotNull(layouts);
        assertEquals(1, layouts.size());
        ResourceFile file1 = layouts.get(0);
        assertEquals("layout1.xml", file1.getFile().getName());
        assertSame(mRepository, file1.getRepository());
    }

    public void testGetConfiguredResources() throws Exception {
        FolderConfiguration folderConfig = new FolderConfiguration();
        folderConfig.setLocaleQualifier(LocaleQualifier.getQualifier("es"));
        folderConfig.setScreenOrientationQualifier(
                new ScreenOrientationQualifier(ScreenOrientation.LANDSCAPE));

        Map<ResourceType, Map<String, ResourceValue>> configuredResources =
                mRepository.getConfiguredResources(folderConfig);
        Map<String, ResourceValue> strings = configuredResources
                .get(ResourceType.STRING);
        Map<String, ResourceValue> layouts = configuredResources
                .get(ResourceType.LAYOUT);
        Map<String, ResourceValue> ids = configuredResources
                .get(ResourceType.ID);
        Map<String, ResourceValue> dimens = configuredResources
                .get(ResourceType.DIMEN);
        assertEquals(1, ids.size());
        assertEquals(1, dimens.size());
        assertEquals("dialog_min_width_major", dimens.get("dialog_min_width_major").getName());
        assertEquals("45%", dimens.get("dialog_min_width_major").getValue());
        assertEquals("Todo", strings.get("show_all_apps").getValue());
        assertEquals(3, layouts.size());
        assertNotNull(layouts.get("layout1"));

        ResourceFile file = mRepository.getMatchingFile("dialog_min_width_major",
                ResourceType.DIMEN, folderConfig);
        assertNotNull(file);
        file = mRepository.getMatchingFile("dialog_min_width_major", ResourceFolderType.VALUES,
                folderConfig);
        assertNotNull(file);
        file = mRepository.getMatchingFile("layout1", ResourceFolderType.LAYOUT, folderConfig);
        assertNotNull(file);
        file = mRepository.getMatchingFile("layout1", ResourceType.LAYOUT, folderConfig);
        assertNotNull(file);
    }

    public void testUpdates() throws Exception {
        assertFalse(mRepository.hasResourcesOfType(ResourceType.ANIM));
        assertFalse(mRepository.hasResourcesOfType(ResourceType.MENU));
        assertFalse(mRepository.hasResourcesOfType(ResourceType.BOOL));

        assertTrue(mRepository.hasResourcesOfType(ResourceType.DRAWABLE));
        assertTrue(mRepository.hasResourceItem("@drawable/graphic"));

        // Delete the drawable graphic
        IAbstractFolder drawableFolder = mRepository.getResFolder()
                .getFolder(FD_RES_DRAWABLE);
        assertNotNull(drawableFolder);
        IAbstractFile graphicFile = drawableFolder.getFile("graphic.9.png");
        assertNotNull(graphicFile);
        assertTrue(graphicFile instanceof FileWrapper);
        FileWrapper fileWrapper = (FileWrapper) graphicFile;
        if (fileWrapper.exists()) {
            boolean deleted = fileWrapper.delete();
            assertTrue(deleted);
        }
        ResourceFile resourceFile = mRepository.findResourceFile(fileWrapper);
        assertNotNull(resourceFile);
        mRepository.removeFile(ResourceType.DRAWABLE, resourceFile);

        assertFalse(mRepository.hasResourceItem("@drawable/graphic"));
        assertFalse(mRepository.hasResourcesOfType(ResourceType.DRAWABLE));

        // Delete one of the overridden layouts
        ResourceItem item = mRepository.getResourceItem(ResourceType.LAYOUT, "layout1");
        assertTrue(item.hasAlternates());
        assertTrue(mRepository.hasResourcesOfType(ResourceType.LAYOUT));
        assertTrue(mRepository.hasResourceItem("@layout/layout1"));

        IAbstractFolder layoutFolder = mRepository.getResFolder()
                .getFolder(FD_RES_LAYOUT + "-land");
        assertNotNull(layoutFolder);

        IAbstractFile layoutFile = layoutFolder.getFile("layout1.xml");
        assertNotNull(layoutFile);
        assertTrue(layoutFile instanceof FileWrapper);
        fileWrapper = (FileWrapper) layoutFile;
        if (fileWrapper.exists()) {
            boolean deleted = fileWrapper.delete();
            assertTrue(deleted);
        }
        resourceFile = mRepository.findResourceFile(fileWrapper);
        assertNotNull(resourceFile);

        ResourceFolder layoutResFolder = mRepository.getResourceFolder(layoutFolder);
        assertNotNull(layoutResFolder);
        layoutResFolder.processFile(layoutFile,
                ResourceDeltaKind.REMOVED, new ScanningContext(mRepository));

        mRepository.removeFile(ResourceType.LAYOUT, resourceFile);

        // We still have a layout1: only default now
        assertTrue(mRepository.hasResourceItem("@layout/layout1"));
        item = mRepository.getResourceItem(ResourceType.LAYOUT, "layout1");
        assertFalse(item.hasAlternates());

        // change strings
        assertTrue(mRepository.hasResourceItem("@string/dummy"));
        assertFalse(mRepository.hasResourceItem("@string/myDummy"));
        ResourceFile stringResFile = mRepository.getResourceItem(ResourceType.STRING, "dummy")
                .getSourceFileList().get(0);
        File stringFile = (File) stringResFile.getFile();
        ResourceFolder folder = stringResFile.getFolder();
        assertTrue(stringFile.exists());
        String strings = Files.toString(stringFile, Charsets.UTF_8);
        assertNotNull(strings);
        strings = strings.replace("name=\"dummy\"", "name=\"myDummy\"");
        Files.write(strings, stringFile, Charsets.UTF_8);

        folder.processFile(stringResFile.getFile(), ResourceDeltaKind.CHANGED,
                new ScanningContext(mRepository));
        assertTrue(mRepository.hasResourceItem("@string/myDummy"));
        assertFalse(mRepository.hasResourceItem("@string/dummy"));

        // add files
        assertFalse(mRepository.hasResourceItem("@layout/layout5"));
        File res = new File(mRepository.getDir(), FD_RES);
        File layout = new File(res, FD_RES_LAYOUT);
        File newFile = new File(layout, "layout5.xml");
        boolean created = newFile.createNewFile();
        assertTrue(created);
        mRepository.findResourceFile(newFile);
        assertTrue(mRepository.hasResourceItem("@layout/layout5"));

        // add files 2
        assertFalse(mRepository.hasResourceItem("@layout/layout6"));
        File newFile2 = new File(layout, "layout6.xml");
        created = newFile2.createNewFile();
        assertTrue(created);
        IAbstractFile new2 = layoutResFolder.getFolder().getFile("layout6.xml");
        assertNotNull(new2);
        layoutResFolder.processFile(new2, ResourceDeltaKind.ADDED,
                new ScanningContext(mRepository));
        assertTrue(mRepository.hasResourceItem("@layout/layout6"));
    }

    public void testFindResourceFile() throws Exception {
        assertTrue(mRepository.hasResourceItem("@layout/layout1"));
        ResourceItem item = mRepository.getResourceItem(ResourceType.LAYOUT, "layout1");
        assertNotNull(item);
        List<ResourceFile> sourceFileList = item.getSourceFileList();
        assertNotNull(sourceFileList);
        assertTrue(!sourceFileList.isEmpty());
        ResourceFile first = sourceFileList.get(0);
        IAbstractFile abstractFile = first.getFile();
        assertNotNull(abstractFile);
        assertTrue(abstractFile instanceof File);
        File f = (File) abstractFile;
        assertSame(first, mRepository.findResourceFile(f));
        assertSame(first, mRepository.findResourceFile(new File(f.getPath())));

        File file = new File(f.getParentFile().getParentFile(), "layout" +
                File.separator + "layout2.xml");
        ResourceFile resourceFile = mRepository.findResourceFile(file);
        assertNotNull(resourceFile);

        // Invalid paths
        assertNull(mRepository.findResourceFile(file.getParentFile()));
        assertNull(mRepository.findResourceFile(file.getParentFile().getParentFile()));
        assertNull(mRepository.findResourceFile(file.getParentFile().getParentFile().
                getParentFile()));
        assertNull(mRepository.findResourceFile(new File("/tmp")));
    }
}
