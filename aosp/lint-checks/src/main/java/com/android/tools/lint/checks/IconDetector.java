/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.tools.lint.checks;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.ProductFlavor;
import com.android.builder.model.ProductFlavorContainer;
import com.android.resources.Density;
import com.android.resources.ResourceFolderType;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintUtils;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.ResourceXmlDetector;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.Speed;
import com.android.tools.lint.detector.api.XmlContext;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import lombok.ast.AstVisitor;
import lombok.ast.ConstructorInvocation;
import lombok.ast.Expression;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.MethodDeclaration;
import lombok.ast.MethodInvocation;
import lombok.ast.Node;
import lombok.ast.Select;
import lombok.ast.StrictListAccessor;
import lombok.ast.TypeReference;
import lombok.ast.TypeReferencePart;
import lombok.ast.VariableReference;

import static com.android.SdkConstants.ANDROID_MANIFEST_XML;
import static com.android.SdkConstants.ANDROID_URI;
import static com.android.SdkConstants.ATTR_ICON;
import static com.android.SdkConstants.DOT_9PNG;
import static com.android.SdkConstants.DOT_GIF;
import static com.android.SdkConstants.DOT_JPEG;
import static com.android.SdkConstants.DOT_JPG;
import static com.android.SdkConstants.DOT_PNG;
import static com.android.SdkConstants.DOT_WEBP;
import static com.android.SdkConstants.DOT_XML;
import static com.android.SdkConstants.DRAWABLE_PREFIX;
import static com.android.SdkConstants.MENU_TYPE;
import static com.android.SdkConstants.R_CLASS;
import static com.android.SdkConstants.R_DRAWABLE_PREFIX;
import static com.android.SdkConstants.TAG_ACTIVITY;
import static com.android.SdkConstants.TAG_APPLICATION;
import static com.android.SdkConstants.TAG_ITEM;
import static com.android.SdkConstants.TAG_PROVIDER;
import static com.android.SdkConstants.TAG_RECEIVER;
import static com.android.SdkConstants.TAG_SERVICE;
import static com.android.tools.lint.detector.api.LintUtils.endsWith;

/**
 * Checks for common icon problems, such as wrong icon sizes, placing icons in the
 * density independent drawable folder, etc.
 */
public class IconDetector extends ResourceXmlDetector implements Detector.JavaScanner {

