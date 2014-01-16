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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.widget.Toast;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.manager.PreferencesManager;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

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
    public static final String PREFERENCE_ABOUT_DONATE = "donate_pref";

    // settings preferences
    public static final String PREFERENCE_SETTINGS_RECOVERY = "recovery";
    public static final String PREFERENCE_SETTINGS_INTERNAL_SDCARD = "internalsdcard";
    public static final String PREFERENCE_SETTINGS_EXTERNAL_SDCARD = "externalsdcard";
    public static final String PREFERENCE_SETTINGS_FORCE_EXTERNAL_SDCARD = "mountextsdcard";
    public static final String PREFERENCE_SETTINGS_BACKUP_EXTERNAL_SDCARD = "backupextsdcard";
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
    public static final String PREFERENCE_SETTINGS_RULES = "rules";
    public static final String PREFERENCE_SETTINGS_CHECK_ROOT = "checkroot";

    // recovery preferences
    public static final String PREFERENCE_RECOVERY_BACKUP = "recovery_activity_backup";
    public static final String PREFERENCE_RECOVERY_RESTORE = "recovery_activity_restore";
    public static final String PREFERENCE_RECOVERY_DELETE = "recovery_activity_delete";
    public static final String PREFERENCE_RECOVERY_ACTIONS = "recovery_activity_actions";
    public static final String PREFERENCE_RECOVERY_REBOOT = "recovery_activity_reboot";

    public static final String LOGIN_URL = "http://goo-inside.me/salt";
    public static final String SEARCH_URL = "http://goo.im/json2&action=search&query=ZipInstaller";
    public static final String DOWNLOAD_URL = "http://goo.im/devs/beerbong/apps/ZipInstaller/";

    public static final String DONATE_URL = "http://forum.xda-developers.com/donatetome.php?u=1806623";
    public static final String PRO_URL = "https://play.google.com/store/apps/details?id=com.beerbong.zipinst";
    public static final String ABOUT_URL = "http://forum.xda-developers.com/showthread.php?t=1920057";

    public static final String MIME_TYPE = (Build.VERSION.SDK_INT < 19) ? "file/*" : "application/zip";

    public static final int REQUEST_PICK_FILE = 203;
    public static final int REQUEST_ACCOUNT_PICKER = 204;
    public static final int REQUEST_AUTHORIZATION = 205;

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

    // dropbox
    private static String sDropboxKey = "30sf9jomssqj6x8";
    private static String sDropboxSecret = null;
    private static AccessType sDropboxAccess = AccessType.APP_FOLDER;

    private static int sIsSystemApp = -1;

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
        if (sIsSystemApp > -1) {
            return sIsSystemApp == 1;
        }
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageInfo("com.beerbong.zipinst", PackageManager.GET_ACTIVITIES);
        ApplicationInfo aInfo = info.applicationInfo;
        String path = aInfo.sourceDir.substring(0, aInfo.sourceDir.lastIndexOf("/"));
        sIsSystemApp = path.contains("system/app") ? 1 : 0;
        return sIsSystemApp == 1;
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

    public static String replace(String original, String starts, String replace) {
        return !original.startsWith(starts) ? original : replace
                + original.substring(starts.length());
    }

    public static void showToastOnUiThread(final Context context, final int resourceId) {
        showToastOnUiThread(context, context.getResources().getString(resourceId));
    }

    public static void showToastOnUiThread(final Context context, final String message) {
        ((Activity) context).runOnUiThread(new Runnable() {

            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static boolean hasAndroidSecure() {
        String sdcard = "sdcard";
        return folderExists("/" + sdcard + "/.android-secure");
    }

    public static boolean hasSdExt() {
        return folderExists("/sd-ext");
    }

    public static String getSBINFolder() {
        if (folderExists("/sbin")) {
            return "/sbin/";
        } else if (folderExists("/system/sbin")) {
            return "/system/sbin/";
        }
        return null;
    }

    private static boolean folderExists(String path) {
        File f = new File(path);
        return f.exists() && f.isDirectory();
    }

    public static void showError(Context context, int messageId) {
        showError(context, context.getResources().getString(messageId));
    }

    public static void showError(Context context, String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.error_title);
        alert.setMessage(message);
        alert.setCancelable(false);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    public static DropboxAPI<AndroidAuthSession> createDropboxAPI(Context context) {
        AppKeyPair appKeys = new AppKeyPair(sDropboxKey, getDropboxSecret(context));
        AndroidAuthSession session = new AndroidAuthSession(appKeys, sDropboxAccess);
        DropboxAPI<AndroidAuthSession> dBApi = new DropboxAPI<AndroidAuthSession>(session);
        PreferencesManager pManager = ManagerFactory.getPreferencesManager();
        String key = pManager.getDropboxKey();
        String secret = pManager.getDropboxSecret();
        if (key != null && secret != null) {
            AccessTokenPair access = new AccessTokenPair(key, secret);
            dBApi.getSession().setAccessTokenPair(access);
        }
        return dBApi;
    }

    private static String getDropboxSecret(Context context) {
        if (sDropboxSecret == null) {
            sDropboxSecret = ManagerFactory.getFileManager().readAssets(context, "dropbox");
        }
        return sDropboxSecret;
    }
}