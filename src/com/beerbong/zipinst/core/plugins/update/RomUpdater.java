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

package com.beerbong.zipinst.core.plugins.update;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.view.WindowManager.BadTokenException;

import com.beerbong.zipinst.IntroActivity;
import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.plugins.update.Updater.RomInfo;
import com.beerbong.zipinst.core.plugins.update.impl.GooUpdater;
import com.beerbong.zipinst.core.plugins.update.impl.OUCUpdater;
import com.beerbong.zipinst.http.DownloadFile;
import com.beerbong.zipinst.io.SystemProperties;
import com.beerbong.zipinst.preferences.Preferences;
import com.beerbong.zipinst.ui.widget.Dialog;
import com.beerbong.zipinst.ui.widget.Dialog.OnDialogClosedListener;

public class RomUpdater implements Updater.UpdaterListener {

    public static final String EXTRA_NOTIFICATION_ID = "NOTIFICATION_ID";
    public static final String EXTRA_NAME = "NAME";
    public static final String EXTRA_URL = "URL";
    public static final String EXTRA_MD5 = "MD5";

    private static final int NOTIFICATION_ID = 122303221;

    private Core mCore;
    private Context mContext;
    private Updater mUpdater;
    private String mRomName;
    private int mRomVersion = -1;
    private boolean mFromAlarm;

    public RomUpdater(Core core) {

        mCore = core;

        mFromAlarm = false;

        mUpdater = getUpdater();

        if (mUpdater != null) {
            mRomName = mUpdater.getRomName();
            mRomVersion = mUpdater.getRomVersion();
        }
    }

    public RomUpdater(Context context) {

        mContext = context;

        mFromAlarm = true;

        mUpdater = getUpdater();

        if (mUpdater != null) {
            mRomName = mUpdater.getRomName();
            mRomVersion = mUpdater.getRomVersion();
        }
    }

    public boolean canUpdate() {
        if (mRomName != null && mRomVersion > 0) {
            return true;
        }
        return false;
    }

    public void check() {
        if (!canUpdate() || mUpdater.isScanning()) {
            return;
        }
        mUpdater.searchVersion();
    }

    @Override
    public void versionFound(RomInfo info) {
        if (info != null && info.version > mRomVersion) {
            if (!mFromAlarm) {
                showNewRomFound(info);
            } else {
                Preferences prefs = new Preferences(mContext);
                if (prefs.isAcceptNotifications()) {
                    showNotification(info);
                }
            }
        } else {
            if (!mFromAlarm) {
                Dialog.toast(mCore.getContext(), R.string.check_rom_updates_no_new);
            }
        }
    }

    @Override
    public void versionError(String error) {
        if (!mFromAlarm) {
            if (error != null) {
                Dialog.toast(
                        mCore.getContext(),
                        mCore.getContext().getResources()
                                .getString(R.string.check_rom_updates_error)
                                + ": " + error);
            } else {
                Dialog.toast(mCore.getContext(), R.string.check_rom_updates_error);
            }
        }
    }

    private void showNewRomFound(final RomInfo info) {
        final Context context = mCore.getContext();
        String message = context.getResources().getString(R.string.new_rom_found_summary,
                new Object[] { info.filename, info.folder });
        try {
            Dialog.dialog(context, message, R.string.new_rom_found_title, true, new OnDialogClosedListener() {
    
                @Override
                public void dialogOk() {
                    ((Activity) context).runOnUiThread(new Runnable() {
    
                        public void run() {
                            new DownloadFile(mCore, info.path, info.filename, info.md5);
                        }
                    });
                }
    
                @Override
                public void dialogCancel() {
                }
    
            });
        } catch (BadTokenException ex) {
            // ignore
        }
    }

    private void showNotification(RomInfo info) {

        Resources resources = mContext.getResources();

        Intent intent = new Intent(mContext, IntroActivity.class);
        intent.putExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID);
        intent.putExtra(EXTRA_URL, info.path);
        intent.putExtra(EXTRA_NAME, info.filename);
        intent.putExtra(EXTRA_MD5, info.md5);
        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(mContext)
                .setContentTitle(resources.getString(R.string.new_rom_found_title))
                .setContentText(
                        resources.getString(R.string.new_rom_name, new Object[] {
                            info.filename
                        }))
                .setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntent);

        Notification noti = null;
        if (VERSION.SDK_INT > 15) {
            noti = builder.build();
        } else {
            noti = builder.getNotification();
        }

        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Service.NOTIFICATION_SERVICE);

        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(NOTIFICATION_ID, noti);
    }

    private Updater getUpdater() {
        if (SystemProperties.getProperty(GooUpdater.PROPERTY_GOO_DEVELOPER) != null
                && SystemProperties.getProperty(GooUpdater.PROPERTY_GOO_ROM) != null
                && SystemProperties.getProperty(GooUpdater.PROPERTY_GOO_VERSION) != null) {
            return new GooUpdater(this);
        }
        if (SystemProperties.getProperty(OUCUpdater.PROPERTY_OTA_ID) != null
                && SystemProperties.getProperty(OUCUpdater.PROPERTY_OTA_TIME) != null
                && SystemProperties.getProperty(OUCUpdater.PROPERTY_OTA_VER) != null) {
            return new OUCUpdater(this);
        }
        return null;
    }
}