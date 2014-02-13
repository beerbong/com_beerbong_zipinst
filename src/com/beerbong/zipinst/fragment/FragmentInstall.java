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

package com.beerbong.zipinst.fragment;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.BadTokenException;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.RequestFileActivity;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.plugins.reboot.RebootPlugin;
import com.beerbong.zipinst.core.plugins.recovery.RecoveryPlugin;
import com.beerbong.zipinst.core.plugins.storage.StoragePlugin;
import com.beerbong.zipinst.http.DownloadFile;
import com.beerbong.zipinst.http.DownloadTask;
import com.beerbong.zipinst.http.URLStringReader;
import com.beerbong.zipinst.http.URLStringReader.URLStringReaderListener;
import com.beerbong.zipinst.io.Files;
import com.beerbong.zipinst.io.Strings;
import com.beerbong.zipinst.io.SystemProperties;
import com.beerbong.zipinst.preferences.Preferences;
import com.beerbong.zipinst.preferences.Rule;
import com.beerbong.zipinst.store.FileItem;
import com.beerbong.zipinst.store.FileItemStore;
import com.beerbong.zipinst.store.FileItemStore.FileItemStoreListener;
import com.beerbong.zipinst.store.FileItemsAdapter;
import com.beerbong.zipinst.store.FileItemsAdapter.FileItemsAdapterHolder;
import com.beerbong.zipinst.ui.UIFragment;
import com.beerbong.zipinst.ui.widget.Dialog;
import com.beerbong.zipinst.ui.widget.Dialog.OnDialogClosedListener;
import com.beerbong.zipinst.ui.widget.FolderPicker;
import com.beerbong.zipinst.ui.widget.RecoveryPicker;
import com.beerbong.zipinst.ui.widget.RecoveryPicker.RecoveryPickerClicked;
import com.mobeta.android.dslv.DragSortListView;

