package com.beerbong.zipinst.activities;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.Manager;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.RecoveryInfo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class Settings extends PreferenceActivity {

    private Preference mRecovery;
    private Preference mSdcard;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        mRecovery = findPreference(Constants.PREFERENCE_SETTINGS_RECOVERY);
        mSdcard = findPreference(Constants.PREFERENCE_SETTINGS_SDCARD);
        
        updateSummaries();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        
        if ("recovery".equals(key)) {
            Manager.getRecoveryManager().selectRecovery(this);
            updateSummaries();
        } else if ("sdcard".equals(key)) {
            Manager.getRecoveryManager().selectSdcard(this);
            updateSummaries();
        } else if ("about".equals(key)) {
            Intent i = new Intent(this, About.class);
            startActivity(i);
        } else if ("donate".equals(key)) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.DONATE_URL));
            startActivity(i);
        }
        
        return true;
    }
    
    private void updateSummaries() {
        RecoveryInfo info = Manager.getRecoveryManager().getRecovery();
        mRecovery.setSummary(getResources().getText(R.string.recovery_summary) + " (" + info.getName() + ")");
        mSdcard.setSummary(getResources().getText(R.string.sdcard_summary) + " (" + info.getSdcard() + ")");
    }
}