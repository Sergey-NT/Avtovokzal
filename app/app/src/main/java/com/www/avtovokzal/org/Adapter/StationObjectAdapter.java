package com.www.avtovokzal.org.Adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.www.avtovokzal.org.Object.StationsObject;
import com.www.avtovokzal.org.R;

import java.util.List;

public class StationObjectAdapter extends BaseAdapter {

    private List<StationsObject> list;
    private LayoutInflater layoutInflater;
    private Context myContext;

    public StationObjectAdapter(Context context, List<StationsObject> list) {
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
        private TextView item3;
        private TextView item4;
        private TextView item5;
        private TextView item6;
        private TextView description1;
        private TextView description2;
        private TextView description3;
        private RelativeLayout relativeLayout;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if(view == null) {
            view = layoutInflater.inflate(R.layout.listview_item_row, viewGroup, false);

            holder = new ViewHolder();

            holder.item0 = (TextView) view.findViewById(R.id.textViewMainTime);
            holder.item1 = (TextView) view.findViewById(R.id.textViewMainNumber);
            holder.item2 = (TextView) view.findViewById(R.id.textViewMainName);
            holder.item3 = (TextView) view.findViewById(R.id.textViewMainNameBus);
            holder.item4 = (TextView) view.findViewById(R.id.textViewMainCountBus);
            holder.item5 = (TextView) view.findViewById(R.id.textViewMainFreeBus);
            holder.item6 = (TextView) view.findViewById(R.id.textViewMainCancel);
            holder.description1 = (TextView) view.findViewById(R.id.textViewMainNameBusDesc);
            holder.description2 = (TextView) view.findViewById(R.id.textViewMainCountBusDesc);
            holder.description3 = (TextView) view.findViewById(R.id.textViewMainFreeBusDesc);
            holder.relativeLayout = (RelativeLayout) view.findViewById(R.id.listViewMainItem);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        StationsObject stationsObject = getStationObject(i);

        if (stationsObject.getFreeBus().equals("нет данных") || stationsObject.getFreeBus().equals("мест нет")) {
            holder.item5.setTextColor(ContextCompat.getColor(myContext, R.color.text_color_hint));
        } else {
            holder.item5.setTextColor(ContextCompat.getColor(myContext, R.color.text_color));
        }

        if (stationsObject.getCancelBus() == 1){
            holder.description1.setVisibility(View.GONE);
            holder.description2.setVisibility(View.GONE);
            holder.description3.setVisibility(View.GONE);
            holder.item3.setVisibility(View.GONE);
            holder.item4.setVisibility(View.GONE);
            holder.item5.setVisibility(View.GONE);
            holder.item6.setVisibility(View.VISIBLE);
            holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(myContext, R.color.divider_color));
        } else {
            holder.description1.setVisibility(View.VISIBLE);
            holder.description2.setVisibility(View.VISIBLE);
            holder.description3.setVisibility(View.VISIBLE);
            holder.item3.setVisibility(View.VISIBLE);
            holder.item4.setVisibility(View.VISIBLE);
            holder.item5.setVisibility(View.VISIBLE);
            holder.item6.setVisibility(View.INVISIBLE);
            holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(myContext, R.color.background));
        }

        holder.item0.setText(stationsObject.getTimeOtpr());
        holder.item1.setText(stationsObject.getNumberMarsh());
        holder.item2.setText(stationsObject.getMarshName());
        holder.item3.setText(stationsObject.getNameBus());
        holder.item4.setText(stationsObject.getCountBus());
        holder.item5.setText(stationsObject.getFreeBus());

        holder.item1.setTag(stationsObject.getNumberMarshToSend());

        return view;
    }

    private StationsObject getStationObject(int i) {
        return (StationsObject)getItem(i);
    }
}
