/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.startup.tutorial;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import java.util.Vector;

/**
 * @author Spartacus Rex
 */
public abstract class gen_adaptor implements ListAdapter {

    Vector mItems;

    public gen_adaptor() {
        mItems = new Vector();
    }

    public Vector getItemList() {
        return mItems;
    }

    /*
     * List Adaptor Stuff
     */
    public boolean areAllItemsEnabled() {
        return true;
    }

    public boolean isEnabled(int arg0) {
        return true;
    }

    public void registerDataSetObserver(DataSetObserver arg0) {
    }

    public void unregisterDataSetObserver(DataSetObserver arg0) {
    }

    public int getCount() {
        return mItems.size();
    }

    public Object getItem(int arg0) {
        return mItems.get(arg0);
    }

    public long getItemId(int arg0) {
        return 0;
    }

    public boolean hasStableIds() {
        return true;
    }

    public View getView(int zPosition, View arg1, ViewGroup arg2) {
        return (View) mItems.elementAt(zPosition);
    }

    public abstract int getItemViewType(int arg0);

    public int getViewTypeCount() {
        return 1;
    }

    public boolean isEmpty() {
        return (mItems.size() == 0);
    }
}
