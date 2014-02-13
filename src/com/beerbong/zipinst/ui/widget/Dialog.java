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

import com.beerbong.zipinst.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Dialog extends android.app.Dialog {

    public interface OnDialogClosedListener {

        public void dialogOk();

        public void dialogCancel();
    }

    public interface WizardListener {

        public void wizardNextStep();

        public void wizardCancelled();

        public void wizardFinished();

        public boolean wizardIsLastStep();

        public boolean wizardIsContinueEnabled();
    }

    public static void alert(Context context, int messageId, OnDialogClosedListener listener) {
        alert(context, context.getResources().getString(messageId), listener);
    }

    public static void alert(Context context, String message, OnDialogClosedListener listener) {
        dialog(context, message, R.string.alert_title, false, listener);
    }

    public static void error(Context context, int messageId, OnDialogClosedListener listener) {
        error(context, context.getResources().getString(messageId), listener);
    }

    public static void error(Context context, String message, OnDialogClosedListener listener) {
        dialog(context, message, R.string.error_title, false, listener);
    }

    public static void dialog(Context context, String message, int titleId, boolean showCancel,
            final OnDialogClosedListener listener) {
        dialog(context, message, titleId, showCancel, android.R.string.ok, android.R.string.cancel,
                listener);
    }

    public static void dialog(Context context, String message, int titleId, boolean showCancel,
            int okId, int cancelId, final OnDialogClosedListener listener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(titleId);
        alert.setMessage(message);
        alert.setCancelable(false);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                if (listener != null) {
                    listener.dialogOk();
                }
            }
        });
        if (showCancel) {
            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();

                    if (listener != null) {
                        listener.dialogCancel();
                    }
                }
            });
        }
        alert.show();
    }

    public static AlertDialog wizard(Context context, int titleId, int layoutId, final WizardListener listener) {
        return wizard(context, titleId, layoutId, R.string.wizard_finish, listener);
    }

    public static AlertDialog wizard(Context context, int titleId, int layoutId, final int finishId, final WizardListener listener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleId);
        builder.setCancelable(false);
        View view = LayoutInflater.from(context).inflate(layoutId, null);
        builder.setView(view);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                listener.wizardCancelled();
            }
        });
        builder.setPositiveButton(R.string.wizard_continue, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (listener.wizardIsLastStep()) {
                            alertDialog.dismiss();
                            listener.wizardFinished();
                        } else {
                            listener.wizardNextStep();
                            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            if (listener.wizardIsLastStep()) {
                                button.setText(finishId);
                            }
                            button.setEnabled(listener.wizardIsContinueEnabled());
                            button.invalidate();
                        }
                    }
                });
            }
        });
        alertDialog.show();
        listener.wizardNextStep();
        Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        button.setText(listener.wizardIsLastStep() ? finishId : R.string.wizard_continue);
        button.setEnabled(listener.wizardIsContinueEnabled());
        button.invalidate();
        return alertDialog;
    }

    public static AlertDialog customDialog(Context context, int layoutId, int titleId, int okButtonTextId,
            boolean showCancel, final OnDialogClosedListener listener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleId);
        View view = LayoutInflater.from(context).inflate(layoutId, null);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setPositiveButton(okButtonTextId, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                if (listener != null) {
                    listener.dialogOk();
                }
            }
        });
        if (showCancel) {
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();

                    if (listener != null) {
                        listener.dialogCancel();
                    }
                }
            });
        }
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
    }

    public static void toast(final Context context, final int resourceId) {
        toast(context, context.getResources().getString(resourceId));
    }

    public static void toast(final Context context, final String message) {
        ((Activity) context).runOnUiThread(new Runnable() {

            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private Dialog(Context context) {
        super(context);
    }
}
