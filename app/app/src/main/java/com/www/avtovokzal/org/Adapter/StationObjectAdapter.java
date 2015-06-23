package com.www.avtovokzal.org.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.www.avtovokzal.org.Object.StationsObject;
import com.www.avtovokzal.org.R;

import java.util.List;

public class StationObjectAdapter extends BaseAdapter {

    private List<StationsObject> list;
    private LayoutInflater layoutInflater;

    public StationObjectAdapter(Context context, List<StationsObject> list) {
        this.list = list;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if(view == null) {
            view = layoutInflater.inflate(R.layout.listview_item_row, viewGroup, false);
        }

        StationsObject stationsObject = getStationObject(i);

        TextView item0 = (TextView) view.findViewById(R.id.textViewMainTime);
        TextView item1 = (TextView) view.findViewById(R.id.textViewMainNumber);
        TextView item2 = (TextView) view.findViewById(R.id.textViewMainName);
        TextView item3 = (TextView) view.findViewById(R.id.textViewMainNameBus);
        TextView item4 = (TextView) view.findViewById(R.id.textViewMainCountBus);
        TextView item5 = (TextView) view.findViewById(R.id.textViewMainFreeBus);
        TextView item6 = (TextView) view.findViewById(R.id.textViewMainCancel);

        if (stationsObject.getFreeBus().equals("нет данных") || stationsObject.getFreeBus().equals("мест нет")) {
            item5.setTextColor(Color.parseColor("#90A4AE"));
        } else {
            item5.setTextColor(Color.parseColor("#000000"));
        }

        if (stationsObject.getCancelBus() == 1){
            view.findViewById(R.id.textViewMainNameBusDesc).setVisibility(View.GONE);
            view.findViewById(R.id.textViewMainCountBusDesc).setVisibility(View.GONE);
            view.findViewById(R.id.textViewMainFreeBusDesc).setVisibility(View.GONE);
            item3.setVisibility(View.GONE);
            item4.setVisibility(View.GONE);
            item5.setVisibility(View.GONE);
            item6.setVisibility(View.VISIBLE);
            view.findViewById(R.id.listViewMainItem).setBackgroundColor(Color.parseColor("#CFD8DC"));
        } else {
            view.findViewById(R.id.textViewMainNameBusDesc).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textViewMainCountBusDesc).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textViewMainFreeBusDesc).setVisibility(View.VISIBLE);
            item3.setVisibility(View.VISIBLE);
            item4.setVisibility(View.VISIBLE);
            item5.setVisibility(View.VISIBLE);
            item6.setVisibility(View.INVISIBLE);
            view.findViewById(R.id.listViewMainItem).setBackgroundColor(Color.parseColor("#ECEFF1"));
        }

        item0.setText(stationsObject.getTimeOtpr());
        item1.setText(stationsObject.getNumberMarsh());
        item2.setText(stationsObject.getMarshName());
        item3.setText(stationsObject.getNameBus());
        item4.setText(stationsObject.getCountBus());
        item5.setText(stationsObject.getFreeBus());

        item1.setTag(stationsObject.getNumberMarshToSend());

        return view;
    }

    private StationsObject getStationObject(int i) {
        return (StationsObject)getItem(i);
    }
}
