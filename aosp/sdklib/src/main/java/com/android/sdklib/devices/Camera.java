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

package com.android.sdklib.devices;

import com.android.annotations.NonNull;

public class Camera {
    @NonNull
    private CameraLocation mLocation;
    private boolean mAutofocus;
    private boolean mFlash;

    /**
     * Creates a {@link Camera} with reasonable defaults.
     *
     * The resulting {@link Camera} with be on the {@link CameraLocation#BACK} with both autofocus
     * and flash.
     */
    public Camera() {
        this(CameraLocation.BACK, true, true);
    }

    /**
     * Creates a new {@link Camera} which describes an on device camera and it's features.
     * @param location The location of the {@link Camera} on the device. Either
     * {@link CameraLocation#FRONT} or {@link CameraLocation#BACK}.
     * @param autofocus Whether the {@link Camera} can auto-focus.
     * @param flash Whether the {@link Camera} has flash.
     */
    public Camera(@NonNull CameraLocation location, boolean autofocus, boolean flash) {
        mLocation = location;
        mAutofocus = autofocus;
        mFlash = flash;
    }

    @NonNull
    public CameraLocation getLocation() {
        return mLocation;
    }

    public void setLocation(@NonNull CameraLocation location) {
        mLocation = location;
    }

    public boolean hasAutofocus() {
        return mAutofocus;
    }

    public void setAutofocus(boolean hasAutofocus) {
        mAutofocus = hasAutofocus;
    }

    public boolean hasFlash() {
        return mFlash;
    }

    public void setFlash(boolean flash) {
        mFlash = flash;
    }

    /**
     * Returns a copy of the object that shares no state with it,
     * but is initialized to equivalent values.
     *
     * @return A copy of the object.
     */
    @NonNull
    public Camera deepCopy() {
        Camera c = new Camera();
        c.mLocation = mLocation;
        c.mAutofocus = mAutofocus;
        c.mFlash = mFlash;
        return c;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Camera)) {
            return false;
        }
        Camera c = (Camera) o;
        return mLocation == c.mLocation
                && mAutofocus == c.hasAutofocus()
                && mFlash == c.hasFlash();
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + mLocation.ordinal();
        hash = 31 * hash + (mAutofocus ? 1 : 0);
        hash = 31 * hash + (mFlash ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Camera <mLocation=");
        sb.append(mLocation);
        sb.append(", mAutofocus=");
        sb.append(mAutofocus);
        sb.append(", mFlash=");
        sb.append(mFlash);
        sb.append(">");
        return sb.toString();
    }


}
