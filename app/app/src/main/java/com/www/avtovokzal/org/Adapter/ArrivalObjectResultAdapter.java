package com.www.avtovokzal.org.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.www.avtovokzal.org.Object.ArrivalObjectResult;
import com.www.avtovokzal.org.R;

import java.util.List;

public class ArrivalObjectResultAdapter extends BaseAdapter {

    private List<ArrivalObjectResult> list;
    private LayoutInflater layoutInflater;

    public ArrivalObjectResultAdapter(Context context, List<ArrivalObjectResult> list) {
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

    private ArrivalObjectResult getArrivalObjectResult(int i) {
        return (ArrivalObjectResult)getItem(i);
    }

    static class ViewHolder {
        private TextView item0;
        private TextView item1;
        private TextView item2;
        private TextView item3;
        private TextView item4;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.listview_item_arrival_result_row, viewGroup, false);

            holder = new ViewHolder();

            holder.item0 = (TextView) view.findViewById(R.id.textViewArrivalTimeOtpr);
            holder.item1 = (TextView) view.findViewById(R.id.textViewArrivalNumber);
            holder.item2 = (TextView) view.findViewById(R.id.textViewArrivalName);
            holder.item3 = (TextView) view.findViewById(R.id.textViewArrivalTimePrib);
            holder.item4 = (TextView) view.findViewById(R.id.textViewArrivalSchedule);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        ArrivalObjectResult arrivalObjectResult = getArrivalObjectResult(i);

        holder.item0.setText(arrivalObjectResult.getTimeOtpr());
        holder.item1.setText(arrivalObjectResult.getNumberMarsh());
        holder.item2.setText(arrivalObjectResult.getNameMarsh());
        holder.item3.setText(arrivalObjectResult.getTimePrib());
        holder.item4.setText(arrivalObjectResult.getScheduleMarsh());

        holder.item3.setTag(arrivalObjectResult.getTimeFromStation());

        return view;
    }
}
