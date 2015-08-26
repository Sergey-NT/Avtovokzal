package com.www.avtovokzal.org.Listener;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.www.avtovokzal.org.Adapter.AutocompleteCustomArrayAdapter;
import com.www.avtovokzal.org.ArrivalActivity;
import com.www.avtovokzal.org.Object.AutoCompleteObject;
import com.www.avtovokzal.org.R;

public class ArrivalAutoCompleteTextChangedListener implements TextWatcher {
    Context context;
    private final static boolean LOG_ON = false;

    public ArrivalAutoCompleteTextChangedListener (Context context) {
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

                ArrivalActivity arrivalActivity = ((ArrivalActivity) context);
                arrivalActivity.myAdapter.notifyDataSetChanged();
                AutoCompleteObject[] myObj = arrivalActivity.databaseH.read(userInput.toString(), tableName);

                // обновление адапрета
                arrivalActivity.myAdapter = new AutocompleteCustomArrayAdapter(arrivalActivity, R.layout.listview_dropdown_item, myObj);
                arrivalActivity.myAutoComplete.setAdapter(arrivalActivity.myAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
