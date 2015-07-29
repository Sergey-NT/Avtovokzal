package com.www.avtovokzal.org;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

public class AboutActivity extends AppCompatSettingsActivity {

    private AdView adView;
    private SharedPreferences settings;
    private Toolbar toolbar;
    private Drawer drawerResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        boolean AdShowGone;

        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);

        // Переменная, отвечает за работу с настройками
        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        // Проверка отключения рекламы
        AdShowGone = settings.contains(APP_PREFERENCES_ADS_SHOW) && settings.getBoolean(APP_PREFERENCES_ADS_SHOW, false);

        if (DEVELOPER) {
            AdShowGone = true;
        }

        // Реклама в приложении
        if (!AdShowGone) {
            // Создание экземпляра adView
            adView = new AdView(this);
            adView.setAdUnitId(getString(R.string.admob_menu_activity));
            adView.setAdSize(AdSize.SMART_BANNER);

            // Поиск разметки LinearLayout
            LinearLayout layout = (LinearLayout)findViewById(R.id.adViewAboutActivity);

            // Добавление в разметку экземпляра adView
            layout.addView(adView);

            // Инициирование общего запроса
            AdRequest request = new AdRequest.Builder().build();

            // Загрузка adView с объявлением
            adView.loadAd(request);
        }

        // Создаем кликабельные ссылки в TextView
        TextView tv1 = (TextView) findViewById(R.id.about_icons);
        tv1.setMovementMethod(LinkMovementMethod.getInstance());
        TextView tv2 = (TextView) findViewById(R.id.about_developer);
        tv2.setMovementMethod(LinkMovementMethod.getInstance());

        initializeToolbar();
        initializeNavigationDrawer();
    }

    private void initializeNavigationDrawer() {
        drawerResult = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withDisplayBelowToolbar(true)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        new SectionDrawerItem()
                                .withName(R.string.app_name_city),
                        new PrimaryDrawerItem()
                                .withName(R.string.app_subtitle_main)
                                .withIcon(R.drawable.ic_vertical_align_top_black_18dp),
                        new PrimaryDrawerItem()
                                .withName(R.string.app_subtitle_arrival)
                                .withIcon(R.drawable.ic_vertical_align_bottom_black_18dp),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem()
                                .withName(R.string.menu_settings)
                                .withIcon(R.drawable.ic_settings_black_18dp),
                        new PrimaryDrawerItem()
                                .withName(R.string.menu_about)
                                .withIdentifier(1)
                                .withIcon(R.drawable.ic_info_outline_black_18dp)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> adapterView, View view, int position, long l, IDrawerItem iDrawerItem) {
                        switch (position) {
                            case 1:
                                finish();
                                return true;
                            case 2:
                                Intent intentArrival = new Intent(AboutActivity.this, ArrivalActivity.class);
                                startActivity(intentArrival);
                                finish();
                                return true;
                            case 4:
                                Intent intentMenu = new Intent(AboutActivity.this, MenuActivity.class);
                                startActivity(intentMenu);
                                finish();
                                return true;
                            case  5:
                                drawerResult.closeDrawer();
                                return true;
                        }
                        return false;
                    }
                })
                .build();
        drawerResult.setSelectionByIdentifier(1);
    }

    private void initializeToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name);
            toolbar.setSubtitle(R.string.menu_about);
            setSupportActionBar(toolbar);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerResult != null && drawerResult.isDrawerOpen()) {
            drawerResult.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Google Analytics
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Google Analytics
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
        if (adView != null && getSettingsParams(APP_PREFERENCES_ADS_SHOW)) {
            adView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    // Получаем параметры из файла настроек
    private boolean getSettingsParams(String params) {
        boolean checkBoxValue;
        checkBoxValue = settings.contains(params) && settings.getBoolean(params, false);
        return checkBoxValue;
    }
}
