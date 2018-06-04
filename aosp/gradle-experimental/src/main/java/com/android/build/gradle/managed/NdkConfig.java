package com.android.build.gradle.managed;

import com.android.annotations.NonNull;

import org.gradle.model.Managed;
import org.gradle.model.Unmanaged;

import java.util.List;
import java.util.Set;

/**
 * Root configuration model for android-ndk plugin.
 */
@Managed
public interface NdkConfig extends NdkBuildType {

    /**
     * The toolchain version.
     * Support "gcc" or "clang" (default: "gcc").
     */
    String getToolchain();
    void setToolchain(@NonNull String toolchain);

    /**
     * The toolchain version.
     * Set as empty to use the default version for the toolchain.
     */
    String getToolchainVersion();
    void setToolchainVersion(@NonNull String toolchainVersion);
}
