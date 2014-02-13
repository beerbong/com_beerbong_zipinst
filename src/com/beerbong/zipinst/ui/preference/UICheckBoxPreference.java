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

package com.beerbong.zipinst.ui.preference;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;

import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.CoreFactory;
import com.beerbong.zipinst.core.plugins.ui.UIPlugin;

public class UICheckBoxPreference extends CheckBoxPreference {

    public UICheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public UICheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UICheckBoxPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        ((UIPlugin) CoreFactory.getCore().getPlugin(Core.PLUGIN_UI)).redraw(view);
    }
}
