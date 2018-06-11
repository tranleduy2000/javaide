/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.menu;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.SubMenu;

import com.duy.ide.R;
import com.jecelyin.editor.v2.manager.MenuManager;

import java.util.ArrayList;

public class JavaMenuManager {
    private final Context mContext;

    public JavaMenuManager(Context context) {
        mContext = context;
    }

    public void createFileMenu(SubMenu menu) {
        //hide create new file menu
        menu.removeItem(R.id.action_new_file);
        menu.removeItem(R.id.action_open);

        ArrayList<MenuItem> oldMenuItems = new ArrayList<>();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            oldMenuItems.add(item);
        }
        menu.clear();

        int[] newMenuIds = new int[]{
                R.id.action_new_java_project, R.string.new_java_project, R.drawable.ic_create_new_folder_white_24dp,
                R.id.action_new_android_project, R.string.new_android_project, R.drawable.ic_create_new_folder_white_24dp,
                R.id.action_new_class, R.string.new_class, R.drawable.ic_create_new_folder_white_24dp,
                R.id.action_new_file, R.string.new_file, R.drawable.ic_fiber_new_white_24dp,
        };
        int[] openMenuIds = new int[]{
                R.id.action_open_java_project, R.string.open_java_project, R.drawable.ic_folder_open_white_24dp,
                R.id.action_open_android_project, R.string.open_android_project, R.drawable.ic_folder_open_white_24dp,
        };
        addToMenu(menu, R.drawable.ic_create_new_folder_white_24dp, R.string.title_menu_new, newMenuIds);
        addToMenu(menu, R.drawable.baseline_folder_open_24, R.string.title_menu_open, openMenuIds);

        //restore, make it bottom
        for (MenuItem item : oldMenuItems) {
            menu.add(item.getGroupId(), item.getItemId(), item.getOrder(), item.getTitle())
                    .setIcon(item.getIcon());
        }
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
