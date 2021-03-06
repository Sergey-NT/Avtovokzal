package com.www.avtovokzal.org;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.www.avtovokzal.org.Adapter.EtrafficObjectAdapter;
import com.www.avtovokzal.org.Fragment.DatePickerFragmentEtraffic;
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

public class EtrafficActivity extends AppCompatSettingsActivity implements DatePickerDialog.OnDateSetListener {

    private static final int LAYOUT = R.layout.activity_etraffic;
    private final static String TAG = "EtrafficActivity";

    private Button btnDate;
    private Drawer drawerResult = null;
    private ListView listView;
    private ProgressDialog queryDialog;

    private String dateNow;
    private int day = 0;

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        Button btnNextDay;

        // Определяем элементы интерфейса
        listView = (ListView) findViewById(R.id.listViewEtraffic);

        // Добавляем футер к списку ListView
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.listview_footer, listView, false);
        listView.addFooterView(footer, null, false);

        // Переменная отвечает за работу с настройками
        settings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        dateNow = settings.getString(Constants.APP_PREFERENCES_DATE, null);

        // Проверка отключения рекламы
        boolean AdShowGone = settings.contains(Constants.APP_PREFERENCES_ADS_SHOW) && settings.getBoolean(Constants.APP_PREFERENCES_ADS_SHOW, false);

        if (Constants.DEVELOPER) {
            AdShowGone = true;
        }

        // Реклама в приложении
        if (!AdShowGone) {
            initializeAd(R.id.adViewEtrafficActivity);
        }

        initializeToolbar(R.string.app_name_city_ggm, R.string.app_subtitle_main);
        initializeNavigationDrawer();

        parsingHTML task = new parsingHTML();
        task.execute(dateNow);

        // Отменяем преобразование текста кнопок в AllCaps програмно
        btnDate = (Button) findViewById(R.id.header);
        String string = getString(R.string.main_schedule) + " " + dateNow;
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
                                Intent intentMain = new Intent(EtrafficActivity.this, MainActivity.class);
                                startActivity(intentMain);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 2:
                                Intent intentArrival = new Intent(EtrafficActivity.this, ArrivalActivity.class);
                                startActivity(intentArrival);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 4:
                                drawerResult.closeDrawer();
                                return true;
                            case 6:
                                Intent intentEtrafficMain = new Intent(EtrafficActivity.this, EtrafficMainActivity.class);
                                startActivity(intentEtrafficMain);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 8:
                                Intent intentMenu = new Intent(EtrafficActivity.this, MenuActivity.class);
                                startActivity(intentMenu);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                            case 9:
                                Intent intentAbout = new Intent(EtrafficActivity.this, AboutActivity.class);
                                startActivity(intentAbout);
                                finish();
                                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                                return true;
                        }
                        return false;
                    }
                })
                .build();
        drawerResult.setSelection(3);
    }

    @Override
    public void onBackPressed() {
        if (drawerResult != null && drawerResult.isDrawerOpen()) {
            drawerResult.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private class parsingHTML extends AsyncTask<String, Void, List<EtrafficObject>> {
        @Override
        protected List<EtrafficObject> doInBackground(String... params) {
            String url = "http://www.e-traffic.ru/schedule/nijniy-tagil?station=1&date="+params[0]+"&page=1";
            List<EtrafficObject> list = new ArrayList<>();
            Document doc;
            String time = null;
            String number = null;
            String name = null;
            String timeArrival = null;
            String countBus = null;
            String price = null;

            if(isOnline()) {
                try {
                    doc = Jsoup.connect(url).get();
                    Element table = doc.getElementsByTag("table").first();
                    if (table == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            @SuppressWarnings("ConstantConditions")
                            public void run() {
                                TextView textView = (TextView) findViewById(R.id.noItems);
                                textView.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        Elements rows = table.select("tr:nth-child(2n+1)");

                        if (rows.size() == 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                @SuppressWarnings("ConstantConditions")
                                public void run() {
                                    TextView textView = (TextView) findViewById(R.id.noItems);
                                    textView.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                @SuppressWarnings("ConstantConditions")
                                public void run() {
                                    TextView textView = (TextView) findViewById(R.id.noItems);
                                    textView.setVisibility(View.GONE);
                                }
                            });

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
                                if (Constants.LOG_ON) Log.v("Info", time + " " + number + " " + name + " " + timeArrival + " " + countBus + " " + price);
                                list.add(new EtrafficObject(time, number, name, timeArrival, countBus, price, ""));
                            }
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
            queryDialog = new ProgressDialog(EtrafficActivity.this);
            queryDialog.setMessage(getString(R.string.main_load));
            queryDialog.setCancelable(false);
            queryDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<EtrafficObject> list) {
            final EtrafficObjectAdapter adapter = new EtrafficObjectAdapter(EtrafficActivity.this, list);
            listView.setAdapter(adapter);
            super.onPostExecute(list);

            queryDialogDismiss();
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

            if (days >= 0 && days <= 9) {
                parsingHTML task = new parsingHTML();
                task.execute(dayNumber + "." + monthNumber + "." + year);
                String string = getString(R.string.main_schedule) + " " + dayNumber + "." + monthNumber + "." + year;
                btnDate.setText(string);
                // fix for Android 4.4.4
                queryDialogDismiss();
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
                .setAction(getString(R.string.analytics_action_set_date_etraffic))
                .build());

        DialogFragment newFragment = new DatePickerFragmentEtraffic();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    // Расписание на следующий день
    public void onClickNextDay(View view) {
        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.analytics_category_button))
                .setAction(getString(R.string.analytics_action_next_day_etraffic))
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
            btnDate.setText(string);

            parsingHTML task = new parsingHTML();
            task.execute(dateNew);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.main_invalid_date) , Toast.LENGTH_SHORT).show();
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

    // Вызов Error Activity
    private void callErrorActivity(){
        Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
        intent.putExtra("activity", TAG);
        startActivity(intent);
        finish();
    }

    private void queryDialogDismiss() {
        try {
            if (queryDialog != null && queryDialog.isShowing()) {
                queryDialog.dismiss();
                if (Constants.LOG_ON) Log.v(TAG, "Dialog.dismiss");
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            queryDialog = null;
        }
    }
}
