/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.build.gradle.internal.tasks;

import static com.google.common.base.Preconditions.checkState;

import com.android.builder.model.SigningConfig;
import com.android.ide.common.signing.KeystoreHelper;
import com.android.ide.common.signing.KeytoolException;
import com.android.prefs.AndroidLocation;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.tooling.BuildException;

import java.io.File;

/**
 * A validate task that creates the debug keystore if it's missing.
 * It only creates it if it's in the default debug keystore location.
 *
 * It's linked to a given SigningConfig
 *
 */
public class ValidateSigningTask extends BaseTask {

    private SigningConfig signingConfig;

    public void setSigningConfig(SigningConfig signingConfig) {
        this.signingConfig = signingConfig;
    }

    public SigningConfig getSigningConfig() {
        return signingConfig;
    }

    /**
     * Annotated getter for task input.
     *
     * This is an Input and not an InputFile because the file might not exist.
     * This is not actually used by the task, this is only for Gradle to check inputs.
     *
     * @return the path of the keystore.
     */
    @Input @Optional
    public String getStoreLocation() {
        File f = signingConfig.getStoreFile();
        if (f != null) {
            return f.getAbsolutePath();
        }
        return null;
    }

    @TaskAction
    public void validate() throws AndroidLocation.AndroidLocationException, KeytoolException {
        File storeFile = signingConfig.getStoreFile();
        if (storeFile == null) {
            throw new IllegalArgumentException(
                    "Keystore file not set for signing config " + signingConfig.getName());
        }
        if (!storeFile.exists()) {
            if (KeystoreHelper.defaultDebugKeystoreLocation().equals(storeFile.getAbsolutePath())) {
                checkState(signingConfig.isSigningReady(), "Debug signing config not ready.");

                getLogger().info(
                        "Creating default debug keystore at {}",
                        storeFile.getAbsolutePath());

                //noinspection ConstantConditions - isSigningReady() called above
                if (!KeystoreHelper.createDebugStore(
                        signingConfig.getStoreType(), signingConfig.getStoreFile(),
                        signingConfig.getStorePassword(), signingConfig.getKeyPassword(),
                        signingConfig.getKeyAlias(), getILogger())) {
                    throw new BuildException("Unable to recreate missing debug keystore.", null);
                }
            } else {
                throw new IllegalArgumentException(
                        String.format(
                                "Keystore file %s not found for signing config '%s'.",
                                storeFile.getAbsolutePath(),
                                signingConfig.getName()));
            }
        }
    }
}
