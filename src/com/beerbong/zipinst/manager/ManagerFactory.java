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
}
