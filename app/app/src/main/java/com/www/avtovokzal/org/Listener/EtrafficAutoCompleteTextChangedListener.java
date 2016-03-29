package com.www.avtovokzal.org.Listener;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.www.avtovokzal.org.Adapter.AutocompleteCustomArrayAdapter;
import com.www.avtovokzal.org.Constants;
import com.www.avtovokzal.org.EtrafficMainActivity;
import com.www.avtovokzal.org.Object.AutoCompleteObject;
import com.www.avtovokzal.org.R;

public class EtrafficAutoCompleteTextChangedListener implements TextWatcher {
    private Context context;

    public EtrafficAutoCompleteTextChangedListener(Context context) {
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
                String tableName = "stations_ekb";

                if (Constants.LOG_ON) Log.v("Input: ", "User input: " + userInput);

                EtrafficMainActivity etrafficMainActivity = ((EtrafficMainActivity) context);
                etrafficMainActivity.myAdapter.notifyDataSetChanged();
                AutoCompleteObject[] myObj = etrafficMainActivity.databaseH.read(userInput.toString(), tableName);

                // обновление адапрета
                etrafficMainActivity.myAdapter = new AutocompleteCustomArrayAdapter(etrafficMainActivity, R.layout.listview_dropdown_item, myObj);
                etrafficMainActivity.myAutoComplete.setAdapter(etrafficMainActivity.myAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
