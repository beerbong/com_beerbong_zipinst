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

package com.beerbong.zipinst.activities;

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
        } else if (Constants.PREFERENCE_RECOVERY_REBOOT.equals(key)) {
            ManagerFactory.getRebootManager().simpleReboot();
        }
        return true;
    }
}