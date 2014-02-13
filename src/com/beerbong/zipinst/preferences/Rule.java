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

package com.beerbong.zipinst.preferences;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.store.FileItem;

public class Rule extends FileItem {

    private static final String RULES_SEPARATOR = "\n";

    public static final int STARTS_WITH = 0;
    public static final int ENDS_WITH = 1;
    public static final int EQUAL = 2;

    public static Rule[] createRules(Context context, String str) {
        List<Rule> list = createRulesAsList(context, str);
        return list.toArray(new Rule[list.size()]);
    }

    public static List<Rule> createRulesAsList(Context context, String str) {
        if (str == null || "".equals(str.trim())) {
            return new ArrayList<Rule>();
        }
        String[] rulesStr = str.split(RULES_SEPARATOR);
        List<Rule> list = new ArrayList<Rule>();
        for (int i = 0; i < rulesStr.length; i++) {
            if (rulesStr[i] != null && !"".equals(rulesStr[i].trim())) {
                list.add(new Rule(context, rulesStr[i]));
            }
        }
        return list;
    }

    public static String storeRules(List<Rule> rules) {
        if (rules == null) {
            return "";
        }
        return storeRules(rules.toArray(new Rule[rules.size()]));
    }

    public static String storeRules(Rule[] rules) {
        if (rules == null || rules.length == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < rules.length; i++) {
            sb.append(rules[i].getType() + rules[i].getOriginalName());
            if (i < rules.length - 1) {
                sb.append(RULES_SEPARATOR);
            }
        }
        return sb.toString();
    }

    private int mType;

    public Rule(Context context, String name, int type) {
        super(name, name, null, false);
        this.mType = type;
        setPath(getTypeString(context));
    }

    private Rule(Context context, String str) {
        super(str.substring(1), str.substring(1), null, false);
        this.mType = Integer.parseInt(new String(new char[] { str.charAt(0) }));
        setPath(getTypeString(context));
    }

    public int getType() {
        return mType;
    }

    @Override
    public String getShortPath() {
        return getPath() + " " + getKey();
    }

    public String getOriginalName() {
        return super.getName();
    }

    @Override
    public String getName() {
        String name = super.getName();
        switch (mType) {
            case STARTS_WITH :
                name = name + "*";
                break;
            case ENDS_WITH :
                name = "*" + name;
                break;
        }
        return name;
    }

    private String getTypeString(Context context) {
        Resources res = context.getResources();
        switch (mType) {
            case STARTS_WITH :
                return res.getString(R.string.rule_starts_with);
            case ENDS_WITH :
                return res.getString(R.string.rule_ends_with);
            case EQUAL :
                return res.getString(R.string.rule_equals);
        }
        return null;
    }

    public boolean apply(String fileName) {
        String name = getKey();
        switch (mType) {
            case STARTS_WITH :
                return fileName.startsWith(name);
            case ENDS_WITH :
                return fileName.endsWith(name);
            case EQUAL :
                return fileName.equals(name);
        }
        return false;
    }
}
