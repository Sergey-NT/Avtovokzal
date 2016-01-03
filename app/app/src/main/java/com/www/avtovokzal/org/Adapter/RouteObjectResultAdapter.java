package com.www.avtovokzal.org.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.www.avtovokzal.org.Object.RouteObjectResult;
import com.www.avtovokzal.org.R;
import com.www.avtovokzal.org.TypefaceSpan2;

import java.util.List;

public class RouteObjectResultAdapter extends BaseAdapter{

    private List<RouteObjectResult> list;
    private LayoutInflater layoutInflater;
    private Context myContext;


    public RouteObjectResultAdapter(Context context, List<RouteObjectResult> list) {
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
        private TextView item7;
        private TextView item8;
        private TextView item9;
        private TextView description1;
        private TextView description2;
        private TextView description3;
        private TextView description4;
        private TextView description5;
        private TextView description6;
        private RelativeLayout relativeLayout;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if(view == null) {
            view = layoutInflater.inflate(R.layout.listview_item_result_row, viewGroup, false);

            holder = new ViewHolder();

            holder.item0 = (TextView) view.findViewById(R.id.textViewMainResultTime);
            holder.item1 = (TextView) view.findViewById(R.id.textViewMainResultNumber);
            holder.item2 = (TextView) view.findViewById(R.id.textViewMainResultName);
            holder.item3 = (TextView) view.findViewById(R.id.textViewMainResultNameBus);
            holder.item4 = (TextView) view.findViewById(R.id.textViewMainResultCountBus);
            holder.item5 = (TextView) view.findViewById(R.id.textViewMainResultFreeBus);
            holder.item6 = (TextView) view.findViewById(R.id.textViewMainResultPriceBus);
            holder.item7 = (TextView) view.findViewById(R.id.textViewMainResultBaggageBus);
            holder.item8 = (TextView) view.findViewById(R.id.textViewMainResultTimePrib);
            holder.item9 = (TextView) view.findViewById(R.id.textViewMainResultCancel);
            holder.description1 = (TextView) view.findViewById(R.id.textViewMainResultTimePribDesc);
            holder.description2 = (TextView) view.findViewById(R.id.textViewMainResultNameBusDesc);
            holder.description3 = (TextView) view.findViewById(R.id.textViewMainResultCountBusDesc);
            holder.description4 = (TextView) view.findViewById(R.id.textViewMainResultFreeBusDesc);
            holder.description5 = (TextView) view.findViewById(R.id.textViewMainResultPriceBusDesc);
            holder.description6 = (TextView) view.findViewById(R.id.textViewMainResultBaggageBusDesc);
            holder.relativeLayout = (RelativeLayout) view.findViewById(R.id.listViewMainResultItem);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        RouteObjectResult routeObjectResult = getRouteObjectResult(i);

        if (routeObjectResult.getCancelBus() == 1){
            holder.description1.setVisibility(View.GONE);
            holder.description2.setVisibility(View.GONE);
            holder.description3.setVisibility(View.GONE);
            holder.description4.setVisibility(View.GONE);
            holder.description5.setVisibility(View.GONE);
            holder.description6.setVisibility(View.GONE);
            holder.item3.setVisibility(View.GONE);
            holder.item4.setVisibility(View.GONE);
            holder.item5.setVisibility(View.GONE);
            holder.item6.setVisibility(View.GONE);
            holder.item7.setVisibility(View.GONE);
            holder.item8.setVisibility(View.GONE);
            holder.item9.setVisibility(View.VISIBLE);
            holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(myContext, R.color.divider_color));
        } else {
            holder.description1.setVisibility(View.VISIBLE);
            holder.description2.setVisibility(View.VISIBLE);
            holder.description3.setVisibility(View.VISIBLE);
            holder.description4.setVisibility(View.VISIBLE);
            holder.description5.setVisibility(View.VISIBLE);
            holder.description6.setVisibility(View.VISIBLE);
            holder.item3.setVisibility(View.VISIBLE);
            holder.item4.setVisibility(View.VISIBLE);
            holder.item5.setVisibility(View.VISIBLE);
            holder.item6.setVisibility(View.VISIBLE);
            holder.item7.setVisibility(View.VISIBLE);
            holder.item8.setVisibility(View.VISIBLE);
            holder.item9.setVisibility(View.INVISIBLE);
            holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(myContext, R.color.background));
        }

        if(routeObjectResult.getFreeBus().equals("нет данных") || routeObjectResult.getFreeBus().equals("мест нет")) {
            holder.item5.setTextColor(ContextCompat.getColor(myContext, R.color.text_color_hint));
        } else {
            holder.item5.setTextColor(ContextCompat.getColor(myContext, R.color.text_color));
        }

        String priceBus = routeObjectResult.getPriceBus() + " \u20BD";
        String priceBaggage = routeObjectResult.getBaggageBus() + " \u20BD";

        CharSequence spannedPriceBus = spanWithRoubleTypeface(priceBus);
        CharSequence spannedPriceBaggage = spanWithRoubleTypeface(priceBaggage);

        holder.item0.setText(routeObjectResult.getTimeOtpr());
        holder.item1.setText(routeObjectResult.getNumberMarsh());
        holder.item2.setText(routeObjectResult.getMarshName());
        holder.item3.setText(routeObjectResult.getNameBus());
        holder.item4.setText(routeObjectResult.getCountBus());
        holder.item5.setText(routeObjectResult.getFreeBus());
        holder.item6.setText(spannedPriceBus);
        holder.item7.setText(spannedPriceBaggage);
        holder.item8.setText(routeObjectResult.getTimePrib());

        holder.item1.setTag(routeObjectResult.getNumberMarshToSend());

        return view;
    }

    private RouteObjectResult getRouteObjectResult(int i) {
        return (RouteObjectResult)getItem(i);
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



