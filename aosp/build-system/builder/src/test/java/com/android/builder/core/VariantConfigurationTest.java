/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.builder.core;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.SigningConfig;
import com.android.builder.signing.DefaultSigningConfig;

import junit.framework.TestCase;

import java.io.File;

public class VariantConfigurationTest extends TestCase {

    private DefaultProductFlavor mDefaultConfig;
    private DefaultProductFlavor mFlavorConfig;
    private DefaultBuildType mBuildType;

    private static class ManifestParserMock implements ManifestParser {

        private final String mPackageName;

        ManifestParserMock(String packageName) {
            mPackageName = packageName;
        }

        @Nullable
        @Override
        public String getPackage(@NonNull File manifestFile) {
            return mPackageName;
        }

        @Override
        public Object getMinSdkVersion(@NonNull File manifestFile) {
            return null;
        }

        @Override
        public Object getTargetSdkVersion(@NonNull File manifestFile) {
            return null;
        }

        @Nullable
        @Override
        public String getVersionName(@NonNull File manifestFile) {
            return "1.0";
        }

        @Override
        public int getVersionCode(@NonNull File manifestFile) {
            return 1;
        }
    }

    @Override
    protected void setUp() throws Exception {
        mDefaultConfig = new DefaultProductFlavor("main");
        mFlavorConfig = new DefaultProductFlavor("flavor");
        mBuildType = new DefaultBuildType("debug");
    }

    public void testPackageOverrideNone() {
        VariantConfiguration variant = getVariant();

        assertNull(variant.getIdOverride());
    }

    public void testPackageOverridePackageFromFlavor() {
        mFlavorConfig.setApplicationId("foo.bar");

        VariantConfiguration variant = getVariant();

        assertEquals("foo.bar", variant.getIdOverride());
    }

    public void testIdOverrideIdFromFlavor() {
        mFlavorConfig.setApplicationId("foo.bar");

        VariantConfiguration variant = getVariant();

        assertEquals("foo.bar", variant.getIdOverride());
    }

    public void testPackageOverridePackageFromFlavorWithSuffix() {
        mFlavorConfig.setApplicationId("foo.bar");
        mBuildType.setApplicationIdSuffix(".fortytwo");

        VariantConfiguration variant = getVariant();

        assertEquals("foo.bar.fortytwo", variant.getIdOverride());
    }

    public void testPackageOverridePackageFromFlavorWithSuffix2() {
        mFlavorConfig.setApplicationId("foo.bar");
        mBuildType.setApplicationIdSuffix("fortytwo");

        VariantConfiguration variant = getVariant();

        assertEquals("foo.bar.fortytwo", variant.getIdOverride());
    }

    public void testPackageOverridePackageWithSuffixOnly() {

        mBuildType.setApplicationIdSuffix("fortytwo");

        VariantConfiguration variant = getVariantWithManifestPackage("fake.package.name");

        assertEquals("fake.package.name.fortytwo", variant.getIdOverride());
    }

    public void testVersionNameFromFlavorWithSuffix() {
        mFlavorConfig.setVersionName("1.0");
        mBuildType.setVersionNameSuffix("-DEBUG");

        VariantConfiguration variant = getVariant();

        assertEquals("1.0-DEBUG", variant.getVersionName());
    }

    public void testVersionNameWithSuffixOnly() {
        mBuildType.setVersionNameSuffix("-DEBUG");

        VariantConfiguration variant = getVariantWithManifestVersion("2.0b1");

        assertEquals("2.0b1-DEBUG", variant.getVersionName());
    }

    public void testSigningBuildTypeOverride() {
        // DefaultSigningConfig doesn't compare the name, so put some content.
        DefaultSigningConfig debugSigning = new DefaultSigningConfig("debug");
        debugSigning.setStorePassword("debug");
        mBuildType.setSigningConfig(debugSigning);

        DefaultSigningConfig override = new DefaultSigningConfig("override");
        override.setStorePassword("override");

        VariantConfiguration variant = getVariant(override);

        assertEquals(override, variant.getSigningConfig());
    }

    public void testSigningProductFlavorOverride() {
        // DefaultSigningConfig doesn't compare the name, so put some content.
        DefaultSigningConfig defaultConfig = new DefaultSigningConfig("defaultConfig");
        defaultConfig.setStorePassword("debug");
        mDefaultConfig.setSigningConfig(defaultConfig);

        DefaultSigningConfig override = new DefaultSigningConfig("override");
        override.setStorePassword("override");

        VariantConfiguration variant = getVariant(override);

        assertEquals(override, variant.getSigningConfig());
    }

    private VariantConfiguration getVariant() {
        return getVariant(null /*signingOverride*/);
    }

    private VariantConfiguration getVariant(SigningConfig signingOverride) {
        VariantConfiguration variant = new VariantConfiguration(
                mDefaultConfig, new MockSourceProvider("main"),
                mBuildType, new MockSourceProvider("debug"),
                VariantType.DEFAULT,
                signingOverride);

        variant.addProductFlavor(mFlavorConfig, new MockSourceProvider("custom"), "");

        return variant;
    }

    private VariantConfiguration getVariantWithManifestPackage(final String packageName) {
        VariantConfiguration variant = new VariantConfiguration(
                mDefaultConfig, new MockSourceProvider("main"),
                mBuildType, new MockSourceProvider("debug"),
                VariantType.DEFAULT,
                null /*signingConfigOverride*/) {
            @Override
            public String getPackageFromManifest() {
                return packageName;
            }
        };

        variant.addProductFlavor(mFlavorConfig, new MockSourceProvider("custom"), "");
        return variant;
    }

    private VariantConfiguration getVariantWithManifestVersion(final String versionName) {
        VariantConfiguration variant = new VariantConfiguration(
                mDefaultConfig, new MockSourceProvider("main"),
                mBuildType, new MockSourceProvider("debug"),
                VariantType.DEFAULT,
                null /*signingConfigOverride*/) {
            @Override
            public String getVersionNameFromManifest() {
                return versionName;
            }
            // don't do validation.
        };

        variant.addProductFlavor(mFlavorConfig, new MockSourceProvider("custom"), "");
        return variant;
    }
}
