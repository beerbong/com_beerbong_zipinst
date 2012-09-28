package com.beerbong.zipinst;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class About extends PreferenceActivity {
    
	private Preference licensePref;
	private Preference sitePref;

	@Override
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.about);

		Preference versionPref = findPreference("about_version");
		try {
			versionPref.setSummary(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			versionPref.setSummary(R.string.about_version_unknown);
		}

		sitePref = findPreference("about_pref");
		licensePref = findPreference("license_pref");
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		if (preference == licensePref) {
			startActivity(new Intent(About.this, License.class));
        } else if (preference == sitePref) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/showthread.php?t=1906396")));
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }
}
