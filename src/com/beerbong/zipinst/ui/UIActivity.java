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

package com.beerbong.zipinst.ui;

import java.io.Serializable;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.CoreFactory;
import com.beerbong.zipinst.core.plugins.ui.UIPlugin;

public abstract class UIActivity extends Activity implements UIInterface, Serializable {

    private Core mCore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        CoreFactory.init(this);

        mCore = CoreFactory.getCore();

        ((UIPlugin) mCore.getPlugin(Core.PLUGIN_UI)).registerUI(this);

        super.onCreate(savedInstanceState);

        int resId = getContentViewId();
        if (resId != 0) {
            setContentView(resId);
        }

        create(savedInstanceState == null);

        if (savedInstanceState != null) {
            restore(savedInstanceState);
        }

        redraw();

    }

    @Override
    protected void onResume() {
        super.onResume();

        CoreFactory.init(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((UIPlugin) mCore.getPlugin(Core.PLUGIN_UI)).unregisterUI(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        save(outState);
    }

    @Override
    public View getMainView(View rootView) {
        return getMainView();
    }

    @Override
    public void redraw() {
        ((UIPlugin) mCore.getPlugin(Core.PLUGIN_UI)).redraw(this);
    }

    public void moveToStart() {
    }

    protected Core getCore() {
        return mCore;
    }

}
