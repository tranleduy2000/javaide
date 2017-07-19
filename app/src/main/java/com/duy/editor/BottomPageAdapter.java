package com.duy.editor;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.duy.compile.diagnostic.DiagnosticFragment;
import com.duy.compile.message.MessageFragment;

/**
 * Created by duy on 19/07/2017.
 */

public class BottomPageAdapter extends FragmentPagerAdapter {


    public BottomPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return MessageFragment.newInstance();
            case 1:
                return DiagnosticFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
