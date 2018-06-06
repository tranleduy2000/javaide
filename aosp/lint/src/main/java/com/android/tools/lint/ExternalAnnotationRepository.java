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

package com.android.tools.lint;

import static com.android.SdkConstants.ATTR_NAME;
import static com.android.SdkConstants.DOT_JAR;
import static com.android.SdkConstants.FN_ANNOTATIONS_ZIP;
import static com.android.SdkConstants.VALUE_FALSE;
import static com.android.SdkConstants.VALUE_TRUE;
import static com.android.tools.lint.checks.SupportAnnotationDetector.PERMISSION_ANNOTATION;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.Dependencies;
import com.android.builder.model.Variant;
import com.android.tools.lint.client.api.JavaParser.DefaultTypeDescriptor;
import com.android.tools.lint.client.api.JavaParser.ResolvedAnnotation;
import com.android.tools.lint.client.api.JavaParser.ResolvedAnnotation.Value;
import com.android.tools.lint.client.api.JavaParser.ResolvedClass;
import com.android.tools.lint.client.api.JavaParser.ResolvedField;
import com.android.tools.lint.client.api.JavaParser.ResolvedMethod;
import com.android.tools.lint.client.api.JavaParser.ResolvedPackage;
import com.android.tools.lint.client.api.JavaParser.TypeDescriptor;
import com.android.tools.lint.client.api.LintClient;
import com.android.tools.lint.detector.api.LintUtils;
import com.android.tools.lint.detector.api.Project;
import com.android.utils.XmlUtils;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

/**
 * Handler for IntelliJ database files for external annotations.
 * It can be pointed to an annotations .jar file, which it then reads,
 * and can return {@link ResolvedAnnotation} instances when queried
 * for annotations on a {@link ResolvedClass} or a {@link ResolvedMethod},
 * including its parameters.
 */
public class ExternalAnnotationRepository {
    public static final String SDK_ANNOTATIONS_PATH = "platform-tools/api/annotations.zip"; //$NON-NLS-1$
    public static final String FN_ANNOTATIONS_XML = "annotations.xml"; //$NON-NLS-1$

    private static final boolean DEBUG = false;

    private static ExternalAnnotationRepository sSingleton;

    private final List<AnnotationsDatabase> mDatabases;

    private ExternalAnnotationRepository(@NonNull List<AnnotationsDatabase> databases) {
        mDatabases = databases;
    }

    @NonNull
    public static synchronized ExternalAnnotationRepository get(@NonNull LintClient client) {
        if (sSingleton == null) {
            HashSet<AndroidLibrary> seen = Sets.newHashSet();
            Collection<Project> projects = client.getKnownProjects();
            List<File> files = Lists.newArrayListWithExpectedSize(2);
            for (Project project : projects) {
                if (project.isGradleProject()) {
                    Variant variant = project.getCurrentVariant();
                    AndroidProject model = project.getGradleProjectModel();
                    if (model != null && variant != null) {
                        Dependencies dependencies = variant.getMainArtifact().getDependencies();
                        for (AndroidLibrary library : dependencies.getLibraries()) {
                            addLibraries(files, library, seen);
                        }
                    }
                }
            }

            File sdkAnnotations = client.findResource(SDK_ANNOTATIONS_PATH);
            if (sdkAnnotations == null) {
                // Until the SDK annotations are bundled in platform tools, provide
                // a fallback for Gradle builds to point to a locally installed version
                String path = System.getenv("SDK_ANNOTATIONS");
                if (path != null) {
                    sdkAnnotations = new File(path);
                    if (!sdkAnnotations.exists()) {
                        sdkAnnotations = null;
                    }
                }
            }
            if (sdkAnnotations != null) {
                files.add(sdkAnnotations);
            }

            sSingleton = create(client, files);
        }

        return sSingleton;
    }

