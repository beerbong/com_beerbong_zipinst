/*
 * Copyright (C) 2013 ZipInstaller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.beerbong.zipinst.activities;

import com.beerbong.zipinst.R;
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

        Preference versionPref = findPreference(Constants.PREFERENCE_ABOUT_VERSION);
        try {
            versionPref
                    .setSummary(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (NameNotFoundException e) {
            versionPref.setSummary(R.string.about_version_unknown);
        }
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
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }
}
