package com.duy.ide.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.commonsware.cwac.pager.PageDescriptor;
import com.commonsware.cwac.pager.v4.ArrayPagerAdapter;
import com.duy.ide.diagnostic.DiagnosticFragment;
import com.duy.ide.diagnostic.MessageFragment;

import java.util.List;

/**
 * Created by duy on 19/07/2017.
 */

public class BottomPageAdapter extends ArrayPagerAdapter<Fragment> {

    public BottomPageAdapter(FragmentManager fragmentManager, List<PageDescriptor> descriptors) {
        super(fragmentManager, descriptors);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    protected Fragment createFragment(PageDescriptor desc) {
        if (desc.getFragmentTag().equals(MessageFragment.TAG)) {
            return MessageFragment.newInstance();
        } else if (desc.getFragmentTag().equals(DiagnosticFragment.TAG)) {
            return DiagnosticFragment.newInstance();
        }
        return null;
    }
}
