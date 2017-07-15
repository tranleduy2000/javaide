/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.startup.tutorial;

import android.app.Activity;
import android.os.Bundle;

/**
 * @author Spartacus Rex
 */
public class tutview extends Activity {

    @Override
    public void onCreate(Bundle zBundle) {
        super.onCreate(zBundle);

        //Which tutorial is this..
        int layoutID = getIntent().getExtras().getInt("com.spartacusrex.prodj.tutorial");

        //Set it..
        setContentView(layoutID);
    }
}
