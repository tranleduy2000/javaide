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