public class FragmentInstall extends UIFragment implements OnItemClickListener,
        FileItemStoreListener, FileItemsAdapterHolder {

    private DragSortListView mFileList;
    private TextView mHelp;

    private DragSortListView.DropListener mDropListener = new DragSortListView.DropListener() {

        public void drop(int from, int to) {
            FileItemStore.move(from, to);
            redrawItems();
        }
    };

    @Override
    public int getContentViewId() {
        return R.layout.fragment_install;
    }

    @Override
    public View getMainView(View rootView) {
        return rootView.findViewById(R.id.install_layout);
    }

    @Override
    public int getTitle() {
        return R.string.install_title;
    }

    @Override
    public void create(boolean isNew) {

        mFileList = (DragSortListView) getMainView().findViewById(R.id.file_list);
        mFileList.setUiInterface(this);
        mFileList.setOnItemClickListener(this);
        mFileList.setDropListener(mDropListener);
        mFileList.setRemoveListener(new DragSortListView.RemoveListener() {

            @Override
            public void remove(int which) {
                FileItem item = FileItemStore.getItem(which);
                removeItem(item);
            }
        });

        mHelp = (TextView) getMainView().findViewById(R.id.install_fragment_help);

        redrawItems();

        if (isNew) {
            FileItemStore.setFileItemStoreListener(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileItem item = FileItemStore.getItem(position);
        showInfoDialog(item);
    }

    @Override
    public int[] getVisibleMenuItems() {
        RecoveryPlugin rPlugin = (RecoveryPlugin) getCore().getPlugin(Core.PLUGIN_RECOVERY);
        int[] list = new int[] {
                R.id.menu_add_zip,
                R.id.menu_install,
                0,
                0,
                0
        };
        if (getCore().getPreferences().getRules().size() > 0) {
            list[2] = R.id.menu_apply_rules;
        }
        if (rPlugin.hasRecoveryBlock()) {
            list[3] = R.id.menu_install_recovery;
        }
        if (rPlugin.hasBootBlock()) {
            list[4] = R.id.menu_install_boot;
        }
        return list;
    }

    @Override
    public void restore(Bundle savedInstanceState) {
    }

    @Override
    public void save(Bundle outState) {
    }

    @Override
    public void storeChanged() {
        redrawItems();
    }

    public void removeItem(FileItem item) {

        FileItemStore.removeItem(item.getKey());

        redrawItems();

    }

    public void redrawItems() {

        mHelp.setVisibility(FileItemStore.size() > 0 ? View.GONE : View.VISIBLE);

        mFileList.setAdapter(new FileItemsAdapter(getCore(), this, FileItemStore.getItems()));

        redraw();
    }

    @Override
    public boolean useDragAndDrop() {
        return getCore().getPreferences().isUseDragAndDrop();
    }

    @Override
    public boolean canRemove() {
        return true;
    }

    @Override
    public boolean showPath() {
        return true;
    }

    @Override
    public boolean showSize() {
        return false;
    }

    @Override
    public boolean showDate() {
        return false;
    }

    @Override
    public int getItemLayoutId() {
        return R.layout.item_file;
    }

    @Override
    public void onOptionsItemSelected(int id) {
        switch (id) {
            case R.id.menu_add_zip:
                addZip();
                break;
            case R.id.menu_install:
                RebootPlugin rPlugin = (RebootPlugin) getCore().getPlugin(Core.PLUGIN_REBOOT);
                rPlugin.checkFilesAndMd5();
                break;
            case R.id.menu_apply_rules:
                applyRules();
                break;
            case R.id.menu_install_boot:
                selectImage(true);
                break;
            case R.id.menu_install_recovery:
                new RecoveryPicker(getCore().getContext(), new RecoveryPickerClicked() {

                    @Override
                    public void onRecoveryPickerClicked(int id) {
                        switch (id) {
                            case RecoveryPicker.ZIP:
                                selectImage(false);
                                break;
                            case RecoveryPicker.TWRP:
                                getTwrpUrl();
                                break;
                        }
                    }
                }).show();
                break;
        }
    }

    private void showInfoDialog(final FileItem item) {
        Context context = getCore().getContext();
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(context.getResources().getString(R.string.alert_file_title,
                new Object[] { item.getName() }));
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_fileinfo,
                (ViewGroup) ((Activity) context).findViewById(R.id.fileinfo_dialog_layout));
        alert.setView(view);

        String path = item.getPath();
        File file = new File(path);

        String message = context.getResources().getString(
                R.string.alert_file_summary,
                new Object[] { (file.getParent() == null ? "" : file.getParent()) + "/",
                        Strings.formatSize(file.length()),
                        Strings.formatDate(file.lastModified()) });
        ((TextView)view.findViewById(R.id.fileinfo_text)).setText(message);

        CheckBox cbDelete = (CheckBox) view.findViewById(R.id.delete);
        cbDelete.setChecked(item.isDelete());
        cbDelete.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                item.setDelete(isChecked);
                redrawItems();
            }
            
        });

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
        final Context context = getCore().getContext();

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.alert_md5_title);
        alert.setMessage(R.string.alert_md5_summary);

        final EditText input = new EditText(context);
        alert.setView(input);
        input.selectAll();

        alert.setPositiveButton(R.string.alert_md5_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                final ProgressDialog pDialog = new ProgressDialog(context);
                pDialog.setIndeterminate(true);
                pDialog.setMessage(context.getResources().getString(R.string.alert_md5_loading));
                pDialog.setCancelable(false);
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.show();

                (new Thread() {

                    public void run() {

                        String path = item.getPath();
                        File file = new File(path);
                        final String md5 = Files.md5(file);

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
        Context context = getCore().getContext();
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        if (text == null || "".equals(text.trim())) {
            alert.setMessage(md5);
        } else {
            if (md5.equals(text)) {
                alert.setMessage(context.getResources().getString(R.string.alert_md5_match));
            } else {
                alert.setMessage(context.getResources().getString(R.string.alert_md5_no_match,
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

    private void addZip() {
        Preferences prefs = getCore().getPreferences();
        Context context = getCore().getContext();
        if (prefs.isUseFolder()) {
            File folder = new File(prefs.getFolder());
            if (!folder.exists() || !folder.isDirectory()) {
                Toast.makeText(context, R.string.error_folder, Toast.LENGTH_SHORT).show();
            } else {
                new FolderPicker(context,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FolderPicker picker = (FolderPicker) dialog;
                                StoragePlugin sPlugin = (StoragePlugin) getCore()
                                        .getPlugin(Core.PLUGIN_STORAGE);
                                sPlugin.addFileItemToStore(picker.getPath());
                            }
                        }, prefs.getFolder(), new String[] { "zip" },
                        null,
                        true).show();
            }
        } else {
            Intent intent = new Intent(context, RequestFileActivity.class);
            context.startActivity(intent);
        }
    }

    private void selectImage(final boolean isBoot) {
        Preferences prefs = getCore().getPreferences();
        Context context = getCore().getContext();
        File folder = new File(prefs.getFolder());
        if (!folder.exists() || !folder.isDirectory()) {
            Toast.makeText(context, R.string.error_folder, Toast.LENGTH_SHORT).show();
        } else {
            FolderPicker p = new FolderPicker(context, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FolderPicker picker = (FolderPicker) dialog;
                    installImage(picker.getPath(), isBoot);
                }
            }, prefs.getFolder(), new String[] { "img" }, null,
                    true);
            try {
                p.show();
            } catch (BadTokenException ex) {
                // ignore
            }
        }
    }

    private void getTwrpUrl() {
        final Context context = getCore().getContext();
        String board = SystemProperties.getProperty("ro.product.device");
        new URLStringReader(new URLStringReaderListener() {

            @Override
            public void onReadEnd(final String buffer) {
                try {
                    if (buffer != null && !"false".equals(buffer)) {
                        ((Activity) context).runOnUiThread(new Runnable() {

                            public void run() {
                                try {
                                    JSONObject result = (JSONObject) new JSONTokener(buffer).nextValue();
                                    new DownloadFile(getCore(),
                                            "http://goo.im" + result.getString("path"),
                                            result.getString("filename"), result.getString("md5"), new DownloadTask.OnDownloadFinishListener() {
            
                                                @Override
                                                public void onDownloadFinish(File file) {
                                                    installImage(file.getAbsolutePath(), false);
                                                }

                                                @Override
                                                public void onDownloadError(Exception ex) {
                                                    Dialog.error(context, R.string.downloading_error, null);
                                                }
                                    });

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                    } else {
                        Dialog.toast(context, R.string.picker_recovery_no_recovery_found);
                    }
                } catch (Exception ex) {
                    System.out.println(buffer);
                    ex.printStackTrace();
                }
            }

            @Override
            public void onReadError(Exception ex) {
                Dialog.toast(context, R.string.picker_recovery_error_twrp);
            }
        }).execute("http://goo.im/json2&action=recovery&ro_board=" + board);
    }

    private void installImage(final String path, final boolean isBoot) {
        final Context context = getCore().getContext();
        int titleId = -1;
        String message = "";
        if (isBoot) {
            titleId = R.string.flashboot_confirm_title;
            message = context.getResources()
                    .getString(R.string.flashboot_confirm_message, path);
        } else {
            titleId = R.string.flashrecovery_confirm_title;
            message = context.getResources()
                    .getString(R.string.flashrecovery_confirm_message, path);
        }
        Dialog.dialog(context, message, titleId, true, new OnDialogClosedListener() {

            @Override
            public void dialogOk() {
                RecoveryPlugin rPlugin = (RecoveryPlugin) getCore().getPlugin(Core.PLUGIN_RECOVERY);
                if (isBoot) {
                    rPlugin.installBoot(path);
                    Dialog.toast(context, R.string.flashboot_installed);
                } else {
                    rPlugin.installRecovery(path);
                    Dialog.toast(context, R.string.flashrecovery_installed);
                }
            }

            @Override
            public void dialogCancel() {
            }

        });
    }

    public void applyRules() {
        FileItemStore.removeItems();
        StoragePlugin sPlugin = (StoragePlugin) getCore().getPlugin(Core.PLUGIN_STORAGE);
        Preferences prefs = getCore().getPreferences();
        List<Rule> rules = prefs.getRules();
        if (rules.size() > 0) {
            File folder = new File(prefs.getFolder());
            File[] files = folder.listFiles();

            if (files == null) {
                return;
            }

            Arrays.sort(files, new Comparator<File>() {

                @Override
                public int compare(File lhs, File rhs) {
                    String name1 = lhs.getName().toLowerCase();
                    String name2 = rhs.getName().toLowerCase();
                    return name1.compareTo(name2);
                }

            });

            for (Rule rule : rules) {
                for (File file : files) {
                    String fileName = file.getName();
                    if (fileName.toLowerCase().endsWith(".zip")
                            && !file.isDirectory()
                            && rule.apply(fileName)) {

                        sPlugin.addFileItemToStore(file.getAbsolutePath());
                    }
                }
            }
        }
    }

}
