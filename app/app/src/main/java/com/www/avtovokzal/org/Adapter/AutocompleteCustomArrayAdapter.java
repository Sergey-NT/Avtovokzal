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

    private Context context;
    private int layoutResourceId;
    private AutoCompleteObject data[] = null;

    public AutocompleteCustomArrayAdapter(Context context, int layoutResourceId, AutoCompleteObject[] data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    static class ViewHolder {
        private TextView textViewItem;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(layoutResourceId, viewGroup, false);

            holder = new ViewHolder();

            holder.textViewItem = (TextView) view.findViewById(R.id.textViewDropDownItem);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        AutoCompleteObject objectItem = data[i];

        holder.textViewItem.setText(objectItem.objectName);
        holder.textViewItem.setTag(objectItem.objectCode);

        return view;
    }
}
