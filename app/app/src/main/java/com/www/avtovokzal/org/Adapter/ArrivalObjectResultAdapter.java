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

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = layoutInflater.inflate(R.layout.listview_item_arrival_result_row, viewGroup, false);
        }

        ArrivalObjectResult arrivalObjectResult = getArrivalObjectResult(i);

        TextView item0 = (TextView) view.findViewById(R.id.textViewArrivalTimeOtpr);
        TextView item1 = (TextView) view.findViewById(R.id.textViewArrivalNumber);
        TextView item2 = (TextView) view.findViewById(R.id.textViewArrivalName);
        TextView item3 = (TextView) view.findViewById(R.id.textViewArrivalTimePrib);
        TextView item4 = (TextView) view.findViewById(R.id.textViewArrivalSchedule);

        item0.setText(arrivalObjectResult.getTimeOtpr());
        item1.setText(arrivalObjectResult.getNumberMarsh());
        item2.setText(arrivalObjectResult.getNameMarsh());
        item3.setText(arrivalObjectResult.getTimePrib());
        item4.setText(arrivalObjectResult.getScheduleMarsh());

        item3.setTag(arrivalObjectResult.getTimeFromStation());

        return view;
    }
}
