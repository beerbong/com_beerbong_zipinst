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

package com.beerbong.zipinst.core.plugins.superuser;

import android.os.AsyncTask;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.CoreImpl;
import com.beerbong.zipinst.core.Plugin;
import com.beerbong.zipinst.ui.widget.Dialog;
import com.beerbong.zipinst.ui.widget.Dialog.OnDialogClosedListener;

public class SuperUserPlugin extends Plugin {

    private static Shell sShell;
    private static boolean sAccess = false;

    public SuperUserPlugin(Core core) {
        super(core, Core.PLUGIN_SUPERUSER);
    }

    @Override
    public void start() {
        ((CoreImpl) getCore()).setMessage(R.string.requesting_su);
        if (sShell == null) {
            sShell = new Shell();
        }
        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                sAccess = test();
                return (Void) null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (sAccess) {
                    started();
                } else {
                    Dialog.alert(getCore().getContext(), R.string.alert_no_superuser, new OnDialogClosedListener() {

                        @Override
                        public void dialogOk() {
                            started();
                        }

                        @Override
                        public void dialogCancel() {
                        }
                    });
                }
            }
        }).execute((Void) null);
    }

    @Override
    public void stop() {
        stopped();
    }

    public CommandResult run(String s) {
        return sShell.runWaitFor(s, null);
    }

    public void run(String s, ShellCallback callback) {
        sShell.runWaitFor(s, callback);
    }

    public boolean test() {
        return (sAccess = sShell.test());
    }

    public boolean hasAccess() {
        return sAccess;
    }
}
