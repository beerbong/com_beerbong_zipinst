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

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.View;

import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.CoreFactory;
import com.beerbong.zipinst.core.plugins.ui.UIPlugin;


public abstract class UIPreferenceFragment extends PreferenceFragment implements IFragment, UIInterface {

    private Core mCore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCore = CoreFactory.getCore();

        ((UIPlugin) mCore.getPlugin(Core.PLUGIN_UI)).registerUI(this);

        addPreferencesFromResource(getContentViewId());

        create(savedInstanceState == null);

        getActivity().setTitle(getTitle());

        if (savedInstanceState != null) {
            restore(savedInstanceState);
        }

        redraw();
    }

    public abstract int getTitle();

    @Override
    public void setTheme(int resid) {
    }

    @Override
    public void redraw() {
        ((UIPlugin) mCore.getPlugin(Core.PLUGIN_UI)).redraw(this);
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
    public View getMainView() {
        return getView();
    }

    protected Core getCore() {
        return mCore;
    }

    @Override
    public void onOptionsItemSelected(int id) {
    }

}
