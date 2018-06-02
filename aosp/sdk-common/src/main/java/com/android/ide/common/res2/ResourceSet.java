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

import static com.android.ide.common.res2.ResourceFile.ATTR_QUALIFIER;
import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.base.Preconditions.checkState;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.blame.Message;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.resources.FolderTypeRelationship;
import com.android.resources.ResourceConstants;
import com.android.resources.ResourceFolderType;
import com.android.resources.ResourceType;
import com.android.utils.ILogger;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link DataSet} for {@link ResourceItem} and {@link ResourceFile}.
 *
 * This is able to detect duplicates from the same source folders (same resource coming from
 * the values folder in same or different files).
 */
public class ResourceSet extends DataSet<ResourceItem, ResourceFile> {

    private boolean mNormalizeResources = false;
    private ResourceSet mGeneratedSet;
    private ResourcePreprocessor mPreprocessor;

    public ResourceSet(String name) {
        this(name, true /*validateEnabled*/);
    }

    public ResourceSet(String name, boolean validateEnabled) {
        super(name, validateEnabled);
    }

    public void setNormalizeResources(boolean normalizeResources) {
        mNormalizeResources = normalizeResources;
    }

    public void setGeneratedSet(ResourceSet generatedSet) {
        mGeneratedSet = generatedSet;
    }

