package com.beerbong.zipinst.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.SharedPreferences;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.RecoveryInfo;
import com.beerbong.zipinst.util.StoredPreferences;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class RecoveryManager {
    
    private Activity mActivity;
    private Map<Integer, RecoveryInfo> recoveries = new HashMap();

    protected RecoveryManager(Activity activity) {
        mActivity = activity;
        
        recoveries.put(R.id.cwmbased, new RecoveryInfo(R.id.cwmbased, "cwmbased", "emmc"));
        recoveries.put(R.id.twrp, new RecoveryInfo(R.id.twrp, "twrp", "sdcard"));
        recoveries.put(R.id.fourext, new RecoveryInfo(R.id.fourext, "fourext", "sdcard"));
    }

    public RecoveryInfo getRecovery() {
        String recovery = mActivity.getSharedPreferences(Constants.PREFS_NAME, 0).getString(Constants.PROPERTY_RECOVERY, Constants.DEFAULT_RECOVERY);
        Iterator<Integer> it = recoveries.keySet().iterator();
        while (it.hasNext()) {
            int id = it.next();
            RecoveryInfo info = recoveries.get(id);
            if (info.getName().equals(recovery)) {
                return info;
            }
        }
        return null;
    }
    public void setRecovery(int id) {
        RecoveryInfo info = recoveries.get(id);
        SharedPreferences settings = mActivity.getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.PROPERTY_RECOVERY, info.getName());
        editor.putString(Constants.PROPERTY_INTERNAL_STORAGE, info.getSdcard());
        editor.commit();
    }
    public String getCommandsFile() {

        RecoveryInfo info = getRecovery();
        
        switch (info.getId()) {
            case R.id.cwmbased : return "extendedcommand";
            case R.id.twrp : return "openrecoveryscript";
            default : return null;
        }
    }
    public String[] getPreviousCommands() throws Exception {
        List<String> commands = new ArrayList();
        
        RecoveryInfo info = getRecovery();
        
        switch (info.getId()) {
            case R.id.cwmbased :
                commands.add("mkdir -p /sdcard/clockworkmod");
                commands.add("echo 1 > /sdcard/clockworkmod/.recoverycheckpoint");
                break;
        }

        return commands.toArray(new String[commands.size()]);
    }
    public String[] getCommands(boolean[] wipeOptions) throws Exception {
        List<String> commands = new ArrayList();

        int size = StoredPreferences.size(), i = 0;
        
        RecoveryInfo info = getRecovery();
        
        switch (info.getId()) {
            case R.id.cwmbased :
            case R.id.fourext :

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
                    commands.add("assert(install_zip(\"" + StoredPreferences.getPreference(i).getKey() + "\"));");
                }

                commands.add("ui_print(\" Rebooting\");");
                break;
                
            case R.id.twrp :
                commands.add("print -------------------------------------");
                commands.add("print  ZipInstaller " + mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0).versionName);
                commands.add("print -------------------------------------");

                if (wipeOptions[0]) {
                    commands.add("print  Wiping data");
                    commands.add("wipe data");
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
                break;
        }

        return commands.toArray(new String[commands.size()]);
    }
}