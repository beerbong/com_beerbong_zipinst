package com.beerbong.zipinst.manager;

import android.app.Activity;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class Manager {
    
    private static FileManager fileManager;
    private static RebootManager rebootManager;
    private static MenuManager menuManager;
    private static RecoveryManager recoveryManager;

    public static void start(Activity mActivity) {
        fileManager = new FileManager(mActivity);
        rebootManager = new RebootManager(mActivity);
        menuManager = new MenuManager(mActivity);
        recoveryManager = new RecoveryManager(mActivity);
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
}
