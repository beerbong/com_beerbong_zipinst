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

package com.beerbong.zipinst.io;

import java.io.Serializable;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

public class Version implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String[] STATIC_REMOVE = { "ZipInstaller-", ".apk" };
    private final String[] PHASES = { "a", "b", "rc", "GOLD" };

    private static final int ALPHA = 0;
    private static final int BETA = 1;
    private static final int RELEASE_CANDIDATE = 2;
    private static final int GOLD = 3;

    private int mMajor = 0;
    private int mMinor = 0;
    private int mMaintenance = 0;
    private int mPhase = GOLD;
    private int mPhaseNumber = 0;

    private String mFileName;
    private String mFilePath;
    private String mFileMd5;

    public Version(Context context) {
        try {
            String version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            init(version);
        } catch (NameNotFoundException ex) {
            // wtf?
        }
    }

    public Version(String fileName, String filePath, String fileMd5) {
        mFileName = fileName;
        mFilePath = filePath;
        mFileMd5 = fileMd5;
        init(fileName);
    }

    private void init(String version) {

        for (int i = 0; i < STATIC_REMOVE.length; i++) {
            version = version.replace(STATIC_REMOVE[i], "");
        }

        String[] split = version.split("-");

        if (split.length < 1) {
            // malformed version
            return;
        }

        version = split[0];
        String[] vSplit = version.split("\\.");
        mMajor = Integer.parseInt(vSplit[0]);
        if (vSplit.length > 1) {
            mMinor = Integer.parseInt(vSplit[1]);
            if (vSplit.length > 2) {
                mMaintenance = Integer.parseInt(vSplit[2]);
            }
        }

        if (split.length > 1) {
            version = split[1];
            if (version.toUpperCase().startsWith("A")) {
                mPhase = ALPHA;
                if (version.toUpperCase().startsWith("ALPHA")) {
                    version = version.substring(5);
                } else {
                    version = version.substring(1);
                }
            } else if (version.toUpperCase().startsWith("B")) {
                mPhase = BETA;
                if (version.toUpperCase().startsWith("BETA")) {
                    version = version.substring(4);
                } else {
                    version = version.substring(1);
                }
            } else if (version.toUpperCase().startsWith("RC")) {
                mPhase = RELEASE_CANDIDATE;
                version = version.substring(2);
            }
            if (!version.isEmpty()) {
                mPhaseNumber = Integer.parseInt(version);
            }
        }
    }

    public String getFileName() {
        return mFileName;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public String getFileMd5() {
        return mFileMd5;
    }

    public int getMajor() {
        return mMajor;
    }

    public int getMinor() {
        return mMinor;
    }

    public int getMaintenance() {
        return mMaintenance;
    }

    public int getPhase() {
        return mPhase;
    }

    public String getPhaseName() {
        return PHASES[mPhase];
    }

    public int getPhaseNumber() {
        return mPhaseNumber;
    }

    public boolean isEmpty() {
        return mMajor == 0;
    }

    public String toString() {
        return mMajor
                + "."
                + mMinor
                + "." + mMaintenance
                + (mPhase != GOLD ? "-" + getPhaseName() + (mPhaseNumber > 0 ? mPhaseNumber : "")
                        : "");
    }

    public static int compare(Version v1, Version v2) {
        if (v1.getMajor() != v2.getMajor()) {
            return v1.getMajor() < v2.getMajor() ? -1 : 1;
        }
        if (v1.getMinor() != v2.getMinor()) {
            return v1.getMinor() < v2.getMinor() ? -1 : 1;
        }
        if (v1.getMaintenance() != v2.getMaintenance()) {
            return v1.getMaintenance() < v2.getMaintenance() ? -1 : 1;
        }
        if (v1.getPhase() != v2.getPhase()) {
            return v1.getPhase() < v2.getPhase() ? -1 : 1;
        }
        if (v1.getPhaseNumber() != v2.getPhaseNumber()) {
            return v1.getPhaseNumber() < v2.getPhaseNumber() ? -1 : 1;
        }
        return 0;
    }

    public static Version max(Version v1, Version v2) {
        int compare = compare(v1, v2);
        return compare > 0 ? v1 : v2;
    }
}
