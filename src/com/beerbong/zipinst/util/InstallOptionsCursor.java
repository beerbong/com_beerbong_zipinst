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

import android.content.Context;
import android.database.AbstractCursor;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.ManagerFactory;
import com.beerbong.zipinst.manager.PreferencesManager;

public class InstallOptionsCursor extends AbstractCursor {

    private static final String[] COLUMN_NAMES = { "_id", "TEXT", "CHECKED" };

    private String[] mOption;
    private String[] mText;
    private int[] mChecked;
    private int mCount = 0;

    public InstallOptionsCursor(Context context) {
        PreferencesManager pManager = ManagerFactory.getPreferencesManager();

        for (int i = 0; i < Constants.INSTALL_OPTIONS.length; i++) {
            if (pManager.isShowOption(Constants.INSTALL_OPTIONS[i])) {
                mCount++;
            }
        }
        mOption = new String[mCount];
        mText = new String[mCount];
        mChecked = new int[mCount];
        int count = 0;
        for (int i = 0; i < Constants.INSTALL_OPTIONS.length; i++) {
            if (pManager.isShowOption(Constants.INSTALL_OPTIONS[i])) {
                mOption[count] = Constants.INSTALL_OPTIONS[i];
                mText[count] = context.getResources().getString(
                        getText(Constants.INSTALL_OPTIONS[i]));
                mChecked[count] = 0;
                count++;
            }
        }
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public String[] getColumnNames() {
        return COLUMN_NAMES;
    }

    @Override
    public String getString(int column) {
        switch (column) {
            case 0:
                return mOption[getPosition()];
            case 1:
                return mText[getPosition()];
        }
        return null;
    }

    @Override
    public short getShort(int column) {
        return 0;
    }

    @Override
    public int getInt(int column) {
        if (column == 2) {
            return mChecked[getPosition()];
        }
        return 0;
    }

    @Override
    public long getLong(int column) {
        return 0;
    }

    @Override
    public float getFloat(int column) {
        return 0;
    }

    @Override
    public double getDouble(int column) {
        return 0;
    }

    @Override
    public boolean isNull(int column) {
        return false;
    }

    public void setOption(int which, boolean isChecked) {
        mChecked[which] = isChecked ? 1 : 0;
    }

    public boolean isBackup() {
        for (int i = 0; i < mCount; i++) {
            if (Constants.INSTALL_BACKUP.equals(mOption[i])) {
                return mChecked[i] == 1;
            }
        }
        return false;
    }

    public boolean isWipeSystem() {
        for (int i = 0; i < mCount; i++) {
            if (Constants.INSTALL_WIPESYSTEM.equals(mOption[i])) {
                return mChecked[i] == 1;
            }
        }
        return false;
    }

    public boolean isWipeData() {
        for (int i = 0; i < mCount; i++) {
            if (Constants.INSTALL_WIPEDATA.equals(mOption[i])) {
                return mChecked[i] == 1;
            }
        }
        return false;
    }

    public boolean isWipeCaches() {
        for (int i = 0; i < mCount; i++) {
            if (Constants.INSTALL_WIPECACHES.equals(mOption[i])) {
                return mChecked[i] == 1;
            }
        }
        return false;
    }

    public boolean isFixPermissions() {
        for (int i = 0; i < mCount; i++) {
            if (Constants.INSTALL_FIXPERM.equals(mOption[i])) {
                return mChecked[i] == 1;
            }
        }
        return false;
    }

    public String getIsCheckedColumn() {
        return "CHECKED";
    }

    public String getLabelColumn() {
        return "TEXT";
    }

    private int getText(String option) {
        if (Constants.INSTALL_BACKUP.equals(option)) {
            return R.string.backup;
        } else if (Constants.INSTALL_WIPESYSTEM.equals(option)) {
            return R.string.wipe_system;
        } else if (Constants.INSTALL_WIPEDATA.equals(option)) {
            return R.string.wipe_data;
        } else if (Constants.INSTALL_WIPECACHES.equals(option)) {
            return R.string.wipe_caches;
        } else if (Constants.INSTALL_FIXPERM.equals(option)) {
            return R.string.fix_permissions;
        }
        return -1;
    }
}