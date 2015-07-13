package com.www.avtovokzal.org.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.www.avtovokzal.org.Object.AutoCompleteObject;
import com.www.avtovokzal.org.R;

public class AutocompleteCustomArrayAdapter extends ArrayAdapter<AutoCompleteObject> {

    Context mContext;
    int layoutResourceId;
    AutoCompleteObject data[] = null;

    public AutocompleteCustomArrayAdapter(Context mContext, int layoutResourceId, AutoCompleteObject[] data) {
        super(mContext, layoutResourceId, data);
        this.mContext = mContext;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        try {
            if (convertView==null){
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }

            AutoCompleteObject objectItem = data[position];

            TextView textViewItem = (TextView) convertView.findViewById(R.id.textViewDropDownItem);
            textViewItem.setText(objectItem.objectName);
            textViewItem.setTag(objectItem.objectCode);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }
}
