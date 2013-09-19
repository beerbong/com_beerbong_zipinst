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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.beerbong.zipinst.MainActivity;
import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.manager.PreferencesManager;
import com.beerbong.zipinst.manager.ProManager;
import com.beerbong.zipinst.manager.ProManager.ManageMode;
import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.RecoveryInfo;
import com.beerbong.zipinst.widget.DirectoryChooserDialog;
import com.beerbong.zipinst.widget.PreferenceActivity;

public class Settings extends PreferenceActivity implements OnPreferenceChangeListener {

    private Preference mRecovery;
    private Preference mInternalSdcard;
    private Preference mExternalSdcard;
    private CheckBoxPreference mDad;
    private CheckBoxPreference mDarkTheme;
    private CheckBoxPreference mCheckExists;
    private CheckBoxPreference mCheckMd5;
    private CheckBoxPreference mOverrideList;
    private CheckBoxPreference mAutoloadList;
    private CheckBoxPreference mSystemWipeAlert;
    private Preference mDownloadPath;
    private ListPreference mZipPosition;
    private MultiSelectListPreference mOptions;
    private ListPreference mSpaceLeft;
    private CheckBoxPreference mUseFolder;
    private Preference mFolder;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState, R.layout.settings);

        mRecovery = findPreference(Constants.PREFERENCE_SETTINGS_RECOVERY);
        mInternalSdcard = findPreference(Constants.PREFERENCE_SETTINGS_INTERNAL_SDCARD);
        mExternalSdcard = findPreference(Constants.PREFERENCE_SETTINGS_EXTERNAL_SDCARD);
        mDad = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_DAD);
        mDarkTheme = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_DARK_THEME);
        mCheckExists = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_CHECK_EXISTS);
        mCheckMd5 = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_CHECK_MD5);
        mOverrideList = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_OVERRIDE_LIST);
        mAutoloadList = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_AUTOLOAD_LIST);
        mDownloadPath = findPreference(Constants.PREFERENCE_SETTINGS_DOWNLOAD_PATH);
        mZipPosition = (ListPreference) findPreference(Constants.PREFERENCE_SETTINGS_ZIP_POSITION);
        mOptions = (MultiSelectListPreference) findPreference(Constants.PREFERENCE_SETTINGS_OPTIONS);
        mSpaceLeft = (ListPreference) findPreference(Constants.PREFERENCE_SETTINGS_SPACE_LEFT);
        mSystemWipeAlert = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_SYSTEMWIPE_ALERT);
        mUseFolder = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SETTINGS_USE_FOLDER);
        mFolder = findPreference(Constants.PREFERENCE_SETTINGS_FOLDER);

        PreferencesManager pManager = ManagerFactory.getPreferencesManager();

        mDad.setChecked(pManager.isUseDragAndDrop());

        mDarkTheme.setChecked(pManager.isDarkTheme());

        mCheckExists.setChecked(pManager.isCheckExists());

        mCheckMd5.setChecked(pManager.isCheckMD5());

        mOverrideList.setChecked(pManager.isOverrideList());

        mAutoloadList.setChecked(pManager.isAutoloadList());

        mZipPosition.setValue(pManager.getZipPosition());
        mZipPosition.setOnPreferenceChangeListener(this);

        mOptions.setValues(pManager.getShowOptions());

        mSpaceLeft.setValue(String.valueOf(pManager.getSpaceLeft()));
        mSpaceLeft.setOnPreferenceChangeListener(this);

        mSystemWipeAlert.setChecked(pManager.isShowSystemWipeAlert());

        mUseFolder.setChecked(pManager.isUseFolder());
        
        if (!ManagerFactory.getFileManager().hasExternalStorage()) {
            getPreferenceScreen().removePreference(mExternalSdcard);
        }

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

            selectRecovery();

        } else if (Constants.PREFERENCE_SETTINGS_INTERNAL_SDCARD.equals(key)) {

            selectSdcard(true);

        } else if (Constants.PREFERENCE_SETTINGS_EXTERNAL_SDCARD.equals(key)) {

            selectSdcard(false);

        } else if (Constants.PREFERENCE_SETTINGS_DAD.equals(key)) {

            boolean useDad = ((CheckBoxPreference) preference).isChecked();
            pManager.setUseDragAndDrop(useDad);

            UI.getInstance().removeAllItems();

        } else if (Constants.PREFERENCE_SETTINGS_DARK_THEME.equals(key)) {

            boolean darkTheme = ((CheckBoxPreference) preference).isChecked();
            pManager.setDarkTheme(darkTheme);

            showRestartDialog();

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

            selectFolder(pManager.getDownloadPath(), new DirectoryChooserDialog.DirectoryChooserListener() {

                @Override
                public void onDirectoryChosen(String chosenDir) {
                    ManagerFactory.getPreferencesManager().setDownloadPath(chosenDir);
                    updateSummaries();
                }
            });

        } else if (Constants.PREFERENCE_SETTINGS_FOLDER.equals(key)) {

            selectFolder(pManager.getFolder(), new DirectoryChooserDialog.DirectoryChooserListener() {

                @Override
                public void onDirectoryChosen(String chosenDir) {
                    ManagerFactory.getPreferencesManager().setFolder(chosenDir);
                    updateSummaries();
                }
            });

        } else if (Constants.PREFERENCE_SETTINGS_SYSTEMWIPE_ALERT.equals(key)) {

            boolean showAlert = ((CheckBoxPreference) preference).isChecked();
            pManager.setShowSystemWipeAlert(showAlert);

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

        PreferencesManager pManager = ManagerFactory.getPreferencesManager();

        if (Constants.PREFERENCE_SETTINGS_SPACE_LEFT.equals(key)) {

            pManager.setSpaceLeft(Double.parseDouble(newValue.toString()));

        } else if (Constants.PREFERENCE_SETTINGS_ZIP_POSITION.equals(key)) {

            pManager.setZipPosition((String)newValue);

        }
        return false;
    }

    private void updateSummaries() {
        RecoveryInfo info = ManagerFactory.getRecoveryManager().getRecovery();
        PreferencesManager pManager = ManagerFactory.getPreferencesManager();
        mRecovery.setSummary(getResources().getText(R.string.recovery_summary) + " ("
                + info.getName() + ")");
        mInternalSdcard.setSummary(getResources().getText(R.string.internalsdcard_summary) + " ("
                + pManager.getInternalStorage() + ")");
        mExternalSdcard.setSummary(getResources().getText(R.string.externalsdcard_summary) + " ("
                + pManager.getExternalStorage() + ")");
        mDownloadPath.setSummary(pManager.getDownloadPath());
        mFolder.setSummary(pManager.getFolder());
    }

    private void selectFolder(String defaultValue,
            DirectoryChooserDialog.DirectoryChooserListener listener) {
        new DirectoryChooserDialog(this, listener).chooseDirectory(defaultValue);
    }

    private void selectSdcard(final boolean internal) {
        final PreferencesManager pManager = ManagerFactory.getPreferencesManager();
        
        final EditText input = new EditText(this);
        input.setText(internal ? pManager.getInternalStorage() : pManager.getExternalStorage());

        new AlertDialog.Builder(this)
                .setTitle(R.string.sdcard_alert_title)
                .setMessage(R.string.sdcard_alert_summary)
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();

                        if (value == null || "".equals(value.trim())) {
                            Toast.makeText(Settings.this, R.string.sdcard_alert_error,
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }

                        if (value.startsWith("/")) {
                            value = value.substring(1);
                        }

                        if (internal) {
                            pManager.setInternalStorage(value);
                        } else {
                            pManager.setExternalStorage(value);
                        }
                        updateSummaries();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void selectRecovery() {
        View view = LayoutInflater.from(this).inflate(R.layout.recovery,
                (ViewGroup) findViewById(R.id.recovery_layout));

        RadioButton cbCwmbased = (RadioButton) view.findViewById(R.id.cwmbased);
        RadioButton cbTwrp = (RadioButton) view.findViewById(R.id.twrp);
        RadioButton cb4ext = (RadioButton) view.findViewById(R.id.fourext);

        final RadioGroup mGroup = (RadioGroup) view.findViewById(R.id.recovery_radio_group);

        RecoveryInfo info = ManagerFactory.getRecoveryManager().getRecovery();
        switch (info.getId()) {
            case R.id.cwmbased:
                cbCwmbased.setChecked(true);
                break;
            case R.id.twrp:
                cbTwrp.setChecked(true);
                break;
            case R.id.fourext:
                cb4ext.setChecked(true);
                break;
        }

        new AlertDialog.Builder(this).setTitle(R.string.recovery_alert_title)
                .setMessage(R.string.recovery_alert_summary).setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        int id = mGroup.getCheckedRadioButtonId();

                        ManagerFactory.getRecoveryManager().setRecovery(id);

                        updateSummaries();

                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void showRestartDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.restart_needed);
        alert.setMessage(R.string.restart_needed_theme_message);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                dialog.dismiss();

                Intent intent = new Intent(Settings.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                Settings.this.startActivity(intent);

                Settings.this.finish();
            }
        });
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
}