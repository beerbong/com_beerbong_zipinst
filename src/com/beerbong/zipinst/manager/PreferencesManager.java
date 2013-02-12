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

package com.beerbong.zipinst.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class PreferencesManager extends Manager {

    private static final String SDCARD = "sdcard";

    private static final String PROPERTY_INTERNAL_STORAGE = "internal-storage";
    private static final String PROPERTY_RECOVERY = "recovery";
    private static final String PROPERTY_LIST = "list";
    private static final String PROPERTY_DRAG_AND_DROP = "drag-and-drop";
    private static final String PROPERTY_SHOW_BACKUP = "show-backup";
    private static final String PROPERTY_DARK_THEME = "dark-theme";
    private static final String PROPERTY_CHECK_EXISTS = "check_exists";
    private static final String PROPERTY_CHECK_UPDATES_STARTUP = "check_updates_startup";
    private static final String PROPERTY_DOWNLOAD_PATH = "download_path";
    private static final String PROPERTY_CHECK_MD5 = "check_md5";
    private static final String PROPERTY_OVERRIDE_LIST = "override_list";
    private static final String PROPERTY_AUTOLOAD_LIST = "autoload_list";
    private static final String PROPERTY_ZIP_POSITION = "zip_position";

    public static final String PROPERTY_ENABLE_NOTIFICATIONS = "enable_notifications";
    public static final String PROPERTY_TIME_NOTIFICATIONS = "time_notifications";

    private static final String DEFAULT_RECOVERY = "cwmbased";
    private static final String DEFAULT_INTERNAL_STORAGE = "emmc";
    private static final String DEFAULT_DOWNLOAD_PATH = "/" + SDCARD + "/download/";
    private static final String DEFAULT_ZIP_POSITION = "last";
    private static final String DEFAULT_TIME_NOTIFICATIONS = "3600000"; // an hour
    private static final boolean DEFAULT_DRAG_AND_DROP = true;
    private static final boolean DEFAULT_SHOW_BACKUP = true;
    private static final boolean DEFAULT_DARK_THEME = true;
    private static final boolean DEFAULT_CHECK_EXISTS = true;
    private static final boolean DEFAULT_CHECK_UPDATES_STARTUP = false;
    private static final boolean DEFAULT_CHECK_MD5 = true;
    private static final boolean DEFAULT_OVERRIDE_LIST = true;
    private static final boolean DEFAULT_AUTOLOAD_LIST = false;
    private static final boolean DEFAULT_ENABLE_NOTIFICATIONS = true;

    private SharedPreferences settings;

    protected PreferencesManager(Context context) {
        super(context);
        settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getInternalStorage() {
        return settings.getString(PROPERTY_INTERNAL_STORAGE, DEFAULT_INTERNAL_STORAGE);
    }

    public void setInternalStorage(String value) {
        savePreference(PROPERTY_INTERNAL_STORAGE, value);
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

    public boolean isShowBackupOption() {
        return settings.getBoolean(PROPERTY_SHOW_BACKUP, DEFAULT_SHOW_BACKUP);
    }

    public void setShowBackupOption(boolean value) {
        savePreference(PROPERTY_SHOW_BACKUP, value);
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
        return Long.parseLong(settings.getString(PROPERTY_TIME_NOTIFICATIONS, DEFAULT_TIME_NOTIFICATIONS));
    }

    public void setTimeNotifications(long value) {
        savePreference(PROPERTY_TIME_NOTIFICATIONS, String.valueOf(value));
    }

    public String getZipPosition() {
        return settings.getString(PROPERTY_ZIP_POSITION, DEFAULT_ZIP_POSITION);
    }

    public void setZipPosition(String value) {
        savePreference(PROPERTY_ZIP_POSITION, value);
    }

    private void savePreference(String preference, String value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(preference, value);
        editor.commit();
    }

    private void savePreference(String preference, boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(preference, value);
        editor.commit();
    }
}