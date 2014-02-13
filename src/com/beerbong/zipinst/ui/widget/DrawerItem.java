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

package com.beerbong.zipinst.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

public class DrawerItem {

    private Class<?> mFragmentClass;
    private String mText;
    private Drawable mIcon;
    private Drawable mSelectedIcon;
    private boolean mChecked = false;

    public DrawerItem(Context context, Class<?> fragmentClass, int text, int icon, int selectedIcon) {

        mFragmentClass = fragmentClass;

        mText = context.getResources().getString(text);

        if (icon != 0) {
            TypedArray ta = context.getTheme().obtainStyledAttributes(new int[] { icon });
            mIcon = ta.getDrawable(0);
            ta.recycle();
        }

        if (selectedIcon != 0) {
            TypedArray ta = context.getTheme().obtainStyledAttributes(new int[] { selectedIcon });
            mSelectedIcon = ta.getDrawable(0);
            ta.recycle();
        }
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public Class<?> getFragmentClass() {
        return mFragmentClass;
    }

    public String getText() {
        return mText;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public Drawable getSelectedIcon() {
        return mSelectedIcon;
    }

}
