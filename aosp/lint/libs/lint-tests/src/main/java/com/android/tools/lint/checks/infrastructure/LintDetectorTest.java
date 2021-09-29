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

package com.android.tools.lint.checks.infrastructure;

import static com.android.SdkConstants.ANDROID_URI;
import static com.android.SdkConstants.ATTR_ID;
import static com.android.SdkConstants.NEW_ID_PREFIX;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.res2.AbstractResourceRepository;
import com.android.ide.common.res2.DuplicateDataException;
import com.android.ide.common.res2.MergingException;
import com.android.ide.common.res2.ResourceFile;
import com.android.ide.common.res2.ResourceItem;
import com.android.ide.common.res2.ResourceMerger;
import com.android.ide.common.res2.ResourceRepository;
import com.android.ide.common.res2.ResourceSet;
import com.android.resources.ResourceFolderType;
import com.android.resources.ResourceType;
import com.android.sdklib.IAndroidTarget;
import com.android.testutils.SdkTestCase;
import com.android.tools.lint.ExternalAnnotationRepository;
import com.android.tools.lint.LintCliClient;
import com.android.tools.lint.LintCliFlags;
import com.android.tools.lint.Reporter;
import com.android.tools.lint.TextReporter;
import com.android.tools.lint.Warning;
import com.android.tools.lint.checks.BuiltinIssueRegistry;
import com.android.tools.lint.client.api.Configuration;
import com.android.tools.lint.client.api.DefaultConfiguration;
import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.client.api.LintClient;
import com.android.tools.lint.client.api.LintDriver;
import com.android.tools.lint.client.api.LintRequest;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.LintUtils;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Project;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;
import com.android.utils.ILogger;
import com.android.utils.SdkUtils;
import com.android.utils.StdLogger;
import com.android.utils.XmlUtils;
import com.google.common.annotations.Beta;
import com.google.common.base.Charsets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import org.intellij.lang.annotations.Language;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test case for lint detectors.
 * <p>
 * <b>NOTE: This is not a public or final API; if you rely on this be prepared
 * to adjust your code for the next tools release.</b>
 */
