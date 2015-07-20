package com.www.avtovokzal.org;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.www.avtovokzal.org.Adapter.RouteObjectInfoAdapter;
import com.www.avtovokzal.org.Object.RouteObjectInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class InfoActivity extends ActionBarActivity {

    TextView textView;
    Menu myMenu;

    private SharedPreferences settings;
    private ListView listView;
    private AdView adView;
    private ProgressDialog progressDialog;

    private String number;
    private String time;
    private String numberToView;
    private String name;

    private int day;

    private final static String TAG = "InfoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        boolean AdShowGone;

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
            // Создание экземпляра adView
            adView = new AdView(this);
            adView.setAdUnitId(getString(R.string.admob_info_activity));
            adView.setAdSize(AdSize.SMART_BANNER);

            // Поиск разметки LinearLayout
            LinearLayout layout = (LinearLayout)findViewById(R.id.adViewInfoActivity);

            // Добавление в разметку экземпляра adView
            layout.addView(adView);

            // Инициирование общего запроса
            AdRequest request = new AdRequest.Builder().build();

            // Загрузка adView с объявлением
            adView.loadAd(request);
        }

        // Проверка статуса сервера
        getServerStatus();

        // Определяем элементы интерфейса
        listView = (ListView) findViewById(R.id.listViewInfo);
        textView = (TextView) findViewById(R.id.textViewInfoName);

        // Получаем переменные
        number = getIntent().getStringExtra("number");
        numberToView = getIntent().getStringExtra("numberToView");
        time = getIntent().getStringExtra("time");
        name = getIntent().getStringExtra("name");
        day = getIntent().getIntExtra("day", 0);

        textView.setText(time + " " + numberToView + " " + name);

        // Загружаем информацию об остановках на маршруте
        try {
            loadRouteInfo(number, time);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RelativeLayout relativeLayout = (RelativeLayout) view;
                TextView textViewCodeStation = (TextView) relativeLayout.getChildAt(0);
                TextView textViewNoteStation = (TextView) relativeLayout.getChildAt(2);

                String codeStation = textViewCodeStation.getTag().toString();
                String codeName = textViewCodeStation.getText().toString();
                String noteStation = textViewNoteStation.getText().toString();

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
        myMenu = menu;
        // Скрываем статус сервера
        MenuItem item = myMenu.findItem(R.id.lamp);
        item.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(InfoActivity.this, MenuActivity.class);
                startActivity(intent);
                return true;
            case R.id.lamp:
                Toast.makeText(getApplicationContext(), getString(R.string.main_status), Toast.LENGTH_LONG).show();
                return true;
            case R.id.arrival:
                Intent intentArrival = new Intent(InfoActivity.this, ArrivalActivity.class);
                startActivity(intentArrival);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Запрос информации об остановках на маршруте
    private void loadRouteInfo(Object... params) throws UnsupportedEncodingException {
        String query = Uri.encode(params[0].toString());
        String url = "http://www.avtovokzal.org/php/app/info.php?number="+query+"&time="+params[1];

        if (isOnline()) {
            progressDialog = new ProgressDialog(InfoActivity.this);
            progressDialog.setMessage(getString(R.string.info_load_data));
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
                    if(LOG_ON) {VolleyLog.d(TAG, "Error: " + error.getMessage());}
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

    // Проверка статуса сервера
    private void getServerStatus(){
        String url = "http://www.avtovokzal.org/php/app/status.php";

        if (isOnline()) {
            StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response == null || response.length() == 0) {
                        callErrorActivity();
                        finish();
                    }

                    if(LOG_ON){Log.d(TAG, response);}

                    assert response != null;
                    response = response.trim();

                    // Проверяем когда было последнее обновление расписания
                    try {
                        int delta = Integer.parseInt(response);

                        if (delta > 900) {
                            MenuItem item = myMenu.findItem(R.id.lamp);
                            item.setVisible(true);
                        }
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        MenuItem item = myMenu.findItem(R.id.lamp);
                        item.setVisible(true);
                        Toast.makeText(getApplicationContext(), getString(R.string.main_status_error), Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(LOG_ON) {VolleyLog.d(TAG, "Error: " + error.getMessage());}
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

    // Проверка подключения к сети интернет
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    // Получаем параметры из файла настроек
    private boolean getSettingsParams(String params) {
        boolean checkBoxValue;
        checkBoxValue = settings.contains(params) && settings.getBoolean(params, false);
        return checkBoxValue;
    }

    private class processingLoadRouteInfo extends AsyncTask<String, Void, List<RouteObjectInfo>>{
        @Override
        protected List<RouteObjectInfo> doInBackground(String... response) {
            JSONObject dataJsonQbj;
            List<RouteObjectInfo> list = new ArrayList<>();

            if(LOG_ON){Log.d(TAG, response[0]);}

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