    private static final boolean INCLUDE_LDPI;
    /**
     * Pattern for icon names that include their dp size as part of the name
     */
    private static final Pattern DP_NAME_PATTERN = Pattern.compile(".+_(\\d+)dp\\.png"); //$NON-NLS-1$
    // TODO: Convert this over to using the Density enum and FolderConfiguration
    // for qualifier lookup
    private static final String[] DENSITY_QUALIFIERS =
            new String[]{
                    "-ldpi",  //$NON-NLS-1$
                    "-mdpi",  //$NON-NLS-1$
                    "-hdpi",  //$NON-NLS-1$
                    "-xhdpi", //$NON-NLS-1$
                    "-xxhdpi",//$NON-NLS-1$
                    "-xxxhdpi",//$NON-NLS-1$
            };
    /**
     * Scope needed to detect the types of icons (which involves scanning .java files,
     * the manifest, menu files etc to see how icons are used
     */
    private static final EnumSet<Scope> ICON_TYPE_SCOPE = EnumSet.of(Scope.ALL_RESOURCE_FILES,
            Scope.JAVA_FILE, Scope.MANIFEST);
    private static final Implementation IMPLEMENTATION_JAVA = new Implementation(
            IconDetector.class,
            ICON_TYPE_SCOPE);
    /**
     * Wrong icon size according to published conventions
     */
    public static final Issue ICON_EXPECTED_SIZE = Issue.create(
            "IconExpectedSize", //$NON-NLS-1$
            "Icon has incorrect size",
            "There are predefined sizes (for each density) for launcher icons. You " +
                    "should follow these conventions to make sure your icons fit in with the " +
                    "overall look of the platform.",
            Category.ICONS,
            5,
            Severity.WARNING,
            IMPLEMENTATION_JAVA)
            // Still some potential false positives:
            .setEnabledByDefault(false)
            .addMoreInfo(
                    "http://developer.android.com/design/style/iconography.html"); //$NON-NLS-1$
    /**
     * Wrong filename according to the format
     */
    public static final Issue ICON_COLORS = Issue.create(
            "IconColors", //$NON-NLS-1$
            "Icon colors do not follow the recommended visual style",

            "Notification icons and Action Bar icons should only white and shades of gray. " +
                    "See the Android Design Guide for more details. " +
                    "Note that the way Lint decides whether an icon is an action bar icon or " +
                    "a notification icon is based on the filename prefix: `ic_menu_` for " +
                    "action bar icons, `ic_stat_` for notification icons etc. These correspond " +
                    "to the naming conventions documented in " +
                    "http://developer.android.com/guide/practices/ui_guidelines/icon_design.html",
            Category.ICONS,
            6,
            Severity.WARNING,
            IMPLEMENTATION_JAVA).addMoreInfo(
            "http://developer.android.com/design/style/iconography.html"); //$NON-NLS-1$
    /**
     * Wrong launcher icon shape
     */
    public static final Issue ICON_LAUNCHER_SHAPE = Issue.create(
            "IconLauncherShape", //$NON-NLS-1$
            "The launcher icon shape should use a distinct silhouette",

            "According to the Android Design Guide " +
                    "(http://developer.android.com/design/style/iconography.html) " +
                    "your launcher icons should \"use a distinct silhouette\", " +
                    "a \"three-dimensional, front view, with a slight perspective as if viewed " +
                    "from above, so that users perceive some depth.\"\n" +
                    "\n" +
                    "The unique silhouette implies that your launcher icon should not be a filled " +
                    "square.",
            Category.ICONS,
            6,
            Severity.WARNING,
            IMPLEMENTATION_JAVA).addMoreInfo(
            "http://developer.android.com/design/style/iconography.html"); //$NON-NLS-1$
    private static final Implementation IMPLEMENTATION_RES_ONLY = new Implementation(
            IconDetector.class,
            Scope.ALL_RESOURCES_SCOPE);
    /**
     * Inconsistent dip size across densities
     */
    public static final Issue ICON_DIP_SIZE = Issue.create(
            "IconDipSize", //$NON-NLS-1$
            "Icon density-independent size validation",
            "Checks the all icons which are provided in multiple densities, all compute to " +
                    "roughly the same density-independent pixel (`dip`) size. This catches errors where " +
                    "images are either placed in the wrong folder, or icons are changed to new sizes " +
                    "but some folders are forgotten.",
            Category.ICONS,
            5,
            Severity.WARNING,
            IMPLEMENTATION_RES_ONLY);
    /**
     * Images in res/drawable folder
     */
    public static final Issue ICON_LOCATION = Issue.create(
            "IconLocation", //$NON-NLS-1$
            "Image defined in density-independent drawable folder",
            "The res/drawable folder is intended for density-independent graphics such as " +
                    "shapes defined in XML. For bitmaps, move it to `drawable-mdpi` and consider " +
                    "providing higher and lower resolution versions in `drawable-ldpi`, `drawable-hdpi` " +
                    "and `drawable-xhdpi`. If the icon *really* is density independent (for example " +
                    "a solid color) you can place it in `drawable-nodpi`.",
            Category.ICONS,
            5,
            Severity.WARNING,
            IMPLEMENTATION_RES_ONLY).addMoreInfo(
            "http://developer.android.com/guide/practices/screens_support.html"); //$NON-NLS-1$
    /**
     * Missing density versions of image
     */
    public static final Issue ICON_DENSITIES = Issue.create(
            "IconDensities", //$NON-NLS-1$
            "Icon densities validation",
            "Icons will look best if a custom version is provided for each of the " +
                    "major screen density classes (low, medium, high, extra high). " +
                    "This lint check identifies icons which do not have complete coverage " +
                    "across the densities.\n" +
                    "\n" +
                    "Low density is not really used much anymore, so this check ignores " +
                    "the ldpi density. To force lint to include it, set the environment " +
                    "variable `ANDROID_LINT_INCLUDE_LDPI=true`. For more information on " +
                    "current density usage, see " +
                    "http://developer.android.com/resources/dashboard/screens.html",
            Category.ICONS,
            4,
            Severity.WARNING,
            IMPLEMENTATION_RES_ONLY).addMoreInfo(
            "http://developer.android.com/guide/practices/screens_support.html"); //$NON-NLS-1$
    /**
     * Missing density folders
     */
    public static final Issue ICON_MISSING_FOLDER = Issue.create(
            "IconMissingDensityFolder", //$NON-NLS-1$
            "Missing density folder",
            "Icons will look best if a custom version is provided for each of the " +
                    "major screen density classes (low, medium, high, extra-high, extra-extra-high). " +
                    "This lint check identifies folders which are missing, such as `drawable-hdpi`.\n" +
                    "\n" +
                    "Low density is not really used much anymore, so this check ignores " +
                    "the ldpi density. To force lint to include it, set the environment " +
                    "variable `ANDROID_LINT_INCLUDE_LDPI=true`. For more information on " +
                    "current density usage, see " +
                    "http://developer.android.com/resources/dashboard/screens.html",
            Category.ICONS,
            3,
            Severity.WARNING,
            IMPLEMENTATION_RES_ONLY).addMoreInfo(
            "http://developer.android.com/guide/practices/screens_support.html"); //$NON-NLS-1$
    /**
     * Using .gif bitmaps
     */
    public static final Issue GIF_USAGE = Issue.create(
            "GifUsage", //$NON-NLS-1$
            "Using `.gif` format for bitmaps is discouraged",
            "The `.gif` file format is discouraged. Consider using `.png` (preferred) " +
                    "or `.jpg` (acceptable) instead.",
            Category.ICONS,
            5,
            Severity.WARNING,
            IMPLEMENTATION_RES_ONLY).addMoreInfo(
            "http://developer.android.com/guide/topics/resources/drawable-resource.html#Bitmap"); //$NON-NLS-1$
    /**
     * Duplicated icons across different names
     */
    public static final Issue DUPLICATES_NAMES = Issue.create(
            "IconDuplicates", //$NON-NLS-1$
            "Duplicated icons under different names",
            "If an icon is repeated under different names, you can consolidate and just " +
                    "use one of the icons and delete the others to make your application smaller. " +
                    "However, duplicated icons usually are not intentional and can sometimes point " +
                    "to icons that were accidentally overwritten or accidentally not updated.",
            Category.ICONS,
            3,
            Severity.WARNING,
            IMPLEMENTATION_RES_ONLY);
    /**
     * Duplicated contents across configurations for a given name
     */
    public static final Issue DUPLICATES_CONFIGURATIONS = Issue.create(
            "IconDuplicatesConfig", //$NON-NLS-1$
            "Identical bitmaps across various configurations",
            "If an icon is provided under different configuration parameters such as " +
                    "`drawable-hdpi` or `-v11`, they should typically be different. This detector " +
                    "catches cases where the same icon is provided in different configuration folder " +
                    "which is usually not intentional.",
            Category.ICONS,
            5,
            Severity.WARNING,
            IMPLEMENTATION_RES_ONLY);
    /**
     * Icons appearing in both -nodpi and a -Ndpi folder
     */
    public static final Issue ICON_NODPI = Issue.create(
            "IconNoDpi", //$NON-NLS-1$
            "Icon appears in both `-nodpi` and dpi folders",
            "Bitmaps that appear in `drawable-nodpi` folders will not be scaled by the " +
                    "Android framework. If a drawable resource of the same name appears *both* in " +
                    "a `-nodpi` folder as well as a dpi folder such as `drawable-hdpi`, then " +
                    "the behavior is ambiguous and probably not intentional. Delete one or the " +
                    "other, or use different names for the icons.",
            Category.ICONS,
            7,
            Severity.WARNING,
            IMPLEMENTATION_RES_ONLY);
    /**
     * Drawables provided as both .9.png and .png files
     */
    public static final Issue ICON_MIX_9PNG = Issue.create(
            "IconMixedNinePatch", //$NON-NLS-1$
            "Clashing PNG and 9-PNG files",

            "If you accidentally name two separate resources `file.png` and `file.9.png`, " +
                    "the image file and the nine patch file will both map to the same drawable " +
                    "resource, `@drawable/file`, which is probably not what was intended.",
            Category.ICONS,
            5,
            Severity.WARNING,
            IMPLEMENTATION_RES_ONLY);
    /**
     * Icons appearing as both drawable xml files and bitmaps
     */
    public static final Issue ICON_XML_AND_PNG = Issue.create(
            "IconXmlAndPng", //$NON-NLS-1$
            "Icon is specified both as `.xml` file and as a bitmap",
            "If a drawable resource appears as an `.xml` file in the `drawable/` folder, " +
                    "it's usually not intentional for it to also appear as a bitmap using the " +
                    "same name; generally you expect the drawable XML file to define states " +
                    "and each state has a corresponding drawable bitmap.",
            Category.ICONS,
            7,
            Severity.WARNING,
            IMPLEMENTATION_RES_ONLY);
    /**
     * Wrong filename according to the format
     */
    public static final Issue ICON_EXTENSION = Issue.create(
            "IconExtension", //$NON-NLS-1$
            "Icon format does not match the file extension",

            "Ensures that icons have the correct file extension (e.g. a `.png` file is " +
                    "really in the PNG format and not for example a GIF file named `.png`.)",
            Category.ICONS,
            3,
            Severity.WARNING,
            IMPLEMENTATION_RES_ONLY);
    private static final String NOTIFICATION_CLASS = "Notification";              //$NON-NLS-1$
    private static final String NOTIFICATION_COMPAT_CLASS = "NotificationCompat"; //$NON-NLS-1$
    private static final String BUILDER_CLASS = "Builder";                        //$NON-NLS-1$
    private static final String SET_SMALL_ICON = "setSmallIcon";                  //$NON-NLS-1$
    private static final String ON_CREATE_OPTIONS_MENU = "onCreateOptionsMenu";   //$NON-NLS-1$

