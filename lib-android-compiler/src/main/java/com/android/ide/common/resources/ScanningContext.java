/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.ide.common.resources;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ScanningContext} keeps track of state during a resource file scan,
 * such as any parsing errors encountered, whether Android ids have changed, and
 * so on.
 */
public class ScanningContext {
    protected final ResourceRepository mRepository;
    private boolean mNeedsFullAapt;
    private List<String> mErrors = null;

    /**
     * Constructs a new {@link ScanningContext}
     *
     * @param repository the associated resource repository
     */
    public ScanningContext(@NonNull ResourceRepository repository) {
        super();
        mRepository = repository;
    }

    /**
     * Returns a list of errors encountered during scanning
     *
     * @return a list of errors encountered during scanning (or null)
     */
    @Nullable
    public List<String> getErrors() {
        return mErrors;
    }

    /**
     * Adds the given error to the scanning context. The error should use the
     * same syntax as real aapt error messages such that the aapt parser can
     * properly detect the filename, line number, etc.
     *
     * @param error the error message, including file name and line number at
     *            the beginning
     */
    public void addError(@NonNull String error) {
        if (mErrors == null) {
            mErrors = new ArrayList<String>();
        }
        mErrors.add(error);
    }

    /**
     * Returns the repository associated with this scanning context
     *
     * @return the associated repository, never null
     */
    @NonNull
    public ResourceRepository getRepository() {
        return mRepository;
    }

    /**
     * Marks that a full aapt compilation of the resources is necessary because it has
     * detected a change that cannot be incrementally handled.
     */
    protected void requestFullAapt() {
        mNeedsFullAapt = true;
    }

    /**
     * Returns whether this repository has been marked as "dirty"; if one or
     * more of the constituent files have declared that the resource item names
     * that they provide have changed.
     *
     * @return true if a full aapt compilation is required
     */
    public boolean needsFullAapt() {
        return mNeedsFullAapt;
    }

    /**
     * Asks the context to check whether the given attribute name and value is valid
     * in this context.
     *
     * @param uri the XML namespace URI
     * @param name the attribute local name
     * @param value the attribute value
     * @return true if the attribute is valid
     */
    public boolean checkValue(@Nullable String uri, @NonNull String name, @NonNull String value) {
        return true;
    }
}
