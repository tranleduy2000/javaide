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

package com.android.sdklib.repository;

/**
 * Interface for elements that can provide a description of themselves.
 */
public interface IListDescription {

    /**
     * Returns a description of this package that is suitable for a list display.
     * Should not be empty. Must never be null.
     * <p/>
     * Note that this is the "base" name for the package
     * with no specific revision nor API mentioned.
     * In contrast, {@link IDescription#getShortDescription()} should be used if you
     * want more details such as the package revision number or the API, if applicable.
     */
    String getListDescription();
}
