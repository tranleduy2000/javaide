package com.duy.android.compiler.builder.task.android;

import android.os.Build;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.core.AaptPackageProcessBuilder;
import com.android.builder.core.VariantConfiguration;
import com.android.builder.core.VariantType;
import com.android.builder.dependency.SymbolFileProvider;
import com.android.builder.internal.SymbolLoader;
import com.android.builder.internal.SymbolWriter;
import com.android.builder.model.AaptOptions;
import com.android.builder.sdk.TargetInfo;
import com.android.ide.common.process.LoggedProcessOutputHandler;
import com.android.ide.common.process.ProcessException;
import com.android.ide.common.process.ProcessInfo;
import com.android.ide.common.process.ProcessOutputHandler;
import com.android.ide.common.process.ProcessResult;
import com.android.utils.FileUtils;
import com.android.utils.ILogger;
import com.duy.android.compiler.builder.AndroidAppBuilder;
import com.android.build.gradle.internal.AaptOptionsImpl;
import com.duy.android.compiler.builder.task.ATask;
import com.duy.android.compiler.env.Environment;
import com.duy.android.compiler.project.AndroidAppProject;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkState;

public class ProcessAndroidResourceTask2 extends ATask<AndroidAppProject> {
    @NonNull
    private final ILogger mLogger;
    private File textSymbolOutputDir;
    private boolean debuggable;
    private String preferredDensity;
    private Collection<String> resourceConfigs;
    private boolean pseudoLocalesEnabled;
    private Collection<String> splits;
    private File proguardOutputFile;
    private File packageOutputFile;
    private File sourceOutputDir;
    private File resDir;
    private VariantType type;
    private TargetInfo mTargetInfo;

    public ProcessAndroidResourceTask2(AndroidAppBuilder builder) {
        super(builder);
        mLogger = mBuilder.getLogger();
        setDefaultValue();
    }

    @Nullable
    private static String absolutePath(@Nullable File file) {
        return file == null ? null : file.getAbsolutePath();
    }

    private void setDefaultValue() {
        File textSymbolOutputDir = new File(project.getDirBuildIntermediates(), "symbols");
        textSymbolOutputDir.mkdirs();

        setTextSymbolOutputDir(textSymbolOutputDir);
        setDebuggable(true);
        setPseudoLocalesEnabled(false);
        setProguardOutputFile(null);
        setPackageOutputFile(project.getProcessResourcePackageOutputFile());
        setSourceOutputDir(project.getRClassSourceOutputDir());
        setType(VariantType.DEFAULT);
        setTargetInfo(getBuilder().getTargetInfo());
    }

    private AndroidAppBuilder getBuilder() {
        return (AndroidAppBuilder) mBuilder;
    }

    @Override
    public String getTaskName() {
        return "Process android resource";
    }

