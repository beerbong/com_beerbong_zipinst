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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.beerbong.zipinst.MainActivity;
import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.plugins.license.LicensePlugin;
import com.beerbong.zipinst.core.plugins.recovery.RecoveryInfo;
import com.beerbong.zipinst.core.plugins.recovery.RecoveryPlugin;
import com.beerbong.zipinst.core.plugins.storage.StoragePlugin;
import com.beerbong.zipinst.core.plugins.superuser.SuperUserPlugin;
import com.beerbong.zipinst.core.plugins.update.RomUpdater;
import com.beerbong.zipinst.io.SystemProperties;
import com.beerbong.zipinst.preferences.Preferences;
import com.beerbong.zipinst.store.FileItemStore;
import com.beerbong.zipinst.ui.UIPreferenceFragment;
import com.beerbong.zipinst.ui.widget.FolderPicker;

public class FragmentSettings extends UIPreferenceFragment implements OnPreferenceChangeListener {

    private static final String PREFERENCE_CATEGORY_RECOVERY = "settings_recovery";
    private static final String PREFERENCE_CATEGORY_UPDATE = "settings_update";
    private static final String PREFERENCE_RECOVERY = "recovery";
    private static final String PREFERENCE_INTERNAL_SDCARD = "internalsdcard";
    private static final String PREFERENCE_EXTERNAL_SDCARD = "externalsdcard";
    private static final String PREFERENCE_FORCE_EXTERNAL_SDCARD = "mountextsdcard";
    private static final String PREFERENCE_FORCE_DATA_MEDIA = "forcedatamedia";
    private static final String PREFERENCE_BACKUP_EXTERNAL_SDCARD = "backupextsdcard";
    private static final String PREFERENCE_DAD = "draganddrop";
    private static final String PREFERENCE_DARK_THEME = "darktheme";
    private static final String PREFERENCE_CHECK_EXISTS = "checkexists";
    private static final String PREFERENCE_CHECK_UPDATE_STARTUP = "updateonstartup";
    private static final String PREFERENCE_CHECK_MD5 = "checkmd5";
//    private static final String PREFERENCE_OVERRIDE_LIST = "overridelist";
//    private static final String PREFERENCE_AUTOLOAD_LIST = "autoloadlist";
    private static final String PREFERENCE_DOWNLOAD_PATH = "downloadpath";
    private static final String PREFERENCE_ZIP_POSITION = "zipposition";
    private static final String PREFERENCE_OPTIONS = "show-options";
    private static final String PREFERENCE_SPACE_LEFT = "spaceleft";
    private static final String PREFERENCE_SYSTEMWIPE_ALERT = "wipesystemalert";
    private static final String PREFERENCE_USE_FOLDER = "usefolder";
    private static final String PREFERENCE_FOLDER = "folder";
    private static final String PREFERENCE_CHECK_ROOT = "checkroot";
    private static final String PREFERENCE_TIME_NOTIFICATIONS = "time_notifications";
    private static final String PREFERENCE_CHECK_ROM_UPDATES = "check_rom_updates";

    private Preference mRecovery;
    private Preference mInternalSdcard;
    private Preference mExternalSdcard;
    private CheckBoxPreference mForceExternalSdcard;
    private CheckBoxPreference mForceDataMedia;
    private CheckBoxPreference mBackupExternalSdcard;
    private CheckBoxPreference mDad;
    private CheckBoxPreference mDarkTheme;
    private CheckBoxPreference mCheckExists;
    private CheckBoxPreference mCheckMd5;
    //    private CheckBoxPreference mOverrideList;
    //    private CheckBoxPreference mAutoloadList;
    private CheckBoxPreference mSystemWipeAlert;
    private CheckBoxPreference mUpdatesOnStartup;
    private Preference mDownloadPath;
    private ListPreference mZipPosition;
    private MultiSelectListPreference mOptions;
    private ListPreference mSpaceLeft;
    private CheckBoxPreference mUseFolder;
    private Preference mFolder;
    private Preference mCheckRom;
    private ListPreference mCheckTime;

    private RomUpdater mRomUpdater;

    @Override
    public int getContentViewId() {
        return R.layout.fragment_settings;
    }

