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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.activities.Folder;
import com.beerbong.zipinst.manager.SUManager.CommandResult;
import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.ui.UIListener;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.DownloadTask;
import com.beerbong.zipinst.util.FileItem;
import com.beerbong.zipinst.util.NoSuException;
import com.beerbong.zipinst.util.StoredItems;

public class FileManager extends Manager implements UIListener {

    private String mInternalStoragePath;
    private String mExternalStoragePath;
    private int mSelectedBackup = -1;

    protected FileManager(Context context) throws NoSuException {
        super(context);

        UI.getInstance().addUIListener(this);

        init();
    }

    private void init() throws NoSuException {

        readMounts();

        if (ManagerFactory.getPreferencesManager().isAutoloadList()) {
            loadList();
        }

        Intent intent = getActivity().getIntent();
        onNewIntent(intent);
    }

    public void onButtonClicked(int id) {
        if (id == R.id.choose_zip) {
            if (ManagerFactory.getPreferencesManager().isUseFolder()) {
                File folder = new File(ManagerFactory.getPreferencesManager().getFolder());
                if (!folder.exists() || !folder.isDirectory()) {
                    Toast.makeText(mContext, R.string.folder_error, Toast.LENGTH_SHORT).show();
                } else {
                    mContext.startActivity(new Intent(mContext, Folder.class));
                }
            } else {
                PackageManager packageManager = mContext.getPackageManager();
                Intent test = new Intent(Intent.ACTION_GET_CONTENT);
                test.setType("file/*");
                List<ResolveInfo> list = packageManager.queryIntentActivities(test,
                        PackageManager.GET_ACTIVITIES);
                if (list.size() > 0) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                    intent.setType("file/*");
                    getActivity().startActivityForResult(intent, Constants.REQUEST_PICK_FILE);
                } else {
                    // No app installed to handle the intent - file explorer
                    // required
                    Toast.makeText(mContext, R.string.install_file_manager_error,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void onFileItemClicked(FileItem item) {
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
    }

    public boolean hasExternalStorage() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public boolean isExternalStorage(String path) {
        return !path.startsWith(mInternalStoragePath) && !path.startsWith("/sdcard")
                && !path.startsWith("/mnt/sdcard");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_PICK_FILE) {
            if (data == null) {
                // Nothing returned by user, probably pressed back button in
                // file manager
                return;
            }

            String filePath = data.getData().getPath();

            addFile(filePath);

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
            addFile(path);
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
                    FileItem item = StoredItems.getItem(i);
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

    public void addFile(String filePath) {

        if (filePath == null || (!filePath.endsWith(".zip") && !filePath.endsWith(".sh"))) {
            Toast.makeText(mContext, R.string.install_file_manager_invalid_zip, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (!filePath.endsWith(".zip") && !filePath.endsWith(".sh")) {
            Toast.makeText(mContext, R.string.install_file_manager_zip, Toast.LENGTH_SHORT).show();
            return;
        }

        if (filePath.endsWith(".sh") && isExternalStorage(filePath)) {
            // sh from external sdcard not allowed
            Toast.makeText(mContext, R.string.install_file_manager_intsdcard, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        String sdcardPath = new String(filePath);

        filePath = getPath(filePath);

        File file = new File(sdcardPath);
        if (!file.exists()) {
            Toast.makeText(mContext, R.string.install_file_manager_not_found_zip, Toast.LENGTH_LONG)
                    .show();
        } else {

            UI.getInstance().addItem(filePath, sdcardPath);
        }
    }

    public String getPath(String path) {
        String filePath = new String(path);

        String internalStorage = ManagerFactory.getPreferencesManager().getInternalStorage();
        String externalStorage = ManagerFactory.getPreferencesManager().getExternalStorage();

        String[] internalNames = new String[] {
                mInternalStoragePath,
                "/mnt/sdcard",
                "/storage/sdcard/",
                "/sdcard",
                "/storage/sdcard0",
                "/storage/emulated/0" };
        String[] externalNames = new String[] {
                mExternalStoragePath == null ? " " : mExternalStoragePath,
                "/mnt/extSdCard",
                "/storage/extSdCard/",
                "/extSdCard",
                "/storage/sdcard1",
                "/storage/emulated/1" };
        for (int i = 0; i < internalNames.length; i++) {
            String internalName = internalNames[i];
            String externalName = externalNames[i];
            boolean external = isExternalStorage(filePath);
            if (filePath.endsWith(".sh")) {
                if (!external) {
                    if (filePath.startsWith(internalName)) {
                        if (internalName.endsWith("/")) {
                            filePath = filePath.replace(internalName, "/" + "sdcard" + "/");
                        } else {
                            filePath = filePath.replace(internalName, "/" + "sdcard");
                        }
                        break;
                    }
                }
            } else {
                if (filePath.startsWith(externalName)) {
                    if (externalName.endsWith("/")) {
                        filePath = filePath.replace(externalName, "/" + externalStorage + "/");
                    } else {
                        filePath = filePath.replace(externalName, "/" + externalStorage);
                    }
                    break;
                } else if (filePath.startsWith(internalName)) {
                    if (internalName.endsWith("/")) {
                        filePath = filePath.replace(internalName, "/" + internalStorage + "/");
                    } else {
                        filePath = filePath.replace(internalName, "/" + internalStorage);
                    }
                    break;
                }
            }
        }
        return filePath;
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
            if (!fileName.contains(".zip") && !fileName.contains(".apk")) {
                fileName = "unknown-file-name.zip";
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
            String scheme = zipUri.getScheme();
            if (ContentResolver.SCHEME_CONTENT.endsWith(scheme)) {
                String[] projection = { MediaStore.Images.Media.DATA };
                Cursor cursor = mContext.getContentResolver().query(zipUri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                addFile(cursor.getString(column_index));
            } else if (ContentResolver.SCHEME_FILE.endsWith(scheme)) {
                addFile(zipUri.getPath());
            }
        }
    }

    private void handleSendMultipleZips(Intent intent) {
        ArrayList<Uri> zipUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (zipUris != null) {
            for (int i = 0; i < zipUris.size(); i++) {
                addFile(zipUris.get(i).getPath());
            }
        }
    }

    private void showInfoDialog(final FileItem item) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(mContext.getResources().getString(R.string.alert_file_title,
                new Object[] { item.getName() }));

        String path = item.getPath();
        File file = new File(path);

        alert.setMessage(mContext.getResources().getString(
                R.string.alert_file_summary,
                new Object[] { (file.getParent() == null ? "" : file.getParent()) + "/",
                        Constants.formatSize(file.length()),
                        Constants.formatDate(file.lastModified()) }));

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

        alert.show();
    }

    private void showMd5Dialog(final FileItem item) {

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

    public boolean recursiveDelete(File f) {
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

    public boolean writeToFile(String data, String path, String fileName) {

        File folder = new File(path);
        File file = new File(folder, fileName);

        folder.mkdirs();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data.getBytes());
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public String readAssets(Context contex, String fileName) {
        BufferedReader in = null;
        StringBuilder data = null;
        try {
            data = new StringBuilder(2048);
            char[] buf = new char[2048];
            int nRead = -1;
            in = new BufferedReader(new InputStreamReader(contex.getAssets().open(fileName)));
            while ((nRead = in.read(buf)) != -1) {
                data.append(buf, 0, nRead);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        if (TextUtils.isEmpty(data)) {
            return null;
        }
        return data.toString();
    }

    public double getSpaceLeft() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks() * (double) stat.getBlockSize();
        // One binary gigabyte equals 1,073,741,824 bytes.
        return sdAvailSize / 1073741824;
    }

    private void readMounts() throws NoSuException {

        ArrayList<String> mounts = new ArrayList<String>();
        ArrayList<String> vold = new ArrayList<String>();

        try {
            Scanner scanner = new Scanner(new File("/proc/mounts"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.startsWith("/dev/block/vold/")) {
                    String[] lineElements = line.split(" ");
                    String element = lineElements[1];

                    mounts.add(element);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mounts.size() == 0 || (mounts.size() == 1 && hasExternalStorage())) {
            mounts.add("/mnt/sdcard");
        }

        File fstab = findFstab();
        if (fstab != null) {
            try {
                copyOrRemoveCache(fstab, true);

                Scanner scanner = new Scanner(new File("/cache/" + fstab.getName()));
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("dev_mount")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[2];
    
                        if (element.contains(":")) {
                            element = element.substring(0, element.indexOf(":"));
                        }
    
                        if (element.toLowerCase().indexOf("usb") < 0) {
                            vold.add(element);
                        }
                    } else if (line.startsWith("/devices/platform")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[1];
    
                        if (element.contains(":")) {
                            element = element.substring(0, element.indexOf(":"));
                        }
    
                        if (element.toLowerCase().indexOf("usb") < 0) {
                            vold.add(element);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                copyOrRemoveCache(fstab, false);
            }
        }
        if (vold.size() == 0 || (vold.size() == 1 && hasExternalStorage())) {
            vold.add("/mnt/sdcard");
        }

        for (int i = 0; i < mounts.size(); i++) {
            String mount = mounts.get(i);
            File root = new File(mount);
            if (!vold.contains(mount) || (!root.exists() || !root.isDirectory() || !root.canWrite())) {
                mounts.remove(i--);
            }
        }

        for (int i = 0; i < mounts.size(); i++) {
            String mount = mounts.get(i);
            if (mount.indexOf("sdcard0") >= 0 || mount.equalsIgnoreCase("/mnt/sdcard")
                    || mount.equalsIgnoreCase("/sdcard")) {
                mInternalStoragePath = mount;
            } else {
                mExternalStoragePath = mount;
            }
        }

        if (mInternalStoragePath == null) {
            mInternalStoragePath = "/sdcard";
        }
    }

    private File findFstab() {

        File file = null;

        file = new File("/system/etc/vold.fstab");
        if (file.exists()) {
            return file;
        }

        SUManager suManager = ManagerFactory.getSUManager(mContext);
        SUManager.CommandResult cm = suManager.runWaitFor("grep -ls \"/dev/block/\" * --include=fstab.* --exclude=fstab.goldfish");
        if (cm.stdout != null) {
            String[] files = cm.stdout.split("\n");
            for (int i = 0; i < files.length; i++) {
                file = new File(files[i]);
                if (file.exists()) {
                    return file;
                }
            }
        }

        return null;
    }

    private void copyOrRemoveCache(File file, boolean copy) throws NoSuException {
        SUManager suManager = ManagerFactory.getSUManager(mContext);
        CommandResult cm = null;
        if (copy) {
            cm = suManager.runWaitFor("cp " + file.getAbsolutePath() + " /cache/" + file.getName());
            suManager.runWaitFor("chmod 644 /cache/" + file.getName());
        } else {
            cm = suManager.runWaitFor("rm -f /cache/" + file.getName());
        }
        if (!cm.success()) {
            throw new NoSuException();
        }
    }
}