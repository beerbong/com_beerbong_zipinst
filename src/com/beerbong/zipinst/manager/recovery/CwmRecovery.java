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

package com.beerbong.zipinst.manager.recovery;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.storage.StorageManager;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.manager.PreferencesManager;
import com.beerbong.zipinst.manager.RecoveryInfo;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.FileItem;
import com.beerbong.zipinst.util.StoredItems;
import com.koushikdutta.rommanager.api.IClockworkRecoveryScriptBuilder;
import com.koushikdutta.rommanager.api.IROMManagerAPIService;

public class CwmRecovery extends RecoveryInfo {

    private Context mContext;

    public CwmRecovery(Context context) {
        super();

        mContext = context;

        setId(R.id.cwm);
        setName("cwm");
        setInternalSdcard("sdcard");
        setExternalSdcard(externalStorage(context));
    }

    @Override
    public String getFullName(Context context) {
        return context.getString(R.string.recovery_cwm_official);
    }

    @Override
    public String getFolderPath() {
        return "/sdcard/clockworkmod/";
    }

    @Override
    public String getCommandsFile() {
        return "extendedcommand";
    }

    @Override
    public String getBackupFolder(String sdcard, boolean force) {
        if (force) {
            while (sdcard.startsWith("/")) {
                sdcard = sdcard.substring(1);
            }
            return "/" + sdcard + "/clockworkmod/backup/";
        }
        return "";
    }

    @Override
    public List<String> getCommands(final String storage, final boolean external,
            final boolean wipeSystem, final boolean wipeData, final boolean wipeCaches,
            final boolean fixPermissions, final String backupFolder, final String backupOptions,
            final String restore) throws Exception {
        Intent i = new Intent("com.koushikdutta.rommanager.api.BIND");
        try {
            mContext.bindService(i, new ServiceConnection() {
                @Override
                public void onServiceDisconnected(ComponentName name) {
                }

                @Override
                public void onServiceConnected(ComponentName name, IBinder binder) {
                    IROMManagerAPIService service = IROMManagerAPIService.Stub.asInterface(binder);
                    if (service == null) {
                        Constants.showError(getContext(), R.string.error_no_rommanager_connection);
                        return;
                    }
                    try {
                        if (!service.isPremium()) {
                            Constants.showError(getContext(), R.string.error_no_rommanager_premium);
                            return;
                        }
                        IClockworkRecoveryScriptBuilder builder = service.createClockworkRecoveryScriptBuilder();
                        run(builder, storage, external, wipeSystem, wipeData, wipeCaches,
                                fixPermissions, backupFolder, backupOptions, restore);
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                        Constants.showError(getContext(), R.string.error_rommanager_unknown);
                    }
                }
            }, Service.BIND_AUTO_CREATE);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Constants.showError(getContext(), R.string.error_no_rommanager_connection);
        }
        return new ArrayList<String>();
    }

    protected Context getContext() {
        return mContext;
    }

    private void run(IClockworkRecoveryScriptBuilder builder, String storage, boolean external,
            boolean wipeSystem, boolean wipeData, boolean wipeCaches, boolean fixPermissions,
            String backupFolder, String backupOptions, String restore) throws Exception {

        String sbin = Constants.getSBINFolder();

        PreferencesManager pManager = ManagerFactory.getPreferencesManager();
        String internalStorage = pManager.getInternalStorage();
        while (internalStorage.startsWith("/")) {
            internalStorage = internalStorage.substring(1);
        }
        
        builder.print("-------------------------------------");
        builder.print(" ZipInstaller "
                + getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName);
        builder.print("-------------------------------------");

        if (ManagerFactory.getFileManager().hasExternalStorage()
                && ManagerFactory.getPreferencesManager().isForceExternalStorage()) {
            builder.print(" Mounting external sd");
            List<String> params = new ArrayList<String>();
            params.add(ManagerFactory.getPreferencesManager().getExternalStorage());
            builder.runProgram("/sbin/mount", params);
        }

        if (restore != null) {
            builder.print(" Restore ROM");
            builder.restore("/" + storage + "/clockworkmod/backup/" + restore, true, true, true,
                    true, true);
        }

        if (backupFolder != null) {
            builder.print(" Backup ROM");
            builder.backupWithPath("/" + storage + "/clockworkmod/backup/" + backupFolder);
        }

        if (wipeSystem) {
            builder.print(" Wiping system");
            builder.format("/system");
        }

        if (wipeData) {
            builder.print(" Wiping data");
            builder.format("/data");
            builder.print(" Wiping android secure");
            builder.format("/" + internalStorage + "/.android_secure");
        }
        if (wipeCaches) {
            builder.print(" Wiping cache");
            builder.format("/cache");
            builder.print(" Wiping dalvik cache");
            builder.format("/data/dalvik-cache");
            builder.format("/cache/dalvik-cache");
            builder.format("/sd-ext/dalvik-cache");
        }

        int size = StoredItems.size(), i = 0;

        if (size > 0) {
            for (; i < size; i++) {
                FileItem item = StoredItems.getItem(i);
                if (item.isZip()) {
                    builder.print(" Installing zip");
                    builder.installZip(item.getPath());
                } else if (item.isScript()) {
                    builder.print(" Executing script");
                    List<String> params = new ArrayList<String>();
                    params.add("cp");
                    params.add(item.getKey());
                    params.add("/cache/" + item.getName());
                    builder.runProgram("/sbin/busybox", params);
                    params = new ArrayList<String>();
                    params.add("chmod");
                    params.add("+x");
                    params.add("/cache/" + item.getName());
                    builder.runProgram(sbin, params);
                    params = new ArrayList<String>();
                    params.add("sh");
                    params.add("/cache/" + item.getName());
                    builder.runProgram(sbin, params);
                    params = new ArrayList<String>();
                    params.add("rm");
                    params.add("/cache/" + item.getName());
                    builder.runProgram("/sbin/busybox", params);
                }
            }
        }

        if (fixPermissions) {
            builder.print(" Fix permissions");
            List<String> params = new ArrayList<String>();
            params.add("chmod");
            params.add("+x");
            params.add("/cache/fix_permissions.sh");
            builder.runProgram(sbin, params);
            params = new ArrayList<String>();
            params.add("sh");
            params.add("/cache/fix_permissions.sh");
            builder.runProgram(sbin, params);
            params = new ArrayList<String>();
            params.add("rm");
            params.add("/cache/fix_permissions.sh");
            builder.runProgram("/sbin/busybox", params);
        }

        builder.print(" Rebooting");

        builder.runScript();
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
