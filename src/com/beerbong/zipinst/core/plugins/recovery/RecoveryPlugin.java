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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.CoreImpl;
import com.beerbong.zipinst.core.Plugin;
import com.beerbong.zipinst.core.plugins.recovery.impl.CwmBasedRecovery;
import com.beerbong.zipinst.core.plugins.recovery.impl.CwmRecovery;
import com.beerbong.zipinst.core.plugins.recovery.impl.FourExtRecovery;
import com.beerbong.zipinst.core.plugins.recovery.impl.TwrpRecovery;
import com.beerbong.zipinst.core.plugins.storage.StoragePlugin;
import com.beerbong.zipinst.core.plugins.superuser.SuperUserPlugin;
import com.beerbong.zipinst.preferences.Preferences;

public class RecoveryPlugin extends Plugin {

    private SparseArray<RecoveryInfo> mRecoveries = new SparseArray<RecoveryInfo>();
    private int mInstalledRecovery = -1;
    private String mBootBlock;
    private String mRecoveryBlock;

    public RecoveryPlugin(Core core) {
        super (core, Core.PLUGIN_RECOVERY);
    }

    @Override
    public void start() {
        ((CoreImpl) getCore()).setMessage(R.string.reading_recovery_info);

        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                Core core = getCore();

                mRecoveries.put(RecoveryInfo.RECOVERY_CWM_BASED, new CwmBasedRecovery(core));
                mRecoveries.put(RecoveryInfo.RECOVERY_CWM, new CwmRecovery(core));
                mRecoveries.put(RecoveryInfo.RECOVERY_TWRP, new TwrpRecovery(core));
                mRecoveries.put(RecoveryInfo.RECOVERY_4EXT, new FourExtRecovery(core));

                SuperUserPlugin suPlugin = (SuperUserPlugin) core.getPlugin(Core.PLUGIN_SUPERUSER);
                suPlugin.run("chmod 777 /cache/recovery/");
                suPlugin.run("chmod 777 /cache/recovery/last_log");

                Preferences prefs = getCore().getPreferences();
                mBootBlock = prefs.getBootBlock();
                mRecoveryBlock = prefs.getRecoveryBlock();

                mInstalledRecovery = testLastLog();

                suPlugin.run("chmod -R 777 /data/media/clockworkmod/");
                suPlugin.run("chmod -R 777 /data/media/clockworkmod/backup/");
                String path = mRecoveries.get(RecoveryInfo.RECOVERY_TWRP).getBackupFolder("sdcard", true,
                        false);
                File file = new File(path);
                suPlugin.run("chmod -R 777 " + file.getAbsolutePath());
                if (file.getParentFile() != null) {
                    suPlugin.run("chmod -R 777 " + file.getParentFile().getAbsolutePath());
                }

                if (mRecoveryBlock != null) {
                    prefs.setRecoveryBlock(mRecoveryBlock);
                }
                if (mBootBlock != null) {
                    prefs.setBootBlock(mBootBlock);
                }

                return (Void) null;
            }

            @Override
            protected void onPostExecute(Void result) {

                Core core = getCore();
                final Preferences prefs = core.getPreferences();
                final Context context = core.getContext();

                if (!prefs.existsRecovery()) {
                    if (mInstalledRecovery != -1) {
                        setRecovery(mInstalledRecovery);
                        started();
                    } else {
                        test(mRecoveries.get(RecoveryInfo.RECOVERY_4EXT), true);
                    }
                } else {
                    if (prefs.isAlertOnChangeRecovery()) {
                        if (mInstalledRecovery != -1 && mInstalledRecovery != getRecovery().getId()) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(context);
                            alert.setTitle(R.string.alert_changed_recovery_title);
                            View view = LayoutInflater.from(context).inflate(R.layout.dialog_changed_recovery,
                                    (ViewGroup) ((Activity) context).findViewById(R.id.changed_recovery_dialog_layout));
                            alert.setView(view);

                            TextView text = (TextView) view.findViewById(R.id.text);
                            text.setText(context.getResources().getString(
                                    R.string.alert_changed_recovery_message,
                                    getRecovery().getFullName(context),
                                    mRecoveries.get(mInstalledRecovery).getFullName(context)));

                            final CheckBox cbDontShow = (CheckBox) view.findViewById(R.id.dontshow);
                            cbDontShow.setChecked(!prefs.isAlertOnChangeRecovery());

                            alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();

                                    setRecovery(mInstalledRecovery);
                                    started();

                                    prefs.setAlertOnChangeRecovery(!cbDontShow.isChecked());
                                }
                            });

                            alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();

                                    started();

