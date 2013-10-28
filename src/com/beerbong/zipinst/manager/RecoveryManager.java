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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.SparseArray;
import android.widget.Toast;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.recovery.CwmRecovery;
import com.beerbong.zipinst.manager.recovery.FourExtRecovery;
import com.beerbong.zipinst.manager.recovery.TwrpRecovery;
import com.beerbong.zipinst.util.FileItem;
import com.beerbong.zipinst.util.StoredItems;

public class RecoveryManager extends Manager {

    private SparseArray<RecoveryInfo> mRecoveries = new SparseArray<RecoveryInfo>();
    private Map<Integer, List<String>> mProCommands;

    protected RecoveryManager(Context context) {
        super(context);

        mRecoveries.put(R.id.cwmbased, new CwmRecovery(context));
        mRecoveries.put(R.id.twrp, new TwrpRecovery());
        mRecoveries.put(R.id.fourext, new FourExtRecovery());

        if (!ManagerFactory.getPreferencesManager().existsRecovery()) {
            test(mRecoveries.get(R.id.fourext));
        }

        ManagerFactory.getProManager().manage(this, ProManager.ManageMode.Recovery);
    }

    public RecoveryInfo getRecovery() {
        String recovery = ManagerFactory.getPreferencesManager().getRecovery();
        for (int i = 0; i < mRecoveries.size(); i++) {
            int key = mRecoveries.keyAt(i);
            RecoveryInfo info = mRecoveries.get(key);
            if (info.getName().equals(recovery)) {
                return info;
            }
        }
        return null;
    }

    public void setRecovery(int id) {
        RecoveryInfo info = mRecoveries.get(id);
        PreferencesManager pManager = ManagerFactory.getPreferencesManager();
        pManager.setRecovery(info.getName());
        pManager.setInternalStorage(info.getInternalSdcard());
        pManager.setExternalStorage(info.getExternalSdcard());
    }

    public String getBackupDir(boolean force) {

        RecoveryInfo info = getRecovery();

        PreferencesManager pManager = ManagerFactory.getPreferencesManager();
        String sdcard = ManagerFactory.getFileManager().hasExternalStorage()
                && pManager.isBackupExternalStorage()
                && info.getId() == R.id.cwmbased ? ManagerFactory
                .getFileManager().getExternalStoragePath() : "sdcard";
        String str = "";

        switch (info.getId()) {
            case R.id.twrp:
                File f = new File("/" + sdcard + "/TWRP/BACKUPS/");
                if (f.exists()) {
                    File[] fs = f.listFiles();
                    str += fs[0].getName() + "/";
                }
                break;
            default:
                if (force) {
                    str = "/" + sdcard + "/clockworkmod/backup/";
                }
                break;
        }
        return str;
    }

    public String[] getBackupList() {

        RecoveryInfo info = getRecovery();

        PreferencesManager pManager = ManagerFactory.getPreferencesManager();
        String sdcard = ManagerFactory.getFileManager().hasExternalStorage()
                && pManager.isBackupExternalStorage()
                && info.getId() == R.id.cwmbased ? ManagerFactory
                .getFileManager().getExternalStoragePath() : "sdcard";
        String folder = "";

        switch (info.getId()) {
            case R.id.cwmbased:
            case R.id.fourext:
                folder = "/" + sdcard + "/clockworkmod/backup/";
                break;
            case R.id.twrp:
                folder = "/" + sdcard + "/TWRP/BACKUPS/";
                File f = new File(folder);
                if (f.exists()) {
                    File[] fs = f.listFiles();
                    folder += fs[0].getName() + "/";
                }
                break;
        }

        List<String> list = new ArrayList<String>();

        File f = new File(folder);
        if (f.exists()) {
            File[] fs = f.listFiles();
            for (int i = 0; i < fs.length; i++) {
                list.add(fs[i].getName());
            }
        }

        Collections.sort(list, new Comparator<String>() {

            @Override
            public int compare(String s1, String s2) {
                int value = s1.compareTo(s2);
                return -value;
            }
        });

        return list.toArray(new String[list.size()]);
    }

    public String getCommandsFile() {

        RecoveryInfo info = getRecovery();

        switch (info.getId()) {
            case R.id.cwmbased:
            case R.id.fourext:
                return "extendedcommand";
            case R.id.twrp:
                return "openrecoveryscript";
            default:
                return null;
        }
    }

