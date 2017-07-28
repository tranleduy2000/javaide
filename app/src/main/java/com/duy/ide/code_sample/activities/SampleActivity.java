package com.duy.ide.code_sample.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
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
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setTitle(R.string.code_sample);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
    public void onProjectClick(final String projectName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("This action will be create new project");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File out = new File(FileManager.EXTERNAL_DIR, projectName);
                int count = 1;
                while (out.exists()) {
                    out = new File(FileManager.EXTERNAL_DIR, projectName + count);
                    count++;
                }
                boolean success = SampleUtil.extractTo(SampleActivity.this, out, category, projectName);
                if (success) {
                    ProjectFile pf = ProjectManager.createProjectIfNeed(out);
                    Intent intent = getIntent();
                    intent.putExtra(PROJECT_FILE, pf);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(SampleActivity.this, "Can not copy project!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();

    }
}
