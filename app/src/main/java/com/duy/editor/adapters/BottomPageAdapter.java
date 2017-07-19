package com.duy.editor.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.duy.compile.diagnostic.DiagnosticFragment;
import com.duy.compile.message.MessageFragment;

/**
 * Created by duy on 19/07/2017.
 */

public class BottomPageAdapter extends FragmentPagerAdapter {


    public static final int COUNT = 2;
    private final FragmentManager fm;
    private final DiagnosticFragment mDiagnosticFragment;
    private final MessageFragment mMessageFragment;

    public BottomPageAdapter(FragmentManager fm,
                             DiagnosticFragment mDiagnosticFragment, MessageFragment mMessageFragment) {
        super(fm);
        this.fm = fm;
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
    public int getCount() {
        return COUNT;
    }
}
