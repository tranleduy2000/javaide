package com.duy.ide.code_sample.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.duy.ide.R;
import com.duy.ide.activities.AbstractAppCompatActivity;
import com.duy.ide.code_sample.fragments.SelectCategoryFragment;
import com.duy.ide.code_sample.fragments.SelectProjectFragment;
import com.duy.ide.code_sample.model.SampleUtil;
import com.duy.ide.file.FileManager;
import com.duy.project.ProjectFile;
import com.duy.project.ProjectManager;

import java.io.File;

/**
 * Created by Duy on 27-Jul-17.
 */

public class SampleActivity extends AbstractAppCompatActivity implements
        SelectCategoryFragment.CategoryClickListener, SelectProjectFragment.ProjectClickListener {
    public static final String PROJECT_FILE = "project_file";
    private String category = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
        fm.replace(R.id.content, SelectCategoryFragment.newInstance()).commit();
    }

    @Override
    public void onCategoryClick(String category) {
        this.category = category;
        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
        fm.add(R.id.content, SelectProjectFragment.newInstance(category), SelectProjectFragment.TAG).commit();
        fm.addToBackStack(null);
    }


    @Override
    public void onProjectClick(String projectName) {
        File file = new File(FileManager.EXTERNAL_DIR_SRC, projectName);
        int count = 1;
        while (file.exists()) {
            file = new File(FileManager.EXTERNAL_DIR_SRC, projectName + count);
        }
        boolean success = SampleUtil.extractTo(this,
                new File(FileManager.EXTERNAL_DIR_SRC), category, projectName);
        if (success) {
            ProjectFile pf = ProjectManager.createProjectIfNeed(file);
            Intent intent = getIntent();
            intent.putExtra(PROJECT_FILE, pf);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(this, "Can not copy project!", Toast.LENGTH_SHORT).show();
        }
    }
}
