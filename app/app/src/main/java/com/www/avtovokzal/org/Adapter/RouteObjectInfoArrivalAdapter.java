package com.www.avtovokzal.org.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.www.avtovokzal.org.Object.RouteObjectInfoArrival;
import com.www.avtovokzal.org.R;

import java.util.List;

public class RouteObjectInfoArrivalAdapter extends BaseAdapter {

    private List<RouteObjectInfoArrival> list;
    private LayoutInflater layoutInflater;
    Context myContext;

    public RouteObjectInfoArrivalAdapter(Context context, List<RouteObjectInfoArrival> list) {
        myContext = context;
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
            view = layoutInflater.inflate(R.layout.listview_item_info_arrival_row, viewGroup, false);
        }

        RouteObjectInfoArrival routeObjectInfoArrival = getRouteObjectInfoArrival(i);

        TextView item0 = (TextView) view.findViewById(R.id.textViewInfoArrivalNameStation);
        TextView item1 = (TextView) view.findViewById(R.id.textViewInfoArrivalStationNote);
        TextView item2 = (TextView) view.findViewById(R.id.textViewInfoArrivalTimeOtpr);

        if (routeObjectInfoArrival.getNoteStation().length() == 0){
            view.findViewById(R.id.textViewInfoArrivalNoteStationDesc).setVisibility(View.GONE);
            item1.setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.textViewInfoArrivalNoteStationDesc).setVisibility(View.VISIBLE);
            item1.setVisibility(View.VISIBLE);
        }

        item0.setText(routeObjectInfoArrival.getNameStation());
        item1.setText(routeObjectInfoArrival.getNoteStation());
        item2.setText(routeObjectInfoArrival.getTimeOtpr());

        item0.setTag(routeObjectInfoArrival.getCode());

        return view;
    }

    private RouteObjectInfoArrival getRouteObjectInfoArrival(int i) {
        return (RouteObjectInfoArrival)getItem(i);
    }
}