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

package com.beerbong.zipinst.http;

import java.util.ArrayList;
import java.util.List;

public class Downloads {

    private static List<DownloadTask> sList = new ArrayList<DownloadTask>();

    public static void addDownloadTask(DownloadTask task) {
        sList.add(task);
    }

    public static void removeDownloadTask(DownloadTask task) {
        sList.remove(task);
    }

    public static void cancelAll() {
        int size = sList.size();
        for (int i = size - 1; i >= 0; i--) {
            DownloadTask task = (DownloadTask) sList.get(i);
            task.detach();
            task.cancel(true);
        }
    }
}
