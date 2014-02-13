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

package com.beerbong.zipinst.core.plugins.update.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.beerbong.zipinst.core.plugins.update.Updater;
import com.beerbong.zipinst.http.URLStringReader;
import com.beerbong.zipinst.io.SystemProperties;

public class GooUpdater implements Updater {

    public static final String PROPERTY_GOO_DEVELOPER = "ro.goo.developerid";
    public static final String PROPERTY_GOO_ROM = "ro.goo.rom";
    public static final String PROPERTY_GOO_VERSION = "ro.goo.version";

    private UpdaterListener mListener;
    private List<RomInfo> mFoundRoms;
    private int mScanning = 0;

    public GooUpdater(UpdaterListener listener) {
        mListener = listener;
    }

    public String getDeveloperId() {
        return SystemProperties.getProperty(PROPERTY_GOO_DEVELOPER);
    }

    @Override
    public String getRomName() {
        return SystemProperties.getProperty(PROPERTY_GOO_ROM);
    }

    @Override
    public int getRomVersion() {
        String version = SystemProperties.getProperty(PROPERTY_GOO_VERSION);
        if (version != null) {
            try {
                return Integer.parseInt(version);
            } catch (NumberFormatException ex) {
            }
        }
        return -1;
    }

    @Override
    public void searchVersion() {
        mScanning = 0;
        mFoundRoms = new ArrayList<RomInfo>();
        searchGoo("/devs/" + getDeveloperId());
    }

    @Override
    public boolean isScanning() {
        return mScanning > 0;
    }

    private void searchGoo(String path) {
        mScanning++;
        new URLStringReader(this).execute("http://goo.im/json2&path=" + path + "&ro_board="
                + getDevice());
    }

    private String getDevice() {
        return SystemProperties.getProperty(PROPERTY_DEVICE);
    }

    @Override
    public void onReadEnd(String buffer) {
        try {
            String developerId = getDeveloperId();
            String romName = getRomName();
            String device = getDevice();
            int version = getRomVersion();
            mScanning--;
            JSONObject object = (JSONObject) new JSONTokener(buffer).nextValue();
            if (!object.isNull("list")) {
                JSONArray list = object.getJSONArray("list");
                for (int i = 0; i < list.length(); i++) {
                    JSONObject result = list.getJSONObject(i);
                    String fileName = result.optString("filename");
                    if (fileName != null && !"".equals(fileName.trim())) {
                        RomInfo info = new RomInfo();
                        info.developerid = result.optString("ro_developerid");
                        info.board = result.optString("ro_board");
                        info.rom = result.optString("ro_rom");
                        info.version = result.optInt("ro_version");
                        if (!developerId.equals(info.developerid) || !romName.equals(info.rom)
                                || !device.equals(info.board) || info.version <= version) {
                            continue;
                        }
                        info.id = result.optInt("id");
                        info.filename = result.optString("filename");
                        info.path = "http://goo.im" + result.optString("path");
                        info.folder = result.optString("folder");
                        info.md5 = result.optString("md5");
                        info.type = result.optString("type");
                        info.description = result.optString("description");
                        info.is_flashable = result.optInt("is_flashable");
                        info.modified = result.optLong("modified");
                        info.downloads = result.optInt("downloads");
                        info.status = result.optInt("status");
                        info.additional_info = result.optString("additional_info");
                        info.short_url = result.optString("short_url");
                        info.developer_id = result.optInt("developer_id");
                        info.gapps_package = result.optInt("gapps_package");
                        info.incremental_file = result.optInt("incremental_file");
                        mFoundRoms.add(info);
                    } else {
                        String folder = result.getString("folder");
                        searchGoo(folder);
                    }
                }
            }
            if (mScanning == 0) {
                long newVersion = -2;
                RomInfo newRom = null;
                for (int i = 0; i < mFoundRoms.size(); i++) {
                    RomInfo info = mFoundRoms.get(i);
                    if (info.version > newVersion) {
                        newRom = info;
                    }
                    newVersion = Math.max(newVersion, info.version);
                }
                mListener.versionFound(newRom);
            }
        } catch (Exception ex) {
            mScanning = 0;
            mFoundRoms = new ArrayList<RomInfo>();
            ex.printStackTrace();
            mListener.versionError(null);
        }
    }

    @Override
    public void onReadError(Exception ex) {
        mListener.versionError(null);
    }

}