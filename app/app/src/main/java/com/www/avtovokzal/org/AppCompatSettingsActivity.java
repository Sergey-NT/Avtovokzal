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
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AppCompatSettingsActivity extends AppCompatActivity {

    public AdView adView;
    public SharedPreferences settings;
    public Toolbar toolbar;

    private static final String TAG = "AppCompatSettings";

    // Получаем параметры из файла настроек
    public boolean getSettingsParams(String params) {
        settings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        return settings.getBoolean(params, false);
    }

    // Проверка подключения к сети интернет
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @NonNull
    public IDrawerItem[] getDrawerItems() {
        return new IDrawerItem[]{
                new SectionDrawerItem()
                        .withName(R.string.app_name_city),
                new PrimaryDrawerItem()
                        .withName(R.string.app_subtitle_main)
                        .withIdentifier(1)
                        .withIcon(GoogleMaterial.Icon.gmd_vertical_align_top),
                new PrimaryDrawerItem()
                        .withName(R.string.app_subtitle_arrival)
                        .withIdentifier(2)
                        .withIcon(GoogleMaterial.Icon.gmd_vertical_align_bottom),
                new SectionDrawerItem()
                        .withName(R.string.app_name_city_ggm),
                new PrimaryDrawerItem()
                        .withName(R.string.app_subtitle_main)
                        .withIdentifier(3)
                        .withIcon(GoogleMaterial.Icon.gmd_vertical_align_top),
                new SectionDrawerItem()
                        .withName(R.string.app_name_city_ekb),
                new PrimaryDrawerItem()
                        .withName(R.string.app_subtitle_main)
                        .withIdentifier(4)
                        .withIcon(GoogleMaterial.Icon.gmd_vertical_align_top),
                new DividerDrawerItem(),
                new PrimaryDrawerItem()
                        .withName(R.string.menu_settings)
                        .withIdentifier(5)
                        .withIcon(GoogleMaterial.Icon.gmd_settings),
                new PrimaryDrawerItem()
                        .withName(R.string.menu_about)
                        .withIdentifier(6)
                        .withIcon(GoogleMaterial.Icon.gmd_info_outline)};
    }

    @SuppressWarnings("ConstantConditions")
    public void initializeAd(int layoutId) {
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.admob_activity));
        adView.setAdSize(AdSize.SMART_BANNER);

        LinearLayout layout = (LinearLayout)findViewById(layoutId);
        layout.addView(adView);

        AdRequest request = new AdRequest.Builder()
                // Nexus 5
                .addTestDevice("4B954499F159024FD4EFD592E7A5F658")
                .build();

        adView.loadAd(request);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdOpened() {
                onClickAd();
                if (Constants.LOG_ON) Log.v(TAG, "Ad opened");
                super.onAdOpened();
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
        if (Constants.LOG_ON) Log.v(TAG, date);

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.APP_PREFERENCES_AD_DATE, date);
        editor.putBoolean(Constants.APP_PREFERENCES_ADS_SHOW, true);
        editor.apply();
    }
}
