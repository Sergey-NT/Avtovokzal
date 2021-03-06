package com.www.avtovokzal.org;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.google.android.gms.analytics.Tracker;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.www.avtovokzal.org.Adapter.ArrivalObjectResultAdapter;
import com.www.avtovokzal.org.Adapter.AutocompleteCustomArrayAdapter;
import com.www.avtovokzal.org.Fragment.KoltsovoDialogFragment;
import com.www.avtovokzal.org.Listener.ArrivalAutoCompleteTextChangedListener;
import com.www.avtovokzal.org.Object.ArrivalObjectResult;
import com.www.avtovokzal.org.Object.AutoCompleteObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ArrivalActivity extends AppCompatSettingsActivity {

    private static final int LAYOUT = R.layout.activity_arrival;
    private static final String TAG = "ArrivalActivity";

    public CustomAutoCompleteView myAutoComplete;
    public ArrayAdapter<AutoCompleteObject> myAdapter;
    public DatabaseHandler databaseH;

    private Drawer drawerResult = null;
    private ListView listView;
    private ProgressDialog progressDialog;

    private String code;
    private String newNameStation;

    private boolean showDialogKoltsovo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        boolean defaultStation;
        databaseH = DatabaseHandler.getInstance(getApplicationContext());

        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);

        // Переменная, отвечает за работу с настройками
        settings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        showDialogKoltsovo = settings.getBoolean(Constants.APP_PREFERENCES_SHOW_DIALOG_KOLTSOVO, true);

        // Проверка отключения рекламы
        boolean AdShowGone = settings.contains(Constants.APP_PREFERENCES_ADS_SHOW) && settings.getBoolean(Constants.APP_PREFERENCES_ADS_SHOW, false);

        if (Constants.DEVELOPER) {
            AdShowGone = true;
        }

        // Реклама в приложении
        if (!AdShowGone) {
            initializeAd(R.id.adViewArrivalActivity);
        }

        // Определяем элементы интерфейса
        listView = (ListView) findViewById(R.id.listViewArrival);
        myAutoComplete = (CustomAutoCompleteView) findViewById(R.id.autoCompleteArrival);

        // Получаем переменные
        code = getIntent().getStringExtra("code");
        newNameStation = getIntent().getStringExtra("newNameStation");

        if (newNameStation != null) {
            myAutoComplete.setText(newNameStation);
        }

        if (Constants.LOG_ON) Log.v("newNameStation", "" + newNameStation);

        defaultStation = getSettingsParams(Constants.APP_PREFERENCES_DEFAULT);

        if (defaultStation && settings.contains(Constants.APP_PREFERENCES_STATION_CODE) && settings.contains(Constants.APP_PREFERENCES_STATION_NAME)) {
            code = settings.getString(Constants.APP_PREFERENCES_STATION_CODE, null);
            myAutoComplete.setText(settings.getString(Constants.APP_PREFERENCES_STATION_NAME, null));
            loadArrivalResult(code);
        } else if (code != null) {
            loadArrivalResult(code);
        }

        listViewListener();
        myAutoCompleteListener();
        myAutoCompleteFocus();

        myAutoComplete.addTextChangedListener(new ArrivalAutoCompleteTextChangedListener(this));
        AutoCompleteObject[] ObjectItemData = new AutoCompleteObject[0];
        myAdapter = new AutocompleteCustomArrayAdapter(this, R.layout.listview_dropdown_item, ObjectItemData);

        initializeToolbar(R.string.app_name_city, R.string.app_subtitle_arrival);
        initializeNavigationDrawer();
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
                myAutoComplete.setText(tv.getText().toString());

                // Програмное скрытие клавиатуры
                InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(myAutoComplete.getWindowToken(), 0);

                code = tv.getTag().toString();

                // Запрос и отображение результатов поиска
                loadArrivalResult(code);

                myAutoComplete.clearFocus();

                if (Integer.parseInt(code) == 102 && showDialogKoltsovo) {
                    FragmentManager manager = getSupportFragmentManager();
                    KoltsovoDialogFragment dialogFragment = new KoltsovoDialogFragment();
                    dialogFragment.show(manager, "dialog");
                }
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
                        loadArrivalResult(code);

                        myAutoComplete.clearFocus();

                        if (Integer.parseInt(code) == 102 && showDialogKoltsovo) {
                            FragmentManager manager = getSupportFragmentManager();
                            KoltsovoDialogFragment dialogFragment = new KoltsovoDialogFragment();
                            dialogFragment.show(manager, "dialog");
                        }
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
                                Intent intentMain = new Intent(ArrivalActivity.this, MainActivity.class);
                                startActivity(intentMain);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 2:
                                drawerResult.closeDrawer();
                                return true;
                            case 4:
                                Intent intentEtraffic = new Intent(ArrivalActivity.this, EtrafficActivity.class);
                                startActivity(intentEtraffic);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 6:
                                Intent intentEtrafficMain = new Intent(ArrivalActivity.this, EtrafficMainActivity.class);
                                startActivity(intentEtrafficMain);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 8:
                                Intent intentMenu = new Intent(ArrivalActivity.this, MenuActivity.class);
                                startActivity(intentMenu);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 9:
                                Intent intentAbout = new Intent(ArrivalActivity.this, AboutActivity.class);
                                startActivity(intentAbout);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                        }
                        return false;
                    }
                })
                .build();
        drawerResult.setSelection(2);
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
        if (adView != null && getSettingsParams(Constants.APP_PREFERENCES_ADS_SHOW)) {
            adView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        queryDialogDismiss();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        // Скрываем статус сервера
        MenuItem itemLamp = menu.findItem(R.id.lamp);
        itemLamp.setVisible(false);
        return true;
    }

    // Загрузка расписания прибытия
    private void loadArrivalResult(Object... params){
        String url = "http://www.avtovokzal.org/php/app/arrival.php?id="+params[0];

        if (isOnline()) {
            progressDialog = new ProgressDialog(ArrivalActivity.this);
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
                    processingLoadArrivalResult task = new processingLoadArrivalResult();
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

    private class processingLoadArrivalResult extends AsyncTask<String, Void, List<ArrivalObjectResult>> {
        @Override
        protected List<ArrivalObjectResult> doInBackground(String... response) {
            JSONObject dataJsonQbj;
            List<ArrivalObjectResult> list = new ArrayList<>();

            if(Constants.LOG_ON)Log.v(TAG, response[0]);

            try {
                dataJsonQbj = new JSONObject(response[0]);
                JSONArray rasp = dataJsonQbj.getJSONArray("arrival");

                if (rasp.length() == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        @SuppressWarnings("ConstantConditions")
                        public void run() {
                            TextView textView1 = (TextView) findViewById(R.id.noArrivalItems);
                            textView1.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                     runOnUiThread(new Runnable() {
                        @Override
                        @SuppressWarnings("ConstantConditions")
                        public void run() {
                            TextView textView1 = (TextView) findViewById(R.id.noArrivalItems);
                            textView1.setVisibility(View.GONE);
                        }
                     });
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
            return list;
        }

        @Override
        protected void onPostExecute(List<ArrivalObjectResult> list) {
            final ArrivalObjectResultAdapter adapter = new ArrivalObjectResultAdapter(ArrivalActivity.this, list);
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
