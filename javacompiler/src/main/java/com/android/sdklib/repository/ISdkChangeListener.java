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

package com.android.sdklib.repository;


/**
 * Interface for listeners on SDK modifications by the SDK Manager UI.
 * This notifies when the SDK manager is first loading the SDK or before/after it installed
 * a package.
 */
public interface ISdkChangeListener {
    /**
     * Invoked when the content of the SDK is being loaded by the SDK Manager UI
     * for the first time.
     * This is generally followed by a call to {@link #onSdkReload()}
     * or by a call to {@link #preInstallHook()}.
     */
    void onSdkLoaded();

    /**
     * Invoked when the SDK Manager UI is about to start installing packages.
     * This will be followed by a call to {@link #postInstallHook()}.
     */
    void preInstallHook();

    /**
     * Invoked when the SDK Manager UI is done installing packages.
     * Some new packages might have been installed or the user might have cancelled the operation.
     * This is generally followed by a call to {@link #onSdkReload()}.
     */
    void postInstallHook();

    /**
     * Invoked when the content of the SDK is being reloaded by the SDK Manager UI,
     * typically after a package was installed. The SDK content might or might not
     * have changed.
     */
    void onSdkReload();
}

