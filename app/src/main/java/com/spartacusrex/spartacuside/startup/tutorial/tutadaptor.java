/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.startup.tutorial;

import android.content.Context;

import com.duy.editor.R;

import java.util.Vector;

/**
 * @author Spartacus Rex
 */
public class tutadaptor extends gen_adaptor {

    public tutadaptor(Context zContext) {
        super();

        //Add the Tutorials
        Vector items = getItemList();

        items.add(new tutlistitem(zContext, "Introduction", "Please start here!", R.layout.tut_intro, R.drawable.app_terminal));
        items.add(new tutlistitem(zContext, "Keyboard", "Special Terminal IDE keyboard", R.layout.tut_keyboard, R.drawable.app_terminal));
        items.add(new tutlistitem(zContext, "Tutorial", "Step by step walkthrough..", R.layout.tut_first, R.drawable.app_terminal));
        items.add(new tutlistitem(zContext, "bash", "The Command Line", R.layout.tut_bash, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "busybox", "The applications", R.layout.tut_busybox, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "vim", "THE editor", R.layout.tut_vim, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "javac, java, dx..", "Java Development tools", R.layout.tut_javac, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "gcc 4.4.0", "C/CPP Development tools", R.layout.tut_gcc, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "utelnetd / sshd / rsync", "Connect over USB / WiFi ", R.layout.tut_remote, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "git", "Version Control System", R.layout.tut_git, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "Midnight Commander (mc)", "THE File manager", R.layout.tut_mc, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "TMUX", "Terminal Multiplexer", R.layout.tut_tmux, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "BitchX", "IRC Chat Client", R.layout.tut_bitchx, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "Additional Apps", "Adding more native apps to Terminal IDE", R.layout.tut_expand, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "Trouble", "Issues, bugs, fixes..", R.layout.tut_trouble, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "Thanks", "The Mighty Ones", R.layout.tut_thanks, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "GPL License", "The gplv2 license", R.layout.tut_gpl, R.drawable.sym_keyboard_done));

    }

    @Override
    public int getItemViewType(int arg0) {
        return R.layout.tutorial_list;
    }

}
