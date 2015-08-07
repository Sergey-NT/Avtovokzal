package com.www.avtovokzal.org.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.www.avtovokzal.org.Object.GgmObject;
import com.www.avtovokzal.org.R;

import java.util.List;

public class GgmObjectAdapter extends BaseAdapter{

    private List<GgmObject> list;
    private LayoutInflater layoutInflater;

    public GgmObjectAdapter(Context context, List<GgmObject> list) {
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

    private GgmObject getGgmObject(int i) {
        return (GgmObject)getItem(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = layoutInflater.inflate(R.layout.listview_item_ggm_row, viewGroup, false);
        }

        GgmObject ggmObject = getGgmObject(i);

        TextView item0 = (TextView) view.findViewById(R.id.textViewGgmTime);
        TextView item1 = (TextView) view.findViewById(R.id.textViewGgmNumber);
        TextView item2 = (TextView) view.findViewById(R.id.textViewGgmName);
        TextView item3 = (TextView) view.findViewById(R.id.textViewGgmTimeArrival);
        TextView item4 = (TextView) view.findViewById(R.id.textViewGgmCountBus);
        TextView item5 = (TextView) view.findViewById(R.id.textViewGgmPrice);

        item0.setText(ggmObject.getTime());
        item1.setText(ggmObject.getNumber());
        item2.setText(ggmObject.getName());
        item3.setText(ggmObject.getTimeArrival());
        item4.setText(ggmObject.getCountBus());
        item5.setText(ggmObject.getPrice());

        return view;
    }
}
