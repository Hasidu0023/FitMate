package com.example.fitmate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitmate.R;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // Wait 2.5 seconds, then go to LoginUser activity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(LoadingActivity.this, LoginUser.class);
            startActivity(intent);
            finish(); // finish to remove this activity from back stack
        }, 2500); // Delay in milliseconds (2500 = 2.5 seconds)
    }
}
