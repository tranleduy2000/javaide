package com.duy.project.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.duy.ide.R;
import com.duy.ide.activities.AbstractAppCompatActivity;
import com.duy.ide.file.FileManager;
import com.duy.project.ProjectFile;
import com.duy.project.fragments.FolderStructureFragment;

import java.io.File;
import java.io.IOException;


public class FileManagerActivity extends AbstractAppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        try {
            ProjectFile projectFile = new ProjectFile("Main", "com.duy.example", "Demo100");
             projectFile.create(new File(FileManager.EXTERNAL_DIR));
            fragmentTransaction.replace(R.id.container, FolderStructureFragment.newInstance(projectFile)).commit();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}