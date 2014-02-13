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

package com.beerbong.zipinst.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.beerbong.zipinst.NotificationAlarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class SystemProperties {

    private static final int ALARM_ID = 122303221;

    public static String getProperty(String prop) {
        try {
            Process p = Runtime.getRuntime().exec("getprop " + prop);
            p.waitFor();
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = input.readLine();
            return "".equals(line) ? null : line;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    public static void setAlarm(Context context, long time, boolean trigger) {

        Intent i = new Intent(context, NotificationAlarm.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pi = PendingIntent
                .getBroadcast(context, ALARM_ID, i, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        if (time > 0) {
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, trigger ? 0 : time, time, pi);
        }
    }

    public static boolean alarmExists(Context context) {
        return (PendingIntent.getBroadcast(context, ALARM_ID, new Intent(context,
                NotificationAlarm.class), PendingIntent.FLAG_NO_CREATE) != null);
    }
}
