package com.beerbong.zipinst.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.Toast;

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
        
        if (!mActivity.getSharedPreferences(Constants.PREFS_NAME, 0).contains(Constants.PROPERTY_RECOVERY)) {
            test(R.id.fourext);
        }
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
            case R.id.cwmbased :
            case R.id.fourext :
                return "extendedcommand";
            case R.id.twrp : return "openrecoveryscript";
            default : return null;
        }
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
                    commands.add("format(\"/" + internalStorage + "/.android_secure\");");
                }
                if (wipeOptions[1]) {
                    commands.add("ui_print(\" Wiping cache\");");
                    commands.add("format(\"/cache\");");
                    commands.add("ui_print(\" Wiping dalvik cache\");");
                    commands.add("format(\"/data/dalvik-cache\");");
                    commands.add("format(\"/cache/dalvik-cache\");");
                    commands.add("format(\"/sd-ext/dalvik-cache\");");
                }

                commands.add("ui_print(\" Installing zips\");");
                for (;i<size;i++) {
                    commands.add("assert(install_zip(\"" + StoredPreferences.getPreference(i).getKey() + "\"));");
                }

                commands.add("ui_print(\" Rebooting\");");
                break;
                
            case R.id.twrp :
//                commands.add("print -------------------------------------");
//                commands.add("print  ZipInstaller " + mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0).versionName);
//                commands.add("print -------------------------------------");

                if (wipeOptions[0]) {
//                    commands.add("print  Wiping data");
                    commands.add("wipe data");
                }
                if (wipeOptions[1]) {
//                    commands.add("print  Wiping cache");
                    commands.add("wipe cache");
//                    commands.add("print  Wiping dalvik cache");
                    commands.add("wipe dalvik");
                }

//                commands.add("print  Installing zips");
                for (;i<size;i++) {
                    commands.add("install " + StoredPreferences.getPreference(i).getKey());
                }

//                commands.add("print  Rebooting");
                break;
        }

        return commands.toArray(new String[commands.size()]);
    }
    
    private void test(final int id) {
        
        String name = null, path = null;
        
        switch (id) {
            case R.id.fourext :
                name = mActivity.getString(R.string.recovery_4ext);
                path = "/cache/4ext/";
                break;
            case R.id.twrp :
                name = mActivity.getString(R.string.recovery_twrp);
                path = "/sdcard/TWRP/";
                break;
            case R.id.cwmbased :
                setRecovery(R.id.cwmbased);
                Toast.makeText(mActivity, mActivity.getString(R.string.recovery_changed, mActivity.getString(R.string.recovery_cwm)), Toast.LENGTH_LONG).show();
                return;
        }
        
        final String recoveryName = name;
        
        File folder = new File(path);
        if (folder.exists()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
            alert.setTitle(R.string.recovery_change_alert_title);
            alert.setMessage(mActivity.getString(R.string.recovery_change_alert_message, recoveryName));
            alert.setPositiveButton(R.string.recovery_alert_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                    setRecovery(id);
                    Toast.makeText(mActivity, mActivity.getString(R.string.recovery_changed, recoveryName), Toast.LENGTH_LONG).show();
                }
            });
            alert.setNegativeButton(R.string.recovery_alert_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    switch (id) {
                        case R.id.fourext :
                            test(R.id.twrp);
                            break;
                        case R.id.twrp :
                            test(R.id.cwmbased);
                            break;
                    }
                }
            });
            alert.show();
        } else {
            switch (id) {
                case R.id.fourext :
                    test(R.id.twrp);
                    break;
                case R.id.twrp :
                    test(R.id.cwmbased);
                    break;
            }
        }
    }
}