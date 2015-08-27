package com.www.avtovokzal.org;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

public class AppCompatSettingsActivity extends AppCompatActivity {

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
    public static final String APP_PREFERENCES_DATE = "date";

    // Получаем параметры из файла настроек
    public boolean getSettingsParams(String params) {
        boolean checkValue;
        SharedPreferences settings;

        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        checkValue = settings.getBoolean(params, false);

        return checkValue;
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
