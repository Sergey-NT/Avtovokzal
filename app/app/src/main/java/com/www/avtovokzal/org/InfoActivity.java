package com.www.avtovokzal.org;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.www.avtovokzal.org.Adapter.RouteObjectInfoAdapter;
import com.www.avtovokzal.org.Object.RouteObjectInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class InfoActivity extends AppCompatSettingsActivity {

    private static final int LAYOUT = R.layout.activity_info;
    private final static String TAG = "InfoActivity";

    private ListView listView;
    private ProgressDialog progressDialog;

    private String number;
    private String time;
    private String numberToView;
    private String name;
    private int day;

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);

        // Определяем элементы интерфейса
        listView = (ListView) findViewById(R.id.listViewInfo);
        TextView textView = (TextView) findViewById(R.id.textViewInfoName);

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
        initToolbar(R.string.app_name, R.string.app_subtitle_info);
    }

    @SuppressWarnings("ConstantConditions")
    public void initToolbar(int title, int subtitle) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onDestroy() {
        queryDialogDismiss();
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
                    if(Constants.LOG_ON) VolleyLog.d(TAG, "Error: " + error.getMessage());
                    queryDialogDismiss();
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

            if(Constants.LOG_ON) Log.v(TAG, response[0]);

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

            queryDialogDismiss();
        }
    }

    private void queryDialogDismiss() {
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
