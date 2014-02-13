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

package com.beerbong.zipinst.core.plugins.recovery.impl;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Environment;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.plugins.recovery.RecoveryInfo;
import com.beerbong.zipinst.core.plugins.storage.StoragePlugin;
import com.beerbong.zipinst.io.Files;
import com.beerbong.zipinst.io.Strings;
import com.beerbong.zipinst.preferences.Preferences;
import com.beerbong.zipinst.store.FileItem;
import com.beerbong.zipinst.store.FileItemStore;

public class CwmBasedRecovery extends CwmRecovery {

    public CwmBasedRecovery(Core core) {
        super(core);

        setId(RECOVERY_CWM_BASED);
        setName("cwmbased");
        setInternalSdcard(internalStorage());
    }

    @Override
    public String getFullName(Context context) {
        return context.getString(R.string.recovery_cwm_official);
    }

    @Override
    public List<String> getCommands(String storage, boolean external, boolean wipeSystem,
            boolean wipeData, boolean wipeCaches, boolean fixPermissions, String backupFolder,
            String backupOptions, String restore) {

        String sbin = Files.getSBINFolder();

        Preferences prefs = getCore().getPreferences();
        String internalStorage = prefs.getInternalStorage();
        while (internalStorage.startsWith("/")) {
            internalStorage = internalStorage.substring(1);
        }

        List<String> commands = new ArrayList<String>();

        commands.add("ui_print(\"-------------------------------------\");");
        commands.add("ui_print(\" ZipInstaller "
                + getCore().getVersion().toString()
                + "\");");
        commands.add("ui_print(\"-------------------------------------\");");

        StoragePlugin sPlugin = (StoragePlugin) getCore().getPlugin(Core.PLUGIN_STORAGE);

        if (getId() == RecoveryInfo.RECOVERY_CWM_BASED
                && sPlugin.hasExternalStorage()
                && prefs.isForceExternalStorage()) {
            commands.add("ui_print(\" Mounting external sd\");");
            commands.add("run_program(\"/sbin/mount\", \""
                    + prefs.getExternalStorage() + "\");");
        }

        String folder = "/" + storage + "/clockworkmod/backup/";
        if (!external && isOldBackup() && storage != null) {
            folder = getFolderPath(storage, external) + "backup/";
        }

        if (restore != null) {
            commands.add("ui_print(\" Restore ROM from " + folder + restore + "\");");
            commands.add("restore_rom(\"" + folder
                    + restore
                    + "\", \"boot\", \"system\", \"data\", \"cache\", \"sd-ext\")");
        }

        if (backupFolder != null) {
            commands.add("ui_print(\" Backup ROM to " + folder + backupFolder + "\");");
            commands.add("assert(backup_rom(\"" + folder
                    + backupFolder + "\"));");
        }

        if (wipeSystem) {
            commands.add("ui_print(\" Wiping system\");");
            commands.add("format(\"/system\");");
        }

        if (wipeData) {
            commands.add("ui_print(\" Wiping data\");");
            commands.add("format(\"/data\");");
            commands.add("ui_print(\" Wiping android secure\");");
            commands.add("format(\"/" + internalStorage + "/.android_secure\");");
        }
        if (wipeCaches) {
            commands.add("ui_print(\" Wiping cache\");");
            commands.add("format(\"/cache\");");
            commands.add("ui_print(\" Wiping dalvik cache\");");
            commands.add("format(\"/data/dalvik-cache\");");
            commands.add("format(\"/cache/dalvik-cache\");");
            commands.add("format(\"/sd-ext/dalvik-cache\");");
        }

        int size = FileItemStore.size(), i = 0;

        if (size > 0) {
            for (; i < size; i++) {
                FileItem item = FileItemStore.getItem(i);
                if (item.isZip()) {
                    commands.add("ui_print(\" Installing zip\");");
                    commands.add("assert(install_zip(\"" + item.getKey() + "\"));");
                } else if (item.isScript()) {
                    commands.add("ui_print(\" Executing script\");");
                    commands.add("run_program(\"/sbin/busybox\", \"cp\", \""
                            + item.getKey() + "\", \"/cache/" + item.getName() + "\");");
                    commands.add("run_program(\"" + sbin + "chmod\", \"+x\", \"/cache/"
                            + item.getName() + "\");");
                    commands.add("run_program(\"" + sbin + "sh\", \"/cache/"
                            + item.getName() + "\");");
                    commands.add("run_program(\"/sbin/busybox\", \"rm\", \"/cache/"
                            + item.getName() + "\");");
                }
            }
        }

        if (fixPermissions) {
            commands.add("ui_print(\" Fix permissions\");");
            commands.add("run_program(\"" + sbin
                    + "chmod\", \"+x\", \"/cache/fix_permissions.sh\");");
            commands.add("run_program(\"" + sbin + "sh\", \"/cache/fix_permissions.sh\");");
            commands.add("run_program(\"/sbin/busybox\", \"rm\", \"/cache/fix_permissions.sh\");");
        }

        commands.add("ui_print(\" Rebooting\");");

        return commands;
    }

    private String internalStorage() {
        if (Environment.getExternalStorageDirectory() == null) {
            return "sdcard";
        }
        String path, dirPath;
        dirPath = path = Environment.getExternalStorageDirectory().getAbsolutePath();
        dirPath = Strings.replace(Strings.replace(
                Strings.replace(dirPath, "/mnt/sdcard", "/sdcard"), "/mnt/emmc", "/emmc"), path,
                "/sdcard");
        if (VERSION.SDK_INT > 16) {
            String emulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
            if ((emulatedStorageTarget != null) && (path.startsWith(emulatedStorageTarget))) {
                String number = path.replace(emulatedStorageTarget, "");
                dirPath = Strings.replace(dirPath, "/sdcard", "/sdcard" + number);
            }
            String emulatedStorageSource = System.getenv("EMULATED_STORAGE_SOURCE");
            if (emulatedStorageSource != null) {
                dirPath = Strings.replace(dirPath, emulatedStorageSource,
                        "/data/media");
            }
            if (emulatedStorageTarget == null && emulatedStorageSource == null
                    && "/storage/sdcard0".equals(path)
                    && "/sdcard".equals(dirPath)) {
                dirPath = path;
            }
        } else if (dirPath.startsWith("/mnt/emmc")) {
            dirPath = "emmc";
        }
        return dirPath;
    }

}
