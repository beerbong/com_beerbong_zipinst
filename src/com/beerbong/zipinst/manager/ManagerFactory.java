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

package com.beerbong.zipinst.manager;

import com.beerbong.zipinst.manager.pro.ProManagerImpl;
import com.beerbong.zipinst.util.NoSuException;

import android.app.Activity;
import android.content.Context;

public class ManagerFactory {

    private static ProManager mProManager;
    private static FileManager mFileManager;
    private static RebootManager mRebootManager;
    private static MenuManager mMenuManager;
    private static RecoveryManager mRecoveryManager;
    private static PreferencesManager mPreferencesManager;
    private static UpdateManager mUpdateManager;
    private static SUManager mSUManager;
    private static DownloadManager mDownloadManager;

    public static void start(Activity mActivity) throws NoSuException {
        mProManager = new ProManagerImpl(mActivity);
        mFileManager = new FileManager(mActivity);
        mRebootManager = new RebootManager(mActivity);
        mMenuManager = new MenuManager(mActivity);
        mRecoveryManager = new RecoveryManager(mActivity);
        if (mPreferencesManager == null)
            mPreferencesManager = new PreferencesManager(mActivity);
        mUpdateManager = new UpdateManager(mActivity);
        mSUManager = new SUManager(mActivity);
        mDownloadManager = new DownloadManager(mActivity);
    }

    public static FileManager getFileManager() {
        return mFileManager;
    }

    public static RebootManager getRebootManager() {
        return mRebootManager;
    }

    public static MenuManager getMenuManager() {
        return mMenuManager;
    }

    public static RecoveryManager getRecoveryManager() {
        return mRecoveryManager;
    }

    public static PreferencesManager getPreferencesManager() {
        return mPreferencesManager;
    }

    public static PreferencesManager getPreferencesManager(Context context) {
        if (mPreferencesManager == null)
            mPreferencesManager = new PreferencesManager(context);
        return mPreferencesManager;
    }

    public static UpdateManager getUpdateManager() {
        return mUpdateManager;
    }

    public static SUManager getSUManager() {
        return mSUManager;
    }

    public static SUManager getSUManager(Context context) {
        if (mSUManager == null)
            mSUManager = new SUManager(context);
        return mSUManager;
    }

    public static ProManager getProManager() {
        return mProManager;
    }

    public static ProManager getProManager(Context context) {
        if (mProManager == null)
            mProManager = new ProManagerImpl(context);
        return mProManager;
    }

    public static DownloadManager getDownloadManager() {
        return mDownloadManager;
    }
}
