package com.www.avtovokzal.org;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AppCompatSettingsActivity extends AppCompatActivity {

    public static final boolean DEVELOPER = false;
    public static final boolean LOG_ON = false;
    public static final String APP_PREFERENCES = "settings";
    public static final String APP_PREFERENCES_ADS_SHOW = "adsDisable";
    public static final String APP_PREFERENCES_DEFAULT = "default";
    public static final String APP_PREFERENCES_STATION_NAME = "station_name";
    public static final String APP_PREFERENCES_STATION_CODE = "station_code";
    public static final String APP_PREFERENCES_CANCEL = "cancel";
    public static final String APP_PREFERENCES_SELL = "sell";
    public static final String APP_PREFERENCES_LOAD = "load";
    public static final String APP_PREFERENCES_DATE = "date";
    public static final String APP_PREFERENCES_AD_DATE = "date_ad_click";

    public AdView adView;
    public InterstitialAd interstitial;
    public SharedPreferences settings;
    public Toolbar toolbar;

    private static final String TAG = "AppCompatSettings";
    private static final String APP_PREFERENCES_COUNT_AD = "countAd";

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

    public void initializeAd(int layoutId) {
        // Создание межстраничного объявления
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(getString(R.string.admob_interstitial));

        // Создание запроса объявления.
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("4B954499F159024FD4EFD592E7A5F658")
                .addTestDevice("4A47A797D4302A0BEC716C29A53C4881")
                .build();

        // Запуск загрузки межстраничного объявления
        interstitial.loadAd(adRequest);

        // Создание экземпляра adView
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.admob_activity));
        adView.setAdSize(AdSize.SMART_BANNER);

        // Поиск разметки LinearLayout
        LinearLayout layout = (LinearLayout)findViewById(layoutId);

        // Добавление в разметку экземпляра adView
        layout.addView(adView);

        // Инициирование общего запроса
        AdRequest request = new AdRequest.Builder()
                .addTestDevice("4B954499F159024FD4EFD592E7A5F658")
                .addTestDevice("4A47A797D4302A0BEC716C29A53C4881")
                .build();

        // Загрузка adView с объявлением
        adView.loadAd(request);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdOpened() {
                onClickAd();
                if (LOG_ON) Log.v(TAG, "Ad opened");
                super.onAdOpened();
            }
        });

        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLeftApplication() {
                onClickAd();
                if (LOG_ON) Log.v(TAG, "Left application");
                super.onAdLeftApplication();
            }
        });
    }

    public void initializeToolbar(int title, int subtitle) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(title);
            toolbar.setSubtitle(subtitle);
            setSupportActionBar(toolbar);
        }
    }

    private void onClickAd () {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        String date = sdf.format(calendar.getTime());
        if (LOG_ON) Log.v(TAG, date);

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(APP_PREFERENCES_AD_DATE, date);
        editor.putBoolean(APP_PREFERENCES_ADS_SHOW, true);
        editor.apply();
    }
}
