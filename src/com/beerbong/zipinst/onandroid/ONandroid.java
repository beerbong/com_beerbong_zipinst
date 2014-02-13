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

package com.beerbong.zipinst.onandroid;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.plugins.recovery.RecoveryInfo;
import com.beerbong.zipinst.core.plugins.recovery.RecoveryPlugin;
import com.beerbong.zipinst.core.plugins.storage.StoragePlugin;
import com.beerbong.zipinst.core.plugins.superuser.CommandResult;
import com.beerbong.zipinst.core.plugins.superuser.ShellCallback;
import com.beerbong.zipinst.core.plugins.superuser.SuperUserPlugin;
import com.beerbong.zipinst.http.DownloadFile;
import com.beerbong.zipinst.http.DownloadTask;
import com.beerbong.zipinst.http.DownloadTask.DownloadTaskListener;
import com.beerbong.zipinst.http.DownloadTask.OnDownloadFinishListener;
import com.beerbong.zipinst.http.URLStringReader;
import com.beerbong.zipinst.http.URLStringReader.URLStringReaderListener;
import com.beerbong.zipinst.io.Files;
import com.beerbong.zipinst.io.SystemProperties;
import com.beerbong.zipinst.preferences.Preferences;
import com.beerbong.zipinst.ui.widget.Dialog;
import com.beerbong.zipinst.ui.widget.Dialog.WizardListener;

public class ONandroid implements WizardListener {

    private static final String SCRIPT_URL = "https://raw.github.com/ameer1234567890/OnlineNandroid/master/onandroid";
    private static final String SCRIPT_VERSION_URL = "https://raw.github.com/ameer1234567890/OnlineNandroid/master/version";
    private static final String SYSTEM_FILE = "/system/partlayout4nandroid";
    private static final String SCRIPT_NAME = "onandroid";
    private static final String PARTITION_LAYOUTS_URL = "https://raw.github.com/ameer1234567890/OnlineNandroid/master/part_layouts/codenames";
    private static final String MTD_DEVICES_URL = "https://raw.github.com/ameer1234567890/OnlineNandroid/master/part_layouts/mtd_devices";
    private static final String PART_LAYOUT_URL = "https://raw.github.com/ameer1234567890/OnlineNandroid/master/part_layouts/zip/part_detect_tool.%s.zip";

    private static final int[] WIZARD_STEPS = new int[] { R.id.step1, R.id.step2, R.id.step3, R.id.step4 };

    public interface ONandroidFinishListener {

        public void oNandroidFinished();
    }

    private Core mCore;
    private File mScriptFile;
    private double mVersion = 0D;

    private ONandroidFinishListener mFinishListener;

    private AlertDialog mWizard;
    private DownloadTask mDownloadTask;
    private int mCurrentWizardStep;

    private File mPartitionsFile;
    private File mMtdDevicesFile;
    private LinkedList<String> mCodenamesManufacturers;
    private LinkedList<String> mCodenames;
    private LinkedList<String> mFilteredCodenames;
    private LinkedList<String> mFilteredCodenamesNames;
    private LinkedList<String> mMtdManufacturers;
    private LinkedList<String> mMtd;
    private LinkedList<String> mFilteredMtd;
    private LinkedList<String> mFilteredMtdNames;
    private Spinner mSpinnerDevice;
    private Spinner mSpinnerMtdDevice;
    private String mBoard;
    private int mFoundManufacturer = -1;

    public ONandroid(Core core) {
        mCore = core;

        mScriptFile = new File(mCore.getContext().getCacheDir(), SCRIPT_NAME);
        mBoard = SystemProperties.getProperty("ro.product.device");

        getVersion();
    }

    public boolean isConfigured() {
        return isPartLayoutPresent() && isScriptPresent();
    }

    public void setONandroidFinishListener(ONandroidFinishListener listener) {
        mFinishListener = listener;
    }

