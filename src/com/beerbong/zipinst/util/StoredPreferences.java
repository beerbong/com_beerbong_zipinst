package com.beerbong.zipinst.util;

import java.util.ArrayList;
import java.util.List;

import android.preference.Preference;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class StoredPreferences {

    private static List<Preference> list = new ArrayList();

    public static int size() {
        return list.size();
    }
    public static Preference getPreference(int i) {
        return list.get(i);
    }
    public static void addPreference(Preference preference) {
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

    private StoredPreferences() {
    }
}