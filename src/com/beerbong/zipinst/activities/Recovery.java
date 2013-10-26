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

package com.beerbong.zipinst.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.widget.PreferenceActivity;

public class Recovery extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState, R.layout.recovery_activity);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if (Constants.PREFERENCE_RECOVERY_BACKUP.equals(key)) {
            ManagerFactory.getRebootManager().showBackupDialog(this);
        } else if (Constants.PREFERENCE_RECOVERY_RESTORE.equals(key)) {
            ManagerFactory.getRebootManager().showRestoreDialog(this);
        } else if (Constants.PREFERENCE_RECOVERY_DELETE.equals(key)) {
            ManagerFactory.getFileManager().showDeleteDialog(this);
        } else if (Constants.PREFERENCE_RECOVERY_ACTIONS.equals(key)) {
            showAlert();
        } else if (Constants.PREFERENCE_RECOVERY_REBOOT.equals(key)) {
            ManagerFactory.getRebootManager().simpleReboot();
        }
        return true;
    }

    private void showAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.recovery_activity_actions);
        
        String[] items = {
                getResources().getString(R.string.wipe_data),
                getResources().getString(R.string.wipe_caches),
                getResources().getString(R.string.fix_permissions)
        };
        final boolean[] checkedItems = new boolean[3];
        alert.setMultiChoiceItems(items, checkedItems, new OnMultiChoiceClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                checkedItems[which] = isChecked;
            }
            
        });
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                dialog.dismiss();
                
                ManagerFactory.getRebootManager().simpleReboot(checkedItems[0], checkedItems[1], checkedItems[2]);
            }
        });
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
}