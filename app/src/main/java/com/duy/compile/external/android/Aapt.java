package com.duy.compile.external.android;

import com.duy.project.file.android.AndroidProject;

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
 */
public class Aapt {

    public static void run(AndroidProject project) {

    }

}