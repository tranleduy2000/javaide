/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.startup.tutorial;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.duy.editor.R;

/**
 * @author Spartacus Rex
 */
public class tutlist extends ListActivity {

    @Override
    public void onCreate(Bundle zBundle) {
        super.onCreate(zBundle);
        setListAdapter(new tutadaptor(this));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int zPosition, long zID) {
        tutlistitem tl = (tutlistitem) v;
        int layoutid = tl.getLayoutID();

        //Is this VIM
        if (layoutid == R.layout.tut_first) {
            //Create the specific VIM help list
            startActivity(new Intent(this, vimlist.class));

        } else if (layoutid == R.layout.tut_remote) {
            //Create the specific VIM help list
            startActivity(new Intent(this, remotelist.class));

        } else {
            //Now create an intent and return..
            Intent res = new Intent(this, tutview.class);
            res.putExtra("com.spartacusrex.prodj.tutorial", layoutid);
            startActivity(res);
        }
    }
}
