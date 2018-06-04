package com.duy.android.compiler.builder.task.android;

import android.os.Build;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.core.AaptPackageProcessBuilder;
import com.android.builder.core.VariantConfiguration;
import com.android.builder.core.VariantType;
import com.android.builder.dependency.LibraryDependency;
import com.android.builder.dependency.SymbolFileProvider;
import com.android.builder.internal.SymbolLoader;
import com.android.builder.internal.SymbolWriter;
import com.android.builder.model.AaptOptions;
import com.android.ide.common.process.ProcessException;
import com.android.ide.common.process.ProcessExecutor;
import com.android.ide.common.process.ProcessInfo;
import com.android.ide.common.process.ProcessOutputHandler;
import com.android.ide.common.process.ProcessResult;
import com.android.sdklib.BuildToolInfo;
import com.android.sdklib.repository.FullRevision;
import com.android.utils.FileUtils;
import com.android.utils.ILogger;
import com.duy.android.compiler.builder.AndroidAppBuilder;
import com.duy.android.compiler.builder.internal.dependency.SymbolFileProviderImpl;
import com.duy.android.compiler.builder.internal.process.ProcessOutputHandlerImpl;
import com.duy.android.compiler.builder.task.Task;
import com.duy.android.compiler.env.Environment;
import com.duy.android.compiler.gradleapi.TaskConfigAction;
import com.duy.android.compiler.project.AndroidAppProject;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * $ aapt
 * Android Asset Packaging Tool
 * <p>
 * Usage:
 * aapt l[ist] [-v] [-a] file.{zip,jar,apk}
 * List contents of Zip-compatible archive.
 * <p>
 * aapt d[ump] [--values] WHAT file.{apk} [asset [asset ...]]
 * badging          Print the label and icon for the app declared in APK.
 * permissions      Print the permissions from the APK.
 * resources        Print the resource table from the APK.
 * configurations   Print the configurations in the APK.
 * xmltree          Print the compiled xmls in the given assets.
 * xmlstrings       Print the strings of the given compiled xml assets.
 * <p>
 * aapt p[ackage] [-d][-f][-m][-u][-v][-x][-z][-M AndroidManifest.xml] \
 * [-0 extension [-0 extension ...]] [-g tolerance] [-j jarfile] \
 * [--min-sdk-version VAL] [--target-sdk-version VAL] \
 * [--max-sdk-version VAL] [--app-version VAL] \
 * [--app-version-name TEXT] [--custom-package VAL] \
 * [-I base-package [-I base-package ...]] \
 * [-A asset-source-dir]  [-G class-list-file] [-P public-definitions-file] \
 * [-S resource-sources [-S resource-sources ...]]         [-F apk-file] [-J R-file-dir] \
 * [raw-files-dir [raw-files-dir] ...]
 * <p>
 * Package the android resources.  It will read assets and resources that are
 * supplied with the -M -A -S or raw-files-dir arguments.  The -J -P -F and -R
 * options control which files are output.
 * <p>
 * aapt r[emove] [-v] file.{zip,jar,apk} file1 [file2 ...]
 * Delete specified files from Zip-compatible archive.
 * <p>
 * aapt a[dd] [-v] file.{zip,jar,apk} file1 [file2 ...]
 * Add specified files to Zip-compatible archive.
 * <p>
 * aapt v[ersion]
 * Print program version.
 * <p>
 * Modifiers:
 * -a  print Android-specific data (resources, manifest) when listing
 * -c  specify which configurations to include.  The default is all
 * configurations.  The value of the parameter should be a comma
 * separated list of configuration values.  Locales should be specified
 * as either a language or language-region pair.  Some examples:
 * en
 * port,en
 * port,land,en_US
 * If you put the special locale, zz_ZZ on the list, it will perform
 * pseudolocalization on the default locale, modifying all of the
 * strings so you can look for strings that missed the
 * internationalization process.  For example:
 * port,land,zz_ZZ
 * -d  one or more device assets to include, separated by commas
 * -f  force overwrite of existing files
 * -g  specify a pixel tolerance to force images to grayscale, default 0
 * -j  specify a jar or zip file containing classes to include
 * -k  junk path of file(s) added
 * -m  make package directories under location specified by -J
 * -u  update existing packages (add new, replace older, remove deleted files)
 * -v  verbose output
 * -x  create extending (non-application) resource IDs
 * -z  require localization of resource attributes marked with
 * localization="suggested"
 * -A  additional directory in which to find raw asset files
 * -G  A file to output proguard options into.
 * -F  specify the apk file to output
 * -I  add an existing package to base include set
 * -J  specify where to output R.java resource constant definitions
 * -M  specify full path to AndroidManifest.xml to include in zip
 * -P  specify where to output public resource definitions
 * -S  directory in which to find resources.  Multiple directories will be scanned
 * and the first match found (left to right) will take precedence.
 * -0  specifies an additional extension for which such files will not
 * be stored compressed in the .apk.  An empty string means to not
 * compress any files at all.
 * --min-sdk-version
 * inserts android:minSdkVersion in to manifest.
 * --target-sdk-version
 * inserts android:targetSdkVersion in to manifest.
 * --max-sdk-version
 * inserts android:maxSdkVersion in to manifest.
 * --values
 * when used with "dump resources" also includes resource values.
 * --version-code
 * inserts android:versionCode in to manifest.
 * --version-name
 * inserts android:versionName in to manifest.
 * --custom-package
 * generates R.java into a different package.
 *
 * @link https://elinux.org/Android_aapt
 * @link https://android.googlesource.com/platform/frameworks/base.git/+/master/tools/aapt/Main.cpp
 */