    @VisibleForTesting
    @NonNull
    static synchronized ExternalAnnotationRepository create(
            @Nullable LintClient client,
            @NonNull List<File> files) {
        long begin;
        if (DEBUG) {
            begin = System.currentTimeMillis();
        }

        List<AnnotationsDatabase> databases = Lists.newArrayListWithExpectedSize(files.size());
        for (File file : files) {
            try {
                AnnotationsDatabase database = getDatabase(file);
                if (database != null) {
                    databases.add(database);
                }
            } catch (IOException ioe) {
                if (client != null) {
                    client.log(ioe, "Could not read %1$s", file.getPath());
                } else {
                    ioe.printStackTrace();
                }
            }
        }

        ExternalAnnotationRepository manager = new ExternalAnnotationRepository(databases);

        if (DEBUG) {
            long end = System.currentTimeMillis();
            System.out.println("Initialization of annotations took " + (end - begin) + " ms");
        }

        return manager;
    }

    private static void addLibraries(
            @NonNull List<File> result,
            @NonNull AndroidLibrary library,
            Set<AndroidLibrary> seen) {
        if (seen.contains(library)) {
            return;
        }
        seen.add(library);

        // As of 1.2 this is available in the model:
        //  https://android-review.googlesource.com/#/c/137750/
        // Switch over to this when it's in more common usage
        // (until it is, we'll pay for failed proxying errors)
        File zip = new File(library.getResFolder().getParent(), FN_ANNOTATIONS_ZIP);
        if (zip.exists()) {
            result.add(zip);
        }

        for (AndroidLibrary dependency : library.getLibraryDependencies()) {
            addLibraries(result, dependency, seen);
        }
    }

    @Nullable
    private static AnnotationsDatabase getDatabase(
            @NonNull LintClient client,
            @NonNull File file) {
        try {
            return file.isFile() ? new AnnotationsDatabase(file) : null;
        } catch (IOException ioe) {
            client.log(ioe, "Could not read %1$s", file.getPath());
            return null;
        }
    }

    @VisibleForTesting
    @Nullable
    static AnnotationsDatabase getDatabase(@NonNull File file) throws IOException {
        return file.exists() ? new AnnotationsDatabase(file) : null;
    }

    @Nullable
    private static AnnotationsDatabase getDatabase(
            @NonNull LintClient client,
            @NonNull AndroidLibrary library) {
        // As of 1.2 this is available in the model:
        //  https://android-review.googlesource.com/#/c/137750/
        // Switch over to this when it's in more common usage
        // (until it is, we'll pay for failed proxying errors)
        File zip = new File(library.getResFolder().getParent(), FN_ANNOTATIONS_ZIP);
        return getDatabase(client, zip);
    }

    // ---- Query methods ----

    @Nullable
    public ResolvedAnnotation getAnnotation(@NonNull ResolvedMethod method, @NonNull String type) {
        for (AnnotationsDatabase database : mDatabases) {
            ResolvedAnnotation annotation = database.getAnnotation(method, type);
            if (annotation != null) {
                return annotation;
            }
        }

        return null;
    }

    @Nullable
    public Collection<ResolvedAnnotation> getAnnotations(@NonNull ResolvedMethod method) {
        for (AnnotationsDatabase database : mDatabases) {
            Collection<ResolvedAnnotation> annotations = database.getAnnotations(method);
            if (annotations != null) {
                return annotations;
            }
        }

        return null;
    }

    @Nullable
    public ResolvedAnnotation getAnnotation(@NonNull ResolvedMethod method,
            int parameterIndex, @NonNull String type) {
        for (AnnotationsDatabase database : mDatabases) {
            ResolvedAnnotation annotation = database.getAnnotation(method, parameterIndex, type);
            if (annotation != null) {
                return annotation;
            }
        }

        return null;
    }

    @Nullable
    public Collection<ResolvedAnnotation> getAnnotations(
            @NonNull ResolvedMethod method,
            int parameterIndex) {
        for (AnnotationsDatabase database : mDatabases) {
            Collection<ResolvedAnnotation> annotations = database.getAnnotations(method,
                    parameterIndex);
            if (annotations != null) {
                return annotations;
            }
        }

        return null;
    }

    @Nullable
    public ResolvedAnnotation getAnnotation(@NonNull ResolvedClass cls, @NonNull String type) {
        for (AnnotationsDatabase database : mDatabases) {
            ResolvedAnnotation annotation = database.getAnnotation(cls, type);
            if (annotation != null) {
                return annotation;
            }
        }

        return null;
    }

