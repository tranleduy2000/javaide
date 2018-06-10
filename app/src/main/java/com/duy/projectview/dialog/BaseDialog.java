package com.duy.projectview.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;


public abstract class BaseDialog {
    private Context context;

    public BaseDialog(Context context) {
        this.context = context;
    }

    public abstract AlertDialog show();

    protected AlertDialog.Builder getBuilder() {
        return new AlertDialog.Builder(context);
    }

    protected LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(context);
    }

    protected Context getContext() {
        return context;
    }

    protected CharSequence getString(int id) {
        return getContext().getString(id);
    }

}
