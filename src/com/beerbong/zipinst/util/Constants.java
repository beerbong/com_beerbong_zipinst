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

package com.beerbong.zipinst.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Constants {

    // install options
    public static final String INSTALL_BACKUP = "BACKUP";
    public static final String INSTALL_WIPESYSTEM = "WIPESYSTEM";
    public static final String INSTALL_WIPEDATA = "WIPEDATA";
    public static final String INSTALL_WIPECACHES = "WIPECACHES";
    public static final String INSTALL_FIXPERM = "FIXPERM";
    public static final String[] INSTALL_OPTIONS = {
            INSTALL_BACKUP,
            INSTALL_WIPESYSTEM,
            INSTALL_WIPEDATA,
            INSTALL_WIPECACHES,
            INSTALL_FIXPERM };

    // about preferences
    public static final String PREFERENCE_ABOUT_VERSION = "about_version";
    public static final String PREFERENCE_ABOUT_SITE = "about_pref";
    public static final String PREFERENCE_ABOUT_LICENSE = "license_pref";
    public static final String PREFERENCE_ABOUT_CHANGELOG = "changelog_pref";

    // settings preferences
    public static final String PREFERENCE_SETTINGS_RECOVERY = "recovery";
    public static final String PREFERENCE_SETTINGS_INTERNAL_SDCARD = "internalsdcard";
    public static final String PREFERENCE_SETTINGS_EXTERNAL_SDCARD = "externalsdcard";
    public static final String PREFERENCE_SETTINGS_DAD = "draganddrop";
    public static final String PREFERENCE_SETTINGS_DARK_THEME = "darktheme";
    public static final String PREFERENCE_SETTINGS_CHECK_EXISTS = "checkexists";
    public static final String PREFERENCE_SETTINGS_CHECK_UPDATE_STARTUP = "updateonstartup";
    public static final String PREFERENCE_SETTINGS_CHECK_MD5 = "checkmd5";
    public static final String PREFERENCE_SETTINGS_OVERRIDE_LIST = "overridelist";
    public static final String PREFERENCE_SETTINGS_AUTOLOAD_LIST = "autoloadlist";
    public static final String PREFERENCE_SETTINGS_DOWNLOAD_PATH = "downloadpath";
    public static final String PREFERENCE_SETTINGS_ZIP_POSITION = "zipposition";
    public static final String PREFERENCE_SETTINGS_OPTIONS = "show-options";
    public static final String PREFERENCE_SETTINGS_SPACE_LEFT = "spaceleft";
    public static final String PREFERENCE_SETTINGS_SYSTEMWIPE_ALERT = "wipesystemalert";
    public static final String PREFERENCE_SETTINGS_USE_FOLDER = "usefolder";
    public static final String PREFERENCE_SETTINGS_FOLDER = "folder";

    // recovery preferences
    public static final String PREFERENCE_RECOVERY_BACKUP = "recovery_activity_backup";
    public static final String PREFERENCE_RECOVERY_RESTORE = "recovery_activity_restore";
    public static final String PREFERENCE_RECOVERY_DELETE = "recovery_activity_delete";
    public static final String PREFERENCE_RECOVERY_ACTIONS = "recovery_activity_actions";
    public static final String PREFERENCE_RECOVERY_REBOOT = "recovery_activity_reboot";

    public static final String SEARCH_URL = "http://goo.im/json2&action=search&query=ZipInstaller";
    public static final String DOWNLOAD_URL = "http://goo.im/devs/beerbong/apps/ZipInstaller/";

    public static final String DONATE_URL = "http://forum.xda-developers.com/donatetome.php?u=1806623";
    public static final String PRO_URL = "https://play.google.com/store/apps/details?id=com.beerbong.zipinst";
    public static final String ABOUT_URL = "http://forum.xda-developers.com/showthread.php?t=1920057";

    public static final int REQUEST_PICK_FILE = 203;

    public static final int NOTIFICATION_ID = 122303221;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");

    private static final char[] HEX_DIGITS = new char[] {
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'a',
            'b',
            'c',
            'd',
            'e',
            'f' };

    private static final long K = 1024;
    private static final long M = K * K;
    private static final long G = M * K;
    private static final long T = G * K;

    private static int isSystemApp = -1;

    public static String getDateAndTime() {
        return SDF.format(new Date(System.currentTimeMillis()));
    }

    public static String formatSize(final long value) {
        final long[] dividers = new long[] { T, G, M, K, 1 };
        final String[] units = new String[] { "TB", "GB", "MB", "KB", "B" };
        if (value < 1)
            throw new IllegalArgumentException("Invalid file size: " + value);
        String result = null;
        for (int i = 0; i < dividers.length; i++) {
            final long divider = dividers[i];
            if (value >= divider) {
                result = format(value, divider, units[i]);
                break;
            }
        }
        return result;
    }

    public static String formatDate(final long value) {
        return SDF.format(value);
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

    private static String format(final long value, final long divider, final String unit) {
        final double result = divider > 1 ? (double) value / (double) divider : (double) value;
        return String.format("%.1f %s", Double.valueOf(result), unit);
    }

    public static String md5(File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read = 0;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String md5 = bigInt.toString(16);
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }
            return md5;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
    }

    public static String byteArrToStr(byte[] bytes) {
        StringBuffer str = new StringBuffer();
        for (int q = 0; q < bytes.length; q++) {
            str.append(HEX_DIGITS[(0xF0 & bytes[q]) >>> 4]);
            str.append(HEX_DIGITS[0xF & bytes[q]]);
        }
        return str.toString();
    }

    public static String getProperty(String prop) {
        try {
            String output = null;
            Process p = Runtime.getRuntime().exec("getprop " + prop);
            p.waitFor();
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            output = input.readLine();
            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}