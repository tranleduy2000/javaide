/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.startup.tutorial;

import android.content.Context;

import com.spartacusrex.spartacuside.R;

import java.util.Vector;

/**
 * @author Spartacus Rex
 */
public class remoteadaptor extends gen_adaptor {

    public remoteadaptor(Context zContext) {
        super();

        //Add the Tutorials
        Vector items = getItemList();

        items.add(new tutlistitem(zContext, "telnet", "Simple and fast telnet", R.layout.tut_telnet, R.drawable.app_terminal));
        items.add(new tutlistitem(zContext, "ssh", "Secure SSH access", R.layout.tut_ssh, R.drawable.app_terminal));
        items.add(new tutlistitem(zContext, "rsync", "File sync and backup", R.layout.tut_rsync, R.drawable.sym_keyboard_done));
    }

    @Override
    public int getItemViewType(int arg0) {
        return R.layout.tutorial_list;
    }

}
