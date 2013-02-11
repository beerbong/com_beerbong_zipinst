/*
 * Copyright (C) 2013 ZipInstaller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.beerbong.zipinst.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.ui.UIListener;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.DownloadTask;
import com.beerbong.zipinst.util.StoredItems;
import com.beerbong.zipinst.util.ZipItem;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class FileManager extends Manager implements UIListener {

    private NodeList pathList = null;
    private int mSelectedBackup = -1;

    protected FileManager(Context context) {
        super(context);

        UI.getInstance().addUIListener(this);

        init();
    }

    private void init() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(mContext.getAssets().open("paths.xml"));

            pathList = doc.getElementsByTagName("path");

        } catch (Exception ex) {
            Toast.makeText(mContext, R.string.paths_error, Toast.LENGTH_LONG).show();
            return;
        }
        
        if (ManagerFactory.getPreferencesManager().isAutoloadList()) {
            loadList();
        }

        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if ("application/zip".equals(type) || "*/*".equals(type)) {
            if (Intent.ACTION_SEND.equals(action)) {
                handleSendZip(intent);
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                handleSendMultipleZips(intent);
            }
        }
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri zipUri = (Uri) intent.getData();
            download(mContext, zipUri.toString(), null, null);
        }

        onNewIntent(intent);
    }

    public void onButtonClicked(int id) {
        if (id == R.id.choose_zip) {
            PackageManager packageManager = mContext.getPackageManager();
            Intent test = new Intent(Intent.ACTION_GET_CONTENT);
            test.setType("file/*");
            List<ResolveInfo> list = packageManager.queryIntentActivities(test,
                    PackageManager.GET_ACTIVITIES);
            if (list.size() > 0) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                intent.setType("file/*");
                getActivity().startActivityForResult(intent, Constants.REQUEST_PICK_ZIP);
            } else {
                // No app installed to handle the intent - file explorer
                // required
                Toast.makeText(mContext, R.string.install_file_manager_error, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public void onZipItemClicked(ZipItem item) {
        showInfoDialog(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu) {
    }

    @Override
    public void onOptionsItemSelected(MenuItem item) {
    }

    @Override
    public void onNewIntent(Intent intent) {

        if (intent.getExtras() != null && intent.getExtras().containsKey("NOTIFICATION_ID")) {
            String url = intent.getExtras().getString("URL");
            String md5 = intent.getStringExtra("MD5");
            String name = intent.getStringExtra("ZIP_NAME");

            download(mContext, url, name, md5);
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_PICK_ZIP) {
            if (data == null) {
                // Nothing returned by user, probably pressed back button in
                // file manager
                return;
            }

            String zipPath = data.getData().getEncodedPath();

            addZip(zipPath);

        }
    }

    public void saveList() {
        int size = StoredItems.size();
        if (size == 0)
            return;

        StringBuffer list = new StringBuffer();

        for (int i = 0; i < size; i++) {
            String path = StoredItems.getItem(i).getPath();
            list.append(path);
            if (i < size - 1)
                list.append("\n");
        }

        ManagerFactory.getPreferencesManager().setList(list.toString());

        Toast.makeText(mContext, R.string.list_saved, Toast.LENGTH_SHORT).show();
    }

    public void loadList() {

        PreferencesManager pManager = ManagerFactory.getPreferencesManager();

        if (pManager.isOverrideList()) {
            UI.getInstance().removeAllItems();
        }

        String list = pManager.getList();

        StringTokenizer tokenizer = new StringTokenizer(list, "\n");
        while (tokenizer.hasMoreTokens()) {
            String path = tokenizer.nextToken();
            addZip(path);
        }

        Toast.makeText(mContext, R.string.list_loaded, Toast.LENGTH_SHORT).show();
    }

    public void checkFilesAndMd5(final RebootManager manager) {

        final boolean checkExists = ManagerFactory.getPreferencesManager().isCheckExists();
        final boolean checkMd5 = ManagerFactory.getPreferencesManager().isCheckMD5();

        if (!checkExists && !checkMd5) {
            manager.showRebootDialog();
            return;
        }

        final ProgressDialog pDialog = new ProgressDialog(mContext);
        pDialog.setIndeterminate(true);
        pDialog.setMessage(mContext.getResources().getString(R.string.alert_file_checking));
        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();

        (new Thread() {

            public void run() {

                int size = StoredItems.size();
                for (int i = 0; i < size; i++) {
                    ZipItem item = StoredItems.getItem(i);
                    String path = item.getPath();
                    final File file = new File(path);

                    getActivity().runOnUiThread(new Runnable() {

                        public void run() {
                            pDialog.setMessage(mContext.getResources().getString(
                                    R.string.alert_file_exists_checking,
                                    new Object[] { file.getName() }));
                        }
                    });

                    if (checkExists && !file.exists()) {
                        pDialog.dismiss();
                        showAlertOnUIThread(R.string.alert_file_alert,
                                R.string.alert_file_not_exists, new Object[] { file.getName() });
                        return;
                    }

                    if (checkMd5) {

                        getActivity().runOnUiThread(new Runnable() {

                            public void run() {
                                pDialog.setMessage(mContext.getResources().getString(
                                        R.string.alert_file_md5_checking,
                                        new Object[] { file.getName() }));
                            }
                        });

                        File folder = file.getParentFile();
                        File md5File = new File(folder, file.getName() + ".md5sum");
                        if (md5File.exists()) {
                            String content[] = readMd5File(md5File);
                            if (!file.getName().equals(content[1])) {
                                pDialog.dismiss();
                                showAlertOnUIThread(R.string.alert_file_alert,
                                        R.string.alert_file_incorrect_md5_file,
                                        new Object[] { file.getName() });
                                return;
                            }
                            String md5 = Constants.md5(file);
                            if (!md5.equals(content[0])) {
                                pDialog.dismiss();
                                showAlertOnUIThread(R.string.alert_file_alert,
                                        R.string.alert_file_incorrect_md5,
                                        new Object[] { file.getName() });
                                return;
                            }
                        }
                    }
                }

                pDialog.dismiss();
                getActivity().runOnUiThread(new Runnable() {

                    public void run() {
                        manager.showRebootDialog();
                    }
                });
            }
        }).start();
    }

    public void downloadZip() {
        final EditText input = new EditText(mContext);
        input.setText("http://");
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(mContext)
                .setTitle(R.string.downloadzip_alert_title)
                .setMessage(R.string.downloadzip_alert_summary)
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        final String value = input.getText().toString();

                        if (value == null || "".equals(value.trim())) {
                            Toast.makeText(mContext, R.string.downloadzip_alert_error,
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }

                        dialog.dismiss();

                        ((Activity) mContext).runOnUiThread(new Runnable() {

                            public void run() {
                                download(mContext, value, null, null);
                            }
                        });
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public void selectDownloadPath(final Activity activity) {
        final EditText input = new EditText(activity);
        input.setText(ManagerFactory.getPreferencesManager().getDownloadPath());

        new AlertDialog.Builder(activity)
                .setTitle(R.string.download_alert_title)
                .setMessage(R.string.download_alert_summary)
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();

                        if (value == null || "".equals(value.trim()) || !value.startsWith("/")) {
                            Toast.makeText(activity, R.string.download_alert_error,
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }

                        ManagerFactory.getPreferencesManager().setDownloadPath(value);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public void addZip(String zipPath) {

        if (zipPath == null || !zipPath.endsWith(".zip")) {
            Toast.makeText(mContext, R.string.install_file_manager_invalid_zip, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        for (int i = 0; i < pathList.getLength(); i++) {
            String name = pathList.item(i).getAttributes().getNamedItem("name").getNodeValue();
            String allowed = pathList.item(i).getAttributes().getNamedItem("allowed")
                    .getNodeValue();
            if ("0".equals(allowed) && zipPath.startsWith(name)) {
                // external sdcard not allowed
                Toast.makeText(mContext, R.string.install_file_manager_intsdcard,
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!zipPath.endsWith(".zip")) {
            Toast.makeText(mContext, R.string.install_file_manager_zip, Toast.LENGTH_SHORT).show();
            return;
        }

        String sdcardPath = new String(zipPath);

        String internalStorage = ManagerFactory.getPreferencesManager().getInternalStorage();

        for (int i = 0; i < pathList.getLength(); i++) {
            String name = pathList.item(i).getAttributes().getNamedItem("name").getNodeValue();
            String allowed = pathList.item(i).getAttributes().getNamedItem("allowed")
                    .getNodeValue();
            if ("1".equals(allowed) && zipPath.startsWith(name))
                zipPath = zipPath.replace(name, "/" + internalStorage);
        }

        UI.getInstance().addItem(zipPath, sdcardPath);
    }

    public void showDeleteDialog(final Context context) {

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.alert_delete_title);

        final String backupFolder = ManagerFactory.getRecoveryManager().getBackupDir(true);
        final String[] backups = ManagerFactory.getRecoveryManager().getBackupList();
        mSelectedBackup = backups.length > 0 ? 0 : -1;

        alert.setSingleChoiceItems(backups, mSelectedBackup, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                mSelectedBackup = which;
            }
        });

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                if (mSelectedBackup >= 0) {
                    final String toDelete = backupFolder + backups[mSelectedBackup];

                    final ProgressDialog pDialog = new ProgressDialog(context);
                    pDialog.setIndeterminate(true);
                    pDialog.setMessage(context.getResources().getString(
                            R.string.alert_deleting_folder));
                    pDialog.setCancelable(false);
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.show();

                    (new Thread() {

                        public void run() {

                            recursiveDelete(new File(toDelete));

                            pDialog.dismiss();
                        }
                    }).start();
                }
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();

    }

    public void download(Context context, String url, String fileName, String md5) {

        final ProgressDialog progressDialog = new ProgressDialog(context);

        if (fileName == null) {
            fileName = url.substring(url.lastIndexOf("/") + 1);
            if (fileName.indexOf("?") >= 0) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }
        }

        final DownloadTask downloadFile = new DownloadTask(progressDialog, url, fileName, md5);

        progressDialog.setMessage(context.getResources().getString(R.string.downloading,
                new Object[] { url, ManagerFactory.getPreferencesManager().getDownloadPath() }));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setProgress(0);
        progressDialog.setButton(Dialog.BUTTON_NEGATIVE,
                context.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.dismiss();
                        downloadFile.cancel(true);
                    }
                });

        downloadFile.attach(progressDialog);
        progressDialog.show();
        downloadFile.execute();
    }

    private void handleSendZip(Intent intent) {
        Uri zipUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (zipUri != null) {
            addZip(zipUri.getEncodedPath());
        }
    }

    private void handleSendMultipleZips(Intent intent) {
        ArrayList<Uri> zipUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (zipUris != null) {
            for (int i = 0; i < zipUris.size(); i++) {
                addZip(zipUris.get(i).getEncodedPath());
            }
        }
    }

    private void showInfoDialog(final ZipItem item) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(mContext.getResources().getString(R.string.alert_file_title,
                new Object[] { item.getName() }));

        String path = item.getPath();
        File file = new File(path);

        alert.setMessage(mContext.getResources().getString(
                R.string.alert_file_summary,
                new Object[] {
                        (file.getParent() == null ? "" : file.getParent()) + "/",
                        Constants.formatSize(file.length()),
                        new Date(file.lastModified()).toString() }));

        alert.setPositiveButton(R.string.alert_file_close, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.setNeutralButton(R.string.alert_file_md5, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                showMd5Dialog(item);
            }
        });
        alert.setNegativeButton(R.string.alert_file_delete, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                UI.getInstance().removeItem(item);
            }
        });

        alert.show();
    }

    private void showMd5Dialog(final ZipItem item) {

        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(R.string.alert_md5_title);
        alert.setMessage(R.string.alert_md5_summary);

        final EditText input = new EditText(mContext);
        alert.setView(input);
        input.selectAll();

        alert.setPositiveButton(R.string.alert_md5_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                final ProgressDialog pDialog = new ProgressDialog(mContext);
                pDialog.setIndeterminate(true);
                pDialog.setMessage(mContext.getResources().getString(R.string.alert_md5_loading));
                pDialog.setCancelable(false);
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.show();

                (new Thread() {

                    public void run() {

                        String path = item.getPath();
                        File file = new File(path);
                        final String md5 = Constants.md5(file);

                        pDialog.dismiss();

                        final String text = input.getText() == null ? null : input.getText()
                                .toString();

                        getActivity().runOnUiThread(new Runnable() {

                            public void run() {
                                showMd5(md5, text);
                            }
                        });
                    }
                }).start();
            }
        });
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void showMd5(String md5, String text) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        if (text == null || "".equals(text.trim())) {
            alert.setMessage(md5);
        } else {
            if (md5.equals(text)) {
                alert.setMessage(mContext.getResources().getString(R.string.alert_md5_match));
            } else {
                alert.setMessage(mContext.getResources().getString(R.string.alert_md5_no_match,
                        new Object[] { text, md5 }));
            }
        }
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private String[] readMd5File(File file) {
        try {
            StringBuffer fileData = new StringBuffer(1000);
            BufferedReader reader;

            reader = new BufferedReader(new FileReader(file));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
            reader.close();
            String content = fileData.toString();
            StringTokenizer st = new StringTokenizer(content, " ");
            return new String[] { st.nextToken(), st.nextToken() };
        } catch (Exception e) {
        }
        return null;
    }

    private void showAlertOnUIThread(final int titleId, final int messageId,
            final Object[] messageParams) {
        getActivity().runOnUiThread(new Runnable() {

            public void run() {
                AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                alert.setTitle(titleId);
                alert.setMessage(mContext.getResources().getString(messageId, messageParams));
                alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        });
    }

    private Activity getActivity() {
        return (Activity) mContext;
    }

    public static boolean recursiveDelete(File f) {
        try {
            if (f.isDirectory()) {
                File[] files = f.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (!recursiveDelete(files[i])) {
                        return false;
                    }
                }
                if (!f.delete()) {
                    return false;
                }
            } else {
                if (!f.delete()) {
                    return false;
                }
            }
        } catch (Exception ignore) {
        }
        return true;
    }
}