    static {
        boolean includeLdpi = false;

        String value = System.getenv("ANDROID_LINT_INCLUDE_LDPI"); //$NON-NLS-1$
        if (value != null) {
            includeLdpi = Boolean.valueOf(value);
        }
        INCLUDE_LDPI = includeLdpi;
    }

    private Set<String> mActionBarIcons;
    private Set<String> mNotificationIcons;
    private Set<String> mLauncherIcons;
    private Multimap<String, String> mMenuToIcons;

    /**
     * Constructs a new {@link IconDetector} check
     */
    public IconDetector() {
    }

    /**
     * Like {@link LintUtils#isBitmapFile(File)} but (a) operates on Strings instead
     * of files and (b) also considers XML drawables as images
     */
    private static boolean isDrawableFile(String name) {
        // endsWith(name, DOT_PNG) is also true for endsWith(name, DOT_9PNG)
        return endsWith(name, DOT_PNG) || endsWith(name, DOT_JPG) || endsWith(name, DOT_GIF)
                || endsWith(name, DOT_XML) || endsWith(name, DOT_JPEG) || endsWith(name, DOT_WEBP);
    }

    /**
     * Adds in the resConfig values specified by the given flavor container, assuming
     * it's in one of the relevant variantFlavors, into the given set
     */
    private static void addResConfigsFromFlavor(@NonNull Set<String> relevantDensities,
                                                @Nullable List<String> variantFlavors,
                                                @NonNull ProductFlavorContainer container) {
        ProductFlavor flavor = container.getProductFlavor();
        if (variantFlavors == null || variantFlavors.contains(flavor.getName())) {
            if (!flavor.getResourceConfigurations().isEmpty()) {
                for (String densityName : flavor.getResourceConfigurations()) {
                    Density density = Density.getEnum(densityName);
                    if (density != null && density.isRecommended()
                            && density != Density.NODPI && density != Density.ANYDPI) {
                        relevantDensities.add(densityName);
                    }
                }
            }
        }
    }

