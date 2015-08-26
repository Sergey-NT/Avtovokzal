package com.www.avtovokzal.org.Listener;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.www.avtovokzal.org.Adapter.AutocompleteCustomArrayAdapter;
import com.www.avtovokzal.org.MenuActivity;
import com.www.avtovokzal.org.Object.AutoCompleteObject;
import com.www.avtovokzal.org.R;

public class MenuAutoCompleteTextChangedListener implements TextWatcher {
    Context context;
    private final static boolean LOG_ON = false;

    public MenuAutoCompleteTextChangedListener(Context context) {
        this.context = context;
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence userInput, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence userInput, int start, int before, int count) {
        try {
            if(userInput.length() > 0) {
                String tableName = "stations";

                if (LOG_ON) Log.v("Input: ", "User input: " + userInput);

                MenuActivity menuActivity = ((MenuActivity) context);
                menuActivity.myAdapter.notifyDataSetChanged();
                AutoCompleteObject[] myObj = menuActivity.databaseH.read(userInput.toString(), tableName);

                // обновление адапрета
                menuActivity.myAdapter = new AutocompleteCustomArrayAdapter(menuActivity, R.layout.listview_dropdown_item, myObj);
                menuActivity.myAutoComplete.setAdapter(menuActivity.myAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