    @Override
    public void create(boolean isNew) {

        mRecovery = findPreference(PREFERENCE_RECOVERY);
        mInternalSdcard = findPreference(PREFERENCE_INTERNAL_SDCARD);
        mExternalSdcard = findPreference(PREFERENCE_EXTERNAL_SDCARD);
        mForceExternalSdcard = (CheckBoxPreference) findPreference(PREFERENCE_FORCE_EXTERNAL_SDCARD);
        mForceDataMedia = (CheckBoxPreference) findPreference(PREFERENCE_FORCE_DATA_MEDIA);
        mBackupExternalSdcard = (CheckBoxPreference) findPreference(PREFERENCE_BACKUP_EXTERNAL_SDCARD);
        mDad = (CheckBoxPreference) findPreference(PREFERENCE_DAD);
        mDarkTheme = (CheckBoxPreference) findPreference(PREFERENCE_DARK_THEME);
        mCheckExists = (CheckBoxPreference) findPreference(PREFERENCE_CHECK_EXISTS);
        mCheckMd5 = (CheckBoxPreference) findPreference(PREFERENCE_CHECK_MD5);
        //        mOverrideList = (CheckBoxPreference) findPreference(PREFERENCE_OVERRIDE_LIST);
        //        mAutoloadList = (CheckBoxPreference) findPreference(PREFERENCE_AUTOLOAD_LIST);
        mDownloadPath = findPreference(PREFERENCE_DOWNLOAD_PATH);
        mZipPosition = (ListPreference) findPreference(PREFERENCE_ZIP_POSITION);
        mOptions = (MultiSelectListPreference) findPreference(PREFERENCE_OPTIONS);
        mSpaceLeft = (ListPreference) findPreference(PREFERENCE_SPACE_LEFT);
        mSystemWipeAlert = (CheckBoxPreference) findPreference(PREFERENCE_SYSTEMWIPE_ALERT);
        mUpdatesOnStartup = (CheckBoxPreference) findPreference(PREFERENCE_CHECK_UPDATE_STARTUP);
        mUseFolder = (CheckBoxPreference) findPreference(PREFERENCE_USE_FOLDER);
        mFolder = findPreference(PREFERENCE_FOLDER);
        mCheckRom = findPreference(PREFERENCE_CHECK_ROM_UPDATES);
        mCheckTime = (ListPreference) findPreference(PREFERENCE_TIME_NOTIFICATIONS);

        Preferences prefs = getCore().getPreferences();

        mForceExternalSdcard.setChecked(prefs.isForceExternalStorage());
        mForceDataMedia.setChecked(prefs.isForceDataMedia());
        mBackupExternalSdcard.setChecked(prefs.isBackupExternalStorage());
        mDad.setChecked(prefs.isUseDragAndDrop());
        mDarkTheme.setChecked(prefs.isDarkTheme());
        mCheckExists.setChecked(prefs.isCheckExists());
        mCheckMd5.setChecked(prefs.isCheckMD5());
//        mOverrideList.setChecked(prefs.isOverrideList());
//        mAutoloadList.setChecked(prefs.isAutoloadList());
        mZipPosition.setValue(prefs.getZipPosition());
        mZipPosition.setOnPreferenceChangeListener(this);
        mOptions.setValues(prefs.getShowOptions());
        mSpaceLeft.setValue(String.valueOf(prefs.getSpaceLeft()));
        mSpaceLeft.setOnPreferenceChangeListener(this);
        mSystemWipeAlert.setChecked(prefs.isShowSystemWipeAlert());
        mUseFolder.setChecked(prefs.isUseFolder());
        mUpdatesOnStartup.setChecked(prefs.isCheckUpdatesStartup());
        mCheckTime.setValue(String.valueOf(prefs.getTimeNotifications()));

        mRomUpdater = new RomUpdater(getCore());

        StoragePlugin sPlugin = (StoragePlugin) getCore().getPlugin(Core.PLUGIN_STORAGE);
        if (!sPlugin.hasExternalStorage()) {
            PreferenceCategory category = (PreferenceCategory) findPreference(PREFERENCE_CATEGORY_RECOVERY);
            category.removePreference(mExternalSdcard);
            category.removePreference(mForceExternalSdcard);
            category.removePreference(mBackupExternalSdcard);
        }

        LicensePlugin lPlugin = (LicensePlugin) getCore().getPlugin(Core.PLUGIN_LICENSE);
        PreferenceCategory category = (PreferenceCategory) findPreference(PREFERENCE_CATEGORY_UPDATE);
        if (lPlugin.isPurchased()) {
            category.removePreference(mUpdatesOnStartup);
        }
        if (!mRomUpdater.canUpdate()) {
            category.removePreference(mCheckRom);
            category.removePreference(mCheckTime);
        }

        updateSummaries();
    }

