package com.beerbong.zipinst.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class Constants {

    public static final String PREFS_NAME = "ZipInstallerPrefsFile";

    public static final String PROPERTY_INTERNAL_STORAGE = "internal-storage";
    public static final String DEFAULT_INTERNAL_STORAGE = "emmc";
    public static final String PROPERTY_RECOVERY = "recovery";
    public static final String DEFAULT_RECOVERY = "cwmbased";
    public static final String PROPERTY_LIST = "list";
    public static final String PROPERTY_DRAG_AND_DROP = "drag-and-drop";
    public static final String PROPERTY_SHOW_BACKUP = "show-backup";
    public static final boolean DEFAULT_DRAG_AND_DROP = true;
    public static final boolean DEFAULT_SHOW_BACKUP = true;

    // main preferences
    public static final String PREFERENCE_FILE_LIST = "file_list";
    public static final String PREFERENCE_CHOOSE_ZIP = "choose_zip";
    public static final String PREFERENCE_INSTALL_NOW = "install_now";

    // about preferences
    public static final String PREFERENCE_ABOUT_VERSION = "about_version";
    public static final String PREFERENCE_ABOUT_SITE = "about_pref";
    public static final String PREFERENCE_ABOUT_LICENSE = "license_pref";
    public static final String PREFERENCE_ABOUT_CHANGELOG = "changelog_pref";
    
    // settings preferences
    public static final String PREFERENCE_SETTINGS_RECOVERY = "recovery";
    public static final String PREFERENCE_SETTINGS_SDCARD = "sdcard";
    public static final String PREFERENCE_SETTINGS_DAD = "draganddrop";
    public static final String PREFERENCE_SETTINGS_SHOW_BACKUP = "showbackup";

    public static final String DONATE_URL = "http://forum.xda-developers.com/donatetome.php?u=1806623";
    public static final String ABOUT_URL = "http://forum.xda-developers.com/showthread.php?t=1920057";

    public static final int REQUEST_PICK_ZIP = 203;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");

    private static final long K = 1024;
    private static final long M = K * K;
    private static final long G = M * K;
    private static final long T = G * K;
    
    private static int isSystemApp = -1;
    
    public static String getDateAndTime() {
        return SDF.format(new Date(System.currentTimeMillis()));
    }

    public static String formatSize(final long value){
        final long[] dividers = new long[] { T, G, M, K, 1 };
        final String[] units = new String[] { "TB", "GB", "MB", "KB", "B" };
        if(value < 1)
            throw new IllegalArgumentException("Invalid file size: " + value);
        String result = null;
        for(int i = 0; i < dividers.length; i++){
            final long divider = dividers[i];
            if(value >= divider){
                result = format(value, divider, units[i]);
                break;
            }
        }
        return result;
    }
    public static boolean isSystemApp(Context context) throws Exception {
        if (isSystemApp > -1) {
            return isSystemApp == 1;
        }
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageInfo("com.beerbong.zipinst", PackageManager.GET_ACTIVITIES);
        ApplicationInfo aInfo = info.applicationInfo;
        String path = aInfo.sourceDir.substring(0, aInfo.sourceDir.lastIndexOf("/"));
        isSystemApp = path.contains("system/app") ? 1 : 0;
        return isSystemApp == 1;
    }

    private static String format(final long value, final long divider, final String unit){
        final double result = divider > 1 ? (double) value / (double) divider : (double) value;
        return String.format("%.1f %s", Double.valueOf(result), unit);
    }
}