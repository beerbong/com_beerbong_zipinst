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

package com.beerbong.zipinst.manager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;

public abstract class RecoveryInfo {

    private Context mContext;
    private int id;
    private String name = null;
    private String internalSdcard = null;
    private String externalSdcard = null;

    public RecoveryInfo(Context context) {

        mContext = context;

        setExternalSdcard(externalStorage(context));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInternalSdcard() {
        return internalSdcard;
    }

    public void setInternalSdcard(String sdcard) {
        this.internalSdcard = sdcard;
    }

    public String getExternalSdcard() {
        return externalSdcard;
    }

    public void setExternalSdcard(String sdcard) {
        this.externalSdcard = sdcard;
    }

    public abstract String getFullName(Context context);

    public abstract String getFolderPath();

    public abstract String getCommandsFile();

    public abstract String getBackupFolder(String sdcard, boolean force, boolean external);

    public abstract List<String> getCommands(String storage, boolean external, boolean wipeSystem,
            boolean wipeData, boolean wipeCaches, boolean fixPermissions, String backupFolder,
            String backupOptions, String restore) throws Exception;

    protected Context getContext() {
        return mContext;
    }

    private String externalStorage(Context paramContext) {
        String dirPath = null;
        try {
            String[] volumePaths = null;
            ArrayList<String> volumePathsList = null;
            String path = null;
            if (Build.VERSION.SDK_INT >= 14) {
                volumePaths = volumePaths(paramContext);
                if (volumePaths != null) {
                    volumePathsList = new ArrayList<String>();
                    path = Environment.getExternalStorageDirectory().getAbsolutePath();
                }
            }
            try {
                String primaryVolumePath = primaryVolumePath(paramContext);
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
}