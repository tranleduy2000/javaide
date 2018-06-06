/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.build.gradle;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.AndroidProject;

import org.gradle.api.Project;

/**
 * Determines if various options, triggered from the command line or environment, are set.
 */
public class AndroidGradleOptions {


    public static boolean invokedFromIde(@NonNull Project project) {
        return getBoolean(project, AndroidProject.PROPERTY_INVOKED_FROM_IDE);
    }

    public static boolean buildModelOnly(@NonNull Project project) {
        return getBoolean(project, AndroidProject.PROPERTY_BUILD_MODEL_ONLY);
    }

    public static boolean buildModelOnlyAdvanced(@NonNull Project project) {
        return getBoolean(project, AndroidProject.PROPERTY_BUILD_MODEL_ONLY_ADVANCED);
    }

    @Nullable
    public static String getApkLocation(@NonNull Project project) {
        return getString(project, AndroidProject.PROPERTY_APK_LOCATION);
    }

    @Nullable
    public static SigningOptions getSigningOptions(@NonNull Project project) {
        String signingStoreFile =
                getString(project, AndroidProject.PROPERTY_SIGNING_STORE_FILE);
        String signingStorePassword =
                getString(project, AndroidProject.PROPERTY_SIGNING_STORE_PASSWORD);
        String signingKeyAlias =
                getString(project, AndroidProject.PROPERTY_SIGNING_KEY_ALIAS);
        String signingKeyPassword =
                getString(project, AndroidProject.PROPERTY_SIGNING_KEY_PASSWORD);

        if (signingStoreFile != null
                && signingStorePassword != null
                && signingKeyAlias != null
                && signingKeyPassword != null) {
            String signingStoreType =
                    getString(project, AndroidProject.PROPERTY_SIGNING_STORE_TYPE);

            return new SigningOptions(
                    signingStoreFile,
                    signingStorePassword,
                    signingKeyAlias,
                    signingKeyPassword,
                    signingStoreType);
        }

        return null;
    }

    @Nullable
    private static String getString(@NonNull Project project, String propertyName) {
        return (String) project.getProperties().get(propertyName);
    }

    private static boolean getBoolean(
            @NonNull Project project,
            @NonNull String propertyName) {
        if (project.hasProperty(propertyName)) {
            Object value = project.getProperties().get(propertyName);
            if (value instanceof String) {
                return Boolean.parseBoolean((String) value);
            }
        }

        return false;
    }

    public static class SigningOptions {
        @NonNull
        public final String storeFile;
        @NonNull
        public final String storePassword;
        @NonNull
        public final String keyAlias;
        @NonNull
        public final String keyPassword;
        @Nullable
        public final String storeType;

        public SigningOptions(
                @NonNull String storeFile,
                @NonNull String storePassword,
                @NonNull String keyAlias,
                @NonNull String keyPassword,
                @Nullable String storeType) {
            this.storeFile = storeFile;
            this.storeType = storeType;
            this.storePassword = storePassword;
            this.keyAlias = keyAlias;
            this.keyPassword = keyPassword;
        }
    }
}
