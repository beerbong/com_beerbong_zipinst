/*
 * Copyright 2014 ZipInstaller Project
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

package com.beerbong.zipinst.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.plugins.storage.StoragePlugin;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

public class Files {

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

    public static String[] readMd5File(File file) {
        try {
            StringBuffer fileData = new StringBuffer(1000);
            BufferedReader reader;

            reader = new BufferedReader(new FileReader(file));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
            reader.close();
            String content = fileData.toString();
            StringTokenizer st = new StringTokenizer(content, " ");
            return new String[] { st.nextToken(), st.nextToken() };
        } catch (Exception e) {
        }
        return null;
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

    public static boolean writeToFile(String data, String path, String fileName) {

        File folder = new File(path);
        File file = new File(folder, fileName);

        folder.mkdirs();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data.getBytes());
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public static String readAssets(Context contex, String fileName) {
        BufferedReader in = null;
        StringBuilder data = null;
        try {
            data = new StringBuilder(2048);
            char[] buf = new char[2048];
            int nRead = -1;
            in = new BufferedReader(new InputStreamReader(contex.getAssets().open(fileName)));
            while ((nRead = in.read(buf)) != -1) {
                data.append(buf, 0, nRead);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        if (TextUtils.isEmpty(data)) {
            return null;
        }
        return data.toString();
    }

    public static String[] readAssetsSplit(Context context, String fileName) {
        try {
            return readFileSplit(context.getAssets().open(fileName));
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String[] readFileSplit(String fileName) {
        try {
            return readFileSplit(new FileInputStream(fileName));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static String[] readFileSplit(InputStream is) {
        List<String> data = new ArrayList<String>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(new InputStreamReader(is));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                data.add(line);
            }
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return data.toArray(new String[data.size()]);
    }

    public static String findLineInFile(Context contex, File file, String regExp) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new InputStreamReader(new FileInputStream(file)));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.matches(regExp)) {
                    return line;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return null;
    }

    public static boolean recursiveDelete(File f) {
        try {
            if (f.isDirectory()) {
                File[] files = f.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (!recursiveDelete(files[i])) {
                        return false;
                    }
                }
                if (!f.delete()) {
                    return false;
                }
            } else {
                if (!f.delete()) {
                    return false;
                }
            }
        } catch (Exception ignore) {
        }
        return true;
    }

    public static String getPathFromUri(Core core, Uri uri) {
        StoragePlugin sPlugin = (StoragePlugin) core.getPlugin(Core.PLUGIN_STORAGE);
        String filePath = uri.getPath();
        if (!(new File(filePath)).exists()) {
            ContentResolver cr = core.getContext().getContentResolver();
            Cursor cursor = cr.query(uri, null, null, null, null);
            try {
                if (cursor.moveToNext()) {
                    int index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                    if (index >= 0) {
                        filePath = cursor.getString(index);
                    } else if (Build.VERSION.SDK_INT >= 19
                            && uri.toString().startsWith(ContentResolver.SCHEME_CONTENT)) {
                        String newUri = new Uri.Builder()
                                .scheme(ContentResolver.SCHEME_CONTENT)
                                .authority(uri.getAuthority()).appendPath("document")
                                .build().toString();
                        String path = uri.toString();
                        if (path.startsWith(newUri)) {
                            String firstPath = filePath.substring(0, filePath.indexOf(":"));
                            filePath = filePath.substring(filePath.indexOf(":") + 1);
                            String storage = sPlugin.getInternalStoragePath();
                            if (firstPath.indexOf(StoragePlugin.ROOT_ID_PRIMARY_EMULATED) < 0) {
                                storage = sPlugin.getExternalStoragePath();
                            }
                            filePath = storage + "/" + filePath;
                        }

                    }
                }
            } finally {
                cursor.close();
            }
        }
        return filePath;
    }
}
