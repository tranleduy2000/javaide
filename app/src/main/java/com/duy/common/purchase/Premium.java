/*
 * Copyright (c) 2018 by Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.common.purchase;

import android.content.Context;

import com.duy.ide.BuildConfig;

import static com.duy.common.purchase.Premium.Config.CLEAR_LICENSE_WHEN_REFUND;


/**
 * Created by Duy on 14-Jul-17.
 */

public class Premium {
    static final String BASE64_KEY;
    //SKU for my product: the premium upgrade
    static final String SKU_PREMIUM;
    /**
     * access faster
     */
    private static boolean IS_PREMIUM = false;

    static {
        BASE64_KEY = BuildConfig.BASE64_KEY.replaceAll("\\s+", "");
        SKU_PREMIUM = BuildConfig.SKU_PREMIUM.replaceAll("\\s+", "");
    }

    /**
     * Purchase user
     *
     * @param context - Android context
     */
    public static boolean isPremiumUser(Context context) {
        return IS_PREMIUM || PremiumFileUtil.licenseCached(context);
    }

    /**
     * Purchase user
     */
    public static void setPremiumUser(Context context, boolean isPremium) {
        IS_PREMIUM = isPremium;
        if (isPremium) {
            PremiumFileUtil.saveLicence(context);
        } else {
            if (CLEAR_LICENSE_WHEN_REFUND) {
                PremiumFileUtil.clearLicence(context);
            }
        }
    }

    static class Config {
        /**
         * Some device return null value when check purchase, so it can be clear license and show
         * ads even user buy premium
         */
        static final boolean CLEAR_LICENSE_WHEN_REFUND = false;
    }

}
