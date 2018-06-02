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

package com.android.ide.common.res2;

import static com.android.SdkConstants.ATTR_NAME;
import static com.android.SdkConstants.ATTR_TYPE;
import static com.android.SdkConstants.DOT_9PNG;
import static com.android.SdkConstants.DOT_PNG;
import static com.android.SdkConstants.DOT_XML;
import static com.android.SdkConstants.RES_QUALIFIER_SEP;
import static com.android.SdkConstants.TAG_EAT_COMMENT;
import static com.android.SdkConstants.TAG_RESOURCES;
import static com.android.utils.SdkUtils.createPathComment;
import static com.google.common.base.Preconditions.checkState;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.internal.PngCruncher;
import com.android.ide.common.internal.PngException;
import com.android.resources.ResourceFolderType;
import com.android.resources.ResourceType;
import com.android.utils.SdkUtils;
import com.android.utils.XmlUtils;
import com.google.common.base.Charsets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * A {@link MergeWriter} for assets, using {@link ResourceItem}.
 */
public class MergedResourceWriter extends MergeWriter<ResourceItem> {

    @NonNull
    private final PngCruncher mCruncher;

    @NonNull
    private final ResourcePreprocessor mPreprocessor;

    /**
     * If non-null, points to a File that we should write public.txt to
     */
    private final File mPublicFile;

    private DocumentBuilderFactory mFactory;

    private boolean mInsertSourceMarkers = true;

    private final boolean mCrunchPng;

    private final boolean mProcess9Patch;

    private final int mCruncherKey;

    /**
     * map of XML values files to write after parsing all the files. the key is the qualifier.
     */
    private ListMultimap<String, ResourceItem> mValuesResMap;

    /**
     * Set of qualifier that had a previously written resource now gone. This is to keep a list of
     * values files that must be written out even with no touched or updated resources, in case one
     * or more resources were removed.
     */
    private Set<String> mQualifierWithDeletedValues;

    public MergedResourceWriter(@NonNull File rootFolder,
            @NonNull PngCruncher pngRunner,
            boolean crunchPng,
            boolean process9Patch,
            @Nullable File publicFile,
            @NonNull ResourcePreprocessor preprocessor) {
        super(rootFolder);
        mCruncher = pngRunner;
        mCruncherKey = mCruncher.start();
        mCrunchPng = crunchPng;
        mProcess9Patch = process9Patch;
        mPublicFile = publicFile;
        mPreprocessor = preprocessor;
    }

    /**
     * Sets whether this manifest merger will insert source markers into the merged source
     *
     * @param insertSourceMarkers if true, insert source markers
     */
    public void setInsertSourceMarkers(boolean insertSourceMarkers) {
        mInsertSourceMarkers = insertSourceMarkers;
    }

    /**
     * Returns whether this manifest merger will insert source markers into the merged source
     *
     * @return whether this manifest merger will insert source markers into the merged source
     */
    public boolean isInsertSourceMarkers() {
        return mInsertSourceMarkers;
    }

    @Override
    public void start(@NonNull DocumentBuilderFactory factory) throws ConsumerException {
        super.start(factory);
        mValuesResMap = ArrayListMultimap.create();
        mQualifierWithDeletedValues = Sets.newHashSet();
        mFactory = factory;
    }

    @Override
    public void end() throws ConsumerException {
        // Make sure all PNGs are generated first.
        super.end();
        try {
            // Wait for all PNGs to be crunched.
            mCruncher.end(mCruncherKey);
        } catch (InterruptedException e) {
            throw new ConsumerException(e);
        }

        mValuesResMap = null;
        mQualifierWithDeletedValues = null;
        mFactory = null;
    }

    @Override
    public boolean ignoreItemInMerge(ResourceItem item) {
        return item.getIgnoredFromDiskMerge();
    }

