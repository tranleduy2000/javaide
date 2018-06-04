package com.duy.android.compiler.builder.task.android;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.dependency.ManifestDependency;
import com.android.ide.common.internal.CommandLineRunner;
import com.android.manifmerger.ICallback;
import com.android.manifmerger.ManifestMerger;
import com.android.manifmerger.MergerLog;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.repository.FullRevision;
import com.android.utils.ILogger;
import com.android.utils.SdkUtils;
import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.sdk.SdkInfo;
import com.duy.android.compiler.builder.sdk.TargetInfo;
import com.duy.android.compiler.builder.task.ATask;
import com.duy.android.compiler.project.AndroidAppProject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class MergeManifestTask extends ATask<AndroidAppProject> {
    private static final String TAG = "MergeManifestTask";

    private static final FullRevision MIN_BUILD_TOOLS_REV = new FullRevision(19, 0, 0);


    private final ILogger mLogger;
    private final CommandLineRunner mCmdLineRunner;
    private final boolean mVerboseExec;

    private String mCreatedBy;

    private SdkInfo mSdkInfo;
    private TargetInfo mTargetInfo;

    public MergeManifestTask(IBuilder<? extends AndroidAppProject> builder,
                             @NonNull ILogger log,
                             boolean verboseExec) {
        super(builder);
        mLogger = log;
        mVerboseExec = verboseExec;
        mCmdLineRunner = new CommandLineRunner(mLogger);
    }

    @NonNull
    private static Map<String, String> getAttributeInjectionMap(
            int versionCode,
            @Nullable String versionName,
            @Nullable String minSdkVersion,
            int targetSdkVersion) {

        Map<String, String> attributeInjection = Maps.newHashMap();

        if (versionCode != -1) {
            attributeInjection.put(
                    "/manifest|http://schemas.android.com/apk/res/android versionCode",
                    Integer.toString(versionCode));
        }

        if (versionName != null) {
            attributeInjection.put(
                    "/manifest|http://schemas.android.com/apk/res/android versionName",
                    versionName);
        }

        if (minSdkVersion != null) {
            attributeInjection.put(
                    "/manifest/uses-sdk|http://schemas.android.com/apk/res/android minSdkVersion",
                    minSdkVersion);
        }

        if (targetSdkVersion != -1) {
            attributeInjection.put(
                    "/manifest/uses-sdk|http://schemas.android.com/apk/res/android targetSdkVersion",
                    Integer.toString(targetSdkVersion));
        }
        return attributeInjection;
    }

    /**
     * Sets the SdkInfo and the targetInfo on the builder. This is required to actually
     * build (some of the steps).
     *
     * @param sdkInfo    the SdkInfo
     * @param targetInfo the TargetInfo
     *                   <p>
     *                   //     * @see com.android.builder.sdk.SdkLoader
     */
    public void setTargetInfo(@NonNull SdkInfo sdkInfo, @NonNull TargetInfo targetInfo) {
        mSdkInfo = sdkInfo;
        mTargetInfo = targetInfo;

        if (mTargetInfo.getBuildTools().getRevision().compareTo(MIN_BUILD_TOOLS_REV) < 0) {
            throw new IllegalArgumentException(String.format(
                    "The SDK Build Tools revision (%1$s) is too low. Minimum required is %2$s",
                    mTargetInfo.getBuildTools().getRevision(), MIN_BUILD_TOOLS_REV));
        }
    }

    @Override
    public String getTaskName() {
        return "Merge manifest";
    }

    @Override
    public boolean run() throws Exception {
        mBuilder.stderr(TAG + ": Not impalement yet");
        return true;
    }

    /**
     * Merges all the manifests into a single manifest
     *
     * @param mainManifest        The main manifest of the application.
     * @param manifestOverlays    manifest overlays coming from flavors and build types
     * @param libraries           the library dependency graph
     * @param packageOverride     a package name override. Can be null.
     * @param versionCode         a version code to inject in the manifest or -1 to do nothing.
     * @param versionName         a version name to inject in the manifest or null to do nothing.
     * @param minSdkVersion       a minSdkVersion to inject in the manifest or -1 to do nothing.
     * @param targetSdkVersion    a targetSdkVersion to inject in the manifest or -1 to do nothing.
     * @param outManifestLocation the output location for the merged manifest
     *                            //     * @see com.android.builder.VariantConfiguration#getMainManifest()
     *                            //     * @see com.android.builder.VariantConfiguration#getManifestOverlays()
     *                            //     * @see com.android.builder.VariantConfiguration#getDirectLibraries()
     *                            //     * @see com.android.builder.VariantConfiguration#getMergedFlavor()
     *                            //     * @see DefaultProductFlavor#getVersionCode()
     *                            //     * @see DefaultProductFlavor#getVersionName()
     *                            //     * @see DefaultProductFlavor#getMinSdkVersion()
     *                            //     * @see DefaultProductFlavor#getTargetSdkVersion()
     */
    public void processManifest(
            @NonNull File mainManifest,
            @NonNull List<File> manifestOverlays,
            @NonNull List<? extends ManifestDependency> libraries,
            String packageOverride,
            int versionCode,
            String versionName,
            @Nullable String minSdkVersion,
            int targetSdkVersion,
            @NonNull String outManifestLocation) {
        checkNotNull(mainManifest, "mainManifest cannot be null.");
        checkNotNull(manifestOverlays, "manifestOverlays cannot be null.");
        checkNotNull(libraries, "libraries cannot be null.");
        checkNotNull(outManifestLocation, "outManifestLocation cannot be null.");
        checkState(mTargetInfo != null,
                "Cannot call processManifest() before setTargetInfo() is called.");

        final IAndroidTarget target = mTargetInfo.getTarget();

        ICallback callback = new ICallback() {
            @Override
            public int queryCodenameApiLevel(@NonNull String codename) {
                if (codename.equals(target.getVersion().getCodename())) {
                    return target.getVersion().getApiLevel();
                }
                return ICallback.UNKNOWN_CODENAME;
            }
        };

        try {
            Map<String, String> attributeInjection = getAttributeInjectionMap(
                    versionCode, versionName, minSdkVersion, targetSdkVersion);

            if (manifestOverlays.isEmpty() && libraries.isEmpty()) {
                // if no manifest to merge, just copy to location, unless we have to inject
                // attributes
                if (attributeInjection.isEmpty() && packageOverride == null) {
                    SdkUtils.copyXmlWithSourceReference(mainManifest,
                            new File(outManifestLocation));
                } else {
                    ManifestMerger merger = new ManifestMerger(MergerLog.wrapSdkLog(mLogger),
                            callback);
                    doMerge(merger, new File(outManifestLocation), mainManifest,
                            attributeInjection, packageOverride);
                }
            } else {
                File outManifest = new File(outManifestLocation);

                // first merge the app manifest.
                if (!manifestOverlays.isEmpty()) {
                    File mainManifestOut = outManifest;

                    // if there is also libraries, put this in a temp file.
                    if (!libraries.isEmpty()) {
                        // TODO find better way of storing intermediary file?
                        mainManifestOut = File.createTempFile("manifestMerge", ".xml");
                        mainManifestOut.deleteOnExit();
                    }

                    ManifestMerger merger = new ManifestMerger(MergerLog.wrapSdkLog(mLogger),
                            callback);
                    doMerge(merger, mainManifestOut, mainManifest, manifestOverlays,
                            attributeInjection, packageOverride);

                    // now the main manifest is the newly merged one
                    mainManifest = mainManifestOut;
                    // and the attributes have been inject, no need to do it below
                    attributeInjection = null;
                }

                if (!libraries.isEmpty()) {
                    // recursively merge all manifests starting with the leaves and up toward the
                    // root (the app)
                    mergeLibraryManifests(mainManifest, libraries,
                            new File(outManifestLocation), attributeInjection, packageOverride,
                            callback);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Merges library manifests into a main manifest.
     *
     * @param mainManifest    the main manifest
     * @param directLibraries the libraries to merge
     * @param outManifest     the output file
     * @throws IOException
     */
    private void mergeLibraryManifests(
            File mainManifest,
            Iterable<? extends ManifestDependency> directLibraries,
            File outManifest, Map<String, String> attributeInjection,
            String packageOverride,
            @NonNull ICallback callback)
            throws IOException {

        List<File> manifests = Lists.newArrayList();
        for (ManifestDependency library : directLibraries) {
            Collection<? extends ManifestDependency> subLibraries = library.getManifestDependencies();
            if (subLibraries.isEmpty()) {
                manifests.add(library.getManifest());
            } else {
                File mergeLibManifest = File.createTempFile("manifestMerge", ".xml");
                mergeLibManifest.deleteOnExit();

                // don't insert the attribute injection into libraries
                mergeLibraryManifests(
                        library.getManifest(), subLibraries, mergeLibManifest, null, null, callback);

                manifests.add(mergeLibManifest);
            }
        }

        ManifestMerger merger = new ManifestMerger(MergerLog.wrapSdkLog(mLogger), callback);
        doMerge(merger, outManifest, mainManifest, manifests, attributeInjection, packageOverride);
    }

    private void doMerge(ManifestMerger merger, File output, File input,
                         Map<String, String> injectionMap, String packageOverride) {
        List<File> list = Collections.emptyList();
        doMerge(merger, output, input, list, injectionMap, packageOverride);
    }

    private void doMerge(ManifestMerger merger, File output, File input, List<File> subManifests,
                         Map<String, String> injectionMap, String packageOverride) {
        if (!merger.process(output, input,
                subManifests.toArray(new File[subManifests.size()]),
                injectionMap, packageOverride)) {
            throw new RuntimeException("Manifest merging failed. See console for more info.");
        }
    }

}
