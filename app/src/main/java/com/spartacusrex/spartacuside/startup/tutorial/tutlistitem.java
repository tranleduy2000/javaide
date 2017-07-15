/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.startup.tutorial;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spartacusrex.spartacuside.R;

/**
 * @author Spartacus Rex
 */
public class tutlistitem extends LinearLayout {

    int mLayoutID;

    public tutlistitem(Context zContext, String zChapter, String zVerse, int zLayoutID, int zIconID) {
        super(zContext);

        //Inflate the View
        View.inflate(zContext, R.layout.tutorial_list, this);

        //ImageView im = (ImageView)findViewById(R.id.tutlist_icon);
        //im.setImageResource(zIconID);

        TextView tv = (TextView) findViewById(R.id.tutlist_chapter);
        tv.setText(zChapter);

        tv = (TextView) findViewById(R.id.tutlist_verse);
        tv.setText(zVerse);

        mLayoutID = zLayoutID;
    }

    public int getLayoutID() {
        return mLayoutID;
    }
}