    /**
     * Compute the difference in names between a and b. This is not just
     * Sets.difference(a, b) because we want to make the comparisons <b>without
     * file extensions</b> and return the result <b>with</b>..
     */
    private static Set<String> nameDifferences(Set<String> a, Set<String> b) {
        Set<String> names1 = new HashSet<String>(a.size());
        for (String s : a) {
            names1.add(LintUtils.getBaseName(s));
        }
        Set<String> names2 = new HashSet<String>(b.size());
        for (String s : b) {
            names2.add(LintUtils.getBaseName(s));
        }

        names1.removeAll(names2);

        if (!names1.isEmpty()) {
            // Map filenames back to original filenames with extensions
            Set<String> result = new HashSet<String>(names1.size());
            for (String s : a) {
                if (names1.contains(LintUtils.getBaseName(s))) {
                    result.add(s);
                }
            }
            for (String s : b) {
                if (names1.contains(LintUtils.getBaseName(s))) {
                    result.add(s);
                }
            }

            return result;
        }

        return Collections.emptySet();
    }

    /**
     * Compute the intersection in names between a and b. This is not just
     * Sets.intersection(a, b) because we want to make the comparisons <b>without
     * file extensions</b> and return the result <b>with</b>.
     */
    private static Set<String> nameIntersection(Set<String> a, Set<String> b) {
        Set<String> names1 = new HashSet<String>(a.size());
        for (String s : a) {
            names1.add(LintUtils.getBaseName(s));
        }
        Set<String> names2 = new HashSet<String>(b.size());
        for (String s : b) {
            names2.add(LintUtils.getBaseName(s));
        }

        names1.retainAll(names2);

        if (!names1.isEmpty()) {
            // Map filenames back to original filenames with extensions
            Set<String> result = new HashSet<String>(names1.size());
            for (String s : a) {
                if (names1.contains(LintUtils.getBaseName(s))) {
                    result.add(s);
                }
            }
            for (String s : b) {
                if (names1.contains(LintUtils.getBaseName(s))) {
                    result.add(s);
                }
            }

            return result;
        }

        return Collections.emptySet();
    }

