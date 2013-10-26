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

package com.beerbong.zipinst;

import java.io.File;

import com.beerbong.zipinst.manager.ManagerFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String[] files = ManagerFactory.getPreferencesManager(context).getToDelete();
        for (int i = 0; i < files.length; i++) {
            if (!"".equals(files[i].trim())) {
                File file = new File(files[i]);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        // call the pro receiver if available
        try {
            Class<?> pClass = Class.forName("com.beerbong.zipinst.pro.Receiver");
            BroadcastReceiver mProReceiver = (BroadcastReceiver) pClass.newInstance();
            mProReceiver.onReceive(context, intent);
        } catch (Throwable t) {
            // sorry, you are not a pro
        }
    }

}
