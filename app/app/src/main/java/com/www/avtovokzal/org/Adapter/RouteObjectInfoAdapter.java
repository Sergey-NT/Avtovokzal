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
    private Context myContext;

    public RouteObjectInfoAdapter(Context context, List<RouteObjectInfo> list) {
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
            view = layoutInflater.inflate(R.layout.listview_item_info_row, viewGroup, false);
        }

        RouteObjectInfo routeObjectInfo = getRouteObjectInfo(i);

        String priceBus = routeObjectInfo.getPriceBus() + " \u20BD";
        String priceBaggage = routeObjectInfo.getBaggageBus() + " \u20BD";
        String distanceData = routeObjectInfo.getDistanceData() + " км";

        CharSequence spannedPriceBus = spanWithRoubleTypeface(priceBus);
        CharSequence spannedPriceBaggage = spanWithRoubleTypeface(priceBaggage);

        TextView item0 = (TextView) view.findViewById(R.id.textViewInfoNameStation);
        TextView item1 = (TextView) view.findViewById(R.id.textViewInfoStationNote);
        TextView item2 = (TextView) view.findViewById(R.id.textViewInfoTimePrib);
        TextView item3 = (TextView) view.findViewById(R.id.textViewInfoTimeWay);
        TextView item4 = (TextView) view.findViewById(R.id.textViewInfoDistance);
        TextView item5 = (TextView) view.findViewById(R.id.textViewInfoPriceBus);
        TextView item6 = (TextView) view.findViewById(R.id.textViewInfoBaggageBus);

        if (routeObjectInfo.getNoteStation().length() == 0){
            view.findViewById(R.id.textViewInfoNoteStationDesc).setVisibility(View.GONE);
            item1.setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.textViewInfoNoteStationDesc).setVisibility(View.VISIBLE);
            item1.setVisibility(View.VISIBLE);
        }

        if (routeObjectInfo.getTimeWay().equals("00:00")){
            view.findViewById(R.id.textViewInfoInfoTimeWayDesc).setVisibility(View.GONE);
            view.findViewById(R.id.textViewInfoTimePribDesc).setVisibility(View.GONE);
            item2.setVisibility(View.GONE);
            item3.setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.textViewInfoInfoTimeWayDesc).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textViewInfoTimePribDesc).setVisibility(View.VISIBLE);
            item2.setVisibility(View.VISIBLE);
            item3.setVisibility(View.VISIBLE);
        }

        if(routeObjectInfo.getDistanceData().equals("0.0")){
            view.findViewById(R.id.textViewInfoDistanceDesc).setVisibility(View.GONE);
            item4.setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.textViewInfoDistanceDesc).setVisibility(View.VISIBLE);
            item4.setVisibility(View.VISIBLE);
        }

        item0.setText(routeObjectInfo.getNameStation());
        item1.setText(routeObjectInfo.getNoteStation());
        item2.setText(routeObjectInfo.getTimePrib());
        item3.setText(routeObjectInfo.getTimeWay());
        item4.setText(distanceData);
        item5.setText(spannedPriceBus);
        item6.setText(spannedPriceBaggage);

        item0.setTag(routeObjectInfo.getCodeStation());

        return view;
    }

    private RouteObjectInfo getRouteObjectInfo(int i) {
        return (RouteObjectInfo)getItem(i);
    }

    private CharSequence spanWithRoubleTypeface(String priceHint) {
        final Typeface roubleSupportedTypeface = Typeface.createFromAsset(myContext.getAssets(), "fonts/rouble2.ttf");

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
