package com.www.avtovokzal.org;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

public class AppCompatSettingsActivity extends AppCompatActivity {

    public static final boolean DEVELOPER = false;
    public static final boolean LOG_ON = false;
    public static final String APP_PREFERENCES_COUNT_AD = "countAd";
    public static final String APP_PREFERENCES = "settings";
    public static final String APP_PREFERENCES_ADS_SHOW = "adsDisable";
    public static final String APP_PREFERENCES_DEFAULT = "default";
    public static final String APP_PREFERENCES_STATION_NAME = "station_name";
    public static final String APP_PREFERENCES_STATION_CODE = "station_code";
    public static final String APP_PREFERENCES_CANCEL = "cancel";
    public static final String APP_PREFERENCES_SELL = "sell";
    public static final String APP_PREFERENCES_LOAD = "load";
    public static final String APP_PREFERENCES_DATE = "date";

    SharedPreferences settings;

    // Получаем параметры из файла настроек
    public boolean getSettingsParams(String params) {
        boolean checkValue;
        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        checkValue = settings.getBoolean(params, false);
        return checkValue;
    }

    public int getCountAD() {
        int count;
        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        count = settings.getInt(APP_PREFERENCES_COUNT_AD, 0);
        if(LOG_ON) Log.v("Count", ""+count);
        return count;
    }

    public void setCountOnePlus() {
        int count;
        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        count = settings.getInt(APP_PREFERENCES_COUNT_AD, 0);
        count++;
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(APP_PREFERENCES_COUNT_AD, count);
        editor.apply();
    }

    // Проверка подключения к сети интернет
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @NonNull
    public IDrawerItem[] getDrawerItems() {
        return new IDrawerItem[]{new SectionDrawerItem()
                .withName(R.string.app_name_city),
                new PrimaryDrawerItem()
                        .withName(R.string.app_subtitle_main)
                        .withIdentifier(1)
                        .withIcon(R.drawable.ic_vertical_align_top_black_18dp),
                new PrimaryDrawerItem()
                        .withName(R.string.app_subtitle_arrival)
                        .withIdentifier(2)
                        .withIcon(R.drawable.ic_vertical_align_bottom_black_18dp),
                new SectionDrawerItem()
                        .withName(R.string.app_name_city_ggm),
                new PrimaryDrawerItem()
                        .withName(R.string.app_subtitle_main)
                        .withIdentifier(3)
                        .withIcon(R.drawable.ic_vertical_align_top_black_18dp),
                new SectionDrawerItem()
                        .withName(R.string.app_name_city_ekb),
                new PrimaryDrawerItem()
                        .withName(R.string.app_subtitle_main)
                        .withIdentifier(4)
                        .withIcon(R.drawable.ic_vertical_align_top_black_18dp),
                new DividerDrawerItem(),
                new PrimaryDrawerItem()
                        .withName(R.string.menu_settings)
                        .withIdentifier(5)
                        .withIcon(R.drawable.ic_settings_black_18dp),
                new PrimaryDrawerItem()
                        .withName(R.string.menu_about)
                        .withIdentifier(6)
                        .withIcon(R.drawable.ic_info_outline_black_18dp)};
    }
}
