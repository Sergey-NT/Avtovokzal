package com.www.avtovokzal.org;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.www.avtovokzal.org.Adapter.RouteObjectInfoArrivalAdapter;
import com.www.avtovokzal.org.Object.RouteObjectInfoArrival;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InfoArrivalActivity extends AppCompatSettingsActivity {

    private AdView adView;
    private Drawer drawerResult = null;
    private InterstitialAd interstitial;
    private ListView listView;
    private ProgressDialog progressDialog;
    private Toolbar toolbar;


    private String number;
    private String timePrib;
    private String timeFromStation;
    private String name;

    private boolean AdShowGone;

    private final static String TAG = "InfoArrivalActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_arrival);

        TextView textViewName;
        TextView textViewTime;
        SharedPreferences settings;

        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);

        // Переменная, отвечает за работу с настройками
        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        // Проверка отключена ли реклама
        AdShowGone = settings.contains(APP_PREFERENCES_ADS_SHOW) && settings.getBoolean(APP_PREFERENCES_ADS_SHOW, false);

        if (DEVELOPER) {
            AdShowGone = true;
        }

        // Реклама в приложении
        if (!AdShowGone) {
            initializeAd();
        }

        // Определяем элементы интерфейса
        listView = (ListView) findViewById(R.id.listViewInfoArrival);
        textViewName = (TextView) findViewById(R.id.textViewInfoArrivalName);
        textViewTime = (TextView) findViewById(R.id.textViewInfoArrivalTimeToStation);

        // Получаем переменные
        number = getIntent().getStringExtra("number");
        name = getIntent().getStringExtra("name");
        timePrib = getIntent().getStringExtra("timePrib");
        timeFromStation = getIntent().getStringExtra("timeFromStation");

        String string = number + " " + name;
        textViewName.setText(string);
        string = getString(R.string.arrival_time) + " " + timePrib;
        textViewTime.setText(string);

        // Загружаем информацию об остановках на маршруте
        loadRouteInfoArrival(number, timePrib, timeFromStation);

        listViewListener();

        initializeToolbar();
        initializeNavigationDrawer();
    }

    private void listViewListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RelativeLayout relativeLayout = (RelativeLayout) view;
                TextView textViewNameStation = (TextView) relativeLayout.getChildAt(0);
                TextView textViewNoteStation = (TextView) relativeLayout.getChildAt(2);

                String codeStation = textViewNameStation.getTag().toString();
                String nameStation = textViewNameStation.getText().toString();
                String noteStation = textViewNoteStation.getText().toString();

                setCountOnePlus();

                Intent intent = new Intent(getApplicationContext(), ArrivalActivity.class);
                intent.putExtra("newNameStation", nameStation + " " + noteStation);
                intent.putExtra("code", codeStation);

                if (LOG_ON) Log.v("newNameStation", "" + nameStation + " " + noteStation);
                // Google Analytics
                Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
                t.send(new HitBuilders.EventBuilder()
                        .setCategory(getString(R.string.analytics_category_listview))
                        .setAction(getString(R.string.analytics_action_listview_arrival_info))
                        .build());

                startActivity(intent);
                finish();
            }
        });
    }

    private void initializeAd() {
        // Создание межстраничного объявления
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(getString(R.string.admob_interstitial));

        // Создание запроса объявления.
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("4B954499F159024FD4EFD592E7A5F658")
                .build();

        // Запуск загрузки межстраничного объявления
        interstitial.loadAd(adRequest);
        // Создание экземпляра adView
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.admob_info_activity));
        adView.setAdSize(AdSize.SMART_BANNER);

        // Поиск разметки LinearLayout
        LinearLayout layout = (LinearLayout)findViewById(R.id.adViewInfoArrivalActivity);

        // Добавление в разметку экземпляра adView
        layout.addView(adView);

        // Инициирование общего запроса
        AdRequest request = new AdRequest.Builder()
                .addTestDevice("4B954499F159024FD4EFD592E7A5F658")
                .build();

        // Загрузка adView с объявлением
        adView.loadAd(request);
    }

    private void initializeNavigationDrawer() {
        drawerResult = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withDisplayBelowStatusBar(true)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(getDrawerItems())
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem iDrawerItem) {
                        switch (position) {
                            case 1:
                                setCountOnePlus();
                                Intent intentMain = new Intent(InfoArrivalActivity.this, MainActivity.class);
                                startActivity(intentMain);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 2:
                                setCountOnePlus();
                                Intent intentArrival = new Intent(InfoArrivalActivity.this, ArrivalActivity.class);
                                startActivity(intentArrival);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 4:
                                setCountOnePlus();
                                Intent intentEtraffic = new Intent(InfoArrivalActivity.this, EtrafficActivity.class);
                                startActivity(intentEtraffic);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 6:
                                setCountOnePlus();
                                Intent intentEtrafficMain = new Intent(InfoArrivalActivity.this, EtrafficMainActivity.class);
                                startActivity(intentEtrafficMain);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 8:
                                setCountOnePlus();
                                Intent intentMenu = new Intent(InfoArrivalActivity.this, MenuActivity.class);
                                startActivity(intentMenu);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 9:
                                setCountOnePlus();
                                Intent intentAbout = new Intent(InfoArrivalActivity.this, AboutActivity.class);
                                startActivity(intentAbout);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                        }
                        return false;
                    }
                })
                .build();
    }

    private void initializeToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name);
            toolbar.setSubtitle(R.string.app_subtitle_info);
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
        int count;
        count = getCountAD();
        if (count % 5 == 0) {
            if (!AdShowGone) {
                if (!getSettingsParams(APP_PREFERENCES_ADS_SHOW)) {
                    if (interstitial.isLoaded()) {
                        setCountOnePlus();
                        interstitial.show();
                    }
                }
            }
        }
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }  finally {
            progressDialog = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        // Скрываем статус сервера
        MenuItem item = menu.findItem(R.id.lamp);
        item.setVisible(false);
        return true;
    }

    // Запрос информации об остановках на маршруте
    private void loadRouteInfoArrival(Object... params) {
        String url = "http://www.avtovokzal.org/php/app/infoArrival.php?number="+params[0]+"&time="+params[1]+"&time_from="+params[2];

        if (isOnline()) {
            progressDialog = new ProgressDialog(InfoArrivalActivity.this);
            progressDialog.setMessage(getString(R.string.main_load));
            progressDialog.setCancelable(false);
            progressDialog.show();

            StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response == null) {
                        callErrorActivity();
                        finish();
                    }
                    processingLoadRouteInfoArrival task = new processingLoadRouteInfoArrival();
                    task.execute(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(LOG_ON) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());}
                    try {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }  finally {
                        progressDialog = null;
                    }
                    callErrorActivity();
                }
            });
            // Установливаем TimeOut, Retry
            strReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Добавляем запрос в очередь
            AppController.getInstance().addToRequestQueue(strReq);
        } else {
            callErrorActivity();
        }
    }

    // Вызов Error Activity
    private void callErrorActivity(){
        Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
        intent.putExtra("activity", TAG);
        intent.putExtra("number", number);
        intent.putExtra("name", name);
        intent.putExtra("timePrib", timePrib);
        intent.putExtra("timeFromStation", timeFromStation);
        startActivity(intent);
        finish();
    }

    private class processingLoadRouteInfoArrival extends AsyncTask<String, Void, List<RouteObjectInfoArrival>> {
        @Override
        protected List<RouteObjectInfoArrival> doInBackground(String... response) {
            JSONObject dataJsonQbj;
            List<RouteObjectInfoArrival> list = new ArrayList<>();

            if(LOG_ON){Log.d(TAG, response[0]);}

            try {
                dataJsonQbj = new JSONObject(response[0]);
                JSONArray rasp = dataJsonQbj.getJSONArray("info_arrival");

                for (int i = 0; i < rasp.length(); i++) {
                    JSONObject oneObject = rasp.getJSONObject(i);

                    String timeOtpr = oneObject.getString("time_otpr");
                    String nameStation = oneObject.getString("name_station");
                    String noteStation = oneObject.getString("note_station");
                    String code = oneObject.getString("id_station");
                    list.add(new RouteObjectInfoArrival(timeOtpr, nameStation, noteStation, code));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<RouteObjectInfoArrival> list) {
            final RouteObjectInfoArrivalAdapter adapter = new RouteObjectInfoArrivalAdapter(InfoArrivalActivity.this, list);
            listView.setAdapter(adapter);
            super.onPostExecute(list);

            try {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }  finally {
                progressDialog = null;
            }
        }
    }
}
