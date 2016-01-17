package com.www.avtovokzal.org;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
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

public class MainActivity extends AppCompatSettingsActivity implements DatePickerDialog.OnDateSetListener {

    public CustomAutoCompleteView myAutoComplete;
    public ArrayAdapter<AutoCompleteObject> myAdapter;
    public DatabaseHandler databaseH;

    private Button btnNextDay;
    private Drawer drawerResult = null;
    private ListView listView;
    private Menu toolbarMenu;
    private ProgressDialog queryDialog;
    private TextView textView;
    private FloatingActionButton fab;

    private String code;
    private String dateNow;
    private String number;
    private String time;
    private String nameStation;
    private int day = 0;
    private boolean cancel;
    private boolean load;
    private boolean sell;
    private boolean AdShowGone;
    private boolean update;
    private boolean all;

    private static final String TAG = "MainActivity";
    private static final String SKU_ADS_DISABLE = "com.www.avtovokzal.org.ads.disable";
    private static final String APP_PREFERENCES_MD5 = "md5";
    private static final String APP_PREFERENCES_MD5_EKB = "md5_ekb";
    private static final String APP_PREFERENCES_MD5_CHECK = "checkMD5";
    private static final String APP_PREFERENCES_MD5_EKB_CHECK = "checkMD5_ekb";
    private static final String APP_PREFERENCES_SEND_PHONE_INFORMATION = "send_phone_information";

    IabHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean defaultStation;
        boolean checkMD5;
        boolean checkMD5ekb;
        boolean sendPhoneInfo;
        Button btnDate;
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv5XXw+M1Yp9Nz7EbiKEBrknpsTRGV2NKZU8e6EMB3C0BvgiKvDiCQTqYJasfPj/ICsJ+oAfYMlJRS1y5V/fpOWYJCHr0vr7r+cgnd7GqKk5DMIxRe8hKMppqYDdTjW4oPuoS/qhH5mVapZWyOWh/kl4ZshAAmxnk9eRRA9W5zUz62jzAu30lwbr66YpwKulYYQw3wcOoBQcm9bYXMK4SEJKfkiZ7btYS1iDq1pshm9F5dW3E067JYdf4Sdxg9kLpVtOh9FqvHCrXai0stTf+0wLlBLOogNzPG9Gj7z2TVaZIdCWJKqZ97XP/Ur8kGBNaqDLCBSzm6IL+hsE5bzbmlQIDAQAB";
        databaseH = DatabaseHandler.getInstance(getApplicationContext());

        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);

        // Переменная отвечает за работу с настройками
        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        // Проверка отключения рекламы
        AdShowGone = settings.contains(APP_PREFERENCES_ADS_SHOW) && settings.getBoolean(APP_PREFERENCES_ADS_SHOW, false);

        // Check for Google Play Services
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int status = api.isGooglePlayServicesAvailable(getApplicationContext());
        if (status == ConnectionResult.SUCCESS && !AdShowGone) {
            // Создание helper, передавая ему наш контекст и открытый ключ для проверки подписи
            mHelper = new IabHelper(this, base64EncodedPublicKey);
            // Включаем логирование в debug режиме (перед публикацией поставить false)
            mHelper.enableDebugLogging(false);

            // Инициализируем. Запрос асинхронен будет вызван, когда инициализация завершится
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        if (LOG_ON) Log.v(TAG, "Ошибка создания в приложении биллинга: " + result);
                        return;
                    }
                    if (mHelper == null) return;
                    // Проверка уже купленного.
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                }
            });
        } else if (status != ConnectionResult.SUCCESS) {
            // Google Analytics
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.analytics_category_google))
                    .setAction(getString(R.string.analytics_action_google_result))
                    .build());
        }

        if (DEVELOPER) {
            AdShowGone = true;
        }

        // Реклама в приложении
        if (!AdShowGone) {
            initializeAd(R.id.adViewMainActivity);
        }

        loadSystemInfo();

        // Проверка есть ли строки в таблицах остановок
        if (!databaseH.checkIfExistsRowTable("stations")) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(APP_PREFERENCES_MD5_CHECK, false);
            editor.apply();
        }
        if (!databaseH.checkIfExistsRowTable("stations_ekb")) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(APP_PREFERENCES_MD5_EKB_CHECK, false);
            editor.apply();
        }

        // Проверка есть ли изменения в списке остановок
        checkMD5 = settings.getBoolean(APP_PREFERENCES_MD5_CHECK, false);
        checkMD5ekb = settings.getBoolean(APP_PREFERENCES_MD5_EKB_CHECK, false);

        if(LOG_ON) Log.v("Check MD5 Ekb", " " + checkMD5ekb);

        if(!checkMD5){
            if(LOG_ON) Log.v("checkMD5", "Load Station To DB");
            loadStationToDB();
        }

        if(!checkMD5ekb) {
            loadStationEkbToDB();
        }

        // Определяем элементы интерфейса
        myAutoComplete = (CustomAutoCompleteView) findViewById(R.id.autoCompleteMain);
        listView = (ListView) findViewById(R.id.listViewMain);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        // Добавляем футер к списку ListView
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.listview_footer, listView, false);
        listView.addFooterView(footer, null, false);

        // Загрузка сохраненных настроек приложения
        cancel = getSettingsParams(APP_PREFERENCES_CANCEL);
        sell = getSettingsParams(APP_PREFERENCES_SELL);
        load = getSettingsParams(APP_PREFERENCES_LOAD);
        all = getSettingsParams(APP_PREFERENCES_ALL);
        defaultStation = getSettingsParams(APP_PREFERENCES_DEFAULT);
        sendPhoneInfo = getSettingsParams(APP_PREFERENCES_SEND_PHONE_INFORMATION);

        if (defaultStation && settings.contains(APP_PREFERENCES_STATION_CODE) && settings.contains(APP_PREFERENCES_STATION_NAME) && !load) {
            code = settings.getString(APP_PREFERENCES_STATION_CODE, null);
            nameStation = settings.getString(APP_PREFERENCES_STATION_NAME, null);
            myAutoComplete.setText(nameStation);

            loadScheduleResult(code, day, cancel, sell, all);
        } else if (!load && !defaultStation)  {
            loadSchedule(day, cancel, sell);
        }

        listViewListener();
        myAutoCompleteListener();
        myAutoCompleteFocus();

        myAutoComplete.addTextChangedListener(new CustomAutoCompleteTextChangedListener(this));
        AutoCompleteObject[] ObjectItemData = new AutoCompleteObject[0];
        myAdapter = new AutocompleteCustomArrayAdapter(this, R.layout.listview_dropdown_item, ObjectItemData);

        // Отменяем преобразование текста кнопок в AllCaps програмно
        btnDate = (Button) findViewById(R.id.header);
        btnNextDay = (Button) findViewById(R.id.buttonNextDay);
        btnDate.setTransformationMethod(null);
        btnNextDay.setTransformationMethod(null);

        initializeToolbar(R.string.app_name_city, R.string.app_subtitle_main);
        initializeNavigationDrawer();
        initializeFloatingActionButton();
        checkAdSettings();

        if (!sendPhoneInfo) {
            sendPhoneInformationToServer();
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(APP_PREFERENCES_SEND_PHONE_INFORMATION, true);
            editor.apply();
        }
    }

    private void sendPhoneInformationToServer() {
        String version = null;
        PackageInfo packageInfo = null;

        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String manufacturer = Uri.encode(Build.MANUFACTURER);
        String model = Uri.encode(Build.MODEL);
        String device = Uri.encode(Build.DEVICE);
        String board = Uri.encode(Build.BOARD);
        String brand = Uri.encode(Build.BRAND);
        String display = Uri.encode(Build.DISPLAY);
        String id = Uri.encode(Build.ID);
        String product = Uri.encode(Build.PRODUCT);
        String release = Uri.encode(Build.VERSION.RELEASE);

        if (packageInfo != null) {
            version = Uri.encode(packageInfo.versionName);
        }

        if(LOG_ON) {
            Log.v(TAG, "1: " + manufacturer + " 2: " + model + " 3: " + device + " 4: " + board + " 5: " + brand + " 6: " + display + " 7: " + id + " 8: " + product + " 9: " + release + " 10: " + version);
        }

        String url = "http://www.avtovokzal.org/php/app/sendPhoneInformation.php?manufacturer="+manufacturer+"&model="+model+"&device="+device+"&board="+board+"&brand="+brand+"&display="+display+"&build_id="+id+"&product="+product+"&release_number="+release+"&version="+version;

        if (isOnline()) {
            StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {}
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(LOG_ON) VolleyLog.d(TAG, "Error: " + error.getMessage());
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

    private void myAutoCompleteFocus() {
        // При получении фокуса полем AutoComplete стираем ранее введенный текст
        myAutoComplete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean bool) {
                if (bool) {
                    myAutoComplete.setText("");
                }
            }
        });
    }

    private void myAutoCompleteListener() {
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
                loadScheduleResult(code, day, cancel, sell, all);

                myAutoComplete.clearFocus();
            }
        });

        myAutoComplete.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    int count = myAdapter.getCount();
                    if (count > 0) {
                        AutoCompleteObject object = myAdapter.getItem(0);
                        myAutoComplete.setText(object.toString());

                        code = object.getObjectCode();

                        // Програмное скрытие клавиатуры
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(myAutoComplete.getWindowToken(), 0);

                        // Запрос и отображение результатов поиска
                        loadScheduleResult(code, day, cancel, sell, all);

                        myAutoComplete.clearFocus();
                    }
                }
                return false;
            }
        });
    }

    private void listViewListener() {
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

                setCountOnePlus();

                Intent intent = new Intent(getApplicationContext(), InfoActivity.class);

                intent.putExtra("number", number);
                intent.putExtra("numberToView", numberToView);
                intent.putExtra("time", time);
                intent.putExtra("name", name);
                intent.putExtra("day", day);

                startActivity(intent);
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int def = totalItemCount - (firstVisibleItem + visibleItemCount);
                if (totalItemCount > 0 && def < 3 && fab.getVisibility() == View.VISIBLE) {
                    Animation fadeOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_out);
                    fab.setAnimation(fadeOutAnimation);
                    fab.setVisibility(View.GONE);
                } else if (totalItemCount > 0 && def > 3 && fab.getVisibility() != View.VISIBLE) {
                    Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
                    fab.setAnimation(fadeInAnimation);
                    fab.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initializeFloatingActionButton() {
        fab.setImageDrawable(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_vertical_align_bottom)
                .color(Color.WHITE));
        fab.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setCountOnePlus();
                        // Google Analytics
                        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
                        t.send(new HitBuilders.EventBuilder()
                                .setCategory(getString(R.string.analytics_category_button))
                                .setAction(getString(R.string.analytics_action_fab))
                                .build());
                        Intent intentArrival = new Intent(MainActivity.this, ArrivalActivity.class);
                        if (code != null) {
                            intentArrival.putExtra("code", code);
                            intentArrival.putExtra("newNameStation", nameStation);
                        }
                        startActivity(intentArrival);
                    }
                }
        );
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
                                drawerResult.closeDrawer();
                                return true;
                            case 2:
                                drawerResult.closeDrawer();
                                setCountOnePlus();
                                Intent intentArrival = new Intent(MainActivity.this, ArrivalActivity.class);
                                if (code != null) {
                                    intentArrival.putExtra("code", code);
                                    intentArrival.putExtra("newNameStation", nameStation);
                                }
                                startActivity(intentArrival);
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 4:
                                drawerResult.closeDrawer();
                                setCountOnePlus();
                                Intent intentEtraffic = new Intent(MainActivity.this, EtrafficActivity.class);
                                startActivity(intentEtraffic);
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 6:
                                drawerResult.closeDrawer();
                                setCountOnePlus();
                                Intent intentEtrafficMain = new Intent(MainActivity.this, EtrafficMainActivity.class);
                                startActivity(intentEtrafficMain);
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 8:
                                drawerResult.closeDrawer();
                                setCountOnePlus();
                                Intent intentMenu = new Intent(MainActivity.this, MenuActivity.class);
                                intentMenu.putExtra("day", day);
                                intentMenu.putExtra("activity", "MainActivity");
                                if (code != null) {
                                    intentMenu.putExtra("code", code);
                                }
                                startActivity(intentMenu);
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 9:
                                drawerResult.closeDrawer();
                                setCountOnePlus();
                                Intent intentAbout = new Intent(MainActivity.this, AboutActivity.class);
                                startActivity(intentAbout);
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                        }
                        return false;
                    }
                })
                .build();
        drawerResult.setSelection(1);
    }

    private void checkAdSettings () {
        if (settings.contains(APP_PREFERENCES_AD_DATE)) {
            String dateSettings = settings.getString(APP_PREFERENCES_AD_DATE, null);
            if (dateSettings != null) {
                if (dateSettings.length() > 3) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault());
                    Calendar calendar = Calendar.getInstance();
                    Calendar calendarSettings = Calendar.getInstance();
                    try {
                        calendarSettings.setTime(sdf.parse(dateSettings));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long diff = calendar.getTimeInMillis() - calendarSettings.getTimeInMillis();
                    int countDays = (int) (diff / (24 * 60 * 60 * 1000));
                    if(LOG_ON) Log.v(TAG, String.valueOf(countDays));
                    if (countDays > 6) {
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(APP_PREFERENCES_AD_DATE, null);
                        editor.putBoolean(APP_PREFERENCES_ADS_SHOW, false);
                        editor.apply();
                        if (adView == null) {
                            initializeAd(R.id.adViewMainActivity);
                        }
                    }
                }
            }
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
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
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
        if (adView != null && getSettingsParams(APP_PREFERENCES_ADS_SHOW)) {
            adView.setVisibility(View.GONE);
        }
        drawerResult.setSelection(1);
    }

    @Override
    protected void onDestroy() {
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
        all = getIntent().getBooleanExtra("all", getSettingsParams(APP_PREFERENCES_ALL));

        if (code != null){
            loadScheduleResult(code, day, cancel, sell, all);
            if (newNameStation != null) {
                myAutoComplete.setText(newNameStation);
            }
        } else if (!load){
            loadSchedule(day, cancel, sell);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        toolbarMenu = menu;
        getMenuInflater().inflate(R.menu.menu_items, menu);
        MenuItem item = toolbarMenu.findItem(R.id.lamp);
        item.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
                    btnNextDay.setEnabled(false);

                    processingLoadStationToDB task = new processingLoadStationToDB();
                    task.execute(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(LOG_ON) VolleyLog.d(TAG, "Error: " + error.getMessage());
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

    private void loadStationEkbToDB() {
        String url = "http://www.avtovokzal.org/php/app/station_ekb.php";

        if (isOnline()) {
            StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response == null) {
                        callErrorActivity();
                        finish();
                    }

                    processingLoadStationEkbToDB task = new processingLoadStationEkbToDB();
                    task.execute(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(LOG_ON) VolleyLog.d(TAG, "Error: " + error.getMessage());
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
                    if(LOG_ON) VolleyLog.d(TAG, "Error: " + error.getMessage());
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
        String url = "http://www.avtovokzal.org/php/app/result_1.3.8.php?id="+params[0]+"&day="+params[1]+"&cancel="+params[2]+"&sell="+params[3]+"&all="+params[4];

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
                    if(LOG_ON) VolleyLog.d(TAG, "Error: " + error.getMessage());
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

    // Запрос даты, хеша остановок, времени обновления
    private void loadSystemInfo() {
        String url = "http://www.avtovokzal.org/php/app/system_1.3.5_v1.php";

        if (isOnline()) {
            StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    String md5hashFromSettings;
                    String md5hashEkbFromSettings;

                    if (response == null) {
                        callErrorActivity();
                        finish();
                    }

                    JSONObject dataJsonQbj;

                    if (LOG_ON) Log.v("Result", response);

                    try {
                        dataJsonQbj = new JSONObject(response);
                        JSONArray system = dataJsonQbj.getJSONArray("system");

                        JSONObject oneObject = system.getJSONObject(0);

                        dateNow = oneObject.getString("date");
                        update = oneObject.getBoolean("update");
                        String md5hash = oneObject.getString("md5");
                        String md5hashEkb = oneObject.getString("md5_ekb");

                        // Показывааем статус сервера
                        if (!update && toolbarMenu != null) {
                            MenuItem item = toolbarMenu.findItem(R.id.lamp);
                            item.setVisible(true);
                        }

                        // Установка текущей даты
                        textView = (TextView) findViewById(R.id.header);
                        String string = getString(R.string.main_schedule) + " " + dateNow;
                        textView.setText(string);
                        SharedPreferences.Editor editorDate = settings.edit();
                        editorDate.putString(APP_PREFERENCES_DATE, dateNow);
                        editorDate.apply();

                        // Проверка требуется ли обновлять список остановок
                        md5hashFromSettings = settings.getString(APP_PREFERENCES_MD5, "");
                        md5hashEkbFromSettings = settings.getString(APP_PREFERENCES_MD5_EKB, "");

                        if (LOG_ON) {
                            Log.v("MD5 from Settings Ekb", md5hashEkbFromSettings + " " + md5hashEkb.equals(md5hashEkbFromSettings));
                            Log.v("MD5 Ekb", md5hashEkb);
                            Log.v("MD5 from Settings", md5hashFromSettings + " " + md5hash.equals(md5hashFromSettings));
                            Log.v("MD5", md5hash);
                        }

                        // Сохраняем значение нового md5 хэша остановок в настройках
                        if (!md5hash.equals(md5hashFromSettings)) {
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean(APP_PREFERENCES_MD5_CHECK, false);
                            editor.putString(APP_PREFERENCES_MD5, md5hash);
                            editor.apply();
                            if (LOG_ON) Log.v("Settings", "Station false");
                        }
                        if (!md5hashEkb.equals(md5hashEkbFromSettings)) {
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean(APP_PREFERENCES_MD5_EKB_CHECK, false);
                            editor.putString(APP_PREFERENCES_MD5_EKB, md5hashEkb);
                            editor.apply();
                            if (LOG_ON) Log.v("Settings", "Station_ekb false");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(LOG_ON) VolleyLog.d(TAG, "Error: " + error.getMessage());
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

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault());
            Calendar c = Calendar.getInstance();
            try {
                c.setTime(sdf.parse(dateNew));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            c.add(Calendar.DATE, day);
            dateNew = sdf.format(c.getTime());

            String string = getString(R.string.main_schedule) + " " + dateNew;
            textView.setText(string);

            if (code != null) {
                loadScheduleResult(code, day, cancel, sell, all);
            } else {
                loadSchedule(day, cancel, sell);
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.main_invalid_date) , Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayDatePicker) {
        if (dateNow != null) {
            String monthNumber;
            String dayNumber;

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault());

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
            int days = (int) (diff / (24 * 60 * 60 * 1000));

            day = 0;
            day = day + days;

            if (month < 9) {
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
                loadScheduleResult(code, day, cancel, sell, all);
                String string = getString(R.string.main_schedule) + " " + dayNumber + "." + monthNumber + "." + year;
                textView.setText(string);
                // fix for Android 4.4.4
                try {
                    if (queryDialog != null && queryDialog.isShowing()) {
                        queryDialog.dismiss();
                        if (LOG_ON) Log.v(TAG, "Dialog.dismiss");
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } finally {
                    queryDialog = null;
                }
            } else if (days >= 0 && days <= 9) {
                loadSchedule(day, cancel, sell);
                String string = getString(R.string.main_schedule) + " " + dayNumber + "." + monthNumber + "." + year;
                textView.setText(string);
                // fix for Android 4.4.4
                try {
                    if (queryDialog != null && queryDialog.isShowing()) {
                        queryDialog.dismiss();
                        if (LOG_ON) Log.d(TAG, "Dialog.dismiss");
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } finally {
                    queryDialog = null;
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.main_invalid_date), Toast.LENGTH_SHORT).show();
            }
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
        finish();
    }

    // Listener для востановителя покупок.
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null) return;

            if (result.isFailure()) {
                if(LOG_ON) Log.v(TAG, "Failed to query inventory: " + result);
                return;
            }

            // Проверка отключена ли реклама в приложении
            Purchase purchase = inventory.getPurchase(SKU_ADS_DISABLE);

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(APP_PREFERENCES_ADS_SHOW, (purchase != null && verifyDeveloperPayload(purchase)));
            editor.putString(APP_PREFERENCES_AD_DATE, null);
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
                    if(LOG_ON) Log.v(TAG, "Dialog.dismiss");
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
            String tableName = "stations";

            if(LOG_ON) Log.v(TAG, response[0]);

            try {
                dataJsonQbj = new JSONObject(response[0]);
                JSONArray rasp = dataJsonQbj.getJSONArray("station");

                databaseH.removeAll(tableName);

                for (int i = 0; i < rasp.length(); i++) {
                    JSONObject oneObject = rasp.getJSONObject(i);

                    String nameStation = oneObject.getString("name_station");
                    String noteStation = oneObject.getString("note_station");
                    long codeStation = oneObject.getLong("id_station");
                    long sumStation = oneObject.getLong("sum_station");
                    databaseH.create(new AutoCompleteObject((nameStation + " " + noteStation), sumStation, codeStation), tableName);
                }

                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(APP_PREFERENCES_MD5_CHECK, true);
                editor.apply();
                if (LOG_ON) Log.v("Settings", "Station true");
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
            btnNextDay.setEnabled(true);
        }
    }

    private class processingLoadStationEkbToDB extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... response) {
            JSONObject dataJsonQbj;
            String tableName = "stations_ekb";

            if(LOG_ON) Log.v(TAG, response[0]);

            try {
                dataJsonQbj = new JSONObject(response[0]);
                JSONArray rasp = dataJsonQbj.getJSONArray("station");

                databaseH.removeAll(tableName);

                for (int i = 0; i < rasp.length(); i++) {
                    JSONObject oneObject = rasp.getJSONObject(i);

                    String nameStation = oneObject.getString("name_station");
                    long codeStation = oneObject.getLong("id_station");
                    long sumStation = oneObject.getLong("sum_station");
                    databaseH.create(new AutoCompleteObject(nameStation, sumStation, codeStation), tableName);
                }

                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(APP_PREFERENCES_MD5_EKB_CHECK, true);
                editor.apply();
                if (LOG_ON) Log.v("Settings", "Station true");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class processingLoadScheduleResult extends AsyncTask<String, Void, List<RouteObjectResult>> {
        @Override
        protected List<RouteObjectResult> doInBackground(String... response) {
            JSONObject dataJsonQbj;
            List<RouteObjectResult> list = new ArrayList<>();

            if(LOG_ON) Log.v(TAG, response[0]);

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