    private static boolean isNoDpiFolder(File file) {
        return file.getName().contains("-nodpi");
    }

    // Like LintUtils.getBaseName, but for files like .svn it returns "" rather than ".svn"
    private static String getBaseName(String name) {
        String baseName = name;
        int index = baseName.indexOf('.');
        if (index != -1) {
            baseName = baseName.substring(0, index);
        }

        return baseName;
    }

    private static void checkMixedNinePatches(Context context,
                                              Map<File, Set<String>> folderToNames) {
        Set<String> conflictSet = null;

        for (Entry<File, Set<String>> entry : folderToNames.entrySet()) {
            Set<String> baseNames = new HashSet<String>();
            Set<String> names = entry.getValue();
            for (String name : names) {
                assert isDrawableFile(name) : name;
                String base = getBaseName(name);
                if (baseNames.contains(base)) {
                    String ninepatch = base + DOT_9PNG;
                    String png = base + DOT_PNG;
                    if (names.contains(ninepatch) && names.contains(png)) {
                        if (conflictSet == null) {
                            conflictSet = Sets.newHashSet();
                        }
                        conflictSet.add(base);
                    }
                } else {
                    baseNames.add(base);
                }
            }
        }

        if (conflictSet == null || conflictSet.isEmpty()) {
            return;
        }

        Map<String, List<File>> conflicts = null;
        for (Entry<File, Set<String>> entry : folderToNames.entrySet()) {
            File dir = entry.getKey();
            Set<String> names = entry.getValue();
            for (String name : names) {
                assert isDrawableFile(name) : name;
                String base = getBaseName(name);
                if (conflictSet.contains(base)) {
                    if (conflicts == null) {
                        conflicts = Maps.newHashMap();
                    }
                    List<File> files = conflicts.get(base);
                    if (files == null) {
                        files = Lists.newArrayList();
                        conflicts.put(base, files);
                    }
                    files.add(new File(dir, name));
                }
            }
        }

        assert conflicts != null && !conflicts.isEmpty() : conflictSet;
        List<String> names = new ArrayList<String>(conflicts.keySet());
        Collections.sort(names);
        for (String name : names) {
            List<File> files = conflicts.get(name);
            assert files != null : name;
            Location location = chainLocations(files);

            String message = String.format(
                    "The files `%1$s.png` and `%1$s.9.png` clash; both "
                            + "will map to `@drawable/%1$s`", name);
            context.report(ICON_MIX_9PNG, location, message);
        }
    }

