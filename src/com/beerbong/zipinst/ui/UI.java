package com.beerbong.zipinst.ui;

import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public abstract class UI {

    private static UI instance = null;

    public static synchronized void create(PreferenceActivity activity) {
        if (instance == null) {
            instance = new UIImpl(activity);
        } else {
            instance.redraw(activity);
        }
    }
    public static synchronized UI getInstance() {
        return instance;
    }

    public abstract void addPreference(String realPath, String sdcardPath);
    public abstract void redraw(PreferenceActivity activity);
    public abstract boolean onPreferenceTreeClick(Preference preference);
    public abstract void addUIListener(UIListener listener);
    public abstract void removeUIListener(UIListener listener);
    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);
    public abstract boolean onCreateOptionsMenu(Menu menu);
    public abstract boolean onOptionsItemSelected(MenuItem item);
}