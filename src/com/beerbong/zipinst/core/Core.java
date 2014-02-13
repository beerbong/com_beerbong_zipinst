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

package com.beerbong.zipinst.core;

import java.io.Serializable;

import com.beerbong.zipinst.cloud.CloudStorage;
import com.beerbong.zipinst.io.Version;
import com.beerbong.zipinst.onandroid.ONandroid;
import com.beerbong.zipinst.preferences.Preferences;

import android.content.Context;

public interface Core extends Serializable {

    public static final String PLUGIN_UI = "UIPlugin";
    public static final String PLUGIN_SUPERUSER = "SuperUserPlugin";
    public static final String PLUGIN_RECOVERY = "RecoveryPlugin";
    public static final String PLUGIN_STORAGE = "StoragePlugin";
    public static final String PLUGIN_REBOOT = "RebootPlugin";
    public static final String PLUGIN_LICENSE = "LicensePlugin";
    public static final String PLUGIN_UPDATE = "UpdatePlugin";

    public interface CoreListener {

        public void pluginStarted(String name);

        public void pluginStopped(String name);

        public void coreMessage(int resId);

        public void coreStarted();

        public void coreStopped();
    }

    public void destroy();

    public boolean isStarted();

    public Context getContext();

    public void setContext(Context context);

    public Plugin getPlugin(String name);

    public Preferences getPreferences();

    public CloudStorage getCloudStorage();

    public void setCloudStorage(int cloudStorage);

    public ONandroid getONandroid();

    public Version getVersion();

    public void moveToInstall();
}
