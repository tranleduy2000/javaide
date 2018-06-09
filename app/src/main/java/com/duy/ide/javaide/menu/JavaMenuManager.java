package com.duy.ide.javaide.menu;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;

import com.duy.ide.R;
import com.jecelyin.editor.v2.manager.MenuManager;

public class JavaMenuManager {
    private final Context mContext;

    public JavaMenuManager(Context context) {
        mContext = context;
    }

    public void createFileMenu(MenuItem fileMenu) {
        int[] menuIds = new int[]{
                R.id.action_new_java_project, R.string.new_java_project, R.drawable.ic_create_new_folder_white_24dp,
                R.id.action_new_android_project, R.string.new_android_project, R.drawable.ic_create_new_folder_white_24dp,
                R.id.action_new_class, R.string.new_class, R.drawable.ic_create_new_folder_white_24dp,
                R.id.action_new_file, R.string.new_file, R.drawable.ic_fiber_new_white_24dp,
                R.id.action_open_android_project, R.string.open_java_project, R.drawable.ic_folder_open_white_24dp,
                R.id.action_open_java_project, R.string.open_android_project, R.drawable.ic_folder_open_white_24dp,
        };
        for (int i = 0; i < menuIds.length / 3; i++) {
            Drawable icon = MenuManager.makeMenuNormalIcon(mContext, menuIds[3 * i + 2]);
            fileMenu.getSubMenu().add(0, menuIds[3 * i], 0, menuIds[3 * i + 1])
                    .setIcon(icon);
        }
    }
}
