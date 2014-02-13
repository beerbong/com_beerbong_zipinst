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

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.plugins.recovery.RecoveryInfo;
import com.beerbong.zipinst.core.plugins.storage.StoragePlugin;
import com.beerbong.zipinst.io.Files;
import com.beerbong.zipinst.preferences.Preferences;
import com.beerbong.zipinst.store.FileItem;
import com.beerbong.zipinst.store.FileItemStore;
import com.beerbong.zipinst.ui.widget.Dialog;
import com.koushikdutta.rommanager.api.IClockworkRecoveryScriptBuilder;
import com.koushikdutta.rommanager.api.IROMManagerAPIService;

public class CwmRecovery extends RecoveryInfo {

    public CwmRecovery(Core core) {
        super(core);
        setId(RECOVERY_CWM);
        setName("cwm");
        setInternalSdcard("sdcard");
    }

    @Override
    public String getFullName(Context context) {
        return context.getString(R.string.recovery_cwm_official);
    }

    @Override
    public String getFolderPath() {
        checkForOldBackup();
        if (isOldBackup()) {
            return "/data/media/clockworkmod/";
        }
        return "/sdcard/clockworkmod/";
    }

    public String getFolderPath(String sdcard, boolean external) {
        checkForOldBackup();
        if (!external && isOldBackup()) {
            return "/data/media/clockworkmod/";
        }
        while (sdcard.startsWith("/")) {
            sdcard = sdcard.substring(1);
        }
        return "/" + sdcard + "/clockworkmod/backup/";
    }

    @Override
    public String getCommandsFile() {
        return "extendedcommand";
    }

    @Override
    public String getBackupFolder(String sdcard, boolean force, boolean external) {
        if (force) {
            checkForOldBackup();
            if (!external && isOldBackup()) {
                return "/data/media/clockworkmod/backup/";
            }
            while (sdcard.startsWith("/")) {
                sdcard = sdcard.substring(1);
            }
            return "/" + sdcard + "/clockworkmod/backup/";
        }
        return "";
    }

    @Override
    public List<String> getCommands(final String storage,
            final boolean external, final boolean wipeSystem,
            final boolean wipeData, final boolean wipeCaches,
            final boolean fixPermissions, final String backupFolder,
            final String backupOptions, final String restore) {
        final Context context = getCore().getContext();
        Intent i = new Intent("com.koushikdutta.rommanager.api.BIND");
        try {
            context.bindService(i, new ServiceConnection() {
                @Override
                public void onServiceDisconnected(ComponentName name) {
                }

                @Override
                public void onServiceConnected(ComponentName name,
                        IBinder binder) {
                    IROMManagerAPIService service = IROMManagerAPIService.Stub
                            .asInterface(binder);
                    if (service == null) {
                        Dialog.error(context, R.string.error_no_rommanager_connection, null);
                        return;
                    }
                    try {
                        if (!service.isPremium()) {
                            Dialog.error(context, R.string.error_no_rommanager_premium, null);
                            return;
                        }
                        IClockworkRecoveryScriptBuilder builder = service
                                .createClockworkRecoveryScriptBuilder();
                        run(builder, storage, external, wipeSystem, wipeData,
                                wipeCaches, fixPermissions, backupFolder,
                                backupOptions, restore);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Dialog.error(context, R.string.error_rommanager_unknown, null);
                    }
                }
            }, Service.BIND_AUTO_CREATE);
        } catch (Exception ex) {
            ex.printStackTrace();
            Dialog.error(context, R.string.error_no_rommanager_connection, null);
        }
        return new ArrayList<String>();
    }

    private void run(IClockworkRecoveryScriptBuilder builder, String storage, boolean external,
            boolean wipeSystem, boolean wipeData, boolean wipeCaches, boolean fixPermissions,
            String backupFolder, String backupOptions, String restore) throws Exception {

        String sbin = Files.getSBINFolder();

        Preferences prefs = getCore().getPreferences();
        String internalStorage = prefs.getInternalStorage();
        while (internalStorage.startsWith("/")) {
            internalStorage = internalStorage.substring(1);
        }

        builder.print("-------------------------------------");
        builder.print(" ZipInstaller "
                + getCore().getVersion().toString());
        builder.print("-------------------------------------");

        StoragePlugin sPlugin = (StoragePlugin) getCore().getPlugin(Core.PLUGIN_STORAGE);

        if (sPlugin.hasExternalStorage()
                && prefs.isForceExternalStorage()) {
            builder.print(" Mounting external sd");
            List<String> params = new ArrayList<String>();
            params.add(prefs.getExternalStorage());
            builder.runProgram("/sbin/mount", params);
        }

        String folder = storage + "/clockworkmod/backup/";
        if (!external && isOldBackup() && storage != null) {
            folder = getFolderPath(storage, external) + "backup/";
        }

        if (restore != null) {
            builder.print(" Restore ROM");
            builder.restore("/" + folder + restore, true, true, true,
                    true, true);
        }

        if (backupFolder != null) {
            builder.print(" Backup ROM");
            builder.backupWithPath("/" + folder + backupFolder);
        }

        if (wipeSystem) {
            builder.print(" Wiping system");
            builder.format("/system");
        }

        if (wipeData) {
            builder.print(" Wiping data");
            builder.format("/data");
            builder.print(" Wiping android secure");
            builder.format("/" + internalStorage + "/.android_secure");
        }
        if (wipeCaches) {
            builder.print(" Wiping cache");
            builder.format("/cache");
            builder.print(" Wiping dalvik cache");
            builder.format("/data/dalvik-cache");
            builder.format("/cache/dalvik-cache");
            builder.format("/sd-ext/dalvik-cache");
        }

        int size = FileItemStore.size(), i = 0;

        if (size > 0) {
            for (; i < size; i++) {
                FileItem item = FileItemStore.getItem(i);
                if (item.isZip()) {
                    builder.print(" Installing zip");
                    builder.installZip(item.getPath());
                } else if (item.isScript()) {
                    builder.print(" Executing script");
                    List<String> params = new ArrayList<String>();
                    params.add("cp");
                    params.add(item.getKey());
                    params.add("/cache/" + item.getName());
                    builder.runProgram("/sbin/busybox", params);
                    params = new ArrayList<String>();
                    params.add("chmod");
                    params.add("+x");
                    params.add("/cache/" + item.getName());
                    builder.runProgram(sbin, params);
                    params = new ArrayList<String>();
                    params.add("sh");
                    params.add("/cache/" + item.getName());
                    builder.runProgram(sbin, params);
                    params = new ArrayList<String>();
                    params.add("rm");
                    params.add("/cache/" + item.getName());
                    builder.runProgram("/sbin/busybox", params);
                }
            }
        }

        if (fixPermissions) {
            builder.print(" Fix permissions");
            List<String> params = new ArrayList<String>();
            params.add("chmod");
            params.add("+x");
            params.add("/cache/fix_permissions.sh");
            builder.runProgram(sbin, params);
            params = new ArrayList<String>();
            params.add("sh");
            params.add("/cache/fix_permissions.sh");
            builder.runProgram(sbin, params);
            params = new ArrayList<String>();
            params.add("rm");
            params.add("/cache/fix_permissions.sh");
            builder.runProgram("/sbin/busybox", params);
        }

        builder.print(" Rebooting");

        builder.runScript();
    }
}
