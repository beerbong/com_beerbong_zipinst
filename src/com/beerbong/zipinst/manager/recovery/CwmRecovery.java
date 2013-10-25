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

package com.beerbong.zipinst.manager.recovery;

import java.util.ArrayList;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.RecoveryInfo;
import com.beerbong.zipinst.util.Constants;

public class CwmRecovery extends RecoveryInfo {

    public CwmRecovery(Context context) {
        super();

        setId(R.id.cwmbased);
        setName("cwmbased");
        setInternalSdcard(internalStorage());
        setExternalSdcard(externalStorage(context));
    }

    @Override
    public String getFullName(Context context) {
        return context.getString(R.string.recovery_cwm);
    }

    @Override
    public String getFolderPath() {
        return "/sdcard/clockworkmod/";
    }

    private String internalStorage() {
        if (Environment.getExternalStorageDirectory() == null) {
            return "sdcard";
        }
        String path, dirPath;
        dirPath = path = Environment.getExternalStorageDirectory().getAbsolutePath();
        dirPath = Constants.replace(Constants.replace(
                Constants.replace(dirPath, "/mnt/sdcard", "/sdcard"), "/mnt/emmc", "/emmc"), path,
                "/sdcard");
        if (Build.VERSION.SDK_INT > 16) {
            String emulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
            if ((emulatedStorageTarget != null) && (path.startsWith(emulatedStorageTarget))) {
                String number = path.replace(emulatedStorageTarget, "");
                dirPath = Constants.replace(dirPath, "/sdcard", "/sdcard" + number);
            }
            String emulatedStorageSource = System.getenv("EMULATED_STORAGE_SOURCE");
            if (emulatedStorageSource != null) {
                dirPath = Constants.replace(dirPath, emulatedStorageSource,
                        "/data/media");
            }
            if (emulatedStorageTarget == null && emulatedStorageSource == null
                    && "/storage/sdcard0".equals(path)
                    && "/sdcard".equals(dirPath)) {
                dirPath = path;
            }
        } else if (dirPath.startsWith("/mnt/emmc")) {
            dirPath = "emmc";
        }
        return dirPath;
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
