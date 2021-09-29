package com.android.build.gradle.tasks;

import com.android.build.gradle.internal.tasks.IncrementalTask;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.Map;

/**
 * A task that processes the manifest
 */
public abstract class ManifestProcessorTask extends IncrementalTask {
    /**
     * The processed Manifest.
     */
    private File manifestOutputFile;
    /**
     * The aapt friendly processed Manifest. In case we are processing a library manifest, some
     * placeholders may not have been resolved (and will be when the library is merged into the
     * importing application). However, such placeholders keys are not friendly to aapt which
     * flags some illegal characters. Such characters are replaced/encoded in this version.
     */
    private File aaptFriendlyManifestOutputFile;

    /**
     * Serialize a map key+value pairs into a comma separated list. Map elements are sorted to
     * ensure stability between instances.
     *
     * @param mapToSerialize the map to serialize.
     */
    protected String serializeMap(Map<String, String> mapToSerialize) {
        final Joiner keyValueJoiner = Joiner.on(":");
        // transform the map on a list of key:value items, sort it and concatenate it.
        return Joiner.on(",")
                .join(
                        Lists.newArrayList(
                                Iterables.transform(mapToSerialize.entrySet(),
                                        new Function<Map.Entry<String, String>, String>() {
                                            @Override
                                            public String apply(final Map.Entry<String, String> input) {
                                                return keyValueJoiner.join(input.getKey(), input.getValue());
                                            }

                                        }))
                                .iterator());
    }

    /**
     * Returns the manifest processing output file. if an aapt friendly version was requested,
     * return that otherwise return the actual output of the manifest merger tool directly.
     */
    public File getOutputFile() {
        return getAaptFriendlyManifestOutputFile() == null ? getManifestOutputFile() : getAaptFriendlyManifestOutputFile();
    }

    public File getManifestOutputFile() {
        return manifestOutputFile;
    }

    public void setManifestOutputFile(File manifestOutputFile) {
        this.manifestOutputFile = manifestOutputFile;
    }

    public File getAaptFriendlyManifestOutputFile() {
        return aaptFriendlyManifestOutputFile;
    }

    public void setAaptFriendlyManifestOutputFile(File aaptFriendlyManifestOutputFile) {
        this.aaptFriendlyManifestOutputFile = aaptFriendlyManifestOutputFile;
    }
}
