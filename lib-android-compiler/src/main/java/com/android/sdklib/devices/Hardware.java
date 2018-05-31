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
import com.android.annotations.Nullable;
import com.android.resources.Keyboard;
import com.android.resources.Navigation;
import com.android.resources.UiMode;
import com.google.common.base.Objects;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Hardware {
    private Screen mScreen;
    private EnumSet<Network> mNetworking = EnumSet.noneOf(Network.class);
    private EnumSet<Sensor> mSensors = EnumSet.noneOf(Sensor.class);
    private boolean mMic;
    private List<Camera> mCameras = new ArrayList<Camera>(2);
    private Keyboard mKeyboard;
    private Navigation mNav;
    private Storage mRam;
    private ButtonType mButtons;
    private List<Storage> mInternalStorage = new ArrayList<Storage>();
    private List<Storage> mRemovableStorage = new ArrayList<Storage>();
    private String mCpu;
    private String mGpu;
    private EnumSet<Abi> mAbis = EnumSet.noneOf(Abi.class);
    private EnumSet<UiMode> mUiModes = EnumSet.noneOf(UiMode.class);
    private PowerType mPluggedIn;
    private File mSkinFile;

    public void setSkinFile(@Nullable File skinFile) {
      mSkinFile = skinFile;
    }

    @Nullable
    public File getSkinFile() {
        return mSkinFile;
    }

    @NonNull
    public Set<Network> getNetworking() {
        return mNetworking;
    }

    public void addNetwork(@NonNull Network n) {
        mNetworking.add(n);
    }

    public void addAllNetworks(@NonNull Collection<Network> ns) {
        mNetworking.addAll(ns);
    }

    @NonNull
    public Set<Sensor> getSensors() {
        return mSensors;
    }

    public void addSensor(@NonNull Sensor sensor) {
        mSensors.add(sensor);
    }

    public void addAllSensors(@NonNull Collection<Sensor> sensors) {
        mSensors.addAll(sensors);
    }

    public boolean hasMic() {
        return mMic;
    }

    public void setHasMic(boolean hasMic) {
        mMic = hasMic;
    }

    @NonNull
    public List<Camera> getCameras() {
        return mCameras;
    }

    public void addCamera(@NonNull Camera c) {
        mCameras.add(c);
    }

    public void addAllCameras(@NonNull Collection<Camera> cs) {
        mCameras.addAll(cs);
    }

    @NonNull
    public Camera getCamera(int i) {
        return mCameras.get(i);
    }

    @Nullable
    public Camera getCamera(@NonNull CameraLocation location) {
        for (Camera c : mCameras) {
            if (location == c.getLocation()) {
                return c;
            }
        }
        return null;
    }

    public Keyboard getKeyboard() {
        return mKeyboard;
    }

    public void setKeyboard(@NonNull Keyboard keyboard) {
        mKeyboard = keyboard;
    }

    public Navigation getNav() {
        return mNav;
    }

    public void setNav(@NonNull Navigation n) {
        mNav = n;
    }

    public Storage getRam() {
        return mRam;
    }

    public void setRam(@NonNull Storage ram) {
        mRam = ram;
    }

    public ButtonType getButtonType() {
        return mButtons;
    }

    public void setButtonType(@NonNull ButtonType bt) {
        mButtons = bt;
    }

    @NonNull
    public List<Storage> getInternalStorage() {
        return mInternalStorage;
    }

    public void addInternalStorage(@NonNull Storage is) {
        mInternalStorage.add(is);
    }

    public void addAllInternalStorage(@NonNull Collection<Storage> is) {
        mInternalStorage.addAll(is);
    }

    @NonNull
    public List<Storage> getRemovableStorage() {
        return mRemovableStorage;
    }

    public void addRemovableStorage(@NonNull Storage rs) {
        mRemovableStorage.add(rs);
    }

    public void addAllRemovableStorage(@NonNull Collection<Storage> rs) {
        mRemovableStorage.addAll(rs);
    }

    public String getCpu() {
        return mCpu;
    }

    public void setCpu(@NonNull String cpuName) {
        mCpu = cpuName;
    }

    public String getGpu() {
        return mGpu;
    }

    public void setGpu(@NonNull String gpuName) {
        mGpu = gpuName;
    }

    @NonNull
    public Set<Abi> getSupportedAbis() {
        return mAbis;
    }

    public void addSupportedAbi(@NonNull Abi abi) {
        mAbis.add(abi);
    }

    public void addAllSupportedAbis(@NonNull Collection<Abi> abis) {
        mAbis.addAll(abis);
    }

    @NonNull
    public Set<UiMode> getSupportedUiModes() {
        return mUiModes;
    }

    public void addSupportedUiMode(@NonNull UiMode uiMode) {
        mUiModes.add(uiMode);
    }

    public void addAllSupportedUiModes(@NonNull Collection<UiMode> uiModes) {
        mUiModes.addAll(uiModes);
    }

    public PowerType getChargeType() {
        return mPluggedIn;
    }

    public void setChargeType(@NonNull PowerType chargeType) {
        mPluggedIn = chargeType;
    }

    public Screen getScreen() {
        return mScreen;
    }

    public void setScreen(@NonNull Screen s) {
        mScreen = s;
    }

    /**
     * Returns a copy of the object that shares no state with it,
     * but is initialized to equivalent values.
     *
     * @return A copy of the object.
     */
    public Hardware deepCopy() {
        Hardware hw = new Hardware();
        hw.mScreen = mScreen.deepCopy();
        hw.mNetworking =  mNetworking.clone();
        hw.mSensors = mSensors.clone();
        // Get the constant boolean value
        hw.mMic = mMic;
        hw.mCameras = new ArrayList<Camera>();
        for (Camera c : mCameras) {
            hw.mCameras.add(c.deepCopy());
        }
        hw.mKeyboard = mKeyboard;
        hw.mNav = mNav;
        hw.mRam = mRam.deepCopy();
        hw.mButtons = mButtons;
        hw.mInternalStorage = new ArrayList<Storage>();
        for (Storage s : mInternalStorage) {
            hw.mInternalStorage.add(s.deepCopy());
        }
        hw.mRemovableStorage = new ArrayList<Storage>();
        for (Storage s : mRemovableStorage) {
            hw.mRemovableStorage.add(s.deepCopy());
        }
        hw.mCpu = mCpu;
        hw.mGpu = mGpu;
        hw.mAbis = mAbis.clone();
        hw.mUiModes = mUiModes.clone();
        hw.mPluggedIn = mPluggedIn;
        hw.mSkinFile = mSkinFile;
        return hw;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Hardware)) {
            return false;
        }
        Hardware hw = (Hardware) o;
        return mScreen.equals(hw.getScreen())
                && mNetworking.equals(hw.getNetworking())
                && mSensors.equals(hw.getSensors())
                && mMic == hw.hasMic()
                && mCameras.equals(hw.getCameras())
                && mKeyboard == hw.getKeyboard()
                && mNav == hw.getNav()
                && mRam.equals(hw.getRam())
                && mButtons == hw.getButtonType()
                && mInternalStorage.equals(hw.getInternalStorage())
                && mRemovableStorage.equals(hw.getRemovableStorage())
                && mCpu.equals(hw.getCpu())
                && mGpu.equals(hw.getGpu())
                && mAbis.equals(hw.getSupportedAbis())
                && mUiModes.equals(hw.getSupportedUiModes())
                && mPluggedIn == hw.getChargeType()
                && Objects.equal(mSkinFile, hw.getSkinFile());
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + mScreen.hashCode();

        // Since sets have no defined order, we need to hash them in such a way that order doesn't
        // matter.
        int temp = 0;
        for (Network n : mNetworking) {
            temp |= 1 << n.ordinal();
        }
        hash = 31 * hash + temp;

        temp = 0;
        for (Sensor s : mSensors) {
            temp |= 1 << s.ordinal();
        }

        hash = 31 * hash + temp;
        hash = 31 * hash + (mMic ? 1 : 0);
        hash = mCameras.hashCode();
        hash = 31 * hash + mKeyboard.ordinal();
        hash = 31 * hash + mNav.ordinal();
        hash = 31 * hash + mRam.hashCode();
        hash = 31 * hash + mButtons.ordinal();
        hash = 31 * hash + mInternalStorage.hashCode();
        hash = 31 * hash + mRemovableStorage.hashCode();
        if (mSkinFile != null) {
            hash = 31 * hash + mSkinFile.hashCode();
        }

        for (Character c : mCpu.toCharArray()) {
            hash = 31 * hash + c;
        }

        for (Character c : mGpu.toCharArray()) {
            hash = 31 * hash + c;
        }

        temp = 0;
        for (Abi a : mAbis) {
            temp |= 1 << a.ordinal();
        }
        hash = 31 * hash + temp;

        temp = 0;
        for (UiMode ui : mUiModes) {
            temp |= 1 << ui.ordinal();
        }
        hash = 31 * hash + temp;

        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hardware <mScreen=");
        sb.append(mScreen);
        sb.append(", mNetworking=");
        sb.append(mNetworking);
        sb.append(", mSensors=");
        sb.append(mSensors);
        sb.append(", mMic=");
        sb.append(mMic);
        sb.append(", mCameras=");
        sb.append(mCameras);
        sb.append(", mKeyboard=");
        sb.append(mKeyboard);
        sb.append(", mNav=");
        sb.append(mNav);
        sb.append(", mRam=");
        sb.append(mRam);
        sb.append(", mButtons=");
        sb.append(mButtons);
        sb.append(", mInternalStorage=");
        sb.append(mInternalStorage);
        sb.append(", mRemovableStorage=");
        sb.append(mRemovableStorage);
        sb.append(", mCpu=");
        sb.append(mCpu);
        sb.append(", mGpu=");
        sb.append(mGpu);
        sb.append(", mAbis=");
        sb.append(mAbis);
        sb.append(", mUiModes=");
        sb.append(mUiModes);
        sb.append(", mPluggedIn=");
        sb.append(mPluggedIn);
        sb.append(", mSkinFile=");
        sb.append(mSkinFile);
        sb.append(">");
        return sb.toString();
    }
}
