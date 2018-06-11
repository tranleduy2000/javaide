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

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.duy.common.DLog;

public class InAppPurchaseHelper implements BillingProcessor.IBillingHandler {
    private static final String TAG = "InAppPurchaseHelper";
    private BillingProcessor mBillingProcessor;
    private AppCompatActivity mActivity;
    private PurchaseCallback mPurchaseCallback;

    public InAppPurchaseHelper(AppCompatActivity activity) {
        mActivity = activity;
        mBillingProcessor = new BillingProcessor(activity, Premium.BASE64_KEY, this);
    }

    /*
     * Called when BillingProcessor was initialized and it's ready to purchase
     */
    @Override
    public void onBillingInitialized() {
        if (DLog.DEBUG) DLog.d(TAG, "onBillingInitialized() called");

    }

    /*
     * Called when requested PRODUCT ID was successfully purchased
     */
    @Override
    public void onProductPurchased(@NonNull String productId, TransactionDetails details) {
        if (productId.contentEquals(Premium.SKU_PREMIUM)) {
            Premium.setPremiumUser(mActivity, true);
            if (mPurchaseCallback != null) {
                mPurchaseCallback.updateUI(true);
            }
        }
    }

    /*
     * Called when some error occurred. See Constants class for more details
     *
     * Note - this includes handling the case where the user canceled the buy dialog:
     * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
     */
    @Override
    public void onBillingError(int errorCode, Throwable error) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "onBillingError() called with: errorCode = [" + errorCode + "], error = [" + error + "]");
        }
        Toast.makeText(mActivity, error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    /*
     * Called when purchase history was restored and the list of all owned PRODUCT ID's
     * was loaded from Google Play
     */
    @Override
    public void onPurchaseHistoryRestored() {

    }

    public void purchase(String sku) {
        mBillingProcessor.purchase(mActivity, sku);
    }

    public void upgradePremium() {
        purchase(Premium.SKU_PREMIUM);
    }

    public void restorePurchase() {
        mBillingProcessor.loadOwnedPurchasesFromGoogle();
    }

    /**
     * Called when activity destroy
     */
    public void destroy() {
        mBillingProcessor.release();
    }

    public interface PurchaseCallback {
        void updateUI(boolean premium);
    }
}
