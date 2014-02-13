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

package com.beerbong.zipinst.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileItemStore {

    public interface FileItemStoreListener {

        public void storeChanged();
    }

    private static FileItemStoreListener sListener;
    private static List<FileItem> sList = new ArrayList<FileItem>();

    public static void setFileItemStoreListener(FileItemStoreListener listener) {
        sListener = listener;
    }

    public static int size() {
        return sList.size();
    }

    public static List<FileItem> getItems() {
        return sList;
    }

    public static String[] getPaths() {
        String[] paths = new String[sList.size()];
        int i = 0;
        for (FileItem item : sList) {
            paths[i] = item.getPath();
            i++;
        }
        return paths;
    }

    public static FileItem getItem(int i) {
        return sList.get(i);
    }

    public static void addItem(String realPath, String sdcardPath, boolean delete, String zipPosition) {

        sList.remove(realPath);

        FileItem item = new FileItem(realPath, sdcardPath.substring(sdcardPath.lastIndexOf("/") + 1),
                sdcardPath, delete);

        if ("first".equals(zipPosition)) {
            sList.add(0, item);
        } else {
            sList.add(item);
        }

        if (sListener != null) {
            sListener.storeChanged();
        }
    }

    public static void removeItems() {
        sList.clear();
        if (sListener != null) {
            sListener.storeChanged();
        }
    }

    public static void removeItem(String key) {
        int size = size(), i = 0;
        for (; i < size; i++) {
            if (key.equals(sList.get(i).getKey())) {
                sList.remove(i);
                return;
            }
        }
        if (sListener != null) {
            sListener.storeChanged();
        }
    }

    public static void move(int from, int to) {
        if (from == to)
            return;
        FileItem toMove = sList.get(from);
        while (sList.indexOf(toMove) != to) {
            int i = sList.indexOf(toMove);
            Collections.swap(sList, i, to < from ? i - 1 : i + 1);
        }
        if (sListener != null) {
            sListener.storeChanged();
        }
    }

    private FileItemStore() {
    }
}
