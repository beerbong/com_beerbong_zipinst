package com.beerbong.zipinst.pro;

import com.beerbong.zipinst.manager.ManagerFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ProConstants.setAlarm(context, ManagerFactory.getPreferencesManager(context)
                .getTimeNotifications(), true);
    }

}