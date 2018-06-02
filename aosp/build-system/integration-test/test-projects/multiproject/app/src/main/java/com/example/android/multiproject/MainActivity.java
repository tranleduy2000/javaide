package com.example.android.multiproject;

import com.example.android.multiproject.library.ShowPeopleActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void sendMessage(View view) {
        Intent intent = new Intent(this, ShowPeopleActivity.class);
        startActivity(intent);
    }
}
