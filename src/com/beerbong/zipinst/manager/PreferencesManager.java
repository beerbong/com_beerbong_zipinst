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

package com.beerbong.zipinst.manager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.Rule;

public class PreferencesManager extends Manager {

    private static final String PROPERTY_INTERNAL_STORAGE = "internal-storage";
    private static final String PROPERTY_EXTERNAL_STORAGE = "external-storage";
    private static final String PROPERTY_FORCE_EXTERNAL_STORAGE = "mountextsdcard";
    private static final String PROPERTY_FORCE_DATA_MEDIA = "forcedatamedia";
    private static final String PROPERTY_BACKUP_EXTERNAL_STORAGE = "backupextsdcard";
    private static final String PROPERTY_RECOVERY = "recovery";
    private static final String PROPERTY_CHANGED_RECOVERY = "changedrecovery";
    private static final String PROPERTY_LIST = "list";
    private static final String PROPERTY_DRAG_AND_DROP = "drag-and-drop";
    private static final String PROPERTY_SHOW_OPTIONS = "show-options";
    private static final String PROPERTY_DARK_THEME = "dark-theme";
    private static final String PROPERTY_CHECK_EXISTS = "check_exists";
    private static final String PROPERTY_CHECK_UPDATES_STARTUP = "check_updates_startup";
    private static final String PROPERTY_DOWNLOAD_PATH = "download_path";
    private static final String PROPERTY_CHECK_MD5 = "check_md5";
    private static final String PROPERTY_OVERRIDE_LIST = "override_list";
    private static final String PROPERTY_AUTOLOAD_LIST = "autoload_list";
    private static final String PROPERTY_ZIP_POSITION = "zip_position";
    private static final String PROPERTY_SPACE_LEFT = "space_left";
    private static final String PROPERTY_SHOW_WIPESYSTEM_ALERT = "wipesystem_alert";
    private static final String PROPERTY_USE_FOLDER = "usefolder";
    private static final String PROPERTY_FOLDER = "folder";
    private static final String PROPERTY_TO_DELETE = "to_delete";
    private static final String PROPERTY_RULES = "rules";
    private static final String PROPERTY_LOGIN = "goologin";
    private static final String PROPERTY_LOGIN_USERNAME = "goologinusername";
    private static final String PROPERTY_DROPBOX_KEY = "dropboxkey";
    private static final String PROPERTY_DROPBOX_SECRET = "dropboxsecret";
    private static final String PROPERTY_LICENSE = "license";
    private static final String PROPERTY_RECOVERY_BLOCK = "recoveryblock";
    private static final String PROPERTY_BOOT_BLOCK = "bootblock";

    public static final String PROPERTY_ENABLE_NOTIFICATIONS = "enable_notifications";
    public static final String PROPERTY_TIME_NOTIFICATIONS = "time_notifications";
    public static final String PROPERTY_USE_ONANDROID = "use_onandroid";

    private static final String DEFAULT_RECOVERY = "cwmbased";
    private static final String DEFAULT_INTERNAL_STORAGE = "emmc";
    private static final String DEFAULT_EXTERNAL_STORAGE = "sdcard";
    private static final String DEFAULT_DOWNLOAD_PATH = "/sdcard/zipinstaller/";
    private static final String DEFAULT_ZIP_POSITION = "last";
    private static final String DEFAULT_TIME_NOTIFICATIONS = "3600000"; // an hour
    private static final Set<String> DEFAULT_SHOW_OPTIONS = new HashSet<String>();
    private static final String DEFAULT_SPACE_LEFT = "-1";
    private static final boolean DEFAULT_CHANGED_RECOVERY = true;
    private static final boolean DEFAULT_FORCE_EXTERNAL_STORAGE = true;
    private static final boolean DEFAULT_FORCE_DATA_MEDIA = false;
    private static final boolean DEFAULT_BACKUP_EXTERNAL_STORAGE = false;
    private static final boolean DEFAULT_DRAG_AND_DROP = true;
    private static final boolean DEFAULT_DARK_THEME = false;
    private static final boolean DEFAULT_CHECK_EXISTS = true;
    private static final boolean DEFAULT_CHECK_UPDATES_STARTUP = false;
    private static final boolean DEFAULT_CHECK_MD5 = true;
    private static final boolean DEFAULT_OVERRIDE_LIST = true;
    private static final boolean DEFAULT_AUTOLOAD_LIST = false;
    private static final boolean DEFAULT_ENABLE_NOTIFICATIONS = true;
    private static final boolean DEFAULT_SHOW_WIPESYSTEM_ALERT = true;
    private static final boolean DEFAULT_USE_ONANDROID = false;
    private static final boolean DEFAULT_USE_FOLDER = false;
    private static final String DEFAULT_FOLDER = "/sdcard/download/";

