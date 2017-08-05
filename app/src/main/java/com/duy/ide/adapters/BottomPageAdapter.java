package com.duy.ide.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.duy.compile.diagnostic.DiagnosticFragment;
import com.duy.compile.message.MessageFragment;

/**
 * Created by duy on 19/07/2017.
 */

public class BottomPageAdapter extends FragmentPagerAdapter {


    public static final int COUNT = 2;
    private DiagnosticFragment mDiagnosticFragment;
    private MessageFragment mMessageFragment;

    public BottomPageAdapter(FragmentManager fm,
                             DiagnosticFragment mDiagnosticFragment, MessageFragment mMessageFragment) {
        super(fm);
        this.mDiagnosticFragment = mDiagnosticFragment;
        this.mMessageFragment = mMessageFragment;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return mMessageFragment;
            case 1:
                return mDiagnosticFragment;
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Message";
            case 1:
                return "Diagnostics";
        }
        return super.getPageTitle(position);
    }

    @Override
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public int getCount() {
        return COUNT;
    }
}
