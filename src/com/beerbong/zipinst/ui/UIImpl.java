package com.beerbong.zipinst.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.StoredPreferences;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class UIImpl extends UI {

    private List<UIListener> listeners = new ArrayList();

    private PreferenceActivity activity = null;

    private PreferenceCategory fileList;
    private Preference mChooseZip;
    private Preference mInstallNow;

    protected UIImpl(PreferenceActivity activity) {
      
        redraw(activity);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void redraw(PreferenceActivity activity) {

        listeners.clear();

        this.activity = activity;

        activity.addPreferencesFromResource(R.xml.main);
      
        fileList = (PreferenceCategory)activity.findPreference(Constants.PREFERENCE_FILE_LIST);
        mChooseZip = activity.findPreference(Constants.PREFERENCE_CHOOSE_ZIP);
        mInstallNow = activity.findPreference(Constants.PREFERENCE_INSTALL_NOW);
      
        redrawPreferences();
    }
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {

        boolean retValue = false;

        if (preference == mChooseZip || preference == mInstallNow) {
            retValue = true;
        }

        dispatchOnPreferenceClicked(preference.getKey());

        return retValue;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        dispatchOnActivityResult(requestCode, resultCode, data);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        dispatchOnCreateOptionsMenu(menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        dispatchOnOptionsItemSelected(item);

        return true;
    }
    @Override
    public void addPreference(String realPath, String sdcardPath) {

        StoredPreferences.removePreference(realPath);

        Preference pref = new Preference(activity);
        pref.setKey(realPath);
        pref.setTitle(sdcardPath);
        pref.setPersistent(true);
      
        pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(final Preference preference) {
                AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                alert.setTitle(R.string.alert_file_title);

                String path = (String)preference.getTitle();
                File file = new File(path);

                String summary = path + "\n";
                summary += Constants.formatSize(file.length());

                alert.setMessage(summary);

                alert.setPositiveButton(R.string.alert_file_close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                alert.setNegativeButton(R.string.alert_file_delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        removePreference(preference);
                    }
                });

                alert.show();

                return false;
            }
        });

        fileList.addPreference(pref);

        StoredPreferences.addPreference(pref);

        redrawPreferences();
    }
    @Override
    public void addUIListener(UIListener listener) {
        listeners.add(listener);
    }
    @Override
    public void removeUIListener(UIListener listener) {
        listeners.remove(listener);
    }

    private void removePreference(Preference preference) {

        StoredPreferences.removePreference(preference.getKey());

        fileList.removePreference(preference);

        redrawPreferences();

    }
    private void dispatchOnPreferenceClicked(String id) {
        int size = listeners.size(), i = 0;
        for (;i<size;i++) {
            listeners.get(i).onPreferenceClicked(id);
        }
    }
    private void dispatchOnActivityResult(int requestCode, int resultCode, Intent data) {
        int size = listeners.size(), i = 0;
        for (;i<size;i++) {
            listeners.get(i).onActivityResult(requestCode, resultCode, data);
        }
    }
    private void dispatchOnCreateOptionsMenu(Menu menu) {
        int size = listeners.size(), i = 0;
        for (;i<size;i++) {
            listeners.get(i).onCreateOptionsMenu(menu);
        }
    }
    private void dispatchOnOptionsItemSelected(MenuItem menuItem) {
        int size = listeners.size(), i = 0;
        for (;i<size;i++) {
            listeners.get(i).onOptionsItemSelected(menuItem);
        }
    }
    private void redrawPreferences() {

        fileList.removeAll();

        for (int i=0;i<StoredPreferences.size();i++) {
            fileList.addPreference(StoredPreferences.getPreference(i));
        }
    }
}