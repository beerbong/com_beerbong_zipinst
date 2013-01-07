package com.beerbong.zipinst.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private SparseArray<RecoveryInfo> recoveries = new SparseArray<RecoveryInfo>();

    protected RecoveryManager(Activity activity) {
        mActivity = activity;
        
        recoveries.put(R.id.cwmbased, new RecoveryInfo(R.id.cwmbased, "cwmbased", "emmc"));
        recoveries.put(R.id.twrp, new RecoveryInfo(R.id.twrp, "twrp", "sdcard"));
        recoveries.put(R.id.fourext, new RecoveryInfo(R.id.fourext, "fourext", "sdcard"));
        
        if (!mActivity.getSharedPreferences(Constants.PREFS_NAME, 0).contains(Constants.PROPERTY_RECOVERY)) {
            test(R.id.fourext);
        }
    }

    public void selectRecovery(Activity activity) {
        View view = LayoutInflater.from(activity).inflate(R.xml.recovery, (ViewGroup)activity.findViewById(R.id.recovery_layout));

        RadioButton cbCwmbased = (RadioButton)view.findViewById(R.id.cwmbased);
        RadioButton cbTwrp = (RadioButton)view.findViewById(R.id.twrp);
        RadioButton cb4ext = (RadioButton)view.findViewById(R.id.fourext);
        
        final RadioGroup mGroup = (RadioGroup)view.findViewById(R.id.recovery_radio_group);
        
        RecoveryInfo info = getRecovery();
        switch (info.getId()) {
            case R.id.cwmbased :
                cbCwmbased.setChecked(true);
                break;
            case R.id.twrp :
                cbTwrp.setChecked(true);
                break;
            case R.id.fourext :
                cb4ext.setChecked(true);
                break;
        }

        new AlertDialog.Builder(activity)
            .setTitle(R.string.recovery_alert_title)
            .setMessage(R.string.recovery_alert_summary)
            .setView(view)
            .setPositiveButton(R.string.recovery_alert_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    
                    int id = mGroup.getCheckedRadioButtonId();
                    
                    setRecovery(id);
                    
                    dialog.dismiss();
                }
            }).setNegativeButton(R.string.recovery_alert_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            }).show();
    }
    public void selectSdcard(final Activity activity) {
        final EditText input = new EditText(activity);
        input.setText(activity.getSharedPreferences(Constants.PREFS_NAME, 0).getString(Constants.PROPERTY_INTERNAL_STORAGE, Constants.DEFAULT_INTERNAL_STORAGE));

        new AlertDialog.Builder(activity)
            .setTitle(R.string.sdcard_alert_title)
            .setMessage(R.string.sdcard_alert_summary)
            .setView(input)
            .setPositiveButton(R.string.sdcard_alert_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = input.getText().toString();

                    if (value == null || "".equals(value.trim())) {
                        Toast.makeText(activity, R.string.sdcard_alert_error, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }

                    if (value.startsWith("/")) {
                        value = value.substring(1);
                    }

                    SharedPreferences settings = activity.getSharedPreferences(Constants.PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(Constants.PROPERTY_INTERNAL_STORAGE, value);
                    editor.commit();
                    dialog.dismiss();
                }
            }).setNegativeButton(R.string.sdcard_alert_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            }).show();
    }
    public RecoveryInfo getRecovery() {
        String recovery = mActivity.getSharedPreferences(Constants.PREFS_NAME, 0).getString(Constants.PROPERTY_RECOVERY, Constants.DEFAULT_RECOVERY);
        for (int i=0;i<recoveries.size();i++) {
            int key = recoveries.keyAt(i);
            RecoveryInfo info = recoveries.get(key);
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
    public String getBackupDir() {

        RecoveryInfo info = getRecovery();

        String sdcard = "sdcard";
        String str = "";
        
        switch (info.getId()) {
            case R.id.twrp :
                File f = new File("/" + sdcard + "/TWRP/BACKUPS/");
                if (f.exists()) {
                    File[] fs = f.listFiles();
                    str += fs[0].getName() + "/";
                }
                break;
        }
        return str;
    }
    public String[] getBackupList() {

        RecoveryInfo info = getRecovery();

        String sdcard = "sdcard";
        String folder = "";
        
        switch (info.getId()) {
            case R.id.cwmbased :
            case R.id.fourext :
                folder = "/" + sdcard + "/clockworkmod/backup/";
                break;
            case R.id.twrp :
                folder = "/" + sdcard + "/TWRP/BACKUPS/";
                File f = new File(folder);
                if (f.exists()) {
                    File[] fs = f.listFiles();
                    folder += fs[0].getName() + "/";
                }
                break;
        }
        
        List<String> list = new ArrayList<String>();
        
        File f = new File(folder);
        if (f.exists()) {
            File[] fs = f.listFiles();
            for (int i=0;i<fs.length;i++) {
                list.add(fs[i].getName());
            }
        }
        
        Collections.sort(list);
        
        return list.toArray(new String[list.size()]);
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
    public String[] getCommands(boolean[] wipeOptions, String backupFolder, String restore) throws Exception {
        List<String> commands = new ArrayList<String>();

        int size = StoredPreferences.size(), i = 0;
        
        RecoveryInfo info = getRecovery();

        String internalStorage = mActivity.getSharedPreferences(Constants.PREFS_NAME, 0).getString(Constants.PROPERTY_INTERNAL_STORAGE, Constants.DEFAULT_INTERNAL_STORAGE);
        
        boolean wipeData = false, wipeCaches = false;
        if (wipeOptions != null) {
            boolean showBackup = mActivity.getSharedPreferences(Constants.PREFS_NAME, 0).getBoolean(Constants.PROPERTY_SHOW_BACKUP, Constants.DEFAULT_SHOW_BACKUP);
            if (showBackup) {
                wipeData = wipeOptions[1];
                wipeCaches = wipeOptions[2];
            } else {
                wipeData = wipeOptions[0];
                wipeCaches = wipeOptions[1];
            }
        }
        
        switch (info.getId()) {
            case R.id.cwmbased :
            case R.id.fourext :

                commands.add("ui_print(\"-------------------------------------\");");
                commands.add("ui_print(\" ZipInstaller " + mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0).versionName + "\");");
                commands.add("ui_print(\"-------------------------------------\");");
                
                if (restore != null) {
                    commands.add("ui_print(\" Restore ROM\");");
                    commands.add("restore_rom(\"/" + internalStorage + "/clockworkmod/backup/" + restore + "\", \"boot\", \"system\", \"data\", \"cache\", \"sd-ext\")");
                }
                
                if (backupFolder != null) {
                    commands.add("ui_print(\" Backup ROM\");");
                    commands.add("backup_rom(\"/" + internalStorage + "/clockworkmod/backup/" + backupFolder + "\");");
                }

                if (wipeOptions != null) {
                    if (wipeData) {
                        commands.add("ui_print(\" Wiping data\");");
                        commands.add("format(\"/data\");");
                        commands.add("ui_print(\" Wiping android secure\");");
                        commands.add("format(\"/" + internalStorage + "/.android_secure\");");
                    }
                    if (wipeCaches) {
                        commands.add("ui_print(\" Wiping cache\");");
                        commands.add("format(\"/cache\");");
                        commands.add("ui_print(\" Wiping dalvik cache\");");
                        commands.add("format(\"/data/dalvik-cache\");");
                        commands.add("format(\"/cache/dalvik-cache\");");
                        commands.add("format(\"/sd-ext/dalvik-cache\");");
                    }
                }

                if (size > 0) {
                    commands.add("ui_print(\" Installing zips\");");
                    for (;i<size;i++) {
                        commands.add("assert(install_zip(\"" + StoredPreferences.getPreference(i).getKey() + "\"));");
                    }
                }

                commands.add("ui_print(\" Rebooting\");");
                break;
                
            case R.id.twrp :

                String sdcard = "sdcard";
                
                if (restore != null) {
                    String str = "restore /" + internalStorage + "/TWRP/BACKUPS/" + restore + " SDCR123B";
                    if (folderExists("/" + sdcard + "/.android-secure")) {
                        str += "A";
                    }
                    if (folderExists("/sd-ext")) {
                        str += "E";
                    }
                    commands.add(str);
                }
                
                if (backupFolder != null) {
                    String str = "backup SDCR123B";
                    if (folderExists("/" + sdcard + "/.android-secure")) {
                        str += "A";
                    }
                    if (folderExists("/sd-ext")) {
                        str += "E";
                    }
                    commands.add(str + "O " + backupFolder);
                }

                if (wipeOptions != null) {
                    if (wipeData) {
                        commands.add("wipe data");
                    }
                    if (wipeCaches) {
                        commands.add("wipe cache");
                        commands.add("wipe dalvik");
                    }
                }

                for (;i<size;i++) {
                    commands.add("install " + StoredPreferences.getPreference(i).getKey());
                }

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
                String sdcard = "sdcard";
                path = "/" + sdcard + "/TWRP/";
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
    private boolean folderExists(String path) {
        File f = new File(path);
        return f.exists() && f.isDirectory();
    }
}