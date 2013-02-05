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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.manager.PreferencesManager;
import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.RecoveryInfo;
import com.beerbong.zipinst.widget.PreferenceActivity;

public class Settings extends PreferenceActivity {

    private Preference mRecovery;
    private Preference mSdcard;
    private CheckBoxPreference mDad;
    private CheckBoxPreference mShowBackup;
    private CheckBoxPreference mDarkTheme;
    private CheckBoxPreference mCheckExists;
    private CheckBoxPreference mCheckMd5;
    private CheckBoxPreference mOverrideList;
    private Preference mDownloadPath;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState, R.layout.settings);

        mRecovery = findPreference(Constants.PREFERENCE_SETTINGS_RECOVERY);
        mSdcard = findPreference(Constants.PREFERENCE_SETTINGS_SDCARD);
        mDad = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_DAD);
        mShowBackup = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_SHOW_BACKUP);
        mDarkTheme = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_DARK_THEME);
        mCheckExists = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_CHECK_EXISTS);
        mCheckMd5 = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_CHECK_MD5);
        mOverrideList = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_OVERRIDE_LIST);
        mDownloadPath = findPreference(Constants.PREFERENCE_SETTINGS_DOWNLOAD_PATH);

        PreferencesManager pManager = ManagerFactory.getPreferencesManager();

        mDad.setChecked(pManager.isUseDragAndDrop());

        mShowBackup.setChecked(pManager.isShowBackupOption());

        mDarkTheme.setChecked(pManager.isDarkTheme());

        mCheckExists.setChecked(pManager.isCheckExists());

        mCheckMd5.setChecked(pManager.isCheckMD5());

        mOverrideList.setChecked(pManager.isOverrideList());

        if (!ManagerFactory.getProManager().iAmPro()) {
            PreferenceCategory category = (PreferenceCategory)findPreference("settings_update");
            category.removePreference(findPreference("updates"));
            category.removePreference(findPreference(Constants.PREFERENCE_SETTINGS_CHECK_UPDATE_STARTUP));
        }

        updateSummaries();
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

        } else if (Constants.PREFERENCE_SETTINGS_SHOW_BACKUP.equals(key)) {

            boolean showBackup = ((CheckBoxPreference) preference).isChecked();
            pManager.setShowBackupOption(showBackup);

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

        } else if (Constants.PREFERENCE_SETTINGS_DOWNLOAD_PATH.equals(key)) {

            ManagerFactory.getFileManager().selectDownloadPath(this);
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
        RecoveryInfo info = ManagerFactory.getRecoveryManager().getRecovery();
        mRecovery.setSummary(getResources().getText(R.string.recovery_summary) + " ("
                + info.getName() + ")");
        mSdcard.setSummary(getResources().getText(R.string.sdcard_summary) + " ("
                + info.getSdcard() + ")");
        mDownloadPath.setSummary(ManagerFactory.getPreferencesManager().getDownloadPath());
    }
}