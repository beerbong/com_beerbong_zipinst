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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class IndeterminateDialog extends ProgressDialog {

    public interface IndeterminateDialogCallback {

        public void executeIndeterminate();

        public void finishedIndeterminate();
    }

    private IndeterminateDialogCallback mCallback;

    public IndeterminateDialog(Context context, int messageId, IndeterminateDialogCallback callback) {
        this(context, context.getResources().getString(messageId), callback);
    }

    public IndeterminateDialog(Context context, String message, IndeterminateDialogCallback callback) {
        super(context);

        mCallback = callback;

        setIndeterminate(true);
        setMessage(message);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        show();

        if (mCallback != null) {
            (new AsyncTask<Void, Void, Void>() {
    
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        mCallback.executeIndeterminate();
                    } finally {
                        dismiss();
                    }
                    return (Void) null;
                }
    
                @Override
                protected void onPostExecute(Void result) {
                    mCallback.finishedIndeterminate();
                }
            }).execute((Void) null);
        }
    }
}
