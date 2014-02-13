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

package com.beerbong.zipinst.core.plugins.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.widget.Toast;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.CoreImpl;
import com.beerbong.zipinst.core.Plugin;
import com.beerbong.zipinst.core.plugins.superuser.CommandResult;
import com.beerbong.zipinst.core.plugins.superuser.SuperUserPlugin;
import com.beerbong.zipinst.preferences.Preferences;
import com.beerbong.zipinst.store.FileItemStore;

public class StoragePlugin extends Plugin {

    public static final String ROOT_ID_PRIMARY_EMULATED = "primary";

    private static boolean sScanned = false;

    private String mInternalStoragePath;
    private String mExternalStoragePath;

    public StoragePlugin(Core core) {
        super(core, Core.PLUGIN_STORAGE);
    }

    @Override
    public void start() {
        ((CoreImpl) getCore()).setMessage(R.string.reading_storage_paths);

        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                if (!sScanned) {
                    sScanned = true;
                    readMounts();
                    mExternalStoragePath = externalStorage();
                }

                return (Void) null;
            }

            @Override
            protected void onPostExecute(Void result) {
                started();
            }
        }).execute((Void) null);
    }

    @Override
    public void stop() {
        stopped();
    }

    public boolean hasExternalStorage() {
        return mExternalStoragePath != null;
//        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public boolean isExternalStorage(String path) {
        if (mInternalStoragePath == null) {
            mInternalStoragePath = "/sdcard";
        }
        return !path.startsWith(mInternalStoragePath) && !path.startsWith("/sdcard")
                && !path.startsWith("/mnt/sdcard");
    }

    public String getExternalStoragePath() {
        return mExternalStoragePath;
    }

    public String getInternalStoragePath() {
        return mInternalStoragePath;
    }

    public String getPath(String path) {
        String filePath = new String(path);

        Preferences prefs = getCore().getPreferences();
        String internalStorage = prefs.getInternalStorage();
        String externalStorage = prefs.getExternalStorage();

        String[] internalNames = new String[] {
                mInternalStoragePath,
                "/mnt/sdcard",
                "/storage/sdcard/",
                "/sdcard",
                "/storage/sdcard0",
                "/storage/emulated/0" };
        String[] externalNames = new String[] {
                mExternalStoragePath == null ? " " : mExternalStoragePath,
                "/mnt/extSdCard",
                "/storage/extSdCard/",
                "/extSdCard",
                "/storage/sdcard1",
                "/storage/emulated/1" };
        for (int i = 0; i < internalNames.length; i++) {
            String internalName = internalNames[i];
            String externalName = externalNames[i];
            boolean external = isExternalStorage(filePath);
            if (filePath.endsWith(".sh")) {
                if (!external) {
                    if (filePath.startsWith(internalName)) {
                        if (internalName.endsWith("/")) {
                            filePath = filePath.replace(internalName, "/" + "sdcard" + "/");
                        } else {
                            filePath = filePath.replace(internalName, "/" + "sdcard");
                        }
                        break;
                    }
                }
            } else {
                if (filePath.startsWith(externalName)) {
                    if (externalName.endsWith("/")) {
                        filePath = filePath.replace(externalName, "/" + externalStorage + "/");
                    } else {
                        filePath = filePath.replace(externalName, "/" + externalStorage);
                    }
                    break;
                } else if (filePath.startsWith(internalName)) {
                    if (internalName.endsWith("/")) {
                        filePath = filePath.replace(internalName, "/" + internalStorage + "/");
                    } else {
                        filePath = filePath.replace(internalName, "/" + internalStorage);
                    }
                    break;
                }
            }
        }
        if (filePath.startsWith("//")) {
            filePath = filePath.substring(1);
        }
        return filePath;
    }

    @SuppressWarnings("deprecation")
    public double getSpaceLeft() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdAvailSize = 0;
        if (Build.VERSION.SDK_INT > 17) {
            sdAvailSize = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
        } else {
            sdAvailSize = stat.getAvailableBlocks() * stat.getBlockSize();
        }
        // One binary gigabyte equals 1,073,741,824 bytes.
        return sdAvailSize / 1073741824;
    }

    public void addFileItemToStore(String filePath) {

        Context context = getCore().getContext();

        if (filePath == null || (!filePath.endsWith(".zip") && !filePath.endsWith(".sh"))) {
            Toast.makeText(context, R.string.error_file_manager_invalid_zip, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (!filePath.endsWith(".zip") && !filePath.endsWith(".sh")) {
            Toast.makeText(context, R.string.error_file_manager_zip, Toast.LENGTH_SHORT).show();
            return;
        }

        String sdcardPath = new String(filePath);

        filePath = getPath(filePath);

        File file = new File(sdcardPath);
        if (!file.exists()) {
            Toast.makeText(context, R.string.error_file_manager_not_found_zip, Toast.LENGTH_LONG)
                    .show();
        } else {

            FileItemStore.addItem(filePath, sdcardPath, false, getCore().getPreferences()
                    .getZipPosition());
        }
    }

    private void readMounts() {

        Core core = getCore();

        ArrayList<String> mounts = new ArrayList<String>();
        ArrayList<String> vold = new ArrayList<String>();

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("/proc/mounts"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.startsWith("/dev/block/vold/")) {
                    String[] lineElements = line.split(" ");
                    String element = lineElements[1];

                    mounts.add(element);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        boolean addExternal = mounts.size() == 1 && hasExternalStorage();
        if (mounts.size() == 0 || addExternal) {
            mounts.add("/mnt/sdcard");
        }

        File fstab = findFstab(core);
        if (fstab != null) {
            try {
                String path = core.getContext().getExternalCacheDir() + File.separator
                        + fstab.getName();
                SuperUserPlugin suPlugin = (SuperUserPlugin) core.getPlugin(Core.PLUGIN_SUPERUSER);
                suPlugin.run("cp " + fstab.getAbsolutePath() + " " + path);
                suPlugin.run("chmod -R 777 " + path);

                scanner = new Scanner(new File(path));
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("dev_mount")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[2];

                        if (element.contains(":")) {
                            element = element.substring(0, element.indexOf(":"));
                        }

                        if (element.toLowerCase().indexOf("usb") < 0) {
                            vold.add(element);
                        }
                    } else if (line.startsWith("/devices/platform")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[1];

                        if (element.contains(":")) {
                            element = element.substring(0, element.indexOf(":"));
                        }

                        if (element.toLowerCase().indexOf("usb") < 0) {
                            vold.add(element);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (scanner != null) {
                    scanner.close();
                }
            }
        }
        if (addExternal && (vold.size() == 1 && hasExternalStorage())) {
            mounts.add(vold.get(0));
        }
        if (vold.size() == 0 || (vold.size() == 1 && hasExternalStorage())) {
            vold.add("/mnt/sdcard");
        }

        for (int i = 0; i < mounts.size(); i++) {
            String mount = mounts.get(i);
            File root = new File(mount);
            if (!vold.contains(mount)
                    || (!root.exists() || !root.isDirectory() || !root.canWrite())) {
                mounts.remove(i--);
            }
        }

        for (int i = 0; i < mounts.size(); i++) {
            String mount = mounts.get(i);
            if (mount.indexOf("sdcard0") >= 0 || mount.equalsIgnoreCase("/mnt/sdcard")
                    || mount.equalsIgnoreCase("/sdcard")) {
                mInternalStoragePath = mount;
//            } else {
//                mExternalStoragePath = mount;
            }
        }

        if (mInternalStoragePath == null) {
            mInternalStoragePath = "/sdcard";
        }
    }

    private String externalStorage() {
        Context context = getCore().getContext();
        String dirPath = null;
        try {
            String[] volumePaths = null;
            ArrayList<String> volumePathsList = null;
            String path = null;
            if (Build.VERSION.SDK_INT >= 14) {
                volumePaths = volumePaths(context);
                if (volumePaths != null) {
                    volumePathsList = new ArrayList<String>();
                    path = Environment.getExternalStorageDirectory().getAbsolutePath();
                }
            }
            try {
                String primaryVolumePath = primaryVolumePath(context);
                int i = volumePaths.length;
                for (int j = 0;; j++)
                    if (j < i) {
                        String volumePath = volumePaths[j];
                        try {
                            if ((volumePath.equals(System.getenv("EMULATED_STORAGE_SOURCE")))
                                    || (volumePath.equals(System.getenv("EXTERNAL_STORAGE")))
                                    || (volumePath.equals(path))
                                    || (volumePath.equals(primaryVolumePath))
                                    || (volumePath.toLowerCase().contains("usb")))
                                continue;
                            volumePathsList.add(volumePath);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        if (volumePathsList.size() == 1) {
                            dirPath = (String) volumePathsList.get(0);
                        }
                        return dirPath;
                    }
            } catch (Exception ex) {
                ex.printStackTrace();

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dirPath;
    }

    private String[] volumePaths(Context context) {
        try {
            StorageManager localStorageManager = (StorageManager) context
                    .getSystemService("storage");
            return (String[]) (String[]) localStorageManager.getClass()
                    .getMethod("getVolumePaths", new Class[0])
                    .invoke(localStorageManager, new Object[0]);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private String primaryVolumePath(Context context) {
        try {
            StorageManager localStorageManager = (StorageManager) context
                    .getSystemService("storage");
            Object localObject = localStorageManager.getClass()
                    .getMethod("getPrimaryVolume", new Class[0])
                    .invoke(localStorageManager, new Object[0]);
            return (String) localObject.getClass().getMethod("getPath", new Class[0])
                    .invoke(localObject, new Object[0]);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private File findFstab(Core core) {

        File file = null;

        file = new File("/system/etc/vold.fstab");
        if (file.exists()) {
            return file;
        }

        SuperUserPlugin suPlugin = (SuperUserPlugin) core.getPlugin(Core.PLUGIN_SUPERUSER);
        CommandResult cm = suPlugin
                .run("grep -ls \"/dev/block/\" * --include=fstab.* --exclude=fstab.goldfish");
        if (cm.getOutString() != null) {
            String[] files = cm.getOutString().split("\n");
            for (int i = 0; i < files.length; i++) {
                file = new File(files[i]);
                if (file.exists()) {
                    return file;
                }
            }
        }

        return null;
    }

}
