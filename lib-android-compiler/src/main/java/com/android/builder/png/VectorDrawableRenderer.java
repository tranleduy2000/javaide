/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.builder.png;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.android.annotations.NonNull;
import com.android.ide.common.res2.ResourcePreprocessor;
import com.android.ide.common.resources.configuration.DensityQualifier;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.ide.common.resources.configuration.VersionQualifier;
import com.android.ide.common.vectordrawable.VdPreview;
import com.android.resources.Density;
import com.android.resources.ResourceFolderType;
import com.android.utils.ILogger;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Generates PNG images (and XML copies) from VectorDrawable files.
 */
@SuppressWarnings("MethodMayBeStatic")
public class VectorDrawableRenderer implements ResourcePreprocessor {
    /** Projects with minSdk set to this or higher don't need to generate PNGs. */
    public static final int MIN_SDK_WITH_VECTOR_SUPPORT = 21;

    private final ILogger mLogger;
    private final File mOutputDir;
    private final Collection<Density> mDensities;

    public VectorDrawableRenderer(File outputDir, Collection<Density> densities, ILogger logger) {
        mOutputDir = outputDir;
        mDensities = densities;
        mLogger = logger;
    }

    public Collection<File> createPngFiles(
            @NonNull File inputXmlFile,
            @NonNull File outputResDirectory,
            @NonNull Collection<Density> densities) throws IOException {
        throw new RuntimeException("Replaced by the new way to generate PNGs");
    }

    @Override
    public boolean needsPreprocessing(File resourceFile) {
        return isXml(resourceFile)
                && isInDrawable(resourceFile)
                && getEffectiveVersion(resourceFile) < MIN_SDK_WITH_VECTOR_SUPPORT
                && isRootVector(resourceFile);
    }

    @Override
    public Collection<File> getFilesToBeGenerated(File inputXmlFile) {
        FolderConfiguration originalConfiguration = getFolderConfiguration(inputXmlFile);

        // Create all the PNG files and duplicate the XML into folders with the version qualifier.
        Collection<File> filesToBeGenerated = Lists.newArrayList();
        for (Density density : mDensities) {
            FolderConfiguration newConfiguration = FolderConfiguration.copyOf(originalConfiguration);
            newConfiguration.setDensityQualifier(new DensityQualifier(density));

            File directory = new File(
                    mOutputDir,
                    newConfiguration.getFolderName(ResourceFolderType.DRAWABLE));
            File pngFile = new File(
                    directory,
                    inputXmlFile.getName().replace(".xml", ".png"));

            filesToBeGenerated.add(pngFile);

            newConfiguration.setVersionQualifier(new VersionQualifier(MIN_SDK_WITH_VECTOR_SUPPORT));
            File destination = new File(
                    mOutputDir,
                    newConfiguration.getFolderName(ResourceFolderType.DRAWABLE));
            File xmlCopy = new File(destination, inputXmlFile.getName());
            filesToBeGenerated.add(xmlCopy);
        }

        return filesToBeGenerated;
    }

    @Override
    public void generateFile(File toBeGenerated, File original) throws IOException {
        Files.createParentDirs(toBeGenerated);

        if (isXml(toBeGenerated)) {
            Files.copy(original, toBeGenerated);
        } else {
            mLogger.info(
                    "Generating PNG: [%s] from [%s]",
                    toBeGenerated.getAbsolutePath(),
                    original.getAbsolutePath());

            FolderConfiguration folderConfiguration = getFolderConfiguration(toBeGenerated);
            checkState(folderConfiguration.getDensityQualifier() != null);
            Density density = folderConfiguration.getDensityQualifier().getValue();

            String xmlContent = Files.toString(original, Charsets.UTF_8);
            float scaleFactor = density.getDpiValue() / (float) Density.MEDIUM.getDpiValue();
            if (scaleFactor <= 0) {
                scaleFactor = 1.0f;
            }

            final VdPreview.TargetSize imageSize = VdPreview.TargetSize.createSizeFromScale(scaleFactor);
            BufferedImage image = VdPreview.getPreviewFromVectorXml(imageSize, xmlContent, null);
            checkState(image != null, "Generating the image failed.");
            ImageIO.write(image, "png", toBeGenerated);
        }
    }

    @NonNull
    private FolderConfiguration getFolderConfiguration(@NonNull File inputXmlFile) {
        String parentName = inputXmlFile.getParentFile().getName();
        FolderConfiguration originalConfiguration =
                FolderConfiguration.getConfigForFolder(parentName);
        checkArgument(
                originalConfiguration != null,
                "Invalid resource folder name [%s].",
                parentName);
        return originalConfiguration;
    }

    private boolean isInDrawable(@NonNull File inputXmlFile) {
        ResourceFolderType folderType =
                ResourceFolderType.getFolderType(inputXmlFile.getParentFile().getName());

        return folderType == ResourceFolderType.DRAWABLE;
    }

    /**
     * Parse the root element of the file, return true if it is a vector.
     * TODO: Combine this with the drawing code, such that we don't parse the file twice.
     */
    private boolean isRootVector(File resourceFile) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        boolean result = false;
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(resourceFile);
            Element root = doc.getDocumentElement();
            if (root != null && root.getNodeName().equalsIgnoreCase("vector")) {
                result = true;
            }
        } catch (Exception e) {
            mLogger.error(e, "Exception in parsing the XML resource" + e.getMessage());
        }

        return result;
    }

    private boolean isXml(File resourceFile) {
        return resourceFile.getPath().endsWith(".xml");
    }

    private int getEffectiveVersion(File resourceFile) {
        FolderConfiguration configuration = getFolderConfiguration(resourceFile);
        if (configuration.getVersionQualifier() == null) {
            configuration.createDefault();
        }
        //noinspection ConstantConditions - handled above.
        return configuration.getVersionQualifier().getVersion();
    }
}
