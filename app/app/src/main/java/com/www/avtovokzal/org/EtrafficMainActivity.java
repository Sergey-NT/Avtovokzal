package com.www.avtovokzal.org;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.www.avtovokzal.org.Adapter.AutocompleteCustomArrayAdapter;
import com.www.avtovokzal.org.Adapter.EtrafficObjectAdapter;
import com.www.avtovokzal.org.Listener.EtrafficAutoCompleteTextChangedListener;
import com.www.avtovokzal.org.Object.AutoCompleteObject;
import com.www.avtovokzal.org.Object.EtrafficObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EtrafficMainActivity extends AppCompatSettingsActivity implements DatePickerDialog.OnDateSetListener {

    public CustomAutoCompleteView myAutoComplete;
    public ArrayAdapter<AutoCompleteObject> myAdapter;
    public DatabaseHandler databaseH;

    private List<EtrafficObject> list;
    private Button btnDate;
    private Drawer drawerResult = null;
    private ListView listView;
    private ProgressDialog queryDialog;
    private EtrafficObjectAdapter adapter;

    private int day = 0;
    private int countRows = 0;
    private int previousTotal = 0;
    private int currentPage = 1;

    private boolean rest = true;
    private boolean loading = true;
    private boolean AdShowGone;

    private String date;
    private String code;
    private String dateSettings = "";
    private String dateSystem = "";
    private String codeSettings = "";
    private String nameStation;

    private final static String TAG = "EtrafficMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etraffic_main);

        Button btnNextDay;

        databaseH = DatabaseHandler.getInstance(getApplicationContext());
        list = new ArrayList<>();

        // Определяем элементы интерфейса
        myAutoComplete = (CustomAutoCompleteView) findViewById(R.id.autoCompleteEtrafficMain);
        listView = (ListView) findViewById(R.id.listViewEtrafficMain);

        // Добавляем футер к списку ListView
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.listview_footer, listView, false);
        listView.addFooterView(footer, null, false);

        // Переменная отвечает за работу с настройками
        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        dateSystem = settings.getString(APP_PREFERENCES_DATE, null);
        date = dateSystem;

        // Проверка отключения рекламы
        AdShowGone = settings.contains(APP_PREFERENCES_ADS_SHOW) && settings.getBoolean(APP_PREFERENCES_ADS_SHOW, false);

        if (DEVELOPER) {
            AdShowGone = true;
        }

        // Реклама в приложении
        if (!AdShowGone) {
            initializeAd(R.id.adViewEtrafficMainActivity);
        }

        myAutoCompleteFocus();
        myAutoCompleteListener();

        initializeToolbar(R.string.app_name_city_ekb, R.string.app_subtitle_main);
        initializeNavigationDrawer();

        myAutoComplete.addTextChangedListener(new EtrafficAutoCompleteTextChangedListener(this));
        AutoCompleteObject[] ObjectItemData = new AutoCompleteObject[0];
        myAdapter = new AutocompleteCustomArrayAdapter(this, R.layout.listview_dropdown_item, ObjectItemData);
        listView.setOnScrollListener(new EndlessScrollListener());

        // Отменяем преобразование текста кнопок в AllCaps програмно
        btnDate = (Button) findViewById(R.id.header);
        String string = getString(R.string.main_schedule) + " " + date;
        btnDate.setText(string);
        btnDate.setTransformationMethod(null);
        btnNextDay = (Button) findViewById(R.id.buttonNextDay);
        btnNextDay.setTransformationMethod(null);
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
                                Intent intentMain = new Intent(EtrafficMainActivity.this, MainActivity.class);
                                startActivity(intentMain);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 2:
                                setCountOnePlus();
                                Intent intentArrival = new Intent(EtrafficMainActivity.this, ArrivalActivity.class);
                                startActivity(intentArrival);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 4:
                                setCountOnePlus();
                                Intent intentEtraffic = new Intent(EtrafficMainActivity.this, EtrafficActivity.class);
                                startActivity(intentEtraffic);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 6:
                                drawerResult.closeDrawer();
                                return true;
                            case 8:
                                setCountOnePlus();
                                Intent intentMenu = new Intent(EtrafficMainActivity.this, MenuActivity.class);
                                startActivity(intentMenu);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 9:
                                setCountOnePlus();
                                Intent intentAbout = new Intent(EtrafficMainActivity.this, AboutActivity.class);
                                startActivity(intentAbout);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                        }
                        return false;
                    }
                })
                .build();
        drawerResult.setSelection(4);
    }

    @Override
    public void onBackPressed() {
        if (drawerResult != null && drawerResult.isDrawerOpen()) {
            drawerResult.closeDrawer();
        } else {
            super.onBackPressed();
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

                parsingHTML task = new parsingHTML();
                task.execute(code, date, "1");

                sendIdToDb(code);

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

                        parsingHTML task = new parsingHTML();
                        task.execute(code, date, "1");

                        sendIdToDb(code);

                        myAutoComplete.clearFocus();
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayDatePicker) {
        if (date != null) {
            String monthNumber;
            String dayNumber;

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault());

            Calendar calendarNow = Calendar.getInstance();
            try {
                calendarNow.setTime(sdf.parse(dateSystem));
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

            date = dayNumber + "." + monthNumber + "." + year;

            if (days >= 0 && days <= 9) {
                if (code != null) {
                    parsingHTML task = new parsingHTML();
                    task.execute(code, date, "1");
                    loading = true;
                }
                String string = getString(R.string.main_schedule) + " " + date;
                btnDate.setText(string);
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
                .setAction(getString(R.string.analytics_action_set_date_etraffic_main))
                .build());

        DialogFragment newFragment = new DatePickerFragmentEtrafficMain();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    // Расписание на следующий день
    public void onClickNextDay(View view) {
        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.analytics_category_button))
                .setAction(getString(R.string.analytics_action_next_day_etraffic_main))
                .build());

        day = day + 1;

        if (day >= 0 && day <= 9) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault());
            Calendar c = Calendar.getInstance();
            try {
                c.setTime(sdf.parse(dateSystem));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            c.add(Calendar.DATE, day);
            date = sdf.format(c.getTime());

            String string = getString(R.string.main_schedule) + " " + date;
            btnDate.setText(string);

            parsingHTML task = new parsingHTML();
            task.execute(code, date, "1");
            loading = true;
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.main_invalid_date) , Toast.LENGTH_SHORT).show();
        }
    }

    private class parsingHTML extends AsyncTask<String, Void, List<EtrafficObject>> {
        @Override
        protected List<EtrafficObject> doInBackground(String... params) {
            String url = "http://www.e-traffic.ru/schedule/ekaterinburg?station="+params[0]+"&date="+params[1]+"&page="+params[2];
            Document doc;
            String time = null;
            String number = null;
            String name = null;
            String timeArrival = null;
            String countBus = null;
            String price = null;
            String urlToBuy = "";
            int count = 0;

            if (dateSettings.equals(params[1])) {
                rest = false;
            }

            if (!codeSettings.equals(params[0]) || !dateSettings.equals(params[1])) {
                list.clear();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listView.getAdapter() != null) {
                            listView.setAdapter(null);
                        }
                    }
                });
                rest = true;
                countRows = 0;
                previousTotal = 0;
                currentPage = 1;
                codeSettings = params[0];
                dateSettings = params[1];
            }

            if(isOnline()) {
                try {
                    doc = Jsoup.connect(url).get();
                    Element table = doc.getElementsByTag("table").first();
                    if (table == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView textView = (TextView) findViewById(R.id.noItems);
                                textView.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        Elements rows = table.select("tr:nth-child(2n+1)");

                        if (rows.size() == 0 && params[2].equals("1")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView textView = (TextView) findViewById(R.id.noItems);
                                    textView.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView textView = (TextView) findViewById(R.id.noItems);
                                    textView.setVisibility(View.GONE);
                                }
                            });

                            for (int i = 1; i < rows.size(); i++) {
                                Element row = rows.get(i);
                                Elements cols = row.select("td");

                                for (int y = 0; y < cols.size(); y++) {
                                    Element col = cols.get(y);
                                    switch (y) {
                                        case 0:
                                            time = col.text();
                                            break;
                                        case 1:
                                            number = col.text();
                                            break;
                                        case 2:
                                            name = col.text();
                                            break;
                                        case 3:
                                            timeArrival = col.text();
                                            break;
                                        case 4:
                                            countBus = col.text().toLowerCase();
                                            break;
                                        case 5:
                                            price = col.text();
                                            break;
                                        case 6:
                                            urlToBuy = col.select("a[href]").attr("abs:href");
                                            break;
                                    }
                                }
                                if (LOG_ON) Log.v("Info", time + " " + number + " " + name + " " + timeArrival + " " + countBus + " " + price);
                                list.add(new EtrafficObject(time, number, name, timeArrival, countBus, price, urlToBuy));
                                count = i;
                            }
                            countRows = count;
                            if (LOG_ON) Log.v("Count rows:", ""+countRows);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                callErrorActivity();
            }
            return list;
        }

        @Override
        protected void onPreExecute() {
            queryDialog = new ProgressDialog(EtrafficMainActivity.this);
            queryDialog.setMessage(getString(R.string.main_load));
            queryDialog.setCancelable(false);
            queryDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<EtrafficObject> list) {
            if (listView.getAdapter() == null || rest) {
                adapter = new EtrafficObjectAdapter(EtrafficMainActivity.this, list);
                listView.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
            try {
                if (queryDialog != null && queryDialog.isShowing()) {
                    queryDialog.dismiss();
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } finally {
                queryDialog = null;
            }
            super.onPostExecute(list);
        }
    }

    public class EndlessScrollListener implements AbsListView.OnScrollListener {

        private int visibleThreshold = 5;

        public EndlessScrollListener() {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold) && countRows == 20) {
                new parsingHTML().execute(code, date, ""+currentPage);
                loading = true;
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

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
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
        super.onDestroy();
    }

    // Вызов Error Activity
    private void callErrorActivity(){
        Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
        intent.putExtra("activity", TAG);
        startActivity(intent);
        finish();
    }

    private void sendIdToDb(String id) {
        String url = "http://www.avtovokzal.org/php/app/result_ekb.php?id="+id;

        if (isOnline()) {
            StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response == null) {
                        callErrorActivity();
                        finish();
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
}
