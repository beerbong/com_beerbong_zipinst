/*
 * Copyright 2014 ZipInstaller Project
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

package com.beerbong.zipinst.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.plugins.license.LicensePlugin;
import com.beerbong.zipinst.core.plugins.update.UpdatePlugin;
import com.beerbong.zipinst.io.Files;
import com.beerbong.zipinst.ui.UIPreferenceFragment;

public class FragmentAbout extends UIPreferenceFragment {

    private static final String DONATE_URL = "http://forum.xda-developers.com/donatetome.php?u=1806623";
    private static final String PRO_URL = "https://play.google.com/store/apps/details?id=com.beerbong.zipinst";

    private static final String PREFERENCE_VERSION = "about_version";
    private static final String PREFERENCE_SITE = "about_pref";
    private static final String PREFERENCE_LICENSE = "license_pref";
    private static final String PREFERENCE_CHANGELOG = "changelog_pref";
    private static final String PREFERENCE_UPDATE = "updates_pref";
    private static final String PREFERENCE_DONATE = "donate_pref";

    @Override
    public int[] getVisibleMenuItems() {
        return null;
    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_about;
    }

    @Override
    public void create(boolean isNew) {

        Preference pref = findPreference(PREFERENCE_VERSION);
        pref.setSummary(getCore().getVersion().toString());

        LicensePlugin plugin = (LicensePlugin) getCore().getPlugin(Core.PLUGIN_LICENSE);
        boolean purchased = plugin.isPurchased();

        pref = findPreference(PREFERENCE_DONATE);
        pref.setTitle(purchased ? R.string.donate_title
                : R.string.become_a_pro);

        if (purchased) {
            PreferenceCategory cat = (PreferenceCategory) findPreference("about_category");
            cat.removePreference(findPreference(PREFERENCE_UPDATE));
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        String key = preference.getKey();

        if (PREFERENCE_LICENSE.equals(key)) {
            showDocument("license.html", R.string.license_title, R.string.license_error);
        } else if (PREFERENCE_CHANGELOG.equals(key)) {
            showDocument("changelog.html", R.string.changelog_title, R.string.changelog_error);
        } else if (PREFERENCE_SITE.equals(key)) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getCore().getContext()
                    .getResources().getString(R.string.about_zip_summary))));
        } else if (PREFERENCE_DONATE.equals(key)) {
            LicensePlugin plugin = (LicensePlugin) getCore().getPlugin(Core.PLUGIN_LICENSE);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(plugin.isPurchased() ? DONATE_URL : PRO_URL)));
        } else if (PREFERENCE_UPDATE.equals(key)) {
            UpdatePlugin plugin = (UpdatePlugin) getCore().getPlugin(Core.PLUGIN_UPDATE);
            plugin.checkApplication();
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }

    @Override
    public void restore(Bundle savedInstanceState) {
    }

    @Override
    public void save(Bundle outState) {
    }

    @Override
    public int getTitle() {
        return R.string.about_title;
    }

    private void showDocument(String document, final int title, int onError) {
        final Context context = getCore().getContext();

        String data = Files.readAssets(context, document);

        if (data == null) {
            Toast.makeText(context, onError, Toast.LENGTH_LONG).show();
            return;
        }

        WebView webView = new WebView(context);

        webView.loadDataWithBaseURL(null, data, "text/html", "utf-8", null);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context); 
                alert.setTitle(title);
                alert.setView(view);
                alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        });
    }

}
