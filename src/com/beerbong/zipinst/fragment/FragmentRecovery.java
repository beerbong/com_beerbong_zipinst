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

package com.beerbong.zipinst.fragment;

import java.io.File;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.plugins.reboot.RebootPlugin;
import com.beerbong.zipinst.core.plugins.recovery.RecoveryPlugin;
import com.beerbong.zipinst.core.plugins.superuser.SuperUserPlugin;
import com.beerbong.zipinst.io.Files;
import com.beerbong.zipinst.ui.UIPreferenceFragment;

public class FragmentRecovery extends UIPreferenceFragment {

    private static final String PREFERENCE_BACKUP = "recovery_fragment_backup";
    private static final String PREFERENCE_RESTORE = "recovery_fragment_restore";
    private static final String PREFERENCE_DELETE = "recovery_fragment_delete";
    private static final String PREFERENCE_WIPE_DATA = "recovery_fragment_wipe_data";
    private static final String PREFERENCE_WIPE_CACHES = "recovery_fragment_wipe_caches";
    private static final String PREFERENCE_FIX_PERMISSIONS = "recovery_fragment_fix_permissions";
    private static final String PREFERENCE_REBOOT = "recovery_fragment_reboot";

    private int mSelectedBackup;

    @Override
    public int[] getVisibleMenuItems() {
        return null;
    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_recovery;
    }

    @Override
    public void create(boolean isNew) {
    }

    @Override
    public void restore(Bundle savedInstanceState) {
    }

    @Override
    public void save(Bundle outState) {
    }

    @Override
    public int getTitle() {
        return R.string.recovery_fragment_title;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        final String key = preference.getKey();

        final Core core = getCore();
        final Context context = core.getContext();
        Resources res = context.getResources();

        final RebootPlugin rebootPlugin = (RebootPlugin) core.getPlugin(Core.PLUGIN_REBOOT);
        if (PREFERENCE_BACKUP.equals(key)) {
            rebootPlugin.showBackupDialog();
            return true;
        } else if (PREFERENCE_RESTORE.equals(key)) {
            rebootPlugin.showRestoreDialog();
            return true;
        } else if (PREFERENCE_DELETE.equals(key)) {
            showDeleteDialog();
            return true;
        }

        String message = "";
        if (PREFERENCE_WIPE_DATA.equals(key)) {
            message = res.getString(R.string.recovery_fragment_reboot,
                    res.getString(R.string.recovery_fragment_wipe_data));
        } else if (PREFERENCE_WIPE_CACHES.equals(key)) {
            message = res.getString(R.string.recovery_fragment_reboot,
                    res.getString(R.string.recovery_fragment_wipe_caches));
        } else if (PREFERENCE_FIX_PERMISSIONS.equals(key)) {
            message = res.getString(R.string.recovery_fragment_reboot,
                    res.getString(R.string.recovery_fragment_fix_permissions));
        } else if (PREFERENCE_REBOOT.equals(key)) {
            message = res.getString(R.string.recovery_fragment_reboot_question);
        }

        new AlertDialog.Builder(context)
                .setTitle(R.string.recovery_fragment_title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (PREFERENCE_WIPE_DATA.equals(key)) {
                            rebootPlugin.simpleReboot(true, false, false);
                        } else if (PREFERENCE_WIPE_CACHES.equals(key)) {
                            rebootPlugin.simpleReboot(false, true, false);
                        } else if (PREFERENCE_FIX_PERMISSIONS.equals(key)) {
                            rebootPlugin.simpleReboot(false, false, true);
                        } else if (PREFERENCE_REBOOT.equals(key)) {
                            rebootPlugin.simpleReboot();
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).show();

        return true;
    }

    private void showDeleteDialog() {

        Core core = getCore();
        final Context context = core.getContext();

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.alert_delete_title);

        RecoveryPlugin rPlugin = (RecoveryPlugin) core.getPlugin(Core.PLUGIN_RECOVERY);
        final String backupFolder = rPlugin.getBackupDir(true);
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
                    final String toDelete = backupFolder + backups[mSelectedBackup];

                    final ProgressDialog pDialog = new ProgressDialog(context);
                    pDialog.setIndeterminate(true);
                    pDialog.setMessage(context.getResources().getString(
                            R.string.alert_deleting_folder, new Object[] {
                                backups[mSelectedBackup]
                            }));
                    pDialog.setCancelable(false);
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.show();

                    (new Thread() {

                        public void run() {

                            boolean deleted = Files.recursiveDelete(new File(toDelete));
                            if (!deleted) {
                                SuperUserPlugin sPlugin = (SuperUserPlugin) getCore().getPlugin(
                                        Core.PLUGIN_SUPERUSER);
                                sPlugin.run("rm -r " + toDelete);
                            }

                            pDialog.dismiss();
                        }
                    }).start();
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

}
