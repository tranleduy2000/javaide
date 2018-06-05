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

package com.android.build.gradle.tasks.annotations;

import static com.android.SdkConstants.AMP_ENTITY;
import static com.android.SdkConstants.APOS_ENTITY;
import static com.android.SdkConstants.ATTR_NAME;
import static com.android.SdkConstants.ATTR_VALUE;
import static com.android.SdkConstants.DOT_CLASS;
import static com.android.SdkConstants.DOT_JAR;
import static com.android.SdkConstants.DOT_XML;
import static com.android.SdkConstants.GT_ENTITY;
import static com.android.SdkConstants.INT_DEF_ANNOTATION;
import static com.android.SdkConstants.LT_ENTITY;
import static com.android.SdkConstants.QUOT_ENTITY;
import static com.android.SdkConstants.STRING_DEF_ANNOTATION;
import static com.android.SdkConstants.SUPPORT_ANNOTATIONS_PREFIX;
import static com.android.SdkConstants.TYPE_DEF_FLAG_ATTRIBUTE;
import static com.android.SdkConstants.TYPE_DEF_VALUE_ATTRIBUTE;
import static com.android.SdkConstants.VALUE_TRUE;
import static com.android.tools.lint.checks.SupportAnnotationDetector.INT_RANGE_ANNOTATION;
import static com.android.tools.lint.detector.api.LintUtils.assertionsEnabled;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.utils.XmlUtils;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.xml.XmlEscapers;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.NumberLiteral;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.ByteConstant;
import org.eclipse.jdt.internal.compiler.impl.CharConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.DoubleConstant;
import org.eclipse.jdt.internal.compiler.impl.FloatConstant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.impl.LongConstant;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.impl.ShortConstant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MemberTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

/**
 * Annotation extractor which looks for annotations in parsed compilation units and writes
 * the annotations into a format suitable for use by IntelliJ and Android Studio etc;
 * it's basically an XML file, organized by package, which lists the signatures for
 * fields and methods in classes in the given package, and identifiers method parameters
 * by index, and lists the annotations annotated on that element.
 * <p>
 * This is primarily intended for use in Android libraries such as the support library,
 * where you want to use the resource int ({@code StringRes}, {@code DrawableRes}, and so on)
 * annotations to indicate what types of id's are expected, or the {@code IntDef} or
 * {@code StringDef} annotations to record which specific constants are allowed in int and
 * String parameters.
 * <p>
 * However, the code is also used to extract SDK annotations from the platform, where
 * the package names of the annotations differ slightly (and where the nullness annotations
 * do not have class retention for example). Therefore, this code contains some extra
 * support not needed when extracting annotations in an Android library, such as code
 * to skip annotations for any method/field not mentioned in the API database, and code
 * to rewrite the android.jar file to insert annotations in the generated bytecode.
 * <p>
 * TODO:
 * - Warn if the {@code @IntDef} annotation is used on a non-int, and similarly if
 *   {@code @StringDef} is used on a non-string
 * - Ignore annotations defined on @hide elements
 */
public class Extractor {
    /** Whether we should include type args like &lt;T*gt; in external annotations signatures */
    private static final boolean INCLUDE_TYPE_ARGS = false;

    /** Whether to sort annotation attributes (otherwise their declaration order is used) */
    private final boolean sortAnnotations;

    /**
     * Whether we should include class-retention annotations into the extracted file;
     * we don't need {@code android.support.annotation.Nullable} to be in the extracted XML
     * file since it has class retention and will appear in the compiled .jar version of
     * the library
     */
    private final boolean includeClassRetentionAnnotations;

    /**
     * Whether we should skip nullable annotations in merged in annotations zip files
     * (these are typically from infer nullity, which sometimes is a bit aggressive
     * in assuming something should be marked as nullable; see for example issue #66999
     * or all the manual removals of findViewById @Nullable return value annotations
     */
    private static final boolean INCLUDE_INFERRED_NULLABLE = false;

    public static final String ANDROID_ANNOTATIONS_PREFIX = "android.annotation.";
    public static final String ANDROID_NULLABLE = "android.annotation.Nullable";
    public static final String SUPPORT_NULLABLE = "android.support.annotation.Nullable";
    public static final String SUPPORT_KEEP = "android.support.annotation.Keep";
    public static final String RESOURCE_TYPE_ANNOTATIONS_SUFFIX = "Res";
    public static final String ANDROID_NOTNULL = "android.annotation.NonNull";
    public static final String SUPPORT_NOTNULL = "android.support.annotation.NonNull";
    public static final String ANDROID_INT_DEF = "android.annotation.IntDef";
    public static final String ANDROID_INT_RANGE = "android.annotation.IntRange";
    public static final String ANDROID_STRING_DEF = "android.annotation.StringDef";
    public static final String REQUIRES_PERMISSION = "android.support.annotation.RequiresPermission";
    public static final String ANDROID_REQUIRES_PERMISSION = "android.annotation.RequiresPermission";
    public static final String IDEA_NULLABLE = "org.jetbrains.annotations.Nullable";
    public static final String IDEA_NOTNULL = "org.jetbrains.annotations.NotNull";
    public static final String IDEA_MAGIC = "org.intellij.lang.annotations.MagicConstant";
    public static final String IDEA_CONTRACT = "org.jetbrains.annotations.Contract";
    public static final String IDEA_NON_NLS = "org.jetbrains.annotations.NonNls";
    public static final String ATTR_VAL = "val";

    @NonNull
    private final Map<String, List<AnnotationData>> types = Maps.newHashMap();

    @NonNull
    private final Set<String> irrelevantAnnotations = Sets.newHashSet();

    private final File classDir;

    @NonNull
    private final Map<String, Map<String, List<Item>>> itemMap = Maps.newHashMap();

    @Nullable
    private final ApiDatabase apiFilter;

    private final boolean displayInfo;

    private final Map<String,Integer> stats = Maps.newHashMap();
    private int filteredCount;
    private int mergedCount;
    private final Set<CompilationUnitDeclaration> processedFiles = Sets.newHashSetWithExpectedSize(100);
    private final Set<String> ignoredAnnotations = Sets.newHashSet();
    private boolean listIgnored;
    private Map<String,List<Annotation>> typedefs;
    private List<String> typedefClasses;
    private Map<String,Boolean> sourceRetention;
    private final List<Item> keepItems = Lists.newArrayList();

    public Extractor(@Nullable ApiDatabase apiFilter, @Nullable File classDir, boolean displayInfo,
            boolean includeClassRetentionAnnotations, boolean sortAnnotations) {
        this.apiFilter = apiFilter;
        this.listIgnored = apiFilter != null;
        this.classDir = classDir;
        this.displayInfo = displayInfo;
        this.includeClassRetentionAnnotations = includeClassRetentionAnnotations;
        this.sortAnnotations = sortAnnotations;
    }

    public void extractFromProjectSource(Collection<CompilationUnitDeclaration> units) {
        TypedefCollector collector = new TypedefCollector(units, false /*requireHide*/,
                true /*requireSourceRetention*/);
        typedefs = collector.getTypedefs();
        typedefClasses = collector.getNonPublicTypedefClasses();

        for (CompilationUnitDeclaration unit : units) {
            analyze(unit);
        }
    }

    public void removeTypedefClasses() {
        if (classDir != null && typedefClasses != null && !typedefClasses.isEmpty()) {
            boolean quiet = false;
            boolean verbose = false;
            boolean dryRun = false;
            //noinspection ConstantConditions
            TypedefRemover remover = new TypedefRemover(this, quiet, verbose, dryRun);
            remover.remove(classDir, typedefClasses);
        }
    }

    public void export(@Nullable File annotationsZip, @Nullable File proguardCfg) {
        if (proguardCfg != null) {
            if (keepItems.isEmpty()) {
                if (proguardCfg.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    proguardCfg.delete();
                }
            } else if (writeKeepRules(proguardCfg)) {
                info("ProGuard keep rules written to " + proguardCfg);
            }
        }

        if (annotationsZip != null) {
            if (itemMap.isEmpty()) {
                if (annotationsZip.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    annotationsZip.delete();
                }
            } else if (writeExternalAnnotations(annotationsZip)) {
                writeStats();
                info("Annotations written to " + annotationsZip);
            }
        }

    }

    public void writeStats() {
        if (!displayInfo) {
            return;
        }

        if (!stats.isEmpty()) {
            List<String> annotations = Lists.newArrayList(stats.keySet());
            Collections.sort(annotations, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    int frequency1 = stats.get(s1);
                    int frequency2 = stats.get(s2);
                    int delta = frequency2 - frequency1;
                    if (delta != 0) {
                        return delta;
                    }
                    return s1.compareTo(s2);
                }
            });
            Map<String,String> fqnToName = Maps.newHashMap();
            int max = 0;
            int count = 0;
            for (String fqn : annotations) {
                String name = fqn.substring(fqn.lastIndexOf('.') + 1);
                fqnToName.put(fqn, name);
                max = Math.max(max, name.length());
                count += stats.get(fqn);
            }

