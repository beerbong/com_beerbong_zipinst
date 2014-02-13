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

package com.beerbong.zipinst.core.plugins.reboot;

import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.Plugin;
import com.beerbong.zipinst.core.plugins.recovery.RecoveryInfo;
import com.beerbong.zipinst.core.plugins.recovery.RecoveryPlugin;
import com.beerbong.zipinst.core.plugins.storage.StoragePlugin;
import com.beerbong.zipinst.core.plugins.superuser.SuperUserPlugin;
import com.beerbong.zipinst.io.Files;
import com.beerbong.zipinst.io.InstallOptionsCursor;
import com.beerbong.zipinst.io.Strings;
import com.beerbong.zipinst.preferences.Preferences;
import com.beerbong.zipinst.store.FileItem;
import com.beerbong.zipinst.store.FileItemStore;

public class RebootPlugin extends Plugin {

    private int mSelectedBackup;
    private int mIsSystemApp = -1;

    public RebootPlugin(Core core) {
        super(core, Core.PLUGIN_REBOOT);
    }

    @Override
    public void start() {
        started();
    }

    @Override
    public void stop() {
        stopped();
    }

    public void checkFilesAndMd5() {

        Preferences prefs = getCore().getPreferences();
        final boolean checkExists = prefs.isCheckExists();
        final boolean checkMd5 = prefs.isCheckMD5();

        if (!checkExists && !checkMd5) {
            showRebootDialog();
            return;
        }

        final Context context = getCore().getContext();

        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setIndeterminate(true);
        pDialog.setMessage(context.getResources().getString(R.string.alert_file_checking));
        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();

        (new Thread() {

            public void run() {

                int size = FileItemStore.size();
                for (int i = 0; i < size; i++) {
                    FileItem item = FileItemStore.getItem(i);
                    String path = item.getPath();
                    final File file = new File(path);

                    ((Activity) context).runOnUiThread(new Runnable() {

                        public void run() {
                            pDialog.setMessage(context.getResources().getString(
                                    R.string.alert_file_exists_checking,
                                    new Object[] { file.getName() }));
                        }
                    });

                    if (checkExists && !file.exists()) {
                        pDialog.dismiss();
                        showAlertOnUIThread(R.string.alert_file_alert,
                                R.string.alert_file_not_exists, new Object[] { file.getName() });
                        return;
                    }

                    if (checkMd5) {

                        ((Activity) context).runOnUiThread(new Runnable() {

                            public void run() {
                                pDialog.setMessage(context.getResources().getString(
                                        R.string.alert_file_md5_checking,
                                        new Object[] { file.getName() }));
                            }
                        });

                        File folder = file.getParentFile();
                        File md5File = new File(folder, file.getName() + ".md5sum");
                        if (md5File.exists()) {
                            String content[] = Files.readMd5File(md5File);
                            if (!file.getName().equals(content[1])) {
                                pDialog.dismiss();
                                showAlertOnUIThread(R.string.alert_file_alert,
                                        R.string.alert_file_incorrect_md5_file,
                                        new Object[] { file.getName() });
                                return;
                            }
                            String md5 = Files.md5(file);
                            if (!md5.equals(content[0])) {
                                pDialog.dismiss();
                                showAlertOnUIThread(R.string.alert_file_alert,
                                        R.string.alert_file_incorrect_md5,
                                        new Object[] { file.getName() });
                                return;
                            }
                        }
                    }
                }

                pDialog.dismiss();
                ((Activity) context).runOnUiThread(new Runnable() {

                    public void run() {
                        showRebootDialog();
                    }
                });
            }
        }).start();
    }

    public void showBackupDialog() {
        showBackupDialog(true, false, false, false, false, true);
    }

    public void showRestoreDialog() {
        Context context = getCore().getContext();

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.alert_restore_title);

        RecoveryPlugin rPlugin = (RecoveryPlugin) getCore().getPlugin(Core.PLUGIN_RECOVERY);

        final String backupFolder = rPlugin.getBackupDir(false);
        final String[] backups = rPlugin.getBackupList();
        mSelectedBackup = backups.length > 0 ? 0 : -1;

