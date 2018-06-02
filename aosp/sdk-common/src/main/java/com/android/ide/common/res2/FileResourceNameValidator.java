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

package com.android.ide.common.res2;

import static com.android.SdkConstants.DOT_9PNG;
import static com.android.SdkConstants.DOT_XML;
import static com.android.SdkConstants.DOT_XSD;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.resources.ResourceType;
import com.android.utils.SdkUtils;

import java.io.File;
import java.util.List;

import javax.lang.model.SourceVersion;

public final class FileResourceNameValidator {

    private FileResourceNameValidator() {
    }

    /**
     * Validate a single-file resource name.
     *
     * @param file the file resource to validate.
     * @param resourceType the resource type.
     * @throws MergingException is the resource name is not valid.
     */
    public static void validate(@NonNull File file, @NonNull ResourceType resourceType)
            throws MergingException {
        String error = getErrorTextForFileResource(file.getName(), resourceType);
        if (error != null) {
            throw MergingException.withMessage(error).withFile(file).build();
        }
    }

    /**
     * Validate a single-file resource name.
     *
     * @param fileNameWithExt the resource file name to validate.
     * @param resourceType the resource type.
     * @return null if no error, otherwise a string describing the error.
     */
    @Nullable
    public static String getErrorTextForFileResource(@NonNull final String fileNameWithExt,
            @NonNull final ResourceType resourceType) {
        return getErrorTextForFileResource(fileNameWithExt, resourceType, false);
    }

    /**
     * Validate a file-based resource name as it is being typed in a text field.
     *
     * Partial or no extensions are allowed, so the user does not see errors while typing.
     *
     * @param partialFileNameWithExt the resource file name or prefix of file name to validate.
     * @param resourceType the resource type.
     * @return null if no error, otherwise a string describing the error.
     */
    @Nullable
    public static String getErrorTextForPartialName(
            @NonNull final String partialFileNameWithExt,
            @NonNull final ResourceType resourceType) {
        return getErrorTextForFileResource(partialFileNameWithExt, resourceType, true);
    }

    @Nullable
    private static String getErrorTextForFileResource(@NonNull final String fileNameWithExt,
            @NonNull final ResourceType resourceType, boolean allowPartialOrMissingExtension) {
        if (fileNameWithExt.trim().isEmpty()) {
            return "Resource must have a name";
        }

        final String fileName;

        if (resourceType == ResourceType.RAW) {
            // Allow any single file extension.
            fileName = removeSingleExtension(fileNameWithExt);
        } else if (resourceType == ResourceType.DRAWABLE | resourceType == ResourceType.MIPMAP) {
            // Require either an image or xml file extension
            if (SdkUtils.endsWithIgnoreCase(fileNameWithExt, DOT_XML)) {
                fileName = fileNameWithExt
                        .substring(0, fileNameWithExt.length() - DOT_XML.length());
            } else if (SdkUtils.hasImageExtension(fileNameWithExt)) {
                if (SdkUtils.endsWithIgnoreCase(fileNameWithExt, DOT_9PNG)) {
                    fileName = fileNameWithExt
                            .substring(0, fileNameWithExt.length() - DOT_9PNG.length());
                } else {
                    fileName = fileNameWithExt.substring(0, fileNameWithExt.lastIndexOf('.'));
                }
            } else {
                if (!allowPartialOrMissingExtension) {
                    return "The file name must end with .xml or .png";
                } else {
                    String possibleFileName = removeSingleExtension(fileNameWithExt);
                    if (possibleFileName.endsWith(".9")) {
                        fileName = removeSingleExtension(possibleFileName);
                    } else {
                        fileName = possibleFileName;
                    }
                    String ext = fileNameWithExt.substring(fileName.length());
                    if (!SdkUtils.startsWithIgnoreCase(DOT_XML, ext) &&
                            !oneOfStartsWithIgnoreCase(SdkUtils.IMAGE_EXTENSIONS, ext)) {
                        return "The file name must end with .xml or .png";
                    }
                }
            }
        } else if (resourceType == ResourceType.XML) {
            // Also allow xsd as they are xml files.
            if (SdkUtils.endsWithIgnoreCase(fileNameWithExt, DOT_XML) ||
                    SdkUtils.endsWithIgnoreCase(fileNameWithExt, DOT_XSD)) {
                fileName = removeSingleExtension(fileNameWithExt);
            } else {
                if (!allowPartialOrMissingExtension) {
                    return "The file name must end with .xml";
                } else {
                    fileName = removeSingleExtension(fileNameWithExt);
                    String ext = fileNameWithExt.substring(fileName.length());
                    if (!SdkUtils.startsWithIgnoreCase(DOT_XML, ext) &&
                            !SdkUtils.startsWithIgnoreCase(DOT_XSD, ext)) {
                        return "The file name must end with .xml";
                    }
                }
            }
        } else {
            // Require xml extension
            if (SdkUtils.endsWithIgnoreCase(fileNameWithExt, DOT_XML)) {
                fileName = fileNameWithExt
                        .substring(0, fileNameWithExt.length() - DOT_XML.length());
            } else {
                if (!allowPartialOrMissingExtension) {
                    return "The file name must end with .xml";
                } else {
                    fileName = removeSingleExtension(fileNameWithExt);
                    String ext = fileNameWithExt.substring(fileName.length());
                    if (!SdkUtils.startsWithIgnoreCase(DOT_XML, ext)) {
                        return "The file name must end with .xml";
                    }
                }
            }
        }
        return getErrorTextForNameWithoutExtension(fileName);
    }

    /**
     * Validate a single-file resource name.
     *
     * @param fileNameWithoutExt The resource file name to validate, without an extension.
     * @return null if no error, otherwise a string describing the error.
     */
    @Nullable
    public static String getErrorTextForNameWithoutExtension(
            @NonNull final String fileNameWithoutExt) {
        char first = fileNameWithoutExt.charAt(0);
        if (!Character.isJavaIdentifierStart(first)) {
            return "The resource name must start with a letter";
        }

        // AAPT only allows lowercase+digits+_:
        // "%s: Invalid file name: must contain only [a-z0-9_.]","
        for (int i = 0, n = fileNameWithoutExt.length(); i < n; i++) {
            char c = fileNameWithoutExt.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_')) {
                return String.format("'%1$c' is not a valid file-based resource name character: "
                        + "File-based resource names must contain only lowercase a-z, 0-9,"
                        + " or underscore", c);
            }
        }
        if (SourceVersion.isKeyword(fileNameWithoutExt)) {
            return String.format("%1$s is not a valid resource name (reserved Java keyword)",
                    fileNameWithoutExt);
        }

        // Success!
        return null;

    }

    private static String removeSingleExtension(String fileNameWithExt) {
        int lastDot = fileNameWithExt.lastIndexOf('.');
        if (lastDot != -1) {
            return fileNameWithExt.substring(0, lastDot);
        } else {
            return fileNameWithExt;
        }
    }

    private static boolean oneOfStartsWithIgnoreCase(List<String> strings, String prefix) {
        boolean matches = false;
        for (String allowedString : strings) {
            if (SdkUtils.startsWithIgnoreCase(allowedString, prefix)) {
                matches = true;
                break;
            }
        }
        return matches;
    }
}
