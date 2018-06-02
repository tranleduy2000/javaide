/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.manifmerger;

import static com.android.manifmerger.ManifestMerger2.SystemProperty;
import static com.android.manifmerger.PlaceholderHandler.KeyBasedValueResolver;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.blame.SourceFile;
import com.google.common.base.Optional;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Utilities for testing ManifestMerge classes.
 */
public class TestUtils {

    private static final KeyResolver<String> NULL_RESOLVER = new KeyResolver<String>() {
        @Nullable
        @Override
        public String resolve(String key) {
            return null;
        }

        @Override
        public List<String> getKeys() {
            return Collections.emptyList();
        }
    };

    private static final KeyBasedValueResolver<SystemProperty> NO_PROPERTY_RESOLVER =
            new KeyBasedValueResolver<SystemProperty>() {
                @Nullable
                @Override
                public String getValue(@NonNull SystemProperty key) {
                    return null;
                }
            };

    static SourceFile sourceFile(Class sourceClass, String location) {
        return new SourceFile(sourceClass.getSimpleName() + "#" + location);
    }

    static XmlDocument xmlDocumentFromString(
            SourceFile location,
            String input)  throws IOException, SAXException, ParserConfigurationException {

        return XmlLoader.load(
                NULL_RESOLVER, NO_PROPERTY_RESOLVER, location, input, XmlDocument.Type.MAIN,
                Optional.<String>absent() /* mainManifestPackageName */);
    }

    static XmlDocument xmlLibraryFromString(
            SourceFile location,
            String input)  throws IOException, SAXException, ParserConfigurationException {

        return XmlLoader.load(
                NULL_RESOLVER, NO_PROPERTY_RESOLVER, location, input, XmlDocument.Type.LIBRARY,
                Optional.<String>absent()  /* mainManifestPackageName */);
    }

    static XmlDocument xmlDocumentFromString(
            SourceFile location,
            String input,
            XmlDocument.Type type,
            Optional<String> mainManifestPackageName)  throws IOException, SAXException, ParserConfigurationException {

        return XmlLoader.load(NULL_RESOLVER, NO_PROPERTY_RESOLVER, location, input, type, mainManifestPackageName);
    }

    static XmlDocument xmlDocumentFromString(
            @NonNull KeyResolver<String> selectors,
            @NonNull SourceFile location,
            String input)  throws IOException, SAXException, ParserConfigurationException {

        return XmlLoader.load(selectors, NO_PROPERTY_RESOLVER, location, input,
                XmlDocument.Type.LIBRARY, Optional.<String>absent() /* mainManifestPackageName */);
    }

}
