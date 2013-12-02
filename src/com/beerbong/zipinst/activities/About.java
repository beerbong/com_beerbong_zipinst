/*
 * Copyright 2013 ZipInstaller Project
 *
 * This file is part of ZipInstaller.
 *
 * ZipInstaller is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ZipInstaller is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZipInstaller.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.beerbong.zipinst.activities;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.widget.PreferenceActivity;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

public class About extends PreferenceActivity {

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState, R.layout.about);

        Preference pref = findPreference(Constants.PREFERENCE_ABOUT_VERSION);
        try {
            pref.setSummary(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (NameNotFoundException e) {
            pref.setSummary(R.string.about_version_unknown);
        }

        pref = findPreference(Constants.PREFERENCE_ABOUT_DONATE);
        pref.setTitle(ManagerFactory.getProManager().iAmPro() ? R.string.donate_title
                : R.string.become_a_pro);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if (Constants.PREFERENCE_ABOUT_LICENSE.equals(key)) {
            startActivity(new Intent(About.this, License.class));
        } else if (Constants.PREFERENCE_ABOUT_CHANGELOG.equals(key)) {
            startActivity(new Intent(About.this, Changelog.class));
        } else if (Constants.PREFERENCE_ABOUT_SITE.equals(key)) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.ABOUT_URL)));
        } else if (Constants.PREFERENCE_ABOUT_DONATE.equals(key)) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ManagerFactory.getProManager()
                    .iAmPro() ? Constants.DONATE_URL : Constants.PRO_URL)));
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }
}
