package com.www.avtovokzal.org;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.www.avtovokzal.org.Adapter.GgmObjectAdapter;
import com.www.avtovokzal.org.Object.GgmObject;

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

public class GgmActivity extends AppCompatSettingsActivity implements DatePickerDialog.OnDateSetListener {

    private Drawer drawerResult = null;
    private Toolbar toolbar;
    private ListView listView;
    private String dateNow;
    private Button btnDate;
    private ProgressDialog queryDialog;
    private AdView adView;
    private SharedPreferences settings;
    private int day = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ggm);

        Button btnNextDay;
        boolean AdShowGone;

        listView = (ListView) findViewById(R.id.listViewGgm);

        // Добавляем футер к списку ListView
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.listview_footer, listView, false);
        listView.addFooterView(footer, null, false);

        // Переменная отвечает за работу с настройками
        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        dateNow = settings.getString(APP_PREFERENCES_DATE, null);

        // Проверка отключения рекламы
        AdShowGone = settings.contains(APP_PREFERENCES_ADS_SHOW) && settings.getBoolean(APP_PREFERENCES_ADS_SHOW, false);

        if (DEVELOPER) {
            AdShowGone = true;
        }

        Log.v("Date", dateNow);

        // Реклама в приложении
        if (!AdShowGone) {
            initializeAd();
        }

        initializeToolbar();
        initializeNavigationDrawer();
        parsingHTML task = new parsingHTML();
        task.execute(dateNow);

        // Отменяем преобразование текста кнопок в AllCaps програмно
        btnDate = (Button) findViewById(R.id.header);
        btnDate.setText(getString(R.string.main_schedule) + " " + dateNow);
        btnNextDay = (Button) findViewById(R.id.buttonNextDay);
        btnDate.setTransformationMethod(null);
        btnNextDay.setTransformationMethod(null);
    }

    private void initializeToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name_city_ggm);
            toolbar.setSubtitle(R.string.app_subtitle_main);
            setSupportActionBar(toolbar);
        }
    }

    private void initializeNavigationDrawer() {
        drawerResult = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withDisplayBelowToolbar(true)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        new SectionDrawerItem()
                                .withName(R.string.app_name_city),
                        new PrimaryDrawerItem()
                                .withName(R.string.app_subtitle_main)
                                .withIcon(R.drawable.ic_vertical_align_top_black_18dp),
                        new PrimaryDrawerItem()
                                .withName(R.string.app_subtitle_arrival)
                                .withIcon(R.drawable.ic_vertical_align_bottom_black_18dp),
                        new SectionDrawerItem()
                                .withName(R.string.app_name_city_ggm),
                        new PrimaryDrawerItem()
                                .withName(R.string.app_subtitle_main)
                                .withIdentifier(1)
                                .withIcon(R.drawable.ic_vertical_align_top_black_18dp),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem()
                                .withName(R.string.menu_settings)
                                .withIcon(R.drawable.ic_settings_black_18dp),
                        new PrimaryDrawerItem()
                                .withName(R.string.menu_about)
                                .withIcon(R.drawable.ic_info_outline_black_18dp)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> adapterView, View view, int position, long l, IDrawerItem iDrawerItem) {
                        switch (position) {
                            case 1:
                                Intent intentMain = new Intent(GgmActivity.this, MainActivity.class);
                                startActivity(intentMain);
                                drawerResult.closeDrawer();
                                finish();
                                return true;
                            case 2:
                                Intent intentArrival = new Intent(GgmActivity.this, ArrivalActivity.class);
                                startActivity(intentArrival);
                                drawerResult.closeDrawer();
                                finish();
                                return true;
                            case 4:
                                drawerResult.closeDrawer();
                                return true;
                            case 6:
                                Intent intentMenu = new Intent(GgmActivity.this, MenuActivity.class);
                                startActivity(intentMenu);
                                drawerResult.closeDrawer();
                                finish();
                                return true;
                            case 7:
                                Intent intentAbout = new Intent(GgmActivity.this, AboutActivity.class);
                                startActivity(intentAbout);
                                drawerResult.closeDrawer();
                                finish();
                                return true;
                        }
                        return false;
                    }
                })
                .build();
        drawerResult.setSelectionByIdentifier(1);
    }

    @Override
    public void onBackPressed() {
        if (drawerResult != null && drawerResult.isDrawerOpen()) {
            drawerResult.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private void initializeAd() {
        // Создание экземпляра adView
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.admob_main_activity));
        adView.setAdSize(AdSize.SMART_BANNER);

        // Поиск разметки LinearLayout
        LinearLayout layout = (LinearLayout)findViewById(R.id.adViewGgmActivity);

        // Добавление в разметку экземпляра adView
        layout.addView(adView);

        // Инициирование общего запроса
        AdRequest request = new AdRequest.Builder().build();

        // Загрузка adView с объявлением
        adView.loadAd(request);
    }

    private class parsingHTML extends AsyncTask<String, Void, List<GgmObject>> {
        @Override
        protected List<GgmObject> doInBackground(String... params) {
            String url = "http://www.e-traffic.ru/schedule/nijniy-tagil?station=1&date="+params[0]+"&page=1";
            Document doc;
            List<GgmObject> list = new ArrayList<>();
            String time = null;
            String number = null;
            String name = null;
            String timeArrival = null;
            String countBus = null;
            String price = null;

            try {
                doc = Jsoup.connect(url).get();
                Element table = doc.getElementsByTag("table").get(0);
                Elements rows = table.select("tr:nth-child(2n+1)");

                for (int i = 1; i < rows.size(); i++) {
                    Element row = rows.get(i);
                    Elements cols = row.select("td");

                    for (int y = 0; y < cols.size() - 1; y++) {
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
                        }
                    }
                    Log.v("Info", time + " " + number + " " + name + " " + timeArrival + " " + countBus + " " + price);
                    list.add(new GgmObject(time, number, name, timeArrival, countBus, price));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPreExecute() {
            queryDialog = new ProgressDialog(GgmActivity.this);
            queryDialog.setMessage(getString(R.string.main_load));
            queryDialog.setCancelable(false);
            queryDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<GgmObject> list) {
            final GgmObjectAdapter adapter = new GgmObjectAdapter(GgmActivity.this, list);
            listView.setAdapter(adapter);
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

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayDatePicker) {
        if (dateNow != null) {
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
            int days = (int) (diff / (24 * 60 * 60 * 1000));

            day = 0;
            day = day + days;
            Log.v("day onDateSet", "" +day);

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

            if (days >= 0 && days <= 9) {
                parsingHTML task = new parsingHTML();
                task.execute(dayNumber + "." + monthNumber + "." + year);
                btnDate.setText(getString(R.string.main_schedule) + " " + dayNumber + "." + monthNumber + "." + year);
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

        DialogFragment newFragment = new DatePickerFragmentGmm();
        newFragment.show(getSupportFragmentManager(), "datePicker");
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

        Log.v("day onClick", "" + day);

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

            btnDate.setText(getString(R.string.main_schedule) + " " + dateNew);

            parsingHTML task = new parsingHTML();
            task.execute(dateNew);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.main_invalid_date) , Toast.LENGTH_SHORT).show();
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
        super.onDestroy();
    }

    // Получаем параметры из файла настроек
    private boolean getSettingsParams(String params) {
        boolean checkBoxValue;
        checkBoxValue = settings.contains(params) && settings.getBoolean(params, false);
        return checkBoxValue;
    }

    // Вызов Error Activity
    private void callErrorActivity(){
        Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
        startActivity(intent);
        finish();
    }

    // Проверка подключения к сети интернет
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
