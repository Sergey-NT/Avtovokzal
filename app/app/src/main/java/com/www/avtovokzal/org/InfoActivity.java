package com.www.avtovokzal.org;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.www.avtovokzal.org.Adapter.RouteObjectInfoAdapter;
import com.www.avtovokzal.org.Object.RouteObjectInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class InfoActivity extends AppCompatSettingsActivity {

    private Drawer drawerResult = null;
    private ListView listView;
    private ProgressDialog progressDialog;
    private Toolbar toolbar;

    private String number;
    private String time;
    private String numberToView;
    private String name;
    private int day;
    private boolean AdShowGone;

    private final static String TAG = "InfoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TextView textView;
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
            initializeAd(R.id.adViewInfoActivity);
        }

        // Определяем элементы интерфейса
        listView = (ListView) findViewById(R.id.listViewInfo);
        textView = (TextView) findViewById(R.id.textViewInfoName);

        // Получаем переменные
        number = getIntent().getStringExtra("number");
        numberToView = getIntent().getStringExtra("numberToView");
        time = getIntent().getStringExtra("time");
        name = getIntent().getStringExtra("name");
        day = getIntent().getIntExtra("day", 0);

        String string = time + " " + numberToView + " " + name;
        textView.setText(string);

        // Загружаем информацию об остановках на маршруте
        try {
            loadRouteInfo(number, time);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        listViewListener();

        initializeToolbar();
        initializeNavigationDrawer();
    }

    private void listViewListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RelativeLayout relativeLayout = (RelativeLayout) view;
                TextView textViewCodeStation = (TextView) relativeLayout.getChildAt(0);
                TextView textViewNoteStation = (TextView) relativeLayout.getChildAt(2);

                String codeStation = textViewCodeStation.getTag().toString();
                String codeName = textViewCodeStation.getText().toString();
                String noteStation = textViewNoteStation.getText().toString();

                setCountOnePlus();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("code", codeStation);
                intent.putExtra("newNameStation", codeName + " " + noteStation);
                intent.putExtra("day", day);
                // Google Analytics
                Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
                t.send(new HitBuilders.EventBuilder()
                        .setCategory(getString(R.string.analytics_category_listview))
                        .setAction(getString(R.string.analytics_action_listview_info))
                        .build());

                startActivity(intent);
                finish();
            }
        });
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
                                Intent intentMain = new Intent(InfoActivity.this, MainActivity.class);
                                startActivity(intentMain);
                                finish();
                                return true;
                            case 2:
                                setCountOnePlus();
                                Intent intentArrival = new Intent(InfoActivity.this, ArrivalActivity.class);
                                startActivity(intentArrival);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 4:
                                setCountOnePlus();
                                Intent intentEtraffic = new Intent(InfoActivity.this, EtrafficActivity.class);
                                startActivity(intentEtraffic);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 6:
                                setCountOnePlus();
                                Intent intentEtrafficMain = new Intent(InfoActivity.this, EtrafficMainActivity.class);
                                startActivity(intentEtrafficMain);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 8:
                                setCountOnePlus();
                                Intent intentMenu = new Intent(InfoActivity.this, MenuActivity.class);
                                startActivity(intentMenu);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 9:
                                setCountOnePlus();
                                Intent intentAbout = new Intent(InfoActivity.this, AboutActivity.class);
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
    private void loadRouteInfo(Object... params) throws UnsupportedEncodingException {
        String query = Uri.encode(params[0].toString());
        String url = "http://www.avtovokzal.org/php/app/info.php?number="+query+"&time="+params[1];

        if (isOnline()) {
            progressDialog = new ProgressDialog(InfoActivity.this);
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
                    processingLoadRouteInfo task = new processingLoadRouteInfo();
                    task.execute(response);
                 }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(LOG_ON) VolleyLog.d(TAG, "Error: " + error.getMessage());
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
        intent.putExtra("time", time);
        intent.putExtra("numberToView", numberToView);
        intent.putExtra("name", name);
        intent.putExtra("day", day);
        startActivity(intent);
        finish();
    }

    private class processingLoadRouteInfo extends AsyncTask<String, Void, List<RouteObjectInfo>>{
        @Override
        protected List<RouteObjectInfo> doInBackground(String... response) {
            JSONObject dataJsonQbj;
            List<RouteObjectInfo> list = new ArrayList<>();

            if(LOG_ON) Log.v(TAG, response[0]);

            try {
                dataJsonQbj = new JSONObject(response[0]);
                JSONArray rasp = dataJsonQbj.getJSONArray("info");

                for (int i = 0; i < rasp.length(); i++) {
                    JSONObject oneObject = rasp.getJSONObject(i);

                    String timePrib = oneObject.getString("time_prib");
                    String timeWay = oneObject.getString("time_way");
                    String nameStation = oneObject.getString("name_station");
                    String noteStation = oneObject.getString("note_station");
                    long codeStation = oneObject.getLong("id_station");
                    String priceBus = oneObject.getString("price_data");
                    String baggageBus = oneObject.getString("baggage_data");
                    String distanceData = oneObject.getString("distance_data");
                    list.add(new RouteObjectInfo(timePrib, timeWay, nameStation, noteStation, codeStation, priceBus, baggageBus, distanceData));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<RouteObjectInfo> list) {
            final RouteObjectInfoAdapter adapter = new RouteObjectInfoAdapter(InfoActivity.this, list);
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