public class ProcessAndroidResourceTask2 extends Task<AndroidAppProject> {
    private File manifestFile;
    private File resDir;
    private File assetsDir;
    private File sourceOutputDir;
    private File textSymbolOutputDir;
    private File packageOutputFile;
    private File proguardOutputFile;
    private Collection<String> resourceConfigs;
    private String preferredDensity;
    private List<SymbolFileProviderImpl> libraries;
    private String packageForR;
    private Collection<String> splits;
    private boolean enforceUniquePackageName;
    private VariantType type;
    private boolean debuggable;
    private boolean pseudoLocalesEnabled;
    private AaptOptions aaptOptions;
    private ILogger mLogger;

    public ProcessAndroidResourceTask2(AndroidAppBuilder builder) {
        super(builder);
        config();
    }

    @Nullable
    private static String absolutePath(@Nullable File file) {
        return file == null ? null : file.getAbsolutePath();
    }

    @Override
    public boolean doFullTaskAction() throws Exception {
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
                new AaptPackageProcessBuilder(getManifestFile(), getAaptOptions())
                        .setAssetsFolder(getAssetsDir())
                        .setResFolder(getResDir())
                        .setLibraries(getLibraries())
                        .setPackageForR(getPackageForR())
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
         processResources(aaptPackageCommandBuilder, false,
                getBuilder().getProcessExecutor(),
                new ProcessOutputHandlerImpl(mBuilder.getStdout(), mBuilder.getStderr()));

         return false;
    }

    private AndroidAppBuilder getBuilder() {
        return (AndroidAppBuilder) mBuilder;
    }

    public boolean getDebuggable() {
        return debuggable;
    }

