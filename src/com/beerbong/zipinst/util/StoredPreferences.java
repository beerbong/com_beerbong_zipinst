package com.beerbong.zipinst.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.preference.Preference;
import android.util.Log;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class StoredPreferences {
    
    private static final String TAG = "StoredPreferences";

    private static List<Preference> list = new ArrayList<Preference>();

    public static int size() {
        return list.size();
    }
    public static Preference getPreference(int i) {
        return list.get(i);
    }
    public static void addPreference(Preference preference) {
        preference.setOrder(Preference.DEFAULT_ORDER);
        list.add(preference);
    }
    public static void removePreferences() {
        list.clear();
    }
    public static void removePreference(String key) {
        int size = size(), i = 0;
        for (;i<size;i++) {
            if (key.equals(list.get(i).getKey())) {
                list.remove(i);
                return;
            }
        }
    }
    public static void move(int from, int to) {
        if (from == to) return;
        Log.d(TAG, "BEFORE");
        for (int i=0;i<list.size();i++) {
            Log.d(TAG, list.get(i).getTitle().toString());
        }
        Preference toMove = list.get(from);
        while (list.indexOf(toMove) != to) {
            int i = list.indexOf(toMove);
            Collections.swap(list, i, to < from ? i - 1 : i + 1);
        }
        Log.d(TAG, "AFTER");
        for (int i=0;i<list.size();i++) {
            Log.d(TAG, list.get(i).getTitle().toString());
        }
    }

    private StoredPreferences() {
    }
}