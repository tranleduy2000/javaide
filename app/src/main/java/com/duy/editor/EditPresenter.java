package com.duy.editor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.commonsware.cwac.pager.SimplePageDescriptor;
import com.duy.compile.diagnostic.DiagnosticContract;
import com.duy.editor.editor.EditorPagerAdapter;
import com.duy.editor.file.FileManager;
import com.duy.editor.setting.JavaPreferences;

import java.io.File;

/**
 * Created by duy on 19/07/2017.
 */

public class EditPresenter implements EditContract.Presenter {

    private static final String TAG = "EditPresenter";
    private ViewPager mViewPager;
    private EditorPagerAdapter mPageAdapter;
    private TabLayout mTabLayout;
    private JavaPreferences mPreferences;
    private FileManager mFileManager;
    private Context mContext;

    public EditPresenter(Context context, ViewPager mViewPager,
                         EditorPagerAdapter mPageAdapter, TabLayout tabLayout,
                         FileManager fileManager) {
        this.mViewPager = mViewPager;
        this.mPageAdapter = mPageAdapter;
        this.mTabLayout = tabLayout;
        mPreferences = new JavaPreferences(context);
        mFileManager = fileManager;
        mContext = context;
    }

    @Override
    public void gotoPage(File file) {
        if (file == null) return;
        gotoPage(file.getPath());
    }

    @Override
    public void gotoPage(@NonNull String path) {
        int pos = mPageAdapter.getPositionForTag(path);
        mViewPager.setCurrentItem(pos);
    }

    @Override
    public void addPage(String path, boolean select) {
        try {
            File f = new File(path);
            if (f.exists()) {
                addPage(f, select);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void addPage(File file, boolean select) {
        if (file == null || !file.exists()) return;

        int position = mPageAdapter.getPositionForTag(file.getPath());
        if (position != -1) { //existed in list file
            //check need select tab
            if (select) {
                TabLayout.Tab tab = mTabLayout.getTabAt(position);
                if (tab != null) {
                    tab.select();
                    mViewPager.setCurrentItem(position);
                }
            }
        } else { //new file
            if (mPageAdapter.getCount() >= mPreferences.getMaxPage()) {
                Fragment existingFragment = mPageAdapter.getExistingFragment(0);
                if (existingFragment != null) {
                    mFileManager.removeTabFile(existingFragment.getTag());
                    removePage(0);
                }
            }

            //add to database
            mFileManager.addNewPath(file.getPath());

            //new page
            mPageAdapter.add(new SimplePageDescriptor(file.getPath(), file.getName()));
            invalidateTab();

            if (select) {
                int indexOfNewPage = mPageAdapter.getCount() - 1;
                TabLayout.Tab tab = mTabLayout.getTabAt(indexOfNewPage);
                if (tab != null) {
                    tab.select();
                    mViewPager.setCurrentItem(indexOfNewPage);
                }
            }
        }
    }

    @Override
    public void invalidateTab() {

    }

    @Override
    public void removePage(String path) {

    }

    @Override
    public void removePage(int position) {
        Fragment existingFragment = mPageAdapter.getExistingFragment(position);
        if (existingFragment == null) {
            if (DLog.DEBUG) DLog.d(TAG, "removePage: " + "null page " + position);
            return;
        }

        //delete in database
        String filePath = existingFragment.getTag();
        mFileManager.removeTabFile(filePath);

        //remove page
        mPageAdapter.remove(position);
        invalidateTab();

        Toast.makeText(mContext,
                mContext.getString(R.string.closed) + " " + new File(filePath).getName(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean hasPage(String path) {
        return false;
    }

    @Override
    public int getPagePosition(String path) {
        return 0;
    }

    @Override
    public DiagnosticContract.View getCurrentPage() {
        return null;
    }

    @Override
    public void showError(DiagnosticContract.View view, int line) {

    }
}
