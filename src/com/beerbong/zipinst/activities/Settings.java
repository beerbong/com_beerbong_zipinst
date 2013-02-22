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

import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.manager.PreferencesManager;
import com.beerbong.zipinst.manager.ProManager;
import com.beerbong.zipinst.manager.ProManager.ManageMode;
import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.RecoveryInfo;
import com.beerbong.zipinst.widget.PreferenceActivity;

public class Settings extends PreferenceActivity implements OnPreferenceChangeListener {

    private Preference mRecovery;
    private Preference mSdcard;
    private CheckBoxPreference mDad;
    private CheckBoxPreference mDarkTheme;
    private CheckBoxPreference mCheckExists;
    private CheckBoxPreference mCheckMd5;
    private CheckBoxPreference mOverrideList;
    private CheckBoxPreference mAutoloadList;
    private Preference mDownloadPath;
    private ListPreference mZipPosition;
    private ListPreference mOptions;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState, R.xml.settings);

        mRecovery = findPreference(Constants.PREFERENCE_SETTINGS_RECOVERY);
        mSdcard = findPreference(Constants.PREFERENCE_SETTINGS_SDCARD);
        mDad = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_DAD);
        mDarkTheme = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_DARK_THEME);
        mCheckExists = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_CHECK_EXISTS);
        mCheckMd5 = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_CHECK_MD5);
        mOverrideList = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_OVERRIDE_LIST);
        mAutoloadList = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_AUTOLOAD_LIST);
        mDownloadPath = findPreference(Constants.PREFERENCE_SETTINGS_DOWNLOAD_PATH);
        mZipPosition = (ListPreference) findPreference(Constants.PREFERENCE_SETTINGS_ZIP_POSITION);
        mOptions = (ListPreference) findPreference(Constants.PREFERENCE_SETTINGS_OPTIONS);

        PreferencesManager pManager = ManagerFactory.getPreferencesManager();

        mDad.setChecked(pManager.isUseDragAndDrop());

        mDarkTheme.setChecked(pManager.isDarkTheme());

        mCheckExists.setChecked(pManager.isCheckExists());

        mCheckMd5.setChecked(pManager.isCheckMD5());

        mOverrideList.setChecked(pManager.isOverrideList());

        mAutoloadList.setChecked(pManager.isAutoloadList());

        mZipPosition.setValue(pManager.getZipPosition());

        mOptions.setValue(pManager.getShowOptions());
        mOptions.setOnPreferenceChangeListener(this);

        ProManager proManager = ManagerFactory.getProManager();

        if (proManager.iAmPro()) {
            PreferenceCategory category = (PreferenceCategory) findPreference("settings_update");
            category.removePreference(findPreference("updates"));
            category.removePreference(findPreference(Constants.PREFERENCE_SETTINGS_CHECK_UPDATE_STARTUP));
        } else {
            findPreference("donate").setTitle(R.string.become_a_pro);
        }

        updateSummaries();

        proManager.manage(this, ManageMode.Settings);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();

        PreferencesManager pManager = ManagerFactory.getPreferencesManager();

        if (Constants.PREFERENCE_SETTINGS_RECOVERY.equals(key)) {

            ManagerFactory.getRecoveryManager().selectRecovery(this);
            updateSummaries();

        } else if (Constants.PREFERENCE_SETTINGS_SDCARD.equals(key)) {

            ManagerFactory.getRecoveryManager().selectSdcard(this);
            updateSummaries();

        } else if (Constants.PREFERENCE_SETTINGS_DAD.equals(key)) {

            boolean useDad = ((CheckBoxPreference) preference).isChecked();
            pManager.setUseDragAndDrop(useDad);

            UI.getInstance().removeAllItems();

        } else if (Constants.PREFERENCE_SETTINGS_DARK_THEME.equals(key)) {

            boolean darkTheme = ((CheckBoxPreference) preference).isChecked();
            pManager.setDarkTheme(darkTheme);

            UI.getInstance().requestRestart();

        } else if (Constants.PREFERENCE_SETTINGS_CHECK_EXISTS.equals(key)) {

            boolean checkExists = ((CheckBoxPreference) preference).isChecked();
            pManager.setCheckExists(checkExists);

        } else if ("updates".equals(key)) {

            ManagerFactory.getUpdateManager().checkForUpdate(this);

        } else if (Constants.PREFERENCE_SETTINGS_CHECK_UPDATE_STARTUP.equals(key)) {

            boolean checkUpdate = ((CheckBoxPreference) preference).isChecked();
            pManager.setCheckUpdatesStartup(checkUpdate);

        } else if (Constants.PREFERENCE_SETTINGS_CHECK_MD5.equals(key)) {

            boolean checkMd5 = ((CheckBoxPreference) preference).isChecked();
            pManager.setCheckMD5(checkMd5);

        } else if (Constants.PREFERENCE_SETTINGS_OVERRIDE_LIST.equals(key)) {

            boolean overrideList = ((CheckBoxPreference) preference).isChecked();
            pManager.setOverrideList(overrideList);

        } else if (Constants.PREFERENCE_SETTINGS_AUTOLOAD_LIST.equals(key)) {

            boolean autoloadList = ((CheckBoxPreference) preference).isChecked();
            pManager.setAutoloadList(autoloadList);

        } else if (Constants.PREFERENCE_SETTINGS_DOWNLOAD_PATH.equals(key)) {

            ManagerFactory.getFileManager().selectDownloadPath(this);
            updateSummaries();

        } else if (Constants.PREFERENCE_SETTINGS_ZIP_POSITION.equals(key)) {

            String zipPosition = ((ListPreference) preference).getValue();
            pManager.setZipPosition(zipPosition);

        } else if ("about".equals(key)) {

            Intent i = new Intent(this, About.class);
            startActivity(i);

        } else if ("donate".equals(key)) {

            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(ManagerFactory.getProManager()
                    .iAmPro() ? Constants.DONATE_URL : Constants.PRO_URL));
            startActivity(i);

        }

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();

        if (Constants.PREFERENCE_SETTINGS_OPTIONS.equals(key)) {
            List<String> values = (List<String>) newValue;
            String result = "";
            for (int i = 0; i < values.size(); i++) {
                result += values.get(i);
                if (i < values.size() - 1)
                    result += "|";
            }
            ManagerFactory.getPreferencesManager().setShowOptions(result);
        }
        return false;
    }

    private void updateSummaries() {
        RecoveryInfo info = ManagerFactory.getRecoveryManager().getRecovery();
        mRecovery.setSummary(getResources().getText(R.string.recovery_summary) + " ("
                + info.getName() + ")");
        mSdcard.setSummary(getResources().getText(R.string.sdcard_summary) + " ("
                + info.getSdcard() + ")");
        mDownloadPath.setSummary(ManagerFactory.getPreferencesManager().getDownloadPath());
    }
}