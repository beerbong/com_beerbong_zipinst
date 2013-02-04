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

package com.beerbong.zipinst.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class StoredItems {

    private static List<ZipItem> list = new ArrayList<ZipItem>();

    public static int size() {
        return list.size();
    }

    public static List<ZipItem> getItems() {
        return list;
    }

    public static ZipItem getItem(int i) {
        return list.get(i);
    }

    public static void addItem(ZipItem item) {
        list.add(item);
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
        ZipItem toMove = list.get(from);
        while (list.indexOf(toMove) != to) {
            int i = list.indexOf(toMove);
            Collections.swap(list, i, to < from ? i - 1 : i + 1);
        }
    }

    private StoredItems() {
    }
}