    @Override
    public void restore(Bundle savedInstanceState) {
    }

    @Override
    public void save(Bundle outState) {
    }

    @Override
    public int getTitle() {
        return R.string.settings_title;
    }

    @Override
    public int[] getVisibleMenuItems() {
        return null;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();

        final Preferences prefs = getCore().getPreferences();

        if (PREFERENCE_RECOVERY.equals(key)) {

            selectRecovery();

        } else if (PREFERENCE_INTERNAL_SDCARD.equals(key)) {

            selectSdcard(true);

        } else if (PREFERENCE_EXTERNAL_SDCARD.equals(key)) {

            selectSdcard(false);

        } else if (PREFERENCE_DAD.equals(key)) {

            boolean useDad = ((CheckBoxPreference) preference).isChecked();
            prefs.setUseDragAndDrop(useDad);

            FileItemStore.removeItems();

        } else if (PREFERENCE_DARK_THEME.equals(key)) {

            boolean darkTheme = ((CheckBoxPreference) preference).isChecked();
            prefs.setDarkTheme(darkTheme);

            showRestartDialog();

        } else if (PREFERENCE_CHECK_EXISTS.equals(key)) {

            boolean checkExists = ((CheckBoxPreference) preference).isChecked();
            prefs.setCheckExists(checkExists);

        } else if (PREFERENCE_CHECK_UPDATE_STARTUP.equals(key)) {

            boolean checkUpdate = ((CheckBoxPreference) preference).isChecked();
            prefs.setCheckUpdatesStartup(checkUpdate);

        } else if (PREFERENCE_CHECK_MD5.equals(key)) {

            boolean checkMd5 = ((CheckBoxPreference) preference).isChecked();
            prefs.setCheckMD5(checkMd5);

//        } else if (PREFERENCE_OVERRIDE_LIST.equals(key)) {
//
//            boolean overrideList = ((CheckBoxPreference) preference).isChecked();
//            pManager.setOverrideList(overrideList);
//
//        } else if (PREFERENCE_AUTOLOAD_LIST.equals(key)) {
//
//            boolean autoloadList = ((CheckBoxPreference) preference).isChecked();
//            pManager.setAutoloadList(autoloadList);

        } else if (PREFERENCE_DOWNLOAD_PATH.equals(key)) {

            pickFolder(prefs.getDownloadPath(), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FolderPicker picker = (FolderPicker) dialog;
                    prefs.setDownloadPath(picker.getPath());
                    updateSummaries();
                }

            });

        } else if (PREFERENCE_FOLDER.equals(key)) {

            pickFolder(prefs.getFolder(), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FolderPicker picker = (FolderPicker) dialog;
                    prefs.setFolder(picker.getPath());
                    updateSummaries();
                }

            });

        } else if (PREFERENCE_SYSTEMWIPE_ALERT.equals(key)) {

            boolean showAlert = ((CheckBoxPreference) preference).isChecked();
            prefs.setShowSystemWipeAlert(showAlert);

        } else if (PREFERENCE_CHECK_ROOT.equals(key)) {

            SuperUserPlugin sPlugin = (SuperUserPlugin) getCore().getPlugin(Core.PLUGIN_SUPERUSER);
            showCheckRootDialog(sPlugin.test());

        } else if (PREFERENCE_CHECK_ROM_UPDATES.equals(key)) {

            mRomUpdater.check();

        }

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();

        Preferences prefs = getCore().getPreferences();

        if (PREFERENCE_SPACE_LEFT.equals(key)) {

            prefs.setSpaceLeft(Double.parseDouble(newValue.toString()));

        } else if (PREFERENCE_ZIP_POSITION.equals(key)) {

            prefs.setZipPosition((String)newValue);

        } else if (PREFERENCE_TIME_NOTIFICATIONS.equals(key)) {

            long time = Long.parseLong((String)newValue);
            prefs.setTimeNotifications(time);
            SystemProperties.setAlarm(getCore().getContext(), time, false);

        }
        return false;
    }

    private void updateSummaries() {
        RecoveryPlugin rPlugin = (RecoveryPlugin) getCore().getPlugin(Core.PLUGIN_RECOVERY);
        RecoveryInfo info = rPlugin.getRecovery();
        Preferences prefs = getCore().getPreferences();
        mRecovery.setSummary(getResources().getText(R.string.recovery_summary) + " ("
                + info.getName() + ")");
        mInternalSdcard.setSummary(getResources().getText(R.string.internalsdcard_summary) + " ("
                + prefs.getInternalStorage() + ")");
        mExternalSdcard.setSummary(getResources().getText(R.string.externalsdcard_summary) + " ("
                + prefs.getExternalStorage() + ")");
        mDownloadPath.setSummary(prefs.getDownloadPath());
        mFolder.setSummary(prefs.getFolder());
    }

    private void pickFolder(String defaultValue, DialogInterface.OnClickListener listener) {
        new FolderPicker(getCore().getContext(), listener, defaultValue).show();
    }

    private void selectSdcard(final boolean internal) {
        final Context context = getCore().getContext();
        final Preferences prefs = getCore().getPreferences();
        
        final EditText input = new EditText(context);
        input.setText(internal ? prefs.getInternalStorage() : prefs.getExternalStorage());

        new AlertDialog.Builder(context)
                .setTitle(R.string.sdcard_alert_title)
                .setMessage(R.string.sdcard_alert_summary)
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();

                        if (value == null || "".equals(value.trim())) {
                            Toast.makeText(context, R.string.sdcard_alert_error,
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }

                        if (value.startsWith("/")) {
                            value = value.substring(1);
                        }

                        if (internal) {
                            prefs.setInternalStorage(value);
                        } else {
                            prefs.setExternalStorage(value);
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
        Context context = getCore().getContext();

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_recovery, null);

        RadioButton cbCwmbased = (RadioButton) view.findViewById(R.id.cwmbased);
        RadioButton cbCwm = (RadioButton) view.findViewById(R.id.cwm);
        RadioButton cbTwrp = (RadioButton) view.findViewById(R.id.twrp);
        RadioButton cb4ext = (RadioButton) view.findViewById(R.id.fourext);

        final RadioGroup mGroup = (RadioGroup) view.findViewById(R.id.recovery_radio_group);

        final RecoveryPlugin rPlugin = (RecoveryPlugin) getCore().getPlugin(Core.PLUGIN_RECOVERY);
        RecoveryInfo info = rPlugin.getRecovery();
        switch (info.getId()) {
            case RecoveryInfo.RECOVERY_CWM_BASED:
                cbCwmbased.setChecked(true);
                break;
            case RecoveryInfo.RECOVERY_CWM:
                cbCwm.setChecked(true);
                break;
            case RecoveryInfo.RECOVERY_TWRP:
                cbTwrp.setChecked(true);
                break;
            case RecoveryInfo.RECOVERY_4EXT:
                cb4ext.setChecked(true);
                break;
        }

        new AlertDialog.Builder(context).setTitle(R.string.recovery_alert_title)
                .setMessage(R.string.recovery_alert_summary).setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        int id = mGroup.getCheckedRadioButtonId();

                        switch (id) {
                            case R.id.cwmbased:
                                id = RecoveryInfo.RECOVERY_CWM_BASED;
                                break;
                            case R.id.cwm:
                                id = RecoveryInfo.RECOVERY_CWM;
                                break;
                            case R.id.twrp:
                                id = RecoveryInfo.RECOVERY_TWRP;
                                break;
                            case R.id.fourext:
                                id = RecoveryInfo.RECOVERY_4EXT;
                                break;
                        }

                        rPlugin.setRecovery(id);

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
        final Context context = getCore().getContext();
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.restart_needed);
        alert.setMessage(R.string.restart_needed_theme_message);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                dialog.dismiss();

                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
                getActivity().finish();

            }
        });
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void showCheckRootDialog(boolean granted) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getCore().getContext());
        alert.setTitle(R.string.checkroot_title);
        alert.setMessage(granted ? R.string.checkroot_yes : R.string.checkroot_no);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                dialog.dismiss();
            }
        });
        alert.show();
    }

}
