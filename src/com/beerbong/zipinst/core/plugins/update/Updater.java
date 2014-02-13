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

package com.beerbong.zipinst.core.plugins.update;

import com.beerbong.zipinst.http.HttpStringReader.HttpStringReaderListener;
import com.beerbong.zipinst.http.URLStringReader.URLStringReaderListener;

public interface Updater extends URLStringReaderListener, HttpStringReaderListener {

    public class RomInfo {

        public int id;
        public String filename;
        public String path;
        public String folder;
        public String md5;
        public String type;
        public String description;
        public int is_flashable;
        public long modified;
        public int downloads;
        public int status;
        public String additional_info;
        public String short_url;
        public int developer_id;
        public String developerid;
        public String board;
        public String rom;
        public long version;
        public int gapps_package;
        public int incremental_file;
    }

    public static final String PROPERTY_DEVICE = "ro.product.device";

    public static interface UpdaterListener {

        public void versionFound(RomInfo info);
        public void versionError(String error);
    }

    public String getRomName();

    public int getRomVersion();

    public void searchVersion();

    public boolean isScanning();
}