    private static Location chainLocations(List<File> files) {
        // Chain locations together
        Collections.sort(files);
        Location location = null;
        for (File file : files) {
            Location linkedLocation = location;
            location = Location.create(file);
            location.setSecondary(linkedLocation);
        }
        return location;
    }

    /**
     * Is this drawable folder for an Android 3.0 drawable? This will be the
     * case if it specifies -v11+, or if the minimum SDK version declared in the
     * manifest is at least 11.
     */
    private static boolean isAndroid30(Context context, int folderVersion) {
        return folderVersion >= 11 || context.getMainProject().getMinSdk() >= 11;
    }

    @NonNull
    @Override
    public Speed getSpeed() {
        return Speed.SLOW;
    }

    // XML detector: Skim manifest and menu files

    @Override
    public void beforeCheckProject(@NonNull Context context) {
        mLauncherIcons = null;
        mActionBarIcons = null;
        mNotificationIcons = null;
    }

    @Override
    public void afterCheckLibraryProject(@NonNull Context context) {
        if (!context.getProject().getReportIssues()) {
            // If this is a library project not being analyzed, ignore it
            return;
        }

    }

    @Override
    public void afterCheckProject(@NonNull Context context) {
    }

    private boolean isActionBarIcon(String name) {
        assert name.indexOf('.') == -1; // Should supply base name

        // Naming convention
        //noinspection SimplifiableIfStatement
        if (name.startsWith("ic_action_")) { //$NON-NLS-1$
            return true;
        }

        // Naming convention

        return mActionBarIcons != null && mActionBarIcons.contains(name);
    }

    // ---- Implements JavaScanner ----

    private boolean isActionBarIcon(Context context, String name, File file) {
        if (isActionBarIcon(name)) {
            return true;
        }

        // As of Android 3.0 ic_menu_ are action icons
        //noinspection SimplifiableIfStatement,RedundantIfStatement
        if (file != null && name.startsWith("ic_menu_") //$NON-NLS-1$
                && isAndroid30(context, context.getDriver().getResourceFolderVersion(file))) {
            // Naming convention
            return true;
        }

        return false;
    }

    @Override
    public boolean appliesTo(@NonNull Context context, @NonNull File file) {
        return file.getName().equals(ANDROID_MANIFEST_XML);
    }

    @Override
    public boolean appliesTo(@NonNull ResourceFolderType folderType) {
        return folderType == ResourceFolderType.MENU;
    }

    @Override
    public Collection<String> getApplicableElements() {
        return Arrays.asList(
                // Manifest
                TAG_APPLICATION,
                TAG_ACTIVITY,
                TAG_SERVICE,
                TAG_PROVIDER,
                TAG_RECEIVER,

                // Menu
                TAG_ITEM
        );
    }

    @Override
    public void visitElement(@NonNull XmlContext context, @NonNull Element element) {
        String icon = element.getAttributeNS(ANDROID_URI, ATTR_ICON);
        if (icon != null && icon.startsWith(DRAWABLE_PREFIX)) {
            icon = icon.substring(DRAWABLE_PREFIX.length());

            String tagName = element.getTagName();
            if (tagName.equals(TAG_ITEM)) {
                if (mMenuToIcons == null) {
                    mMenuToIcons = ArrayListMultimap.create();
                }
                String menu = getBaseName(context.file.getName());
                mMenuToIcons.put(menu, icon);
            } else {
                // Manifest tags: launcher icons
                if (mLauncherIcons == null) {
                    mLauncherIcons = Sets.newHashSet();
                }
                mLauncherIcons.add(icon);
            }
        }
    }

    @Override
    @Nullable
    public AstVisitor createJavaVisitor(@NonNull JavaContext context) {
        return new NotificationFinder();
    }

