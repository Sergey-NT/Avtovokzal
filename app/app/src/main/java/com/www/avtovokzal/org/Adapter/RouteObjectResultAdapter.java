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

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if(view == null) {
            view = layoutInflater.inflate(R.layout.listview_item_result_row, viewGroup, false);
        }

        RouteObjectResult routeObjectResult = getRouteObjectResult(i);

        TextView item0 = (TextView) view.findViewById(R.id.textViewMainResultTime);
        TextView item1 = (TextView) view.findViewById(R.id.textViewMainResultNumber);
        TextView item2 = (TextView) view.findViewById(R.id.textViewMainResultName);
        TextView item3 = (TextView) view.findViewById(R.id.textViewMainResultNameBus);
        TextView item4 = (TextView) view.findViewById(R.id.textViewMainResultCountBus);
        TextView item5 = (TextView) view.findViewById(R.id.textViewMainResultFreeBus);
        TextView item6 = (TextView) view.findViewById(R.id.textViewMainResultPriceBus);
        TextView item7 = (TextView) view.findViewById(R.id.textViewMainResultBaggageBus);
        TextView item8 = (TextView) view.findViewById(R.id.textViewMainResultTimePrib);
        TextView item9 = (TextView) view.findViewById(R.id.textViewMainResultCancel);

        if (routeObjectResult.getCancelBus() == 1){
            view.findViewById(R.id.textViewMainResultTimePribDesc).setVisibility(View.GONE);
            view.findViewById(R.id.textViewMainResultNameBusDesc).setVisibility(View.GONE);
            view.findViewById(R.id.textViewMainResultCountBusDesc).setVisibility(View.GONE);
            view.findViewById(R.id.textViewMainResultFreeBusDesc).setVisibility(View.GONE);
            view.findViewById(R.id.textViewMainResultPriceBusDesc).setVisibility(View.GONE);
            view.findViewById(R.id.textViewMainResultBaggageBusDesc).setVisibility(View.GONE);
            item3.setVisibility(View.GONE);
            item4.setVisibility(View.GONE);
            item5.setVisibility(View.GONE);
            item6.setVisibility(View.GONE);
            item7.setVisibility(View.GONE);
            item8.setVisibility(View.GONE);
            item9.setVisibility(View.VISIBLE);
            view.findViewById(R.id.listViewMainResultItem).setBackgroundColor(myContext.getResources().getColor(R.color.divider_color));
        } else {
            view.findViewById(R.id.textViewMainResultTimePribDesc).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textViewMainResultNameBusDesc).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textViewMainResultCountBusDesc).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textViewMainResultFreeBusDesc).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textViewMainResultPriceBusDesc).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textViewMainResultBaggageBusDesc).setVisibility(View.VISIBLE);
            item3.setVisibility(View.VISIBLE);
            item4.setVisibility(View.VISIBLE);
            item5.setVisibility(View.VISIBLE);
            item6.setVisibility(View.VISIBLE);
            item7.setVisibility(View.VISIBLE);
            item8.setVisibility(View.VISIBLE);
            item9.setVisibility(View.INVISIBLE);
            view.findViewById(R.id.listViewMainResultItem).setBackgroundColor(myContext.getResources().getColor(R.color.background));
        }

        if(routeObjectResult.getFreeBus().equals("нет данных") || routeObjectResult.getFreeBus().equals("мест нет")) {
            item5.setTextColor(myContext.getResources().getColor(R.color.text_color_hint));
        } else {
            item5.setTextColor(myContext.getResources().getColor(R.color.text_color));
        }

        String priceBus = routeObjectResult.getPriceBus() + " \u20BD";
        String priceBaggage = routeObjectResult.getBaggageBus() + " \u20BD";

        CharSequence spannedPriceBus = spanWithRoubleTypeface(priceBus);
        CharSequence spannedPriceBaggage = spanWithRoubleTypeface(priceBaggage);

        item0.setText(routeObjectResult.getTimeOtpr());
        item1.setText(routeObjectResult.getNumberMarsh());
        item2.setText(routeObjectResult.getMarshName());
        item3.setText(routeObjectResult.getNameBus());
        item4.setText(routeObjectResult.getCountBus());
        item5.setText(routeObjectResult.getFreeBus());
        item6.setText(spannedPriceBus);
        item7.setText(spannedPriceBaggage);
        item8.setText(routeObjectResult.getTimePrib());

        item1.setTag(routeObjectResult.getNumberMarshToSend());

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



