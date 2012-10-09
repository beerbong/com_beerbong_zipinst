package com.beerbong.zipinst.activities;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.util.Constants;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class About extends PreferenceActivity {

    private Preference changelogPref;
    private Preference licensePref;
    private Preference sitePref;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.about);

        Preference versionPref = findPreference(Constants.PREFERENCE_ABOUT_VERSION);
        try {
            versionPref.setSummary(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (NameNotFoundException e) {
            versionPref.setSummary(R.string.about_version_unknown);
        }

        sitePref = findPreference(Constants.PREFERENCE_ABOUT_SITE);
        licensePref = findPreference(Constants.PREFERENCE_ABOUT_LICENSE);
        changelogPref = findPreference(Constants.PREFERENCE_ABOUT_CHANGELOG);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == licensePref) {
            startActivity(new Intent(About.this, License.class));
        } else if (preference == changelogPref) {
            startActivity(new Intent(About.this, Changelog.class));
        } else if (preference == sitePref) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.ABOUT_URL)));
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }
}
