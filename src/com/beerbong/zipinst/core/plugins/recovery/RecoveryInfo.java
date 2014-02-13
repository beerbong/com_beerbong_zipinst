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

package com.beerbong.zipinst.core.plugins.recovery;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.os.Build.VERSION;

import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.plugins.storage.StoragePlugin;
import com.beerbong.zipinst.preferences.Preferences;

public abstract class RecoveryInfo {

    public static final int RECOVERY_CWM = 0;
    public static final int RECOVERY_CWM_BASED = 1;
    public static final int RECOVERY_TWRP = 2;
    public static final int RECOVERY_4EXT = 3;

    private Core mCore;
    private boolean mOldBackup = false;
    private int id;
    private String name = null;
    private String internalSdcard = null;
    private String externalSdcard = null;

    public RecoveryInfo(Core core) {

        mCore = core;

        StoragePlugin rPlugin = (StoragePlugin) core.getPlugin(Core.PLUGIN_STORAGE);
        setExternalSdcard(rPlugin.getExternalStoragePath());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInternalSdcard() {
        return internalSdcard;
    }

    public void setInternalSdcard(String sdcard) {
        this.internalSdcard = sdcard;
    }

    public String getExternalSdcard() {
        return externalSdcard;
    }

    public void setExternalSdcard(String sdcard) {
        this.externalSdcard = sdcard;
    }

    public abstract String getFullName(Context context);

    public abstract String getFolderPath();

    public abstract String getCommandsFile();

    public abstract String getBackupFolder(String sdcard, boolean force, boolean external);

    public abstract List<String> getCommands(String storage, boolean external, boolean wipeSystem,
            boolean wipeData, boolean wipeCaches, boolean fixPermissions, String backupFolder,
            String backupOptions, String restore) throws Exception;

    protected Core getCore() {
        return mCore;
    }

    public boolean isOldBackup() {
        checkForOldBackup();
        return mOldBackup;
    }

    protected void checkForOldBackup() {
        Preferences prefs = getCore().getPreferences();
        File file = new File("/data/media/clockworkmod/");
        if (prefs.isForceDataMedia()) {
            mOldBackup = true;
        } else {
            mOldBackup = VERSION.SDK_INT >= 16
                    || (file.exists() && file.isDirectory());
        }
    }

}