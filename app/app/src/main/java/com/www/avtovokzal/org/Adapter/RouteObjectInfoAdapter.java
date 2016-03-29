package com.www.avtovokzal.org.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.www.avtovokzal.org.Object.RouteObjectInfo;
import com.www.avtovokzal.org.R;
import com.www.avtovokzal.org.TypefaceSpan2;

import java.util.List;

public class RouteObjectInfoAdapter extends BaseAdapter{

    private List<RouteObjectInfo> list;
    private LayoutInflater layoutInflater;
    private Context context;

    public RouteObjectInfoAdapter(Context context, List<RouteObjectInfo> list) {
        this.context = context;
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
        private TextView description4;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if(view == null) {
            view = layoutInflater.inflate(R.layout.listview_item_info_row, viewGroup, false);

            holder = new ViewHolder();

            holder.item0 = (TextView) view.findViewById(R.id.textViewInfoNameStation);
            holder.item1 = (TextView) view.findViewById(R.id.textViewInfoStationNote);
            holder.item2 = (TextView) view.findViewById(R.id.textViewInfoTimePrib);
            holder.item3 = (TextView) view.findViewById(R.id.textViewInfoTimeWay);
            holder.item4 = (TextView) view.findViewById(R.id.textViewInfoDistance);
            holder.item5 = (TextView) view.findViewById(R.id.textViewInfoPriceBus);
            holder.item6 = (TextView) view.findViewById(R.id.textViewInfoBaggageBus);
            holder.description1 = (TextView) view.findViewById(R.id.textViewInfoNoteStationDesc);
            holder.description2 = (TextView) view.findViewById(R.id.textViewInfoInfoTimeWayDesc);
            holder.description3 = (TextView) view.findViewById(R.id.textViewInfoTimePribDesc);
            holder.description4 = (TextView) view.findViewById(R.id.textViewInfoDistanceDesc);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        RouteObjectInfo routeObjectInfo = getRouteObjectInfo(i);

        String priceBus = routeObjectInfo.getPriceBus() + " \u20BD";
        String priceBaggage = routeObjectInfo.getBaggageBus() + " \u20BD";
        String distanceData = routeObjectInfo.getDistanceData() + " км";

        CharSequence spannedPriceBus = spanWithRoubleTypeface(priceBus);
        CharSequence spannedPriceBaggage = spanWithRoubleTypeface(priceBaggage);

        if (routeObjectInfo.getNoteStation().length() == 0){
            holder.description1.setVisibility(View.GONE);
            holder.item1.setVisibility(View.GONE);
        } else {
            holder.description1.setVisibility(View.VISIBLE);
            holder.item1.setVisibility(View.VISIBLE);
        }

        if (routeObjectInfo.getTimeWay().equals("00:00")){
            holder.description2.setVisibility(View.GONE);
            holder.description3.setVisibility(View.GONE);
            holder.item2.setVisibility(View.GONE);
            holder.item3.setVisibility(View.GONE);
        } else {
            holder.description2.setVisibility(View.VISIBLE);
            holder.description3.setVisibility(View.VISIBLE);
            holder.item2.setVisibility(View.VISIBLE);
            holder.item3.setVisibility(View.VISIBLE);
        }

        if(routeObjectInfo.getDistanceData().equals("0.0")){
            holder.description4.setVisibility(View.GONE);
            holder.item4.setVisibility(View.GONE);
        } else {
            holder.description4.setVisibility(View.VISIBLE);
            holder.item4.setVisibility(View.VISIBLE);
        }

        holder.item0.setText(routeObjectInfo.getNameStation());
        holder.item1.setText(routeObjectInfo.getNoteStation());
        holder.item2.setText(routeObjectInfo.getTimePrib());
        holder.item3.setText(routeObjectInfo.getTimeWay());
        holder.item4.setText(distanceData);
        holder.item5.setText(spannedPriceBus);
        holder.item6.setText(spannedPriceBaggage);

        holder.item0.setTag(routeObjectInfo.getCodeStation());

        return view;
    }

    private RouteObjectInfo getRouteObjectInfo(int i) {
        return (RouteObjectInfo)getItem(i);
    }

    private CharSequence spanWithRoubleTypeface(String priceHint) {
        final Typeface roubleSupportedTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/rouble2.ttf");

        SpannableStringBuilder resultSpan = new SpannableStringBuilder(priceHint);
        for (int i = 0; i < resultSpan.length(); i++) {
            if (resultSpan.charAt(i) == '\u20BD') {
                TypefaceSpan2 roubleTypefaceSpan = new TypefaceSpan2(roubleSupportedTypeface);
                resultSpan.setSpan(roubleTypefaceSpan, i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return resultSpan;
    }
}
