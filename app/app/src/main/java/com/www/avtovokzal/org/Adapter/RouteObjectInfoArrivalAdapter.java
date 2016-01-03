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

    static class ViewHolder {
        private TextView item0;
        private TextView item1;
        private TextView item2;
        private TextView description1;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if(view == null) {
            view = layoutInflater.inflate(R.layout.listview_item_info_arrival_row, viewGroup, false);

            holder = new ViewHolder();

            holder.item0 = (TextView) view.findViewById(R.id.textViewInfoArrivalNameStation);
            holder.item1 = (TextView) view.findViewById(R.id.textViewInfoArrivalStationNote);
            holder.item2 = (TextView) view.findViewById(R.id.textViewInfoArrivalTimeOtpr);
            holder.description1 = (TextView) view.findViewById(R.id.textViewInfoArrivalNoteStationDesc);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        RouteObjectInfoArrival routeObjectInfoArrival = getRouteObjectInfoArrival(i);

        if (routeObjectInfoArrival.getNoteStation().length() == 0){
            holder.description1.setVisibility(View.GONE);
            holder.item1.setVisibility(View.GONE);
        } else {
            holder.description1.setVisibility(View.VISIBLE);
            holder.item1.setVisibility(View.VISIBLE);
        }

        holder.item0.setText(routeObjectInfoArrival.getNameStation());
        holder.item1.setText(routeObjectInfoArrival.getNoteStation());
        holder.item2.setText(routeObjectInfoArrival.getTimeOtpr());

        holder.item0.setTag(routeObjectInfoArrival.getCode());

        return view;
    }

    private RouteObjectInfoArrival getRouteObjectInfoArrival(int i) {
        return (RouteObjectInfoArrival)getItem(i);
    }
}