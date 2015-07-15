package com.www.avtovokzal.org;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ActionBarActivity extends AppCompatActivity {

    public static final boolean DEVELOPER = true;
    public static final boolean LOG_ON = true;
    public static final String APP_PREFERENCES = "settings";
    public static final String APP_PREFERENCES_ADS_SHOW = "adsDisable";
    public static final String APP_PREFERENCES_DEFAULT = "default";
    public static final String APP_PREFERENCES_STATION_NAME = "station_name";
    public static final String APP_PREFERENCES_STATION_CODE = "station_code";
    public static final String APP_PREFERENCES_CANCEL = "cancel";
    public static final String APP_PREFERENCES_SELL = "sell";
    public static final String APP_PREFERENCES_LOAD = "load";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Установка названия приложения в две строки и цвета ActionBar
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle(getString(R.string.app_name));
        ab.setSubtitle(getString(R.string.app_subtitle));
        ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary)));
    }
}
