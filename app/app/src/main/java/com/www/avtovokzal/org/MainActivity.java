package com.www.avtovokzal.org;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.www.avtovokzal.org.Adapter.AutocompleteCustomArrayAdapter;
import com.www.avtovokzal.org.Adapter.RouteObjectResultAdapter;
import com.www.avtovokzal.org.Adapter.StationObjectAdapter;
import com.www.avtovokzal.org.Billing.IabHelper;
import com.www.avtovokzal.org.Billing.IabResult;
import com.www.avtovokzal.org.Billing.Inventory;
import com.www.avtovokzal.org.Billing.Purchase;
import com.www.avtovokzal.org.Listener.CustomAutoCompleteTextChangedListener;
import com.www.avtovokzal.org.Object.AutoCompleteObject;
import com.www.avtovokzal.org.Object.RouteObjectResult;
import com.www.avtovokzal.org.Object.StationsObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends ActionBarActivity implements DatePickerDialog.OnDateSetListener {

    public CustomAutoCompleteView myAutoComplete;
    public ArrayAdapter<AutoCompleteObject> myAdapter;
    public DatabaseHandler databaseH;

    private ListView listView;
    private TextView textView;
    private AdView adView;
    private InterstitialAd interstitial;
    private SharedPreferences settings;
    private ProgressDialog queryDialog;

    private String code;
    private String dateNow;
    private String number;
    private String time;
    private String md5hash;
    private String nameStation;
    private int day = 0;
    private boolean cancel;
    private boolean load;
    private boolean sell;
    private boolean AdShowGone;
    private boolean checkMD5;

    private static final String TAG = "MainActivity";
    private static final String SKU_ADS_DISABLE = "com.www.avtovokzal.org.ads.disable";

    public static final String APP_PREFERENCES_MD5 = "md5";

    Menu myMenu;
    IabHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean defaultStation;
        Button btnDate;
        Button btnNextDay;
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv5XXw+M1Yp9Nz7EbiKEBrknpsTRGV2NKZU8e6EMB3C0BvgiKvDiCQTqYJasfPj/ICsJ+oAfYMlJRS1y5V/fpOWYJCHr0vr7r+cgnd7GqKk5DMIxRe8hKMppqYDdTjW4oPuoS/qhH5mVapZWyOWh/kl4ZshAAmxnk9eRRA9W5zUz62jzAu30lwbr66YpwKulYYQw3wcOoBQcm9bYXMK4SEJKfkiZ7btYS1iDq1pshm9F5dW3E067JYdf4Sdxg9kLpVtOh9FqvHCrXai0stTf+0wLlBLOogNzPG9Gj7z2TVaZIdCWJKqZ97XP/Ur8kGBNaqDLCBSzm6IL+hsE5bzbmlQIDAQAB";
        databaseH = new DatabaseHandler(MainActivity.this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withDisplayBelowToolbar(true)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(R.string.app_subtitle_main)
                                .withIdentifier(1),
                        new SecondaryDrawerItem()
                                .withName(R.string.app_subtitle_arrival)
                                .withIdentifier(2),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem()
                                .withName(R.string.menu_settings)
                                .withIdentifier(3)
                )
                .build();


        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);

        // Создание helper, передавая ему наш контекст и открытый ключ для проверки подписи
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        // Включаем логирование в debug режиме (перед публикацией поставить false)
        mHelper.enableDebugLogging(false);

        // Инициализируем. Запрос асинхронен будет вызван, когда инициализация завершится
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    if(LOG_ON) {Log.v(TAG, "Ошибка создания в приложении биллинга: " + result);}
                    return;
                }
                if (mHelper == null) return;
                // Проверка уже купленного.
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });

        // Переменная отвечает за работу с настройками
        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        // Проверка отключения рекламы
        AdShowGone = settings.contains(APP_PREFERENCES_ADS_SHOW) && settings.getBoolean(APP_PREFERENCES_ADS_SHOW, false);

        if (DEVELOPER) {
            AdShowGone = true;
        }

        // Реклама в приложении
        if (!AdShowGone) {
            // Создание межстраничного объявления
            interstitial = new InterstitialAd(this);
            interstitial.setAdUnitId(getString(R.string.admob_interstitial));

            // Создание запроса объявления.
            AdRequest adRequest = new AdRequest.Builder().build();

            // Запуск загрузки межстраничного объявления
            interstitial.loadAd(adRequest);

            // Создание экземпляра adView
            adView = new AdView(this);
            adView.setAdUnitId(getString(R.string.admob_main_activity));
            adView.setAdSize(AdSize.SMART_BANNER);

            // Поиск разметки LinearLayout
            LinearLayout layout = (LinearLayout)findViewById(R.id.adViewMainActivity);

            // Добавление в разметку экземпляра adView
            layout.addView(adView);

            // Инициирование общего запроса
            AdRequest request = new AdRequest.Builder().build();

            // Загрузка adView с объявлением
            adView.loadAd(request);
        }

        // Проверка есть ли изменения в списке остановок
        if(!checkMD5Hash()){
            loadStationToDB();
        }

        // Проверка статуса сервера
        getServerStatus();

        // Установка текущей даты
        getDateNow();

        // Определяем элементы интерфейса
        myAutoComplete = (CustomAutoCompleteView) findViewById(R.id.autoCompleteMain);
        listView = (ListView) findViewById(R.id.listViewMain);

        // Добавляем футер к списку ListView
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.listview_footer, listView, false);
        listView.addFooterView(footer, null, false);

        // Загрузка сохраненных настроек приложения
        cancel = getSettingsParams(APP_PREFERENCES_CANCEL);
        sell = getSettingsParams(APP_PREFERENCES_SELL);
        load = getSettingsParams(APP_PREFERENCES_LOAD);
        defaultStation = getSettingsParams(APP_PREFERENCES_DEFAULT);

        if (defaultStation && settings.contains(APP_PREFERENCES_STATION_CODE) && settings.contains(APP_PREFERENCES_STATION_NAME) && !load) {
            code = settings.getString(APP_PREFERENCES_STATION_CODE, null);
            nameStation = settings.getString(APP_PREFERENCES_STATION_NAME, null);
            myAutoComplete.setText(nameStation);

            loadScheduleResult(code, day, cancel, sell);
        } else if (!load && !defaultStation)  {
            loadSchedule(day, cancel, sell);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name;
                String numberToView;

                RelativeLayout rl = (RelativeLayout) view;
                TextView textViewTime = (TextView) rl.getChildAt(0);
                TextView textViewNumber = (TextView) rl.getChildAt(1);
                TextView textViewName = (TextView) rl.getChildAt(2);

                time = textViewTime.getText().toString();
                number = textViewNumber.getTag().toString();
                numberToView = textViewNumber.getText().toString();
                name = textViewName.getText().toString();

                Intent intent = new Intent(getApplicationContext(), InfoActivity.class);

                intent.putExtra("number", number);
                intent.putExtra("numberToView", numberToView);
                intent.putExtra("time", time);
                intent.putExtra("name", name);
                intent.putExtra("day", day);

                startActivity(intent);
            }
        });


        myAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View agr1, int pos, long id) {
                RelativeLayout rl = (RelativeLayout) agr1;
                TextView tv = (TextView) rl.getChildAt(0);
                nameStation = tv.getText().toString();
                myAutoComplete.setText(nameStation);

                // Програмное скрытие клавиатуры
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(myAutoComplete.getWindowToken(), 0);

                code = tv.getTag().toString();

                // Запрос и отображение результатов поиска
                loadScheduleResult(code, day, cancel, sell);

                myAutoComplete.clearFocus();
            }
        });

        // При получении фокуса полем AutoComplete стираем ранее введенный текст
        myAutoComplete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean bool) {
                if (bool) {
                    myAutoComplete.setText("");
                }
            }
        });

        myAutoComplete.addTextChangedListener(new CustomAutoCompleteTextChangedListener(this));
        AutoCompleteObject[] ObjectItemData = new AutoCompleteObject[0];
        myAdapter = new AutocompleteCustomArrayAdapter(this, R.layout.listview_dropdown_item, ObjectItemData);

        // Floating Action Button
        findViewById(R.id.fab).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intentArrival = new Intent(MainActivity.this, ArrivalActivity.class);
                        if (code != null) {
                            intentArrival.putExtra("code", code);
                            intentArrival.putExtra("newNameStation", nameStation);
                        }
                        startActivity(intentArrival);
                    }
                }
        );

        // Отменяем преобразование текста кнопок в AllCaps програмно
        btnDate = (Button) findViewById(R.id.header);
        btnNextDay = (Button) findViewById(R.id.buttonNextDay);
        btnDate.setTransformationMethod(null);
        btnNextDay.setTransformationMethod(null);

