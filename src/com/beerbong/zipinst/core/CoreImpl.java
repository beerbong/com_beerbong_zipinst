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

package com.beerbong.zipinst.core;

import android.content.Context;
import android.util.Log;

import com.beerbong.zipinst.cloud.CloudStorage;
import com.beerbong.zipinst.cloud.DriveStorage;
import com.beerbong.zipinst.cloud.DropboxStorage;
import com.beerbong.zipinst.core.plugins.license.LicensePlugin;
import com.beerbong.zipinst.core.plugins.reboot.RebootPlugin;
import com.beerbong.zipinst.core.plugins.recovery.RecoveryPlugin;
import com.beerbong.zipinst.core.plugins.storage.StoragePlugin;
import com.beerbong.zipinst.core.plugins.superuser.SuperUserPlugin;
import com.beerbong.zipinst.core.plugins.ui.UIPlugin;
import com.beerbong.zipinst.core.plugins.update.UpdatePlugin;
import com.beerbong.zipinst.io.Version;
import com.beerbong.zipinst.onandroid.ONandroid;
import com.beerbong.zipinst.preferences.Preferences;
import com.beerbong.zipinst.ui.UIActivity;

public class CoreImpl implements Core {

    private static final String TAG = "CoreImpl";

    private Context mContext;
    private Preferences mPreferences;
    private CloudStorage mCloudStorage;
    private ONandroid mONandroid;
    private CoreListener mListener;
    private Version mVersion;

    private Plugin[] mPlugins;
    private int mCurrentPlugin = -1;

    private long mCurrentTime = -1L;

    private boolean mStarted = false;

    protected CoreImpl(Context context) {
        this(context, null);
    }

    protected CoreImpl(Context context, CoreListener listener) {
        mContext = context;
        mListener = listener;

        mPreferences = new Preferences(this);
        mONandroid = new ONandroid(this);

        mVersion = new Version(context);

        mPlugins = new Plugin[] {
                new UIPlugin(this),
                new SuperUserPlugin(this),
                new StoragePlugin(this),
                new RecoveryPlugin(this),
                new RebootPlugin(this),
                new LicensePlugin(this),
                new UpdatePlugin(this) };

        mCurrentTime = System.currentTimeMillis();
        nextPlugin(true);

    }

    @Override
    public void moveToInstall() {
        ((UIActivity) mContext).moveToStart();
    }

    @Override
    public void destroy() {
        if (!mStarted) {
            return;
        }
        mCurrentPlugin = -1;
        mCurrentTime = System.currentTimeMillis();
        nextPlugin(false);
    }

    private void onStarted() {
        mStarted = true;
        if (mListener != null) {
            mListener.coreStarted();
        }
    }

    private void onStopped() {
        if (mListener != null) {
            mListener.coreStopped();
        }
    }

    @Override
    public boolean isStarted() {
        return mStarted;
    }

    @Override
    public Version getVersion() {
        return mVersion;
    }

    @Override
    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public Plugin getPlugin(String name) {
        for (Plugin plugin : mPlugins) {
            if (name.equals(plugin.getName())) {
                return plugin;
            }
        }
        return null;
    }

    public void setMessage(int resId) {
        if (mListener != null) {
            mListener.coreMessage(resId);
        }
    }

    @Override
    public Preferences getPreferences() {
        return mPreferences;
    }

    @Override
    public ONandroid getONandroid() {
        return mONandroid;
    }

    @Override
    public CloudStorage getCloudStorage() {
        int type = mPreferences.getCloudStorage();
        if (type != CloudStorage.STORAGE_NONE && mCloudStorage == null) {
            setCloudStorage(type);
        }
        return mCloudStorage;
    }

    @Override
    public void setCloudStorage(int cloudStorage) {
        if (mCloudStorage != null && mCloudStorage.getType() == cloudStorage) {
            return;
        }
        switch (cloudStorage) {
            case CloudStorage.STORAGE_DROPBOX :
                mCloudStorage = new DropboxStorage(this);
                break;
            case CloudStorage.STORAGE_DRIVE :
                mCloudStorage = new DriveStorage(this);
                break;
            default :
                mCloudStorage = null;
                break;
        }
    }

    protected void nextPlugin(boolean start) {
        mCurrentPlugin++;
        if (mCurrentPlugin >= mPlugins.length) {
            nextPlugin(mCurrentPlugin - 1, start);
            if (start) {
                onStarted();
            } else {
                onStopped();
            }
            return;
        } else if (mCurrentPlugin > 0) {
            nextPlugin(mCurrentPlugin - 1, start);
            mCurrentTime = System.currentTimeMillis();
        }
        if (start) {
            mPlugins[mCurrentPlugin].start();
        } else {
            mPlugins[mCurrentPlugin].stop();
        }
    }

    private void nextPlugin(int plugin, boolean start) {
        String name = mPlugins[plugin].getName();
        long time = System.currentTimeMillis();
        time = time - mCurrentTime;
        Log.d(TAG, name + " " + (start ? "started" : "stopped") + " in " + time + "ms");
        if (mListener != null) {
            if (start) {
                mListener.pluginStarted(name);
            } else {
                mListener.pluginStopped(name);
            }
        }
    }
}
