/*
 * Copyright (C) 2013 ZipInstaller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.beerbong.zipinst.manager;

import java.io.DataOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
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
    public void onCreateOptionsMenu(Menu menu) {
    }

    @Override
    public void onOptionsItemSelected(MenuItem item) {
    }

    @Override
    public void onNewIntent(Intent intent) {
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
                    reboot(false, false, false, false, null, backupFolder
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
        reboot(false, false, false, false, null, null, true);
    }

    public void fixPermissions() {
        StoredItems.removeItems();
        reboot(false, false, false, true, null, null, false);
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
        alert.setMessage(R.string.alert_backup_message);

        final EditText input = new EditText(mContext);
        alert.setView(input);
        input.setText(Constants.getDateAndTime());
        input.selectAll();

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                String text = input.getText().toString();
                text = text.replace(" ", "");

                reboot(wipeSystem, wipeData, wipeCaches, false, text, null);
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
                            cursor.isFixPermissions(), null, null);
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
            boolean fixPermissions, String backupFolder, String restore) {
        reboot(wipeSystem, wipeData, wipeCaches, fixPermissions, backupFolder, restore, false);
    }

    private void reboot(final boolean wipeSystem, final boolean wipeData, final boolean wipeCaches,
            final boolean fixPermissions, final String backupFolder, final String restore,
            final boolean skipCommands) {

        if (wipeSystem && ManagerFactory.getPreferencesManager().isShowSystemWipeAlert()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            alert.setTitle(R.string.alert_wipe_system_title);
            alert.setMessage(R.string.alert_wipe_system_message);

            alert.setPositiveButton(R.string.alert_reboot_now,
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();

                            _reboot(wipeSystem, wipeData, wipeCaches, fixPermissions, backupFolder,
                                    restore, skipCommands);

                        }
                    });

            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();
        } else {
            _reboot(wipeSystem, wipeData, wipeCaches, fixPermissions, backupFolder, restore,
                    skipCommands);
        }

    }

    private void _reboot(boolean wipeSystem, boolean wipeData, boolean wipeCaches,
            boolean fixPermissions, String backupFolder, String restore, boolean skipCommands) {
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
                        fixPermissions, backupFolder, restore);
                if (commands != null) {
                    int size = commands.length, i = 0;
                    for (; i < size; i++) {
                        os.writeBytes("echo '" + commands[i] + "' >> /cache/recovery/" + file
                                + "\n");
                    }
                }
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