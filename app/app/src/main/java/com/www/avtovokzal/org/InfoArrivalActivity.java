package com.www.avtovokzal.org;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.www.avtovokzal.org.Adapter.RouteObjectInfoArrivalAdapter;
import com.www.avtovokzal.org.Object.RouteObjectInfoArrival;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InfoArrivalActivity extends AppCompatSettingsActivity {

    private static final int LAYOUT = R.layout.activity_info_arrival;
    private final static String TAG = "InfoArrivalActivity";

    private ListView listView;
    private ProgressDialog progressDialog;

    private String number;
    private String timePrib;
    private String timeFromStation;
    private String name;

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);

        // Определяем элементы интерфейса
        listView = (ListView) findViewById(R.id.listViewInfoArrival);
        TextView textViewName = (TextView) findViewById(R.id.textViewInfoArrivalName);
        TextView textViewTime = (TextView) findViewById(R.id.textViewInfoArrivalTimeToStation);

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
                TextView textViewNameStation = (TextView) relativeLayout.getChildAt(0);
                TextView textViewNoteStation = (TextView) relativeLayout.getChildAt(2);

                String codeStation = textViewNameStation.getTag().toString();
                String nameStation = textViewNameStation.getText().toString();
                String noteStation = textViewNoteStation.getText().toString();

                Intent intent = new Intent(getApplicationContext(), ArrivalActivity.class);
                intent.putExtra("newNameStation", nameStation + " " + noteStation);
                intent.putExtra("code", codeStation);

                if (Constants.LOG_ON) Log.v("newNameStation", "" + nameStation + " " + noteStation);
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
                    if(Constants.LOG_ON) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());}
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

            if(Constants.LOG_ON){Log.d(TAG, response[0]);}

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
