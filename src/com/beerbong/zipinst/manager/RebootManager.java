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

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.ui.UIListener;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.FileItem;
import com.beerbong.zipinst.util.InstallOptionsCursor;
import com.beerbong.zipinst.util.StoredItems;

public class RebootManager extends Manager implements UIListener {

    private Context mContext;
    private int mSelectedBackup;

    protected RebootManager(Context context) {
        super(context);

        mContext = context;

        UI.getInstance().addUIListener(this);
    }

    public void onButtonClicked(int id) {

        if (id == R.id.install_now) {
            ManagerFactory.getFileManager().checkFilesAndMd5(this);
        }
    }

    @Override
    public void onFileItemClicked(FileItem item) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu) {
    }

    @Override
    public void onOptionsItemSelected(MenuItem item) {
    }

    @Override
    public void onNewIntent(Intent intent) {
    }

    @Override
    public void onPause() {
    }

    public void showBackupDialog(Context context) {
        showBackupDialog(context, true, false, false, false, false);
    }

    public void showRestoreDialog(Context context) {

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.alert_restore_title);

        final String backupFolder = ManagerFactory.getRecoveryManager().getBackupDir(false);
        final String[] backups = ManagerFactory.getRecoveryManager().getBackupList();
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

    public void simpleReboot() {
        reboot(false, false, false, false, null, null, null, true);
    }

    public void simpleReboot(boolean wipeData, boolean wipeCaches, boolean fixPermissions) {
        StoredItems.removeItems();
        reboot(false, wipeData, wipeCaches, fixPermissions, null, null, null, false);
    }

    public void fixPermissions() {
        StoredItems.removeItems();
        reboot(false, false, false, true, null, null, null, false);
    }

    private void showBackupDialog(final Context context, final boolean removePreferences,
            final boolean wipeSystem, final boolean wipeData, final boolean wipeCaches,
            final boolean fixPermissions) {

        double checkSpace = ManagerFactory.getPreferencesManager().getSpaceLeft();
        if (checkSpace > 0) {
            double spaceLeft = ManagerFactory.getFileManager().getSpaceLeft();
            if (spaceLeft < checkSpace) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle(R.string.alert_backup_space_title);
                alert.setMessage(context.getResources().getString(
                        R.string.alert_backup_space_message, checkSpace));

                alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();

                        reallyShowBackupDialog(context, removePreferences, wipeSystem, wipeData,
                                wipeCaches, fixPermissions);
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
                reallyShowBackupDialog(context, removePreferences, wipeSystem, wipeData,
                        wipeCaches, fixPermissions);
            }
        } else {
            reallyShowBackupDialog(context, removePreferences, wipeSystem, wipeData, wipeCaches,
                    fixPermissions);
        }
    }

    private void reallyShowBackupDialog(Context context, boolean removePreferences,
            final boolean wipeSystem, final boolean wipeData, final boolean wipeCaches,
            final boolean fixPermissions) {
        if (removePreferences)
            UI.getInstance().removeAllItems();

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.alert_backup_title);
        View view = LayoutInflater.from(context).inflate(R.layout.backup_dialog,
                (ViewGroup) ((Activity) context).findViewById(R.id.backup_dialog_layout));
        alert.setView(view);

        final CheckBox cbSystem = (CheckBox) view.findViewById(R.id.system);
        final CheckBox cbData = (CheckBox) view.findViewById(R.id.data);
        final CheckBox cbCache = (CheckBox) view.findViewById(R.id.cache);
        final CheckBox cbRecovery = (CheckBox) view.findViewById(R.id.recovery);
        final CheckBox cbBoot = (CheckBox) view.findViewById(R.id.boot);
        final CheckBox cbSecure = (CheckBox) view.findViewById(R.id.androidsecure);
        final CheckBox cbSdext = (CheckBox) view.findViewById(R.id.sdext);
        final EditText input = (EditText) view.findViewById(R.id.backupname);

        input.setText(Constants.getDateAndTime());
        input.selectAll();

        final RecoveryManager rManager = ManagerFactory.getRecoveryManager();
        if (rManager.getRecovery().getId() == R.id.twrp) {
            if (!rManager.hasAndroidSecure()) {
                cbSecure.setVisibility(View.GONE);
            }
            if (!rManager.hasSdExt()) {
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
                text = text.replace(" ", "");

                String backupOptions = null;
                if (rManager.getRecovery().getId() == R.id.twrp) {
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

                reboot(wipeSystem, wipeData, wipeCaches, fixPermissions, text,
                        backupOptions, null);
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

        if (StoredItems.size() == 0)
            return;

        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(R.string.alert_reboot_title);

        final InstallOptionsCursor cursor = new InstallOptionsCursor(mContext);

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
                    showBackupDialog(mContext, false, cursor.isWipeSystem(), cursor.isWipeData(),
                            cursor.isWipeCaches(), cursor.isFixPermissions());
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

        if (wipeSystem && ManagerFactory.getPreferencesManager().isShowSystemWipeAlert()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
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

            RecoveryManager manager = ManagerFactory.getRecoveryManager();

            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());

            os.writeBytes("rm -f /cache/recovery/command\n");
            os.writeBytes("rm -f /cache/recovery/extendedcommand\n");
            os.writeBytes("rm -f /cache/recovery/openrecoveryscript\n");

            if (!skipCommands) {
                String file = manager.getCommandsFile();

                String[] commands = manager.getCommands(wipeSystem, wipeData, wipeCaches,
                        fixPermissions, backupFolder, backupOptions, restore);
                if (commands != null) {
                    int size = commands.length, i = 0;
                    for (; i < size; i++) {
                        os.writeBytes("echo '" + commands[i] + "' >> /cache/recovery/" + file
                                + "\n");
                    }
                }

                ManagerFactory.getPreferencesManager().setToDelete(new String[0]);
                int size = StoredItems.size();
                List<String> list = new ArrayList<String>();
                for (int i = 0; i < size; i++) {
                    FileItem item = StoredItems.getItem(i);
                    if (item.isDelete()) {
                        list.add(item.getPath());
                    }
                }
                ManagerFactory.getPreferencesManager().setToDelete(
                        list.toArray(new String[list.size()]));
            }

            os.writeBytes("/system/bin/touch /cache/recovery/boot\n");
            os.writeBytes("reboot recovery\n");

            os.writeBytes("sync\n");
            os.writeBytes("exit\n");
            os.flush();
            p.waitFor();

            if (Constants.isSystemApp(mContext)) {
                ((PowerManager) mContext.getSystemService(Activity.POWER_SERVICE))
                        .reboot("recovery");
            } else {
                Runtime.getRuntime().exec("/system/bin/reboot recovery");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean prepareFixPermissions() {

        FileManager fManager = ManagerFactory.getFileManager();

        String data = fManager.readAssets(mContext, "fix_permissions.sh");

        if (data != null
                && fManager.writeToFile(data, "/data/data/com.beerbong.zipinst/files/",
                        "fix_permissions.sh")) {

            ManagerFactory
                    .getSUManager()
                    .runWaitFor(
                            "cp /data/data/com.beerbong.zipinst/files/fix_permissions.sh /cache/fix_permissions.sh");

            return true;
        }
        return false;
    }
}