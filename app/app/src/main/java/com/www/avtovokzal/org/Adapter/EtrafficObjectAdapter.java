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

    static class ViewHolder {
        private TextView item0;
        private TextView item1;
        private TextView item2;
        private TextView item3;
        private TextView item4;
        private TextView item5;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.listview_item_etraffic_row, viewGroup, false);

            holder = new ViewHolder();

            holder.item0 = (TextView) view.findViewById(R.id.textViewEtrafficTime);
            holder.item1 = (TextView) view.findViewById(R.id.textViewEtrafficNumber);
            holder.item2 = (TextView) view.findViewById(R.id.textViewEtrafficName);
            holder.item3 = (TextView) view.findViewById(R.id.textViewEtrafficTimeArrival);
            holder.item4 = (TextView) view.findViewById(R.id.textViewEtrafficCountBus);
            holder.item5 = (TextView) view.findViewById(R.id.textViewEtrafficPrice);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        EtrafficObject etrafficObject = getEtrafficObject(i);

        String priceBus = etrafficObject.getPrice() + " \u20BD";
        CharSequence spannedPriceBus = spanWithRoubleTypeface(priceBus);

        holder.item0.setText(etrafficObject.getTime());
        holder.item1.setText(etrafficObject.getNumber());
        holder.item2.setText(etrafficObject.getName());
        holder.item3.setText(etrafficObject.getTimeArrival());
        holder.item4.setText(etrafficObject.getCountBus());
        holder.item5.setText(spannedPriceBus);

        holder.item0.setTag(etrafficObject.getUrlToBuy());

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
