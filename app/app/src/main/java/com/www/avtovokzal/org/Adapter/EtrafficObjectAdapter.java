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

import com.www.avtovokzal.org.Object.EtrafficObject;
import com.www.avtovokzal.org.R;
import com.www.avtovokzal.org.TypefaceSpan2;

import java.util.List;

public class EtrafficObjectAdapter extends BaseAdapter{

    private List<EtrafficObject> list;
    private LayoutInflater layoutInflater;
    private Context context;

    public EtrafficObjectAdapter(Context context, List<EtrafficObject> list) {
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

    private EtrafficObject getEtrafficObject(int i) {
        return (EtrafficObject)getItem(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = layoutInflater.inflate(R.layout.listview_item_etraffic_row, viewGroup, false);
        }

        EtrafficObject etrafficObject = getEtrafficObject(i);

        TextView item0 = (TextView) view.findViewById(R.id.textViewEtrafficTime);
        TextView item1 = (TextView) view.findViewById(R.id.textViewEtrafficNumber);
        TextView item2 = (TextView) view.findViewById(R.id.textViewEtrafficName);
        TextView item3 = (TextView) view.findViewById(R.id.textViewEtrafficTimeArrival);
        TextView item4 = (TextView) view.findViewById(R.id.textViewEtrafficCountBus);
        TextView item5 = (TextView) view.findViewById(R.id.textViewEtrafficPrice);

        String priceBus = etrafficObject.getPrice() + " \u20BD";
        CharSequence spannedPriceBus = spanWithRoubleTypeface(priceBus);

        item0.setText(etrafficObject.getTime());
        item1.setText(etrafficObject.getNumber());
        item2.setText(etrafficObject.getName());
        item3.setText(etrafficObject.getTimeArrival());
        item4.setText(etrafficObject.getCountBus());
        item5.setText(spannedPriceBus);

        return view;
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
