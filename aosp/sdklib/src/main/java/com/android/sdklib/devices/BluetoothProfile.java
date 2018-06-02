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

public enum BluetoothProfile {
    A2DP("A2DP"),
    ATT("ATT"),
    AVRCP("AVRCP"),
    AVDTP("AVDTP"),
    BIP("BIP"),
    BPP("BPP"),
    CIP("CIP"),
    CTP("CTP"),
    DIP("DIP"),
    DUN("DUN"),
    FAX("FAX"),
    FTP("FTP"),
    GAVDP("GAVDP"),
    GAP("GAP"),
    GATT("GATT"),
    GOEP("GOEP"),
    HCRP("HCRP"),
    HDP("HDP"),
    HFP("HFP"),
    HID("HID"),
    HSP("HSP"),
    ICP("ICP"),
    LAP("LAP"),
    MAP("MAP"),
    OPP("OPP"),
    PAN("PAN"),
    PBA("PBA"),
    PBAP("PBAP"),
    SPP("SPP"),
    SDAP("SDAP"),
    SAP("SAP"),
    SIM("SIM"),
    rSAP("rSAP"),
    SYNCH("SYNCH"),
    VDP("VDP"),
    WAPB("WAPB");


    @NonNull private final String mValue;

    BluetoothProfile(@NonNull String value) {
        mValue = value;
    }

    @Nullable
    public static BluetoothProfile getEnum(@NonNull String value) {
        for (BluetoothProfile bp : values()) {
            if (bp.mValue.equals(value)) {
                return bp;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return mValue;
    }
}
