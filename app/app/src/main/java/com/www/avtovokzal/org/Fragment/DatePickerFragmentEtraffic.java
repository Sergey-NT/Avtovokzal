package com.www.avtovokzal.org.Fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.www.avtovokzal.org.EtrafficActivity;

import java.util.Calendar;
import java.util.Locale;

public class DatePickerFragmentEtraffic extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        if (!Locale.getDefault().getLanguage().equals("US") && isBrokenSamsungDevice()){
            Configuration config = new Configuration();
            config.locale = new Locale("US");
            getActivity().getApplicationContext().getResources().updateConfiguration(config, null);
        }

        return new DatePickerDialog(getActivity(), (EtrafficActivity)getActivity(), year, month, day);
    }

    private static boolean isBrokenSamsungDevice() {
        return (Build.MANUFACTURER.equalsIgnoreCase("samsung")
                && isBetweenAndroidVersions(Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.LOLLIPOP_MR1));
    }

    private static boolean isBetweenAndroidVersions(int min, int max) {
        return Build.VERSION.SDK_INT >= min && Build.VERSION.SDK_INT <= max;
    }
}