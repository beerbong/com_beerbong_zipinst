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

package com.beerbong.zipinst.core.plugins.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.CoreImpl;
import com.beerbong.zipinst.core.Plugin;
import com.beerbong.zipinst.ui.UIInterface;

public class UIPlugin extends Plugin {

    private static final String ROBOTO_THIN = "font/Roboto-Light.ttf";

    private Map<String, UIInterface> mUiInterfaces;
    private Typeface mRobotoThin;

    public UIPlugin(Core core) {
        super(core, Core.PLUGIN_UI);
    }

    @Override
    public void start() {
        ((CoreImpl) getCore()).setMessage(R.string.initializing_ui);


        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                mUiInterfaces = new HashMap<String, UIInterface>();

                mRobotoThin = Typeface
                        .createFromAsset(getCore().getContext().getAssets(), ROBOTO_THIN);
                return (Void) null;
            }

            @Override
            protected void onPostExecute(Void result) {
                started();
            }
        }).execute((Void) null);
    }

    @Override
    public void stop() {
        stopped();
    }

    public void unregisterUI(UIInterface uIInterface) {
        if (mUiInterfaces != null) {
            mUiInterfaces.remove(uIInterface.getClass().getName());
        }
    }

    public void registerUI(UIInterface uIInterface) {
        boolean darkTheme = getCore().getPreferences().isDarkTheme();
        uIInterface.setTheme(darkTheme ? R.style.AppTheme_Dark
                : R.style.AppTheme);

        redraw(uIInterface);

        if (mUiInterfaces != null) {
            mUiInterfaces.put(uIInterface.getClass().getName(), uIInterface);
        }
    }

    public void redraw() {
        Iterator<String> it = mUiInterfaces.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            UIInterface uiInterface = mUiInterfaces.get(key);
            redraw(uiInterface);
        }
    }

    public void redraw(UIInterface uIInterface) {
        redraw(uIInterface.getMainView());
    }

    public void redraw(View view) {
        setFont(view);
    }

    private void setFont(View view) {
        if (view == null) {
            return;
        }
        if (view instanceof ViewGroup) {
            int count = ((ViewGroup) view).getChildCount();
            for (int i = 0; i < count; i++) {
                setFont(((ViewGroup) view).getChildAt(i));
            }
        } else if (view instanceof TextView) {
            ((TextView) view).setTypeface(mRobotoThin);
        }
    }

}
