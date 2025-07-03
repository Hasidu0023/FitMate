package com.example.fitmate.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Go directly to registration screen
        Intent intent = new Intent(MainActivity.this, LoginUser.class);
        startActivity(intent);
        finish(); // prevent back press to return here
    }
}
