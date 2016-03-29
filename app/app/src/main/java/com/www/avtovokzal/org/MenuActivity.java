package com.www.avtovokzal.org;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.www.avtovokzal.org.Adapter.AutocompleteCustomArrayAdapter;
import com.www.avtovokzal.org.Listener.MenuAutoCompleteTextChangedListener;
import com.www.avtovokzal.org.Object.AutoCompleteObject;

public class MenuActivity extends AppCompatSettingsActivity implements BillingProcessor.IBillingHandler {

    private static final int LAYOUT = R.layout.menu_layout;
    private static final String PRODUCT_ID = "com.www.avtovokzal.org.ads.disable";
    private static final String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv5XXw+M1Yp9Nz7EbiKEBrknpsTRGV2NKZU8e6EMB3C0BvgiKvDiCQTqYJasfPj/ICsJ+oAfYMlJRS1y5V/fpOWYJCHr0vr7r+cgnd7GqKk5DMIxRe8hKMppqYDdTjW4oPuoS/qhH5mVapZWyOWh/kl4ZshAAmxnk9eRRA9W5zUz62jzAu30lwbr66YpwKulYYQw3wcOoBQcm9bYXMK4SEJKfkiZ7btYS1iDq1pshm9F5dW3E067JYdf4Sdxg9kLpVtOh9FqvHCrXai0stTf+0wLlBLOogNzPG9Gj7z2TVaZIdCWJKqZ97XP/Ur8kGBNaqDLCBSzm6IL+hsE5bzbmlQIDAQAB";

    public CustomAutoCompleteView myAutoComplete;
    public ArrayAdapter<AutoCompleteObject> myAdapter;
    public DatabaseHandler databaseH;

    private BillingProcessor bp;
    private Button btnAdsDisable;
    private CheckBox checkBoxDefaultStation;

