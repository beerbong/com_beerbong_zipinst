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

public class RecoveryManager extends Manager {

    private SparseArray<RecoveryInfo> mRecoveries = new SparseArray<RecoveryInfo>();
    private Map<Integer, List<String>> mProCommands;

    protected RecoveryManager(Context context) {
        super(context);

        mRecoveries.put(R.id.cwmbased, new CwmRecovery(context));
        mRecoveries.put(R.id.twrp, new TwrpRecovery());
        mRecoveries.put(R.id.fourext, new FourExtRecovery(context));

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
        boolean external = ManagerFactory.getFileManager().hasExternalStorage()
                && pManager.isBackupExternalStorage();
        String sdcard = external ? ManagerFactory.getFileManager()
                .getExternalStoragePath() : "sdcard";

        return info.getBackupFolder(sdcard, force);
    }

    public String[] getBackupList() {

        RecoveryInfo info = getRecovery();

        PreferencesManager pManager = ManagerFactory.getPreferencesManager();
        String sdcard = ManagerFactory.getFileManager().hasExternalStorage()
                && pManager.isBackupExternalStorage() ? ManagerFactory.getFileManager()
                .getExternalStoragePath() : "sdcard";

        String folder = info.getBackupFolder(sdcard, true);

        List<String> list = new ArrayList<String>();

        if (folder != null && !"".equals(folder)) {
            File f = new File(folder);
            if (f.exists()) {
                File[] fs = f.listFiles();
                for (int i = 0; i < fs.length; i++) {
                    list.add(fs[i].getName());
                }
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

        return info.getCommandsFile();
    }

    public void setProCommands(Map<Integer, List<String>> proCommands) {
        mProCommands = proCommands;
    }

    public String[] getCommands(boolean wipeSystem, boolean wipeData, boolean wipeCaches,
            boolean fixPermissions, String backupFolder, String backupOptions, String restore)
            throws Exception {
        List<String> commands = new ArrayList<String>();

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

        List<String> proCommands = mProCommands == null ? null : mProCommands.get(info.getId());
        if (proCommands != null) {
            commands.addAll(proCommands);
        }

        boolean external = ManagerFactory.getFileManager().hasExternalStorage()
                && pManager.isBackupExternalStorage();
        String storage = external ? externalStorage : internalStorage;

        commands.addAll(info.getCommands(storage, external, wipeSystem, wipeData, wipeCaches,
                fixPermissions, backupFolder, backupOptions, restore));

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
}