    public void doBackup(String backupName, String backupOptions) {

        final AlertDialog dialog = Dialog.customDialog(mCore.getContext(), R.layout.dialog_onandroid,
                R.string.onandroid_dialog_title, R.string.onandroid_dialog_button, false, null);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        final TextView nandroid = (TextView) dialog.findViewById(R.id.nandroid);
        nandroid.setMovementMethod(new ScrollingMovementMethod());
        nandroid.setText("");

        String command = mScriptFile.getAbsolutePath();

        RecoveryPlugin recPlugin = (RecoveryPlugin) mCore.getPlugin(Core.PLUGIN_RECOVERY);
        RecoveryInfo info = recPlugin.getRecovery();
        if (info.getId() == RecoveryInfo.RECOVERY_TWRP) {
            command += " -w";
            backupOptions = backupOptions.replace("E", "x");
            backupOptions = backupOptions.toLowerCase();
            command += " -a " + backupOptions;
        }

        Preferences prefs = mCore.getPreferences();
        StoragePlugin stoPlugin = (StoragePlugin) mCore.getPlugin(Core.PLUGIN_STORAGE);
        boolean external = stoPlugin.hasExternalStorage() && prefs.isBackupExternalStorage();
        String storage = "";
        if (external) {
            storage = prefs.getExternalStorage();
        } else {
            if (info.isOldBackup()) {
                storage = "data/media";
            } else {
                storage = "sdcard";
            }
        }
        if (!storage.startsWith("/")) {
            storage = "/" + storage;
        }
        while (storage.indexOf("//") >= 0) {
            storage = storage.replace("//", "/");
        }
        command += " -s " + storage;

        command += " -c " + backupName;

        SuperUserPlugin supPlugin = (SuperUserPlugin) mCore.getPlugin(Core.PLUGIN_SUPERUSER);
        supPlugin.run(command, new ShellCallback() {

            @Override
            public void lineRead(final String line) {
                ((Activity) mCore.getContext()).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        String str = nandroid.getText().toString();
                        if (str.length() > 0) {
                            str = str + "\n" + line;
                        } else {
                            str = line;
                        }
                        nandroid.setText(str);
                        int scrollAmount = nandroid.getLayout().getLineTop(
                                nandroid.getLineCount())
                                - nandroid.getHeight();
                        if (scrollAmount > 0) {
                            nandroid.scrollTo(0, scrollAmount);
                        } else {
                            nandroid.scrollTo(0, 0);
                        }
                    }

                });
            }

