/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.startup.tutorial;

import android.content.Context;

import com.duy.frontend.R;

import java.util.Vector;

/**
 * @author Spartacus Rex
 */
public class vimadaptor extends gen_adaptor {

    public vimadaptor(Context zContext) {
        super();

        //Add the Tutorials
        Vector items = getItemList();

        items.add(new tutlistitem(zContext, "Tutorial 1", "Basic system setup", R.layout.tut_first_1, R.drawable.app_terminal));
        items.add(new tutlistitem(zContext, "Tutorial 2", "Hello World! (java)", R.layout.tut_first_2, R.drawable.app_terminal));
        items.add(new tutlistitem(zContext, "Tutorial 3", "Java library", R.layout.tut_first_3, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "Tutorial 4", "Full java command line app", R.layout.tut_first_4, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "Tutorial 5", "Full Android app", R.layout.tut_first_5, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "Tutorial 6", "Hello World C app", R.layout.tut_first_6, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "Tutorial 7", "Hello World CPP app", R.layout.tut_first_7, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "Tutorial 8", "C library", R.layout.tut_first_8, R.drawable.sym_keyboard_done));
        items.add(new tutlistitem(zContext, "Tutorial 9", "Full C app (with c lib)", R.layout.tut_first_9, R.drawable.sym_keyboard_done));

//        items.add(new tutlistitem(zContext, "(5) Folding", "Java Development tools", R.layout.tut_personal, R.drawable.sym_keyboard_done));
//        items.add(new tutlistitem(zContext, "(6) Auto Completion", "THE editor", R.layout.tut_personal, R.drawable.sym_keyboard_done));
//        items.add(new tutlistitem(zContext, "(7) Compiling, error detection", "Java Development tools", R.layout.tut_personal, R.drawable.sym_keyboard_done));
//        items.add(new tutlistitem(zContext, "(8) Plugins", "How to customise", R.layout.tut_personal, R.drawable.sym_keyboard_done));
//        items.add(new tutlistitem(zContext, "(9) NERDTree", "File explorer", R.layout.tut_personal, R.drawable.sym_keyboard_done));
//        items.add(new tutlistitem(zContext, "(0) SnipMate", "Intelligent auto insert", R.layout.tut_personal, R.drawable.sym_keyboard_done));

    }

    @Override
    public int getItemViewType(int arg0) {
        return R.layout.tutorial_list;
    }

}
