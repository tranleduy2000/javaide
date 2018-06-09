package com.duy.ide.javaide.menu;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.SubMenu;

import com.duy.ide.R;
import com.jecelyin.editor.v2.manager.MenuManager;

public class JavaMenuManager {
    private final Context mContext;

    public JavaMenuManager(Context context) {
        mContext = context;
    }

    public void createFileMenu(SubMenu menu) {
        int[] newMenuIds = new int[]{
                R.id.action_new_java_project, R.string.new_java_project, R.drawable.ic_create_new_folder_white_24dp,
                R.id.action_new_android_project, R.string.new_android_project, R.drawable.ic_create_new_folder_white_24dp,
                R.id.action_new_class, R.string.new_class, R.drawable.ic_create_new_folder_white_24dp,
                R.id.action_new_file, R.string.new_file, R.drawable.ic_fiber_new_white_24dp,
        };
        int[] openMenuIds = new int[]{
                R.id.action_open_android_project, R.string.open_java_project, R.drawable.ic_folder_open_white_24dp,
                R.id.action_open_java_project, R.string.open_android_project, R.drawable.ic_folder_open_white_24dp,
        };
        addToMenu(menu, R.drawable.ic_create_new_folder_white_24dp, R.string.title_menu_new, newMenuIds);
        addToMenu(menu, R.drawable.baseline_folder_open_24, R.string.title_menu_open, openMenuIds);
    }

    private void addToMenu(SubMenu menu, int iconId, int title, int[] child) {

        SubMenu subMenu = menu.addSubMenu(0, 0, 0, title);
        subMenu.getItem().setIcon(MenuManager.makeMenuNormalIcon(mContext, iconId))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        for (int i = 0; i < child.length / 3; i++) {
            Drawable icon = MenuManager.makeMenuNormalIcon(mContext, child[3 * i + 2]);
            subMenu.add(0, child[3 * i], 0, child[3 * i + 1])
                    .setIcon(icon);
        }
    }
}