@Beta
@SuppressWarnings("javadoc")
public abstract class LintDetectorTest extends SdkTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        BuiltinIssueRegistry.reset();
    }

    protected abstract Detector getDetector();

    private Detector mDetector;

    protected final Detector getDetectorInstance() {
        if (mDetector == null) {
            mDetector = getDetector();
        }

        return mDetector;
    }

    protected abstract List<Issue> getIssues();

    public class CustomIssueRegistry extends IssueRegistry {
        @NonNull
        @Override
        public List<Issue> getIssues() {
            return LintDetectorTest.this.getIssues();
        }
    }

    protected String lintFiles(String... relativePaths) throws Exception {
        List<File> files = new ArrayList<File>();
        File targetDir = getTargetDir();
        for (String relativePath : relativePaths) {
            File file = getTestfile(targetDir, relativePath);
            assertNotNull(file);
            files.add(file);
        }

        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                ResourceFolderType folder1 = ResourceFolderType.getFolderType(
                        file1.getParentFile().getName());
                ResourceFolderType folder2 = ResourceFolderType.getFolderType(
                        file2.getParentFile().getName());
                if (folder1 != null && folder2 != null && folder1 != folder2) {
                    return folder1.compareTo(folder2);
                }
                return file1.compareTo(file2);
            }
        });

        addManifestFile(targetDir);

        return checkLint(files);
    }

    protected String checkLint(List<File> files) throws Exception {
        TestLintClient lintClient = createClient();
        return checkLint(lintClient, files);
    }

    protected String checkLint(TestLintClient lintClient, List<File> files) throws Exception {
        if (System.getenv("ANDROID_BUILD_TOP") != null) {
            fail("Don't run the lint tests with $ANDROID_BUILD_TOP set; that enables lint's "
                    + "special support for detecting AOSP projects (looking for .class "
                    + "files in $ANDROID_HOST_OUT etc), and this confuses lint.");
        }

        mOutput = new StringBuilder();
        String result = lintClient.analyze(files);

        // The output typically contains a few directory/filenames.
        // On Windows we need to change the separators to the unix-style
        // forward slash to make the test as OS-agnostic as possible.
        if (File.separatorChar != '/') {
            result = result.replace(File.separatorChar, '/');
        }

        for (File f : files) {
            deleteFile(f);
        }

        return result;
    }

    protected void checkReportedError(
            @NonNull Context context,
            @NonNull Issue issue,
            @NonNull Severity severity,
            @Nullable Location location,
            @NonNull String message) {
    }

    protected TestLintClient createClient() {
        return new TestLintClient();
    }

    protected TestConfiguration getConfiguration(LintClient client, Project project) {
        return new TestConfiguration(client, project, null);
    }

    protected void configureDriver(LintDriver driver) {
    }

    /**
     * Run lint on the given files when constructed as a separate project
     * @return The output of the lint check. On Windows, this transforms all directory
     *   separators to the unix-style forward slash.
     */
    protected String lintProject(String... relativePaths) throws Exception {
        File projectDir = getProjectDir(null, relativePaths);
        return checkLint(Collections.singletonList(projectDir));
    }

    protected String lintProjectIncrementally(String currentFile, String... relativePaths)
            throws Exception {
        File projectDir = getProjectDir(null, relativePaths);
        File current = new File(projectDir, currentFile.replace('/', File.separatorChar));
        assertTrue(current.exists());
        TestLintClient client = createClient();
        client.setIncremental(current);
        return checkLint(client, Collections.singletonList(projectDir));
    }

    protected String lintProjectIncrementally(String currentFile, TestFile... files)
            throws Exception {
        File projectDir = getProjectDir(null, files);
        File current = new File(projectDir, currentFile.replace('/', File.separatorChar));
        assertTrue(current.exists());
        TestLintClient client = createClient();
        client.setIncremental(current);
        return checkLint(client, Collections.singletonList(projectDir));
    }

    /**
     * Run lint on the given files when constructed as a separate project
     * @return The output of the lint check. On Windows, this transforms all directory
     *   separators to the unix-style forward slash.
     */
    protected String lintProject(TestFile... files) throws Exception {
        File projectDir = getProjectDir(null, files);
        return checkLint(Collections.singletonList(projectDir));
    }

    @Override
    protected File getTargetDir() {
        File targetDir = new File(getTempDir(), getClass().getSimpleName() + "_" + getName());
        addCleanupDir(targetDir);
        return targetDir;
    }

    @NonNull
    public TestFile file() {
        return new TestFile();
    }

    @NonNull
    public TestFile source(@NonNull String to, @NonNull String source) {
        return file().to(to).withSource(source);
    }

    @NonNull
    public TestFile java(@NonNull String to, @NonNull @Language("JAVA") String source) {
        return file().to(to).withSource(source);
    }

    @NonNull
    public TestFile xml(@NonNull String to, @NonNull @Language("XML") String source) {
        return file().to(to).withSource(source);
    }

    @NonNull
    public TestFile copy(@NonNull String from, @NonNull String to) {
        return file().from(from).to(to);
    }

    @NonNull
    public TestFile copy(@NonNull String from) {
        return file().from(from).to(from);
    }

    /** Creates a project directory structure from the given files */
    protected File getProjectDir(String name, String ...relativePaths) throws Exception {
        assertFalse("getTargetDir must be overridden to make a unique directory",
                getTargetDir().equals(getTempDir()));

        List<TestFile> testFiles = Lists.newArrayList();
        for (String relativePath : relativePaths) {
            testFiles.add(file().copy(relativePath));
        }
        return getProjectDir(name, testFiles.toArray(new TestFile[testFiles.size()]));
    }

    /** Creates a project directory structure from the given files */
    protected File getProjectDir(String name, TestFile... testFiles) throws Exception {
        assertFalse("getTargetDir must be overridden to make a unique directory",
                getTargetDir().equals(getTempDir()));

        File projectDir = getTargetDir();
        if (name != null) {
            projectDir = new File(projectDir, name);
        }
        if (!projectDir.exists()) {
            assertTrue(projectDir.getPath(), projectDir.mkdirs());
        }

        for (TestFile fp : testFiles) {
            File file = fp.createFile(projectDir);
            assertNotNull(file);
        }

        addManifestFile(projectDir);
        return projectDir;
    }

    private static void addManifestFile(File projectDir) throws IOException {
        // Ensure that there is at least a manifest file there to make it a valid project
        // as far as Lint is concerned:
        if (!new File(projectDir, "AndroidManifest.xml").exists()) {
            File manifest = new File(projectDir, "AndroidManifest.xml");
            FileWriter fw = new FileWriter(manifest);
            fw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    package=\"foo.bar2\"\n" +
                "    android:versionCode=\"1\"\n" +
                "    android:versionName=\"1.0\" >\n" +
                "</manifest>\n");
            fw.close();
        }
    }

    private StringBuilder mOutput = null;

    @Override
    protected InputStream getTestResource(String relativePath, boolean expectExists) {
        String path = "data" + File.separator + relativePath; //$NON-NLS-1$
        InputStream stream = LintDetectorTest.class.getResourceAsStream(path);
        if (!expectExists && stream == null) {
            return null;
        }
        return stream;
    }

    protected boolean isEnabled(Issue issue) {
        Class<? extends Detector> detectorClass = getDetectorInstance().getClass();
        if (issue.getImplementation().getDetectorClass() == detectorClass) {
            return true;
        }

        if (issue == IssueRegistry.LINT_ERROR || issue == IssueRegistry.PARSER_ERROR) {
            return !ignoreSystemErrors();
        }

        return false;
    }

    protected boolean includeParentPath() {
        return false;
    }

    protected EnumSet<Scope> getLintScope(List<File> file) {
        return null;
    }

    public String getSuperClass(Project project, String name) {
        return null;
    }

    protected boolean ignoreSystemErrors() {
        return true;
    }

    public class TestLintClient extends LintCliClient {
        private StringWriter mWriter = new StringWriter();
        private File mIncrementalCheck;

        public TestLintClient() {
            super(new LintCliFlags());
            mFlags.getReporters().add(new TextReporter(this, mFlags, mWriter, false));
        }

        @Override
        public String getSuperClass(@NonNull Project project, @NonNull String name) {
            String superClass = LintDetectorTest.this.getSuperClass(project, name);
            if (superClass != null) {
                return superClass;
            }

            return super.getSuperClass(project, name);
        }

        public String analyze(List<File> files) throws Exception {
            mDriver = new LintDriver(new CustomIssueRegistry(), this);
            configureDriver(mDriver);
            LintRequest request = new LintRequest(this, files);
            if (mIncrementalCheck != null) {
                assertEquals(1, files.size());
                File projectDir = files.get(0);
                assertTrue(isProjectDirectory(projectDir));
                Project project = createProject(projectDir, projectDir);
                project.addFile(mIncrementalCheck);
                List<Project> projects = Collections.singletonList(project);
                request.setProjects(projects);
            }

            mDriver.analyze(request.setScope(getLintScope(files)));

            // Check compare contract
            Warning prev = null;
            for (Warning warning : mWarnings) {
                if (prev != null) {
                    boolean equals = warning.equals(prev);
                    assertEquals(equals, prev.equals(warning));
                    int compare = warning.compareTo(prev);
                    assertEquals(equals, compare == 0);
                    assertEquals(-compare, prev.compareTo(warning));
                }
                prev = warning;
            }

            Collections.sort(mWarnings);

            // Check compare contract & transitivity
            Warning prev2 = prev;
            prev = null;
            for (Warning warning : mWarnings) {
                if (prev != null && prev2 != null) {
                    assertTrue(warning.compareTo(prev) >= 0);
                    assertTrue(prev.compareTo(prev2) >= 0);
                    assertTrue(warning.compareTo(prev2) >= 0);

                    assertTrue(prev.compareTo(warning) <= 0);
                    assertTrue(prev2.compareTo(prev) <= 0);
                    assertTrue(prev2.compareTo(warning) <= 0);
                }
                prev2 = prev;
                prev = warning;
            }

            for (Reporter reporter : mFlags.getReporters()) {
                reporter.write(mErrorCount, mWarningCount, mWarnings);
            }

            mOutput.append(mWriter.toString());

            if (mOutput.length() == 0) {
                mOutput.append("No warnings.");
            }

            String result = mOutput.toString();
            if (result.equals("No issues found.\n")) {
                result = "No warnings.";
            }

            result = cleanup(result);

            return result;
        }

        public String getErrors() throws Exception {
            return mWriter.toString();
        }

        @Override
        public void report(
                @NonNull Context context,
                @NonNull Issue issue,
                @NonNull Severity severity,
                @Nullable Location location,
                @NonNull String message,
                @NonNull TextFormat format) {
            if (ignoreSystemErrors() && (issue == IssueRegistry.LINT_ERROR)) {
                return;
            }

            // Use plain ascii in the test golden files for now. (This also ensures
            // that the markup is wellformed, e.g. if we have a ` without a matching
            // closing `, the ` would show up in the plain text.)
            message = format.convertTo(message, TextFormat.TEXT);

            checkReportedError(context, issue, severity, location, message);

            if (severity == Severity.FATAL) {
                // Treat fatal errors like errors in the golden files.
                severity = Severity.ERROR;
            }

            // For messages into all secondary locations to ensure they get
            // specifically included in the text report
            if (location != null && location.getSecondary() != null) {
                Location l = location.getSecondary();
                if (l == location) {
                    fail("Location link cycle");
                }
                while (l != null) {
                    if (l.getMessage() == null) {
                        l.setMessage("<No location-specific message");
                    }
                    if (l == l.getSecondary()) {
                        fail("Location link cycle");
                    }
                    l = l.getSecondary();
                }
            }

            super.report(context, issue, severity, location, message, format);

            // Make sure errors are unique!
            Warning prev = null;
            for (Warning warning : mWarnings) {
                assertNotSame(warning, prev);
                assert prev == null || !warning.equals(prev) : warning;
                prev = warning;
            }
        }

        @Override
        public void log(Throwable exception, String format, Object... args) {
            if (exception != null) {
                exception.printStackTrace();
            }
            StringBuilder sb = new StringBuilder();
            if (format != null) {
                sb.append(String.format(format, args));
            }
            if (exception != null) {
                sb.append(exception.toString());
            }
            System.err.println(sb);

            if (exception != null) {
                fail(exception.toString());
            }
        }

        @NonNull
        @Override
        public Configuration getConfiguration(@NonNull Project project,
                @Nullable LintDriver driver) {
            return LintDetectorTest.this.getConfiguration(this, project);
        }

        @Override
        public File findResource(@NonNull String relativePath) {
            if (relativePath.equals("platform-tools/api/api-versions.xml")) {
                // Look in the current Git repository and try to find it there
                File rootDir = getRootDir();
                if (rootDir != null) {
                    File file = new File(rootDir, "development" + File.separator + "sdk"
                            + File.separator + "api-versions.xml");
                    if (file.exists()) {
                        return file;
                    }
                }
                // Look in an SDK install, if found
                File home = getSdkHome();
                if (home != null) {
                    return new File(home, relativePath);
                }
            } else if (relativePath.equals(ExternalAnnotationRepository.SDK_ANNOTATIONS_PATH)) {
                // Look in the current Git repository and try to find it there
                File rootDir = getRootDir();
                if (rootDir != null) {
                    File file = new File(rootDir,
                            "tools" + File.separator
                            + "adt" + File.separator
                            + "idea" + File.separator
                            + "android" + File.separator
                            + "annotations");
                    if (file.exists()) {
                        return file;
                    }
                }
                // Look in an SDK install, if found
                File home = getSdkHome();
                if (home != null) {
                    File file = new File(home, relativePath);
                    return file.exists() ? file : null;
                }
            } else if (relativePath.startsWith("tools/support/")) {
                // Look in the current Git repository and try to find it there
                String base = relativePath.substring("tools/support/".length());
                File rootDir = getRootDir();
                if (rootDir != null) {
                    File file = new File(rootDir, "tools"
                            + File.separator + "base"
                            + File.separator + "files"
                            + File.separator + "typos"
                            + File.separator + base);
                    if (file.exists()) {
                        return file;
                    }
                }
                // Look in an SDK install, if found
                File home = getSdkHome();
                if (home != null) {
                    return new File(home, relativePath);
                }
            } else {
                fail("Unit tests don't support arbitrary resource lookup yet.");
            }

            return super.findResource(relativePath);
        }

        @NonNull
        @Override
        public List<File> findGlobalRuleJars() {
            // Don't pick up random custom rules in ~/.android/lint when running unit tests
            return Collections.emptyList();
        }

        public void setIncremental(File currentFile) {
            mIncrementalCheck = currentFile;
        }

        @Override
        public boolean supportsProjectResources() {
            return mIncrementalCheck != null;
        }

        @Nullable
        @Override
        public AbstractResourceRepository getProjectResources(Project project,
                boolean includeDependencies) {
            if (mIncrementalCheck == null) {
                return null;
            }

            ResourceRepository repository = new ResourceRepository(false);
            ILogger logger = new StdLogger(StdLogger.Level.INFO);
            ResourceMerger merger = new ResourceMerger();

            ResourceSet resourceSet = new ResourceSet(getName()) {
                @Override
                protected void checkItems() throws DuplicateDataException {
                    // No checking in ProjectResources; duplicates can happen, but
                    // the project resources shouldn't abort initialization
                }
            };
            // Only support 1 resource folder in test setup right now
            int size = project.getResourceFolders().size();
            assertTrue("Found " + size + " test resources folders", size <= 1);
            if (size == 1) {
                resourceSet.addSource(project.getResourceFolders().get(0));
            }
            try {
                resourceSet.loadFromFiles(logger);
                merger.addDataSet(resourceSet);
                merger.mergeData(repository.createMergeConsumer(), true);

                // Make tests stable: sort the item lists!
                Map<ResourceType, ListMultimap<String, ResourceItem>> map = repository.getItems();
                for (Map.Entry<ResourceType, ListMultimap<String, ResourceItem>> entry : map.entrySet()) {
                    Map<String, List<ResourceItem>> m = Maps.newHashMap();
                    ListMultimap<String, ResourceItem> value = entry.getValue();
                    List<List<ResourceItem>> lists = Lists.newArrayList();
                    for (Map.Entry<String, ResourceItem> e : value.entries()) {
                        String key = e.getKey();
                        ResourceItem item = e.getValue();

                        List<ResourceItem> list = m.get(key);
                        if (list == null) {
                            list = Lists.newArrayList();
                            lists.add(list);
                            m.put(key, list);
                        }
                        list.add(item);
                    }

                    for (List<ResourceItem> list : lists) {
                        Collections.sort(list, new Comparator<ResourceItem>() {
                            @Override
                            public int compare(ResourceItem o1, ResourceItem o2) {
                                return o1.getKey().compareTo(o2.getKey());
                            }
                        });
                    }

                    // Store back in list multi map in new sorted order
                    value.clear();
                    for (Map.Entry<String, List<ResourceItem>> e : m.entrySet()) {
                        String key = e.getKey();
                        List<ResourceItem> list = e.getValue();
                        for (ResourceItem item : list) {
                            value.put(key, item);
                        }
                    }
                }

                // Workaround: The repository does not insert ids from layouts! We need
                // to do that here.
                Map<ResourceType,ListMultimap<String,ResourceItem>> items = repository.getItems();
                ListMultimap<String, ResourceItem> layouts = items
                        .get(ResourceType.LAYOUT);
                if (layouts != null) {
                    for (ResourceItem item : layouts.values()) {
                        ResourceFile source = item.getSource();
                        if (source == null) {
                            continue;
                        }
                        File file = source.getFile();
                        try {
                            String xml = Files.toString(file, Charsets.UTF_8);
                            Document document = XmlUtils.parseDocumentSilently(xml, true);
                            assertNotNull(document);
                            Set<String> ids = Sets.newHashSet();
                            addIds(ids, document); // TODO: pull parser
                            if (!ids.isEmpty()) {
                                ListMultimap<String, ResourceItem> idMap =
                                        items.get(ResourceType.ID);
                                if (idMap == null) {
                                    idMap = ArrayListMultimap.create();
                                    items.put(ResourceType.ID, idMap);
                                }
                                for (String id : ids) {
                                    ResourceItem idItem = new ResourceItem(id, ResourceType.ID,
                                            null);
                                    String qualifiers = file.getParentFile().getName();
                                    if (qualifiers.startsWith("layout-")) {
                                        qualifiers = qualifiers.substring("layout-".length());
                                    } else if (qualifiers.equals("layout")) {
                                        qualifiers = "";
                                    }
                                    idItem.setSource(new ResourceFile(file, item, qualifiers));
                                    idMap.put(id, idItem);
                                }
                            }
                        } catch (IOException e) {
                            fail(e.toString());
                        }
                    }
                }
            }
            catch (MergingException e) {
                fail(e.getMessage());
            }

            return repository;
        }

        private void addIds(Set<String> ids, Node node) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String id = element.getAttributeNS(ANDROID_URI, ATTR_ID);
                if (id != null && !id.isEmpty()) {
                    ids.add(LintUtils.stripIdPrefix(id));
                }

                NamedNodeMap attributes = element.getAttributes();
                for (int i = 0, n = attributes.getLength(); i < n; i++) {
                    Attr attribute = (Attr) attributes.item(i);
                    String value = attribute.getValue();
                    if (value.startsWith(NEW_ID_PREFIX)) {
                        ids.add(value.substring(NEW_ID_PREFIX.length()));
                    }
                }
            }

            NodeList children = node.getChildNodes();
            for (int i = 0, n = children.getLength(); i < n; i++) {
                Node child = children.item(i);
                addIds(ids, child);
            }
        }

        @Nullable
        @Override
        public IAndroidTarget getCompileTarget(@NonNull Project project) {
            IAndroidTarget compileTarget = super.getCompileTarget(project);
            if (compileTarget == null) {
                IAndroidTarget[] targets = getTargets();
                for (int i = targets.length - 1; i >= 0; i--) {
                    IAndroidTarget target = targets[i];
                    if (target.isPlatform()) {
                        return target;
                    }
                }
            }

            return compileTarget;
        }

        @NonNull
        @Override
        public List<File> getTestSourceFolders(@NonNull Project project) {
            List<File> testSourceFolders = super.getTestSourceFolders(project);

            //List<File> tests = new ArrayList<File>();
            File tests = new File(project.getDir(), "test");
            if (tests.exists()) {
                List<File> all = Lists.newArrayList(testSourceFolders);
                all.add(tests);
                testSourceFolders = all;
            }

            return testSourceFolders;
        }
    }

    /**
     * Returns the Android source tree root dir.
     * @return the root dir or null if it couldn't be computed.
     */
    protected File getRootDir() {
        CodeSource source = getClass().getProtectionDomain().getCodeSource();
        if (source != null) {
            URL location = source.getLocation();
            try {
                File dir = SdkUtils.urlToFile(location);
                assertTrue(dir.getPath(), dir.exists());
                while (dir != null) {
                    File settingsGradle = new File(dir, "settings.gradle"); //$NON-NLS-1$
                    if (settingsGradle.exists()) {
                        return dir.getParentFile().getParentFile();
                    }
                    File lint = new File(dir, "lint");  //$NON-NLS-1$
                    if (lint.exists() && new File(lint, "cli").exists()) { //$NON-NLS-1$
                        return dir.getParentFile().getParentFile();
                    }
                    dir = dir.getParentFile();
                }

                return null;
            } catch (MalformedURLException e) {
                fail(e.getLocalizedMessage());
            }
        }

        return null;
    }

    public class TestConfiguration extends DefaultConfiguration {
        protected TestConfiguration(
                @NonNull LintClient client,
                @NonNull Project project,
                @Nullable Configuration parent) {
            super(client, project, parent);
        }

        public TestConfiguration(
                @NonNull LintClient client,
                @Nullable Project project,
                @Nullable Configuration parent,
                @NonNull File configFile) {
            super(client, project, parent, configFile);
        }

        @Override
        @NonNull
        protected Severity getDefaultSeverity(@NonNull Issue issue) {
            // In unit tests, include issues that are ignored by default
            Severity severity = super.getDefaultSeverity(issue);
            if (severity == Severity.IGNORE) {
                if (issue.getDefaultSeverity() != Severity.IGNORE) {
                    return issue.getDefaultSeverity();
                }
                return Severity.WARNING;
            }
            return severity;
        }

        @Override
        public boolean isEnabled(@NonNull Issue issue) {
            return LintDetectorTest.this.isEnabled(issue);
        }

        @Override
        public void ignore(@NonNull Context context, @NonNull Issue issue,
                @Nullable Location location, @NonNull String message) {
            fail("Not supported in tests.");
        }

        @Override
        public void setSeverity(@NonNull Issue issue, @Nullable Severity severity) {
            fail("Not supported in tests.");
        }
    }
}
