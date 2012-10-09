package com.beerbong.zipinst.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class Recovery {

    public static String[] getCWMCommands(Activity mActivity, boolean[] wipeOptions) throws Exception {
        List<String> commands = new ArrayList();

        int size = StoredPreferences.size(), i = 0;

        String internalStorage = mActivity.getSharedPreferences(Constants.PREFS_NAME, 0).getString(Constants.PROPERTY_INTERNAL_STORAGE, Constants.DEFAULT_INTERNAL_STORAGE);

        commands.add("ui_print(\"-------------------------------------\");");
        commands.add("ui_print(\" ZipInstaller " + mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0).versionName + "\");");
        commands.add("ui_print(\"-------------------------------------\");");

        if (wipeOptions[0]) {
            commands.add("ui_print(\" Wiping data\");");
            commands.add("format(\"/data\");");
            commands.add("ui_print(\" Wiping android secure\");");
            commands.add("__system(\"rm -r /" + internalStorage + "/.android_secure\");");
        }
        if (wipeOptions[1]) {
            commands.add("ui_print(\" Wiping cache\");");
            commands.add("format(\"/cache\");");
            commands.add("ui_print(\" Wiping dalvik cache\");");
            commands.add("__system(\"rm -r /data/dalvik-cache\");");
        }

        commands.add("ui_print(\" Installing zips\");");
        for (;i<size;i++) {
            commands.add("install_zip(\"" + StoredPreferences.getPreference(i).getKey() + "\");");
        }

        commands.add("ui_print(\" Rebooting\");");

        return commands.toArray(new String[commands.size()]);
    }

    public static String[] getTWRPCommands(Activity mActivity, boolean[] wipeOptions) throws Exception {
        List<String> commands = new ArrayList();

        int size = StoredPreferences.size(), i = 0;

        String internalStorage = mActivity.getSharedPreferences(Constants.PREFS_NAME, 0).getString(Constants.PROPERTY_INTERNAL_STORAGE, Constants.DEFAULT_INTERNAL_STORAGE);

        commands.add("print -------------------------------------");
        commands.add("print  ZipInstaller " + mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0).versionName);
        commands.add("print -------------------------------------");

        if (wipeOptions[0]) {
            commands.add("print  Wiping data");
            commands.add("wipe data");
            //commands.add("print  Wiping android secure");
            //commands.add("__system(\"rm -r /" + internalStorage + "/.android_secure");
        }
        if (wipeOptions[1]) {
            commands.add("print  Wiping cache");
            commands.add("wipe cache");
            commands.add("print  Wiping dalvik cache");
            commands.add("wipe dalvik");
        }

        commands.add("print  Installing zips");
        for (;i<size;i++) {
            commands.add("install " + StoredPreferences.getPreference(i).getKey());
        }

        commands.add("print  Rebooting");

        return commands.toArray(new String[commands.size()]);
}
}