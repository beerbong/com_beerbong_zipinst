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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.RecoveryInfo;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.FileItem;
import com.beerbong.zipinst.util.StoredItems;


public class TwrpRecovery extends RecoveryInfo {

    public TwrpRecovery(Context context) {
        super(context);

        setId(R.id.twrp);
        setName("twrp");
        setInternalSdcard("sdcard");
    }

    @Override
    public String getFullName(Context context) {
        return context.getString(R.string.recovery_twrp);
    }

    @Override
    public String getFolderPath() {
        return "/sdcard/TWRP/";
    }

    @Override
    public String getCommandsFile() {
        return "openrecoveryscript";
    }

    @Override
    public String getBackupFolder(String sdcard, boolean force, boolean external) {
        while (sdcard.startsWith("/")) {
            sdcard = sdcard.substring(1);
        }
        File f = new File("/" + sdcard + "/TWRP/BACKUPS/");
        if (f.exists()) {
            File[] fs = f.listFiles();
            return force ? fs[0].getAbsolutePath() + "/" : fs[0].getName() + "/";
        }
        return f.getAbsolutePath();
    }

    @Override
    public List<String> getCommands(String storage, boolean external, boolean wipeSystem,
            boolean wipeData, boolean wipeCaches, boolean fixPermissions, String backupFolder,
            String backupOptions, String restore) throws Exception {

        String sbin = Constants.getSBINFolder();

        List<String> commands = new ArrayList<String>();

        boolean hasAndroidSecure = Constants.hasAndroidSecure();
        boolean hasSdExt = Constants.hasSdExt();

        if (restore != null) {
            String str = "restore /" + storage + "/TWRP/BACKUPS/" + restore
                    + " SDCR123B";
            if (hasAndroidSecure) {
                str += "A";
            }
            if (hasSdExt) {
                str += "E";
            }
            commands.add(str);
        }

        if (backupFolder != null) {
            String str = "backup ";
            if (backupOptions != null && backupOptions.indexOf("S") >= 0) {
                str += "S";
            }
            if (backupOptions != null && backupOptions.indexOf("D") >= 0) {
                str += "D";
            }
            if (backupOptions != null && backupOptions.indexOf("C") >= 0) {
                str += "C";
            }
            if (backupOptions != null && backupOptions.indexOf("R") >= 0) {
                str += "R";
            }
            str += "123";
            if (backupOptions != null && backupOptions.indexOf("B") >= 0) {
                str += "B";
            }
            if (backupOptions != null && backupOptions.indexOf("A") >= 0
                    && hasAndroidSecure) {
                str += "A";
            }
            if (backupOptions != null && backupOptions.indexOf("E") >= 0 && hasSdExt) {
                str += "E";
            }
            if (external) {
                backupFolder = "/" + storage + "/TWRP/BACKUPS/" + backupFolder;
            }
            commands.add(str + "O " + backupFolder);
        }

        if (wipeSystem) {
            commands.add("mount system");
            commands.add("cmd /sbin/busybox rm -r /system/*");
            commands.add("unmount system");
        }

        if (wipeData) {
            commands.add("wipe data");
        }
        if (wipeCaches) {
            commands.add("wipe cache");
            commands.add("wipe dalvik");
        }

        int size = StoredItems.size(), i = 0;

        for (; i < size; i++) {
            FileItem item = StoredItems.getItem(i);
            if (item.isZip()) {
                commands.add("install " + item.getKey());
            } else if (item.isScript()) {
                commands.add("cmd /sbin/busybox cp " + item.getKey() + " /cache/"
                        + item.getName());
                commands.add("cmd " + sbin + "chmod +x /cache/" + item.getName());
                commands.add("cmd " + sbin + "sh /cache/" + item.getName());
                commands.add("cmd /sbin/busybox rm /cache/" + item.getName());
            }
        }

        if (fixPermissions) {
            commands.add("cmd " + sbin + "chmod +x /cache/fix_permissions.sh");
            commands.add("cmd " + sbin + "sh /cache/fix_permissions.sh");
            commands.add("cmd /sbin/busybox rm /cache/fix_permissions.sh");
        }

        return commands;
    }
}
