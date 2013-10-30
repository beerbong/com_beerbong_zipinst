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

package com.beerbong.zipinst.manager.recovery;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.manager.PreferencesManager;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.FileItem;
import com.beerbong.zipinst.util.StoredItems;

public class CwmBasedRecovery extends CwmRecovery {

    public CwmBasedRecovery(Context context) {
        super(context);

        setId(R.id.cwmbased);
        setName("cwmbased");
        setInternalSdcard(internalStorage());
    }

    @Override
    public String getFullName(Context context) {
        return context.getString(R.string.recovery_cwm);
    }

    @Override
    public List<String> getCommands(String storage, boolean external, boolean wipeSystem,
            boolean wipeData, boolean wipeCaches, boolean fixPermissions, String backupFolder,
            String backupOptions, String restore) throws Exception {

        String sbin = Constants.getSBINFolder();

        PreferencesManager pManager = ManagerFactory.getPreferencesManager();
        String internalStorage = pManager.getInternalStorage();
        while (internalStorage.startsWith("/")) {
            internalStorage = internalStorage.substring(1);
        }

        List<String> commands = new ArrayList<String>();

        commands.add("ui_print(\"-------------------------------------\");");
        commands.add("ui_print(\" ZipInstaller "
                + getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName
                + "\");");
        commands.add("ui_print(\"-------------------------------------\");");

        if (getId() == R.id.cwmbased
                && ManagerFactory.getFileManager().hasExternalStorage()
                && ManagerFactory.getPreferencesManager().isForceExternalStorage()) {
            commands.add("ui_print(\" Mounting external sd\");");
            commands.add("run_program(\"/sbin/mount\", \""
                    + ManagerFactory.getPreferencesManager().getExternalStorage() + "\");");
        }

        if (restore != null) {
            commands.add("ui_print(\" Restore ROM\");");
            commands.add("restore_rom(\"/" + storage + "/clockworkmod/backup/"
                    + restore
                    + "\", \"boot\", \"system\", \"data\", \"cache\", \"sd-ext\")");
        }

        if (backupFolder != null) {
            commands.add("ui_print(\" Backup ROM\");");
            commands.add("assert(backup_rom(\"/" + storage + "/clockworkmod/backup/"
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

        int size = StoredItems.size(), i = 0;

        if (size > 0) {
            for (; i < size; i++) {
                FileItem item = StoredItems.getItem(i);
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
        dirPath = Constants.replace(Constants.replace(
                Constants.replace(dirPath, "/mnt/sdcard", "/sdcard"), "/mnt/emmc", "/emmc"), path,
                "/sdcard");
        if (Build.VERSION.SDK_INT > 16) {
            String emulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
            if ((emulatedStorageTarget != null) && (path.startsWith(emulatedStorageTarget))) {
                String number = path.replace(emulatedStorageTarget, "");
                dirPath = Constants.replace(dirPath, "/sdcard", "/sdcard" + number);
            }
            String emulatedStorageSource = System.getenv("EMULATED_STORAGE_SOURCE");
            if (emulatedStorageSource != null) {
                dirPath = Constants.replace(dirPath, emulatedStorageSource,
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
