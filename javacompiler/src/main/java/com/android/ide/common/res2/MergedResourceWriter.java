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

import static com.android.SdkConstants.DOT_PNG;
import static com.android.SdkConstants.DOT_XML;
import static com.android.SdkConstants.RES_QUALIFIER_SEP;
import static com.android.SdkConstants.TAG_EAT_COMMENT;
import static com.android.SdkConstants.TAG_RESOURCES;
import static com.android.utils.SdkUtils.createPathComment;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.internal.PngCruncher;
import com.android.resources.ResourceFolderType;
import com.android.resources.ResourceType;
import com.android.utils.SdkUtils;
import com.android.utils.XmlUtils;
import com.google.common.base.Charsets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import org.w3c.dom.Document;
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
    /** Filename to save the merged file as */
    public static final String FN_VALUES_XML = "values.xml";
    /** Prefix in comments which mark the source locations for merge results */
    public static final String FILENAME_PREFIX = "From: ";

    @Nullable
    private final PngCruncher mCruncher;

    private DocumentBuilderFactory mFactory;

    private boolean mInsertSourceMarkers = true;

    /**
     * map of XML values files to write after parsing all the files. the key is the qualifier.
     */
    private ListMultimap<String, ResourceItem> mValuesResMap;

    /**
     * Set of qualifier that had a previously written resource now gone.
     * This is to keep a list of values files that must be written out even with no
     * touched or updated resources, in case one or more resources were removed.
     */
    private Set<String> mQualifierWithDeletedValues;

    public MergedResourceWriter(@NonNull File rootFolder, @Nullable PngCruncher pngRunner) {
        super(rootFolder);
        mCruncher = pngRunner;
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
        super.end();
        try {
            if (mCruncher != null) {
                mCruncher.end();
            }
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
        ResourceFile.FileType type = item.getSourceType();

        if (type == ResourceFile.FileType.MULTI) {
            // this is a resource for the values files

            // just add the node to write to the map based on the qualifier.
            // We'll figure out later if the files needs to be written or (not)
            mValuesResMap.put(item.getQualifiers(), item);
        } else {
            // This is a single value file.
            // Only write it if the state is TOUCHED.
            if (item.isTouched()) {
                getExecutor().execute(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        ResourceFile resourceFile = item.getSource();
                        File file = resourceFile.getFile();

                        String filename = file.getName();

                        // Validate the filename here. Waiting for aapt isn't good
                        // because the error messages don't point back to the original
                        // file (if it's not an XML file) and besides, aapt prints
                        // the wrong path (it hard-codes "res" into the path for example,
                        // even if the file is not in a folder named res.
                        for (int i = 0, n = filename.length(); i < n; i++) {
                            // This is a direct port of the aapt file check in aapt's
                            // Resource.cpp#makeFileResources validation
                            char c = filename.charAt(i);
                            if (!((c >= 'a' && c <= 'z')
                                    || (c >= '0' && c <= '9')
                                    || c == '_' || c == '.')) {
                                String message =
                                        "Invalid file name: must contain only lowercase "
                                        + "letters and digits ([a-z0-9_.])";
                                throw new MergingException(message).setFile(file);
                            }
                        }

                        ResourceType itemType = item.getType();
                        String folderName = itemType.getName();
                        String qualifiers = resourceFile.getQualifiers();
                        if (!qualifiers.isEmpty()) {
                            folderName = folderName + RES_QUALIFIER_SEP + qualifiers;
                        }

                        File typeFolder = new File(getRootFolder(), folderName);
                        try {
                            createDir(typeFolder);
                        } catch (IOException ioe) {
                            throw new MergingException(ioe).setFile(typeFolder);
                        }

                        File outFile = new File(typeFolder, filename);

                        try {
                            if (itemType == ResourceType.RAW) {
                                // Don't crunch, don't insert source comments, etc - leave alone.
                                Files.copy(file, outFile);
                            } else if (mCruncher != null && filename.endsWith(DOT_PNG)) {
                                // Crunch the the PNG file.
                                mCruncher.crunchPng(file, outFile);
                            } else if (mInsertSourceMarkers && filename.endsWith(DOT_XML)) {
                                SdkUtils.copyXmlWithSourceReference(file, outFile);
                            } else {
                                Files.copy(file, outFile);
                            }
                        } catch (IOException ioe) {
                            throw new MergingException(ioe).setFile(file);
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
        ResourceFile.FileType replacedType = replacedBy != null ?
                replacedBy.getSourceType() : null;

        if (removedType == replacedType) {
            // if the type is multi, then we make sure to flag the qualifier as deleted.
            if (removedType == ResourceFile.FileType.MULTI) {
                mQualifierWithDeletedValues.add(
                        removedItem.getQualifiers());
            } else {
                // both are single type resources, so we actually don't delete the previous
                // file as the new one will replace it instead.
            }
        } else if (removedType == ResourceFile.FileType.SINGLE) {
            // removed type is single.
            // The case of both single type is above, so here either, there is no replacement
            // or the replacement is multi. We always need to remove the old file.
            // if replacedType is non-null, then it was values, if not,
            removeOutFile(removedItem.getSource());
        } else {
            // removed type is multi.
            // whether the new type is single or doesn't exist, we always need to mark the qualifier
            // for rewrite.
            mQualifierWithDeletedValues.add(removedItem.getQualifiers());
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
                File outFile = new File(valuesFolder, FN_VALUES_XML);
                ResourceFile currentFile = null;
                try {
                    createDir(valuesFolder);

                    DocumentBuilder builder = mFactory.newDocumentBuilder();
                    Document document = builder.newDocument();

                    Node rootNode = document.createElement(TAG_RESOURCES);
                    document.appendChild(rootNode);

                    Collections.sort(items);

                    for (ResourceItem item : items) {
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
                        Node adoptedNode = NodeUtils.adoptNode(document, item.getValue());
                        rootNode.appendChild(adoptedNode);

                    }

                    // finish with a carriage return
                    rootNode.appendChild(document.createTextNode("\n"));

                    currentFile = null;

                    String content = XmlUtils.toXml(document, true /*preserveWhitespace*/);
                    Files.write(content, outFile, Charsets.UTF_8);
                } catch (Throwable t) {
                    ConsumerException exception = new ConsumerException(t);
                    exception.setFile(currentFile != null ? currentFile.getFile() : outFile);
                    throw exception;
                }
            }
        }

        // now remove empty values files.
        for (String key : mQualifierWithDeletedValues) {
            String folderName = key != null && !key.isEmpty() ?
                    ResourceFolderType.VALUES.getName() + RES_QUALIFIER_SEP + key :
                    ResourceFolderType.VALUES.getName();

            removeOutFile(folderName, FN_VALUES_XML);
        }
    }

    /**
     * Removes a file that already exists in the out res folder. This has to be a non value file.
     *
     * @param resourceFile the source file that created the file to remove.
     * @return true if success.
     */
    private boolean removeOutFile(ResourceFile resourceFile) {
        if (resourceFile.getType() == ResourceFile.FileType.MULTI) {
            throw new IllegalArgumentException("SourceFile cannot be a FileType.MULTI");
        }

        File file = resourceFile.getFile();
        String fileName = file.getName();
        String folderName = file.getParentFile().getName();

        return removeOutFile(folderName, fileName);
    }

    /**
     * Removes a file from a folder based on a sub folder name and a filename
     *
     * @param folderName the sub folder name
     * @param fileName the file name.
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
}
