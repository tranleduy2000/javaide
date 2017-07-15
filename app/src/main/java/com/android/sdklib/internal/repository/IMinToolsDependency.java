/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.sdklib.internal.repository;

import com.android.sdklib.repository.SdkRepository;

/**
 * Interface used to decorate a {@link Package} that has a dependency
 * on a minimal tools revision, e.g. which XML has a <code>&lt;min-tools-rev&gt;</code> element.
 * <p/>
 * A package that has this dependency can only be installed if the requested tools revision
 * is present or installed at the same time.
 */
public interface IMinToolsDependency {

    /**
     * The value of {@link #getMinToolsRevision()} when the {@link SdkRepository#NODE_MIN_TOOLS_REV}
     * was not specified in the XML source.
     */
    public static final int MIN_TOOLS_REV_NOT_SPECIFIED = 0;

    /**
     * The minimal revision of the tools package required by this extra package if > 0,
     * or {@link #MIN_TOOLS_REV_NOT_SPECIFIED} if there is no such requirement.
     */
    public abstract int getMinToolsRevision();

}
