package com.www.avtovokzal.org.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.www.avtovokzal.org.AppController;
import com.www.avtovokzal.org.Constants;
import com.www.avtovokzal.org.R;

public class KoltsovoDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.koltsovo_title))
                .setMessage(getString(R.string.koltsovo_message))
                .setIcon(R.drawable.ic_koltsovo)
                .setNegativeButton(getString(R.string.koltsovo_negative_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Tracker t = ((AppController) getActivity().getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
                        t.send(new HitBuilders.EventBuilder()
                                .setCategory(getString(R.string.analytics_category_google))
                                .setAction(getString(R.string.analytics_action_koltsovo))
                                .build());

                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean(Constants.APP_PREFERENCES_SHOW_DIALOG_KOLTSOVO, false);
                        editor.apply();

                        dialog.cancel();
                    }
                })
                .setPositiveButton(getString(R.string.koltsovo_positive_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Tracker t = ((AppController) getActivity().getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
                        t.send(new HitBuilders.EventBuilder()
                                .setCategory(getString(R.string.analytics_category_google))
                                .setAction(getString(R.string.analytics_action_koltsovo_not_interested))
                                .build());

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=ru.koltsovo.www.koltsovo"));
                        startActivity(intent);

                        dialog.cancel();
                    }
                });

        return builder.create();
    }
}
