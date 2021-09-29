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

package com.duy.ide.javaide.theme;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.duy.common.purchase.InAppPurchaseHelper;
import com.duy.ide.R;
import com.jecelyin.editor.v2.dialog.AbstractDialog;

public class PremiumDialog extends AbstractDialog {
    private final InAppPurchaseHelper mPurchaseHelper;

    public PremiumDialog(Context context, InAppPurchaseHelper purchaseHelper) {
        super(context);
        this.mPurchaseHelper = purchaseHelper;
    }

    @Override
    public void show() {
        AlertDialog.Builder builder = getBuilder();
        builder.setTitle(R.string.title_premium_version);
        builder.setMessage(R.string.message_premium);
        builder.setPositiveButton(R.string.button_purchase, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPurchaseHelper.upgradePremium();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }
}
