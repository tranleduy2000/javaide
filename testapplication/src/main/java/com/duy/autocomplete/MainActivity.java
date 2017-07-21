package com.duy.autocomplete;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.duy.autocomplete.autocomplete.AutoCompleteProvider;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AutoCompleteCodeEditText mEditText;
    private AutoCompleteProvider mAutoCompleteProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
            return;
        }
        mEditText = (AutoCompleteCodeEditText) findViewById(R.id.edit_input);

        mAutoCompleteProvider = new AutoCompleteProvider(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAutoCompleteProvider.load();
                mEditText.setAutoCompleteProvider(mAutoCompleteProvider);
            }
        }).start();
    }

}
