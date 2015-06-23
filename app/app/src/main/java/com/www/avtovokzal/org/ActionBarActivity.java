package com.www.avtovokzal.org;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ActionBarActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Установка названия приложения в две строки и цвета ActionBar
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle(getString(R.string.app_name));
        ab.setSubtitle(getString(R.string.app_subtitle));
        ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#607D8B")));
    }
}
