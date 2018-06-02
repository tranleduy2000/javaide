/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.manifmerger;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.concurrency.Immutable;
import com.android.utils.ILogger;
import com.android.utils.Pair;
import com.android.utils.SdkUtils;
import com.android.utils.StdLogger;
import com.android.utils.XmlUtils;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.manifmerger.Actions.AttributeRecord;
import static com.android.manifmerger.ManifestModel.NodeTypes;
import static com.android.manifmerger.PlaceholderHandler.KeyBasedValueResolver;

/**
 * merges android manifest files, idempotent.
 */
@Immutable
public class ManifestMerger2 {

    private final File mMainManifestFile;
    private final ImmutableList<Pair<String, File>> mLibraryFiles;
    private final ImmutableList<File> mFlavorsAndBuildTypeFiles;
    private final ImmutableList<Invoker.Feature> mOptionalFeatures;
    private final KeyBasedValueResolver<String> mPlaceHolderValueResolver;
    private final KeyBasedValueResolver<SystemProperty> mSystemPropertyResolver;
    private final ILogger mLogger;

    private ManifestMerger2(
            @NonNull ILogger logger,
            @NonNull File mainManifestFile,
            @NonNull ImmutableList<Pair<String, File>> libraryFiles,
            @NonNull ImmutableList<File> flavorsAndBuildTypeFiles,
            @NonNull ImmutableList<Invoker.Feature> optionalFeatures,
            @NonNull KeyBasedValueResolver<String> placeHolderValueResolver,
            @NonNull KeyBasedValueResolver<SystemProperty> systemPropertiesResolver) {
        this.mLogger = logger;
        this.mMainManifestFile = mainManifestFile;
        this.mLibraryFiles = libraryFiles;
        this.mFlavorsAndBuildTypeFiles = flavorsAndBuildTypeFiles;
        this.mOptionalFeatures = optionalFeatures;
        this.mPlaceHolderValueResolver = placeHolderValueResolver;
        this.mSystemPropertyResolver = systemPropertiesResolver;
    }

    /**
     * Perform high level ordering of files merging and delegates actual merging to
     * {@link XmlDocument#merge(XmlDocument, com.android.manifmerger.MergingReport.Builder)}
     *
     * @return the merging activity report.
     * @throws MergeFailureException if the merging cannot be completed (for instance, if xml
     * files cannot be loaded).
     */
    private MergingReport merge() throws MergeFailureException {
        // initiate a new merging report
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);

        SelectorResolver selectors = new SelectorResolver();
        // invariant : xmlDocumentOptional holds the higher priority document and we try to
        // merge in lower priority documents.
        Optional<XmlDocument> xmlDocumentOptional = Optional.absent();
        for (File inputFile : mFlavorsAndBuildTypeFiles) {
            mLogger.info("Merging flavors and build manifest %s \n", inputFile.getPath());
            xmlDocumentOptional = merge(xmlDocumentOptional,
                    Pair.of((String) null, inputFile),
                    selectors,
                    mergingReportBuilder);
            if (!xmlDocumentOptional.isPresent()) {
                return mergingReportBuilder.build();
            }
        }
        // load all the libraries xml files up front to have a list of all possible node:selector
        // values.
        List<XmlDocument> loadedLibraryDocuments = loadLibraries(selectors);

        mLogger.info("Merging main manifest %s\n", mMainManifestFile.getPath());
        xmlDocumentOptional = merge(xmlDocumentOptional,
                Pair.of(mMainManifestFile.getName(), mMainManifestFile),
                selectors,
                mergingReportBuilder);
        if (!xmlDocumentOptional.isPresent()) {
            return mergingReportBuilder.build();
        }
        for (XmlDocument libraryDocument : loadedLibraryDocuments) {
            mLogger.info("Merging library manifest " + libraryDocument.getSourceLocation());
            xmlDocumentOptional = merge(
                    xmlDocumentOptional, libraryDocument, mergingReportBuilder);
            if (!xmlDocumentOptional.isPresent()) {
                return mergingReportBuilder.build();
            }
        }

        // done with proper merging phase, now we need to trim unwanted elements, placeholder
        // substitution and system properties injection.
        ElementsTrimmer.trim(xmlDocumentOptional.get(), mergingReportBuilder);
        if (mergingReportBuilder.hasErrors()) {
            return mergingReportBuilder.build();
        }

        // do placeholder substitution
        PlaceholderHandler placeholderHandler = new PlaceholderHandler();
        placeholderHandler.visit(
                xmlDocumentOptional.get(),
                mPlaceHolderValueResolver,
                mergingReportBuilder);
        if (mergingReportBuilder.hasErrors()) {
            return mergingReportBuilder.build();
        }

