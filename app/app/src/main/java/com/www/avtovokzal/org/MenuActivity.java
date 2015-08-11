package com.www.avtovokzal.org;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.www.avtovokzal.org.Adapter.AutocompleteCustomArrayAdapter;
import com.www.avtovokzal.org.Billing.IabHelper;
import com.www.avtovokzal.org.Billing.IabResult;
import com.www.avtovokzal.org.Billing.Inventory;
import com.www.avtovokzal.org.Billing.Purchase;
import com.www.avtovokzal.org.Listener.MenuAutoCompleteTextChangedListener;
import com.www.avtovokzal.org.Object.AutoCompleteObject;

import java.util.ArrayList;

public class MenuActivity extends AppCompatSettingsActivity {

    IabHelper mHelper;

    public CustomAutoCompleteView myAutoComplete;
    public ArrayAdapter<AutoCompleteObject> myAdapter;
    public DatabaseHandler databaseH;

    private AdView adView;
    private Button btnAdsDisable;
    private CheckBox checkBoxDefaultStation;
    private Drawer drawerResult = null;
    private SharedPreferences settings;
    private Toolbar toolbar;

    private String activity;
    private String code;
    private int day;
    private boolean cancel;
    private boolean sell;
    private boolean load;

    private static final String TAG = "MenuActivity";
    private static final String SKU_ADS_DISABLE = "com.www.avtovokzal.org.ads.disable";
    private static final int RC_REQUEST = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_layout);

        boolean AdShowGone;
        boolean defaultStation;
        CheckBox checkBoxCancel;
        CheckBox checkBoxSell;
        CheckBox checkBoxLoad;
        Button btnFeedback;
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv5XXw+M1Yp9Nz7EbiKEBrknpsTRGV2NKZU8e6EMB3C0BvgiKvDiCQTqYJasfPj/ICsJ+oAfYMlJRS1y5V/fpOWYJCHr0vr7r+cgnd7GqKk5DMIxRe8hKMppqYDdTjW4oPuoS/qhH5mVapZWyOWh/kl4ZshAAmxnk9eRRA9W5zUz62jzAu30lwbr66YpwKulYYQw3wcOoBQcm9bYXMK4SEJKfkiZ7btYS1iDq1pshm9F5dW3E067JYdf4Sdxg9kLpVtOh9FqvHCrXai0stTf+0wLlBLOogNzPG9Gj7z2TVaZIdCWJKqZ97XP/Ur8kGBNaqDLCBSzm6IL+hsE5bzbmlQIDAQAB";

        // Google Analytics
        Tracker t = ((AppController) getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);

        databaseH = new DatabaseHandler(MenuActivity.this);

        // Определяем элементы интерфейса
        btnAdsDisable = (Button) findViewById(R.id.btnAdsDisable);
        btnFeedback = (Button) findViewById(R.id.btnFeedback);
        checkBoxCancel = (CheckBox) findViewById(R.id.checkBoxCancel);
        checkBoxSell = (CheckBox) findViewById(R.id.checkBoxSell);
        checkBoxLoad = (CheckBox) findViewById(R.id.checkBoxCancelLoad);
        checkBoxDefaultStation = (CheckBox) findViewById(R.id.checkBoxDefaultStation);
        myAutoComplete = (CustomAutoCompleteView) findViewById(R.id.autoCompleteMenu);

        // Отменяем преобразование текста кнопок в AllCaps програмно
        btnAdsDisable.setTransformationMethod(null);
        btnFeedback.setTransformationMethod(null);

        // Переменная, отвечает за работу с настройками
        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        // Проверка отключения рекламы
        AdShowGone = settings.contains(APP_PREFERENCES_ADS_SHOW) && settings.getBoolean(APP_PREFERENCES_ADS_SHOW, false);

        // Создание Helper, передавая ему наш контекст и открытый ключ для проверки подписи
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
                // Проверка уже купленного, запрос цены.
                ArrayList<String> skuList = new ArrayList<>();
                skuList.add(SKU_ADS_DISABLE);
                mHelper.queryInventoryAsync(true, skuList, mGotInventoryListener);
            }
        });

        if (DEVELOPER) {
            AdShowGone = true;
        }

        // Если покупка совершена скрывать кнопку оплаты
        if (AdShowGone) {
            btnAdsDisable.setVisibility(View.GONE);
        }

        // Реклама в приложении
        if (!AdShowGone) {
            initializeAd();
        }

        // Получаем переменные
        code = getIntent().getStringExtra("code");
        day = getIntent().getIntExtra("day", 0);
        activity = getIntent().getStringExtra("activity");

        // Получаем значения настроек
        cancel = getSettingsParams(APP_PREFERENCES_CANCEL);
        sell = getSettingsParams(APP_PREFERENCES_SELL);
        load = getSettingsParams(APP_PREFERENCES_LOAD);
        defaultStation = getSettingsParams(APP_PREFERENCES_DEFAULT);

        checkBoxCancel.setChecked(cancel);
        checkBoxSell.setChecked(sell);
        checkBoxLoad.setChecked(load);
        checkBoxDefaultStation.setChecked(defaultStation);

        checkBoxCancel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                updateCheckBoxValue(APP_PREFERENCES_CANCEL);
            }
        });

        checkBoxSell.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateCheckBoxValue(APP_PREFERENCES_SELL);
            }
        });

        checkBoxLoad.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateCheckBoxValue(APP_PREFERENCES_LOAD);
            }
        });

        checkBoxDefaultStation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateCheckBoxValue(APP_PREFERENCES_DEFAULT);
            }
        });

        // Загружаем если есть сохраненную остановку из настроек
        if (settings.contains(APP_PREFERENCES_STATION_NAME)) {
            myAutoComplete.setText(settings.getString(APP_PREFERENCES_STATION_NAME, null));
        }

        myAutoCompleteListener();
        myAutoCompleteFocus();

        myAutoComplete.addTextChangedListener(new MenuAutoCompleteTextChangedListener(this));
        AutoCompleteObject[] ObjectItemData = new AutoCompleteObject[0];
        myAdapter = new AutocompleteCustomArrayAdapter(this, R.layout.listview_dropdown_item, ObjectItemData);

        initializeToolbar();
        initializeNavigationDrawer();
    }

    private void myAutoCompleteFocus() {
        // При получении фокуса полем AutoComplete стираем ранее введенный текст
        myAutoComplete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean bool) {
                if(bool) {
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
                editor.putBoolean(APP_PREFERENCES_DEFAULT, true);
                editor.putString(APP_PREFERENCES_STATION_NAME, tv.getText().toString());
                editor.putString(APP_PREFERENCES_STATION_CODE, code);
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
    }

    private void initializeAd() {
        // Создание экземпляра adView
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.admob_menu_activity));
        adView.setAdSize(AdSize.SMART_BANNER);

        // Поиск разметки LinearLayout
        LinearLayout layout = (LinearLayout)findViewById(R.id.adViewMenuActivity);

        // Добавление в разметку экземпляра adView
        layout.addView(adView);

        // Инициирование общего запроса
        AdRequest request = new AdRequest.Builder().build();

        // Загрузка adView с объявлением
        adView.loadAd(request);
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
                                .withIcon(R.drawable.ic_vertical_align_top_black_18dp),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem()
                                .withName(R.string.menu_settings)
                                .withIdentifier(1)
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
                                Intent intentMain = new Intent(MenuActivity.this, MainActivity.class);
                                startActivity(intentMain);
                                finish();
                                overridePendingTransition(R.animator.slide_out_left, R.animator.slide_in_right);
                                return true;
                            case 2:
                                Intent intentArrival = new Intent(MenuActivity.this, ArrivalActivity.class);
                                startActivity(intentArrival);
                                finish();
                                overridePendingTransition(R.animator.slide_out_left, R.animator.slide_in_right);
                                return true;
                            case 4:
                                Intent intentEtraffic = new Intent(MenuActivity.this, EtrafficActivity.class);
                                startActivity(intentEtraffic);
                                finish();
                                overridePendingTransition(R.animator.slide_out_left, R.animator.slide_in_right);
                                return true;
                            case 6:
                                drawerResult.closeDrawer();
                                return true;
                            case 7:
                                Intent intentAbout = new Intent(MenuActivity.this, AboutActivity.class);
                                startActivity(intentAbout);
                                finish();
                                overridePendingTransition(R.animator.slide_out_left, R.animator.slide_in_right);
                                return true;
                        }
                        return false;
                    }
                })
                .build();
        drawerResult.setSelectionByIdentifier(1);
    }

    private void initializeToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name);
            toolbar.setSubtitle(R.string.menu_settings);
            setSupportActionBar(toolbar);
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

            // Устанавливаем текст с ценой со знаком "рубль" на кнопку
            String btnAdsDisableText;

            if (inventory.getSkuDetails(SKU_ADS_DISABLE) != null) {
                String price = inventory.getSkuDetails(SKU_ADS_DISABLE).getPrice();
                String replacePrice = price.replace("руб.", "\u20BD");
                btnAdsDisableText = getString(R.string.button_ads_disable) + " " + replacePrice;
                CharSequence spannedBtnAdsDisableText = spanWithRoubleTypeface(btnAdsDisableText);
                btnAdsDisable.setText(spannedBtnAdsDisableText);
            } else {
                btnAdsDisableText = getString(R.string.button_ads_disable);
                btnAdsDisable.setText(btnAdsDisableText);
            }

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(APP_PREFERENCES_ADS_SHOW, (purchase != null && verifyDeveloperPayload(purchase)));
            editor.apply();
        }
    };

    // Callback когда покупка завершена
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (mHelper == null) return;

            if (result.isFailure()) {
                if(LOG_ON) {Log.v(TAG, "Error purchasing: " + result);}
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                if(LOG_ON) {Log.v(TAG,"Error purchasing. Authenticity verification failed.");}
                return;
            }

            if (purchase.getSku().equals(SKU_ADS_DISABLE)) {
                Toast.makeText(getApplicationContext(), getString(R.string.menu_ads_disable_toast), Toast.LENGTH_LONG).show();

                // Сохраняем в настройках
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(APP_PREFERENCES_ADS_SHOW, true);
                editor.apply();

                // Убираем ракламу, кнопку оплаты
                if (adView != null) {
                    adView.setVisibility(View.GONE);
                    btnAdsDisable.setVisibility(View.GONE);
                }
            }
        }
    };

    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null) return;

        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            if(LOG_ON){Log.v(TAG, "onActivityResult handled by IABUtil.");}
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
    }

    @Override
    protected void onDestroy() {
        // Если перешли в MenuActivity из MainActivity
        if (activity != null) {
            boolean checkBoxCancelValue = getSettingsParams(APP_PREFERENCES_CANCEL);
            boolean checkBoxSellValue = getSettingsParams(APP_PREFERENCES_SELL);
            boolean checkBoxLoadValue = getSettingsParams(APP_PREFERENCES_LOAD);

            // Если изменили настройки отображения расписания передаем переменные в MainActivity
            if (checkBoxCancelValue != cancel || checkBoxLoadValue != load || checkBoxSellValue != sell) {
                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                intent.putExtra("cancel", checkBoxCancelValue);
                intent.putExtra("sell", checkBoxSellValue);
                intent.putExtra("cancel_load", checkBoxLoadValue);
                intent.putExtra("day", day);

                if (code != null) {
                    intent.putExtra("code", code);
                }
                startActivity(intent);
            }
        }

        if (adView != null) {
            adView.destroy();
        }

        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
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

        String payload = "";
        mHelper.launchPurchaseFlow(this, SKU_ADS_DISABLE, RC_REQUEST, mPurchaseFinishedListener, payload);
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
        if(checkBoxValue) {
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

    // Получаем параметры из файла настроек
    private boolean getSettingsParams(String params) {
        boolean checkBoxValue;
        checkBoxValue = settings.contains(params) && settings.getBoolean(params, false);
        return checkBoxValue;
    }

    private CharSequence spanWithRoubleTypeface(String priceHint) {
        final Typeface roubleSupportedTypeface = Typeface.createFromAsset(this.getAssets(), "fonts/rouble2.ttf");

        SpannableStringBuilder resultSpan = new SpannableStringBuilder(priceHint);
        for (int i = 0; i < resultSpan.length(); i++) {
            if (resultSpan.charAt(i) == '\u20BD') {
                TypefaceSpan2 roubleTypefaceSpan = new TypefaceSpan2(roubleSupportedTypeface);
                resultSpan.setSpan(roubleTypefaceSpan, i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return resultSpan;
    }
}
