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
import android.preference.PreferenceGroup;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class UIImpl extends UI {
    
    private static final String TAG = "UIImpl";

    private List<UIListener> listeners = new ArrayList<UIListener>();

    private PreferenceActivity activity = null;

    private PreferenceCategory fileList;
    
    private int mCount = 0;
    
    private TouchInterceptor.DropListener mDropListener = new TouchInterceptor.DropListener() {
        public void drop(int from, int to) {
            Log.d(TAG, "DROP " + from + ", " + to + ", " + mCount);
            if (to < mCount) return;
            StoredPreferences.move(from - mCount, to - mCount);
            redrawPreferences();
        }
    };

    protected UIImpl(PreferenceActivity activity) {
      
        redraw(activity);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void redraw(PreferenceActivity activity) {

        listeners.clear();

        this.activity = activity;

        activity.addPreferencesFromResource(R.xml.main);
        
        mCount = countPreferences(activity.getPreferenceScreen());
        
        Log.d(TAG, "COUNT = " + mCount);
      
        fileList = (PreferenceCategory)activity.findPreference(Constants.PREFERENCE_FILE_LIST);
        fileList.setOrderingAsAdded(true);
        
        activity.setContentView(R.xml.list);
        
        ((TouchInterceptor)activity.getListView()).setDropListener(mDropListener);
      
        redrawPreferences();
    }
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {

        dispatchOnPreferenceClicked(preference.getKey());

        return false;
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
        
        boolean useDad = activity.getSharedPreferences(Constants.PREFS_NAME, 0).getBoolean(Constants.PROPERTY_DRAG_AND_DROP, Constants.DEFAULT_DRAG_AND_DROP);
        
        Preference pref = null;
        
        if (useDad) {

            pref = new ZipPreference(activity);
            pref.setLayoutResource(R.xml.order_power_widget_button_list_item);
            ((ZipPreference)pref).setName(sdcardPath);
            
        } else {
            
            pref = new Preference(activity);
            
        }
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
    @Override
    public void removeAllPreferences() {
        
        StoredPreferences.removePreferences();
        
        fileList.removeAll();
        
        redrawPreferences();
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
            Preference pref = StoredPreferences.getPreference(i);
            pref.setOrder(Preference.DEFAULT_ORDER);
            fileList.addPreference(StoredPreferences.getPreference(i));
        }
    }
    private int countPreferences(PreferenceGroup group) {
        int children = group.getPreferenceCount();
        int count = children;
        if (count > 0) {
            for (int i=0;i<count;i++) {
                Preference pref = group.getPreference(i);
                if (pref instanceof PreferenceGroup) {
                    children += countPreferences((PreferenceGroup)pref);
                }
            }
        }
        return children;
    }
}