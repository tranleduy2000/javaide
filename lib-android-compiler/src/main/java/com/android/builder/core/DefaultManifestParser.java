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
import com.android.io.FileWrapper;
import com.android.io.StreamException;
import com.android.utils.XmlUtils;
import com.android.xml.AndroidManifest;
import com.android.xml.AndroidXPathFactory;
import com.google.common.base.Optional;

import org.apache.http.annotation.ThreadSafe;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

@ThreadSafe
class DefaultManifestParser implements ManifestParser {

    Optional<Object> mMinSdkVersion;
    Optional<Object> mTargetSdkVersion;
    Optional<Integer> mVersionCode;
    Optional<String> mPackage;
    Optional<String> mVersionName;

    @Nullable
    @Override
    public synchronized  String getPackage(@NonNull File manifestFile) {
        if (mPackage == null) {
            mPackage = Optional.fromNullable(getStringValue(manifestFile, "/manifest/@package"));
        }
        return mPackage.orNull();
    }

    @Nullable
    @Override
    public synchronized  String getVersionName(@NonNull File manifestFile) {
        if (mVersionName == null) {
            mVersionName = Optional.fromNullable(
                    getStringValue(manifestFile, "/manifest/@android:versionName"));
        }
        return mVersionName.orNull();
    }

    @Override
    @NonNull
    public synchronized int getVersionCode(@NonNull File manifestFile) {
        if (mVersionCode == null) {
            mVersionCode = Optional.absent();
            try {
                String value = getStringValue(manifestFile, "/manifest/@android:versionCode");
                if (value != null) {
                    mVersionCode = Optional.of(Integer.valueOf(value));
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return mVersionCode.or(-1);
    }

    @Override
    @NonNull
    public synchronized Object getMinSdkVersion(@NonNull File manifestFile) {
        if (mMinSdkVersion == null) {
            try {
                mMinSdkVersion = Optional.fromNullable(
                        AndroidManifest.getMinSdkVersion(new FileWrapper(manifestFile)));
            } catch (XPathExpressionException e) {
                throw new RuntimeException(e);
            } catch (StreamException e) {
                throw new RuntimeException(e);
            }
        }
        return mMinSdkVersion.or(1);
    }

    @Override
    @NonNull
    public Object getTargetSdkVersion(@NonNull File manifestFile) {
        if (mTargetSdkVersion == null) {
            try {
                mTargetSdkVersion =
                        Optional.fromNullable(AndroidManifest.getTargetSdkVersion(
                                new FileWrapper(manifestFile)));
            } catch (XPathExpressionException e) {
                return new RuntimeException(e);
            } catch (StreamException e) {
                throw new RuntimeException(e);
            }
        }
        return mTargetSdkVersion.or(-1);
    }

    private static String getStringValue(@NonNull File file, @NonNull String xPath) {
        XPath xpath = AndroidXPathFactory.newXPath();

        try {
            InputSource source = new InputSource(XmlUtils.getUtfReader(file));
            return xpath.evaluate(xPath, source);
        } catch (XPathExpressionException e) {
            // won't happen.
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