    @Override
    public void addItem(@NonNull final ResourceItem item) throws ConsumerException {
        final ResourceFile.FileType type = item.getSourceType();

        if (type == ResourceFile.FileType.XML_VALUES) {
            // this is a resource for the values files

            // just add the node to write to the map based on the qualifier.
            // We'll figure out later if the files needs to be written or (not)
            mValuesResMap.put(item.getQualifiers(), item);
        } else {
            checkState(item.getSource() != null);
            // This is a single value file or a set of generated files. Only write it if the state
            // is TOUCHED.
            if (item.isTouched()) {
                getExecutor().execute(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        File file = item.getFile();

                        String filename = file.getName();
                        String folderName = getFolderName(item);
                        File typeFolder = new File(getRootFolder(), folderName);
                        try {
                            createDir(typeFolder);
                        } catch (IOException ioe) {
                            throw MergingException.wrapException(ioe).withFile(typeFolder).build();
                        }

                        File outFile = new File(typeFolder, filename);

                        if (type == DataFile.FileType.GENERATED_FILES) {
                            mPreprocessor.generateFile(file, item.getSource().getFile());
                        }

                        try {
                            if (item.getType() == ResourceType.RAW) {
                                // Don't crunch, don't insert source comments, etc - leave alone.
                                Files.copy(file, outFile);
                            } else if (filename.endsWith(DOT_PNG)) {
                                if (mCrunchPng && mProcess9Patch) {
                                    mCruncher.crunchPng(mCruncherKey, file, outFile);
                                } else {
                                    // we should not crunch the png files, but we should still
                                    // process the nine patch.
                                    if (mProcess9Patch && filename.endsWith(DOT_9PNG)) {
                                        mCruncher.crunchPng(mCruncherKey, file, outFile);
                                    } else {
                                        Files.copy(file, outFile);
                                    }
                                }
                            } else if (mInsertSourceMarkers && filename.endsWith(DOT_XML)) {
                                SdkUtils.copyXmlWithSourceReference(file, outFile);
                            } else {
                                Files.copy(file, outFile);
                            }
                        } catch (PngException e) {
                            throw MergingException.wrapException(e).withFile(file).build();
                        } catch (IOException ioe) {
                            throw MergingException.wrapException(ioe).withFile(file).build();
                        }
                        return null;
                    }
                });
            }
        }
    }

    @Override
    public void removeItem(@NonNull ResourceItem removedItem, @Nullable ResourceItem replacedBy)
            throws ConsumerException {
        ResourceFile.FileType removedType = removedItem.getSourceType();
        ResourceFile.FileType replacedType = replacedBy != null
                ? replacedBy.getSourceType()
                : null;

        switch (removedType) {
            case SINGLE_FILE: // Fall through.
            case GENERATED_FILES:
                if (replacedType == DataFile.FileType.SINGLE_FILE
                        || replacedType == DataFile.FileType.GENERATED_FILES) {
                    // Save one IO operation and don't delete a file that will be overwritten
                    // anyway.
                    break;
                }
                removeOutFile(removedItem);
                break;
            case XML_VALUES:
                mQualifierWithDeletedValues.add(removedItem.getQualifiers());
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    protected void postWriteAction() throws ConsumerException {

        // now write the values files.
        for (String key : mValuesResMap.keySet()) {
            // the key is the qualifier.

            // check if we have to write the file due to deleted values.
            // also remove it from that list anyway (to detect empty qualifiers later).
            boolean mustWriteFile = mQualifierWithDeletedValues.remove(key);

            // get the list of items to write
            List<ResourceItem> items = mValuesResMap.get(key);

            // now check if we really have to write it
            if (!mustWriteFile) {
                for (ResourceItem item : items) {
                    if (item.isTouched()) {
                        mustWriteFile = true;
                        break;
                    }
                }
            }

            if (mustWriteFile) {
                String folderName = key.isEmpty() ?
                        ResourceFolderType.VALUES.getName() :
                        ResourceFolderType.VALUES.getName() + RES_QUALIFIER_SEP + key;

                File valuesFolder = new File(getRootFolder(), folderName);
                // Name of the file is the same as the folder as AAPT gets confused with name
                // collision when not normalizing folders name.
                File outFile = new File(valuesFolder, folderName + DOT_XML);
                ResourceFile currentFile = null;
                try {
                    createDir(valuesFolder);

                    DocumentBuilder builder = mFactory.newDocumentBuilder();
                    Document document = builder.newDocument();
                    final String publicTag = ResourceType.PUBLIC.getName();
                    List<Node> publicNodes = null;

                    Node rootNode = document.createElement(TAG_RESOURCES);
                    document.appendChild(rootNode);

                    Collections.sort(items);

                    for (ResourceItem item : items) {
                        Node nodeValue = item.getValue();
                        if (nodeValue != null && publicTag.equals(nodeValue.getNodeName())) {
                            if (publicNodes == null) {
                                publicNodes = Lists.newArrayList();
                            }
                            publicNodes.add(nodeValue);
                            continue;
                        }

                        // add a carriage return so that the nodes are not all on the same line.
                        // also add an indent of 4 spaces.
                        rootNode.appendChild(document.createTextNode("\n    "));

                        ResourceFile source = item.getSource();
                        if (source != currentFile && source != null && mInsertSourceMarkers) {
                            currentFile = source;
                            File file = source.getFile();
                            rootNode.appendChild(document.createComment(
                                    createPathComment(file, true)));
                            rootNode.appendChild(document.createTextNode("\n    "));
                            // Add an <eat-comment> element to ensure that this comment won't
                            // get merged into a potential comment from the next child (or
                            // even added as the sole comment in the R class)
                            rootNode.appendChild(document.createElement(TAG_EAT_COMMENT));
                            rootNode.appendChild(document.createTextNode("\n    "));
                        }

                        Node adoptedNode = NodeUtils.adoptNode(document, nodeValue);
                        rootNode.appendChild(adoptedNode);
                    }

                    // finish with a carriage return
                    rootNode.appendChild(document.createTextNode("\n"));

                    currentFile = null;

                    String content = XmlUtils.toXml(document);
                    Files.write(content, outFile, Charsets.UTF_8);

                    if (publicNodes != null && mPublicFile != null) {
                        // Generate public.txt:
                        int size = publicNodes.size();
                        StringBuilder sb = new StringBuilder(size * 80);
                        for (Node node : publicNodes) {
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                Element element = (Element) node;
                                String name = element.getAttribute(ATTR_NAME);
                                String type = element.getAttribute(ATTR_TYPE);
                                if (!name.isEmpty() && !type.isEmpty()) {
                                    sb.append(type).append(' ').append(name).append('\n');
                                }
                            }
                        }
                        File parentFile = mPublicFile.getParentFile();
                        if (!parentFile.exists()) {
                            boolean mkdirs = parentFile.mkdirs();
                            if (!mkdirs) {
                                throw new IOException("Could not create " + parentFile);
                            }
                        }
                        String text = sb.toString();
                        Files.write(text, mPublicFile, Charsets.UTF_8);
                    }
                } catch (Throwable t) {
                    ConsumerException exception = new ConsumerException(t,
                            currentFile != null ? currentFile.getFile() : outFile);
                    throw exception;
                }
            }
        }

        // now remove empty values files.
        for (String key : mQualifierWithDeletedValues) {
            String folderName = key != null && !key.isEmpty() ?
                    ResourceFolderType.VALUES.getName() + RES_QUALIFIER_SEP + key :
                    ResourceFolderType.VALUES.getName();

            removeOutFile(folderName, folderName + DOT_XML);
        }
    }

    /**
     * Removes a file that already exists in the out res folder. This has to be a non value file.
     *
     * @param resourceItem the source item that created the file to remove.
     * @return true if success.
     */
    private boolean removeOutFile(ResourceItem resourceItem) {
        return removeOutFile(getFolderName(resourceItem), resourceItem.getFile().getName());
    }

    /**
     * Removes a file from a folder based on a sub folder name and a filename
     *
     * @param folderName the sub folder name
     * @param fileName   the file name.
     * @return true if success.
     */
    private boolean removeOutFile(String folderName, String fileName) {
        File valuesFolder = new File(getRootFolder(), folderName);
        File outFile = new File(valuesFolder, fileName);
        return outFile.delete();
    }

    private synchronized void createDir(File folder) throws IOException {
        if (!folder.isDirectory() && !folder.mkdirs()) {
            throw new IOException("Failed to create directory: " + folder);
        }
    }

    /**
     * Calculates the right folder name give a resource item.
     *
     * @param resourceItem the resource item to calculate the folder name from.
     * @return a relative folder name
     */
    @NonNull
    private static String getFolderName(ResourceItem resourceItem) {
        ResourceType itemType = resourceItem.getType();
        String folderName = itemType.getName();
        String qualifiers = resourceItem.getQualifiers();
        if (!qualifiers.isEmpty()) {
            folderName = folderName + RES_QUALIFIER_SEP + qualifiers;
        }
        return folderName;
    }
}
