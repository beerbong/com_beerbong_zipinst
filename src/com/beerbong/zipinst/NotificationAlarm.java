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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.manager.ProManager;

public class NotificationAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        ProManager pManager = ManagerFactory.getProManager(context);
        if (pManager.iAmPro()) {
            // call the pro BroadcastReceiver if available
            try {
                Class<?> pClass = Class.forName("com.beerbong.zipinst.pro.NotificationAlarm");
                BroadcastReceiver mProReceiver = (BroadcastReceiver) pClass.newInstance();
                mProReceiver.onReceive(context, intent);
            } catch (Throwable t) {
                // sorry, you are not a pro
            }
        }
    }
}