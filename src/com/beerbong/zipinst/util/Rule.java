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
import java.util.List;

import com.beerbong.zipinst.R;

public class Rule {

    private static final String RULES_SEPARATOR = "\n";

    public static final int STARTS_WITH = 0;
    public static final int ENDS_WITH = 1;
    public static final int EQUAL = 2;

    public static Rule[] createRules(String str) {
        if (str == null || "".equals(str.trim())) {
            return new Rule[0];
        }
        String[] rulesStr = str.split(RULES_SEPARATOR);
        List<Rule> list = new ArrayList<Rule>();
        for (int i = 0; i < rulesStr.length; i++) {
            if (rulesStr[i] != null && !"".equals(rulesStr[i].trim())) {
                list.add(new Rule(rulesStr[i]));
            }
        }
        return list.toArray(new Rule[list.size()]);
    }

    public static String storeRules(Rule[] rules) {
        if (rules == null || rules.length == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < rules.length; i++) {
            sb.append(rules[i].getType() + rules[i].getName());
            if (i < rules.length - 1) {
                sb.append(RULES_SEPARATOR);
            }
        }
        return sb.toString();
    }

    private String name;
    private int type;

    public Rule(String name, int type) {
        this.name = name;
        this.type = type;
    }

    private Rule(String str) {
        this.name = str.substring(1);
        this.type = Integer.parseInt(new String(new char[] { str.charAt(0) }));
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public int getTypeString() {
        switch (type) {
            case STARTS_WITH :
                return R.string.rule_starts_with;
            case ENDS_WITH :
                return R.string.rule_ends_with;
            case EQUAL :
                return R.string.rule_equals;
        }
        return -1;
    }

    public boolean apply(String fileName) {
        switch (type) {
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