    @Nullable
    public Collection<ResolvedAnnotation> getAnnotations(@NonNull ResolvedClass cls) {
        for (AnnotationsDatabase database : mDatabases) {
            Collection<ResolvedAnnotation> annotations = database.getAnnotations(cls);
            if (annotations != null) {
                return annotations;
            }
        }

        return null;
    }

    @Nullable
    public ResolvedAnnotation getAnnotation(@NonNull ResolvedField field, @NonNull String type) {
        for (AnnotationsDatabase database : mDatabases) {
            ResolvedAnnotation annotation = database.getAnnotation(field, type);
            if (annotation != null) {
                return annotation;
            }
        }

        return null;
    }

    @Nullable
    public Collection<ResolvedAnnotation> getAnnotations(@NonNull ResolvedField field) {
        for (AnnotationsDatabase database : mDatabases) {
            Collection<ResolvedAnnotation> annotations = database.getAnnotations(field);
            if (annotations != null) {
                return annotations;
            }
        }

        return null;
    }

    @Nullable
    public Collection<ResolvedAnnotation> getAnnotations(@NonNull ResolvedAnnotation cls) {
        for (AnnotationsDatabase database : mDatabases) {
            Collection<ResolvedAnnotation> annotations = database.getAnnotations(cls);
            if (annotations != null) {
                return annotations;
            }
        }

        return null;
    }

    @Nullable
    public ResolvedAnnotation getAnnotation(@NonNull ResolvedPackage pkg, @NonNull String type) {
        for (AnnotationsDatabase database : mDatabases) {
            ResolvedAnnotation annotation = database.getAnnotation(pkg, type);
            if (annotation != null) {
                return annotation;
            }
        }

        return null;
    }
    @Nullable
    public Collection<ResolvedAnnotation> getAnnotations(@NonNull ResolvedPackage pkg) {
        for (AnnotationsDatabase database : mDatabases) {
            Collection<ResolvedAnnotation> annotations = database.getAnnotations(pkg);
            if (annotations != null) {
                return annotations;
            }
        }

        return null;
    }

    // ---- Reading from storage ----

    private static final Pattern XML_SIGNATURE = Pattern.compile(
            // Class (FieldName | Type? Name(ArgList) Argnum?)
            "(\\S+) (\\S+|((.*)\\s+)?(\\S+)\\((.*)\\)( \\d+)?)");

    /** Map from class fully qualified name to the class annotations info */
    // Query database
    private static class ClassInfo {
        public List<ResolvedAnnotation> annotations;
        public Multimap<String,MethodInfo> methods;
        public Map<String,FieldInfo> fields;
    }

    private static class MethodInfo {
        public String parameters;
        public boolean constructor;
        public List<ResolvedAnnotation> annotations;
        public Multimap<Integer,ResolvedAnnotation> parameterAnnotations;
    }

    private static class FieldInfo {
        public List<ResolvedAnnotation> annotations;
    }

    /** An {@linkplain AnnotationsDatabase} corresponds to a single external annotations .zip
     * file (or if in the dev tree, a corresponding directory tree.
     * <p>
     * The SDK has an annotations database, and AAR libraries can also supply individual databases.
     * The {@linkplain ExternalAnnotationRepository} class manages all of these and performs lookup
     * into the various databases through a single entrypoint.
     * */
    static class AnnotationsDatabase {
        AnnotationsDatabase(@NonNull File file) throws IOException {
            String path = file.getPath();
            if (path.endsWith(DOT_JAR) || path.endsWith(FN_ANNOTATIONS_ZIP)) {
                initializeFromJar(file);
            } else {
                assert file.isDirectory() : file;
                initializeFromDirectory(file);
            }
        }

        // ---- Query methods ----

        @Nullable
        public ResolvedAnnotation getAnnotation(@NonNull ResolvedMethod method,
                @NonNull String type) {
            MethodInfo m = findMethod(method);
            if (m == null) {
                return null;
            }

            if (m.annotations != null) {
                for (ResolvedAnnotation annotation : m.annotations) {
                    if (type.equals(annotation.getSignature())) {
                        return annotation;
                    }
                }
            }

            return null;
        }