            @Override
            public void error(final String error) {
                System.err.println(error);
                ((Activity) mCore.getContext()).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        nandroid.setText(nandroid.getText() + "\nERROR: "
                                + error);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        if (mFinishListener != null) {
                            mFinishListener.oNandroidFinished();
                        }
                    }

                });
            }

            @Override
            public void end(final CommandResult result) {
                ((Activity) mCore.getContext()).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (!result.success()) {
                            nandroid.setText("ERROR: " + result.getErrString());
                        }
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        if (mFinishListener != null) {
                            mFinishListener.oNandroidFinished();
                        }
                    }

                });
            }

        });
    }

    public void startWizard() {
        if (isConfigured()) {
            return;
        }
        mCurrentWizardStep = -1;
        mWizard = Dialog.wizard(mCore.getContext(), R.string.onandroid_wizard_title,
                R.layout.wizard_onandroid, R.string.onandroid_wizard_finish, this);
    }

    @Override
    public void wizardNextStep() {
        mCurrentWizardStep++;
        if (mWizard != null) {
            if (mCurrentWizardStep > 0) {
                mWizard.findViewById(WIZARD_STEPS[mCurrentWizardStep - 1]).setVisibility(View.GONE);
            }
            mWizard.findViewById(WIZARD_STEPS[mCurrentWizardStep]).setVisibility(View.VISIBLE);
            switch (mCurrentWizardStep) {
                case 1:
                    step2();
                    break;
                case 2:
                    step3(true);
                    break;
                case 3:
                    step4();
                    break;
            }
        }
    }

    @Override
    public void wizardCancelled() {
        if (mDownloadTask != null) {
            mDownloadTask.cancel(true);
        }
    }

    @Override
    public void wizardFinished() {
        int position = mSpinnerDevice.getSelectedItemPosition();
        String device = mFilteredCodenames.get(position);
        String[] str = device.split("\t");
        String codename = str[0];
        String url = String.format(PART_LAYOUT_URL, new Object[] { codename });
        String name = url.substring(url.lastIndexOf("/") + 1);
        new DownloadFile(mCore, url, name, null, new OnDownloadFinishListener() {

            @Override
            public void onDownloadFinish(File file) {
                mCore.moveToInstall();
            }

            @Override
            public void onDownloadError(Exception ex) {
            }
        });
    }

    @Override
    public boolean wizardIsLastStep() {
        return mCurrentWizardStep == WIZARD_STEPS.length - 1;
    }

    @Override
    public boolean wizardIsContinueEnabled() {
        switch (mCurrentWizardStep) {
            case 1:
            case 2:
                return false;
        }
        return true;
    }

    private void getVersion() {
        if (isScriptPresent()) {
            String line = Files.findLineInFile(mCore.getContext(), mScriptFile, "^version=.*$");
            if (line != null) {
                line = line.replace("version=", "");
                line = line.replace("\"", "");
                mVersion = Double.parseDouble(line);
            }
        }
    }

    private boolean isScriptPresent() {
        return mScriptFile.exists();
    }

    private boolean isPartLayoutPresent() {
        return new File(SYSTEM_FILE).exists();
    }

    private void step2() {
        final TextView text = (TextView) mWizard.findViewById(R.id.wizard_2_text);
        final ProgressBar progress = (ProgressBar) mWizard.findViewById(R.id.wizard_2_progress);
        final TextView percent = (TextView) mWizard.findViewById(R.id.wizard_2_percent);
        progress.setIndeterminate(true);
        percent.setVisibility(View.GONE);

        new URLStringReader(new URLStringReaderListener() {

            @Override
            public void onReadEnd(String buffer) {
                double serverVersion = Double.parseDouble(buffer);
                if (serverVersion > mVersion || !isScriptPresent()) {
                    percent.setVisibility(View.VISIBLE);
                    percent.setText("0%");
                    text.setText(R.string.onandroid_wizard_2_updating);
                    mDownloadTask = new DownloadTask(mCore, SCRIPT_URL, SCRIPT_NAME, new DownloadTaskListener() {

                        @Override
                        public void onDownloadFinish(File file) {
                            SuperUserPlugin sPlugin = (SuperUserPlugin) mCore.getPlugin(Core.PLUGIN_SUPERUSER);
                            sPlugin.run("cp \"" + file.getAbsolutePath() + "\" \"" + mScriptFile.getAbsolutePath() + "\"");
                            sPlugin.run("chmod 777 \"" + mScriptFile.getAbsolutePath() + "\"");
                            mDownloadTask = null;
                            mWizard.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            progress.setVisibility(View.GONE);
                            percent.setVisibility(View.GONE);
                            text.setText(R.string.onandroid_wizard_2_continue);
                        }

                        @Override
                        public void setDownloadProgress(int p) {
                            progress.setIndeterminate(p < 0);
                            progress.setProgress(p);
                            percent.setText(p + "%");
                        }

                        @Override
                        public void setDownloadMax(int max) {
                            if (max > 0) {
                                progress.setMax(max);
                            }
                        }

                        @Override
                        public void onDownloadError(Exception ex) {
                            progress.setVisibility(View.GONE);
                            percent.setVisibility(View.GONE);
                            text.setText(R.string.downloading_error);
                        }

                    });
                    mDownloadTask.execute();
                } else {
                    mWizard.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    progress.setVisibility(View.GONE);
                    percent.setVisibility(View.GONE);
                    text.setText(R.string.onandroid_wizard_2_continue);
                }
            }

            @Override
            public void onReadError(Exception ex) {
                ex.printStackTrace();
                progress.setVisibility(View.GONE);
                percent.setVisibility(View.GONE);
                text.setText(R.string.downloading_error);
            }
            
        }).execute(SCRIPT_VERSION_URL);
    }

    private void step3(final boolean partitions) {
        final TextView text = (TextView) mWizard.findViewById(R.id.wizard_3_text);
        final ProgressBar progress = (ProgressBar) mWizard.findViewById(R.id.wizard_3_progress);
        final TextView percent = (TextView) mWizard.findViewById(R.id.wizard_3_percent);
        progress.setIndeterminate(true);
        percent.setVisibility(View.GONE);

        String url = PARTITION_LAYOUTS_URL;
        String name = "codenames";
        if (!partitions) {
            url = MTD_DEVICES_URL;
            name = "mtd_devices";
        }

        mDownloadTask = new DownloadTask(mCore, url, name, new DownloadTaskListener() {

            @Override
            public void onDownloadFinish(File file) {
                if (partitions) {
                    mPartitionsFile = file;
                    text.setText(R.string.onandroid_wizard_3_downloading_2);
                    step3(false);
                } else {
                    mMtdDevicesFile = file;
                    percent.setVisibility(View.GONE);
                    progress.setIndeterminate(true);
                    text.setText(R.string.onandroid_wizard_3_reading);
                    step3_2();
                }
            }

            @Override
            public void setDownloadProgress(int p) {
                progress.setIndeterminate(p < 0);
                progress.setProgress(p);
                percent.setText(p + "%");
            }

            @Override
            public void setDownloadMax(int max) {
                if (max > 0) {
                    progress.setMax(max);
                }
            }

            @Override
            public void onDownloadError(Exception ex) {
                progress.setVisibility(View.GONE);
                percent.setVisibility(View.GONE);
                text.setText(R.string.downloading_error);
            }

        });
        mDownloadTask.execute();
    }

    private void step3_2() {
        final TextView text = (TextView) mWizard.findViewById(R.id.wizard_3_text);
        final ProgressBar progress = (ProgressBar) mWizard.findViewById(R.id.wizard_3_progress);

        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                String foundManufacturer = null;

                String[] data = Files.readFileSplit(mPartitionsFile.getAbsolutePath());

                mCodenamesManufacturers = new LinkedList<String>();
                mCodenames = new LinkedList<String>();

                for (int i = 0; i < data.length; i++) {
                    String[] str = data[i].split("\t");
                    String manufacturer = str[1].substring(0, str[1].indexOf(" "));
                    if (str[0].equals(mBoard)) {
                        foundManufacturer = manufacturer;
                    }
                    if (mCodenamesManufacturers.indexOf(manufacturer) < 0) {
                        mCodenamesManufacturers.add(manufacturer);
                    }
                    mCodenames.add(data[i]);
                }

                Collections.sort(mCodenamesManufacturers);
                Collections.sort(mCodenames, new Comparator<String>() {

                    @Override
                    public int compare(String arg0, String arg1) {
                        String[] str0 = arg0.split("\t");
                        String[] str1 = arg1.split("\t");
                        return str0[1].compareTo(str1[1]);
                    }
                    
                });

                if (foundManufacturer != null) {
                    mFoundManufacturer = mCodenamesManufacturers.indexOf(foundManufacturer);
                }

                data = Files.readFileSplit(mMtdDevicesFile.getAbsolutePath());

                mMtdManufacturers = new LinkedList<String>();
                mMtd = new LinkedList<String>();

                for (int i = 0; i < data.length; i++) {
                    String[] str = data[i].split("\t");
                    String manufacturer = str[1].substring(0, str[1].indexOf(" "));
                    if (mMtdManufacturers.indexOf(manufacturer) < 0) {
                        mMtdManufacturers.add(manufacturer);
                    }
                    mMtd.add(data[i]);
                }

                Collections.sort(mMtdManufacturers);
                Collections.sort(mMtd, new Comparator<String>() {

                    @Override
                    public int compare(String arg0, String arg1) {
                        String[] str0 = arg0.split("\t");
                        String[] str1 = arg1.split("\t");
                        return str0[1].compareTo(str1[1]);
                    }
                    
                });
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                progress.setVisibility(View.GONE);
                text.setText(R.string.onandroid_wizard_3_continue);
                mWizard.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        }).execute((Void) null);
    }

    private void step4() {

        Spinner spinnerMan = (Spinner) mWizard.findViewById(R.id.onandroid_manufacturer);
        Spinner spinnerManMtd = (Spinner) mWizard.findViewById(R.id.onandroid_manufacturer_mtd);

        mSpinnerDevice = (Spinner) mWizard.findViewById(R.id.onandroid_device);
        mSpinnerMtdDevice = (Spinner) mWizard.findViewById(R.id.onandroid_device_mtd);

        ArrayAdapter<String> adapterMan = new ArrayAdapter<String>(mCore.getContext(),
                android.R.layout.simple_spinner_item, mCodenamesManufacturers);
        spinnerMan.setAdapter(adapterMan);

        adapterMan = new ArrayAdapter<String>(mCore.getContext(),
                android.R.layout.simple_spinner_item, mMtdManufacturers);
        spinnerManMtd.setAdapter(adapterMan);

        spinnerMan.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                String manufacturer = mCodenamesManufacturers.get(position);
                filter(manufacturer);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        spinnerManMtd.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                String manufacturer = mMtdManufacturers.get(position);
                filter2(manufacturer);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        if (mFoundManufacturer >= 0) {
            spinnerMan.setSelection(mFoundManufacturer);
        }
    }

    private void filter(String manufacturer) {
        mFilteredCodenames = new LinkedList<String>();
        mFilteredCodenamesNames = new LinkedList<String>();

        int foundDevice = -1;

        for (int i = 0; i < mCodenames.size(); i++) {
            String device = mCodenames.get(i);
            String[] str = device.split("\t");
            String man = str[1].substring(0, str[1].indexOf(" "));
            if (man.equals(manufacturer)) {
                if (str[0].equals(mBoard)) {
                    foundDevice = mFilteredCodenames.size();
                }
                mFilteredCodenames.add(device);
                mFilteredCodenamesNames.add(str[1] + " (" + str[0] + ")");
            }
        }

        ArrayAdapter<String> adapterMan = new ArrayAdapter<String>(mCore.getContext(),
                android.R.layout.simple_spinner_item, mFilteredCodenamesNames);
        mSpinnerDevice.setAdapter(adapterMan);

        if (foundDevice >= 0) {
            mSpinnerDevice.setSelection(foundDevice);
        }
    }

    private void filter2(String manufacturer) {
        mFilteredMtd = new LinkedList<String>();
        mFilteredMtdNames = new LinkedList<String>();

        for (int i = 0; i < mMtd.size(); i++) {
            String device = mMtd.get(i);
            String[] str = device.split("\t");
            String man = str[1].substring(0, str[1].indexOf(" "));
            if (man.equals(manufacturer)) {
                mFilteredMtd.add(device);
                mFilteredMtdNames.add(str[1] + " (" + str[0] + ")");
            }
        }

        ArrayAdapter<String> adapterMan = new ArrayAdapter<String>(mCore.getContext(),
                android.R.layout.simple_spinner_item, mFilteredMtdNames);
        mSpinnerMtdDevice.setAdapter(adapterMan);
    }
}