        // perform system property injection.
        performSystemPropertiesInjection(mergingReportBuilder, xmlDocumentOptional.get());

        XmlDocument finalMergedDocument = xmlDocumentOptional.get();
        PostValidator.validate(finalMergedDocument, mergingReportBuilder);
        if (mergingReportBuilder.hasErrors()) {
            finalMergedDocument.getRootNode().addMessage(mergingReportBuilder,
                    MergingReport.Record.Severity.WARNING,
                    "Post merge validation failed");
        }
        XmlDocument cleanedDocument =
                ToolsInstructionsCleaner.cleanToolsReferences(
                        finalMergedDocument, mLogger);

        if (cleanedDocument != null) {
            mergingReportBuilder.setMergedDocument(cleanedDocument);
        }

        MergingReport build = mergingReportBuilder.build();
        StdLogger stdLogger = new StdLogger(StdLogger.Level.INFO);
        build.log(stdLogger);
        stdLogger.error(null, build.getMergedDocument().get().prettyPrint());

        return build;
    }

    // merge the optionally existing xmlDocument with a lower priority xml file.
    private Optional<XmlDocument> merge(
            Optional<XmlDocument> xmlDocument,
            Pair<String, File> lowerPriorityXmlFile,
            KeyResolver<String> selectors,
            MergingReport.Builder mergingReportBuilder) throws MergeFailureException {

        XmlDocument lowerPriorityDocument;
        try {
            lowerPriorityDocument = XmlLoader.load(selectors,
                    lowerPriorityXmlFile.getFirst(), lowerPriorityXmlFile.getSecond());
        } catch (Exception e) {
            throw new MergeFailureException(e);
        }
        return merge(xmlDocument, lowerPriorityDocument, mergingReportBuilder);

    }

    // merge the optionally existing xmlDocument with a lower priority xml file.
    private Optional<XmlDocument> merge(
            Optional<XmlDocument> xmlDocument,
            XmlDocument lowerPriorityDocument,
            MergingReport.Builder mergingReportBuilder) throws MergeFailureException {

        MergingReport.Result validationResult = PreValidator
                .validate(mergingReportBuilder, lowerPriorityDocument);
        if (validationResult == MergingReport.Result.ERROR) {
            mergingReportBuilder.addMessage(lowerPriorityDocument.getSourceLocation(), 0, 0,
                    MergingReport.Record.Severity.ERROR,
                    "Validation failed, exiting");
            return Optional.absent();
        }
        Optional<XmlDocument> result;
        if (xmlDocument.isPresent()) {
            result = xmlDocument.get().merge(lowerPriorityDocument, mergingReportBuilder);
        } else {
            mergingReportBuilder.getActionRecorder().recordDefaultNodeAction(
                    lowerPriorityDocument.getRootNode());
            result = Optional.of(lowerPriorityDocument);
        }

        // if requested, dump each intermediary merging stage into the report.
        if (mOptionalFeatures.contains(Invoker.Feature.KEEP_INTERMEDIARY_STAGES)
                && result.isPresent()) {
            mergingReportBuilder.addMergingStage(result.get().prettyPrint());
        }

        return result;
    }

    private List<XmlDocument> loadLibraries(SelectorResolver selectors)
            throws MergeFailureException {

        ImmutableList.Builder<XmlDocument> loadedLibraryDocuments = ImmutableList.builder();
        for (Pair<String, File> libraryFile : mLibraryFiles) {
            mLogger.info("Loading library manifest " + libraryFile.getSecond().getPath());
            XmlDocument libraryDocument;
            try {
                libraryDocument = XmlLoader.load(selectors,
                        libraryFile.getFirst(), libraryFile.getSecond());
            } catch (Exception e) {
                throw new MergeFailureException(e);
            }
            // extract the package name...
            String libraryPackage = libraryDocument.getRootNode().getXml().getAttribute("package");
            // save it in the selector instance.
            if (!Strings.isNullOrEmpty(libraryPackage)) {
                selectors.addSelector(libraryPackage, libraryFile.getFirst());
            }

            loadedLibraryDocuments.add(libraryDocument);
        }
        return loadedLibraryDocuments.build();
    }

    /**
     * Defines a property that can add or override itself into an XML document.
     */
    public interface AutoAddingProperty {

        /**
         * Add itself (possibly just override the current value) with the passed value
         * @param actionRecorder to record actions.
         * @param document the xml document to add itself to.
         * @param value the value to set of this property.
         */
        void addTo(@NonNull ActionRecorder actionRecorder,
                   @NonNull XmlDocument document,
                   @NonNull String value);
    }

    /**
     * Implementation a {@link com.android.manifmerger.KeyResolver} capable of resolving all
     * selectors value in the context of the passed libraries to this merging activities.
     */
    private static class SelectorResolver implements KeyResolver<String> {

        private final Map<String, String> mSelectors = new HashMap<String, String>();

        private void addSelector(String key, String value) {
            mSelectors.put(key, value);
        }

        @Nullable
        @Override
        public String resolve(String key) {
            return mSelectors.get(key);
        }

        @Override
        public Iterable<String> getKeys() {
            return mSelectors.keySet();
        }
    }

    /**
     * List of manifest files properties that can be directly overridden without using a
     * placeholder.
     */
    public static enum SystemProperty implements AutoAddingProperty {

        /**
         * Allow setting the merged manifest file package name.
         */
        PACKAGE {
            @Override
            public void addTo(@NonNull ActionRecorder actionRecorder,
                    @NonNull XmlDocument document,
                    @NonNull String value) {
                addToElement(this, actionRecorder, value, document.getRootNode());
            }
        },
        /**
         * http://developer.android.com/guide/topics/manifest/manifest-element.html#vcode
         */
        VERSION_CODE {
            @Override
            public void addTo(@NonNull ActionRecorder actionRecorder,
                    @NonNull XmlDocument document,
                    @NonNull String value) {
                addToElementInAndroidNS(this, actionRecorder, value, document.getRootNode());
            }
        },
        /**
         * http://developer.android.com/guide/topics/manifest/manifest-element.html#vname
         */
        VERSION_NAME {
            @Override
            public void addTo(@NonNull ActionRecorder actionRecorder,
                    @NonNull XmlDocument document,
                    @NonNull String value) {
                addToElementInAndroidNS(this, actionRecorder, value, document.getRootNode());
            }
        },
        /**
         * http://developer.android.com/guide/topics/manifest/uses-sdk-element.html#min
         */
        MIN_SDK_VERSION {
            @Override
            public void addTo(@NonNull ActionRecorder actionRecorder,
                    @NonNull XmlDocument document,
                    @NonNull String value) {
                addToElementInAndroidNS(this, actionRecorder, value,
                        createOrGetUseSdk(actionRecorder, document));
            }
        },
        /**
         * http://developer.android.com/guide/topics/manifest/uses-sdk-element.html#target
         */
        TARGET_SDK_VERSION {
            @Override
            public void addTo(@NonNull ActionRecorder actionRecorder,
                    @NonNull XmlDocument document,
                    @NonNull String value) {
                addToElementInAndroidNS(this, actionRecorder, value,
                        createOrGetUseSdk(actionRecorder, document));
            }
        };

        public String toCamelCase() {
            return SdkUtils.constantNameToCamelCase(name());
        }

        // utility method to add an attribute which name is derived from the enum name().
        private static void addToElement(
                SystemProperty systemProperty,
                ActionRecorder actionRecorder,
                String value,
                XmlElement to) {

            to.getXml().setAttribute(systemProperty.toCamelCase(), value);
            XmlAttribute xmlAttribute = new XmlAttribute(to,
                    to.getXml().getAttributeNode(systemProperty.toCamelCase()), null);
            actionRecorder.recordAttributeAction(xmlAttribute, new AttributeRecord(
                    Actions.ActionType.INJECTED,
                    new Actions.ActionLocation(
                            to.getSourceLocation(),
                            PositionImpl.UNKNOWN),
                    xmlAttribute.getId(),
                    null, /* reason */
                    null /* attributeOperationType */));
        }

        // utility method to add an attribute in android namespace which local name is derived from
        // the enum name().
        private static void addToElementInAndroidNS(
                SystemProperty systemProperty,
                ActionRecorder actionRecorder,
                String value,
                XmlElement to) {

            String toolsPrefix = XmlUtils.lookupNamespacePrefix(
                    to.getXml(), SdkConstants.ANDROID_URI, SdkConstants.ANDROID_NS_NAME, false);
            to.getXml().setAttributeNS(SdkConstants.ANDROID_URI,
                    toolsPrefix + XmlUtils.NS_SEPARATOR + systemProperty.toCamelCase(),
                    value);
            Attr attr = to.getXml().getAttributeNodeNS(SdkConstants.ANDROID_URI,
                    systemProperty.toCamelCase());

            XmlAttribute xmlAttribute = new XmlAttribute(to, attr, null);
            actionRecorder.recordAttributeAction(xmlAttribute,
                    new AttributeRecord(
                            Actions.ActionType.INJECTED,
                            new Actions.ActionLocation(
                                    to.getSourceLocation(),
                                    PositionImpl.UNKNOWN),
                            xmlAttribute.getId(),
                            null, /* reason */
                            null /* attributeOperationType */
                    )
            );

        }

        // utility method to create or get an existing use-sdk xml element under manifest.
        // this could be made more generic by adding more metadata to the enum but since there is
        // only one case so far, keep it simple.
        private static XmlElement createOrGetUseSdk(
                ActionRecorder actionRecorder, XmlDocument document) {

            Element manifest = document.getXml().getDocumentElement();
            NodeList usesSdks = manifest
                    .getElementsByTagName(NodeTypes.USES_SDK.toXmlName());
            if (usesSdks.getLength() == 0) {
                // create it first.
                Element useSdk = manifest.getOwnerDocument().createElement(
                        NodeTypes.USES_SDK.toXmlName());
                manifest.appendChild(useSdk);
                XmlElement xmlElement = new XmlElement(useSdk, document);
                Actions.NodeRecord nodeRecord = new Actions.NodeRecord(
                        Actions.ActionType.INJECTED,
                        new Actions.ActionLocation(xmlElement.getSourceLocation(),
                                PositionImpl.UNKNOWN),
                        xmlElement.getId(),
                        "use-sdk injection requested",
                        NodeOperationType.STRICT);
                actionRecorder.recordNodeAction(xmlElement, nodeRecord);
                return xmlElement;
            } else {
                return new XmlElement((Element) usesSdks.item(0), document);
            }
        }
    }

    /**
     * Perform {@link com.android.manifmerger.ManifestMerger2.SystemProperty} injection.
     * @param mergingReport to log actions and errors.
     * @param xmlDocument the xml document to inject into.
     */
    private void performSystemPropertiesInjection(
            MergingReport.Builder mergingReport,
            XmlDocument xmlDocument) {
        for (SystemProperty systemProperty : SystemProperty.values()) {
            String propertyOverride = mSystemPropertyResolver.getValue(systemProperty);
            if (propertyOverride != null) {
                systemProperty.addTo(
                        mergingReport.getActionRecorder(), xmlDocument, propertyOverride);
            }
        }
    }

    /**
     * Creates a new {@link com.android.manifmerger.ManifestMerger2.Invoker} instance to invoke
     * the merging tool.
     *
     * @param mainManifestFile application main manifest file.
     * @param logger the logger interface to use.
     * @return an {@link com.android.manifmerger.ManifestMerger2.Invoker} instance that will allow
     * further customization and trigger the merging tool.
     */
    public static Invoker newInvoker(@NonNull File mainManifestFile, @NonNull ILogger logger) {
        return new Invoker(mainManifestFile, logger);
    }

    // a wrapper exception to all sorts of failure exceptions that can be thrown during merging.
    public static class MergeFailureException extends Exception {

        private MergeFailureException(Exception cause) {
            super(cause);
        }
    }

    /**
     * This class will hold all invocation parameters for the manifest merging tool.
     *
     * There are broadly three types of input to the merging tool :
     * <ul>
     *     <li>Build types and flavors overriding manifests</li>
     *     <li>Application main manifest</li>
     *     <li>Library manifest files</li></lib>
     * </ul>
     *
     * Only the main manifest file is a mandatory parameter.
     *
     * High level description of the merging will be as follow :
     * <ol>
     *     <li>Build type and flavors will be merged first in the order they were added. Highest
     *     priority file added first, lowest added last.</li>
     *     <li>Resulting document is merged with lower priority application main manifest file.</li>
     *     <li>Resulting document is merged with each library file manifest file in the order
     *     they were added. Highest priority added first, lowest added last.</li>
     *     <li>Resulting document is returned as results of the merging process.</li>
     * </ol>
     *
     */
    public static final class Invoker {

        /**
         * Optional behavior of the merging tool can be turned on by setting these Feature.
         */
        public enum Feature {

            /**
             * Keep all intermediary merged files during the merging process. This is particularly
             * useful for debugging/tracing purposes.
             */
            KEEP_INTERMEDIARY_STAGES,

            /**
             * When logging file names, use {@link File#getName()} rather than
             * {@link File#getPath()}
             */
            PRINT_SIMPLE_FILENAMES
        }

        private final File mMainManifestFile;
        private final ImmutableList.Builder<Pair<String, File>> mLibraryFilesBuilder =
                new ImmutableList.Builder<Pair<String, File>>();
        private final ImmutableList.Builder<File> mFlavorsAndBuildTypeFiles =
                new ImmutableList.Builder<File>();
        private final ImmutableList.Builder<Feature> mFeaturesBuilder =
                new ImmutableList.Builder<Feature>();
        private final ImmutableMap.Builder<SystemProperty, String> mSystemProperties =
                new ImmutableMap.Builder<SystemProperty, String>();
        private final ImmutableMap.Builder<String, String> mPlaceHolders =
                new ImmutableMap.Builder<String, String>();
        private final ILogger mLogger;


        /**
         * Creates a new builder with the mandatory main manifest file.
         * @param mainManifestFile application main manifest file.
         * @param logger the logger interface to use.
         */
        private Invoker(@NonNull File mainManifestFile, @NonNull ILogger logger) {
            this.mMainManifestFile = Preconditions.checkNotNull(mainManifestFile);
            this.mLogger = logger;
        }

        /**
         * Add one library file manifest, will be added last in the list of library files which will
         * make the parameter the lowest priority library manifest file.
         * @param file the library manifest file to add.
         * @return itself.
         */
        public Invoker addLibraryManifest(File file) {
            mLibraryFilesBuilder.add(Pair.of(file.getName(), file));
            return this;
        }

        public Invoker addLibraryManifests(List<Pair<String, File>> namesAndFiles) {
            mLibraryFilesBuilder.addAll(namesAndFiles);
            return this;
        }

        /**
         * Add several library file manifests at then end of the list which will make them the
         * lowest priority manifest files. The relative priority between all the files passed as
         * parameters will be respected.
         * @param files library manifest files to add last.
         * @return itself.
         */
        public Invoker addLibraryManifests(File... files) {
            for (File file : files) {
                addLibraryManifest(file);
            }
            return this;
        }

        /**
         * Add a flavor or build type manifest file last in the list.
         * @param file build type or flavor manifest file
         * @return itself.
         */
        public Invoker addFlavorAndBuildTypeManifest(File file) {
            this.mFlavorsAndBuildTypeFiles.add(file);
            return this;
        }

        /**
         * Add several flavor or build type manifest files last in the list. Relative priorities
         * between the passed files as parameters will be respected.
         * @param files build type of flavor manifest files to add.
         * @return itself.
         */
        public Invoker addFlavorAndBuildTypeManifests(File... files) {
            this.mFlavorsAndBuildTypeFiles.add(files);
            return this;
        }

        /**
         * Sets some optional features for the merge tool.
         *
         * @param features one to many features to set.
         * @return itself.
         */
        public Invoker withFeatures(Feature...features) {
            mFeaturesBuilder.add(features);
            return this;
        }

        /**
         * Adds a new placeholder name and value for substitution.
         * @return itself.
         */
        public Invoker setPlaceHolderValue(String placeHolderName, String value) {
            mPlaceHolders.put(placeHolderName, value);
            return this;
        }

        /**
         * Sets a value for a {@link SystemProperty}
         * @param override the property to set
         * @param value the value for the property
         * @return itself.
         */
        public Invoker setOverride(SystemProperty override, String value) {
            mSystemProperties.put(override, value);
            return this;
        }

        /**
         * Perform the merging and return the result.
         *
         * @return an instance of {@link com.android.manifmerger.MergingReport} that will give
         * access to all the logging and merging records.
         *
         * This method can be invoked several time and will re-do the file merges.
         *
         * @throws MergeFailureException if the merging cannot be completed successfully.
         */
        public MergingReport merge() throws MergeFailureException {

            // provide some free placeholders values.
            ImmutableMap<SystemProperty, String> systemProperties = mSystemProperties.build();
            if (systemProperties.containsKey(SystemProperty.PACKAGE)) {
                mPlaceHolders.put("packageName", systemProperties.get(SystemProperty.PACKAGE));
            }

            ManifestMerger2 manifestMerger =
                    new ManifestMerger2(
                            mLogger,
                            mMainManifestFile,
                            mLibraryFilesBuilder.build(),
                            mFlavorsAndBuildTypeFiles.build(),
                            mFeaturesBuilder.build(),
                            new MapBasedKeyBasedValueResolver<String>(mPlaceHolders.build()),
                            new MapBasedKeyBasedValueResolver<SystemProperty>(
                                    systemProperties));
            return manifestMerger.merge();
        }
    }

    /**
     * Helper class for map based placeholders key value pairs.
     */
    public static class MapBasedKeyBasedValueResolver<T> implements KeyBasedValueResolver<T> {

        private final ImmutableMap<T, String> keyValues;

        public MapBasedKeyBasedValueResolver(Map<T, String> keyValues) {
            this.keyValues = ImmutableMap.copyOf(keyValues);
        }

        @Nullable
        @Override
        public String getValue(@NonNull T key) {
            return keyValues.get(key);
        }
    }
}