    private SharedPreferences settings;

    protected PreferencesManager(Context context) {
        super(context);
        settings = PreferenceManager.getDefaultSharedPreferences(context);

        DEFAULT_SHOW_OPTIONS.add(Constants.INSTALL_BACKUP);
        DEFAULT_SHOW_OPTIONS.add(Constants.INSTALL_WIPESYSTEM);
        DEFAULT_SHOW_OPTIONS.add(Constants.INSTALL_WIPEDATA);
        DEFAULT_SHOW_OPTIONS.add(Constants.INSTALL_WIPECACHES);
        DEFAULT_SHOW_OPTIONS.add(Constants.INSTALL_FIXPERM);

        File file = new File(DEFAULT_DOWNLOAD_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public String getInternalStorage() {
        return settings.getString(PROPERTY_INTERNAL_STORAGE, DEFAULT_INTERNAL_STORAGE);
    }

    public void setInternalStorage(String value) {
        savePreference(PROPERTY_INTERNAL_STORAGE, value);
    }

    public String getExternalStorage() {
        return settings.getString(PROPERTY_EXTERNAL_STORAGE, DEFAULT_EXTERNAL_STORAGE);
    }

    public void setExternalStorage(String value) {
        savePreference(PROPERTY_EXTERNAL_STORAGE, value);
    }

    public boolean isForceExternalStorage() {
        return settings.getBoolean(PROPERTY_FORCE_EXTERNAL_STORAGE, DEFAULT_FORCE_EXTERNAL_STORAGE);
    }

    public void setForceExternalStorage(boolean value) {
        savePreference(PROPERTY_FORCE_EXTERNAL_STORAGE, value);
    }

    public boolean isForceDataMedia() {
        return settings.getBoolean(PROPERTY_FORCE_DATA_MEDIA, DEFAULT_FORCE_DATA_MEDIA);
    }

    public void setForceDataMedia(boolean value) {
        savePreference(PROPERTY_FORCE_DATA_MEDIA, value);
    }

    public boolean isBackupExternalStorage() {
        return settings.getBoolean(PROPERTY_BACKUP_EXTERNAL_STORAGE, DEFAULT_BACKUP_EXTERNAL_STORAGE);
    }

    public void setBackupExternalStorage(boolean value) {
        savePreference(PROPERTY_BACKUP_EXTERNAL_STORAGE, value);
    }

    public boolean existsRecovery() {
        return settings.contains(PROPERTY_RECOVERY);
    }

    public String getRecovery() {
        return settings.getString(PROPERTY_RECOVERY, DEFAULT_RECOVERY);
    }

    public void setRecovery(String value) {
        savePreference(PROPERTY_RECOVERY, value);
    }

    public String getList() {
        return settings.getString(PROPERTY_LIST, "");
    }

    public void setList(String value) {
        savePreference(PROPERTY_LIST, value);
    }

    public boolean isShowOption(String option) {
        Set<String> options = getShowOptions();
        return options.contains(option);
    }

    public Set<String> getShowOptions() {
        return settings.getStringSet(PROPERTY_SHOW_OPTIONS, DEFAULT_SHOW_OPTIONS);
    }

    public void setShowOptions(String options) {
        savePreference(PROPERTY_SHOW_OPTIONS, options);
    }

    public boolean isUseDragAndDrop() {
        return settings.getBoolean(PROPERTY_DRAG_AND_DROP, DEFAULT_DRAG_AND_DROP);
    }

    public void setUseDragAndDrop(boolean value) {
        savePreference(PROPERTY_DRAG_AND_DROP, value);
    }

    public boolean isDarkTheme() {
        return settings.getBoolean(PROPERTY_DARK_THEME, DEFAULT_DARK_THEME);
    }

    public void setDarkTheme(boolean value) {
        savePreference(PROPERTY_DARK_THEME, value);
    }

    public boolean isAlertOnChangeRecovery() {
        return settings.getBoolean(PROPERTY_CHANGED_RECOVERY, DEFAULT_CHANGED_RECOVERY);
    }

    public void setAlertOnChangeRecovery(boolean value) {
        savePreference(PROPERTY_CHANGED_RECOVERY, value);
    }

    public boolean isCheckExists() {
        return settings.getBoolean(PROPERTY_CHECK_EXISTS, DEFAULT_CHECK_EXISTS);
    }

    public void setCheckExists(boolean value) {
        savePreference(PROPERTY_CHECK_EXISTS, value);
    }

    public boolean isCheckUpdatesStartup() {
        return settings.getBoolean(PROPERTY_CHECK_UPDATES_STARTUP, DEFAULT_CHECK_UPDATES_STARTUP);
    }

    public void setCheckUpdatesStartup(boolean value) {
        savePreference(PROPERTY_CHECK_UPDATES_STARTUP, value);
    }

    public boolean isCheckMD5() {
        return settings.getBoolean(PROPERTY_CHECK_MD5, DEFAULT_CHECK_MD5);
    }

    public void setCheckMD5(boolean value) {
        savePreference(PROPERTY_CHECK_MD5, value);
    }

    public boolean isOverrideList() {
        return settings.getBoolean(PROPERTY_OVERRIDE_LIST, DEFAULT_OVERRIDE_LIST);
    }

    public void setOverrideList(boolean value) {
        savePreference(PROPERTY_OVERRIDE_LIST, value);
    }

    public boolean isAutoloadList() {
        return settings.getBoolean(PROPERTY_AUTOLOAD_LIST, DEFAULT_AUTOLOAD_LIST);
    }

    public void setAutoloadList(boolean value) {
        savePreference(PROPERTY_AUTOLOAD_LIST, value);
    }

    public String getDownloadPath() {
        return settings.getString(PROPERTY_DOWNLOAD_PATH, DEFAULT_DOWNLOAD_PATH);
    }

    public void setDownloadPath(String value) {
        if (!value.endsWith("/"))
            value = value + "/";
        savePreference(PROPERTY_DOWNLOAD_PATH, value);
    }

    public boolean isAcceptNotifications() {
        return settings.getBoolean(PROPERTY_ENABLE_NOTIFICATIONS, DEFAULT_ENABLE_NOTIFICATIONS);
    }

    public void setAcceptNotifications(boolean value) {
        savePreference(PROPERTY_ENABLE_NOTIFICATIONS, value);
    }

    public long getTimeNotifications() {
        return Long.parseLong(settings.getString(PROPERTY_TIME_NOTIFICATIONS,
                DEFAULT_TIME_NOTIFICATIONS));
    }

    public void setTimeNotifications(long value) {
        savePreference(PROPERTY_TIME_NOTIFICATIONS, String.valueOf(value));
    }

    public boolean isUseONandroid() {
        return settings.getBoolean(PROPERTY_USE_ONANDROID, DEFAULT_USE_ONANDROID);
    }

    public void setUseONandroid(boolean value) {
        savePreference(PROPERTY_USE_ONANDROID, value);
    }

    public String getZipPosition() {
        return settings.getString(PROPERTY_ZIP_POSITION, DEFAULT_ZIP_POSITION);
    }

    public void setZipPosition(String value) {
        savePreference(PROPERTY_ZIP_POSITION, value);
    }

    public double getSpaceLeft() {
        return Double.parseDouble(settings.getString(PROPERTY_SPACE_LEFT, DEFAULT_SPACE_LEFT));
    }

    public void setSpaceLeft(double value) {
        savePreference(PROPERTY_SPACE_LEFT, String.valueOf(value));
    }

    public boolean isShowSystemWipeAlert() {
        return settings.getBoolean(PROPERTY_SHOW_WIPESYSTEM_ALERT, DEFAULT_SHOW_WIPESYSTEM_ALERT);
    }

    public void setShowSystemWipeAlert(boolean value) {
        savePreference(PROPERTY_SHOW_WIPESYSTEM_ALERT, value);
    }

    public boolean isUseFolder() {
        return settings.getBoolean(PROPERTY_USE_FOLDER, DEFAULT_USE_FOLDER);
    }

    public void setUseFolder(boolean value) {
        savePreference(PROPERTY_USE_FOLDER, value);
    }

    public String getFolder() {
        return settings.getString(PROPERTY_FOLDER, DEFAULT_FOLDER);
    }

    public void setFolder(String value) {
        savePreference(PROPERTY_FOLDER, value);
    }

    public String[] getToDelete() {
        return getArrayProperty(PROPERTY_TO_DELETE);
    }

    public void setToDelete(String[] value) {
        setArrayProperty(PROPERTY_TO_DELETE, value);
    }

    public Rule[] getRules() {
        return Rule.createRules(settings.getString(PROPERTY_RULES, ""));
    }

    public boolean hasRules() {
        Rule[] rules = getRules();
        return rules.length > 0;
    }

    public void setRules(Rule[] rules) {
        savePreference(PROPERTY_RULES, Rule.storeRules(rules));
    }

    public void addRule(String name, int type) {
        Rule[] rules = getRules();
        Rule[] newRules = new Rule[rules.length + 1];
        for (int i = 0; i < rules.length;i++) {
            newRules[i] = rules[i];
        }
        newRules[newRules.length - 1] = new Rule(name, type);
        savePreference(PROPERTY_RULES, Rule.storeRules(newRules));
    }

    public String getRecoveryBlock() {
        return settings.getString(PROPERTY_RECOVERY_BLOCK, null);
    }

    public void setRecoveryBlock(String value) {
        savePreference(PROPERTY_RECOVERY_BLOCK, value);
    }

    public String getBootBlock() {
        return settings.getString(PROPERTY_BOOT_BLOCK, null);
    }

    public void setBootBlock(String value) {
        savePreference(PROPERTY_BOOT_BLOCK, value);
    }

    public void logout() {
        savePreference(PROPERTY_LOGIN, "");
    }

    public void login(String value) {
        savePreference(PROPERTY_LOGIN, value);
    }

    public void setLoginUserName(String value) {
        savePreference(PROPERTY_LOGIN_USERNAME, value);
    }

    public boolean isLogged() {
        return !"".equals(settings.getString(PROPERTY_LOGIN, ""));
    }

    public String getLogin() {
        return settings.getString(PROPERTY_LOGIN, "");
    }

    public String getLoginUserName() {
        return settings.getString(PROPERTY_LOGIN_USERNAME, null);
    }

    public void setDropboxKey(String value) {
        savePreference(PROPERTY_DROPBOX_KEY, value);
    }

    public String getDropboxKey() {
        return settings.getString(PROPERTY_DROPBOX_KEY, null);
    }

    public void setDropboxSecret(String value) {
        savePreference(PROPERTY_DROPBOX_SECRET, value);
    }

    public String getDropboxSecret() {
        return settings.getString(PROPERTY_DROPBOX_SECRET, null);
    }

    public void setLicense(boolean value) {
        savePreference(PROPERTY_LICENSE, value);
    }

    public boolean getLicense() {
        return settings.getBoolean(PROPERTY_LICENSE, false);
    }

    private String[] getArrayProperty(String property) {
        String value = settings.getString(property, "");
        String[] array = value.split("\n");
        if (array.length == 1 && "".equals(array[0])) {
            return new String[0];
        }
        return array;
    }

    public void setArrayProperty(String property, String[] value) {
        String array = "";
        for (int i = 0; i < value.length; i++) {
            array += value[i] + "\n";
        }
        savePreference(property, array);
    }

    private void savePreference(String preference, String value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(preference, value);
        editor.commit();
        UI.getInstance().settingsChanged();
    }

    private void savePreference(String preference, boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(preference, value);
        editor.commit();
        UI.getInstance().settingsChanged();
    }
}