    public boolean run() throws Exception {
        String arch = Build.CPU_ABI.substring(0, 3).toLowerCase(Locale.US);
        String aaptName;

        // Position Independent Executables (PIE) were first supported in Jelly Bean 4.1 (API level 16)
        // In Android 5.0, they are required
        // Android versions before 4.1 still need the old binary...
        boolean usePie = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
        // Get the correct AAPT binary for this processor architecture
        switch (arch) {
            case "x86":
                if (usePie) {
                    aaptName = "aapt-x86-pie";
                } else {
                    aaptName = "aapt-x86";
                }
                break;
            case "arm":
            default:
                // Default to ARM, just in case
                if (usePie) {
                    aaptName = "aapt-arm-pie";
                } else {
                    aaptName = "aapt-arm";
                }
                break;
        }
        File aaptFile = new File(Environment.getBinDir(context), aaptName);
        AaptOptions aaptOptions = new AaptOptionsImpl(null, null, false, new ArrayList<String>());

        // we have to clean the source folder output in case the package name changed.
        File srcOut = getSourceOutputDir();
        if (srcOut != null) {
            FileUtils.emptyFolder(srcOut);
        }
        File resOutBaseNameFile = getPackageOutputFile();

        // we have to check the resource output folder in case some splits were removed, we should
        // manually remove them.
        File packageOutputFolder = getResDir();
        if (resOutBaseNameFile != null) {
            for (File file : packageOutputFolder.listFiles()) {
                if (!isSplitPackage(file, resOutBaseNameFile)) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        }

        AaptPackageProcessBuilder aaptPackageCommandBuilder =
                new AaptPackageProcessBuilder(project.getManifestFile(), aaptOptions)
                        .setAssetsFolder(project.getAssetsDir())
                        .setResFolder(project.getResDir())
                        .setLibraries(project.getLibraries())
                        .setPackageForR(project.getPackageForR())
                        .setSourceOutputDir(absolutePath(srcOut))
                        .setSymbolOutputDir(absolutePath(getTextSymbolOutputDir()))
                        .setResPackageOutput(absolutePath(resOutBaseNameFile))
                        .setProguardOutput(absolutePath(getProguardOutputFile()))
                        .setType(getType())
                        .setDebuggable(getDebuggable())
                        .setPseudoLocalesEnabled(getPseudoLocalesEnabled())
                        .setResourceConfigs(getResourceConfigs())
                        .setSplits(getSplits())
                        .setPreferredDensity(getPreferredDensity());

        LoggedProcessOutputHandler outputHandler = new LoggedProcessOutputHandler(mBuilder.getLogger());
        processResources(aaptPackageCommandBuilder, false, outputHandler);

        return false;
    }

    private boolean isSplitPackage(File file, File resBaseName) {
        if (file.getName().startsWith(resBaseName.getName())) {
            for (String split : splits) {
                if (file.getName().contains(split)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Does not change between incremental builds, so does not need to be @Input.
     */
    public VariantType getType() {
        return type;
    }

    public void setType(VariantType type) {
        this.type = type;
    }

    @NonNull
    public File getResDir() {
        return project.getResDir();
    }

    public File getSourceOutputDir() {
        return sourceOutputDir;
    }

    public void setSourceOutputDir(File sourceOutputDir) {
        this.sourceOutputDir = sourceOutputDir;
    }

    public File getPackageOutputFile() {
        return packageOutputFile;
    }

    public void setPackageOutputFile(File packageOutputFile) {
        this.packageOutputFile = packageOutputFile;
    }

    public Collection<String> getSplits() {
        return splits;
    }

    public void setSplits(Collection<String> splits) {
        this.splits = splits;
    }

    public File getProguardOutputFile() {
        return proguardOutputFile;
    }

    public void setProguardOutputFile(File proguardOutputFile) {
        this.proguardOutputFile = proguardOutputFile;
    }

    public Collection<String> getResourceConfigs() {
        return resourceConfigs;
    }

    public void setResourceConfigs(Collection<String> resourceConfigs) {
        this.resourceConfigs = resourceConfigs;
    }

    public boolean getDebuggable() {
        return debuggable;
    }

    public void setDebuggable(boolean debuggable) {
        this.debuggable = debuggable;
    }

    public void setTargetInfo(TargetInfo mTargetInfo) {
        this.mTargetInfo = mTargetInfo;
    }

    public File getTextSymbolOutputDir() {
        return textSymbolOutputDir;
    }

    public void setTextSymbolOutputDir(File textSymbolOutputDir) {
        this.textSymbolOutputDir = textSymbolOutputDir;
    }

    public boolean getPseudoLocalesEnabled() {
        return pseudoLocalesEnabled;
    }

    public void setPseudoLocalesEnabled(boolean pseudoLocalesEnabled) {
        this.pseudoLocalesEnabled = pseudoLocalesEnabled;
    }

    /**
     * Process the resources and generate R.java and/or the packaged resources.
     *
     * @param aaptCommand              aapt command invocation parameters.
     * @param enforceUniquePackageName if true method will fail if some libraries share the same
     *                                 package name
     * @throws IOException
     * @throws InterruptedException
     * @throws ProcessException
     */
    public void processResources(
            @NonNull AaptPackageProcessBuilder aaptCommand,
            boolean enforceUniquePackageName,
            @NonNull ProcessOutputHandler processOutputHandler)
            throws IOException, InterruptedException, ProcessException {

        checkState(mTargetInfo != null,
                "Cannot call processResources() before setTargetInfo() is called.");

        // launch aapt: create the command line
        ProcessInfo processInfo = aaptCommand.build(
                mTargetInfo.getBuildTools(), mTargetInfo.getTarget(), mLogger);

        ProcessResult result = mProcessExecutor.execute(processInfo, processOutputHandler);
        result.rethrowFailure().assertNormalExitValue();

        // now if the project has libraries, R needs to be created for each libraries,
        // but only if the current project is not a library.
        if (aaptCommand.getSourceOutputDir() != null
                && aaptCommand.getType() != VariantType.LIBRARY
                && !aaptCommand.getLibraries().isEmpty()) {
            SymbolLoader fullSymbolValues = null;

            // First pass processing the libraries, collecting them by packageName,
            // and ignoring the ones that have the same package name as the application
            // (since that R class was already created).
            String appPackageName = aaptCommand.getPackageForR();
            if (appPackageName == null) {
                appPackageName = VariantConfiguration.getManifestPackage(aaptCommand.getManifestFile());
            }

            // list of all the symbol loaders per package names.
            Multimap<String, SymbolLoader> libMap = ArrayListMultimap.create();

            for (SymbolFileProvider lib : aaptCommand.getLibraries()) {
                if (lib.isOptional()) {
                    continue;
                }
                String packageName = VariantConfiguration.getManifestPackage(lib.getManifest());
                if (appPackageName == null) {
                    continue;
                }

                if (appPackageName.equals(packageName)) {
                    if (enforceUniquePackageName) {
                        String msg = String.format(
                                "Error: A library uses the same package as this project: %s",
                                packageName);
                        throw new RuntimeException(msg);
                    }

                    // ignore libraries that have the same package name as the app
                    continue;
                }

                File rFile = lib.getSymbolFile();
                // if the library has no resource, this file won't exist.
                if (rFile.isFile()) {

                    // load the full values if that's not already been done.
                    // Doing it lazily allow us to support the case where there's no
                    // resources anywhere.
                    if (fullSymbolValues == null) {
                        fullSymbolValues = new SymbolLoader(new File(aaptCommand.getSymbolOutputDir(), "R.txt"),
                                mLogger);
                        fullSymbolValues.load();
                    }

                    SymbolLoader libSymbols = new SymbolLoader(rFile, mLogger);
                    libSymbols.load();


                    // store these symbols by associating them with the package name.
                    libMap.put(packageName, libSymbols);
                }
            }

            // now loop on all the package name, merge all the symbols to write, and write them
            for (String packageName : libMap.keySet()) {
                Collection<SymbolLoader> symbols = libMap.get(packageName);

                if (enforceUniquePackageName && symbols.size() > 1) {
                    String msg = String.format(
                            "Error: more than one library with package name '%s'\n" +
                                    "You can temporarily disable this error with android.enforceUniquePackageName=false\n" +
                                    "However, this is temporary and will be enforced in 1.0", packageName);
                    throw new RuntimeException(msg);
                }

                SymbolWriter writer = new SymbolWriter(aaptCommand.getSourceOutputDir(), packageName,
                        fullSymbolValues);
                for (SymbolLoader symbolLoader : symbols) {
                    writer.addSymbolsToWrite(symbolLoader);
                }
                writer.write();
            }
        }
    }

    public String getPreferredDensity() {
        return preferredDensity;
    }

    public static final class AAPTOptions {
        /**
         * android.jar
         */
        private static final String ANDROID_JAR_PATH = "androidJarPath";
        /**
         * Output directory for R.java
         */
        private static final String SOURCE_OUTPUT_DIR = "sourceOutputDir";
        /**
         * Output directory for resource.ap_
         */
        private static final String RESOURCE_OUTPUT_APK = "resourceOutputApk";
        /**
         * Public resource definition of library R.txt
         */
        private static final String LIBRARY_SYMBOL_TABLE_FILES = "librarySymbolTableFiles";
        /**
         * Output public resource definition for application
         */
        private static final String OUTPUT_TEXT_SYMBOL = "--output-text-symbols";

        private static final String VERBOSE = "verbose";

        private static final String PROGUARD_OUTPUT_FILE = "proguardOutputFile";

        private static final String MAIN_DEX_LIST_PROGUARD_OUTPUT_FILE = "mainDexListProguardOutputFile";

        private static final String customPackageForR = "customPackageForR";
    }

}