                                    prefs.setAlertOnChangeRecovery(cbDontShow.isChecked());
                                }
                            });

                            alert.show();
                        } else {
                            started();
                        }
                    } else {
                        started();
                    }
                }

            }
        }).execute((Void) null);

    }

    @Override
    public void stop() {
        stopped();
    }

    public boolean hasBootBlock() {
        return mBootBlock != null && !"".equals(mBootBlock);
    }

    public boolean hasRecoveryBlock() {
        return mRecoveryBlock != null && !"".equals(mRecoveryBlock);
    }

    public RecoveryInfo getRecovery() {
        String recovery = getCore().getPreferences().getRecovery();
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
        Preferences prefs = getCore().getPreferences();
        prefs.setRecovery(info.getName());
        prefs.setInternalStorage(info.getInternalSdcard());
        prefs.setExternalStorage(info.getExternalSdcard());
    }

    public String getBackupDir(boolean force) {

        RecoveryInfo info = getRecovery();

        Preferences prefs = getCore().getPreferences();
        StoragePlugin sPlugin = (StoragePlugin) getCore().getPlugin(Core.PLUGIN_STORAGE);

        boolean external = sPlugin.hasExternalStorage()
                && prefs.isBackupExternalStorage();
        String sdcard = external ? sPlugin.getExternalStoragePath() : "sdcard";

        return info.getBackupFolder(sdcard, force, external);
    }

    public String[] getBackupList() {

        RecoveryInfo info = getRecovery();

        Preferences prefs = getCore().getPreferences();
        StoragePlugin sPlugin = (StoragePlugin) getCore().getPlugin(Core.PLUGIN_STORAGE);

        boolean external = sPlugin.hasExternalStorage()
                && prefs.isBackupExternalStorage();
        String sdcard = external ? sPlugin.getExternalStoragePath() : "sdcard";

        String folder = info.getBackupFolder(sdcard, true, external);

        List<String> list = new ArrayList<String>();

        if (folder != null && !"".equals(folder)) {
            File f = new File(folder);
            if (f.exists()) {
                File[] fs = f.listFiles();
                for (int i = 0; i < fs.length; i++) {
                    if (fs[i].isDirectory()) {
                        list.add(fs[i].getName());
                    }
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

    public String[] getCommands(boolean wipeSystem, boolean wipeData, boolean wipeCaches,
            boolean fixPermissions, String backupFolder, String backupOptions, String restore)
            throws Exception {
        List<String> commands = new ArrayList<String>();

        RecoveryInfo info = getRecovery();

        Preferences prefs = getCore().getPreferences();
        String internalStorage = prefs.getInternalStorage();
        while (internalStorage.startsWith("/")) {
            internalStorage = internalStorage.substring(1);
        }

        String externalStorage = prefs.getExternalStorage();
        while (externalStorage.startsWith("/")) {
            externalStorage = externalStorage.substring(1);
        }

        StoragePlugin sPlugin = (StoragePlugin) getCore().getPlugin(Core.PLUGIN_STORAGE);
        boolean external = sPlugin.hasExternalStorage()
                && prefs.isBackupExternalStorage();
        String storage = external ? externalStorage : internalStorage;

        commands.addAll(info.getCommands(storage, external, wipeSystem, wipeData, wipeCaches,
                fixPermissions, backupFolder, backupOptions, restore));

        return commands.toArray(new String[commands.size()]);
    }

    private int testLastLog() {
        File file = new File("/cache/recovery/last_log");
        int retValue = -1;
        if (file.exists()) {
            try {
                Scanner scanner = null;
                try {
                    scanner = new Scanner(file);
                    while ((retValue == -1 || mBootBlock == null || mRecoveryBlock == null) && scanner.hasNext()) {
                        String line = scanner.nextLine();
                        if (line != null) {
                            if (retValue == -1) {
                                if (line.indexOf("CWM-based Recovery") >= 0) {
                                    retValue = RecoveryInfo.RECOVERY_CWM_BASED;
                                } else if (line.indexOf("ClockworkMod Recovery") >= 0) {
                                    retValue = RecoveryInfo.RECOVERY_CWM;
                                } else if (line.indexOf("TWRP") >= 0) {
                                    retValue = RecoveryInfo.RECOVERY_TWRP;
                                } else if (line.indexOf("4EXT") >= 0) {
                                    retValue = RecoveryInfo.RECOVERY_4EXT;
                                }
                            }
                            if (mBootBlock == null) {
                                if (line.indexOf("/boot") >= 0 && line.indexOf("/dev/block/") >= 0) {
                                    mBootBlock = line.substring(line.indexOf("/dev/block"));
                                    int index = -1;
                                    if ((index = mBootBlock.indexOf(" ")) > 0) {
                                        mBootBlock = mBootBlock.substring(0, index);
                                    }
                                }
                            }
                            if (mRecoveryBlock == null) {
                                if (line.indexOf("/recovery") >= 0 && line.indexOf("/dev/block/") >= 0) {
                                    mRecoveryBlock = line.substring(line.indexOf("/dev/block"));
                                    int index = -1;
                                    if ((index = mRecoveryBlock.indexOf(" ")) > 0) {
                                        mRecoveryBlock = mRecoveryBlock.substring(0, index);
                                    }
                                }
                            }
                        }
                    }
                    if (mRecoveryBlock == null) {
                        mRecoveryBlock = "";
                    }
                    if (mBootBlock == null) {
                        mBootBlock = "";
                    }
                } finally {
                    if (scanner != null) {
                        scanner.close();
                    }
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        return retValue;
    }

    private void test(final RecoveryInfo info, boolean first) {

        final Context context = getCore().getContext();
        final String recoveryName = info.getFullName(context);

        if (first) {
            int recNumber = 0;
            File folder = new File(mRecoveries.get(RecoveryInfo.RECOVERY_CWM_BASED).getFolderPath());
            if (folder.exists()) {
                recNumber++;
            }
            folder = new File(mRecoveries.get(RecoveryInfo.RECOVERY_TWRP).getFolderPath());
            if (folder.exists()) {
                recNumber++;
            }
            folder = new File(mRecoveries.get(RecoveryInfo.RECOVERY_4EXT).getFolderPath());
            if (folder.exists()) {
                recNumber++;
            }
            if (recNumber != 1) {
                if (mInstalledRecovery == -1) {
                    selectRecovery(context);
                    return;
                } else {
                    setRecovery(mInstalledRecovery);
                    Toast.makeText(context,
                            context.getString(R.string.recovery_changed, recoveryName),
                            Toast.LENGTH_LONG).show();
                    started();
                    return;
                }
            }
        }

        if (info.getId() == RecoveryInfo.RECOVERY_CWM_BASED) {
            if (mInstalledRecovery == -1) {
                selectRecovery(context);
            } else {
                setRecovery(mInstalledRecovery);
                Toast.makeText(context,
                        context.getString(R.string.recovery_changed, recoveryName),
                        Toast.LENGTH_LONG).show();
                started();
            }
            return;
        }

        File folder = new File(info.getFolderPath());
        if (folder.exists()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setCancelable(false);
            alert.setTitle(R.string.recovery_change_alert_title);
            alert.setMessage(context.getString(R.string.recovery_change_alert_message,
                    recoveryName));
            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                    setRecovery(info.getId());
                    Toast.makeText(context,
                            context.getString(R.string.recovery_changed, recoveryName),
                            Toast.LENGTH_LONG).show();
                    started();
                }
            });
            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    selectRecovery(context);
                }
            });
            alert.show();
        } else {
            switch (info.getId()) {
                case RecoveryInfo.RECOVERY_4EXT:
                    test(mRecoveries.get(RecoveryInfo.RECOVERY_TWRP), false);
                    break;
                case RecoveryInfo.RECOVERY_TWRP:
                    test(mRecoveries.get(RecoveryInfo.RECOVERY_CWM_BASED), false);
                    break;
            }
        }
    }

    private void selectRecovery(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_recovery, null);

        final RadioGroup mGroup = (RadioGroup) view.findViewById(R.id.recovery_radio_group);

        new AlertDialog.Builder(context).setCancelable(false).setTitle(R.string.recovery_alert_title)
                .setMessage(R.string.recovery_alert_summary).setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        int id = mGroup.getCheckedRadioButtonId();
                        int recovery = -1;
                        switch (id) {
                            case R.id.cwm :
                                recovery = RecoveryInfo.RECOVERY_CWM;
                                break;
                            case R.id.cwmbased :
                                recovery = RecoveryInfo.RECOVERY_CWM_BASED;
                                break;
                            case R.id.twrp :
                                recovery = RecoveryInfo.RECOVERY_TWRP;
                                break;
                            case R.id.fourext :
                                recovery = RecoveryInfo.RECOVERY_4EXT;
                                break;
                        }

                        setRecovery(recovery);
                        started();

                        dialog.dismiss();
                    }
                }).show();
    }

    public void installBoot(String path) {
        if (mBootBlock == null) {
            return;
        }
        String command = "dd if=" + path + " of=" + mBootBlock;
        SuperUserPlugin suPlugin = (SuperUserPlugin) getCore().getPlugin(Core.PLUGIN_SUPERUSER);
        suPlugin.run(command);
    }

    public void installRecovery(String path) {
        if (mRecoveryBlock == null) {
            return;
        }
        String command = "dd if=" + path + " of=" + mRecoveryBlock;
        SuperUserPlugin suPlugin = (SuperUserPlugin) getCore().getPlugin(Core.PLUGIN_SUPERUSER);
        suPlugin.run(command);
    }

}