        @Nullable
        public List<ResolvedAnnotation> getAnnotations(@NonNull ResolvedMethod method) {
            MethodInfo m = findMethod(method);
            if (m == null) {
                return null;
            }
            return m.annotations;
        }

        @Nullable
        public ResolvedAnnotation getAnnotation(@NonNull ResolvedMethod method,
                int parameterIndex, @NonNull String type) {
            MethodInfo m = findMethod(method);
            if (m == null) {
                return null;
            }

            if (m.parameterAnnotations != null) {
                Collection<ResolvedAnnotation> annotations = m.parameterAnnotations.get(parameterIndex);
                if (annotations != null) {
                    for (ResolvedAnnotation annotation : annotations) {
                        if (type.equals(annotation.getSignature())) {
                            return annotation;
                        }
                    }
                }
            }

            return null;
        }

        @Nullable
        public Collection<ResolvedAnnotation> getAnnotations(
                @NonNull ResolvedMethod method,
                int parameterIndex) {
            MethodInfo m = findMethod(method);
            if (m == null) {
                return null;
            }

            if (m.parameterAnnotations != null) {
                return m.parameterAnnotations.get(parameterIndex);
            }

            return m.annotations;
        }

        @Nullable
        public ResolvedAnnotation getAnnotation(@NonNull ResolvedClass cls, @NonNull String type) {
            ClassInfo c = findClass(cls);
            if (c == null) {
                return null;
            }

            if (c.annotations != null) {
                for (ResolvedAnnotation annotation : c.annotations) {
                    if (type.equals(annotation.getSignature())) {
                        return annotation;
                    }
                }
            }

            return null;
        }

        @Nullable
        public List<ResolvedAnnotation> getAnnotations(@NonNull ResolvedClass cls) {
            ClassInfo c = findClass(cls);
            if (c == null) {
                return null;
            }

            return c.annotations;
        }

        @Nullable
        public List<ResolvedAnnotation> getAnnotations(@NonNull ResolvedAnnotation cls) {
            ClassInfo c = findClass(cls);
            if (c == null) {
                return null;
            }

            return c.annotations;
        }

        @Nullable
        public ResolvedAnnotation getAnnotation(@NonNull ResolvedPackage pkg, @NonNull String type) {
            ClassInfo c = findPackage(pkg);

            if (c == null) {
                return null;
            }

            if (c.annotations != null) {
                for (ResolvedAnnotation annotation : c.annotations) {
                    if (type.equals(annotation.getSignature())) {
                        return annotation;
                    }
                }
            }

            return null;
        }

        @Nullable
        public List<ResolvedAnnotation> getAnnotations(@NonNull ResolvedPackage pkg) {
            ClassInfo c = findPackage(pkg);
            if (c == null) {
                return null;
            }

            return c.annotations;
        }

        @Nullable
        public ResolvedAnnotation getAnnotation(@NonNull ResolvedField field, @NonNull String type) {
            FieldInfo f = findField(field);

            if (f == null) {
                return null;
            }
            if (f.annotations != null) {
                for (ResolvedAnnotation annotation : f.annotations) {
                    if (type.equals(annotation.getSignature())) {
                        return annotation;
                    }
                }
            }

            return null;
        }

        @Nullable
        public List<ResolvedAnnotation> getAnnotations(@NonNull ResolvedField field) {
            FieldInfo f = findField(field);

            if (f == null) {
                return null;
            }
            return f.annotations;
        }

        // ---- Initialization ----

