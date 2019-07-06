package com.andruid.magic.helpfulsense.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.andruid.magic.helpfulsense.R;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean first = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_first), true);
        if(first)
            startActivity(new Intent(this, IntroActivity.class));
        else
            startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}