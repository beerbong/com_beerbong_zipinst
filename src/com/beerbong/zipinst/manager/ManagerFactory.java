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

import android.app.Activity;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class ManagerFactory {

    private static FileManager fileManager;
    private static RebootManager rebootManager;
    private static MenuManager menuManager;
    private static RecoveryManager recoveryManager;
    private static PreferencesManager preferencesManager;
    private static UpdateManager updateManager;

    public static void start(Activity mActivity) {
        fileManager = new FileManager(mActivity);
        rebootManager = new RebootManager(mActivity);
        menuManager = new MenuManager(mActivity);
        recoveryManager = new RecoveryManager(mActivity);
        if (preferencesManager == null)
            preferencesManager = new PreferencesManager(mActivity);
        updateManager = new UpdateManager(mActivity);
    }

    public static FileManager getFileManager() {
        return fileManager;
    }

    public static RebootManager getRebootManager() {
        return rebootManager;
    }

    public static MenuManager getMenuManager() {
        return menuManager;
    }

    public static RecoveryManager getRecoveryManager() {
        return recoveryManager;
    }

    public static PreferencesManager getPreferencesManager() {
        return preferencesManager;
    }

    public static PreferencesManager getPreferencesManager(Activity mActivity) {
        if (preferencesManager == null)
            preferencesManager = new PreferencesManager(mActivity);
        return preferencesManager;
    }

    public static UpdateManager getUpdateManager() {
        return updateManager;
    }
}
