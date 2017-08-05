package com.duy.project.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.duy.ide.R;
import com.duy.ide.activities.AbstractAppCompatActivity;


public class FileManagerActivity extends AbstractAppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();


    }


}