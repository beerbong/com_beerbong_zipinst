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

package com.beerbong.zipinst.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class ListPreferenceMultiSelect extends ListPreference {

    private static final String SEPARATOR = "|";

    private boolean[] mClickedDialogEntryIndices;

    public ListPreferenceMultiSelect(Context context) {
        this(context, null);
    }

    public ListPreferenceMultiSelect(Context context, AttributeSet attrs) {
        super(context, attrs);
        mClickedDialogEntryIndices = new boolean[getEntries().length];
    }

    @Override
    public void setEntries(CharSequence[] entries) {
        super.setEntries(entries);
        mClickedDialogEntryIndices = new boolean[entries.length];
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        CharSequence[] entries = getEntries();

        restoreCheckedEntries();
        builder.setMultiChoiceItems(entries, mClickedDialogEntryIndices,
                new DialogInterface.OnMultiChoiceClickListener() {

                    public void onClick(DialogInterface dialog, int which, boolean val) {
                        mClickedDialogEntryIndices[which] = val;
                    }
                });
    }

    public List<String> parseStoredValue(CharSequence val) {
        if (val == null || "".equals(val)) {
            return null;
        } else {
            StringTokenizer tokenizer = new StringTokenizer((String)val, SEPARATOR);
            List<String> result = new ArrayList<String>();
            while (tokenizer.hasMoreTokens()) {
                result.add(tokenizer.nextToken());
            }
            return result;
        }
    }

    private void restoreCheckedEntries() {
        CharSequence[] entryValues = getEntryValues();

        List<String> vals = parseStoredValue(getValue());

        if (vals != null) {
            for (int i = 0; i < entryValues.length; i++) {
                CharSequence entry = entryValues[i];
                if (vals.contains(entry)) {
                    mClickedDialogEntryIndices[i] = true;
                }
            }
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        ArrayList<String> values = new ArrayList<String>();

        CharSequence[] entryValues = getEntryValues();
        if (positiveResult && entryValues != null) {
            for (int i = 0; i < entryValues.length; i++) {
                if (mClickedDialogEntryIndices[i] == true) {
                    String val = (String) entryValues[i];
                    values.add(val);
                }
            }

            if (callChangeListener(values)) {
                setValue(join(values, SEPARATOR));
            }
        }
    }

    protected static String join(Iterable<? extends Object> pColl, String separator) {
        Iterator<? extends Object> oIter;
        if (pColl == null || (!(oIter = pColl.iterator()).hasNext())) {
            return "";
        }
        StringBuilder oBuilder = new StringBuilder(String.valueOf(oIter.next()));
        while (oIter.hasNext()) {
            oBuilder.append(separator).append(oIter.next());
        }
        return oBuilder.toString();
    }

    public static boolean contains(String straw, String haystack) {
        String[] vals = haystack.split(SEPARATOR);
        for (int i = 0; i < vals.length; i++) {
            if (vals[i].equals(straw)) {
                return true;
            }
        }
        return false;
    }
}
