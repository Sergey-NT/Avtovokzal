package com.www.avtovokzal.org.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.www.avtovokzal.org.Constants;
import com.www.avtovokzal.org.R;

public class UpdateDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_title_new_version))
                .setMessage(getString(R.string.dialog_message_new_version))
                .setIcon(R.drawable.ic_launcher)
                .setNegativeButton(getString(R.string.dialog_button_negative_update), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean(Constants.APP_PREFERENCES_CANCEL_CHECK_VERSION, true);
                        editor.apply();

                        dialog.cancel();
                        getActivity().finish();
                    }
                })
                .setPositiveButton(getString(R.string.dialog_button_positive_update), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=details?id=com.www.avtovokzal.org"));
                        startActivity(intent);

                        dialog.cancel();
                        getActivity().finish();
                    }
                });

        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        getActivity().finish();
    }
}