    public void setProCommands(Map<Integer, List<String>> proCommands) {
        mProCommands = proCommands;
    }

    public String[] getCommands(boolean wipeSystem, boolean wipeData, boolean wipeCaches,
            boolean fixPermissions, String backupFolder, String backupOptions, String restore)
            throws Exception {
        List<String> commands = new ArrayList<String>();

        int size = StoredItems.size(), i = 0;

        RecoveryInfo info = getRecovery();

        PreferencesManager pManager = ManagerFactory.getPreferencesManager();
        String internalStorage = pManager.getInternalStorage();
        while (internalStorage.startsWith("/")) {
            internalStorage = internalStorage.substring(1);
        }

        String externalStorage = pManager.getExternalStorage();
        while (externalStorage.startsWith("/")) {
            externalStorage = externalStorage.substring(1);
        }

        String sbin = getSBINFolder();

        List<String> proCommands = mProCommands == null ? null : mProCommands.get(info.getId());
        if (proCommands != null) {
            commands.addAll(proCommands);
        }

        switch (info.getId()) {
            case R.id.cwmbased:
            case R.id.fourext:

                commands.add("ui_print(\"-------------------------------------\");");
                commands.add("ui_print(\" ZipInstaller "
                        + mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName
                        + "\");");
                commands.add("ui_print(\"-------------------------------------\");");

                if (info.getId() == R.id.cwmbased
                        && ManagerFactory.getFileManager().hasExternalStorage()
                        && ManagerFactory.getPreferencesManager().isForceExternalStorage()) {
                    commands.add("ui_print(\" Mounting external sd\");");
                    commands.add("run_program(\"/sbin/mount\", \""
                            + ManagerFactory.getPreferencesManager().getExternalStorage() + "\");");
                }

                String storage = ManagerFactory.getFileManager()
                        .hasExternalStorage() && pManager.isBackupExternalStorage() ? externalStorage
                                : internalStorage;

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
                break;

            case R.id.twrp:

                boolean hasAndroidSecure = hasAndroidSecure();
                boolean hasSdExt = hasSdExt();

                if (restore != null) {
                    String str = "restore /" + internalStorage + "/TWRP/BACKUPS/" + restore
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

                break;
        }

        return commands.toArray(new String[commands.size()]);
    }

    private void test(final RecoveryInfo info) {

        if (info.getId() == R.id.cwmbased) {
            setRecovery(R.id.cwmbased);
            Toast.makeText(
                    mContext,
                    mContext.getString(R.string.recovery_changed,
                            mContext.getString(R.string.recovery_cwm)), Toast.LENGTH_LONG).show();
            return;
        }

        final String recoveryName = info.getFullName(mContext);

        File folder = new File(info.getFolderPath());
        if (folder.exists()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            alert.setTitle(R.string.recovery_change_alert_title);
            alert.setMessage(mContext.getString(R.string.recovery_change_alert_message,
                    recoveryName));
            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                    setRecovery(info.getId());
                    Toast.makeText(mContext,
                            mContext.getString(R.string.recovery_changed, recoveryName),
                            Toast.LENGTH_LONG).show();
                }
            });
            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    switch (info.getId()) {
                        case R.id.fourext:
                            test(mRecoveries.get(R.id.twrp));
                            break;
                        case R.id.twrp:
                            test(mRecoveries.get(R.id.cwmbased));
                            break;
                    }
                }
            });
            alert.show();
        } else {
            switch (info.getId()) {
                case R.id.fourext:
                    test(mRecoveries.get(R.id.twrp));
                    break;
                case R.id.twrp:
                    test(mRecoveries.get(R.id.cwmbased));
                    break;
            }
        }
    }

    public boolean hasAndroidSecure() {
        String sdcard = "sdcard";
        return folderExists("/" + sdcard + "/.android-secure");
    }

    public boolean hasSdExt() {
        return folderExists("/sd-ext");
    }

    private String getSBINFolder() {
        if (folderExists("/sbin")) {
            return "/sbin/";
        } else if (folderExists("/system/sbin")) {
            return "/system/sbin/";
        }
        return null;
    }

    private boolean folderExists(String path) {
        File f = new File(path);
        return f.exists() && f.isDirectory();
    }
}