    public boolean getPseudoLocalesEnabled() {
        return pseudoLocalesEnabled;
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

    public File getManifestFile() {
        return manifestFile;
    }

    public void setManifestFile(File manifestFile) {
        this.manifestFile = manifestFile;
    }

    public File getResDir() {
        return resDir;
    }

    public void setResDir(File resDir) {
        this.resDir = resDir;
    }

    public File getAssetsDir() {
        return assetsDir;
    }

    public void setAssetsDir(File assetsDir) {
        this.assetsDir = assetsDir;
    }

    public File getTextSymbolOutputDir() {
        return textSymbolOutputDir;
    }

    public void setTextSymbolOutputDir(File textSymbolOutputDir) {
        this.textSymbolOutputDir = textSymbolOutputDir;
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

    public String getPreferredDensity() {
        return preferredDensity;
    }

    public void setPreferredDensity(String preferredDensity) {
        this.preferredDensity = preferredDensity;
    }

    public List<SymbolFileProviderImpl> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<SymbolFileProviderImpl> libraries) {
        this.libraries = libraries;
    }

    public String getPackageForR() {
        return packageForR;
    }

    public void setPackageForR(String packageForR) {
        this.packageForR = packageForR;
    }

    public Collection<String> getSplits() {
        return splits;
    }

    public void setSplits(Collection<String> splits) {
        this.splits = splits;
    }

    public boolean isEnforceUniquePackageName() {
        return enforceUniquePackageName;
    }

    public void setEnforceUniquePackageName(boolean enforceUniquePackageName) {
        this.enforceUniquePackageName = enforceUniquePackageName;
    }

    public VariantType getType() {
        return type;
    }

    public void setType(VariantType type) {
        this.type = type;
    }

    public boolean isDebuggable() {
        return debuggable;
    }

    public void setDebuggable(boolean debuggable) {
        this.debuggable = debuggable;
    }

    public boolean isPseudoLocalesEnabled() {
        return pseudoLocalesEnabled;
    }

    public void setPseudoLocalesEnabled(boolean pseudoLocalesEnabled) {
        this.pseudoLocalesEnabled = pseudoLocalesEnabled;
    }

    public AaptOptions getAaptOptions() {
        return aaptOptions;
    }

    public void setAaptOptions(AaptOptions aaptOptions) {
        this.aaptOptions = aaptOptions;
    }

    private void config() {
        mLogger = mBuilder.getLogger();
        ConfigAction configAction = new ConfigAction();
        configAction.execute(this);
    }

    public File getSourceOutputDir() {
        return sourceOutputDir;
    }

    public void setSourceOutputDir(File sourceOutputDir) {
        this.sourceOutputDir = sourceOutputDir;
    }

    @Override
    public String getTaskName() {
        return "Process android resource";
    }

    public File getPackageOutputFile() {
        return packageOutputFile;
    }

    public void setPackageOutputFile(File packageOutputFile) {
        this.packageOutputFile = packageOutputFile;
    }

    private File getAaptFile() {
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
        return new File(Environment.getBinDir(context), aaptName);
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
    public boolean processResources(
            @NonNull AaptPackageProcessBuilder aaptCommand,
            boolean enforceUniquePackageName,
            @NonNull ProcessExecutor processExecutor,
            @NonNull ProcessOutputHandler processOutputHandler) throws IOException {
        File aaptFile = getAaptFile();

        // launch aapt: create the command line
        ProcessInfo processInfo = aaptCommand.build2(aaptFile,
                mProject.getBootClassPath(context),
                new BuildToolInfo(new FullRevision(21), Environment.getClasspathFile(context).getParentFile())
                , mBuilder.getLogger());
        ProcessResult execute = processExecutor.execute(processInfo, processOutputHandler);
        if (execute.getExitValue() != 0) {
            return false;
        }
        // now if the project has libraries, R needs to be created for each libraries,
        // but only if the current project is not a library.
        if (/*aaptCommand.getSourceOutputDir() != null
                && aaptCommand.getType() != VariantType.LIBRARY
                && !aaptCommand.getLibraries().isEmpty()*/true) {
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
        return true;
    }


    public static class ConfigAction implements TaskConfigAction<ProcessAndroidResourceTask2> {
        private File symbolLocation;
        private AndroidAppProject mProject;

        @NonNull
        private static List<SymbolFileProviderImpl> getTextSymbolDependencies(
                List<? extends LibraryDependency> libraries) {
            List<SymbolFileProviderImpl> list = Lists.newArrayListWithCapacity(libraries.size());

            for (LibraryDependency lib : libraries) {
                list.add(new SymbolFileProviderImpl(lib));
            }

            return list;
        }

        @Override
        public String getName() {
            return "Process android resource";
        }

        @Override
        public Class<ProcessAndroidResourceTask2> getType() {
            return ProcessAndroidResourceTask2.class;
        }

        @Override
        public void execute(ProcessAndroidResourceTask2 processResources) {
            mProject = processResources.mProject;
            symbolLocation = new File(processResources.mProject.getDirBuildIntermediates(),
                    "symbols");
            Set<String> allFilters = new HashSet<String>();
            processResources.setSplits(allFilters);
            {
                processResources.enforceUniquePackageName = true;
                processResources.setLibraries(getTextSymbolDependencies(mProject.getLibraries()));
                processResources.setPackageForR(mProject.getPackageName());
                processResources.setSourceOutputDir(processResources.mProject.getRClassSourceOutputDir());
                processResources.setTextSymbolOutputDir(symbolLocation);
            }

            processResources.setManifestFile(mProject.getManifestFile());
            processResources.setResDir(mProject.getResDir());
            processResources.setAssetsDir(mProject.getAssetsDir());
            processResources.setPackageOutputFile(mProject.getProcessResourcePackageOutputFile());

            processResources.setType(VariantType.DEFAULT);
            processResources.setDebuggable(true);
            processResources.setAaptOptions(new com.duy.android.compiler.builder.internal.dsl.AaptOptions());
            processResources.setPseudoLocalesEnabled(false);

            processResources.setResourceConfigs(new ArrayList<String>());
        }
    }
}