    @Override
    @Nullable
    public List<Class<? extends Node>> getApplicableNodeTypes() {
        List<Class<? extends Node>> types = new ArrayList<Class<? extends Node>>(3);
        types.add(MethodDeclaration.class);
        types.add(ConstructorInvocation.class);
        return types;
    }

    private boolean handleSelect(Select select) {
        if (select.toString().startsWith(R_DRAWABLE_PREFIX)) {
            String name = select.astIdentifier().astValue();
            if (mNotificationIcons == null) {
                mNotificationIcons = Sets.newHashSet();
            }
            mNotificationIcons.add(name);

            return true;
        }

        return false;
    }

    private final class NotificationFinder extends ForwardingAstVisitor {
        @Override
        public boolean visitMethodDeclaration(MethodDeclaration node) {
            if (ON_CREATE_OPTIONS_MENU.equals(node.astMethodName().astValue())) {
                // Gather any R.menu references found in this method
                node.accept(new MenuFinder());
            }

            return super.visitMethodDeclaration(node);
        }

        @Override
        public boolean visitConstructorInvocation(ConstructorInvocation node) {
            TypeReference reference = node.astTypeReference();
            StrictListAccessor<TypeReferencePart, TypeReference> parts = reference.astParts();
            String typeName = parts.last().astIdentifier().astValue();
            if (NOTIFICATION_CLASS.equals(typeName)) {
                StrictListAccessor<Expression, ConstructorInvocation> args = node.astArguments();
                if (args.size() == 3) {
                    if (args.first() instanceof Select && handleSelect((Select) args.first())) {
                        return super.visitConstructorInvocation(node);
                    }

                    Node method = StringFormatDetector.getParentMethod(node);
                    if (method != null) {
                        // Must track local types
                        String name = StringFormatDetector.getResourceForFirstArg(method, node);
                        if (name != null) {
                            if (mNotificationIcons == null) {
                                mNotificationIcons = Sets.newHashSet();
                            }
                            mNotificationIcons.add(name);
                        }
                    }
                }
            } else if (BUILDER_CLASS.equals(typeName)) {
                boolean isBuilder = false;
                if (parts.size() == 1) {
                    isBuilder = true;
                } else if (parts.size() == 2) {
                    String clz = parts.first().astIdentifier().astValue();
                    if (NOTIFICATION_CLASS.equals(clz) || NOTIFICATION_COMPAT_CLASS.equals(clz)) {
                        isBuilder = true;
                    }
                }
                if (isBuilder) {
                    Node method = StringFormatDetector.getParentMethod(node);
                    if (method != null) {
                        SetIconFinder finder = new SetIconFinder();
                        method.accept(finder);
                    }
                }
            }

            return super.visitConstructorInvocation(node);
        }
    }

    private final class SetIconFinder extends ForwardingAstVisitor {
        @Override
        public boolean visitMethodInvocation(MethodInvocation node) {
            if (SET_SMALL_ICON.equals(node.astName().astValue())) {
                StrictListAccessor<Expression, MethodInvocation> arguments = node.astArguments();
                if (arguments.size() == 1 && arguments.first() instanceof Select) {
                    handleSelect((Select) arguments.first());
                }
            }
            return super.visitMethodInvocation(node);
        }
    }

    private final class MenuFinder extends ForwardingAstVisitor {
        @Override
        public boolean visitSelect(Select node) {
            // R.type.name
            if (node.astOperand() instanceof Select) {
                Select select = (Select) node.astOperand();
                if (select.astOperand() instanceof VariableReference) {
                    VariableReference reference = (VariableReference) select.astOperand();
                    if (reference.astIdentifier().astValue().equals(R_CLASS)) {
                        String type = select.astIdentifier().astValue();

                        if (type.equals(MENU_TYPE)) {
                            String name = node.astIdentifier().astValue();
                            // Reclassify icons in the given menu as action bar icons
                            if (mMenuToIcons != null) {
                                Collection<String> icons = mMenuToIcons.get(name);
                                if (icons != null) {
                                    if (mActionBarIcons == null) {
                                        mActionBarIcons = Sets.newHashSet();
                                    }
                                    mActionBarIcons.addAll(icons);
                                }
                            }
                        }
                    }
                }
            }

            return super.visitSelect(node);
        }
    }
}
