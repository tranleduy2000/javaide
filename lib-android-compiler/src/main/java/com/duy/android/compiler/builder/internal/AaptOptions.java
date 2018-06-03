package com.duy.android.compiler.builder.internal;

import java.util.Collection;
import java.util.List;

/**
 * Options for aapt.
 */
public interface AaptOptions {
    /**
     * Returns the value for the --ignore-assets option, or null
     */
    String getIgnoreAssets();

    /**
     * Returns the list of values for the -0 (disabled compression) option, or null
     */
    Collection<String> getNoCompress();

    /**
     * passes the --error-on-missing-config-entry parameter to the aapt command, by default false.
     */
    boolean getFailOnMissingConfigEntry();

    /**
     * Returns the list of additional parameters to pass.
     *
     * @return
     */
    List<String> getAdditionalParameters();
}
