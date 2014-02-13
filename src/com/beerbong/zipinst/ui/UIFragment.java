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

import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.CoreFactory;
import com.beerbong.zipinst.core.plugins.ui.UIPlugin;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

public abstract class UIFragment extends Fragment implements IFragment, UIInterface, Serializable {

    private Core mCore;
    private View mMainView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mCore = CoreFactory.getCore();

        ((UIPlugin) mCore.getPlugin(Core.PLUGIN_UI)).registerUI(this);

        View rootView = inflater.inflate(getContentViewId(), container, false);

        mMainView = getMainView(rootView);

        create(savedInstanceState == null);

        getActivity().setTitle(getTitle());

        if (savedInstanceState != null) {
            restore(savedInstanceState);
        }

        redraw();

        return rootView;
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
    public View getMainView() {
        return mMainView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    }

    protected Core getCore() {
        return mCore;
    }

}
