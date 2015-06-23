package com.www.avtovokzal.org;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.google.android.gms.analytics.Tracker;
import com.www.avtovokzal.org.Adapter.ArrivalObjectResultAdapter;
import com.www.avtovokzal.org.Adapter.AutocompleteCustomArrayAdapter;
import com.www.avtovokzal.org.Listener.ArrivalAutoCompleteTextChangedListener;
import com.www.avtovokzal.org.Object.ArrivalObjectResult;
import com.www.avtovokzal.org.Object.AutoCompleteObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ArrivalActivity extends ActionBarActivity {

    Menu myMenu;

    public CustomAutoCompleteView myAutoComplete;
    public ArrayAdapter<AutoCompleteObject> myAdapter;
    public DatabaseHandler databaseH;

    private ListView listView;
    private AdView adView;
    private SharedPreferences settings;
    private ProgressDialog progressDialog;

    private String code;
    private String newNameStation;

    private final static boolean LOG_ON = false;
    private final static boolean DEVELOPER = false;

    private final static String TAG = "ArrivalActivity";

    public static final String APP_PREFERENCES_ADS_SHOW = "adsDisable";
    public static final String APP_PREFERENCES = "settings";
    public static final String APP_PREFERENCES_DEFAULT = "default";
    public static final String APP_PREFERENCES_STATION_NAME = "station_name";
    public static final String APP_PREFERENCES_STATION_CODE = "station_code";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrival);

        boolean AdShowGone;
        boolean defaultStation;
        databaseH = new DatabaseHandler(ArrivalActivity.this);

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
            adView.setAdUnitId(getString(R.string.admob_main_activity));
            adView.setAdSize(AdSize.SMART_BANNER);

            // Поиск разметки LinearLayout
            LinearLayout layout = (LinearLayout)findViewById(R.id.adViewArrivalActivity);

            // Добавление в разметку экземпляра adView
            layout.addView(adView);

            // Инициирование общего запроса
            AdRequest request = new AdRequest.Builder().build();

            // Загрузка adView с объявлением
            adView.loadAd(request);
        }

        // Определяем элементы интерфейса
        listView = (ListView) findViewById(R.id.listViewArrival);
        myAutoComplete = (CustomAutoCompleteView) findViewById(R.id.autoCompleteArrival);

        // Проверка статуса сервера
        getServerStatus();

        // Получаем переменные
        code = getIntent().getStringExtra("code");
        newNameStation = getIntent().getStringExtra("newNameStation");

        if (newNameStation != null) {
            myAutoComplete.setText(newNameStation);
        }

        if (LOG_ON) Log.v("newNameStation", "" + newNameStation);

        defaultStation = getSettingsParams(APP_PREFERENCES_DEFAULT);

        if (defaultStation && settings.contains(APP_PREFERENCES_STATION_CODE) && settings.contains(APP_PREFERENCES_STATION_NAME)) {
            code = settings.getString(APP_PREFERENCES_STATION_CODE, null);
            myAutoComplete.setText(settings.getString(APP_PREFERENCES_STATION_NAME, null));

            loadArrivalResult(code);
        } else if (code != null) {
            loadArrivalResult(code);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String number;
                String name;
                String timePrib;
                String timeFromStation;

                RelativeLayout rl = (RelativeLayout) view;
                TextView textViewNumber = (TextView) rl.getChildAt(1);
                TextView textViewNameMarsh = (TextView) rl.getChildAt(2);
                TextView textViewTimePrib = (TextView) rl.getChildAt(4);

                number= textViewNumber.getText().toString();
                name = textViewNameMarsh.getText().toString();
                timePrib = textViewTimePrib.getText().toString();
                timeFromStation = textViewTimePrib.getTag().toString();

                Intent intent = new Intent(getApplicationContext(), InfoArrivalActivity.class);

                intent.putExtra("number", number);
                intent.putExtra("name", name);
                intent.putExtra("timePrib", timePrib);
                intent.putExtra("timeFromStation", timeFromStation);

                startActivity(intent);
            }
        });

        myAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View agr1, int pos, long id) {
                RelativeLayout rl = (RelativeLayout) agr1;
                TextView tv = (TextView) rl.getChildAt(0);
                myAutoComplete.setText(tv.getText().toString());

                // Програмное скрытие клавиатуры
                InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(myAutoComplete.getWindowToken(), 0);

                code = tv.getTag().toString();

                // Запрос и отображение результатов поиска
                loadArrivalResult(code);

                myAutoComplete.clearFocus();
            }
        });

        // При получении фокуса полем AutoComplete стираем ранее введенный текст
        myAutoComplete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean bool) {
                if(bool) {
                    myAutoComplete.setText("");
                }
            }
        });

        myAutoComplete.addTextChangedListener(new ArrivalAutoCompleteTextChangedListener(this));
        AutoCompleteObject[] ObjectItemData = new AutoCompleteObject[0];
        myAdapter = new AutocompleteCustomArrayAdapter(this, R.layout.listview_dropdown_item, ObjectItemData);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        newNameStation = getIntent().getStringExtra("newNameStation");
        code = getIntent().getStringExtra("code");

        if (code != null){
            loadArrivalResult(code);
            if (newNameStation != null) {
                myAutoComplete.setText(newNameStation);
            }
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
        MenuItem itemLamp = myMenu.findItem(R.id.lamp);
        itemLamp.setVisible(false);
        // Скрываем расписание прибытия
        MenuItem itemArrival = myMenu.findItem(R.id.arrival);
        itemArrival.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(ArrivalActivity.this, MenuActivity.class);
                startActivity(intent);
                return true;
            case R.id.lamp:
                Toast.makeText(getApplicationContext(), getString(R.string.main_status), Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Загрузка расписания прибытия
    private void loadArrivalResult(Object... params){
        String url = "http://www.avtovokzal.org/php/app/arrival.php?id="+params[0];

        if (isOnline()) {
            progressDialog = new ProgressDialog(ArrivalActivity.this);
            progressDialog.setMessage(getString(R.string.info_load_data));
            progressDialog.setCancelable(false);
            progressDialog.show();

            StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject dataJsonQbj;
                    List<ArrivalObjectResult> list = new ArrayList<>();

                    if (response == null) {
                        callErrorActivity();
                        finish();
                    }

                    if(LOG_ON){Log.d(TAG, response);}

                    try {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }  finally {
                        progressDialog = null;
                    }

                    try {
                        dataJsonQbj = new JSONObject(response);
                        JSONArray rasp = dataJsonQbj.getJSONArray("arrival");

                        if (rasp.length() == 0) {
                                TextView textView1 = (TextView) findViewById(R.id.noArrivalItems);
                                textView1.setVisibility(View.VISIBLE);
                        } else {
                                TextView textView1 = (TextView) findViewById(R.id.noArrivalItems);
                                textView1.setVisibility(View.GONE);
                        }

                        for (int i = 0; i < rasp.length(); i++) {
                            JSONObject oneObject = rasp.getJSONObject(i);

                            String timeOtpr = oneObject.getString("time_otpr");
                            String timePrib = oneObject.getString("time_prib");
                            String timeFromStation = oneObject.getString("time_nt_reverse_schedule");
                            String numberMarsh = oneObject.getString("number_reverse_schedule");
                            String nameMarsh = oneObject.getString("name_reverse_schedule");
                            String scheduleMarsh = oneObject.getString("day_reverse_schedule");
                            list.add(new ArrivalObjectResult(timeOtpr, timePrib, timeFromStation, numberMarsh, nameMarsh, scheduleMarsh));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    ArrivalObjectResultAdapter adapter = new ArrivalObjectResultAdapter(ArrivalActivity.this, list);
                    listView.setAdapter(adapter);
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

    private void callErrorActivity(){
        Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
        intent.putExtra("activity", TAG);
        if (code != null) {
            intent.putExtra("code", code);
            intent.putExtra("newNameStation", newNameStation);
        }
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
}
