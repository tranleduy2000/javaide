package com.android.build.gradle.internal.tasks;

import com.android.build.gradle.internal.dsl.CoreNdkOptions;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

import java.util.List;
import java.util.Set;

/**
 * Base task for tasks that require an NdkConfig
 */
public class NdkTask extends BaseTask {
    @Input
    @Optional
    public String getModuleName() {
        final CoreNdkOptions config = getNdkConfig();
        return (config == null ? null : config.getModuleName());
    }

    @Input
    @Optional
    public String getcFlags() {
        final CoreNdkOptions config = getNdkConfig();
        return (config == null ? null : config.getcFlags());
    }

    @Input
    @Optional
    public List<String> getLdLibs() {
        final CoreNdkOptions config = getNdkConfig();
        return (config == null ? null : config.getLdLibs());
    }

    @Input
    @Optional
    public Set<String> getAbiFilters() {
        final CoreNdkOptions config = getNdkConfig();
        return (config == null ? null : config.getAbiFilters());
    }

    @Input
    @Optional
    public String getStl() {
        final CoreNdkOptions config = getNdkConfig();
        return (config == null ? null : config.getStl());
    }

    public CoreNdkOptions getNdkConfig() {
        return ndkConfig;
    }

    public void setNdkConfig(CoreNdkOptions ndkConfig) {
        this.ndkConfig = ndkConfig;
    }

    private CoreNdkOptions ndkConfig;
}