            StringBuilder sb = new StringBuilder(200);
            sb.append("Extracted ").append(count).append(" Annotations:");
            for (String fqn : annotations) {
                sb.append('\n');
                String name = fqnToName.get(fqn);
                for (int i = 0, n = max - name.length() + 1; i < n; i++) {
                    sb.append(' ');
                }
                sb.append('@');
                sb.append(name);
                sb.append(':').append(' ');
                sb.append(Integer.toString(stats.get(fqn)));
            }
            if (sb.length() > 0) {
                info(sb.toString());
            }
        }

        if (filteredCount > 0) {
            info(filteredCount + " of these were filtered out (not in API database file)");
        }
        if (mergedCount > 0) {
            info(mergedCount + " additional annotations were merged in");
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    void info(final String message) {
        if (displayInfo) {
            System.out.println(message);
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    static void error(String message) {
        System.err.println("Error: " + message);
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    static void warning(String message) {
        System.out.println("Warning: " + message);
    }

    private void analyze(CompilationUnitDeclaration unit) {
        if (processedFiles.contains(unit)) {
            // The code to process all roots seems to hit some of the same classes
            // repeatedly... so filter these out manually
            return;
        }
        processedFiles.add(unit);

        AnnotationVisitor visitor = new AnnotationVisitor();
        unit.traverse(visitor, unit.scope);
    }

    @Nullable
    private static ClassScope findClassScope(Scope scope) {
        while (scope != null) {
            if (scope instanceof ClassScope) {
                return (ClassScope)scope;
            }
            scope = scope.parent;
        }

        return null;
    }

    @Nullable
    static String getFqn(@NonNull Annotation annotation) {
        if (annotation.resolvedType != null) {
            return new String(annotation.resolvedType.readableName());
        }
        return null;
    }

    @Nullable
    private static String getFqn(@NonNull ClassScope scope) {
        TypeDeclaration typeDeclaration = scope.referenceType();
        if (typeDeclaration != null && typeDeclaration.binding != null) {
            return new String(typeDeclaration.binding.readableName());
        }
        return null;
    }

    @Nullable
    private static String getFqn(@NonNull MethodScope scope) {
        ClassScope classScope = findClassScope(scope);
        if (classScope != null) {
            return getFqn(classScope);
        }

        return null;
    }

    @Nullable
    private static String getFqn(@NonNull BlockScope scope) {
        ClassScope classScope = findClassScope(scope);
        if (classScope != null) {
            return getFqn(classScope);
        }

        return null;
    }

    boolean hasSourceRetention(@NonNull String fqn, @Nullable Annotation annotation) {
        if (sourceRetention == null) {
            sourceRetention = Maps.newHashMapWithExpectedSize(20);
            // The @IntDef and @String annotations have always had source retention,
            // and always must (because we can't express fully qualified field references
            // in a .class file.)
            sourceRetention.put(INT_DEF_ANNOTATION, true);
            sourceRetention.put(STRING_DEF_ANNOTATION, true);
            // The @Nullable and @NonNull annotations have always had class retention
            sourceRetention.put(SUPPORT_NOTNULL, false);
            sourceRetention.put(SUPPORT_NULLABLE, false);

            // TODO: Look at support library statistics and put the other most
            // frequently referenced annotations in here statically

            // The resource annotations vary: up until 22.0.1 they had source
            // retention but then switched to class retention.
        }

        Boolean source = sourceRetention.get(fqn);

        if (source != null) {
            return source;
        }

        if (annotation == null || annotation.type == null
                || annotation.type.resolvedType == null) {
            // Assume it's class retention: that's what nearly all annotations
            // currently are. (We do dynamic lookup of unknown ones to allow for
            // this version of the Gradle plugin to be able to work on future
            // versions of the support library with new annotations, where it's
            // possible some annotations need to use source retention.
            sourceRetention.put(fqn, false);
            return false;
        } else if (annotation.type.resolvedType.getAnnotations() != null) {
            for (AnnotationBinding binding : annotation.type.resolvedType.getAnnotations()) {
                if (hasSourceRetention(binding)) {
                    sourceRetention.put(fqn, true);
                    return true;
                }
            }
        }

        sourceRetention.put(fqn, false);
        return false;
    }

    @SuppressWarnings("unused")
    static boolean hasSourceRetention(@NonNull AnnotationBinding a) {
        if (new String(a.getAnnotationType().readableName()).equals("java.lang.annotation.Retention")) {
            ElementValuePair[] pairs = a.getElementValuePairs();
            if (pairs == null || pairs.length != 1) {
                warning("Expected exactly one parameter passed to @Retention");
                return false;
            }
            ElementValuePair pair = pairs[0];
            Object value = pair.getValue();
            if (value instanceof FieldBinding) {
                FieldBinding field = (FieldBinding) value;
                if ("SOURCE".equals(new String(field.readableName()))) {
                    return true;
                }
            }
        }

        return false;
    }

    @SuppressWarnings("unused")
    static boolean hasSourceRetention(@NonNull Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            String typeName = Extractor.getFqn(annotation);
            if ("java.lang.annotation.Retention".equals(typeName)) {
                MemberValuePair[] pairs = annotation.memberValuePairs();
                if (pairs == null || pairs.length != 1) {
                    warning("Expected exactly one parameter passed to @Retention");
                    return false;
                }
                MemberValuePair pair = pairs[0];
                Expression value = pair.value;
                if (value instanceof NameReference) {
                    NameReference reference = (NameReference) value;
                    Binding binding = reference.binding;
                    if (binding != null) {
                        if (binding instanceof FieldBinding) {
                            FieldBinding fb = (FieldBinding) binding;
                            if ("SOURCE".equals(new String(fb.name)) &&
                                    "java.lang.annotation.RetentionPolicy".equals(
                                            new String(fb.declaringClass.readableName()))) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private void addAnnotations(@Nullable Annotation[] annotations, @NonNull Item item) {
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                if (isRelevantAnnotation(annotation)) {
                    String fqn = getFqn(annotation);
                    if (SUPPORT_KEEP.equals(fqn)) {
                        // Put keep rules in a different place; we don't want to write
                        // these out into the external annotations database, they go
                        // into a special proguard file
                        keepItems.add(item);
                    } else {
                        addAnnotation(annotation, fqn, item.annotations);
                    }
                }
            }
        }
    }

    private void addAnnotation(@NonNull Annotation annotation, @Nullable String fqn,
            @NonNull List<AnnotationData> list) {
        if (fqn == null) {
            return;
        }

        if (fqn.equals(ANDROID_NULLABLE) || fqn.equals(SUPPORT_NULLABLE)) {
            recordStats(fqn);
            list.add(new AnnotationData(SUPPORT_NULLABLE));
            return;
        }

        if (fqn.equals(ANDROID_NOTNULL) || fqn.equals(SUPPORT_NOTNULL)) {
            recordStats(fqn);
            list.add(new AnnotationData(SUPPORT_NOTNULL));
            return;
        }

        if (fqn.startsWith(SUPPORT_ANNOTATIONS_PREFIX)
                && fqn.endsWith(RESOURCE_TYPE_ANNOTATIONS_SUFFIX)) {
            recordStats(fqn);
            list.add(new AnnotationData(fqn));
            return;
        }

        if (fqn.startsWith(ANDROID_ANNOTATIONS_PREFIX)) {
            // System annotations: translate to support library annotations
            if (fqn.endsWith(RESOURCE_TYPE_ANNOTATIONS_SUFFIX)) {
                // Translate e.g. android.annotation.DrawableRes to
                //    android.support.annotation.DrawableRes
                String resAnnotation = SUPPORT_ANNOTATIONS_PREFIX +
                        fqn.substring(ANDROID_ANNOTATIONS_PREFIX.length());
                if (!includeClassRetentionAnnotations
                        && !hasSourceRetention(resAnnotation, null)) {
                    return;
                }
                recordStats(resAnnotation);
                list.add(new AnnotationData(resAnnotation));
                return;
            } else if (isRelevantFrameworkAnnotation(fqn)) {
                // Translate other android.annotation annotations into corresponding
                // support annotations
                String supportAnnotation = SUPPORT_ANNOTATIONS_PREFIX +
                        fqn.substring(ANDROID_ANNOTATIONS_PREFIX.length());
                if (!includeClassRetentionAnnotations
                        && !hasSourceRetention(supportAnnotation, null)) {
                    return;
                }
                recordStats(supportAnnotation);
                list.add(createData(supportAnnotation, annotation));
            }
        }

        if (fqn.startsWith(SUPPORT_ANNOTATIONS_PREFIX)) {
            recordStats(fqn);
            list.add(createData(fqn, annotation));
            return;
        }

        if (isMagicConstant(fqn)) {
            List<AnnotationData> indirect = types.get(fqn);
            if (indirect != null) {
                list.addAll(indirect);
            }
        }
    }

    private void recordStats(String fqn) {
        Integer count = stats.get(fqn);
        if (count == null) {
            count = 0;
        }
        stats.put(fqn, count + 1);
    }

    private boolean hasRelevantAnnotations(@Nullable Annotation[] annotations) {
        if (annotations == null) {
            return false;
        }

        for (Annotation annotation : annotations) {
            if (isRelevantAnnotation(annotation)) {
                return true;
            }
        }

        return false;
    }

    private boolean isRelevantAnnotation(@NonNull Annotation annotation) {
        String fqn = getFqn(annotation);
        if (fqn == null || fqn.startsWith("java.lang.")) {
            return false;
        }
        if (fqn.startsWith(SUPPORT_ANNOTATIONS_PREFIX)) {
            if (fqn.equals(SUPPORT_KEEP)) {
                return true; // even with class file retention we want to process these
            }

            //noinspection PointlessBooleanExpression,ConstantConditions,RedundantIfStatement
            if (!includeClassRetentionAnnotations && !hasSourceRetention(fqn, annotation)) {
                return false;
            }

            return true;
        } else if (fqn.startsWith(ANDROID_ANNOTATIONS_PREFIX)) {
            return isRelevantFrameworkAnnotation(fqn);
        }
        if (fqn.equals(ANDROID_NULLABLE) || fqn.equals(ANDROID_NOTNULL)
                || isMagicConstant(fqn)) {
            return true;
        } else if (fqn.equals(IDEA_CONTRACT)) {
            return true;
        }

        return false;
    }

    private static boolean isRelevantFrameworkAnnotation(@NonNull String fqn) {
        return fqn.startsWith(ANDROID_ANNOTATIONS_PREFIX)
                && !fqn.endsWith(".Widget")
                && !fqn.endsWith(".TargetApi")
                && !fqn.endsWith(".SystemApi")
                && !fqn.endsWith(".SuppressLint")
                && !fqn.endsWith(".SdkConstant");
    }

    boolean isMagicConstant(String typeName) {
        if (irrelevantAnnotations.contains(typeName)
                || typeName.startsWith("java.lang.")) { // @Override, @SuppressWarnings, etc.
            return false;
        }
        if (types.containsKey(typeName) ||
                typeName.equals(INT_DEF_ANNOTATION) ||
                typeName.equals(STRING_DEF_ANNOTATION) ||
                typeName.equals(INT_RANGE_ANNOTATION) ||
                typeName.equals(ANDROID_INT_RANGE) ||
                typeName.equals(ANDROID_INT_DEF) ||
                typeName.equals(ANDROID_STRING_DEF)) {
            return true;
        }

        List<Annotation> typeDefs = typedefs.get(typeName);
        // We only support a single level of IntDef type annotations, not arbitrary nesting
        if (typeDefs != null) {
            boolean match = false;
            for (Annotation typeDef : typeDefs) {
                String fqn = getFqn(typeDef);
                if (isNestedAnnotation(fqn)) {
                    List<AnnotationData> list = types.get(typeName);
                    if (list == null) {
                        list = new ArrayList<AnnotationData>(2);
                        types.put(typeName, list);
                    }
                    addAnnotation(typeDef, fqn, list);
                    match = true;
                }
            }

            return match;
        }

        irrelevantAnnotations.add(typeName);

        return false;
    }

    static boolean isNestedAnnotation(@Nullable String fqn) {
        return (fqn != null &&
                (fqn.equals(INT_DEF_ANNOTATION) ||
                        fqn.equals(STRING_DEF_ANNOTATION) ||
                        fqn.equals(REQUIRES_PERMISSION) ||
                        fqn.equals(ANDROID_REQUIRES_PERMISSION) ||
                        fqn.equals(INT_RANGE_ANNOTATION) ||
                        fqn.equals(ANDROID_INT_RANGE) ||
                        fqn.equals(ANDROID_INT_DEF) ||
                        fqn.equals(ANDROID_STRING_DEF)));
    }

    private boolean writeKeepRules(@NonNull File proguardCfg) {
        if (!keepItems.isEmpty()) {
            try {
                Writer writer = new BufferedWriter(new FileWriter(proguardCfg));
                try {
                    Collections.sort(keepItems);
                    for (Item item : keepItems) {
                        writer.write(item.getKeepRule());
                        writer.write('\n');
                    }
                } finally {
                    writer.close();
                }
            } catch (IOException ioe) {
                error(ioe.toString());
                return true;
            }

            // Now that we've handled these items, remove them from the list
            // such that we don't accidentally also emit them into the annotations.zip
            // file, where they are not needed
            for (Item item : keepItems) {
                removeItem(item.getQualifiedClassName(), item);
            }
        } else if (proguardCfg.exists()) {
            //noinspection ResultOfMethodCallIgnored
            proguardCfg.delete();
        }
        return false;
    }

    private boolean writeExternalAnnotations(@NonNull File annotationsZip) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(annotationsZip);
            JarOutputStream zos = new JarOutputStream(fileOutputStream);

            try {
                // TODO: Extract to share with keep rules
                List<String> sortedPackages = new ArrayList<String>(itemMap.keySet());
                Collections.sort(sortedPackages);
                for (String pkg : sortedPackages) {
                    // Note: Using / rather than File.separator: jar lib requires it
                    String name = pkg.replace('.', '/') + "/annotations.xml";

                    JarEntry outEntry = new JarEntry(name);
                    zos.putNextEntry(outEntry);

                    StringWriter stringWriter = new StringWriter(1000);
                    PrintWriter writer = new PrintWriter(stringWriter);
                    try {
                        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<root>");

                        Map<String, List<Item>> classMap = itemMap.get(pkg);
                        List<String> classes = new ArrayList<String>(classMap.keySet());
                        Collections.sort(classes);
                        for (String cls : classes) {
                            List<Item> items = classMap.get(cls);
                            Collections.sort(items);
                            for (Item item : items) {
                                item.write(writer);
                            }
                        }

                        writer.println("</root>\n");
                        writer.close();
                        String xml = stringWriter.toString();

                        // Validate
                        if (assertionsEnabled()) {
                            Document document = checkDocument(pkg, xml, false);
                            if (document == null) {
                                error("Could not parse XML document back in for entry " + name
                                        + ": invalid XML?\n\"\"\"\n" + xml + "\n\"\"\"\n");
                                return false;
                            }
                        }
                        byte[] bytes = xml.getBytes(Charsets.UTF_8);
                        zos.write(bytes);
                        zos.closeEntry();
                    } finally {
                        writer.close();
                    }
                }
            } finally {
                zos.flush();
                zos.close();
            }
        } catch (IOException ioe) {
            error(ioe.toString());
            return false;
        }

        return true;
    }

    private void addItem(@NonNull String fqn, @NonNull Item item) {
        // Not part of the API?
        if (apiFilter != null && item.isFiltered(apiFilter)) {
            if (isListIgnored()) {
                info("Skipping API because it is not part of the API file: " + item);
            }

            filteredCount++;
            return;
        }

        String pkg = getPackage(fqn);
        Map<String, List<Item>> classMap = itemMap.get(pkg);
        if (classMap == null) {
            classMap = Maps.newHashMapWithExpectedSize(100);
            itemMap.put(pkg, classMap);
        }
        List<Item> items = classMap.get(fqn);
        if (items == null) {
            items = Lists.newArrayList();
            classMap.put(fqn, items);
        }

        items.add(item);
    }

    private void removeItem(@NonNull String classFqn, @NonNull Item item) {
        String pkg = getPackage(classFqn);
        Map<String, List<Item>> classMap = itemMap.get(pkg);
        if (classMap != null) {
            List<Item> items = classMap.get(classFqn);
            if (items != null) {
                items.remove(item);
                if (items.isEmpty()) {
                    classMap.remove(classFqn);
                    if (classMap.isEmpty()) {
                        itemMap.remove(pkg);
                    }
                }
            }
        }
    }

    @Nullable
    private Item findItem(@NonNull String fqn, @NonNull Item item) {
        String pkg = getPackage(fqn);
        Map<String, List<Item>> classMap = itemMap.get(pkg);
        if (classMap == null) {
            return null;
        }
        List<Item> items = classMap.get(fqn);
        if (items == null) {
            return null;
        }
        for (Item existing : items) {
            if (existing.equals(item)) {
                return existing;
            }
        }

        return null;
    }

    @Nullable
    private static Document checkDocument(@NonNull String pkg, @NonNull String xml,
            boolean namespaceAware) {
        try {
            return XmlUtils.parseDocument(xml, namespaceAware);
        } catch (SAXException sax) {
            warning("Failed to parse document for package " + pkg + ": " + sax.toString());
        } catch (Exception e) {
            // pass
            // This method is deliberately silent; will return null
        }

        return null;
    }

    public void mergeExisting(@NonNull File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    mergeExisting(child);
                }
            }
        } else if (file.isFile()) {
            if (file.getPath().endsWith(DOT_JAR)) {
                mergeFromJar(file);
            } else if (file.getPath().endsWith(DOT_XML)) {
                try {
                    String xml = Files.toString(file, Charsets.UTF_8);
                    mergeAnnotationsXml(file.getPath(), xml);
                } catch (IOException e) {
                    error("Aborting: I/O problem during transform: " + e.toString());
                }
            }
        }
    }

    private void mergeFromJar(@NonNull File jar) {
        // Reads in an existing annotations jar and merges in entries found there
        // with the annotations analyzed from source.
        JarInputStream zis = null;
        try {
            @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
            FileInputStream fis = new FileInputStream(jar);
            zis = new JarInputStream(fis);
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                if (entry.getName().endsWith(".xml")) {
                    byte[] bytes = ByteStreams.toByteArray(zis);
                    String xml = new String(bytes, Charsets.UTF_8);
                    mergeAnnotationsXml(jar.getPath() + ": " + entry, xml);
                }
                entry = zis.getNextEntry();
            }
        } catch (IOException e) {
            error("Aborting: I/O problem during transform: " + e.toString());
        } finally {
            //noinspection deprecation
            try {
                Closeables.close(zis, true /* swallowIOException */);
            } catch (IOException e) {
                // cannot happen
            }
        }
    }

    private void mergeAnnotationsXml(@NonNull String path, @NonNull String xml) {
        try {
            Document document = XmlUtils.parseDocument(xml, false);
            mergeDocument(document);
        } catch (Exception e) {
            String message = "Failed to merge " + path + ": " + e.toString();
            if (e instanceof SAXParseException) {
                SAXParseException spe = (SAXParseException)e;
                message = "Line " + spe.getLineNumber() + ":" + spe.getColumnNumber() + ": " + message;
            }
            error(message);
            if (!(e instanceof IOException)) {
                e.printStackTrace();
            }
        }
    }

    private void mergeDocument(@NonNull Document document) {
        @SuppressWarnings("SpellCheckingInspection")
        final Pattern XML_SIGNATURE = Pattern.compile(
                // Class (FieldName | Type? Name(ArgList) Argnum?)
                //"(\\S+) (\\S+|(.*)\\s+(\\S+)\\((.*)\\)( \\d+)?)");
                "(\\S+) (\\S+|((.*)\\s+)?(\\S+)\\((.*)\\)( \\d+)?)");

        Element root = document.getDocumentElement();
        String rootTag = root.getTagName();
        assert rootTag.equals("root") : rootTag;

        for (Element item : getChildren(root)) {
            String signature = item.getAttribute(ATTR_NAME);
            if (signature == null || signature.equals("null")) {
                continue; // malformed item
            }

            if (!hasRelevantAnnotations(item)) {
                continue;
            }

            signature = unescapeXml(signature);
            if (signature.equals("java.util.Arrays void sort(T[], java.util.Comparator<?) 0")) {
                // Incorrect metadata (unbalanced <>'s)
                // See IDEA-137385
                signature = "java.util.Arrays void sort(T[], java.util.Comparator<?>) 0";
            }

            Matcher matcher = XML_SIGNATURE.matcher(signature);
            if (matcher.matches()) {
                String containingClass = matcher.group(1);
                if (containingClass == null) {
                    warning("Could not find class for " + signature);
                }
                String methodName = matcher.group(5);
                if (methodName != null) {
                    String type = matcher.group(4);
                    boolean isConstructor = type == null;
                    String parameters = matcher.group(6);
                    mergeMethodOrParameter(item, matcher, containingClass, methodName, type,
                            isConstructor, parameters);
                } else {
                    String fieldName = matcher.group(2);
                    mergeField(item, containingClass, fieldName);
                }
            } else {
                if (signature.indexOf(' ') != -1 || signature.indexOf('.') == -1) {
                    warning("No merge match for signature " + signature);
                } // else: probably just a class signature, e.g. for @NonNls
            }
        }
    }

    @NonNull
    private static String unescapeXml(@NonNull String escaped) {
        String workingString = escaped.replace(QUOT_ENTITY, "\"");
        workingString = workingString.replace(LT_ENTITY, "<");
        workingString = workingString.replace(GT_ENTITY, ">");
        workingString = workingString.replace(APOS_ENTITY, "'");
        workingString = workingString.replace(AMP_ENTITY, "&");

        return workingString;
    }

    @NonNull
    private static String escapeXml(@NonNull String unescaped) {
        return XmlEscapers.xmlAttributeEscaper().escape(unescaped);
    }

    private void mergeField(Element item, String containingClass, String fieldName) {
        if (apiFilter != null &&
                !apiFilter.hasField(containingClass, fieldName)) {
            if (isListIgnored()) {
                info("Skipping imported element because it is not part of the API file: "
                        + containingClass + "#" + fieldName);
            }
            filteredCount++;
        } else {
            FieldItem fieldItem = new FieldItem(containingClass, ClassKind.CLASS, fieldName, null);
            Item existing = findItem(containingClass, fieldItem);
            if (existing != null) {
                mergedCount += mergeAnnotations(item, existing);
            } else {
                addItem(containingClass, fieldItem);
                mergedCount += addAnnotations(item, fieldItem);
            }
        }
    }

    private void mergeMethodOrParameter(Element item, Matcher matcher, String containingClass,
            String methodName, String type, boolean constructor, String parameters) {
        parameters = fixParameterString(parameters);

        if (apiFilter != null &&
                !apiFilter.hasMethod(containingClass, methodName, parameters)) {
            if (isListIgnored()) {
                info("Skipping imported element because it is not part of the API file: "
                        + containingClass + "#" + methodName + "(" + parameters + ")");
            }
            filteredCount++;
            return;
        }

        String argNum = matcher.group(7);
        if (argNum != null) {
            argNum = argNum.trim();
            ParameterItem parameterItem = new ParameterItem(containingClass, ClassKind.CLASS, type,
                    methodName, parameters, constructor, argNum);
            Item existing = findItem(containingClass, parameterItem);

            if ("java.util.Calendar".equals(containingClass) && "set".equals(methodName)
                    && Integer.parseInt(argNum) > 0) {
                // Skip the metadata for Calendar.set(int, int, int+); see
                // https://code.google.com/p/android/issues/detail?id=73982
                return;
            }

            if (existing != null) {
                mergedCount += mergeAnnotations(item, existing);
            } else {
                addItem(containingClass, parameterItem);
                mergedCount += addAnnotations(item, parameterItem);
            }
        } else {
            MethodItem methodItem = new MethodItem(containingClass, ClassKind.CLASS, type,
                    methodName, parameters, constructor);
            Item existing = findItem(containingClass, methodItem);
            if (existing != null) {
                mergedCount += mergeAnnotations(item, existing);
            } else {
                addItem(containingClass, methodItem);
                mergedCount += addAnnotations(item, methodItem);
            }
        }
    }

    // The parameter declaration used in XML files should not have duplicated spaces,
    // and there should be no space after commas (we can't however strip out all spaces,
    // since for example the spaces around the "extends" keyword needs to be there in
    // types like Map<String,? extends Number>
    private static String fixParameterString(String parameters) {
        return parameters.replace("  ", " ").replace(", ", ",");
    }

    private boolean hasRelevantAnnotations(Element item) {
        for (Element annotationElement : getChildren(item)) {
            if (isRelevantAnnotation(annotationElement)) {
                return true;
            }
        }

        return false;
    }

    private boolean isRelevantAnnotation(Element annotationElement) {
        AnnotationData annotation = createAnnotation(annotationElement);
        if (annotation == null) {
            // Unsupported annotation in import
            return false;
        }
        if (isNullable(annotation.name) || isNonNull(annotation.name)
                || annotation.name.startsWith(ANDROID_ANNOTATIONS_PREFIX)
                || annotation.name.startsWith(SUPPORT_ANNOTATIONS_PREFIX)) {
            return true;
        } else if (annotation.name.equals(IDEA_CONTRACT)) {
            return true;
        } else if (annotation.name.equals(IDEA_NON_NLS)) {
            return false;
        } else {
            if (!ignoredAnnotations.contains(annotation.name)) {
                ignoredAnnotations.add(annotation.name);
                if (isListIgnored()) {
                    info("(Ignoring merge annotation " + annotation.name + ")");
                }
            }
        }

        return false;
    }

    @NonNull
    private static List<Element> getChildren(@NonNull Element element) {
        NodeList itemList = element.getChildNodes();
        int length = itemList.getLength();
        List<Element> result = new ArrayList<Element>(Math.max(5, length / 2 + 1));
        for (int i = 0; i < length; i++) {
            Node node = itemList.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            result.add((Element) node);
        }

        return result;
    }

    private int addAnnotations(Element itemElement, Item item) {
        int count = 0;
        for (Element annotationElement : getChildren(itemElement)) {
            if (!isRelevantAnnotation(annotationElement)) {
                continue;
            }
            AnnotationData annotation = createAnnotation(annotationElement);
            item.annotations.add(annotation);
            count++;
        }
        return count;
    }

    private int mergeAnnotations(Element itemElement, Item item) {
        int count = 0;
        loop:
        for (Element annotationElement : getChildren(itemElement)) {
            if (!isRelevantAnnotation(annotationElement)) {
                continue;
            }
            AnnotationData annotation = createAnnotation(annotationElement);
            if (annotation == null) {
                continue;
            }
            boolean haveNullable = false;
            boolean haveNotNull = false;
            for (AnnotationData existing : item.annotations) {
                if (isNonNull(existing.name)) {
                    haveNotNull = true;
                }
                if (isNullable(existing.name)) {
                    haveNullable = true;
                }
                if (existing.equals(annotation)) {
                    continue loop;
                }
            }

            // Make sure we don't have a conflict between nullable and not nullable
            if (isNonNull(annotation.name) && haveNullable ||
                    isNullable(annotation.name) && haveNotNull) {
                warning("Found both @Nullable and @NonNull after import for " + item);
                continue;
            }

            item.annotations.add(annotation);
            count++;
        }

        return count;
    }

    private static boolean isNonNull(String name) {
        return name.equals(IDEA_NOTNULL)
                || name.equals(ANDROID_NOTNULL)
                || name.equals(SUPPORT_NOTNULL);
    }

    private static boolean isNullable(String name) {
        return name.equals(IDEA_NULLABLE)
                || name.equals(ANDROID_NULLABLE)
                || name.equals(SUPPORT_NULLABLE);
    }

    private AnnotationData createAnnotation(Element annotationElement) {
        String tagName = annotationElement.getTagName();
        assert tagName.equals("annotation") : tagName;
        String name = annotationElement.getAttribute(ATTR_NAME);
        assert name != null && !name.isEmpty();
        AnnotationData annotation;
        if (IDEA_MAGIC.equals(name)) {
            List<Element> children = getChildren(annotationElement);
            assert children.size() == 1 : children.size();
            Element valueElement = children.get(0);
            String valName = valueElement.getAttribute(ATTR_NAME);
            String value = valueElement.getAttribute(ATTR_VAL);
            boolean flagsFromClass = valName.equals("flagsFromClass");
            boolean flag = valName.equals("flags") || flagsFromClass;
            if (valName.equals("valuesFromClass") || flagsFromClass) {
                // Not supported
                boolean found = false;
                if (value.endsWith(DOT_CLASS)) {
                    String clsName = value.substring(0, value.length() - DOT_CLASS.length());
                    StringBuilder sb = new StringBuilder();
                    sb.append('{');


                    Field[] reflectionFields = null;
                    try {
                        Class<?> cls = Class.forName(clsName);
                        reflectionFields = cls.getDeclaredFields();
                    } catch (Exception ignore) {
                        // Class not available: not a problem. We'll rely on API filter.
                        // It's mainly used for sorting anyway.
                    }
                    if (apiFilter != null) {
                        // Search in API database
                        Set<String> fields = apiFilter.getDeclaredIntFields(clsName);
                        if ("java.util.zip.ZipEntry".equals(clsName)) {
                            // The metadata says valuesFromClass ZipEntry, and unfortunately
                            // that class implements ZipConstants and therefore imports a large
                            // number of irrelevant constants that aren't valid here. Instead,
                            // only allow these two:
                            fields = Sets.newHashSet("STORED", "DEFLATED");
                        }

                        if (fields != null) {
                            List<String> sorted = Lists.newArrayList(fields);
                            Collections.sort(sorted);
                            if (reflectionFields != null) {
                                final Map<String,Integer> rank = Maps.newHashMap();
                                for (int i = 0, n = sorted.size(); i < n; i++) {
                                    rank.put(sorted.get(i), reflectionFields.length + i);

                                }
                                for (int i = 0, n = reflectionFields.length; i < n; i++) {
                                    rank.put(reflectionFields[i].getName(), i);
                                }
                                Collections.sort(sorted, new Comparator<String>() {
                                    @Override
                                    public int compare(String o1, String o2) {
                                        int rank1 = rank.get(o1);
                                        int rank2 = rank.get(o2);
                                        int delta = rank1 - rank2;
                                        if (delta != 0) {
                                            return delta;
                                        }
                                        return o1.compareTo(o2);
                                    }
                                });
                            }
                            boolean first = true;
                            for (String field : sorted) {
                                if (first) {
                                    first = false;
                                } else {
                                    sb.append(',').append(' ');
                                }
                                sb.append(clsName).append('.').append(field);
                            }
                            found = true;
                        }
                    }
                    // Attempt to sort in reflection order
                    if (!found && reflectionFields != null && (apiFilter == null || apiFilter.hasClass(clsName))) {
                        // Attempt with reflection
                        boolean first = true;
                        for (Field field : reflectionFields) {
                            if (field.getType() == Integer.TYPE ||
                                    field.getType() == int.class) {
                                if (first) {
                                    first = false;
                                } else {
                                    sb.append(',').append(' ');
                                }
                                sb.append(clsName).append('.').append(field.getName());
                            }
                        }
                    }
                    sb.append('}');
                    value = sb.toString();
                    if (sb.length() > 2) { // 2: { }
                        found = true;
                    }
                }

                if (!found) {
                    return null;
                }
            }

            //noinspection VariableNotUsedInsideIf
            if (apiFilter != null) {
                value = removeFiltered(value);
                while (value.contains(", ,")) {
                    value = value.replace(", ,",",");
                }
                if (value.startsWith(", ")) {
                    value = value.substring(2);
                }
            }

            annotation = new AnnotationData(
                    valName.equals("stringValues") ? STRING_DEF_ANNOTATION : INT_DEF_ANNOTATION,
                    new String[] {
                            TYPE_DEF_VALUE_ATTRIBUTE, value,
                            flag ? TYPE_DEF_FLAG_ATTRIBUTE : null, flag ? VALUE_TRUE : null });
        } else if (STRING_DEF_ANNOTATION.equals(name) || ANDROID_STRING_DEF.equals(name) ||
                INT_DEF_ANNOTATION.equals(name) || ANDROID_INT_DEF.equals(name)) {
            List<Element> children = getChildren(annotationElement);
            Element valueElement = children.get(0);
            String valName = valueElement.getAttribute(ATTR_NAME);
            assert TYPE_DEF_VALUE_ATTRIBUTE.equals(valName);
            String value = valueElement.getAttribute(ATTR_VAL);
            boolean flag = false;
            if (children.size() == 2) {
                valueElement = children.get(1);
                assert TYPE_DEF_FLAG_ATTRIBUTE.equals(valueElement.getAttribute(ATTR_NAME));
                flag = VALUE_TRUE.equals(valueElement.getAttribute(ATTR_VAL));
            }
            boolean intDef = INT_DEF_ANNOTATION.equals(name) || ANDROID_INT_DEF.equals(name);
            annotation = new AnnotationData(
                    intDef ? INT_DEF_ANNOTATION : STRING_DEF_ANNOTATION,
                    new String[] { TYPE_DEF_VALUE_ATTRIBUTE, value,
                    flag ? TYPE_DEF_FLAG_ATTRIBUTE : null, flag ? VALUE_TRUE : null});
        } else if (IDEA_CONTRACT.equals(name)) {
            List<Element> children = getChildren(annotationElement);
            assert children.size() == 1 : children.size();
            Element valueElement = children.get(0);
            String value = valueElement.getAttribute(ATTR_VAL);
            annotation = new AnnotationData(name, new String[] { TYPE_DEF_VALUE_ATTRIBUTE, value });
        } else if (isNonNull(name)) {
            annotation = new AnnotationData(SUPPORT_NOTNULL);
        } else if (isNullable(name)) {
            //noinspection PointlessBooleanExpression,ConstantConditions
            if (!INCLUDE_INFERRED_NULLABLE && IDEA_NULLABLE.equals(name)) {
                return null;
            }
            annotation = new AnnotationData(SUPPORT_NULLABLE);
        } else {
            List<Element> children = getChildren(annotationElement);
            if (children.isEmpty()) {
                return new AnnotationData(name);
            }
            List<String> attributeStrings = Lists.newArrayList();
            for (Element valueElement : children) {
                attributeStrings.add(valueElement.getAttribute(ATTR_NAME));
                attributeStrings.add(valueElement.getAttribute(ATTR_VAL));
            }
            annotation = new AnnotationData(name, attributeStrings.toArray(
                    new String[attributeStrings.size()]));
        }
        return annotation;
    }

    private String removeFiltered(String value) {
        assert apiFilter != null;
        if (value.startsWith("{")) {
            value = value.substring(1);
        }
        if (value.endsWith("}")) {
            value = value.substring(0, value.length() - 1);
        }
        value = value.trim();
        StringBuilder sb = new StringBuilder(value.length());
        sb.append('{');
        for (String fqn : Splitter.on(',').omitEmptyStrings().trimResults().split(value)) {
            fqn = unescapeXml(fqn);
            if (fqn.startsWith("\"")) {
                continue;
            }
            int index = fqn.lastIndexOf('.');
            String cls = fqn.substring(0, index);
            String field = fqn.substring(index + 1);
            if (apiFilter.hasField(cls, field)) {
                if (sb.length() > 1) { // 0: '{'
                    sb.append(", ");
                }
                sb.append(fqn);
            } else if (isListIgnored()) {
                info("Skipping constant from typedef because it is not part of the SDK: " + fqn);
            }
        }
        sb.append('}');
        return escapeXml(sb.toString());
    }


    private static String getPackage(String fqn) {
        // Extract package from the given fqn. Attempts to handle inner classes;
        // e.g.  "foo.bar.Foo.Bar will return "foo.bar".
        int index = 0;
        int last = 0;
        while (true) {
            index = fqn.indexOf('.', index);
            if (index == -1) {
                break;
            }
            last = index;
            if (index < fqn.length() - 1) {
                char next = fqn.charAt(index + 1);
                if (Character.isUpperCase(next)) {
                    break;
                }
            }
            index++;
        }

        return fqn.substring(0, last);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setListIgnored(boolean listIgnored) {
        this.listIgnored = listIgnored;
    }

    public boolean isListIgnored() {
        return listIgnored;
    }

    public AnnotationData createData(@NonNull String name, @NonNull Annotation annotation) {
        MemberValuePair[] pairs = annotation.memberValuePairs();
        if (pairs == null || pairs.length == 0) {
            return new AnnotationData(name);
        }
        return new AnnotationData(name, pairs);
    }

    private class AnnotationData {
        @NonNull
        public final String name;

        @Nullable
        public String[] attributeStrings;

        @Nullable
        public MemberValuePair[] attributes;

        private AnnotationData(@NonNull String name) {
            this.name = name;
        }

        private AnnotationData(@NonNull String name, @Nullable MemberValuePair[] pairs) {
            this(name);
            attributes = pairs;
            assert attributes == null || attributes.length > 0;
        }

        private AnnotationData(@NonNull String name, @Nullable String[] attributeStrings) {
            this(name);
            this.attributeStrings = attributeStrings;
            assert attributeStrings != null && attributeStrings.length > 0;
        }

        void write(PrintWriter writer) {
            writer.print("    <annotation name=\"");
            writer.print(name);

            if (attributes != null) {
                writer.print("\">");
                writer.println();
                //noinspection PointlessBooleanExpression,ConstantConditions
                if (attributes.length > 1 && sortAnnotations) {
                    // Ensure that the value attribute is written first
                    Arrays.sort(attributes, new Comparator<MemberValuePair>() {
                        private String getName(MemberValuePair pair) {
                            if (pair.name == null) {
                                return ATTR_VALUE;
                            } else {
                                return new String(pair.name);
                            }
                        }

                        private int rank(MemberValuePair pair) {
                            return ATTR_VALUE.equals(getName(pair)) ? -1 : 0;
                        }

                        @Override
                        public int compare(MemberValuePair o1, MemberValuePair o2) {
                            int r1 = rank(o1);
                            int r2 = rank(o2);
                            int delta = r1 - r2;
                            if (delta != 0) {
                                return delta;
                            }
                            return getName(o1).compareTo(getName(o2));
                        }
                    });
                }

                MemberValuePair[] attributes = this.attributes;

                if (attributes.length == 1
                        && name.startsWith(REQUIRES_PERMISSION)
                        && name.length() > REQUIRES_PERMISSION.length()
                        && attributes[0].value instanceof SingleMemberAnnotation) {
                    // The external annotations format does not allow for nested/complex annotations.
                    // However, these special annotations (@RequiresPermission.Read,
                    // @RequiresPermission.Write, etc) are known to only be simple containers with a
                    // single permission child, so instead we "inline" the content:
                    //  @Read(@RequiresPermission(allOf={P1,P2},conditional=true)
                    //     =>
                    //      @RequiresPermission.Read(allOf({P1,P2},conditional=true)
                    // That's setting attributes that don't actually exist on the container permission,
                    // but we'll counteract that on the read-annotations side.
                    SingleMemberAnnotation annotation = (SingleMemberAnnotation)attributes[0].value;
                    attributes = annotation.memberValuePairs();
                }

                for (MemberValuePair pair : attributes) {
                    writer.print("      <val name=\"");
                    if (pair.name != null) {
                        writer.print(pair.name);
                    } else {
                        writer.print(ATTR_VALUE); // default name
                    }
                    writer.print("\" val=\"");
                    writer.print(escapeXml(attributeString(pair.value)));
                    writer.println("\" />");
                }
                writer.println("    </annotation>");

            } else if (attributeStrings != null) {
                writer.print("\">");
                writer.println();
                for (int i = 0; i < attributeStrings.length; i += 2) {
                    String name = attributeStrings[i];
                    String value = attributeStrings[i + 1];
                    if (name == null) {
                        continue;
                    }
                    writer.print("      <val name=\"");
                    writer.print(name);
                    writer.print("\" val=\"");
                    writer.print(escapeXml(value));
                    writer.println("\" />");
                }
                writer.println("    </annotation>");
            } else {
                writer.println("\" />");
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            AnnotationData that = (AnnotationData) o;

            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        private String attributeString(@NonNull Expression value) {
            StringBuilder sb = new StringBuilder();
            appendExpression(sb, value);
            return sb.toString();
        }

        private boolean appendExpression(@NonNull StringBuilder sb,
                @NonNull Expression expression) {
            if (expression instanceof ArrayInitializer) {
                sb.append('{');
                ArrayInitializer initializer = (ArrayInitializer) expression;
                boolean first = true;
                int initialLength = sb.length();
                for (Expression e : initializer.expressions) {
                    int length = sb.length();
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    boolean appended = appendExpression(sb, e);
                    if (!appended) {
                        // trunk off comma if it bailed for some reason (e.g. constant
                        // filtered out by API etc)
                        sb.setLength(length);
                        if (length == initialLength) {
                            first = true;
                        }
                    }
                }
                sb.append('}');
                return true;
            } else if (expression instanceof NameReference) {
                NameReference reference = (NameReference) expression;
                if (reference.binding != null) {
                    if (reference.binding instanceof FieldBinding) {
                        FieldBinding fb = (FieldBinding)reference.binding;
                        Constant constant = fb.constant();
                        if (constant != null && constant != Constant.NotAConstant &&
                                !(name.equals(INT_DEF_ANNOTATION)) &&
                                !(name.equals(STRING_DEF_ANNOTATION))) {
                            if (constant instanceof StringConstant) {
                                sb.append('"').append(constant.stringValue()).append('"');
                                return true;
                            } else if (constant instanceof IntConstant) {
                                sb.append(Integer.toString(constant.intValue()));
                                return true;
                            } else if (constant instanceof BooleanConstant) {
                                sb.append(Boolean.toString(constant.booleanValue()));
                                return true;
                            } else if (constant instanceof LongConstant) {
                                sb.append(Long.toString(constant.longValue()));
                                return true;
                            } else if (constant instanceof DoubleConstant) {
                                sb.append(Double.toString(constant.doubleValue()));
                                return true;
                            } else if (constant instanceof CharConstant) {
                                sb.append('\'').append(Character.toString(constant.charValue())).append('\'');
                                return true;
                            } else if (constant instanceof FloatConstant) {
                                sb.append(Float.toString(constant.floatValue()));
                                return true;
                            } else if (constant instanceof ShortConstant) {
                                sb.append(Short.toString(constant.shortValue()));
                                return true;
                            } else if (constant instanceof ByteConstant) {
                                sb.append(Byte.toString(constant.byteValue()));
                                return true;
                            }
                        }
                        if (fb.declaringClass != null) {
                            if (apiFilter != null &&
                                    !apiFilter.hasField(
                                            new String(fb.declaringClass.readableName()),
                                            new String(fb.name))) {
                                if (isListIgnored()) {
                                    info("Filtering out typedef constant "
                                            + new String(fb.declaringClass.readableName()) + "."
                                            + new String(fb.name) + "");
                                }
                                return false;
                            }
                            sb.append(fb.declaringClass.readableName());
                            sb.append('.');
                            sb.append(fb.name);
                        } else {
                            sb.append(reference.binding.readableName());
                        }
                    } else {
                        sb.append(reference.binding.readableName());
                    }
                    return true;
                } else {
                    warning("No binding for reference " + reference);
                }
                return false;
            } else if (expression instanceof StringLiteral) {
                StringLiteral s = (StringLiteral) expression;
                sb.append('"');
                sb.append(s.source());
                sb.append('"');
                return true;
            } else if (expression instanceof NumberLiteral) {
                NumberLiteral number = (NumberLiteral) expression;
                sb.append(number.source());
                return true;
            } else if (expression instanceof TrueLiteral) {
                sb.append(true);
                return true;
            } else if (expression instanceof FalseLiteral) {
                sb.append(false);
                return true;
            } else if (expression instanceof org.eclipse.jdt.internal.compiler.ast.NullLiteral) {
                sb.append("null");
                return true;
            } else {
                // BinaryExpression etc can happen if you put "3 + 4" in as an integer!
                if (expression.constant != null) {
                    if (expression.constant.typeID() == TypeIds.T_int) {
                        sb.append(expression.constant.intValue());
                        return true;
                    } else if (expression.constant.typeID() == TypeIds.T_JavaLangString) {
                        sb.append('"');
                        sb.append(expression.constant.stringValue());
                        sb.append('"');
                        return true;
                    } else {
                        warning("Unexpected type for constant " + expression.constant.toString());
                    }
                } else {
                    warning("Unexpected annotation expression of type " + expression.getClass() + " and is "
                            + expression);
                }
            }

            return false;
        }
    }

    public enum ClassKind {
        CLASS,
        INTERFACE,
        ENUM,
        ANNOTATION;

        @NonNull
        public static ClassKind forType(@Nullable TypeDeclaration declaration) {
            if (declaration == null) {
                return CLASS;
            }
            switch (TypeDeclaration.kind(declaration.modifiers)) {
                case TypeDeclaration.INTERFACE_DECL:
                    return INTERFACE;
                case TypeDeclaration.ANNOTATION_TYPE_DECL:
                    return ANNOTATION;
                case TypeDeclaration.ENUM_DECL:
                    return ENUM;
                default:
                    return CLASS;
            }
        }

        public String getKeepType() {
            // See http://proguard.sourceforge.net/manual/usage.html#classspecification
            switch (this) {
                case INTERFACE:
                    return "interface";
                case ENUM:
                    return "enum";

                case ANNOTATION:
                case CLASS:
                default:
                    return "class";
            }
        }
    }

    /**
     * An item in the XML file: this corresponds to a method, a field, or a method parameter, and
     * has an associated set of annotations
     */
    private abstract static class Item implements Comparable<Item> {
        @NonNull public final String containingClass;
        @NonNull public final ClassKind classKind;

        public Item(@NonNull String containingClass, @NonNull ClassKind classKind) {
            this.containingClass = containingClass;
            this.classKind = classKind;
        }

        public final List<AnnotationData> annotations = Lists.newArrayList();

        void write(PrintWriter writer) {
            if (annotations.isEmpty()) {
                return;
            }
            writer.print("  <item name=\"");
            writer.print(getSignature());
            writer.println("\">");

            for (AnnotationData annotation : annotations) {
                annotation.write(writer);
            }
            writer.print("  </item>");
            writer.println();
        }

        abstract boolean isFiltered(@NonNull ApiDatabase database);

        @NonNull
        abstract String getSignature();

        @Override
        public int compareTo(@SuppressWarnings("NullableProblems") @NonNull Item item) {
            String signature1 = getSignature();
            String signature2 = item.getSignature();

            // IntelliJ's sorting order is not on the escaped HTML but the original
            // signatures, which means android.os.AsyncTask<Params,Progress,Result>
            // should appear *after* android.os.AsyncTask.Status, which when the <'s are
            // escaped it does not
            signature1 = signature1.replace('&', '.');
            signature2 = signature2.replace('&', '.');

            return signature1.compareTo(signature2);
        }

        @NonNull
        public abstract String getKeepRule();

        @NonNull
        public abstract String getQualifiedClassName();
    }

    private static class ClassItem extends Item {
        private ClassItem(@NonNull String containingClass, @NonNull ClassKind classKind) {
            super(containingClass, classKind);
        }

        @NonNull
        static ClassItem create(@NonNull String classFqn, @NonNull ClassKind kind) {
            classFqn = ApiDatabase.getRawClass(classFqn);
            return new ClassItem(classFqn, kind);
        }

        @Override
        boolean isFiltered(@NonNull ApiDatabase database) {
            return !database.hasClass(containingClass);
        }

        @NonNull
        @Override
        String getSignature() {
            return escapeXml(containingClass);
        }

        @NonNull
        @Override
        public String getKeepRule() {
            // See http://proguard.sourceforge.net/manual/usage.html#classspecification
            return "-keep " + classKind.getKeepType() + " " + containingClass + "\n";
        }

        @NonNull
        @Override
        public String getQualifiedClassName() {
            return containingClass;
        }

        @Override
        public String toString() {
            return "Class " + containingClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ClassItem that = (ClassItem) o;

            return containingClass.equals(that.containingClass);
        }

        @Override
        public int hashCode() {
            return containingClass.hashCode();
        }
    }

    private static class FieldItem extends Item {

        @NonNull
        public final String fieldName;

        @Nullable
        public final String fieldType;

        private FieldItem(@NonNull String containingClass, @NonNull ClassKind classKind,
                @NonNull String fieldName, @Nullable String fieldType) {
            super(containingClass, classKind);
            this.fieldName = fieldName;
            this.fieldType = fieldType;
        }

        @Nullable
        static FieldItem create(String classFqn, @NonNull ClassKind classKind, FieldBinding field) {
            String name = new String(field.name);
            String type = getFieldType(field);
            return classFqn != null ? new FieldItem(classFqn, classKind, name, type) : null;
        }

        @Nullable
        private static String getFieldType(FieldBinding binding) {
            if (binding.type != null) {
                return new String(binding.type.readableName());
            }

            return null;
        }

        @Override
        boolean isFiltered(@NonNull ApiDatabase database) {
            return !database.hasField(containingClass, fieldName);
        }

        @NonNull
        @Override
        String getSignature() {
            return escapeXml(containingClass) + ' ' + fieldName;
        }

        @NonNull
        @Override
        public String getKeepRule() {
            if (fieldType == null) {
                return ""; // imported item; these can't have keep rules
            }
            // See http://proguard.sourceforge.net/manual/usage.html#classspecification
            return "-keep " + classKind.getKeepType() + " " + containingClass +
                    " {\n    " + fieldType + " " + fieldName + "\n}\n";
        }

        @NonNull
        @Override
        public String getQualifiedClassName() {
            return containingClass;
        }

        @Override
        public String toString() {
            return "Field " + containingClass + "#" + fieldName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            FieldItem that = (FieldItem) o;

            return containingClass.equals(that.containingClass) &&
                    fieldName.equals(that.fieldName);
        }

        @Override
        public int hashCode() {
            int result = fieldName.hashCode();
            result = 31 * result + containingClass.hashCode();
            return result;
        }
    }

    private static class MethodItem extends Item {

        @NonNull
        public final String methodName;

        @NonNull
        public final String parameterList;

        @Nullable
        public final String returnType;

        public final boolean isConstructor;

        private MethodItem(
                @NonNull String containingClass,
                @NonNull ClassKind classKind,
                @Nullable String returnType,
                @NonNull String methodName,
                @NonNull String parameterList,
                boolean isConstructor) {
            super(containingClass, classKind);
            this.returnType = returnType;
            this.methodName = methodName;
            this.parameterList = parameterList;
            this.isConstructor = isConstructor;
        }

        @NonNull
        public String getName() {
            return methodName;
        }

        @Nullable
        static MethodItem create(@Nullable String classFqn,
                @NonNull ClassKind classKind,
                @NonNull AbstractMethodDeclaration declaration,
                @Nullable MethodBinding binding) {
            if (classFqn == null || binding == null) {
                return null;
            }
            String returnType = getReturnType(binding);
            String methodName = getMethodName(binding);
            Argument[] arguments = declaration.arguments;
            boolean isVarargs = arguments != null && arguments.length > 0 &&
                    arguments[arguments.length - 1].isVarArgs();
            String parameterList = getParameterList(binding, isVarargs);
            if (returnType == null || methodName == null) {
                return null;
            }
            //noinspection PointlessBooleanExpression,ConstantConditions
            if (!INCLUDE_TYPE_ARGS) {
                classFqn = ApiDatabase.getRawClass(classFqn);
                methodName = ApiDatabase.getRawMethod(methodName);
            }
            return new MethodItem(classFqn, classKind, returnType,
                    methodName, parameterList,
                    binding.isConstructor());
        }

        @NonNull
        @Override
        String getSignature() {
            StringBuilder sb = new StringBuilder(100);
            sb.append(escapeXml(containingClass));
            sb.append(' ');

            if (isConstructor) {
                sb.append(escapeXml(methodName));
            } else {
                assert returnType != null;
                sb.append(escapeXml(returnType));
                sb.append(' ');
                sb.append(escapeXml(methodName));
            }

            sb.append('(');

            // The signature must match *exactly* the formatting used by IDEA,
            // since it looks up external annotations in a map by this key.
            // Therefore, it is vital that the parameter list uses exactly one
            // space after each comma between parameters, and *no* spaces between
            // generics variables, e.g. foo(Map<A,B>, int)

            // Insert spaces between commas, but not in generics signatures
            int balance = 0;
            for (int i = 0, n = parameterList.length(); i < n; i++) {
                char c = parameterList.charAt(i);
                if (c == '<') {
                    balance++;
                    sb.append("&lt;");
                } else if (c == '>') {
                    balance--;
                    sb.append("&gt;");
                } else if (c == ',') {
                    sb.append(',');
                    if (balance == 0) {
                        sb.append(' ');
                    }
                } else {
                    sb.append(c);
                }
            }
            sb.append(')');
            return sb.toString();
        }

        @Override
        boolean isFiltered(@NonNull ApiDatabase database) {
            return !database.hasMethod(containingClass, methodName, parameterList);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            MethodItem that = (MethodItem) o;

            return isConstructor == that.isConstructor && containingClass
                    .equals(that.containingClass) && methodName.equals(that.methodName)
                    && parameterList.equals(that.parameterList);

        }

        @Override
        public int hashCode() {
            int result = methodName.hashCode();
            result = 31 * result + containingClass.hashCode();
            result = 31 * result + parameterList.hashCode();
            result = 31 * result + (returnType != null ? returnType.hashCode() : 0);
            result = 31 * result + (isConstructor ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Method " + containingClass + "#" + methodName;
        }

        @NonNull
        @Override
        public String getKeepRule() {
            // See http://proguard.sourceforge.net/manual/usage.html#classspecification
            StringBuilder sb = new StringBuilder();
            sb.append("-keep ");
            sb.append(classKind.getKeepType());
            sb.append(" ");
            sb.append(containingClass);
            sb.append(" {\n");
            sb.append("    ");
            if (isConstructor) {
                sb.append("<init>");
            } else {
                sb.append(returnType);
                sb.append(" ");
                sb.append(methodName);
            }
            sb.append("(");
            sb.append(parameterList); // TODO: Strip generics?
            sb.append(")\n");
            sb.append("}\n");

            return sb.toString();
        }

        @NonNull
        @Override
        public String getQualifiedClassName() {
            return containingClass;
        }
    }

    @Nullable
    private static String getReturnType(MethodBinding binding) {
        if (binding.returnType != null) {
            return new String(binding.returnType.readableName());
        } else if (binding.declaringClass != null) {
            assert binding.isConstructor();
            return new String(binding.declaringClass.readableName());
        }

        return null;
    }

    @Nullable
    private static String getMethodName(@NonNull MethodBinding binding) {
        if (binding.isConstructor()) {
            if (binding.declaringClass != null) {
                String classFqn = new String(binding.declaringClass.readableName());
                return classFqn.substring(classFqn.lastIndexOf('.') + 1);
            }

        }
        if (binding.selector != null) {
            return new String(binding.selector);
        }

        assert binding.isConstructor();

        return null;
    }

    @NonNull
    private static String getParameterList(@NonNull MethodBinding binding, boolean isVarargs) {
        // Create compact type signature (no spaces around commas or generics arguments)
        StringBuilder sb = new StringBuilder();
        TypeBinding[] typeParameters = binding.parameters;
        if (typeParameters != null) {
            for (int i = 0, n = typeParameters.length; i < n; i++) {
                TypeBinding parameter = typeParameters[i];
                if (i > 0) {
                    sb.append(',');
                }
                String str = fixParameterString(new String(parameter.readableName()));
                if (isVarargs && i == n - 1 && str.endsWith("[]")) {
                    str = str.substring(0, str.length() - 2) + "...";
                }
                sb.append(str);
            }
        }
        return sb.toString();
    }

    private static class ParameterItem extends MethodItem {
        @NonNull
        public final String argIndex;

        private ParameterItem(
                @NonNull String containingClass,
                @NonNull ClassKind classKind,
                @Nullable String returnType,
                @NonNull String methodName,
                @NonNull String parameterList,
                boolean isConstructor,
                @NonNull String argIndex) {
            super(containingClass, classKind, returnType, methodName, parameterList, isConstructor);
            this.argIndex = argIndex;
        }

        @Nullable
        static ParameterItem create(
                AbstractMethodDeclaration methodDeclaration,
                Argument argument,
                String classFqn,
                ClassKind classKind,
                MethodBinding methodBinding,
                LocalVariableBinding parameterBinding) {
            if (classFqn == null || methodBinding == null || parameterBinding == null) {
                return null;
            }

            String methodName = getMethodName(methodBinding);
            Argument[] arguments = methodDeclaration.arguments;
            boolean isVarargs = arguments != null && arguments.length > 0 &&
                    arguments[arguments.length - 1].isVarArgs();
            String parameterList = getParameterList(methodBinding, isVarargs);
            String returnType = getReturnType(methodBinding);
            if (methodName == null || returnType == null) {
                return null;
            }

            int index = 0;
            boolean found = false;
            if (methodDeclaration.arguments != null) {
                for (Argument a : methodDeclaration.arguments) {
                    if (a == argument) {
                        found = true;
                        break;
                    }
                    index++;
                }
            }
            if (!found) {
                return null;
            }
            String argNum = Integer.toString(index);

            //noinspection PointlessBooleanExpression,ConstantConditions
            if (!INCLUDE_TYPE_ARGS) {
                classFqn = ApiDatabase.getRawClass(classFqn);
                methodName = ApiDatabase.getRawMethod(methodName);
            }
            return new ParameterItem(classFqn, classKind, returnType, methodName, parameterList,
                    methodBinding.isConstructor(), argNum);
        }


        @NonNull
        @Override
        String getSignature() {
            return super.getSignature() + ' ' + argIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            ParameterItem that = (ParameterItem) o;

            return argIndex.equals(that.argIndex);

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + argIndex.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Parameter #" + argIndex + " in " + super.toString();
        }

        @NonNull
        @Override
        public String getKeepRule() {
            return "";
        }
    }

    private class AnnotationVisitor extends ASTVisitor {
        @Override
        public boolean visit(Argument argument, BlockScope scope) {
            Annotation[] annotations = argument.annotations;
            if (hasRelevantAnnotations(annotations)) {
                ReferenceContext referenceContext = scope.referenceContext();
                if (referenceContext instanceof AbstractMethodDeclaration) {
                    MethodBinding binding = ((AbstractMethodDeclaration) referenceContext).binding;
                    ClassScope classScope = findClassScope(scope);
                    if (classScope == null) {
                        return false;
                    }
                    String fqn = getFqn(classScope);
                    ClassKind kind = ClassKind.forType(classScope.referenceContext);
                    Item item = ParameterItem.create(
                            (AbstractMethodDeclaration) referenceContext, argument, fqn, kind,
                            binding, argument.binding);
                    if (item != null) {
                        addItem(fqn, item);
                        addAnnotations(annotations, item);
                    }
                }
            }
            return false;
        }

        @Override
        public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
            Annotation[] annotations = constructorDeclaration.annotations;
            if (hasRelevantAnnotations(annotations)) {
                MethodBinding constructorBinding = constructorDeclaration.binding;
                if (constructorBinding == null) {
                    return false;
                }

                String fqn = getFqn(scope);
                ClassKind kind = ClassKind.forType(scope.referenceContext);
                Item item = MethodItem.create(fqn, kind, constructorDeclaration, constructorBinding);
                if (item != null) {
                    addItem(fqn, item);
                    addAnnotations(annotations, item);
                }
            }

            Argument[] arguments = constructorDeclaration.arguments;
            if (arguments != null) {
                for (Argument argument : arguments) {
                    argument.traverse(this, constructorDeclaration.scope);
                }
            }
            return false;
        }

        @Override
        public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
            Annotation[] annotations = fieldDeclaration.annotations;
            if (hasRelevantAnnotations(annotations)) {
                FieldBinding fieldBinding = fieldDeclaration.binding;
                if (fieldBinding == null) {
                    return false;
                }

                String fqn = getFqn(scope);
                ClassKind kind = scope.referenceContext instanceof TypeDeclaration ?
                        ClassKind.forType((TypeDeclaration)scope.referenceContext) :
                        ClassKind.CLASS;
                Item item = FieldItem.create(fqn, kind, fieldBinding);
                if (item != null && fqn != null) {
                    addItem(fqn, item);
                    addAnnotations(annotations, item);
                }
            }
            return false;
        }

        @Override
        public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
            Annotation[] annotations = methodDeclaration.annotations;
            if (hasRelevantAnnotations(annotations)) {
                MethodBinding methodBinding = methodDeclaration.binding;
                if (methodBinding == null) {
                    return false;
                }
                String fqn = getFqn(scope);
                ClassKind kind = ClassKind.forType(scope.referenceContext);
                MethodItem item = MethodItem.create(fqn, kind, methodDeclaration,
                        methodDeclaration.binding);
                if (item != null) {
                    addItem(fqn, item);

                    // Deliberately skip findViewById()'s return nullability
                    // for now; it's true that findViewById can return null,
                    // but that means all code which does findViewById(R.id.foo).something()
                    // will be flagged as potentially throwing an NPE, and many developers
                    // will do this when they *know* that the id exists (in which case
                    // the method won't return null.)
                    boolean skipReturnAnnotations = false;
                    if ("findViewById".equals(item.getName())) {
                        skipReturnAnnotations = true;
                        if (item.annotations.isEmpty()) {
                            // No other annotations so far: just remove it
                            removeItem(fqn, item);
                        }
                    }

                    if (!skipReturnAnnotations) {
                        addAnnotations(annotations, item);
                    }
                }
            }

            Argument[] arguments = methodDeclaration.arguments;
            if (arguments != null) {
                for (Argument argument : arguments) {
                    argument.traverse(this, methodDeclaration.scope);
                }
            }
            return false;
        }

        @Override
        public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
            Annotation[] annotations = localTypeDeclaration.annotations;
            if (hasRelevantAnnotations(annotations)) {
                SourceTypeBinding binding = localTypeDeclaration.binding;
                if (binding == null) {
                    return true;
                }

                String fqn = getFqn(scope);
                if (fqn == null) {
                    fqn = new String(localTypeDeclaration.binding.readableName());
                }
                Item item = ClassItem.create(fqn, ClassKind.forType(localTypeDeclaration));
                addItem(fqn, item);
                addAnnotations(annotations, item);

            }
            return true;
        }

        @Override
        public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
            Annotation[] annotations = memberTypeDeclaration.annotations;
            if (hasRelevantAnnotations(annotations)) {
                SourceTypeBinding binding = memberTypeDeclaration.binding;
                if (!(binding instanceof MemberTypeBinding)) {
                    return true;
                }
                if (binding.isAnnotationType() || binding.isAnonymousType()) {
                    return false;
                }

                String fqn = new String(memberTypeDeclaration.binding.readableName());
                Item item = ClassItem.create(fqn, ClassKind.forType(memberTypeDeclaration));
                addItem(fqn, item);
                addAnnotations(annotations, item);
            }
            return true;
        }

        @Override
        public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
            Annotation[] annotations = typeDeclaration.annotations;
            if (hasRelevantAnnotations(annotations)) {
                SourceTypeBinding binding = typeDeclaration.binding;
                if (binding == null) {
                    return true;
                }
                String fqn = new String(typeDeclaration.binding.readableName());
                Item item = ClassItem.create(fqn, ClassKind.forType(typeDeclaration));
                addItem(fqn, item);
                addAnnotations(annotations, item);

            }
            return true;
        }
    }
}