        private void initializeFromDirectory(File file) throws IOException {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        initializeFromDirectory(f);
                    }
                }
            } else if (file.getPath().endsWith(FN_ANNOTATIONS_XML)) {
                String xml = Files.toString(file, Charsets.UTF_8);
                initializePackage(xml, file.getPath());
            }
        }

        private void initializeFromJar(File file) throws IOException {
            // Reads in an existing annotations jar and merges in entries found there
            // with the annotations analyzed from source.
            JarInputStream zis = null;
            try {
                @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
                FileInputStream fis = new FileInputStream(file);
                zis = new JarInputStream(fis);
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    if (entry.getName().endsWith(".xml")) {
                        byte[] bytes = ByteStreams.toByteArray(zis);
                        String xml = new String(bytes, Charsets.UTF_8);
                        initializePackage(xml, entry.getName());
                    }
                    entry = zis.getNextEntry();
                }
            } finally {
                try {
                    Closeables.close(zis, true);
                } catch (IOException e) {
                    // pass
                }
            }
        }

        /**
         * Takes the XML contents of an annotations.xml file, parses it and initialize
         * the necessary data structures
         */
        private void initializePackage(@NonNull String xml, @NonNull String path)
                throws IOException {
            try {
                Document document = XmlUtils.parseDocument(xml, false);

                Element root = document.getDocumentElement();
                String rootTag = root.getTagName();
                assert rootTag.equals("root") : rootTag;

                for (Element item : LintUtils.getChildren(root)) {
                    String signature = item.getAttribute(ATTR_NAME);
                    if (signature == null || signature.equals("null")) {
                        continue; // malformed item
                    }

                    signature = XmlUtils.fromXmlAttributeValue(signature);
                    Matcher matcher = XML_SIGNATURE.matcher(signature);
                    if (matcher.matches()) {
                        String containingClass = matcher.group(1);
                        if (containingClass == null) {
                            throw new IOException("Could not find class for " + signature);
                        }
                        String methodName = matcher.group(5);
                        if (methodName != null) {
                            String type = matcher.group(4);
                            boolean isConstructor = type == null;
                            String parameters = matcher.group(6);
                            mergeMethodOrParameter(item, matcher, containingClass, methodName,
                                    isConstructor, parameters);
                        } else {
                            String fieldName = matcher.group(2);
                            mergeField(item, containingClass, fieldName);
                        }
                    } else if (signature.indexOf(' ') == -1 && signature.indexOf('.') != -1) {
                        mergeClass(item, signature);
                    } else {
                        throw new IOException("No merge match for signature " + signature);
                    }
                }
            } catch (Exception e) {
                throw new IOException("Could not parse XML from " + path);
            }
        }

        // SDK annotations
        private Map<String,ClassInfo> mClassMap = Maps.newHashMapWithExpectedSize(800);

        @Nullable
        private ClassInfo findClass(@NonNull ResolvedClass cls) {
            return mClassMap.get(cls.getName());
        }

        @Nullable
        private ClassInfo findClass(@NonNull ResolvedAnnotation cls) {
            return mClassMap.get(cls.getName());
        }

        private ClassInfo findPackage(@NonNull ResolvedPackage pkg) {
            return mClassMap.get(pkg.getName() +".package-info");
        }

        @Nullable
        private MethodInfo findMethod(@NonNull ResolvedMethod method) {
            ClassInfo c = findClass(method.getContainingClass());
            if (c == null) {
                return null;
            }
            if (c.methods == null) {
                return null;
            }
            Collection<MethodInfo> methods = c.methods.get(method.getName());
            if (methods == null) {
                return null;
            }
            boolean constructor = method.isConstructor();
            for (MethodInfo m : methods) {
                if (constructor != m.constructor) {
                    continue;
                }
                // Check parameter types
                // TODO: Perform faster parameter check! This is inefficient
                // Stash parameter count such that I can quickly compare the two
                String signature = m.parameters;
                int index = 0;
                boolean matches = true;
                for (int i = 0, n = method.getArgumentCount(); i < n; i++) {
                    String parameterType = method.getArgumentType(i).getSignature();
                    int length = parameterType.indexOf('<');
                    if (length == -1) {
                        length = parameterType.length();
                    }
                    if (!signature.regionMatches(false, index, parameterType, 0, length)) {
                        // Check if we have a varargs match: x... vs x[]
                        if (length <= 3 || index <= 3 || ((parameterType.charAt(length - 1) != '.')
                                && (signature.length() < index + length
                                || signature.charAt(index + length - 1) != '.'))
                                || !isVarArgsMatch(signature, index, parameterType, length)) {
                                    matches = false;
                                    break;
                        }
                    }
                    index += length;
                    if (i < n - 1) {
                        if (index == signature.length()) {
                            matches = false;
                            break;
                        } else if (signature.charAt(index) == '<') {
                            // Skip raw types
                            int balance = 1;
                            for (int j = index + 1, max = signature.length(); j < max; j++) {
                                char ch = signature.charAt(j);
                                if (ch == '<') {
                                    balance++;
                                } else if (ch == '>') {
                                    balance--;
                                    if (balance == 0) {
                                        index = j + 1;
                                        break;
                                    }
                                }
                            }
                            if (balance > 0) {
                                matches = false;
                                break;
                            }
                        } else if (signature.charAt(index) != ',') {
                            matches = false;
                            break;
                        }
                    }
                    index++; // skip comma
                }

                if (matches) {
                    return m;
                }
            }

            return null;
        }

        /**
         * Checks whether the string at parameterType(0,length) and signature(index,index+length)
         * are the same, except with one possibly ending with [] and the other with ... - if
         * so these should be taken to match
         */
        private static boolean isVarArgsMatch(String signature, int index, String parameterType,
                int length) {
            return parameterType.regionMatches(false, length - 3, "...", 0, 3) &&
                    signature.regionMatches(false, index + length - 3, "[]", 0, 2) &&
                    parameterType.regionMatches(false, 0, signature, index, length - 3)
                    || parameterType.regionMatches(false, length - 2, "[]", 0, 2) &&
                    signature.regionMatches(false, index + length - 2, "...", 0, 3) &&
                    parameterType.regionMatches(false, 0, signature, index, length - 2);
        }

        @Nullable
        private FieldInfo findField(@NonNull ResolvedField field) {
            ClassInfo c = findClass(field.getContainingClass());
            if (c == null) {
                return null;
            }
            if (c.fields == null) {
                return null;
            }
            return c.fields.get(field.getName());
        }

        @NonNull
        private MethodInfo createMethod(@NonNull String containingClass, @NonNull String methodName,
                boolean constructor, @NonNull String parameters) {
            ClassInfo cls = createClass(containingClass);
            if (cls.methods != null) {
                Collection<MethodInfo> methods = cls.methods.get(methodName);
                if (methods != null) {
                    for (MethodInfo method : methods) {
                        if (parameters.equals(method.parameters)
                                && constructor == method.constructor) {
                            return method;
                        }
                    }
                }
            }

            MethodInfo method = new MethodInfo();
            method.parameters = parameters;
            method.constructor = constructor;

            if (cls.methods == null) {
                cls.methods = ArrayListMultimap.create(); // TODO: Size me
            }
            cls.methods.put(methodName, method);
            return method;
        }

        @NonNull
        private ClassInfo createClass(@NonNull String containingClass) {
            ClassInfo cls = mClassMap.get(containingClass);
            if (cls == null) {
                cls = new ClassInfo();
                mClassMap.put(containingClass, cls);
            }
            return cls;
        }

        @NonNull
        private FieldInfo createField(@NonNull String containingClass, @NonNull String fieldName) {
            ClassInfo cls = createClass(containingClass);
            if (cls.fields != null) {
                FieldInfo field = cls.fields.get(fieldName);
                if (field != null) {
                    return field;
                }
            }

            FieldInfo field = new FieldInfo();
            if (cls.fields == null) {
                cls.fields = Maps.newHashMap(); // TODO: Size me
            }
            cls.fields.put(fieldName, field);
            return field;
        }

        private void mergeMethodOrParameter(Element item, Matcher matcher, String containingClass,
                String methodName, boolean constructor, String parameters) {
            parameters = fixParameterString(parameters);

            MethodInfo method = createMethod(containingClass, methodName, constructor, parameters);
            List<ResolvedAnnotation> annotations = createAnnotations(item);

            String argNum = matcher.group(7);
            if (argNum != null) {
                argNum = argNum.trim();
                int parameter = Integer.parseInt(argNum);

                if (method.parameterAnnotations == null) {
                    // Do I know the parameter count here?
                    int parameterCount = 4;
                    method.parameterAnnotations = ArrayListMultimap
                            .create(parameterCount, annotations.size());
                }
                for (ResolvedAnnotation annotation : annotations) {
                    method.parameterAnnotations.put(parameter, annotation);
                }
            } else {
                if (method.annotations == null) {
                    method.annotations = Lists.newArrayListWithExpectedSize(annotations.size());
                }
                method.annotations.addAll(annotations);
            }
        }

        private void mergeField(Element item, String containingClass, String fieldName) {
            FieldInfo field = createField(containingClass, fieldName);
            List<ResolvedAnnotation> annotations = createAnnotations(item);
            if (field.annotations == null) {
                field.annotations = Lists.newArrayListWithExpectedSize(annotations.size());
            }
            field.annotations.addAll(annotations);
        }

        private void mergeClass(Element item, String containingClass) {
            ClassInfo cls = createClass(containingClass);
            List<ResolvedAnnotation> annotations = createAnnotations(item);
            if (cls.annotations == null) {
                cls.annotations = Lists.newArrayListWithExpectedSize(annotations.size());
            }
            cls.annotations.addAll(annotations);
        }

        private List<ResolvedAnnotation> createAnnotations(Element itemElement) {
            List<Element> children = getChildren(itemElement);
            List<ResolvedAnnotation> result = Lists.newArrayListWithExpectedSize(children.size());
            for (Element annotationElement : children) {
                ResolvedAnnotation annotation = createAnnotation(annotationElement);
                result.add(annotation);
            }

            return result;
        }

        private static class ResolvedExternalAnnotation extends ResolvedAnnotation {

            @NonNull
            private String mSignature;

            @Nullable
            private List<Value> mValues;

            public ResolvedExternalAnnotation(@NonNull String signature) {
                mSignature = signature;
            }

            void addValue(@NonNull Value value) {
                if (mValues == null) {
                    mValues = Lists.newArrayList();
                }
                mValues.add(value);
            }

            @NonNull
            @Override
            public String getName() {
                return mSignature;
            }

            @NonNull
            @Override
            public String getSignature() {
                return mSignature;
            }

            @Override
            public int getModifiers() {
                return Modifier.PUBLIC;
            }

            @NonNull
            @Override
            public Iterable<ResolvedAnnotation> getAnnotations() {
                return Collections.emptyList();
            }

            @Override
            public boolean matches(@NonNull String name) {
                return mSignature.equals(name);
            }

            @NonNull
            @Override
            public TypeDescriptor getType() {
                return new DefaultTypeDescriptor(mSignature);
            }

            @Nullable
            @Override
            public ResolvedClass getClassType() {
                // No nested annotations in the database
                return null;
            }

            @NonNull
            @Override
            public List<Value> getValues() {
                return mValues == null ? Collections.<Value>emptyList() : mValues;
            }
        }

        private Map<String, ResolvedExternalAnnotation> mMarkerAnnotations = Maps.newHashMapWithExpectedSize(30);

        private ResolvedAnnotation createAnnotation(Element annotationElement) {
            String tagName = annotationElement.getTagName();
            assert tagName.equals("annotation") : tagName;
            String name = annotationElement.getAttribute(ATTR_NAME);
            assert name != null && !name.isEmpty();

            ResolvedExternalAnnotation annotation = mMarkerAnnotations.get(name);
            if (annotation != null) {
                return annotation;
            }

            annotation = new ResolvedExternalAnnotation(name);

            List<Element> valueElements = getChildren(annotationElement);
            if (valueElements.isEmpty()
                    // Permission annotations are sometimes used as marker annotations (on
                    // parameters) but that shouldn't let us conclude that any future
                    // permission annotations are
                    && !name.startsWith(PERMISSION_ANNOTATION)) {
                mMarkerAnnotations.put(name, annotation);
                return annotation;
            }

            for (Element valueElement : valueElements) {
                if (valueElement.getTagName().equals("val")) {
                    String valueName = valueElement.getAttribute(ATTR_NAME);
                    String valueString = valueElement.getAttribute("val");
                    if (!valueName.isEmpty() && !valueString.isEmpty()) {
                        // Guess type
                        Object value;
                        if (valueString.equals(VALUE_TRUE)) {
                            value = true;
                        } else if (valueString.equals(VALUE_FALSE)) {
                            value = false;
                        } else if (valueString.startsWith("\"") && valueString.endsWith("\"") &&
                                valueString.length() >= 2) {
                            value = valueString.substring(1, valueString.length() - 1);
                        } else if (valueString.startsWith("{") && valueString.endsWith("}")) {
                            // Array of values
                            String listString = valueString.substring(1, valueString.length() - 1);
                            // We don't know the types, but we'll assume that they're either
                            // all strings (the most common array type in our annotations), or
                            // field references. We can't know the types of the fields; it's
                            // not part of the annotation metadata. We'll place them in an Object[]
                            // for now.
                            boolean allStrings = true;
                            Splitter splitter = Splitter.on(',').omitEmptyStrings().trimResults();
                            List<Object> result = Lists.newArrayList();
                            for (String reference : splitter.split(listString)) {
                                if (reference.startsWith("\"")) {
                                    result.add(reference.substring(1, reference.length() - 1));
                                } else {
                                    result.add(new ResolvedExternalField(reference));
                                    allStrings = false;
                                }
                            }
                            if (allStrings) {
                                value = result.toArray(new String[result.size()]);
                            } else {
                                value = result.toArray();
                            }

                            // We don't know the actual type of these fields; we'll assume they're
                            // a special form of
                        } else if (Character.isDigit(valueString.charAt(0))) {
                            try {
                                if (valueString.contains(".")) {
                                    value = Double.parseDouble(valueString);
                                } else {
                                    value = Long.parseLong(valueString);
                                }
                            } catch (NumberFormatException nufe) {
                                value = valueString;
                            }
                        } else {
                            value = valueString; // unknown type
                        }
                        annotation.addValue(new Value(valueName, value));
                    }
                }
            }

            return annotation;
        }
    }

    /** Special implementation of a {@link ResolvedField} which can
     * do equality comparisons with {@link EcjParser.EcjResolvedField} */
    private static class ResolvedExternalField extends ResolvedField {
        private final String mSignature;

        public ResolvedExternalField(String signature) {
            mSignature = signature;
            assert mSignature.indexOf(' ') == -1 :  '"' + mSignature + '"';
        }

        @NonNull
        @Override
        public String getName() {
            return mSignature.substring(mSignature.lastIndexOf('.') + 1);
        }

        @Override
        public String getSignature() {
            return mSignature;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ResolvedExternalField) {
                return mSignature.equals(((ResolvedExternalField)obj).mSignature);
            } else if (obj instanceof ResolvedField) {
                ResolvedField field = (ResolvedField)obj;
                if (mSignature.endsWith(field.getName())) {
                    String signature = field.getContainingClass().getSignature() +
                            "." + field.getName();
                    return mSignature.equals(signature);
                }
                return false;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return mSignature.hashCode();
        }

        @Override
        public int getModifiers() {
            return 0;
        }

        @Override
        public boolean matches(@NonNull String name) {
            return mSignature.equals(name);
        }

        @NonNull
        @Override
        public TypeDescriptor getType() {
            return new DefaultTypeDescriptor(mSignature);
        }

        @NonNull
        @Override
        public ResolvedClass getContainingClass() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getContainingClassName() {
            return mSignature.substring(0, mSignature.lastIndexOf('.'));
        }

        @Nullable
        @Override
        public Object getValue() {
            return null;
        }

        @NonNull
        @Override
        public Iterable<ResolvedAnnotation> getAnnotations() {
            return Collections.emptyList();
        }
    }

    @NonNull
    private static List<Element> getChildren(@NonNull Element element) {
        NodeList itemList = element.getChildNodes();
        int length = itemList.getLength();
        if (length == 0) {
            return Collections.emptyList();
        }
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

    // The parameter declaration used in XML files should not have duplicated spaces,
    // and there should be no space after commas (we can't however strip out all spaces,
    // since for example the spaces around the "extends" keyword needs to be there in
    // types like Map<String,? extends Number>
    private static String fixParameterString(String parameters) {
        return parameters.replace("  ", " ").replace(", ", ",");
    }

    /** For test usage only */
    @VisibleForTesting
    static synchronized void set(ExternalAnnotationRepository singleton) {
        assert singleton == null || sSingleton == null;
        sSingleton = singleton;
    }
}
