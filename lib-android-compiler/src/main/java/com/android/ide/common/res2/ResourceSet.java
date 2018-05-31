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

package com.android.ide.common.res2;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.resources.FolderTypeRelationship;
import com.android.resources.ResourceConstants;
import com.android.resources.ResourceFolderType;
import com.android.resources.ResourceType;
import com.android.utils.ILogger;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.android.ide.common.res2.ResourceFile.ATTR_QUALIFIER;

/**
 * Implementation of {@link DataSet} for {@link ResourceItem} and {@link ResourceFile}.
 *
 * This is able to detect duplicates from the same source folders (same resource coming from
 * the values folder in same or different files).
 */
public class ResourceSet extends DataSet<ResourceItem, ResourceFile> {

    public ResourceSet(String name) {
        super(name);
    }

    @Override
    protected DataSet<ResourceItem, ResourceFile> createSet(String name) {
        return new ResourceSet(name);
    }

    @Override
    protected ResourceFile createFileAndItems(File sourceFolder, File file, ILogger logger)
            throws MergingException {
        // get the type.
        FolderData folderData = getFolderData(file.getParentFile());

        if (folderData == null) {
            return null;
        }

        return createResourceFile(file, folderData, logger);
    }

    @Override
    protected ResourceFile createFileAndItems(@NonNull File file, @NonNull Node fileNode) {
        Attr qualifierAttr = (Attr) fileNode.getAttributes().getNamedItem(ATTR_QUALIFIER);
        String qualifier = qualifierAttr != null ? qualifierAttr.getValue() : "";

        Attr typeAttr = (Attr) fileNode.getAttributes().getNamedItem(SdkConstants.ATTR_TYPE);
        if (typeAttr == null) {
            // multi res file
            List<ResourceItem> resourceList = Lists.newArrayList();

            // loop on each node that represent a resource
            NodeList resNodes = fileNode.getChildNodes();
            for (int iii = 0, nnn = resNodes.getLength(); iii < nnn; iii++) {
                Node resNode = resNodes.item(iii);

                if (resNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                ResourceItem r = ValueResourceParser2.getResource(resNode, file);
                if (r != null) {
                    resourceList.add(r);
                    if (r.getType() == ResourceType.DECLARE_STYLEABLE) {
                        // Need to also create ATTR items for its children
                        try {
                            ValueResourceParser2.addStyleableItems(resNode, resourceList, null, file);
                        } catch (MergingException ignored) {
                            // since we are not passing a dup map, this will never be thrown
                            assert false : file + ": " + ignored.getMessage();
                        }
                    }
                }
            }

            return new ResourceFile(file, resourceList, qualifier);

        } else {
            // single res file
            ResourceType type = ResourceType.getEnum(typeAttr.getValue());
            if (type == null) {
                return null;
            }

            Attr nameAttr = (Attr) fileNode.getAttributes().getNamedItem(ATTR_NAME);
            if (nameAttr == null) {
                return null;
            }

            ResourceItem item = new ResourceItem(nameAttr.getValue(), type, null);
            return new ResourceFile(file, item, qualifier);
        }
    }

    @Override
    protected void readSourceFolder(File sourceFolder, ILogger logger)
            throws MergingException {
        File[] folders = sourceFolder.listFiles();
        if (folders != null) {
            for (File folder : folders) {
                if (folder.isDirectory() && !isIgnored(folder)) {
                    FolderData folderData = getFolderData(folder);
                    if (folderData != null) {
                        parseFolder(sourceFolder, folder, folderData, logger);
                    }
                }
            }
        }
    }

    @Override
    protected boolean isValidSourceFile(@NonNull File sourceFolder, @NonNull File file) {
        if (!super.isValidSourceFile(sourceFolder, file)) {
            return false;
        }

        File resFolder = file.getParentFile();
        // valid files are right under a resource folder under the source folder
        return resFolder.getParentFile().equals(sourceFolder) &&
                !isIgnored(resFolder) &&
                ResourceFolderType.getFolderType(resFolder.getName()) != null;
    }

    @Override
    protected boolean handleChangedFile(@NonNull File sourceFolder, @NonNull File changedFile)
            throws MergingException {

        FolderData folderData = getFolderData(changedFile.getParentFile());
        if (folderData == null) {
            return true;
        }

        ResourceFile resourceFile = getDataFile(changedFile);

        if (resourceFile == null) {
            String message = String.format(
                    "In DataSet '%s', no data file for changedFile '%s'. "
                            + "This is an internal error in the incremental builds code; "
                            + "to work around it, try doing a full clean build.",
                    getConfigName(), changedFile.getAbsolutePath());
            throw new MergingException(message).setFile(changedFile);
        }

        //noinspection VariableNotUsedInsideIf
        if (folderData.type != null) {
            // single res file
            resourceFile.getItem().setTouched();
        } else {
            // multi res. Need to parse the file and compare the items one by one.
            ValueResourceParser2 parser = new ValueResourceParser2(changedFile);

            List<ResourceItem> parsedItems = parser.parseFile();
            Map<String, ResourceItem> oldItems = Maps.newHashMap(resourceFile.getItemMap());
            Map<String, ResourceItem> newItems  = Maps.newHashMap();

            // create a fake ResourceFile to be able to call resource.getKey();
            // It's ok because we never use this instance anyway.
            ResourceFile fakeResourceFile = new ResourceFile(changedFile, parsedItems,
                    resourceFile.getQualifiers());

            for (ResourceItem newItem : parsedItems) {
                String newKey = newItem.getKey();
                ResourceItem oldItem = oldItems.get(newKey);

                if (oldItem == null) {
                    // this is a new item
                    newItem.setTouched();
                    newItems.put(newKey, newItem);
                } else {
                    // remove it from the list of oldItems (this is to detect deletion)
                    //noinspection SuspiciousMethodCalls
                    oldItems.remove(oldItem.getKey());

                    // now compare the items
                    if (!oldItem.compareValueWith(newItem)) {
                        // if the values are different, take the values from the newItems
                        // and update the old item status.

                        oldItem.setValue(newItem);
                    }
                }
            }

            // at this point oldItems is left with the deleted items.
            // just update their status to removed.
            for (ResourceItem deletedItem : oldItems.values()) {
                deletedItem.setRemoved();
            }

            // Now we need to add the new items to the resource file and the main map
            resourceFile.addItems(newItems.values());
            for (Map.Entry<String, ResourceItem> entry : newItems.entrySet()) {
                addItem(entry.getValue(), entry.getKey());
            }
        }

       return true;
    }

    /**
     * Reads the content of a typed resource folder (sub folder to the root of res folder), and
     * loads the resources from it.
     *
     *
     * @param sourceFolder the main res folder
     * @param folder the folder to read.
     * @param folderData the folder Data
     * @param logger a logger object
     *
     * @throws MergingException if something goes wrong
     */
    private void parseFolder(File sourceFolder, File folder, FolderData folderData, ILogger logger)
            throws MergingException {
        File[] files = folder.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (!file.isFile() || isIgnored(file)) {
                    continue;
                }

                ResourceFile resourceFile = createResourceFile(file, folderData, logger);
                if (resourceFile != null) {
                    processNewDataFile(sourceFolder, resourceFile, true /*setTouched*/);
                }
            }
        }
    }

    private static ResourceFile createResourceFile(File file, FolderData folderData, ILogger logger)
            throws MergingException {
        if (folderData.type != null) {
            int pos;// get the resource name based on the filename
            String name = file.getName();
            pos = name.indexOf('.');
            if (pos >= 0) {
                name = name.substring(0, pos);
            }

            return new ResourceFile(
                    file,
                    new ResourceItem(name, folderData.type, null),
                    folderData.qualifiers);
        } else {
            try {
                ValueResourceParser2 parser = new ValueResourceParser2(file);
                List<ResourceItem> items = parser.parseFile();

                return new ResourceFile(file, items, folderData.qualifiers);
            } catch (MergingException e) {
                e.setFile(file);
                logger.error(e, "Failed to parse %s", file.getAbsolutePath());
                throw e;
            }
        }
    }

    /**
     * temp structure containing a qualifier string and a {@link ResourceType}.
     */
    private static class FolderData {
        String qualifiers = "";
        ResourceType type = null;
        ResourceFolderType folderType = null;
    }

    /**
     * Returns a FolderData for the given folder
     * @param folder the folder.
     * @return the FolderData object.
     */
    @Nullable
    private static FolderData getFolderData(File folder) {
        FolderData fd = new FolderData();

        String folderName = folder.getName();
        int pos = folderName.indexOf(ResourceConstants.RES_QUALIFIER_SEP);
        if (pos != -1) {
            fd.folderType = ResourceFolderType.getTypeByName(folderName.substring(0, pos));
            if (fd.folderType == null) {
                return null;
            }

            FolderConfiguration folderConfiguration = FolderConfiguration.getConfigForFolder(folderName);
            if (folderConfiguration == null) {
                return null;
            }

            // normalize it
            folderConfiguration.normalize();

            // get the qualifier portion from the folder config.
            // the returned string starts with "-" so we remove that.
            fd.qualifiers = folderConfiguration.getUniqueKey().substring(1);

        } else {
            fd.folderType = ResourceFolderType.getTypeByName(folderName);
        }

        if (fd.folderType != null && fd.folderType != ResourceFolderType.VALUES) {
            fd.type = FolderTypeRelationship.getRelatedResourceTypes(fd.folderType).get(0);
        }

        return fd;
    }
}
