package com.www.avtovokzal.org;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class ErrorActivity extends AppCompatSettingsActivity {

    private String number;
    private String time;
    private String timePrib;
    private String timeFromStation;
    private String activity;
    private String numberToView;
    private String name;
    private String code;
    private String newNameStation;
    private int day;
    private boolean cancel;
    private boolean sell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);

        number = getIntent().getStringExtra("number");
        time = getIntent().getStringExtra("time");
        timePrib = getIntent().getStringExtra("timePrib");
        timeFromStation = getIntent().getStringExtra("timeFromStation");
        activity = getIntent().getStringExtra("activity");
        numberToView = getIntent().getStringExtra("numberToView");
        name = getIntent().getStringExtra("name");
        day = getIntent().getIntExtra("day", 0);
        code = getIntent().getStringExtra("code");
        cancel = getIntent().getBooleanExtra("cancel", false);
        sell = getIntent().getBooleanExtra("sell", false);
        newNameStation = getIntent().getStringExtra("newNameStation");

        if (LOG_ON) Log.v("Params", "newNameStation: " + newNameStation + "Number: " + number + " Time: " + time + " TimePrib: " + timePrib + " TimeFromStation: " + timeFromStation + " Activity: " + activity + " Number: " + numberToView + " Name: " + name + " Day: " + day + " Code: " + code + " Cancel: " + cancel + " Sell: " + sell);

        initializeToolbar(R.string.app_name, R.string.no_connect);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        // Скрываем статус сервера
        MenuItem itemLamp = menu.findItem(R.id.lamp);
        itemLamp.setVisible(false);
        return true;
    }

    public void onBack(View view){
        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.analytics_category_button))
                .setAction(getString(R.string.analytics_action_refresh))
                .build());

        switch (activity) {
            case "InfoActivity": {
                Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
                intent.putExtra("number", number);
                intent.putExtra("time", time);
                intent.putExtra("numberToView", numberToView);
                intent.putExtra("name", name);
                intent.putExtra("day", day);
                startActivity(intent);
                finish();
                break;
            }
            case "InfoArrivalActivity": {
                Intent intent = new Intent(getApplicationContext(), InfoArrivalActivity.class);
                intent.putExtra("number", number);
                intent.putExtra("name", name);
                intent.putExtra("timePrib", timePrib);
                intent.putExtra("timeFromStation", timeFromStation);
                startActivity(intent);
                finish();
                break;
            }
            case "ArrivalActivity": {
                Intent intent = new Intent(getApplicationContext(), ArrivalActivity.class);
                if (code != null) {
                    intent.putExtra("code", code);
                    intent.putExtra("newNameStation", newNameStation);
                }
                startActivity(intent);
                finish();
                break;
            }
            case "MainActivity": {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                if (code != null) {
                    intent.putExtra("code", code);
                }
                intent.putExtra("day", day);
                intent.putExtra("cancel", cancel);
                intent.putExtra("sell", sell);
                startActivity(intent);
                finish();
                break;
            }
            case "EtrafficActivity": {
                Intent intent = new Intent(getApplicationContext(), EtrafficActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case "EtrafficMainActivity": {
                Intent intent = new Intent(getApplicationContext(), EtrafficMainActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            default: {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
                break;
            }
        }
    }
}
