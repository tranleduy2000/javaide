/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
