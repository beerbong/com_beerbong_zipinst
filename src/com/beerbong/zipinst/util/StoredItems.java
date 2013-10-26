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

package com.beerbong.zipinst.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StoredItems {

    private static List<FileItem> list = new ArrayList<FileItem>();

    public static int size() {
        return list.size();
    }

    public static List<FileItem> getItems() {
        return list;
    }

    public static String[] getPaths() {
        String[] paths = new String[list.size()];
        int i = 0;
        for (FileItem item : list) {
            paths[i] = item.getPath();
            i++;
        }
        return paths;
    }

    public static FileItem getItem(int i) {
        return list.get(i);
    }

    public static void addItem(FileItem item) {
        list.add(item);
    }

    public static void addItem(FileItem item, int position) {
        list.add(position, item);
    }

    public static void removeItems() {
        list.clear();
    }

    public static void removeItem(String key) {
        int size = size(), i = 0;
        for (; i < size; i++) {
            if (key.equals(list.get(i).getKey())) {
                list.remove(i);
                return;
            }
        }
    }

    public static void move(int from, int to) {
        if (from == to)
            return;
        FileItem toMove = list.get(from);
        while (list.indexOf(toMove) != to) {
            int i = list.indexOf(toMove);
            Collections.swap(list, i, to < from ? i - 1 : i + 1);
        }
    }

    private StoredItems() {
    }
}