    private String activity;
    private String code;
    private int day;
    private boolean cancel;
    private boolean sell;
    private boolean load;
    private boolean all;
    private boolean readyToPurchase = false;

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);

        databaseH = DatabaseHandler.getInstance(getApplicationContext());

        // Определяем элементы интерфейса
        btnAdsDisable = (Button) findViewById(R.id.btnAdsDisable);
        Button btnFeedback = (Button) findViewById(R.id.btnFeedback);
        CheckBox checkBoxAll = (CheckBox) findViewById(R.id.checkBoxAll);
        CheckBox checkBoxCancel = (CheckBox) findViewById(R.id.checkBoxCancel);
        CheckBox checkBoxSell = (CheckBox) findViewById(R.id.checkBoxSell);
        CheckBox checkBoxLoad = (CheckBox) findViewById(R.id.checkBoxCancelLoad);
        CheckBox checkBoxUpdate = (CheckBox) findViewById(R.id.checkBoxUpdate);
        checkBoxDefaultStation = (CheckBox) findViewById(R.id.checkBoxDefaultStation);
        myAutoComplete = (CustomAutoCompleteView) findViewById(R.id.autoCompleteMenu);

        // Отменяем преобразование текста кнопок в AllCaps програмно
        btnAdsDisable.setTransformationMethod(null);
        btnFeedback.setTransformationMethod(null);

        // Переменная, отвечает за работу с настройками
        settings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);

        bp = new BillingProcessor(this, LICENSE_KEY, this);
        boolean isAvailable = BillingProcessor.isIabServiceAvailable(this);
        if(!isAvailable) {
            btnAdsDisable.setVisibility(View.GONE);
            btnFeedback.setVisibility(View.GONE);
        }

        String price = settings.getString(Constants.APP_PREFERENCES_ADS_DISABLE_PRICE, "");
        boolean adShowGone = settings.getBoolean(Constants.APP_PREFERENCES_ADS_SHOW, false);
        if (!adShowGone) {
            String buttonText = getString(R.string.button_ads_disable) + " " + price;
            btnAdsDisable.setText(buttonText);
        } else {
            btnAdsDisable.setVisibility(View.GONE);
        }

        // Получаем переменные
        code = getIntent().getStringExtra("code");
        day = getIntent().getIntExtra("day", 0);
        activity = getIntent().getStringExtra("activity");

        // Получаем значения настроек
        cancel = getSettingsParams(Constants.APP_PREFERENCES_CANCEL);
        sell = getSettingsParams(Constants.APP_PREFERENCES_SELL);
        load = getSettingsParams(Constants.APP_PREFERENCES_LOAD);
        all = getSettingsParams(Constants.APP_PREFERENCES_ALL);
        boolean defaultStation = getSettingsParams(Constants.APP_PREFERENCES_DEFAULT);
        boolean update = getSettingsParams(Constants.APP_PREFERENCES_CANCEL_CHECK_VERSION);

        checkBoxCancel.setChecked(cancel);
        checkBoxSell.setChecked(sell);
        checkBoxLoad.setChecked(load);
        checkBoxDefaultStation.setChecked(defaultStation);
        checkBoxAll.setChecked(all);
        checkBoxUpdate.setChecked(update);

        checkBoxCancel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateCheckBoxValue(Constants.APP_PREFERENCES_CANCEL);
            }
        });

        checkBoxSell.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateCheckBoxValue(Constants.APP_PREFERENCES_SELL);
            }
        });

        checkBoxLoad.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateCheckBoxValue(Constants.APP_PREFERENCES_LOAD);
            }
        });

        checkBoxDefaultStation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateCheckBoxValue(Constants.APP_PREFERENCES_DEFAULT);
            }
        });

        checkBoxAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateCheckBoxValue(Constants.APP_PREFERENCES_ALL);
            }
        });

        checkBoxUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateCheckBoxValue(Constants.APP_PREFERENCES_CANCEL_CHECK_VERSION);
            }
        });

        // Загружаем если есть сохраненную остановку из настроек
        if (settings.contains(Constants.APP_PREFERENCES_STATION_NAME)) {
            myAutoComplete.setText(settings.getString(Constants.APP_PREFERENCES_STATION_NAME, null));
        }

        myAutoCompleteListener();
        myAutoCompleteFocus();

        myAutoComplete.addTextChangedListener(new MenuAutoCompleteTextChangedListener(this));
        AutoCompleteObject[] ObjectItemData = new AutoCompleteObject[0];
        myAdapter = new AutocompleteCustomArrayAdapter(this, R.layout.listview_dropdown_item, ObjectItemData);

        initToolbar(R.string.app_name, R.string.menu_settings);
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

                checkBoxDefaultStation.setChecked(true);

                // Запись названия и кода остановки в файл настроек
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(Constants.APP_PREFERENCES_DEFAULT, true);
                editor.putString(Constants.APP_PREFERENCES_STATION_NAME, tv.getText().toString());
                editor.putString(Constants.APP_PREFERENCES_STATION_CODE, code);
                editor.apply();

                // Google Analytics
                Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
                t.send(new HitBuilders.EventBuilder()
                        .setCategory(getString(R.string.analytics_category_dropdown))
                        .setAction(getString(R.string.analytics_action_dropdown_menu))
                        .build());

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

                        // Програмное скрытие клавиатуры
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(myAutoComplete.getWindowToken(), 0);

                        code = object.getObjectCode();

                        checkBoxDefaultStation.setChecked(true);

                        // Запись названия и кода остановки в файл настроек
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean(Constants.APP_PREFERENCES_DEFAULT, true);
                        editor.putString(Constants.APP_PREFERENCES_STATION_NAME, object.toString());
                        editor.putString(Constants.APP_PREFERENCES_STATION_CODE, code);
                        editor.apply();

                        // Google Analytics
                        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
                        t.send(new HitBuilders.EventBuilder()
                                .setCategory(getString(R.string.analytics_category_dropdown))
                                .setAction(getString(R.string.analytics_action_dropdown_menu))
                                .build());

                        checkBoxDefaultStation.requestFocus();
                    }
                }
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
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
    protected void onDestroy() {
        // Если перешли из MainActivity
        if (activity != null) {
            boolean checkBoxCancelValue = getSettingsParams(Constants.APP_PREFERENCES_CANCEL);
            boolean checkBoxSellValue = getSettingsParams(Constants.APP_PREFERENCES_SELL);
            boolean checkBoxLoadValue = getSettingsParams(Constants.APP_PREFERENCES_LOAD);
            boolean checkBoxAllValue = getSettingsParams(Constants.APP_PREFERENCES_ALL);

            // Если изменили настройки отображения расписания передаем переменные в MainActivity
            if (checkBoxCancelValue != cancel || checkBoxLoadValue != load || checkBoxSellValue != sell || checkBoxAllValue != all) {
                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                intent.putExtra("cancel", checkBoxCancelValue);
                intent.putExtra("sell", checkBoxSellValue);
                intent.putExtra("cancel_load", checkBoxLoadValue);
                intent.putExtra("day", day);
                intent.putExtra("all", checkBoxAllValue);

                if (code != null) {
                    intent.putExtra("code", code);
                }
                startActivity(intent);
            }
        }
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

    public void btnAdsDisableOnClick (View view) {
        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.analytics_category_button))
                .setAction(getString(R.string.analytics_action_ads_disable))
                .build());

        if (!readyToPurchase) {
            showToast(getString(R.string.menu_billing_not_initialized));
            return;
        }
        bp.purchase(this, PRODUCT_ID);
    }

    public void btnFeedbackOnClick (View view) {
        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.analytics_category_button))
                .setAction(getString(R.string.analytics_action_feedback))
                .build());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.www.avtovokzal.org"));
        startActivity(intent);
    }

    // Обновляем параметры настроек в зависимости от изменения состояния CheckBox
    private void updateCheckBoxValue(String params) {
        boolean checkBoxValue;
        // Получаем значение из настроек
        checkBoxValue = getSettingsParams(params);
        if (checkBoxValue) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(params, false);
            editor.apply();
            // Google Analytics
            Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.analytics_category_checkbox))
                    .setAction(params + " " + getString(R.string.analytics_action_checkbox_cancel))
                    .build());
        } else {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(params, true);
            editor.apply();
            // Google Analytics
            Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.analytics_category_checkbox))
                    .setAction(params + " " + getString(R.string.analytics_action_checkbox_ok))
                    .build());
        }
    }

    private class getSkuDetails extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SkuDetails list = bp.getPurchaseListingDetails(PRODUCT_ID);

            if (list != null) {
                String price = String.valueOf(list.priceValue);
                String currency = list.currency;
                String textPrice = price + " " + currency;
                final String buttonText = getString(R.string.button_ads_disable) + " " + textPrice;

                if (!settings.getString(Constants.APP_PREFERENCES_ADS_DISABLE_PRICE, "").equals(textPrice)) {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(Constants.APP_PREFERENCES_ADS_DISABLE_PRICE, textPrice);
                    editor.apply();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnAdsDisable.setText(buttonText);
                        }
                    });
                }
            }
            return null;
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        showToast(getString(R.string.menu_ads_disable_toast));

        // Сохраняем в настройках
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.APP_PREFERENCES_ADS_SHOW, true);
        editor.apply();
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

    }

    @Override
    public void onBillingInitialized() {
        readyToPurchase = true;
        getSkuDetails task = new getSkuDetails();
        task.execute();
    }
}
