/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.ide.themefont.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.duy.ide.R;
import com.duy.ide.themefont.fonts.ConsoleFontFragment;
import com.duy.ide.themefont.fonts.EditorFontFragment;
import com.duy.ide.themefont.themes.ThemeFragment;

/**
 * Created by Duy on 17-May-17.
 */

public class SectionPageAdapter extends FragmentPagerAdapter {

    private Context context;

    public SectionPageAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return EditorFontFragment.newInstance();
            case 1:
                return ConsoleFontFragment.newInstance();
            case 2:
                return ThemeFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.editor_font);
            case 1:
                return context.getString(R.string.console_font);
            case 2:
                return context.getString(R.string.theme);
        }
        return super.getPageTitle(position);
    }

    @Override
    public int getCount() {
        return 3;
    }


}