    public void setPreprocessor(ResourcePreprocessor preprocessor) {
        mPreprocessor = preprocessor;
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
    protected ResourceFile createFileAndItemsFromXml(@NonNull File file, @NonNull Node fileNode)
            throws MergingException {
        String qualifier = firstNonNull(NodeUtils.getAttribute(fileNode, ATTR_QUALIFIER), "");
        String typeAttr = NodeUtils.getAttribute(fileNode, SdkConstants.ATTR_TYPE);

        if (NodeUtils.getAttribute(fileNode, SdkConstants.ATTR_PREPROCESSING) != null) {
            // FileType.GENERATED_FILES
            NodeList childNodes = fileNode.getChildNodes();
            int childCount = childNodes.getLength();

            List<ResourceItem> resourceItems =
                    Lists.newArrayListWithCapacity(childCount);

            for (int i = 0; i < childCount; i++) {
                Node childNode = childNodes.item(i);

                String path = NodeUtils.getAttribute(childNode, SdkConstants.ATTR_PATH);
                if (path == null) {
                    continue;
                }

                File generatedFile = new File(path);
                String resourceType = NodeUtils.getAttribute(childNode, SdkConstants.ATTR_TYPE);
                if (resourceType == null) {
                    continue;
                }
                String qualifers = NodeUtils.getAttribute(childNode, ATTR_QUALIFIER);
                if (qualifers == null) {
                    continue;
                }

                resourceItems.add(
                        new GeneratedResourceItem(
                                getNameForFile(generatedFile),
                                generatedFile,
                                FolderTypeRelationship
                                        .getRelatedResourceTypes(
                                                ResourceFolderType.getTypeByName(resourceType))
                                        .get(0),
                                qualifers));
            }

            return ResourceFile.generatedFiles(file, resourceItems, qualifier);
        }
        else if (typeAttr == null) {
            // FileType.XML_VALUES
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
            ResourceType type = ResourceType.getEnum(typeAttr);
            if (type == null) {
                return null;
            }

            String nameAttr = NodeUtils.getAttribute(fileNode, ATTR_NAME);
            if (nameAttr == null) {
                return null;
            }

            if (getValidateEnabled()) {
                FileResourceNameValidator.validate(file, type);
            }
            ResourceItem item = new ResourceItem(nameAttr, type, null);
            return new ResourceFile(file, item, qualifier);
        }
    }

    @Override
    protected void readSourceFolder(File sourceFolder, ILogger logger)
            throws MergingException {
        List<Message> errors = Lists.newArrayList();
        File[] folders = sourceFolder.listFiles();
        if (folders != null) {
            for (File folder : folders) {
                if (folder.isDirectory() && !isIgnored(folder)) {
                    FolderData folderData = getFolderData(folder);
                    if (folderData != null) {
                        try {
                            parseFolder(sourceFolder, folder, folderData, logger);
                        } catch (MergingException e) {
                            errors.addAll(e.getMessages());
                        }
                    }
                }
            }
        }
        MergingException.throwIfNonEmpty(errors);
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
    protected boolean handleNewFile(File sourceFolder, File file, ILogger logger)
            throws MergingException {
        ResourceFile resourceFile = createFileAndItems(sourceFolder, file, logger);
        processNewResourceFile(sourceFolder, resourceFile);
        return true;
    }

    @Override
    protected boolean handleRemovedFile(File removedFile) {
        if (mGeneratedSet != null && mGeneratedSet.getDataFile(removedFile) != null) {
            return mGeneratedSet.handleRemovedFile(removedFile);
        } else {
            return super.handleRemovedFile(removedFile);
        }
    }

    @Override
    protected boolean handleChangedFile(
            @NonNull File sourceFolder,
            @NonNull File changedFile,
            @NonNull ILogger logger) throws MergingException {
        FolderData folderData = getFolderData(changedFile.getParentFile());
        if (folderData == null) {
            return true;
        }

        ResourceFile resourceFile = getDataFile(changedFile);
        if (mGeneratedSet == null) {
            // This is a generated set.
            doHandleChangedFile(changedFile, resourceFile);
            return true;
        }

        ResourceFile generatedSetResourceFile = mGeneratedSet.getDataFile(changedFile);
        boolean needsPreprocessing = needsPreprocessing(changedFile);

        if (resourceFile != null && generatedSetResourceFile == null && needsPreprocessing) {
            // It didn't use to need preprocessing, but it does now.
            handleRemovedFile(changedFile);
            mGeneratedSet.handleNewFile(sourceFolder, changedFile, logger);
        } else if (resourceFile == null
                && generatedSetResourceFile != null
                && !needsPreprocessing) {
            // It used to need preprocessing, but not anymore.
            mGeneratedSet.handleRemovedFile(changedFile);
            handleNewFile(sourceFolder, changedFile, logger);
        } else if (resourceFile == null
                && generatedSetResourceFile != null
                && needsPreprocessing) {
            // Delegate to the generated set.
            mGeneratedSet.handleChangedFile(sourceFolder, changedFile, logger);
        } else if (resourceFile != null
                && !needsPreprocessing
                && generatedSetResourceFile == null) {
            // The "normal" case, handle it here.
            doHandleChangedFile(changedFile, resourceFile);
        } else {
            // Something strange happened.
            throw MergingException.withMessage("In DataSet '%s', no data file for changedFile. "
                            + "This is an internal error in the incremental builds code; "
                            + "to work around it, try doing a full clean build.",
                    getConfigName()).withFile(changedFile).build();
        }

        return true;
    }

    private void doHandleChangedFile(@NonNull File changedFile, ResourceFile resourceFile)
            throws MergingException {
        switch (resourceFile.getType()) {
            case SINGLE_FILE:
                // single res file
                resourceFile.getItem().setTouched();
                break;
            case GENERATED_FILES:
                handleChangedItems(resourceFile,
                        getResourceItemsForGeneratedFiles(changedFile));
                break;
            case XML_VALUES:
                // multi res. Need to parse the file and compare the items one by one.
                ValueResourceParser2 parser = new ValueResourceParser2(changedFile);

                List<ResourceItem> parsedItems = parser.parseFile();
                handleChangedItems(resourceFile, parsedItems);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private void handleChangedItems(
            ResourceFile resourceFile,
            List<ResourceItem> currentItems) throws MergingException {
        Map<String, ResourceItem> oldItems = Maps.newHashMap(resourceFile.getItemMap());
        Map<String, ResourceItem> addedItems = Maps.newHashMap();

        // Set the source of newly determined items, so we can call getKey() on them.
        for (ResourceItem currentItem : currentItems) {
            currentItem.setSource(resourceFile);
        }

        for (ResourceItem newItem : currentItems) {
            String newKey = newItem.getKey();
            ResourceItem oldItem = oldItems.get(newKey);

            if (oldItem == null) {
                // this is a new item
                newItem.setTouched();
                addedItems.put(newKey, newItem);
            } else {
                // remove it from the list of oldItems (this is to detect deletion)
                //noinspection SuspiciousMethodCalls
                oldItems.remove(oldItem.getKey());

                if (oldItem.getSource().getType() == DataFile.FileType.XML_VALUES) {
                    if (!oldItem.compareValueWith(newItem)) {
                        // if the values are different, take the values from the newItem
                        // and update the old item status.

                        oldItem.setValue(newItem);
                    }
                } else {
                    oldItem.setTouched();
                }
            }
        }

        // at this point oldItems is left with the deleted items.
        // just update their status to removed.
        for (ResourceItem deletedItem : oldItems.values()) {
            deletedItem.setRemoved();
        }

        // Now we need to add the new items to the resource file and the main map
        resourceFile.addItems(addedItems.values());
        for (Map.Entry<String, ResourceItem> entry : addedItems.entrySet()) {
            addItem(entry.getValue(), entry.getKey());
        }
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
                processNewResourceFile(sourceFolder, resourceFile);
            }
        }
    }

    private void processNewResourceFile(File sourceFolder, ResourceFile resourceFile)
            throws MergingException {
        if (resourceFile != null) {
            if (resourceFile.getType() == DataFile.FileType.GENERATED_FILES
                    && mGeneratedSet != null) {
                mGeneratedSet.processNewDataFile(sourceFolder, resourceFile, true);
            } else {
                processNewDataFile(sourceFolder, resourceFile, true /*setTouched*/);
            }
        }
    }

    private ResourceFile createResourceFile(@NonNull File file,
            @NonNull FolderData folderData, @NonNull ILogger logger) throws MergingException {
        if (folderData.type != null) {
            FileResourceNameValidator.validate(file, folderData.type);
            String name = getNameForFile(file);

            if (needsPreprocessing(file)) {
                return ResourceFile.generatedFiles(
                        file,
                        getResourceItemsForGeneratedFiles(file),
                        folderData.qualifiers);
            } else {
                return new ResourceFile(
                        file,
                        new ResourceItem(name, folderData.type, null),
                        folderData.qualifiers);
            }
        } else {
            try {
                ValueResourceParser2 parser = new ValueResourceParser2(file);
                List<ResourceItem> items = parser.parseFile();

                return new ResourceFile(file, items, folderData.qualifiers);
            } catch (MergingException e) {
                logger.error(e, "Failed to parse %s", file.getAbsolutePath());
                throw e;
            }
        }
    }

    private boolean needsPreprocessing(@NonNull File file) {
        return mPreprocessor != null && mPreprocessor.needsPreprocessing(file);
    }

    @NonNull
    private List<ResourceItem> getResourceItemsForGeneratedFiles(
            @NonNull File file)
            throws MergingException {
        List<ResourceItem> resourceItems = new ArrayList<ResourceItem>();

        for (File generatedFile : mPreprocessor.getFilesToBeGenerated(file)) {
            FolderData generatedFileFolderData =
                    getFolderData(generatedFile.getParentFile());

            checkState(
                    generatedFileFolderData != null,
                    "Can't determine folder type for %s",
                    generatedFile.getPath());

            resourceItems.add(
                    new GeneratedResourceItem(
                            getNameForFile(generatedFile),
                            generatedFile,
                            generatedFileFolderData.type,
                            generatedFileFolderData.qualifiers));
        }
        return resourceItems;
    }

    @NonNull
    private static String getNameForFile(@NonNull File file) {
        String name = file.getName();
        int pos = name.indexOf('.'); // get the resource name based on the filename
        if (pos >= 0) {
            name = name.substring(0, pos);
        }
        return name;
    }

    /**
     * temp structure containing a qualifier string and a {@link com.android.resources.ResourceType}.
     */
    private static class FolderData {
        String qualifiers = "";
        ResourceType type = null;
        ResourceFolderType folderType = null;
    }

    /**
     * Returns a FolderData for the given folder.
     *
     * @param folder the folder.
     * @return the FolderData object, or null if we can't determine the {#link ResourceFolderType}
     *         of the folder.
     */
    @Nullable
    private FolderData getFolderData(File folder) throws MergingException {
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
                throw MergingException.withMessage("Invalid resource directory name")
                        .withFile(folder).build();
            }

            if (mNormalizeResources) {
                // normalize it
                folderConfiguration.normalize();
            }

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

    @Override
    void appendToXml(@NonNull Node setNode, @NonNull Document document,
            @NonNull MergeConsumer<ResourceItem> consumer) {
        if (mGeneratedSet != null) {
            NodeUtils.addAttribute(
                    document,
                    setNode,
                    null,
                    "generated-set",
                    mGeneratedSet.getConfigName());
        }
        super.appendToXml(setNode, document, consumer);
    }
}
