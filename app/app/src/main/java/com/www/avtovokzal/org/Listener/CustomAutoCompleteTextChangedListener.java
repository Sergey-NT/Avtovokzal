package com.www.avtovokzal.org.Listener;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.www.avtovokzal.org.Adapter.AutocompleteCustomArrayAdapter;
import com.www.avtovokzal.org.Constants;
import com.www.avtovokzal.org.MainActivity;
import com.www.avtovokzal.org.Object.AutoCompleteObject;
import com.www.avtovokzal.org.R;

public class CustomAutoCompleteTextChangedListener implements TextWatcher {
    private Context context;

    public CustomAutoCompleteTextChangedListener(Context context) {
        this.context = context;
    }

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void beforeTextChanged(CharSequence userInput, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence userInput, int start, int before, int count) {
        try {
            if(userInput.length() > 0) {
                String tableName = "stations";

                if (Constants.LOG_ON) Log.v("Input: ", "User input: " + userInput);

                MainActivity mainActivity = ((MainActivity) context);
                mainActivity.myAdapter.notifyDataSetChanged();
                AutoCompleteObject[] myObj = mainActivity.databaseH.read(userInput.toString(), tableName);

                // обновление адапрета
                mainActivity.myAdapter = new AutocompleteCustomArrayAdapter(mainActivity, R.layout.listview_dropdown_item, myObj);
                mainActivity.myAutoComplete.setAdapter(mainActivity.myAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
