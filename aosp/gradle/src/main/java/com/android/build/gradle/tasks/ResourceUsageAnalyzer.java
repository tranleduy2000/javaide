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

package com.android.build.gradle.tasks;

import static com.android.SdkConstants.ANDROID_STYLE_RESOURCE_PREFIX;
import static com.android.SdkConstants.ANDROID_URI;
import static com.android.SdkConstants.ATTR_NAME;
import static com.android.SdkConstants.ATTR_PARENT;
import static com.android.SdkConstants.ATTR_TYPE;
import static com.android.SdkConstants.DOT_CLASS;
import static com.android.SdkConstants.DOT_GIF;
import static com.android.SdkConstants.DOT_JPEG;
import static com.android.SdkConstants.DOT_JPG;
import static com.android.SdkConstants.DOT_PNG;
import static com.android.SdkConstants.DOT_XML;
import static com.android.SdkConstants.FD_RES_VALUES;
import static com.android.SdkConstants.PREFIX_ANDROID;
import static com.android.SdkConstants.STYLE_RESOURCE_PREFIX;
import static com.android.SdkConstants.TAG_ITEM;
import static com.android.SdkConstants.TAG_RESOURCES;
import static com.android.SdkConstants.TAG_STYLE;
import static com.android.SdkConstants.TOOLS_URI;
import static com.android.utils.SdkUtils.endsWith;
import static com.android.utils.SdkUtils.endsWithIgnoreCase;
import static com.google.common.base.Charsets.UTF_8;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.ClassReader.SKIP_FRAMES;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.android.ide.common.resources.ResourceUrl;
import com.android.ide.common.resources.configuration.DensityQualifier;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.ide.common.resources.configuration.ResourceQualifier;
import com.android.ide.common.xml.XmlPrettyPrinter;
import com.android.resources.FolderTypeRelationship;
import com.android.resources.ResourceFolderType;
import com.android.resources.ResourceType;
import com.android.tools.lint.checks.StringFormatDetector;
import com.android.tools.lint.client.api.DefaultConfiguration;
import com.android.tools.lint.detector.api.LintUtils;
import com.android.utils.XmlUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Class responsible for searching through a Gradle built tree (after resource merging,
 * compilation and ProGuarding has been completed, but before final .apk assembly), which
 * figures out which resources if any are unused, and removes them.
 * <p>
 * It does this by examining
 * <ul>
 *     <li>The merged manifest, to find root resource references (such as drawables
 *         used for activity icons)</li>
 *     <li>The merged R class (to find the actual integer constants assigned to resources)</li>
 *     <li>The ProGuard log files (to find the mapping from original symbol names to
 *         short names)</li>*
 *     <li>The merged resources (to find which resources reference other resources, e.g.
 *         drawable state lists including other drawables, or layouts including other
 *         layouts, or styles referencing other drawables, or menus items including action
 *         layouts, etc.)</li>
 *     <li>The ProGuard output classes (to find resource references in code that are
 *         actually reachable)</li>
 * </ul>
 * From all this, it builds up a reference graph, and based on the root references (e.g.
 * from the manifest and from the remaining code) it computes which resources are actually
 * reachable in the app, and anything that is not reachable is then marked for deletion.
 * <p>
 * A resource is referenced in code if either the field R.type.name is referenced (which
 * is the case for non-final resource references, e.g. in libraries), or if the corresponding
 * int value is referenced (for final resource values). We check this by looking at the
 * ProGuard output classes with an ASM visitor. One complication is that code can also
 * call {@code Resources#getIdentifier(String,String,String)} where they can pass in the names
 * of resources to look up. To handle this scenario, we use the ClassVisitor to see if
 * there are any calls to the specific {@code Resources#getIdentifier} method. If not,
 * great, the usage analysis is completely accurate. If we <b>do</b> find one, we check
 * <b>all</b> the string constants found anywhere in the app, and look to see if any look
 * relevant. For example, if we find the string "string/foo" or "my.pkg:string/foo", we
 * will then mark the string resource named foo (if any) as potentially used. Similarly,
 * if we find just "foo" or "/foo", we will mark <b>all</b> resources named "foo" as
 * potentially used. However, if the string is "bar/foo" or " foo " these strings are
 * ignored. This means we can potentially miss resources usages where the resource name
 * is completed computed (e.g. by concatenating individual characters or taking substrings
 * of strings that do not look like resource names), but that seems extremely unlikely
 * to be a real-world scenario.
 * <p>
 * For now, for reasons detailed in the code, this only applies to file-based resources
 * like layouts, menus and drawables, not value-based resources like strings and dimensions.
 */
public class ResourceUsageAnalyzer {
    private static final String ANDROID_RES = "android_res/";

    /**
     Whether we support running aapt twice, to regenerate the resources.arsc file
     such that we can strip out value resources as well. We don't do this yet, for
     reasons detailed in the ShrinkResources task

     We have two options:
     (1) Copy the resource files over to a new destination directory, filtering out
     removed file resources and rewriting value resource files by stripping out
     the declarations for removed value resources. We then re-run aapt on this
     new destination directory.

     The problem with this approach is that when we re-run aapt it will assign new
     id's to all the resources, so we have to create dummy placeholders for all the
     removed resources. (The alternative would be to then run compilation one more
     time -- regenerating classes.jar, regenerating .dex) -- this would really slow
     down builds.)

     A cleaner solution than this is to get aapt to support using a predefined set
     of id's. It can emit R.txt symbol files now; if we can get it to read R.txt
     and use those numbers in its assignment, we can solve this cleanly. This request
     is tracked in https://code.google.com/p/android/issues/detail?id=70869

     (2) Just rewrite the .ap_ file directly. It's just a .zip file which contains
     (a) binary files for bitmaps and XML file resources such as layouts and menus
     (b) a binary file, resources.arsc, containing all the values.
     The resources.arsc format is opaque to us. However, MOST of the resource bulk
     comes from the bitmap and other resource files.

     So here we don't even need to run aapt a second time; we simply rewrite the
     .ap_ zip file directly, filtering out res/ files we know to be unused.

     Approach #2 gives us most of the space savings without the risk of #1 (running aapt
     a second time introduces the possibility of aapt compilation errors if we haven't
     been careful enough to insert resource aliases for all necessary items (such as
     inline @+id declarations), or if we haven't carefully not created aliases for items
     already defined in other value files as aliases, and perhaps most importantly,
     introduces risk that aapt will pick a different resource order anyway, which we can
     only guard against by doing a full compilation over again.

     Therefore, for now the below code uses #2, but since we can solve #1 with support
     from aapt), we're preserving all the code to rewrite resource files since that will
     give additional space savings, particularly for apps with a lot of strings or a lot
     of translations.
     */
    @SuppressWarnings("SpellCheckingInspection") // arsc
    public static final boolean TWO_PASS_AAPT = false;
    public static final int TYPICAL_RESOURCE_COUNT = 200;

    /** Name of keep attribute in XML */
    private static final String ATTR_KEEP = "keep";
    /** Name of discard attribute in XML (to mark resources as not referenced, despite guesses) */
    private static final String ATTR_DISCARD = "discard";
    /** Name of attribute in XML to control whether we should guess resources to keep */
    private static final String ATTR_SHRINK_MODE = "shrinkMode";
    /** @{linkplain #ATTR_SHRINK_MODE} value to only shrink explicitly encountered resources */
    private static final String VALUE_STRICT = "strict";
    /** @{linkplain #ATTR_SHRINK_MODE} value to keep possibly referenced resources */
    private static final String VALUE_SAFE = "safe";

    /** Special marker regexp which does not match a resource name */
    static final String NO_MATCH = "-nomatch-";

    private final File mResourceClassDir;
    private final File mProguardMapping;
    private final File mClassesJar;
    private final File mMergedManifest;
    private final File mMergedResourceDir;

    private boolean mVerbose;
    private boolean mDebug;
    private boolean mDryRun;

    /** The computed set of unused resources */
    private List<Resource> mUnused;

    /** List of all known resources (parsed from R.java) */
    private List<Resource> mResources = Lists.newArrayListWithExpectedSize(TYPICAL_RESOURCE_COUNT);
    /** Map from R field value to corresponding resource */
    private Map<Integer, Resource> mValueToResource =
            Maps.newHashMapWithExpectedSize(TYPICAL_RESOURCE_COUNT);
    /** Map from resource type to map from resource name to resource object */
    private Map<ResourceType, Map<String, Resource>> mTypeToName =
            Maps.newEnumMap(ResourceType.class);
    /** Map from resource class owners (VM format class) to corresponding resource types.
     * This will typically be the fully qualified names of the R classes, as well as
     * any renamed versions of those discovered in the mapping.txt file from ProGuard */
    private Map<String, ResourceType> mResourceClassOwners = Maps.newHashMapWithExpectedSize(20);

    /**
     * Whether we should attempt to guess resources that should be kept based on looking
     * at the string pool and assuming some of the strings can be used to dynamically construct
     * the resource names. Can be turned off via {@code tools:guessKeep="false"}.
     */
    private boolean mGuessKeep = true;

    public ResourceUsageAnalyzer(
            @NonNull File rDir,
            @NonNull File classesJar,
            @NonNull File manifest,
            @Nullable File mapping,
            @NonNull File resources) {
        mResourceClassDir = rDir;
        mProguardMapping = mapping;
        mClassesJar = classesJar;
        mMergedManifest = manifest;
        mMergedResourceDir = resources;
    }

    public void analyze() throws IOException, ParserConfigurationException, SAXException {
        gatherResourceValues(mResourceClassDir);
        recordMapping(mProguardMapping);
        recordUsages(mClassesJar);
        recordManifestUsages(mMergedManifest);
        recordResources(mMergedResourceDir);
        keepPossiblyReferencedResources();
        dumpReferences();
        findUnused();
    }

    public boolean isDryRun() {
        return mDryRun;
    }

    public void setDryRun(boolean dryRun) {
        mDryRun = dryRun;
    }

    public boolean isVerbose() {
        return mVerbose;
    }

    public void setVerbose(boolean verbose) {
        mVerbose = verbose;
    }


    public boolean isDebug() {
        return mDebug;
    }

    public void setDebug(boolean verbose) {
        mDebug = verbose;
    }

    /**
     * "Removes" resources from an .ap_ file by writing it out while filtering out
     * unused resources. This won't touch the values XML data (resources.arsc) but
     * will remove the individual file-based resources, which is where most of
     * the data is anyway (usually in drawable bitmaps)
     *
     * @param source the .ap_ file created by aapt
     * @param dest a new .ap_ file with unused file-based resources removed
     */
    public void rewriteResourceZip(@NonNull File source, @NonNull File dest)
            throws IOException {
        if (dest.exists()) {
            boolean deleted = dest.delete();
            if (!deleted) {
                throw new IOException("Could not delete " + dest);
            }
        }

        JarInputStream zis = null;
        try {
            FileInputStream fis = new FileInputStream(source);
            try {
                FileOutputStream fos = new FileOutputStream(dest);
                zis = new JarInputStream(fis);
                JarOutputStream zos = new JarOutputStream(fos);
                try {
                    // Rather than using Deflater.DEFAULT_COMPRESSION we use 9 here,
                    // since that seems to match the compressed sizes we observe in source
                    // .ap_ files encountered by the resource shrinker:
                    zos.setLevel(9);

                    ZipEntry entry = zis.getNextEntry();
                    while (entry != null) {
                        String name = entry.getName();
                        boolean directory = entry.isDirectory();
                        Resource resource = getResourceByJarPath(name);
                        if (resource == null || resource.reachable) {
                            // We can't just compress all files; files that are not
                            // compressed in the source .ap_ file must be left uncompressed
                            // here, since for example RAW files need to remain uncompressed in
                            // the APK such that they can be mmap'ed at runtime.
                            // Preserve the STORED method of the input entry.
                            JarEntry outEntry;
                            if (entry.getMethod() == JarEntry.STORED) {
                                outEntry = new JarEntry(entry);
                            } else {
                                // Create a new entry so that the compressed len is recomputed.
                                outEntry = new JarEntry(name);
                                if (entry.getTime() != -1L) {
                                    outEntry.setTime(entry.getTime());
                                }
                            }

                            zos.putNextEntry(outEntry);

                            if (!directory) {
                                byte[] bytes = ByteStreams.toByteArray(zis);
                                if (bytes != null) {
                                    zos.write(bytes);
                                }
                            }

                            zos.closeEntry();
                        } else if (isVerbose()) {
                            System.out.println("Skipped unused resource " + name + ": "
                                    + entry.getSize() + " bytes");
                        }
                        entry = zis.getNextEntry();
                    }
                    zos.flush();
                } finally {
                    Closeables.close(zos, false);
                }
            } finally {
                Closeables.close(fis, true);
            }
        } finally {
            Closeables.close(zis, false);
        }
    }

    /**
     * Remove resources (already identified by {@link #analyze()}).
     *
     * This task will copy all remaining used resources over from the full resource
     * directory to a new reduced resource directory. However, it can't just
     * delete the resources, because it has no way to tell aapt to continue to use
     * the same id's for the resources. When we re-run aapt on the stripped resource
     * directory, it will assign new id's to some of the resources (to fill the gaps)
     * which means the resource id's no longer match the constants compiled into the
     * dex files, and as a result, the app crashes at runtime.
     * <p>
     * Therefore, it needs to preserve all id's by actually keeping all the resource
     * names. It can still save a lot of space by making these resources tiny; e.g.
     * all strings are set to empty, all styles, arrays and plurals are set to not contain
     * any children, and most importantly, all file based resources like bitmaps and
     * layouts are replaced by simple resource aliases which just point to @null.
     *
     * @param destination directory to copy resources into; if null, delete resources in place
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public void removeUnused(@Nullable File destination) throws IOException,
            ParserConfigurationException, SAXException {
        if (TWO_PASS_AAPT) {
            assert mUnused != null; // should always call analyze() first

            int resourceCount = mUnused.size()
                    * 4; // *4: account for some resource folder repetition
            boolean inPlace = destination == null;
            Set<File> skip = inPlace ? null : Sets.<File>newHashSetWithExpectedSize(resourceCount);
            Set<File> rewrite = Sets.newHashSetWithExpectedSize(resourceCount);
            for (Resource resource : mUnused) {
                if (resource.declarations != null) {
                    for (File file : resource.declarations) {
                        String folder = file.getParentFile().getName();
                        ResourceFolderType folderType = ResourceFolderType.getFolderType(folder);
                        if (folderType != null && folderType != ResourceFolderType.VALUES) {
                            if (isVerbose()) {
                                System.out.println("Deleted unused resource " + file);
                            }
                            if (inPlace) {
                                if (!isDryRun()) {
                                    boolean delete = file.delete();
                                    if (!delete) {
                                        System.err.println("Could not delete " + file);
                                    }
                                }
                            } else {
                                assert skip != null;
                                skip.add(file);
                            }
                        } else {
                            // Can't delete values immediately; there can be many resources
                            // in this file, so we have to process them all
                            rewrite.add(file);
                        }
                    }
                }
            }

            // Special case the base values.xml folder
            File values = new File(mMergedResourceDir,
                    FD_RES_VALUES + File.separatorChar + "values.xml");
            boolean valuesExists = values.exists();
            if (valuesExists) {
                rewrite.add(values);
            }

            Map<File, String> rewritten = Maps.newHashMapWithExpectedSize(rewrite.size());

            // Delete value resources: Must rewrite the XML files
            for (File file : rewrite) {
                String xml = Files.toString(file, UTF_8);
                Document document = XmlUtils.parseDocument(xml, true);
                Element root = document.getDocumentElement();
                if (root != null && TAG_RESOURCES.equals(root.getTagName())) {
                    List<String> removed = Lists.newArrayList();
                    stripUnused(root, removed);
                    if (isVerbose()) {
                        System.out.println("Removed " + removed.size() +
                                " unused resources from " + file + ":\n  " +
                                Joiner.on(", ").join(removed));
                    }

                    String formatted = XmlPrettyPrinter.prettyPrint(document, xml.endsWith("\n"));
                    rewritten.put(file, formatted);
                }
            }

            if (isDryRun()) {
                return;
            }

            if (valuesExists) {
                String xml = rewritten.get(values);
                if (xml == null) {
                    xml = Files.toString(values, UTF_8);
                }
                Document document = XmlUtils.parseDocument(xml, true);
                Element root = document.getDocumentElement();

                for (Resource resource : mResources) {
                    if (resource.type == ResourceType.ID && !resource.hasDefault) {
                        Element item = document.createElement(TAG_ITEM);
                        item.setAttribute(ATTR_TYPE, resource.type.getName());
                        item.setAttribute(ATTR_NAME, resource.name);
                        root.appendChild(item);
                    } else if (!resource.reachable
                            && !resource.hasDefault
                            && resource.type != ResourceType.DECLARE_STYLEABLE
                            && resource.type != ResourceType.STYLE
                            && resource.type != ResourceType.PLURALS
                            && resource.type != ResourceType.ARRAY
                            && resource.isRelevantType()) {
                        Element item = document.createElement(TAG_ITEM);
                        item.setAttribute(ATTR_TYPE, resource.type.getName());
                        item.setAttribute(ATTR_NAME, resource.name);
                        root.appendChild(item);
                        String s = "@null";
                        item.appendChild(document.createTextNode(s));
                    }
                }

                String formatted = XmlPrettyPrinter.prettyPrint(document, xml.endsWith("\n"));
                rewritten.put(values, formatted);
            }

            if (inPlace) {
                for (Map.Entry<File, String> entry : rewritten.entrySet()) {
                    File file = entry.getKey();
                    String formatted = entry.getValue();
                    Files.write(formatted, file, UTF_8);
                }
            } else {
                filteredCopy(mMergedResourceDir, destination, skip, rewritten);
            }
        } else {
            assert false;
        }
    }

    /**
     * Copies one resource directory tree into another; skipping some files, replacing
     * the contents of some, and passing everything else through unmodified
     */
    private static void filteredCopy(File source, File destination, Set<File> skip,
            Map<File, String> replace) throws IOException {
        if (TWO_PASS_AAPT) {
            if (source.isDirectory()) {
                File[] children = source.listFiles();
                if (children != null) {
                    if (!destination.exists()) {
                        boolean success = destination.mkdirs();
                        if (!success) {
                            throw new IOException("Could not create " + destination);
                        }
                    }
                    for (File child : children) {
                        filteredCopy(child, new File(destination, child.getName()), skip, replace);
                    }
                }
            } else if (!skip.contains(source) && source.isFile()) {
                String contents = replace.get(source);
                if (contents != null) {
                    Files.write(contents, destination, UTF_8);
                } else {
                    Files.copy(source, destination);
                }
            }
        } else {
            assert false;
        }
    }

    private void stripUnused(Element element, List<String> removed) {
        if (TWO_PASS_AAPT) {
            ResourceType type = getResourceType(element);
            if (type == ResourceType.ATTR) {
                // Not yet properly handled
                return;
            }

            Resource resource = getResource(element);
            if (resource != null) {
                if (resource.type == ResourceType.DECLARE_STYLEABLE ||
                        resource.type == ResourceType.ATTR) {
                    // Don't strip children of declare-styleable; we're not correctly
                    // tracking field references of the R_styleable_attr fields yet
                    return;
                }

                if (!resource.reachable &&
                        (resource.type == ResourceType.STYLE ||
                                resource.type == ResourceType.PLURALS ||
                                resource.type == ResourceType.ARRAY)) {
                    NodeList children = element.getChildNodes();
                    for (int i = children.getLength() - 1; i >= 0; i--) {
                        Node child = children.item(i);
                        element.removeChild(child);
                    }
                    return;
                }
            }

            NodeList children = element.getChildNodes();
            for (int i = children.getLength() - 1; i >= 0; i--) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    stripUnused((Element) child, removed);
                }
            }

            if (resource != null && !resource.reachable) {
                if (mVerbose) {
                    removed.add(resource.getUrl());
                }
                // for themes etc where .'s have been replaced by _'s
                String name = element.getAttribute(ATTR_NAME);
                if (name.isEmpty()) {
                    name = resource.name;
                }
                Node nextSibling = element.getNextSibling();
                Node parent = element.getParentNode();
                NodeList oldChildren = element.getChildNodes();
                parent.removeChild(element);
                Document document = element.getOwnerDocument();
                element = document.createElement("item");
                for (int i = 0; i < oldChildren.getLength(); i++) {
                    element.appendChild(oldChildren.item(i));
                }

                element.setAttribute(ATTR_NAME, name);
                element.setAttribute(ATTR_TYPE, resource.type.getName());
                String text = null;
                switch (resource.type) {
                    case BOOL:
                        text = "true";
                        break;
                    case DIMEN:
                        text = "0dp";
                        break;
                    case INTEGER:
                        text = "0";
                        break;
                }
                element.setTextContent(text);
                parent.insertBefore(element, nextSibling);
            }
        } else {
            assert false;
        }
    }

    private static String getFieldName(Element element) {
        return getFieldName(element.getAttribute(ATTR_NAME));
    }

    @Nullable
    private Resource getResource(Element element) {
        ResourceType type = getResourceType(element);
        if (type != null) {
            String name = getFieldName(element);
            return getResource(type, name);
        }

        return null;
    }

    @Nullable
    private Resource getResourceByJarPath(String path) {
        // Jars use forward slash paths, not File.separator
        if (path.startsWith("res/")) {
            int folderStart = 4; // "res/".length
            int folderEnd = path.indexOf('/', folderStart);
            if (folderEnd != -1) {
                String folderName = path.substring(folderStart, folderEnd);
                ResourceFolderType folderType = ResourceFolderType.getFolderType(folderName);
                if (folderType != null) {
                    int nameStart = folderEnd + 1;
                    int nameEnd = path.indexOf('.', nameStart);
                    if (nameEnd != -1) {
                        String name = path.substring(nameStart, nameEnd);
                        List<ResourceType> types =
                                FolderTypeRelationship.getRelatedResourceTypes(folderType);
                        for (ResourceType type : types) {
                            if (type != ResourceType.ID) {
                                Resource resource = getResource(type, name);
                                if (resource != null) {
                                    return resource;
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private static ResourceType getResourceType(Element element) {
        String tagName = element.getTagName();
        if (tagName.equals(TAG_ITEM)) {
            String typeName = element.getAttribute(ATTR_TYPE);
            if (!typeName.isEmpty()) {
                return ResourceType.getEnum(typeName);
            }
        } else if ("string-array".equals(tagName) || "integer-array".equals(tagName)) {
            return ResourceType.ARRAY;
        } else {
            return ResourceType.getEnum(tagName);
        }
        return null;
    }

    private void findUnused() {
        List<Resource> roots = Lists.newArrayList();

        for (Resource resource : mResources) {
            if (resource.reachable && resource.type != ResourceType.ID
                    && resource.type != ResourceType.ATTR) {
                roots.add(resource);
            }
        }

        if (mDebug) {
            System.out.println("The root reachable resources are:\n" +
                    Joiner.on(",\n   ").join(roots));
        }

        Map<Resource,Boolean> seen = new IdentityHashMap<Resource,Boolean>(mResources.size());
        for (Resource root : roots) {
            visit(root, seen);
        }

        List<Resource> unused = Lists.newArrayListWithExpectedSize(mResources.size());
        for (Resource resource : mResources) {
            if (!resource.reachable && resource.isRelevantType()) {
                unused.add(resource);
            }
        }

        mUnused = unused;

        if (mDebug) {
            System.out.println(dumpResourceModel());
        }
    }

    private static void visit(Resource root, Map<Resource, Boolean> seen) {
        if (seen.containsKey(root)) {
            return;
        }
        seen.put(root, Boolean.TRUE);
        root.reachable = true;
        if (root.references != null) {
            for (Resource referenced : root.references) {
                visit(referenced, seen);
            }
        }
    }

    private void dumpReferences() {
        if (mDebug) {
            System.out.println("Resource Reference Graph:");
            for (Resource resource : mResources) {
                if (resource.references != null) {
                    System.out.println(resource + " => " + resource.references);
                }
            }
        }
    }

    private void keepPossiblyReferencedResources() {
        if ((!mFoundGetIdentifier && !mFoundWebContent) || mStrings == null) {
            // No calls to android.content.res.Resources#getIdentifier; no need
            // to worry about string references to resources
            return;
        }

        if (!mGuessKeep) {
            // User specifically asked for us not to guess resources to keep; they will
            // explicitly mark them as kept if necessary instead
            return;
        }

        if (mDebug) {
            List<String> strings = new ArrayList<String>(mStrings);
            Collections.sort(strings);
            System.out.println("android.content.res.Resources#getIdentifier present: "
                    + mFoundGetIdentifier);
            System.out.println("Web content present: " + mFoundWebContent);
            System.out.println("Referenced Strings:");
            for (String s : strings) {
                s = s.trim().replace("\n", "\\n");
                if (s.length() > 40) {
                    s = s.substring(0, 37) + "...";
                } else if (s.isEmpty()) {
                    continue;
                }
                System.out.println("  " + s);
            }
        }

        int shortest = Integer.MAX_VALUE;
        Set<String> names = Sets.newHashSetWithExpectedSize(50);
        for (Map<String, Resource> map : mTypeToName.values()) {
            for (String name : map.keySet()) {
                names.add(name);
                int length = name.length();
                if (length < shortest) {
                    shortest = length;
                }
            }
        }

        for (String string : mStrings) {
            if (string.length() < shortest) {
                continue;
            }

            // Check whether the string looks relevant
            // We consider four types of strings:
            //  (1) simple resource names, e.g. "foo" from @layout/foo
            //      These might be the parameter to a getIdentifier() call, or could
            //      be composed into a fully qualified resource name for the getIdentifier()
            //      method. We match these for *all* resource types.
            //  (2) Relative source names, e.g. layout/foo, from @layout/foo
            //      These might be composed into a fully qualified resource name for
            //      getIdentifier().
            //  (3) Fully qualified resource names of the form package:type/name.
            //  (4) If mFoundWebContent is true, look for android_res/ URL strings as well

            if (mFoundWebContent) {
                Resource resource = getResourceFromFilePath(string);
                if (resource != null) {
                    markReachable(resource);
                    continue;
                } else {
                    int start = 0;
                    int slash = string.lastIndexOf('/');
                    if (slash != -1) {
                        start = slash + 1;
                    }
                    int dot = string.indexOf('.', start);
                    String name = string.substring(start, dot != -1 ? dot : string.length());
                    if (names.contains(name)) {
                        for (Map<String, Resource> map : mTypeToName.values()) {
                            resource = map.get(name);
                            if (mDebug && resource != null) {
                                System.out.println("Marking " + resource + " used because it "
                                        + "matches string pool constant " + string);
                            }
                            markReachable(resource);
                        }
                    }
                }
            }

            // Look for normal getIdentifier resource URLs
            int n = string.length();
            boolean justName = true;
            boolean formatting = false;
            boolean haveSlash = false;
            for (int i = 0; i < n; i++) {
                char c = string.charAt(i);
                if (c == '/') {
                    haveSlash = true;
                    justName = false;
                } else if (c == '.' || c == ':' || c == '%') {
                    justName = false;
                    if (c == '%') {
                        formatting = true;
                    }
                } else if (!Character.isJavaIdentifierPart(c)) {
                    // This shouldn't happen; we've filtered out these strings in
                    // the {@link #referencedString} method
                    assert false : string;
                    break;
                }
            }

            String name;
            if (justName) {
                // Check name (below)
                name = string;

                // Check for a simple prefix match, e.g. as in
                // getResources().getIdentifier("ic_video_codec_" + codecName, "drawable", ...)
                for (Map<String, Resource> map : mTypeToName.values()) {
                    for (Resource resource : map.values()) {
                        if (resource.name.startsWith(name)) {
                            if (mDebug) {
                                System.out.println("Marking " + resource + " used because its "
                                        + "prefix matches string pool constant " + string);
                            }
                            markReachable(resource);
                        }
                    }
                }
            } else if (!haveSlash) {
                if (formatting) {
                    // Possibly a formatting string, e.g.
                    //   String name = String.format("my_prefix_%1d", index);
                    //   int res = getContext().getResources().getIdentifier(name, "drawable", ...)

                    try {
                        Pattern pattern = Pattern.compile(convertFormatStringToRegexp(string));
                        for (Map<String, Resource> map : mTypeToName.values()) {
                            for (Resource resource : map.values()) {
                                if (pattern.matcher(resource.name).matches()) {
                                    if (mDebug) {
                                        System.out.println("Marking " + resource + " used because "
                                                + "it format-string matches string pool constant "
                                                + string);
                                    }
                                    markReachable(resource);
                                }
                            }
                        }
                    } catch (PatternSyntaxException ignored) {
                        // Might not have been a formatting string after all!
                    }
                }

                // If we have more than just a symbol name, we expect to also see a slash
                //noinspection UnnecessaryContinue
                continue;
            } else {
                // Try to pick out the resource name pieces; if we can find the
                // resource type unambiguously; if not, just match on names
                int slash = string.indexOf('/');
                assert slash != -1; // checked with haveSlash above
                name = string.substring(slash + 1);
                if (name.isEmpty() || !names.contains(name)) {
                    continue;
                }
                // See if have a known specific resource type
                if (slash > 0) {
                    int colon = string.indexOf(':');
                    String typeName = string.substring(colon != -1 ? colon + 1 : 0, slash);
                    ResourceType type = ResourceType.getEnum(typeName);
                    if (type == null) {
                        continue;
                    }
                    Resource resource = getResource(type, name);
                    if (mDebug && resource != null) {
                        System.out.println("Marking " + resource + " used because it "
                                + "matches string pool constant " + string);
                    }
                    markReachable(resource);
                    continue;
                }

                // fall through and check the name
            }

            if (names.contains(name)) {
                for (Map<String, Resource> map : mTypeToName.values()) {
                    Resource resource = map.get(name);
                    if (mDebug && resource != null) {
                        System.out.println("Marking " + resource + " used because it "
                                + "matches string pool constant " + string);
                    }
                    markReachable(resource);
                }
            } else if (Character.isDigit(name.charAt(0))) {
                // Just a number? There are cases where it calls getIdentifier by
                // a String number; see for example SuggestionsAdapter in the support
                // library which reports supporting a string like "2130837524" and
                // "android.resource://com.android.alarmclock/2130837524".
                try {
                    int id = Integer.parseInt(name);
                    if (id != 0) {
                        markReachable(mValueToResource.get(id));
                    }
                } catch (NumberFormatException e) {
                    // pass
                }
            }
        }
    }

    @VisibleForTesting
    static String convertFormatStringToRegexp(String formatString) {
        StringBuilder regexp = new StringBuilder();
        int from = 0;
        boolean hasEscapedLetters = false;
        Matcher matcher = StringFormatDetector.FORMAT.matcher(formatString);
        int length = formatString.length();
        while (matcher.find(from)) {
            int start = matcher.start();
            int end = matcher.end();
            if (start == 0 && end == length) {
                // Don't match if the entire string literal starts with % and ends with
                // the a formatting character, such as just "%d": this just matches absolutely
                // everything and is unlikely to be used in a resource lookup
                return NO_MATCH;
            }
            if (start > from) {
                hasEscapedLetters |= appendEscapedPattern(formatString, regexp, from, start);
            }
            // If the wildcard follows a previous wildcard, just skip it
            // (e.g. don't convert %s%s into .*.*; .* is enough.
            int regexLength = regexp.length();
            if (regexLength < 2
                    || regexp.charAt(regexLength - 1) != '*'
                    || regexp.charAt(regexLength - 2) != '.') {
                regexp.append(".*");
            }
            from = end;
        }

        if (from < length) {
            hasEscapedLetters |= appendEscapedPattern(formatString, regexp, from, length);
        }

        if (!hasEscapedLetters) {
            // If the regexp contains *only* formatting characters, e.g. "%.0f%d", or
            // if it contains only formatting characters and punctuation, e.g. "%s_%d",
            // don't treat this as a possible resource name pattern string: it is unlikely
            // to be intended for actual resource names, and has the side effect of matching
            // most names.
            return NO_MATCH;
        }

        return regexp.toString();
    }

    /**
     * Appends the characters in the range [from,to> from formatString as escaped
     * regexp characters into the given string builder. Returns true if there were
     * any letters in the appended text.
     */
    private static boolean appendEscapedPattern(@NonNull String formatString,
            @NonNull StringBuilder regexp, int from, int to) {
        regexp.append(Pattern.quote(formatString.substring(from, to)));

        for (int i = from; i < to; i++) {
            if (Character.isLetter(formatString.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    private void recordResources(File resDir)
            throws IOException, SAXException, ParserConfigurationException {
        File[] resourceFolders = resDir.listFiles();
        if (resourceFolders != null) {
            for (File folder : resourceFolders) {
                ResourceFolderType folderType = ResourceFolderType.getFolderType(folder.getName());
                if (folderType != null) {
                    recordResources(folderType, folder);
                }
            }
        }
    }

    private void recordResources(@NonNull ResourceFolderType folderType, File folder)
            throws ParserConfigurationException, SAXException, IOException {
        File[] files = folder.listFiles();
        FolderConfiguration config = FolderConfiguration.getConfigForFolder(folder.getName());
        boolean isDefaultFolder = false;
        if (config != null) {
            isDefaultFolder = true;
            for (int i = 0, n = FolderConfiguration.getQualifierCount(); i < n; i++) {
                ResourceQualifier qualifier = config.getQualifier(i);
                // Densities are special: even if they're present in just (say) drawable-hdpi
                // we'll match it on any other density
                if (qualifier != null && !(qualifier instanceof DensityQualifier)) {
                    isDefaultFolder = false;
                    break;
                }
            }
        }

        if (files != null) {
            for (File file : files) {
                String path = file.getPath();
                boolean isXml = endsWithIgnoreCase(path, DOT_XML);

                Resource from = null;
                // Record resource for the whole file
                if (folderType != ResourceFolderType.VALUES
                        && (isXml
                            || endsWith(path, DOT_PNG) //also true for endsWith(name, DOT_9PNG)
                            || endsWith(path, DOT_JPG)
                            || endsWith(path, DOT_GIF)
                            || endsWith(path, DOT_JPEG))) {
                    List<ResourceType> types = FolderTypeRelationship.getRelatedResourceTypes(
                            folderType);
                    ResourceType type = types.get(0);
                    assert type != ResourceType.ID : folderType;
                    String name = file.getName();
                    name = name.substring(0, name.indexOf('.'));
                    Resource resource = getResource(type, name);
                    if (resource != null) {
                        resource.addLocation(file);
                        if (isDefaultFolder) {
                            resource.hasDefault = true;
                        }
                        from = resource;
                    }
                }

                if (isXml) {
                    // For value files, and drawables and colors etc also pull in resource
                    // references inside the file
                    recordXmlResourcesUsages(file, isDefaultFolder, from);
                    if (folderType == ResourceFolderType.XML) {
                        tokenizeUnknownText(Files.toString(file, UTF_8));
                    }
                } else if (folderType == ResourceFolderType.RAW) {
                    // Is this an HTML, CSS or JavaScript document bundled with the app?
                    // If so tokenize and look for resource references.
                    if (endsWithIgnoreCase(path, ".html") || endsWithIgnoreCase(path, ".htm")) {
                        tokenizeHtml(from, Files.toString(file, UTF_8));
                    } else if (endsWithIgnoreCase(path, ".css")) {
                        tokenizeCss(from, Files.toString(file, UTF_8));
                    } else if (endsWithIgnoreCase(path, ".js")) {
                        tokenizeJs(from, Files.toString(file, UTF_8));
                    } else if (file.isFile() && !LintUtils.isBitmapFile(file)) {
                        tokenizeUnknownBinary(file);
                    }
                }
            }
        }
    }

    private void recordMapping(@Nullable File mapping) throws IOException {
        if (mapping == null || !mapping.exists()) {
            return;
        }
        final String ARROW = " -> ";
        final String RESOURCE = ".R$";
        for (String line : Files.readLines(mapping, UTF_8)) {
            if (line.startsWith(" ") || line.startsWith("\t")) {
                continue;
            }
            int index = line.indexOf(RESOURCE);
            if (index == -1) {
                continue;
            }
            int arrow = line.indexOf(ARROW, index + 3);
            if (arrow == -1) {
                continue;
            }
            String typeName = line.substring(index + RESOURCE.length(), arrow);
            ResourceType type = ResourceType.getEnum(typeName);
            if (type == null) {
                continue;
            }
            int end = line.indexOf(':', arrow + ARROW.length());
            if (end == -1) {
                end = line.length();
            }
            String target = line.substring(arrow + ARROW.length(), end).trim();
            String ownerName = target.replace('.', '/');
            mResourceClassOwners.put(ownerName, type);
        }
    }

    private void recordManifestUsages(File manifest)
            throws IOException, ParserConfigurationException, SAXException {
        String xml = Files.toString(manifest, UTF_8);
        Document document = XmlUtils.parseDocument(xml, true);
        recordManifestUsages(document.getDocumentElement());
    }

    private void recordXmlResourcesUsages(@NonNull File file, boolean isDefaultFolder,
            @Nullable Resource from)
            throws IOException, ParserConfigurationException, SAXException {
        String xml = Files.toString(file, UTF_8);
        Document document = XmlUtils.parseDocument(xml, true);
        recordResourceReferences(file, isDefaultFolder, document.getDocumentElement(), from);
    }

    private void tokenizeHtml(@Nullable Resource from, @NonNull String  html) {
        // Look for
        //    (1) URLs of the form /android_res/drawable/foo.ext
        //        which we will use to keep R.drawable.foo
        // and
        //    (2) Filenames. If the web content is loaded with something like
        //        WebView.loadDataWithBaseURL("file:///android_res/drawable/", ...)
        //        this is similar to Resources#getIdentifier handling where all
        //        *potentially* aliased filenames are kept to play it safe.

        // Simple HTML tokenizer
        int length = html.length();
        final int STATE_TEXT = 1;
        final int STATE_SLASH = 2;
        final int STATE_ATTRIBUTE_NAME = 3;
        final int STATE_BEFORE_TAG = 4;
        final int STATE_IN_TAG = 5;
        final int STATE_BEFORE_ATTRIBUTE = 6;
        final int STATE_ATTRIBUTE_BEFORE_EQUALS = 7;
        final int STATE_ATTRIBUTE_AFTER_EQUALS = 8;
        final int STATE_ATTRIBUTE_VALUE_NONE = 9;
        final int STATE_ATTRIBUTE_VALUE_SINGLE = 10;
        final int STATE_ATTRIBUTE_VALUE_DOUBLE = 11;
        final int STATE_CLOSE_TAG = 12;

        int state = STATE_TEXT;
        int offset = 0;
        int valueStart = 0;
        int tagStart = 0;
        String tag = null;
        String attribute = null;
        int attributeStart = 0;
        int prev = -1;
        while (offset < length) {
            if (offset == prev) {
                // Purely here to prevent potential bugs in the state machine from looping
                // infinitely
                offset++;
            }
            prev = offset;


            char c = html.charAt(offset);

            // MAke sure I handle doctypes properly.
            // Make sure I handle cdata properly.
            // Oh and what about <style> tags? tokenize everything inside as CSS!
            // ANd <script> tag content as js!
            switch (state) {
                case STATE_TEXT: {
                    if (c == '<') {
                        state = STATE_SLASH;
                        offset++;
                        continue;
                    }

                    // Other text is just ignored
                    offset++;
                    break;
                }

                case STATE_SLASH: {
                    if (c == '!') {
                        if (html.startsWith("!--", offset)) {
                            // Comment
                            int end = html.indexOf("-->", offset + 3);
                            if (end == -1) {
                                offset = length;
                                break;
                            }
                            offset = end + 3;
                            continue;
                        } else if (html.startsWith("![CDATA[", offset)) {
                            // Skip CDATA text content; HTML text is irrelevant to this tokenizer
                            // anyway
                            int end = html.indexOf("]]>", offset + 8);
                            if (end == -1) {
                                offset = length;
                                break;
                            }
                            offset = end + 3;
                            continue;
                        }
                    } else if (c == '/') {
                        state = STATE_CLOSE_TAG;
                        offset++;
                        continue;
                    } else if (c == '?') {
                        // XML Prologue
                        int end = html.indexOf('>', offset + 2);
                        if (end == -1) {
                            offset = length;
                            break;
                        }
                        offset = end + 1;
                        continue;
                    }
                    state = STATE_IN_TAG;
                    tagStart = offset;
                    break;
                }

                case STATE_CLOSE_TAG: {
                    if (c == '>') {
                        state = STATE_TEXT;
                    }
                    offset++;
                    break;
                }

                case STATE_BEFORE_TAG: {
                    if (!Character.isWhitespace(c)) {
                        state = STATE_IN_TAG;
                        tagStart = offset;
                    }
                    // (For an end tag we'll include / in the tag name here)
                    offset++;
                    break;
                }
                case STATE_IN_TAG: {
                    if (Character.isWhitespace(c)) {
                        state = STATE_BEFORE_ATTRIBUTE;
                        tag = html.substring(tagStart, offset).trim();
                    } else if (c == '>') {
                        tag = html.substring(tagStart, offset).trim();
                        endHtmlTag(from, html, offset, tag);
                        state = STATE_TEXT;
                    }
                    offset++;
                    break;
                }
                case STATE_BEFORE_ATTRIBUTE: {
                    if (c == '>') {
                        endHtmlTag(from, html, offset, tag);
                        state = STATE_TEXT;
                    } else //noinspection StatementWithEmptyBody
                        if (c == '/') {
                        // we expect an '>' next to close the tag
                    } else if (!Character.isWhitespace(c)) {
                        state = STATE_ATTRIBUTE_NAME;
                        attributeStart = offset;
                    }
                    offset++;
                    break;
                }
                case STATE_ATTRIBUTE_NAME: {
                    if (c == '>') {
                        endHtmlTag(from, html, offset, tag);
                        state = STATE_TEXT;
                    } else if (c == '=') {
                        attribute = html.substring(attributeStart, offset);
                        state = STATE_ATTRIBUTE_AFTER_EQUALS;
                    } else if (Character.isWhitespace(c)) {
                        attribute = html.substring(attributeStart, offset);
                        state = STATE_ATTRIBUTE_BEFORE_EQUALS;
                    }
                    offset++;
                    break;
                }
                case STATE_ATTRIBUTE_BEFORE_EQUALS: {
                    if (c == '=') {
                        state = STATE_ATTRIBUTE_AFTER_EQUALS;
                    } else if (c == '>') {
                        endHtmlTag(from, html, offset, tag);
                        state = STATE_TEXT;
                    } else if (!Character.isWhitespace(c)) {
                        // Attribute value not specified (used for some boolean attributes)
                        state = STATE_ATTRIBUTE_NAME;
                        attributeStart = offset;
                    }
                    offset++;
                    break;
                }

                case STATE_ATTRIBUTE_AFTER_EQUALS: {
                    if (c == '\'') {
                        // a='b'
                        state = STATE_ATTRIBUTE_VALUE_SINGLE;
                        valueStart = offset + 1;
                    } else if (c == '"') {
                        // a="b"
                        state = STATE_ATTRIBUTE_VALUE_DOUBLE;
                        valueStart = offset + 1;
                    } else if (!Character.isWhitespace(c)) {
                        // a=b
                        state = STATE_ATTRIBUTE_VALUE_NONE;
                        valueStart = offset + 1;
                    }
                    offset++;
                    break;
                }

                case STATE_ATTRIBUTE_VALUE_SINGLE: {
                    if (c == '\'') {
                        state = STATE_BEFORE_ATTRIBUTE;
                        recordHtmlAttributeValue(from, tag, attribute,
                                html.substring(valueStart, offset));
                    }
                    offset++;
                    break;
                }
                case STATE_ATTRIBUTE_VALUE_DOUBLE: {
                    if (c == '"') {
                        state = STATE_BEFORE_ATTRIBUTE;
                        recordHtmlAttributeValue(from, tag, attribute,
                                html.substring(valueStart, offset));
                    }
                    offset++;
                    break;
                }
                case STATE_ATTRIBUTE_VALUE_NONE: {
                    if (c == '>') {
                        recordHtmlAttributeValue(from, tag, attribute,
                                html.substring(valueStart, offset));
                        endHtmlTag(from, html, offset, tag);
                        state = STATE_TEXT;
                    } else if (Character.isWhitespace(c)) {
                        state = STATE_BEFORE_ATTRIBUTE;
                        recordHtmlAttributeValue(from, tag, attribute,
                                html.substring(valueStart, offset));
                    }
                    offset++;
                    break;
                }
                default:
                    assert false : state;
            }
        }
    }

    private void endHtmlTag(@Nullable Resource from, @NonNull String html, int offset,
            @Nullable String tag) {
        if ("script".equals(tag)) {
            int end = html.indexOf("</script>", offset + 1);
            if (end != -1) {
                // Attempt to tokenize the text as JavaScript
                String js = html.substring(offset + 1, end);
                tokenizeJs(from, js);
            }
        } else if ("style".equals(tag)) {
            int end = html.indexOf("</style>", offset + 1);
            if (end != -1) {
                // Attempt to tokenize the text as CSS
                String css = html.substring(offset + 1, end);
                tokenizeCss(from, css);
            }
        }
    }

    private void tokenizeJs(@Nullable Resource from, @NonNull String js) {
        // Simple JavaScript tokenizer: only looks for literal strings,
        // and records those as string references
        int length = js.length();
        final int STATE_INIT = 1;
        final int STATE_SLASH = 2;
        final int STATE_STRING_DOUBLE = 3;
        final int STATE_STRING_DOUBLE_QUOTED = 4;
        final int STATE_STRING_SINGLE = 5;
        final int STATE_STRING_SINGLE_QUOTED = 6;

        int state = STATE_INIT;
        int offset = 0;
        int stringStart = 0;
        int prev = -1;
        while (offset < length) {
            if (offset == prev) {
                // Purely here to prevent potential bugs in the state machine from looping
                // infinitely
                offset++;
            }
            prev = offset;

            char c = js.charAt(offset);
            switch (state) {
                case STATE_INIT: {
                    if (c == '/') {
                        state = STATE_SLASH;
                    } else if (c == '"') {
                        stringStart = offset + 1;
                        state = STATE_STRING_DOUBLE;
                    } else if (c == '\'') {
                        stringStart = offset + 1;
                        state = STATE_STRING_SINGLE;
                    }
                    offset++;
                    break;
                }
                case STATE_SLASH: {
                    if (c == '*') {
                        // Comment block
                        state = STATE_INIT;
                        int end = js.indexOf("*/", offset + 1);
                        if (end == -1) {
                            offset = length; // unterminated
                            break;
                        }
                        offset = end + 2;
                        continue;
                    } else if (c == '/') {
                        // Line comment
                        state = STATE_INIT;
                        int end = js.indexOf('\n', offset + 1);
                        if (end == -1) {
                            offset = length;
                            break;
                        }
                        offset = end + 1;
                        continue;
                    } else {
                        // division - just continue
                        state = STATE_INIT;
                        offset++;
                        break;
                    }
                }
                case STATE_STRING_DOUBLE: {
                    if (c == '"') {
                        recordJsString(js.substring(stringStart, offset));
                        state = STATE_INIT;
                    } else if (c == '\\') {
                        state = STATE_STRING_DOUBLE_QUOTED;
                    }
                    offset++;
                    break;
                }
                case STATE_STRING_DOUBLE_QUOTED: {
                    state = STATE_STRING_DOUBLE;
                    offset++;
                    break;
                }
                case STATE_STRING_SINGLE: {
                    if (c == '\'') {
                        recordJsString(js.substring(stringStart, offset));
                        state = STATE_INIT;
                    } else if (c == '\\') {
                        state = STATE_STRING_SINGLE_QUOTED;
                    }
                    offset++;
                    break;
                }
                case STATE_STRING_SINGLE_QUOTED: {
                    state = STATE_STRING_SINGLE;
                    offset++;
                    break;
                }
                default:
                    assert false : state;
            }
        }
    }

    private void tokenizeCss(@Nullable Resource from, @NonNull String  css) {
        // Simple CSS tokenizer: Only looks for URL references, and records those
        // filenames. Skips everything else (unrelated to images).
        int length = css.length();
        final int STATE_INIT = 1;
        final int STATE_SLASH = 2;
        int state = STATE_INIT;
        int offset = 0;
        int prev = -1;
        while (offset < length) {
            if (offset == prev) {
                // Purely here to prevent potential bugs in the state machine from looping
                // infinitely
                offset++;
            }
            prev = offset;

            char c = css.charAt(offset);
            switch (state) {
                case STATE_INIT: {
                    if (c == '/') {
                        state = STATE_SLASH;
                    } else if (c == 'u' && css.startsWith("url(", offset) && offset > 0) {
                        char prevChar = css.charAt(offset-1);
                        if (Character.isWhitespace(prevChar) || prevChar == ':') {
                            int end = css.indexOf(')', offset);
                            offset += 4; // skip url(
                            while (offset < length && Character.isWhitespace(css.charAt(offset))) {
                                offset++;
                            }
                            if (end != -1 && end > offset + 1) {
                                while (end > offset
                                        && Character.isWhitespace(css.charAt(end - 1))) {
                                    end--;
                                }
                                if ((css.charAt(offset) == '"'
                                        && css.charAt(end - 1) == '"')
                                        || (css.charAt(offset) == '\''
                                        && css.charAt(end - 1) == '\'')) {
                                    // Strip " or '
                                    offset++;
                                    end--;
                                }
                                recordCssUrl(from, css.substring(offset, end).trim());
                            }
                            offset = end + 1;
                            continue;
                        }

                    }
                    offset++;
                    break;
                }
                case STATE_SLASH: {
                    if (c == '*') {
                        // CSS comment? Skip the whole block rather than staying within the
                        // character tokenizer.
                        int end = css.indexOf("*/", offset + 1);
                        if (end == -1) {
                            offset = length;
                            break;
                        }
                        offset = end + 2;
                        continue;
                    }
                    state = STATE_INIT;
                    offset++;
                    break;
                }
                default:
                    assert false : state;
            }
        }
    }

    private static byte[] sAndroidResBytes;

    /** Look through binary/unknown files looking for resource URLs */
    private void tokenizeUnknownBinary(@NonNull File file) {
        try {
            if (sAndroidResBytes == null) {
                sAndroidResBytes = ANDROID_RES.getBytes(SdkConstants.UTF_8);
            }
            byte[] bytes = Files.toByteArray(file);
            int index = 0;
            while (index != -1) {
                index = indexOf(bytes, sAndroidResBytes, index);
                if (index != -1) {
                    index += sAndroidResBytes.length;

                    // Find the end of the URL
                    int begin = index;
                    int end = begin;
                    for (; end < bytes.length; end++) {
                        byte c = bytes[end];
                        if (c != '/' && !Character.isJavaIdentifierPart((char)c)) {
                            // android_res/raw/my_drawable.png => @raw/my_drawable
                            String url = "@" + new String(bytes, begin, end - begin, UTF_8);
                            markReachable(getResourceFromUrl(url));
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Ignore
        }
    }

    /**
     * Returns the index of the given target array in the first array, looking from the given
     * index
     */
    private static int indexOf(byte[] array, byte[] target, int fromIndex) {
        outer:
        for (int i = fromIndex; i < array.length - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    /** Look through text files of unknown structure looking for resource URLs */
    private void tokenizeUnknownText(@NonNull String text) {
        int index = 0;
        while (index != -1) {
            index = text.indexOf(ANDROID_RES, index);
            if (index != -1) {
                index += ANDROID_RES.length();

                // Find the end of the URL
                int begin = index;
                int end = begin;
                int length = text.length();
                for (; end < length; end++) {
                    char c = text.charAt(end);
                    if (c != '/' && !Character.isJavaIdentifierPart(c)) {
                        // android_res/raw/my_drawable.png => @raw/my_drawable
                        markReachable(getResourceFromUrl("@" + text.substring(begin, end)));
                        break;
                    }
                }
            }
        }
    }

    private void recordCssUrl(@Nullable Resource from, @NonNull String value) {
        if (!referencedUrl(from, value)) {
            referencedString(value);
            mFoundWebContent = true;
        }
    }

    /**
     * See if the given URL is a URL that we can resolve to a specific resource; if so,
     * record it and return true, otherwise returns false.
     */
    private boolean referencedUrl(@Nullable Resource from, @NonNull String url) {
        Resource resource = getResourceFromFilePath(url);
        if (resource != null) {
            if (from != null) {
                from.addReference(resource);
            } else {
                // We don't have an inclusion context, so just assume this resource is reachable
                markReachable(resource);
            }
            return true;
        }

        return false;
    }

    private void recordHtmlAttributeValue(@Nullable Resource from, @Nullable String tagName,
            @Nullable String attribute, @NonNull String value) {
        if ("href".equals(attribute) || "src".equals(attribute)) {
            // In general we'd need to unescape the HTML here (e.g. remove entities) but
            // those wouldn't be valid characters in the resource name anyway
            if (!referencedUrl(from, value)) {
                referencedString(value);
                mFoundWebContent = true;
            }

            // If this document includes another, record the reachability of that script/resource
            if (from != null) {
                from.addReference(getResourceFromFilePath(attribute));
            }
        }
    }

    private void recordJsString(@NonNull String string) {
        referencedString(string);
    }

    @Nullable
    private Resource getResource(@NonNull ResourceType type, @NonNull String name) {
        Map<String, Resource> nameMap = mTypeToName.get(type);
        if (nameMap != null) {
            return nameMap.get(getFieldName(name));
        }
        return null;
    }

    @Nullable
    private Resource getResourceFromUrl(@NonNull String possibleUrlReference) {
        ResourceUrl url = ResourceUrl.parse(possibleUrlReference);
        if (url != null && !url.framework) {
            return getResource(url.type, url.name);
        }

        return null;
    }

    @Nullable
    private Resource getResourceFromFilePath(@NonNull String url) {
        int nameSlash = url.lastIndexOf('/');
        if (nameSlash == -1) {
            return null;
        }

        // Look for
        //   (1) a full resource URL: /android_res/type/name.ext
        //   (2) a partial URL that uniquely identifies a given resource: drawable/name.ext
        // e.g. file:///android_res/drawable/bar.png
        int androidRes = url.indexOf(ANDROID_RES);
        if (androidRes != -1) {
            androidRes += ANDROID_RES.length();
            int slash = url.indexOf('/', androidRes);
            if (slash != -1) {
                String folderName = url.substring(androidRes, slash);
                ResourceFolderType folderType = ResourceFolderType.getFolderType(folderName);
                if (folderType != null) {
                    List<ResourceType> types = FolderTypeRelationship.getRelatedResourceTypes(
                            folderType);
                    if (!types.isEmpty()) {
                        ResourceType type = types.get(0);
                        int nameBegin = slash + 1;
                        int dot = url.indexOf('.', nameBegin);
                        String name = url.substring(nameBegin, dot != -1 ? dot : url.length());
                        return getResource(type, name);
                    }
                }
            }
        }

        // Some other relative path. Just look from the end:
        int typeSlash = url.lastIndexOf('/', nameSlash - 1);
        ResourceType type = ResourceType.getEnum(url.substring(typeSlash + 1, nameSlash));
        if (type != null) {
            int nameBegin = nameSlash + 1;
            int dot = url.indexOf('.', nameBegin);
            String name = url.substring(nameBegin, dot != -1 ? dot : url.length());
            return getResource(type, name);
        }

        return null;
    }

    private void recordManifestUsages(Node node) {
        short nodeType = node.getNodeType();
        if (nodeType == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            NamedNodeMap attributes = element.getAttributes();
            for (int i = 0, n = attributes.getLength(); i < n; i++) {
                Attr attr = (Attr) attributes.item(i);
                markReachable(getResourceFromUrl(attr.getValue()));
            }
        } else if (nodeType == Node.TEXT_NODE) {
            // Does this apply to any manifests??
            String text = node.getNodeValue().trim();
            markReachable(getResourceFromUrl(text));
        }

        NodeList children = node.getChildNodes();
        for (int i = 0, n = children.getLength(); i < n; i++) {
            Node child = children.item(i);
            recordManifestUsages(child);
        }
    }


    private void recordResourceReferences(@NonNull File file, boolean isDefaultFolder,
            @NonNull Node node, @Nullable Resource from) {
        short nodeType = node.getNodeType();
        if (nodeType == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            if (from != null) {
                NamedNodeMap attributes = element.getAttributes();
                for (int i = 0, n = attributes.getLength(); i < n; i++) {
                    Attr attr = (Attr) attributes.item(i);

                    // Ignore tools: namespace attributes, unless it's
                    // a keep attribute
                    if (TOOLS_URI.equals(attr.getNamespaceURI())) {
                        handleToolsAttribute(attr);
                        // Skip all other tools: attributes
                        continue;
                    }

                    Resource resource = getResourceFromUrl(attr.getValue());
                    if (resource != null) {
                        from.addReference(resource);
                    }
                }

                // Android Wear. We *could* limit ourselves to only doing this in files
                // referenced from a manifest meta-data element, e.g.
                // <meta-data android:name="com.google.android.wearable.beta.app"
                //    android:resource="@xml/wearable_app_desc"/>
                // but given that that property has "beta" in the name, it seems likely
                // to change and therefore hardcoding it for that key risks breakage
                // in the future.
                if ("rawPathResId".equals(element.getTagName())) {
                    StringBuilder sb = new StringBuilder();
                    NodeList children = node.getChildNodes();
                    for (int i = 0, n = children.getLength(); i < n; i++) {
                        Node child = children.item(i);
                        if (child.getNodeType() == Element.TEXT_NODE
                                || child.getNodeType() == Element.CDATA_SECTION_NODE) {
                            sb.append(child.getNodeValue());
                        }
                    }
                    if (sb.length() > 0) {
                        Resource resource = getResource(ResourceType.RAW, sb.toString().trim());
                        from.addReference(resource);
                    }
                }
            } else {
                // Look for keep attributes everywhere else since they don't require a source
                handleToolsAttribute(element.getAttributeNodeNS(TOOLS_URI, ATTR_KEEP));
                handleToolsAttribute(element.getAttributeNodeNS(TOOLS_URI, ATTR_DISCARD));
                handleToolsAttribute(element.getAttributeNodeNS(TOOLS_URI, ATTR_SHRINK_MODE));
            }

            Resource definition = getResource(element);
            if (definition != null) {
                from = definition;
                definition.addLocation(file);
                if (isDefaultFolder) {
                    definition.hasDefault = true;
                }
            }

            String tagName = element.getTagName();
            if (TAG_STYLE.equals(tagName)) {
                if (element.hasAttribute(ATTR_PARENT)) {
                    String parent = element.getAttribute(ATTR_PARENT);
                    if (!parent.isEmpty() && !parent.startsWith(ANDROID_STYLE_RESOURCE_PREFIX) &&
                            !parent.startsWith(PREFIX_ANDROID)) {
                        String parentStyle = parent;
                        if (!parentStyle.startsWith(STYLE_RESOURCE_PREFIX)) {
                            parentStyle = STYLE_RESOURCE_PREFIX + parentStyle;
                        }
                        Resource ps = getResourceFromUrl(getFieldName(parentStyle));
                        if (ps != null && definition != null) {
                            definition.addReference(ps);
                        }
                    }
                } else {
                    // Implicit parent styles by name
                    String name = getFieldName(element);
                    while (true) {
                        int index = name.lastIndexOf('_');
                        if (index != -1) {
                            name = name.substring(0, index);
                            Resource ps = getResourceFromUrl(
                                    STYLE_RESOURCE_PREFIX + getFieldName(name));
                            if (ps != null && definition != null) {
                                definition.addReference(ps);
                            }
                        } else {
                            break;
                        }
                    }
                }
            }

            if (TAG_ITEM.equals(tagName)) {
                // In style? If so the name: attribute can be a reference
                if (element.getParentNode() != null
                        && element.getParentNode().getNodeName().equals(TAG_STYLE)) {
                    String name = element.getAttributeNS(ANDROID_URI, ATTR_NAME);
                    if (!name.isEmpty() && !name.startsWith("android:")) {
                        Resource resource = getResource(ResourceType.ATTR, name);
                        if (definition == null) {
                            Element style = (Element) element.getParentNode();
                            definition = getResource(style);
                            if (definition != null) {
                                from = definition;
                                definition.addReference(resource);
                            }
                        }
                    }
                }
            }
        } else if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE) {
            String text = node.getNodeValue().trim();
            Resource textResource = getResourceFromUrl(getFieldName(text));
            if (textResource != null && from != null) {
                from.addReference(textResource);
            }
        }

        NodeList children = node.getChildNodes();
        for (int i = 0, n = children.getLength(); i < n; i++) {
            Node child = children.item(i);
            recordResourceReferences(file, isDefaultFolder, child, from);
        }
    }

    private void handleToolsAttribute(@Nullable Attr attr) {
        if (attr == null) {
            return;
        }
        String localName = attr.getLocalName();
        String value = attr.getValue();
        if (ATTR_KEEP.equals(localName)) {
            handleKeepAttribute(value);
        } else if (ATTR_DISCARD.equals(localName)) {
            handleRemoveAttribute(value);
        } else if (ATTR_SHRINK_MODE.equals(localName)) {
            if (VALUE_STRICT.equals(value)) {
                mGuessKeep = false;
            } else if (VALUE_SAFE.equals(value)) {
                mGuessKeep = true;
            } else if (mDebug) {
                System.out.println("Ignoring unknown " + ATTR_SHRINK_MODE + " " + value);
            }
            if (mDebug) {
                System.out.println("Setting shrink mode to " + value);
            }
        }
    }

    public static String getFieldName(@NonNull String styleName) {
        return styleName.replace('.', '_').replace('-', '_').replace(':', '_');
    }

    /**
     * Marks the given resource (if non-null) as reachable, and returns true if
     * this is the first time the resource is marked reachable
     */
    private static boolean markReachable(@Nullable Resource resource) {
        if (resource != null) {
            boolean wasReachable = resource.reachable;
            resource.reachable = true;
            return !wasReachable;
        }

        return false;
    }

    private static void markUnreachable(@Nullable Resource resource) {
        if (resource != null) {
            resource.reachable = false;
        }
    }

    /**
     * Called for a tools:keep attribute containing a resource URL where that resource name
     * is not referencing a known resource
     *
     * @param value The keep value
     */
    private void handleKeepAttribute(@NonNull String value) {
        // Handle comma separated lists of URLs and globs
        if (value.indexOf(',') != -1) {
            for (String portion : Splitter.on(',').omitEmptyStrings().trimResults().split(value)) {
                handleKeepAttribute(portion);
            }
            return;
        }

        ResourceUrl url = ResourceUrl.parse(value);
        if (url == null || url.framework) {
            return;
        }

        Resource resource = getResource(url.type, url.name);
        if (resource != null) {
            if (mDebug) {
                System.out.println("Marking " + resource + " used because it "
                        + "matches keep attribute " + value);
            }
            markReachable(resource);
        } else if (url.name.contains("*") || url.name.contains("?")) {
            // Look for globbing patterns
            String regexp = DefaultConfiguration.globToRegexp(getFieldName(url.name));
            try {
                Pattern pattern = Pattern.compile(regexp);
                Map<String, Resource> nameMap = mTypeToName.get(url.type);
                if (nameMap != null) {
                    for (Resource r : nameMap.values()) {
                        if (pattern.matcher(r.name).matches()) {
                            if (mDebug) {
                                System.out.println("Marking " + r + " used because it "
                                        + "matches keep globbing pattern " + url.name);
                            }

                            markReachable(r);
                        }
                    }
                }
            } catch (PatternSyntaxException ignored) {
                if (mDebug) {
                    System.out.println("Could not compute keep globbing pattern for " +
                            url.name + ": tried regexp " + regexp + "(" + ignored + ")");
                }
            }
        }
    }

    private void handleRemoveAttribute(@NonNull String value) {
        // Handle comma separated lists of URLs and globs
        if (value.indexOf(',') != -1) {
            for (String portion : Splitter.on(',').omitEmptyStrings().trimResults().split(value)) {
                handleRemoveAttribute(portion);
            }
            return;
        }

        ResourceUrl url = ResourceUrl.parse(value);
        if (url == null || url.framework) {
            return;
        }

        Resource resource = getResource(url.type, url.name);
        if (resource != null) {
            if (mDebug) {
                System.out.println("Marking " + resource + " used because it "
                        + "matches remove attribute " + value);
            }
            markUnreachable(resource);
        } else if (url.name.contains("*") || url.name.contains("?")) {
            // Look for globbing patterns
            String regexp = DefaultConfiguration.globToRegexp(getFieldName(url.name));
            try {
                Pattern pattern = Pattern.compile(regexp);
                Map<String, Resource> nameMap = mTypeToName.get(url.type);
                if (nameMap != null) {
                    for (Resource r : nameMap.values()) {
                        if (pattern.matcher(r.name).matches()) {
                            if (mDebug) {
                                System.out.println("Marking " + r + " used because it "
                                        + "matches remove globbing pattern " + url.name);
                            }

                            markUnreachable(r);
                        }
                    }
                }
            } catch (PatternSyntaxException ignored) {
                if (mDebug) {
                    System.out.println("Could not compute remove globbing pattern for " +
                            url.name + ": tried regexp " + regexp + "(" + ignored + ")");
                }
            }
        }
    }

    private Set<String> mStrings;
    private boolean mFoundGetIdentifier;
    private boolean mFoundWebContent;

    private void referencedString(@NonNull String string) {
        // See if the string is at all eligible; ignore strings that aren't
        // identifiers (has java identifier chars and nothing but .:/), or are empty or too long
        // We also allow "%", used for formatting strings.
        if (string.isEmpty() || string.length() > 80) {
            return;
        }
        boolean haveIdentifierChar = false;
        for (int i = 0, n = string.length(); i < n; i++) {
            char c = string.charAt(i);
            boolean identifierChar = Character.isJavaIdentifierPart(c);
            if (!identifierChar && c != '.' && c != ':' && c != '/' && c != '%') {
                // .:/ are for the fully qualified resource names, or for resource URLs or
                // relative file names
                return;
            } else if (identifierChar) {
                haveIdentifierChar = true;
            }
        }
        if (!haveIdentifierChar) {
            return;
        }

        if (mStrings == null) {
            mStrings = Sets.newHashSetWithExpectedSize(300);
        }
        mStrings.add(string);

        if (!mFoundWebContent && string.contains(ANDROID_RES)) {
            mFoundWebContent = true;
        }
    }

    private void recordUsages(File jarFile) throws IOException {
        if (!jarFile.exists()) {
            return;
        }
        ZipInputStream zis = null;
        try {
            FileInputStream fis = new FileInputStream(jarFile);
            try {
                zis = new ZipInputStream(fis);
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    String name = entry.getName();
                    if (name.endsWith(DOT_CLASS) &&
                            // Skip resource type classes like R$drawable; they will
                            // reference the integer id's we're looking for, but these aren't
                            // actual usages we need to track; if somebody references the
                            // field elsewhere, we'll catch that
                            !isResourceClass(name)) {
                        byte[] bytes = ByteStreams.toByteArray(zis);
                        if (bytes != null) {
                            ClassReader classReader = new ClassReader(bytes);
                            classReader.accept(new UsageVisitor(jarFile, name),
                                    SKIP_DEBUG | SKIP_FRAMES);
                        }
                    }

                    entry = zis.getNextEntry();
                }
            } finally {
                Closeables.close(fis, true);
            }
        } finally {
            Closeables.close(zis, true);
        }
    }

    /** Returns whether the given class path points to an aapt-generated compiled R class */
    @VisibleForTesting
    static boolean isResourceClass(@NonNull String name) {
        assert name.endsWith(DOT_CLASS) : name;
        int index = name.lastIndexOf('/');
        if (index != -1 && name.startsWith("R$", index + 1)) {
            String typeName = name.substring(index + 3, name.length() - DOT_CLASS.length());
            return ResourceType.getEnum(typeName) != null;
        }

        return false;
    }

    private void gatherResourceValues(File file) throws IOException {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    gatherResourceValues(child);
                }
            }
        } else if (file.isFile() && file.getName().equals(SdkConstants.FN_RESOURCE_CLASS)) {
            parseResourceClass(file);
        }
    }

    // TODO: Use Lombok/ECJ here
    private void parseResourceClass(File file) throws IOException {
        String s = Files.toString(file, UTF_8);
        // Simple parser which handles only aapt's special R output
        String pkg = null;
        int index = s.indexOf("package ");
        if (index != -1) {
            int end = s.indexOf(';', index);
            pkg = s.substring(index + "package ".length(), end).trim().replace('.', '/');
        }
        index = 0;
        int length = s.length();
        String classDeclaration = "public static final class ";
        while (true) {
            index = s.indexOf(classDeclaration, index);
            if (index == -1) {
                break;
            }
            int start = index + classDeclaration.length();
            int end = s.indexOf(' ', start);
            if (end == -1) {
                break;
            }
            String typeName = s.substring(start, end);
            ResourceType type = ResourceType.getEnum(typeName);
            if (type == null) {
                break;
            }

            if (pkg != null) {
                mResourceClassOwners.put(pkg + "/R$" + type.getName(), type);
            }

            index = end;

            // Find next declaration
            for (; index < length - 1; index++) {
                char c = s.charAt(index);
                if (Character.isWhitespace(c)) {
                    //noinspection UnnecessaryContinue
                    continue;
                } else if (c == '/') {
                    char next = s.charAt(index + 1);
                    if (next == '*') {
                        // Scan forward to comment end
                        end = index + 2;
                        while (end < length -2) {
                            c = s.charAt(end);
                            if (c == '*' && s.charAt(end + 1) == '/') {
                                end++;
                                break;
                            } else {
                                end++;
                            }
                        }
                        index = end;
                    } else if (next == '/') {
                        // Scan forward to next newline
                        assert false : s.substring(index - 1, index + 50); // we don't put line comments in R files
                    } else {
                        assert false : s.substring(index - 1, index + 50); // unexpected division
                    }
                } else if (c == 'p' && s.startsWith("public ", index)) {
                    if (type == ResourceType.STYLEABLE) {
                        start = s.indexOf(" int", index);
                        if (s.startsWith(" int[] ", start)) {
                            end = s.indexOf('=', start);
                            assert end != -1;
                            String styleable = s.substring(start, end).trim();
                            addResource(ResourceType.DECLARE_STYLEABLE, styleable, null);

                            // TODO: Read in all the action bar ints!
                            // For now, we're simply treating all R.attr fields as used
                        } else if (s.startsWith(" int ")) {
                            // Read these fields in and correlate with the attr R's. Actually
                            // we don't need this for anything; the local attributes are
                            // found by the R attr thing. I just need to record the class
                            // (style).
                            // public static final int ActionBar_background = 10;
                            // ignore - jump to end
                            index = s.indexOf(';', index);
                            if (index == -1) {
                                break;
                            }
                            // For now, we're simply treating all R.attr fields as used
                        }
                    } else {
                        start = s.indexOf(" int ", index);
                        if (start != -1) {
                            start += " int ".length();
                            // e.g. abc_fade_in=0x7f040000;
                            end = s.indexOf('=', start);
                            assert end != -1;
                            String name = s.substring(start, end).trim();
                            start = end + 1;
                            end = s.indexOf(';', start);
                            assert end != -1;
                            String value = s.substring(start, end).trim();
                            addResource(type, name, value);
                        }
                    }
                } else if (c == '}') {
                    // Done with resource class
                    break;
                }
            }
        }
    }

    private void addResource(@NonNull ResourceType type, @NonNull String name,
            @Nullable String value) {
        int realValue = value != null ? Integer.decode(value) : -1;
        Resource resource = getResource(type, name);
        if (resource != null) {
            //noinspection VariableNotUsedInsideIf
            if (value != null) {
                if (resource.value == -1) {
                    resource.value = realValue;
                } else {
                    assert realValue == resource.value;
                }
            }
            return;
        }

        resource = new Resource(type, name, realValue);
        mResources.add(resource);
        if (realValue != -1) {
            mValueToResource.put(realValue, resource);
        }
        Map<String, Resource> nameMap = mTypeToName.get(type);
        if (nameMap == null) {
            nameMap = Maps.newHashMapWithExpectedSize(30);
            mTypeToName.put(type, nameMap);
        }
        nameMap.put(name, resource);

        // TODO: Assert that we don't set the same resource multiple times to different values.
        // Could happen if you pass in stale data!
    }

    public int getUnusedResourceCount() {
        return mUnused.size();
    }

    @VisibleForTesting
    List<Resource> getAllResources() {
        return mResources;
    }

    public static class Resource {
        /** Type of resource */
        public ResourceType type;
        /** Name of resource */
        public String name;
        /** Integer id location */
        public int value;
        /** Whether this resource can be reached from one of the roots (manifest, code) */
        public boolean reachable;
        /** Whether this resource has a default definition (e.g. present in a resource folder
         * with no qualifiers). For id references, an inline definition (@+id) does not count as
         * a default definition.*/
        public boolean hasDefault;
        /** Resources this resource references. For example, a layout can reference another via
         * an include; a style reference in a layout references that layout style, and so on. */
        public List<Resource> references;
        public final List<File> declarations = Lists.newArrayList();

        private Resource(ResourceType type, String name, int value) {
            this.type = type;
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return type + ":" + name + ":" + value;
        }

        @SuppressWarnings("RedundantIfStatement") // Generated by IDE
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Resource resource = (Resource) o;

            if (name != null ? !name.equals(resource.name) : resource.name != null) {
                return false;
            }
            if (type != resource.type) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }

        public void addLocation(@NonNull File file) {
            declarations.add(file);
        }

        public void addReference(@Nullable Resource resource) {
            if (resource != null) {
                if (references == null) {
                    references = Lists.newArrayList();
                } else if (references.contains(resource)) {
                    return;
                }
                references.add(resource);
            }
        }

        public String getUrl() {
            return '@' + type.getName() + '/' + name;
        }

        public boolean isRelevantType() {
            return type != ResourceType.ID; // && getFolderType() != ResourceFolderType.VALUES;
        }
    }

    /**
     * Class visitor responsible for looking for resource references in code.
     * It looks for R.type.name references (as well as inlined constants for these,
     * in the case of non-library code), as well as looking both for Resources#getIdentifier
     * calls and recording string literals, used to handle dynamic lookup of resources.
     */
    private class UsageVisitor extends ClassVisitor {
        private final File mJarFile;
        private final String mCurrentClass;

        public UsageVisitor(File jarFile, String name) {
            super(Opcodes.ASM5);
            mJarFile = jarFile;
            mCurrentClass = name;
        }

        @Override
        public MethodVisitor visitMethod(int access, final String name,
                String desc, String signature, String[] exceptions) {
            return new MethodVisitor(Opcodes.ASM5) {
                @Override
                public void visitLdcInsn(Object cst) {
                    handleCodeConstant(cst, "ldc");
                }

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                    if (opcode == Opcodes.GETSTATIC) {
                        ResourceType type = mResourceClassOwners.get(owner);
                        if (type != null) {
                            Resource resource = getResource(type, name);
                            if (resource != null) {
                                markReachable(resource);
                            }
                        }
                    }
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name,
                        String desc, boolean itf) {
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                    if (owner.equals("android/content/res/Resources")
                            && name.equals("getIdentifier")
                            && desc.equals(
                            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I")) {
                        mFoundGetIdentifier = true;
                        // TODO: Check previous instruction and see if we can find a literal
                        // String; if so, we can more accurately dispatch the resource here
                        // rather than having to check the whole string pool!
                    }
                    if (owner.equals("android/webkit/WebView") && name.startsWith("load")) {
                        mFoundWebContent = true;
                    }
                }

                @Override
                public AnnotationVisitor visitAnnotationDefault() {
                    return new AnnotationUsageVisitor();
                }

                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    return new AnnotationUsageVisitor();
                }

                @Override
                public AnnotationVisitor visitParameterAnnotation(int parameter, String desc,
                        boolean visible) {
                    return new AnnotationUsageVisitor();
                }
            };
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return new AnnotationUsageVisitor();
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature,
                Object value) {
            handleCodeConstant(value, "field");
            return new FieldVisitor(Opcodes.ASM5) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    return new AnnotationUsageVisitor();
                }
            };
        }

        private class AnnotationUsageVisitor extends AnnotationVisitor {
            public AnnotationUsageVisitor() {
                super(Opcodes.ASM5);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String name, String desc) {
                return new AnnotationUsageVisitor();
            }

            @Override
            public AnnotationVisitor visitArray(String name) {
                return new AnnotationUsageVisitor();
            }

            @Override
            public void visit(String name, Object value) {
                handleCodeConstant(value, "annotation");
                super.visit(name, value);
            }
        }

        /** Invoked when an ASM visitor encounters a constant: record corresponding reference */
        private void handleCodeConstant(@Nullable Object cst, @NonNull String context) {
            if (cst instanceof Integer) {
                Integer value = (Integer) cst;
                Resource resource = mValueToResource.get(value);
                if (markReachable(resource) && mDebug) {
                    System.out.println("Marking " + resource + " reachable: referenced from " +
                            context + " in " + mJarFile + ":" + mCurrentClass);
                }
            } else if (cst instanceof int[]) {
                int[] values = (int[]) cst;
                for (int value : values) {
                    Resource resource = mValueToResource.get(value);
                    if (markReachable(resource) && mDebug) {
                        System.out.println("Marking " + resource + " reachable: referenced from " +
                                context + " in " + mJarFile + ":" + mCurrentClass);
                    }
                }
            } else if (cst instanceof String) {
                String string = (String) cst;
                referencedString(string);
            }
        }
    }

    @VisibleForTesting
    String dumpResourceModel() {
        StringBuilder sb = new StringBuilder(1000);
        Collections.sort(mResources, new Comparator<Resource>() {
            @Override
            public int compare(Resource resource1,
                    Resource resource2) {
                int delta = resource1.type.compareTo(resource2.type);
                if (delta != 0) {
                    return delta;
                }
                return resource1.name.compareTo(resource2.name);
            }
        });

        for (Resource resource : mResources) {
            sb.append(resource.getUrl()).append(" : reachable=").append(resource.reachable);
            sb.append("\n");
            if (resource.references != null) {
                for (Resource referenced : resource.references) {
                    sb.append("    ");
                    sb.append(referenced.getUrl());
                    sb.append("\n");
                }
            }
        }

        return sb.toString();
    }
}