//        changeTitleActionBar();
    }

    private void changeTitleActionBar() {
        // Изменение текста подстроки ActionBar
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle(getString(R.string.app_subtitle));
        ab.setSubtitle(getString(R.string.app_subtitle_main));
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
        if (!AdShowGone) {
            if (!getSettingsParams(APP_PREFERENCES_ADS_SHOW)) {
                if (interstitial.isLoaded()) {
                    interstitial.show();
                }
            }
        }
        if (adView != null) {
            adView.destroy();
        }
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
        try {
            if (queryDialog != null && queryDialog.isShowing()) {
                queryDialog.dismiss();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }  finally {
            queryDialog = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        String newNameStation = getIntent().getStringExtra("newNameStation");
        code = getIntent().getStringExtra("code");
        cancel = getIntent().getBooleanExtra("cancel", getSettingsParams(APP_PREFERENCES_CANCEL));
        sell = getIntent().getBooleanExtra("sell", getSettingsParams(APP_PREFERENCES_SELL));
        day = getIntent().getIntExtra("day", 0);
        load = getIntent().getBooleanExtra("cancel_load", getSettingsParams(APP_PREFERENCES_LOAD));

        if (code != null){
            loadScheduleResult(code, day, cancel, sell);
            if (newNameStation != null) {
                myAutoComplete.setText(newNameStation);
            }
        } else if (!load){
            loadSchedule(day, cancel, sell);
        }
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
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                intent.putExtra("day", day);
                intent.putExtra("activity", "MainActivity");
                if (code != null) {
                    intent.putExtra("code", code);
                }
                startActivity(intent);
                return true;
            case R.id.lamp:
                Toast.makeText(getApplicationContext(), getString(R.string.main_status), Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Загрузка остановок в базу данных
    private void loadStationToDB() {
        String url = "http://www.avtovokzal.org/php/app/station.php";

        if (isOnline()) {
            StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response == null) {
                        callErrorActivity();
                        finish();
                    }
                    // Отключаем вожможность ввода до загрузки остановок в базу данных
                    myAutoComplete.setEnabled(false);

                    processingLoadStationToDB task = new processingLoadStationToDB();
                    task.execute(response);
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

    // Загрузка расписания автобусов
    private void loadSchedule (Object... params) {
        String url = "http://www.avtovokzal.org/php/app/main.php?day="+params[0]+"&cancel="+params[1]+"&sell="+params[2];

        if (isOnline()) {
            queryDialog = new ProgressDialog(MainActivity.this);
            queryDialog.setMessage(getString(R.string.main_load));
            queryDialog.setCancelable(false);
            queryDialog.show();

            StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response == null) {
                        callErrorActivity();
                        finish();
                    }
                    processingLoadSchedule task = new processingLoadSchedule();
                    task.execute(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(LOG_ON) {VolleyLog.d(TAG, "Error: " + error.getMessage());}
                    try {
                        if (queryDialog != null && queryDialog.isShowing()) {
                            queryDialog.dismiss();
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }  finally {
                        queryDialog = null;
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

    // Загрузка результатов запроса расписания
    private void loadScheduleResult (Object... params) {
        String url = "http://www.avtovokzal.org/php/app/result.php?id="+params[0]+"&day="+params[1]+"&cancel="+params[2]+"&sell="+params[3];

        if (isOnline()) {
            queryDialog = new ProgressDialog(MainActivity.this);
            queryDialog.setMessage(getString(R.string.main_load));
            queryDialog.setCancelable(false);
            queryDialog.show();

            StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response == null) {
                        callErrorActivity();
                        finish();
                    }
                    processingLoadScheduleResult task = new processingLoadScheduleResult();
                    task.execute(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(LOG_ON) {VolleyLog.d(TAG, "Error: " + error.getMessage());}
                    try {
                        if (queryDialog != null && queryDialog.isShowing()) {
                            queryDialog.dismiss();
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }  finally {
                        queryDialog = null;
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

    // Проверка требуется ли обновлять список остановок
    private boolean checkMD5Hash() {
        String url = "http://www.avtovokzal.org/php/app/md5stations.php";

        if (isOnline()) {
            StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    String md5hashFromSettings;

                    if (response == null) {
                        callErrorActivity();
                        finish();
                    }

                    if(LOG_ON){Log.d(TAG, response);}

                    md5hash = response != null ? response.trim() : null;

                    if (settings.contains(APP_PREFERENCES_MD5)) {
                        md5hashFromSettings = settings.getString(APP_PREFERENCES_MD5, null);
                    } else {
                        md5hashFromSettings = "";
                    }

                    if (md5hash.equals(md5hashFromSettings)) {
                        checkMD5 = true;
                    } else {
                        // Сохраняем значение нового md5 хэша остановок в настройках
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(APP_PREFERENCES_MD5, md5hash);
                        editor.apply();
                        checkMD5 = false;
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
        return checkMD5;
    }

    // Запрос текущей даты
    private void getDateNow() {
        String url = "http://www.avtovokzal.org/php/app/getDate.php";

        if (isOnline()) {
            StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response == null) {
                        callErrorActivity();
                        finish();
                    }

                    if(LOG_ON){Log.d(TAG, response);}

                    dateNow = response;
                    dateNow = dateNow != null ? dateNow.substring(1) : null;
                    dateNow = dateNow != null ? dateNow.trim() : null;

                    textView = (TextView) findViewById(R.id.header);
                    textView.setText(getString(R.string.main_schedule) + " " + dateNow);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    callErrorActivity();
                    finish();
                }
            });
            // Установливаем TimeOut, Retry
            strReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Добавляем запрос в очередь
            AppController.getInstance().addToRequestQueue(strReq);
        } else {
            callErrorActivity();
            finish();
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
                    finish();
                }
            });
            // Установливаем TimeOut, Retry
            strReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Добавляем запрос в очередь
            AppController.getInstance().addToRequestQueue(strReq);
        } else {
            callErrorActivity();
            finish();
        }
    }

    // Расписание на следующий день
    public void onClickNextDay(View view) {
        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.analytics_category_button))
                .setAction(getString(R.string.analytics_action_next_day))
                .build());

        String dateNew;

        day = day + 1;

        if (day >= 0 && day <= 9) {
            dateNew = dateNow;

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyy", java.util.Locale.getDefault());
            Calendar c = Calendar.getInstance();
            try {
                c.setTime(sdf.parse(dateNew));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            c.add(Calendar.DATE, day);
            dateNew = sdf.format(c.getTime());

            textView.setText(getString(R.string.main_schedule) + " " + dateNew);

            if (code != null) {
                loadScheduleResult(code, day, cancel, sell);
            } else {
                loadSchedule(day, cancel, sell);
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.main_invalid_date) , Toast.LENGTH_SHORT).show();
        }
    }

    // Проверка есть ли сетевое подключение
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayDatePicker) {
        String monthNumber;
        String dayNumber;

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyy", java.util.Locale.getDefault());

        Calendar calendarNow = Calendar.getInstance();
        try {
            calendarNow.setTime(sdf.parse(dateNow));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar calendarNew = Calendar.getInstance();
        try {
            calendarNew.setTime(sdf.parse(dayDatePicker + "." + (month + 1) + "." + year));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long diff = calendarNew.getTimeInMillis() - calendarNow.getTimeInMillis();
        int days = (int)(diff / (24 * 60 * 60 * 1000));

        day = 0;
        day = day + days;

        if (month < 10) {
            monthNumber = "0" + (month + 1);
        } else {
            monthNumber = "" + (month + 1);
        }

        if (dayDatePicker < 10) {
            dayNumber = "0" + dayDatePicker;
        } else {
            dayNumber = "" + dayDatePicker;
        }

        if (code != null && days >= 0 && days <= 9) {
            loadScheduleResult(code, day, cancel, sell);
            textView.setText(getString(R.string.main_schedule)+ " " + dayNumber + "." + monthNumber + "." + year);
            try {
                if (queryDialog != null && queryDialog.isShowing()) {
                    queryDialog.dismiss();
                    if(LOG_ON){Log.d(TAG, "Dialog.dismiss");}
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }  finally {
                queryDialog = null;
            }
        } else if (days >= 0 && days <= 9) {
            loadSchedule(day, cancel, sell);
            textView.setText(getString(R.string.main_schedule)+ " " + dayNumber + "." + monthNumber + "." + year);
            try {
                if (queryDialog != null && queryDialog.isShowing()) {
                    queryDialog.dismiss();
                    if(LOG_ON){Log.d(TAG, "Dialog.dismiss");}
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }  finally {
                queryDialog = null;
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.main_invalid_date) , Toast.LENGTH_SHORT).show();
        }
    }

    // Показать диалог выбора даты
    public void showDatePickerDialog(View v) {
        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.analytics_category_button))
                .setAction(getString(R.string.analytics_action_set_date))
                .build());

        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    // Вызов Error Activity
    private void callErrorActivity(){
        Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
        intent.putExtra("activity", TAG);
        intent.putExtra("code", code);
        intent.putExtra("day", day);
        intent.putExtra("cancel", cancel);
        intent.putExtra("sell", sell);
        startActivity(intent);
    }

    // Получаем параметры из файла настроек
    private boolean getSettingsParams(String params) {
        boolean checkBoxValue;
        checkBoxValue = settings.contains(params) && settings.getBoolean(params, false);
        return checkBoxValue;
    }

    // Listener для востановителя покупок.
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null) return;

            if (result.isFailure()) {
                if(LOG_ON) {Log.v(TAG, "Failed to query inventory: " + result);}
                return;
            }

            // Проверка отключена ли реклама в приложении
            Purchase purchase = inventory.getPurchase(SKU_ADS_DISABLE);

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(APP_PREFERENCES_ADS_SHOW, (purchase != null && verifyDeveloperPayload(purchase)));
            editor.apply();

            // Отключаем рекламу если покупка была совершена ранее
            if (purchase != null && verifyDeveloperPayload(purchase) && adView != null) {
                adView.setVisibility(View.GONE);
                AdShowGone = true;
            }
        }
    };

    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        return true;
    }

    private class processingLoadSchedule extends AsyncTask<String, Void, List<StationsObject>> {
        @Override
        protected List<StationsObject> doInBackground(String... response) {
            JSONObject dataJsonQbj;
            List<StationsObject> list = new ArrayList<>();

            if (LOG_ON) Log.v("Result", response[0]);

            try {
                dataJsonQbj = new JSONObject(response[0]);
                JSONArray rasp = dataJsonQbj.getJSONArray("rasp");

                if(LOG_ON) Log.v("Length", " " + rasp.length());

                if (rasp.length() == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView1 = (TextView) findViewById(R.id.noItems);
                            textView1.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView1 = (TextView) findViewById(R.id.noItems);
                            textView1.setVisibility(View.GONE);
                        }
                    });
                }
                for (int i = 0; i < rasp.length(); i++) {
                    JSONObject oneObject = rasp.getJSONObject(i);

                    String timeMarsh = oneObject.getString("time_otpr");
                    String numberMarsh = oneObject.getString("new_number_data");
                    String nameMarsh = oneObject.getString("name_data");
                    String nameBus = oneObject.getString("name_bus");
                    String countBus = oneObject.getString("count_bus");
                    String freeBus = oneObject.getString("free_place_new_schedule");
                    int cancelBus = oneObject.getInt("cancel_new_schedule");
                    String numberMarshToSend = oneObject.getString("number_new_schedule");
                    list.add(new StationsObject(timeMarsh, numberMarsh, numberMarshToSend, nameMarsh, nameBus, countBus, freeBus, cancelBus));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<StationsObject> list) {
            final StationObjectAdapter adapter = new StationObjectAdapter(MainActivity.this, list);
            listView.setAdapter(adapter);
            super.onPostExecute(list);

            try {
                if (queryDialog != null && queryDialog.isShowing()) {
                    queryDialog.dismiss();
                    if(LOG_ON){Log.d(TAG, "Dialog.dismiss");}
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }  finally {
                queryDialog = null;
            }
        }
    }

    private class processingLoadStationToDB extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... response) {
            JSONObject dataJsonQbj;

            if(LOG_ON){Log.d(TAG, response[0]);}

            try {
                dataJsonQbj = new JSONObject(response[0]);
                JSONArray rasp = dataJsonQbj.getJSONArray("station");

                for (int i = 0; i < rasp.length(); i++) {
                    JSONObject oneObject = rasp.getJSONObject(i);

                    String nameStation = oneObject.getString("name_station");
                    String noteStation = oneObject.getString("note_station");
                    long codeStation = oneObject.getLong("id_station");
                    long sumStation = oneObject.getLong("sum_station");
                    databaseH.create(new AutoCompleteObject((nameStation + " " + noteStation), sumStation, codeStation));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Включаем возможность ввода после загрузки остановок в базу данных
            myAutoComplete.setEnabled(true);
        }
    }

    private class processingLoadScheduleResult extends AsyncTask<String, Void, List<RouteObjectResult>> {
        @Override
        protected List<RouteObjectResult> doInBackground(String... response) {
            JSONObject dataJsonQbj;
            List<RouteObjectResult> list = new ArrayList<>();

            if(LOG_ON){Log.d(TAG, response[0]);}

            try {
                dataJsonQbj = new JSONObject(response[0]);
                JSONArray rasp = dataJsonQbj.getJSONArray("rasp");

                if (rasp.length() == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView1 = (TextView) findViewById(R.id.noItems);
                            textView1.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView1 = (TextView) findViewById(R.id.noItems);
                            textView1.setVisibility(View.GONE);
                        }
                    });
                }
                for (int i = 0; i < rasp.length(); i++) {
                    JSONObject oneObject = rasp.getJSONObject(i);

                    String timeMarsh = oneObject.getString("time_otpr");
                    String numberMarsh = oneObject.getString("new_number_data");
                    String numberMarshToSend = oneObject.getString("number_new_schedule");
                    String nameMarsh = oneObject.getString("name_data");
                    String nameBus = oneObject.getString("name_bus");
                    String countBus = oneObject.getString("count_bus");
                    String freeBus = oneObject.getString("free_place_new_schedule");
                    String priceBus = oneObject.getString("price_data");
                    String baggageBus = oneObject.getString("baggage_data");
                    String timePrib = oneObject.getString("time_prib_result");
                    int cancelBus = oneObject.getInt("cancel_new_schedule");
                    list.add(new RouteObjectResult(timeMarsh, numberMarsh, numberMarshToSend, nameMarsh, nameBus, countBus, freeBus, priceBus, baggageBus, timePrib, cancelBus));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<RouteObjectResult> list) {
            final RouteObjectResultAdapter adapterResult = new RouteObjectResultAdapter(MainActivity.this, list);
            listView.setAdapter(adapterResult);
            super.onPostExecute(list);

            try {
                if (queryDialog != null && queryDialog.isShowing()) {
                    queryDialog.dismiss();
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }  finally {
                queryDialog = null;
            }
        }
    }
}
