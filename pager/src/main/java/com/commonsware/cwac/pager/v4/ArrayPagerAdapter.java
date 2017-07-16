/*
 * Copyright (C) 2011 The Android Open Source Project
 * Portions copyright (c) 2013 CommonsWare, LLC 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commonsware.cwac.pager.v4;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.commonsware.cwac.pager.PageDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

abstract public class ArrayPagerAdapter<T extends Fragment> extends
        PagerAdapter {
    public static final RetentionStrategy KEEP = new RetentionStrategy() {
        public void attach(Fragment fragment,
                           FragmentTransaction currTransaction) {
            currTransaction.attach(fragment);
        }

        public void detach(Fragment fragment,
                           FragmentTransaction currTransaction) {
            currTransaction.detach(fragment);
        }
    };
    private static final String KEY_DESCRIPTORS = "descriptors";
    private final FragmentManager fm;
    private ArrayList<PageEntry> entries = new ArrayList<PageEntry>();
    private FragmentTransaction currTransaction = null;
    private T currPrimaryItem = null;
    private HashMap<Fragment, Integer> positionDelta = new HashMap<Fragment, Integer>();
    private RetentionStrategy retentionStrategy = null;

    public ArrayPagerAdapter(FragmentManager fragmentManager,
                             List<PageDescriptor> descriptors) {
        this(fragmentManager, descriptors, null);
    }

    public ArrayPagerAdapter(FragmentManager fragmentManager,
                             List<PageDescriptor> descriptors,
                             RetentionStrategy retentionStrategy) {
        this.fm = fragmentManager;
        this.entries = new ArrayList<PageEntry>();

        for (PageDescriptor desc : descriptors) {
            validatePageDescriptor(desc);

            entries.add(new PageEntry(desc));
        }

        this.retentionStrategy = retentionStrategy;

        if (this.retentionStrategy == null) {
            this.retentionStrategy = KEEP;
        }
    }

    abstract protected T createFragment(PageDescriptor desc);

    @Override
    public int getCount() {
        return (entries.size());
    }

    @Override
    public void startUpdate(ViewGroup container) {
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (currTransaction == null) {
            currTransaction = fm.beginTransaction();
        }

        Fragment fragment = getExistingFragment(position);

        if (fragment != null) {
            if (fragment.getId() == container.getId()) {
                retentionStrategy.attach(fragment, currTransaction);
            } else {
                fm.beginTransaction().remove(fragment).commit();
                fm.executePendingTransactions();

                currTransaction.add(container.getId(), fragment,
                        getFragmentTag(position));
            }
        } else {
            fragment = createFragment(entries.get(position).getDescriptor());
            currTransaction.add(container.getId(), fragment,
                    getFragmentTag(position));
        }

        if (fragment != currPrimaryItem) {
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
        }

        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position,
                            Object object) {
        if (currTransaction == null) {
            currTransaction = fm.beginTransaction();
        }

        retentionStrategy.detach((Fragment) object, currTransaction);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setPrimaryItem(ViewGroup container, int position,
                               Object object) {
        T fragment = (T) object;

        if (fragment != currPrimaryItem) {
            if (currPrimaryItem != null) {
                currPrimaryItem.setMenuVisibility(false);
                currPrimaryItem.setUserVisibleHint(false);
            }

            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }

            currPrimaryItem = fragment;
        }
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (currTransaction != null) {
            currTransaction.commitAllowingStateLoss();
            currTransaction = null;
            fm.executePendingTransactions();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view;
    }

    @Override
    public Parcelable saveState() {
        Bundle state = new Bundle();

        state.putParcelableArrayList(KEY_DESCRIPTORS, entries);

        return (state);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle b = (Bundle) state;

            b.setClassLoader(getClass().getClassLoader());

            entries = ((Bundle) state).getParcelableArrayList(KEY_DESCRIPTORS);
            notifyDataSetChanged();
        }
    }

    @Override
    public String getPageTitle(int position) {
        return (getPageDescriptor(position).getTitle());
    }

    @Override
    public int getItemPosition(Object o) {
        Integer result = positionDelta.get(o);

        if (result == null) {
            return (PagerAdapter.POSITION_UNCHANGED);
        }

        return (result);
    }

    public PageDescriptor getPageDescriptor(int position) {
        return (entries.get(position).getDescriptor());
    }

    public int getPositionForTag(String tag) {
        for (int i = 0; i < entries.size(); i++) {
            PageEntry entry = entries.get(i);

            if (entry.getDescriptor().getFragmentTag().equals(tag)) {
                return (i);
            }
        }

        return (-1);
    }

    public void add(PageDescriptor desc) {
        validatePageDescriptor(desc);

        positionDelta.clear();
        entries.add(new PageEntry(desc));
        notifyDataSetChanged();
    }

    public void insert(PageDescriptor desc, int position) {
        validatePageDescriptor(desc);

        positionDelta.clear();

        for (int i = position; i < entries.size(); i++) {
            Fragment f = getExistingFragment(i);

            if (f != null) {
                positionDelta.put(f, i + 1);
            }
        }

        entries.add(position, new PageEntry(desc));
        notifyDataSetChanged();
    }

    public void remove(int position) {
        positionDelta.clear();

        Fragment f = getExistingFragment(position);

        if (f != null) {
            positionDelta.put(f, PagerAdapter.POSITION_NONE);
        }

        for (int i = position + 1; i < entries.size(); i++) {
            f = getExistingFragment(i);

            if (f != null) {
                positionDelta.put(f, i - 1);
            }
        }

        entries.remove(position);
        notifyDataSetChanged();
    }

    public void move(int oldPosition, int newPosition) {
        if (oldPosition != newPosition) {
            PageDescriptor desc = getPageDescriptor(oldPosition);

            remove(oldPosition);
            insert(desc, newPosition);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public T getExistingFragment(int position) {
        return (T) (fm.findFragmentByTag(getFragmentTag(position)));
    }


    @Nullable
    public T getCurrentFragment() {
        return (currPrimaryItem);
    }

    private String getFragmentTag(int position) {
        return (getPageDescriptor(position).getFragmentTag());
    }

    private void validatePageDescriptor(PageDescriptor desc) {
        for (PageEntry entry : entries) {
            if (desc.getFragmentTag().equals(entry.getDescriptor()
                    .getFragmentTag())) {
                throw new IllegalArgumentException(
                        "PageDescriptor tag not unique: "
                                + desc.getFragmentTag());
            }
        }
    }

    public interface RetentionStrategy {
        void attach(Fragment fragment, FragmentTransaction currTransaction);

        void detach(Fragment fragment, FragmentTransaction currTransaction);
    }

    private static class PageEntry implements Parcelable {
        public static final Creator<PageEntry> CREATOR =
                new Creator<PageEntry>() {
                    public PageEntry createFromParcel(Parcel in) {
                        return new PageEntry(in);
                    }

                    public PageEntry[] newArray(int size) {
                        return new PageEntry[size];
                    }
                };
        private PageDescriptor descriptor = null;
        private Fragment.SavedState state = null;

        PageEntry(PageDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        PageEntry(Parcel in) {
            this.descriptor = in.readParcelable(getClass().getClassLoader());
            this.state = in.readParcelable(getClass().getClassLoader());
        }

        PageDescriptor getDescriptor() {
            return (descriptor);
        }

        @Override
        public int describeContents() {
            return (0);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeParcelable(descriptor, 0);
            out.writeParcelable(state, 0);
        }
    }
}