        alert.setSingleChoiceItems(backups, mSelectedBackup, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                mSelectedBackup = which;
            }
        });

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                if (mSelectedBackup >= 0) {
                    reboot(false, false, false, false, null, null, backupFolder
                            + backups[mSelectedBackup]);
                }
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();

    }

    public void showRestoreDialog(final String path) {
        Context context = getCore().getContext();

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.alert_restore_title);
        alert.setMessage(context.getResources().getString(R.string.alert_restore_message, path));

        RecoveryPlugin rPlugin = (RecoveryPlugin) getCore().getPlugin(Core.PLUGIN_RECOVERY);

        final String backupFolder = rPlugin.getBackupDir(false);

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                reboot(false, false, false, false, null, null, backupFolder + path);
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();

    }

    public void restore(String path) {
        reboot(false, false, false, false, null, null, path);
    }

    public void simpleReboot() {
        reboot(false, false, false, false, null, null, null, true);
    }

    public void simpleReboot(boolean wipeData, boolean wipeCaches, boolean fixPermissions) {
        FileItemStore.removeItems();
        reboot(false, wipeData, wipeCaches, fixPermissions, null, null, null, false);
    }

    public void fixPermissions() {
        FileItemStore.removeItems();
        reboot(false, false, false, true, null, null, null, false);
    }

    private void showBackupDialog(final boolean removePreferences,
            final boolean wipeSystem, final boolean wipeData, final boolean wipeCaches,
            final boolean fixPermissions, final boolean onlyBackup) {
        final Context context = getCore().getContext();

        Preferences prefs = getCore().getPreferences();
        StoragePlugin sPlugin = (StoragePlugin) getCore().getPlugin(Core.PLUGIN_STORAGE);
        double checkSpace = prefs.getSpaceLeft();
        if (checkSpace > 0) {
            double spaceLeft = sPlugin.getSpaceLeft();
            if (spaceLeft < checkSpace) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle(R.string.alert_backup_space_title);
                alert.setMessage(context.getResources().getString(
                        R.string.alert_backup_space_message, checkSpace));

                alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();

                        reallyShowBackupDialog(removePreferences, wipeSystem, wipeData,
                                wipeCaches, fixPermissions, onlyBackup);
                    }
                });

                alert.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alert.show();
            } else {
                reallyShowBackupDialog(removePreferences, wipeSystem, wipeData,
                        wipeCaches, fixPermissions, onlyBackup);
            }
        } else {
            reallyShowBackupDialog(removePreferences, wipeSystem, wipeData, wipeCaches,
                    fixPermissions, onlyBackup);
        }
    }

    private void reallyShowBackupDialog(boolean removePreferences,
            final boolean wipeSystem, final boolean wipeData, final boolean wipeCaches,
            final boolean fixPermissions, final boolean onlyBackup) {
        Context context = getCore().getContext();

        if (removePreferences) {
            FileItemStore.removeItems();
        }

        final boolean isONandroid = onlyBackup && getCore().getPreferences().isUseONandroid();

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.alert_backup_title);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_backup,
                (ViewGroup) ((Activity) context).findViewById(R.id.backup_dialog_layout));
        alert.setView(view);

        if (isONandroid) {
            ((TextView) view.findViewById(R.id.text)).setText(R.string.alert_backup_message_onandroid);
        }

        final CheckBox cbSystem = (CheckBox) view.findViewById(R.id.system);
        final CheckBox cbData = (CheckBox) view.findViewById(R.id.data);
        final CheckBox cbCache = (CheckBox) view.findViewById(R.id.cache);
        final CheckBox cbRecovery = (CheckBox) view.findViewById(R.id.recovery);
        final CheckBox cbBoot = (CheckBox) view.findViewById(R.id.boot);
        final CheckBox cbSecure = (CheckBox) view.findViewById(R.id.androidsecure);
        final CheckBox cbSdext = (CheckBox) view.findViewById(R.id.sdext);
        final EditText input = (EditText) view.findViewById(R.id.backupname);

        input.setText(Strings.getDateAndTime());
        input.selectAll();

        final RecoveryPlugin rPlugin = (RecoveryPlugin) getCore().getPlugin(Core.PLUGIN_RECOVERY);
        if (rPlugin.getRecovery().getId() == RecoveryInfo.RECOVERY_TWRP) {
            if (!Files.hasAndroidSecure()) {
                cbSecure.setVisibility(View.GONE);
            }
            if (!Files.hasSdExt()) {
                cbSdext.setVisibility(View.GONE);
            }
        } else {
            cbSystem.setVisibility(View.GONE);
            cbData.setVisibility(View.GONE);
            cbCache.setVisibility(View.GONE);
            cbRecovery.setVisibility(View.GONE);
            cbBoot.setVisibility(View.GONE);
            cbSecure.setVisibility(View.GONE);
            cbSdext.setVisibility(View.GONE);
        }

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                String text = input.getText().toString();
                text = text.replaceAll("[^a-zA-Z0-9.-]", "");

                String backupOptions = null;
                if (rPlugin.getRecovery().getId() == RecoveryInfo.RECOVERY_TWRP) {
                    backupOptions = "";
                    if (cbSystem.isChecked()) {
                        backupOptions += "S";
                    }
                    if (cbData.isChecked()) {
                        backupOptions += "D";
                    }
                    if (cbCache.isChecked()) {
                        backupOptions += "C";
                    }
                    if (cbRecovery.isChecked()) {
                        backupOptions += "R";
                    }
                    if (cbBoot.isChecked()) {
                        backupOptions += "B";
                    }
                    if (cbSecure.isChecked()) {
                        backupOptions += "A";
                    }
                    if (cbSdext.isChecked()) {
                        backupOptions += "E";
                    }

                    if ("".equals(backupOptions)) {
                        return;
                    }
                }

                if (isONandroid) {
                    getCore().getONandroid().doBackup(text, backupOptions);
                } else {
                    reboot(wipeSystem, wipeData, wipeCaches, fixPermissions, text,
                            backupOptions, null);
                }
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    protected void showRebootDialog() {

        if (FileItemStore.size() == 0) {
            return;
        }

        final Context context = getCore().getContext();

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.alert_reboot_title);

        final InstallOptionsCursor cursor = new InstallOptionsCursor(getCore());

        alert.setMultiChoiceItems(cursor, cursor.getIsCheckedColumn(), cursor.getLabelColumn(),
                new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        cursor.setOption(which, isChecked);
                    }

                });

        alert.setPositiveButton(R.string.alert_reboot_now, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                if (cursor.isBackup()) {
                    showBackupDialog(false, cursor.isWipeSystem(), cursor.isWipeData(),
                            cursor.isWipeCaches(), cursor.isFixPermissions(), false);
                } else {
                    reboot(cursor.isWipeSystem(), cursor.isWipeData(), cursor.isWipeCaches(),
                            cursor.isFixPermissions(), null, null, null);
                }

            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void reboot(boolean wipeSystem, boolean wipeData, boolean wipeCaches,
            boolean fixPermissions, String backupFolder,
            String backupOptions, String restore) {
        reboot(wipeSystem, wipeData, wipeCaches, fixPermissions, backupFolder,
                backupOptions, restore, false);
    }

    private void reboot(final boolean wipeSystem, final boolean wipeData, final boolean wipeCaches,
            final boolean fixPermissions, final String backupFolder,
            final String backupOptions, final String restore, final boolean skipCommands) {

        if (wipeSystem && getCore().getPreferences().isShowSystemWipeAlert()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getCore().getContext());
            alert.setTitle(R.string.alert_wipe_system_title);
            alert.setMessage(R.string.alert_wipe_system_message);

            alert.setPositiveButton(R.string.alert_reboot_now,
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();

                            _reboot(wipeSystem, wipeData, wipeCaches, fixPermissions,
                                    backupFolder, backupOptions, restore, skipCommands);

                        }
                    });

            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();
        } else {
            _reboot(wipeSystem, wipeData, wipeCaches, fixPermissions, backupFolder,
                    backupOptions, restore, skipCommands);
        }

    }

    private void _reboot(boolean wipeSystem, boolean wipeData, boolean wipeCaches,
            boolean fixPermissions, String backupFolder, String backupOptions,
            String restore, boolean skipCommands) {
        try {

            if (fixPermissions) {
                fixPermissions = prepareFixPermissions();
            }

            RecoveryPlugin rPlugin = (RecoveryPlugin) getCore().getPlugin(Core.PLUGIN_RECOVERY);

            boolean isCwmOfficial = rPlugin.getRecovery().getId() == RecoveryInfo.RECOVERY_CWM;

            Process p = null;
            DataOutputStream os = null;

            if (!isCwmOfficial) {
                p = Runtime.getRuntime().exec("su");
                os = new DataOutputStream(p.getOutputStream());

                os.writeBytes("rm -f /cache/recovery/command\n");
                os.writeBytes("rm -f /cache/recovery/extendedcommand\n");
                os.writeBytes("rm -f /cache/recovery/openrecoveryscript\n");
            }

            if (!skipCommands) {

                Preferences prefs = getCore().getPreferences();
                prefs.setToDelete(new String[0]);
                int size = FileItemStore.size();
                List<String> list = new ArrayList<String>();
                for (int i = 0; i < size; i++) {
                    FileItem item = FileItemStore.getItem(i);
                    if (item.isDelete()) {
                        list.add(item.getPath());
                    }
                }
                prefs.setToDelete(
                        list.toArray(new String[list.size()]));

                String file = rPlugin.getCommandsFile();

                String[] commands = rPlugin.getCommands(wipeSystem, wipeData, wipeCaches,
                        fixPermissions, backupFolder, backupOptions, restore);
                if (commands != null) {
                    size = commands.length;
                    for (int i = 0; i < size; i++) {
                        os.writeBytes("echo '" + commands[i] + "' >> /cache/recovery/" + file
                                + "\n");
                    }
                }
            }

            if (!isCwmOfficial) {
                os.writeBytes("/system/bin/touch /cache/recovery/boot\n");
                os.writeBytes("reboot recovery\n");
    
                os.writeBytes("sync\n");
                os.writeBytes("exit\n");
                os.flush();
                p.waitFor();

                Context context = getCore().getContext();

                if (isSystemApp()) {
                    ((PowerManager) context.getSystemService(Activity.POWER_SERVICE))
                            .reboot("recovery");
                } else {
                    Runtime.getRuntime().exec("/system/bin/reboot recovery");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean prepareFixPermissions() {

        String data = Files.readAssets(getCore().getContext(), "fix_permissions.sh");

        if (data != null
                && Files.writeToFile(data, "/data/data/com.beerbong.zipinst/files/",
                        "fix_permissions.sh")) {

            SuperUserPlugin sPlugin = (SuperUserPlugin) getCore().getPlugin(Core.PLUGIN_SUPERUSER);
            sPlugin.run(
                            "cp /data/data/com.beerbong.zipinst/files/fix_permissions.sh /cache/fix_permissions.sh");

            return true;
        }
        return false;
    }

    private boolean isSystemApp() throws Exception {
        if (mIsSystemApp > -1) {
            return mIsSystemApp == 1;
        }
        Context context = getCore().getContext();
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageInfo("com.beerbong.zipinst", PackageManager.GET_ACTIVITIES);
        ApplicationInfo aInfo = info.applicationInfo;
        String path = aInfo.sourceDir.substring(0, aInfo.sourceDir.lastIndexOf("/"));
        mIsSystemApp = path.contains("system/app") ? 1 : 0;
        return mIsSystemApp == 1;
    }

    private void showAlertOnUIThread(final int titleId, final int messageId,
            final Object[] messageParams) {

        final Activity activity = (Activity) getCore().getContext();

        activity.runOnUiThread(new Runnable() {

            public void run() {
                AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                alert.setTitle(titleId);
                alert.setMessage(activity.getResources().getString(messageId, messageParams));
                alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        });
    }

}
