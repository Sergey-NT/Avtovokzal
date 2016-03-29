package com.www.avtovokzal.org;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import de.psdev.licensesdialog.LicensesDialog;

public class AboutActivity extends AppCompatActivity {

    private static final int LAYOUT = R.layout.activity_about;

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);

        Button btnLicense = (Button) findViewById(R.id.btnLicense);
        btnLicense.setTransformationMethod(null);

        // Создаем кликабельные ссылки в TextView
        TextView tv1 = (TextView) findViewById(R.id.about_icons);
        tv1.setMovementMethod(LinkMovementMethod.getInstance());
        TextView tv2 = (TextView) findViewById(R.id.about_developer);
        tv2.setMovementMethod(LinkMovementMethod.getInstance());

        initToolbar(R.string.app_name, R.string.menu_about);
    }

    @SuppressWarnings("ConstantConditions")
    public void initToolbar(int title, int subtitle) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(title);
            toolbar.setSubtitle(subtitle);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    public void btnLicenseOnClick (View view) {
        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.analytics_category_button))
                .setAction(getString(R.string.analytics_action_license))
                .build());

        new LicensesDialog.Builder(this)
                .setNotices(R.raw.notices)
                .build()
                .showAppCompat();
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
}
