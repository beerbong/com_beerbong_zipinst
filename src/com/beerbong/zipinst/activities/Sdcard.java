package com.beerbong.zipinst.activities;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.util.Constants;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class Sdcard extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final EditText input = new EditText(this);
        input.setText(getSharedPreferences(Constants.PREFS_NAME, 0).getString(Constants.PROPERTY_INTERNAL_STORAGE, Constants.DEFAULT_INTERNAL_STORAGE));

        new AlertDialog.Builder(Sdcard.this)
            .setTitle(R.string.sdcard_alert_title)
            .setMessage(R.string.sdcard_alert_summary)
            .setView(input)
            .setPositiveButton(R.string.sdcard_alert_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = input.getText().toString();

                    if (value == null || "".equals(value.trim())) {
                        Toast.makeText(Sdcard.this, R.string.sdcard_alert_error, Toast.LENGTH_SHORT).show();
                        Sdcard.this.finish();
                        return;
                    }

                    if (value.startsWith("/")) {
                        value = value.substring(1);
                    }

                    SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(Constants.PROPERTY_INTERNAL_STORAGE, value);
                    editor.commit();
                    Sdcard.this.finish();
                }
            }).setNegativeButton(R.string.sdcard_alert_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Sdcard.this.finish();
                }
            }).show();
    }
}