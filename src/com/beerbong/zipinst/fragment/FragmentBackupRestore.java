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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.cloud.CloudEntry;
import com.beerbong.zipinst.cloud.CloudStorage;
import com.beerbong.zipinst.cloud.CloudStorage.CloudStorageListener;
import com.beerbong.zipinst.core.Core;
import com.beerbong.zipinst.core.plugins.reboot.RebootPlugin;
import com.beerbong.zipinst.core.plugins.recovery.RecoveryPlugin;
import com.beerbong.zipinst.core.plugins.superuser.CommandResult;
import com.beerbong.zipinst.core.plugins.superuser.SuperUserPlugin;
import com.beerbong.zipinst.io.Files;
import com.beerbong.zipinst.onandroid.ONandroid.ONandroidFinishListener;
import com.beerbong.zipinst.preferences.Preferences;
import com.beerbong.zipinst.store.FileItem;
import com.beerbong.zipinst.store.FileItemsAdapter;
import com.beerbong.zipinst.store.FileItemsAdapter.FileItemsAdapterHolder;
import com.beerbong.zipinst.ui.UIFragment;
import com.beerbong.zipinst.ui.widget.CloudPicker;
import com.beerbong.zipinst.ui.widget.Dialog;
import com.beerbong.zipinst.ui.widget.Dialog.OnDialogClosedListener;
import com.beerbong.zipinst.ui.widget.IndeterminateDialog;
import com.beerbong.zipinst.ui.widget.IndeterminateDialog.IndeterminateDialogCallback;
import com.mobeta.android.dslv.DragSortItemView;
import com.mobeta.android.dslv.DragSortListView;

public class FragmentBackupRestore extends UIFragment implements ONandroidFinishListener {

    private static final String STORAGE_BACKUPS = "STORAGE_BACKUPS";
    private static final String CLOUD_BACKUPS = "CLOUD_BACKUPS";

    private class BackupItemsAdapterHolder implements FileItemsAdapterHolder {

        @Override
        public boolean useDragAndDrop() {
            return true;
        }

        @Override
        public boolean canRemove() {
            return true;
        }

        @Override
        public boolean showPath() {
            return false;
        }

        @Override
        public boolean showSize() {
            return true;
        }

        @Override
        public boolean showDate() {
            return false;
        }

        @Override
        public int getItemLayoutId() {
            return R.layout.item_backup;
        }

    }

    private Switch mSwitchOnandroid;

    private String mBackupFolder;

    private DragSortListView mStorageList;
    private ProgressBar mStorageProgress;
    private TextView mStorageEmpty;
    private List<FileItem> mStorageBackups;

    private ActionMode mActionMode;
    private ActionMode.Callback mActionStorageCallback;
    private ActionMode.Callback mActionCloudCallback;
    private DragSortItemView mSelectedView;
    private int mSelectedPosition;

    private CloudStorage mCloudStorage;

    private LinearLayout mCloudLayout;
    private DragSortListView mCloudList;
    private ProgressBar mCloudProgress;
    private TextView mCloudTitle;
    private TextView mCloudEmpty;
    private List<FileItem> mCloudBackups;
    private boolean mWaitingLogin;

    @Override
    public int[] getVisibleMenuItems() {
        return new int[] {
                R.id.menu_backup,
                R.id.menu_cloud,
                R.id.menu_refresh
        };
    }

    @Override
    public void onOptionsItemSelected(int id) {
        switch (id) {
            case R.id.menu_backup :
                Preferences prefs = getCore().getPreferences();
                RebootPlugin rPlugin = (RebootPlugin) getCore().getPlugin(Core.PLUGIN_REBOOT);
                if (prefs.isUseONandroid()) {
                    if (!getCore().getONandroid().isConfigured()) {
                        getCore().getONandroid().startWizard();
                    } else {
                        rPlugin.showBackupDialog();
                    }
                } else {
                    rPlugin.showBackupDialog();
                }
                break;
            case R.id.menu_refresh :
                refreshStorageBackups();
                if (mCloudStorage != null) {
                    mCloudStorage.refresh();
                }
                break;
            case R.id.menu_cloud :
                new CloudPicker(getCore().getContext(), new CloudPicker.CloudPickerListener() {

                    @Override
                    public void cloudStorageSelected(int id) {
                        mWaitingLogin = true;
                        getCore().getPreferences().setCloudStorage(id);
                        prepareCloudStorage();
                    }
                }).show();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWaitingLogin && mCloudStorage != null) {
            mCloudStorage.refresh();
        }
        mWaitingLogin = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mCloudStorage != null) {
            mCloudStorage.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_backuprestore;
    }

    @Override
    public View getMainView(View rootView) {
        return rootView.findViewById(R.id.backuprestore_layout);
    }

    @Override
    public void create(boolean isNew) {

        View mainView = getMainView();

        final Preferences prefs = getCore().getPreferences();

        getCore().getONandroid().setONandroidFinishListener(this);

        mActionStorageCallback = new ActionMode.Callback() {

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.storage_backups, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                boolean ret = false;
                final String backup = mStorageBackups.get(mSelectedPosition).getName();
                switch (item.getItemId()) {
                    case R.id.menu_restore:
                        RebootPlugin rPlugin = (RebootPlugin) getCore().getPlugin(Core.PLUGIN_REBOOT);
                        rPlugin.showRestoreDialog(backup);
                        mode.finish();
                        break;
                    case R.id.menu_delete:
                        final Context context = getCore().getContext();
                        String message = context.getResources().getString(R.string.delete_backup_confirm,
                                backup);
                        Dialog.dialog(context, message, R.string.alert_delete_title, true,
                                new OnDialogClosedListener() {

                                    @Override
                                    public void dialogOk() {
                                        ((Activity) context).runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                final String toDelete = mBackupFolder + backup;
                
                                                new IndeterminateDialog(context, context.getResources().getString(
                                                        R.string.alert_deleting_folder, new Object[] {
                                                            backup
                                                        }), new IndeterminateDialogCallback() {
                
                                                    @Override
                                                    public void executeIndeterminate() {
                                                        boolean deleted = Files.recursiveDelete(new File(toDelete));
                                                        if (!deleted) {
                                                            SuperUserPlugin sPlugin = (SuperUserPlugin) getCore()
                                                                    .getPlugin(
                                                                            Core.PLUGIN_SUPERUSER);
                                                            sPlugin.run("rm -r " + toDelete);
                                                        }
                                                    }
                
                                                    @Override
                                                    public void finishedIndeterminate() {
                                                        mStorageBackups.remove(mSelectedPosition);
                                                        redrawStorageBackups();
                                                    }
                
                                                });
                                            }
                                        });
                                    }

                                    @Override
                                    public void dialogCancel() {
                                        redrawStorageBackups();
                                    }
                                });
                        mode.finish();
                        ret = true;
                        break;
                    case R.id.menu_sync:
                        FileItem it = mStorageBackups.get(mSelectedPosition);
                        SuperUserPlugin suPlugin = (SuperUserPlugin) getCore().getPlugin(Core.PLUGIN_SUPERUSER);
                        suPlugin.run("chmod -R 777 " + it.getPath());
                        mCloudStorage.zipIt(it.getPath());
                        mode.finish();
                        break;
                }
                if (mSelectedView != null) {
                    mSelectedView.setListSelected(false);
                }
                mSelectedView = null;
                return ret;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                if (mSelectedView != null) {
                    mSelectedView.setListSelected(false);
                }
                mSelectedView = null;
                mActionMode = null;
            }
        };

        mActionCloudCallback = new ActionMode.Callback() {

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.cloud_backups, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                boolean ret = false;
                final CloudEntry entry = (CloudEntry) mCloudBackups.get(mSelectedPosition);
                switch (item.getItemId()) {
                    case R.id.menu_delete:
                        final Context context = getCore().getContext();
                        String message = context.getResources().getString(R.string.delete_backup_confirm,
                                entry.getName());
                        Dialog.dialog(context, message, R.string.alert_delete_title, true,
                                new OnDialogClosedListener() {

                                    @Override
                                    public void dialogOk() {
                                        mCloudStorage.remove(entry);
                                    }

                                    @Override
                                    public void dialogCancel() {
                                        redrawCloudBackups();
                                    }
                                });
                        mode.finish();
                        ret = true;
                        break;
                    case R.id.menu_sync:
                        CloudEntry it = (CloudEntry) mCloudBackups.get(mSelectedPosition);
                        mCloudStorage.download(it);
                        mode.finish();
                        break;
                }
                if (mSelectedView != null) {
                    mSelectedView.setListSelected(false);
                }
                mSelectedView = null;
                return ret;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                if (mSelectedView != null) {
                    mSelectedView.setListSelected(false);
                }
                mSelectedView = null;
                mActionMode = null;
            }
        };

        mSwitchOnandroid = (Switch) mainView.findViewById(R.id.switch_onandroid);
        mSwitchOnandroid.setChecked(prefs.isUseONandroid());
        mSwitchOnandroid.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.setUseONandroid(isChecked);
                if (isChecked) {
                    getCore().getONandroid().startWizard();
                }
            }

        });

        RecoveryPlugin rPlugin = (RecoveryPlugin) getCore().getPlugin(Core.PLUGIN_RECOVERY);
        mBackupFolder = rPlugin.getBackupDir(true);

        Resources res = getCore().getContext().getResources();

        mStorageList = (DragSortListView) mainView.findViewById(R.id.storage_backups_list);
        mStorageList.setUiInterface(this);
        mStorageList.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode != null) {
                    return false;
                }

                mActionMode = getActivity().startActionMode(mActionStorageCallback);
                mSelectedView = (DragSortItemView) view;
                mSelectedView.setListSelected(true);
                mSelectedPosition = position;
                return true;
            }
        });
        mStorageProgress = (ProgressBar) mainView.findViewById(R.id.storage_backups_progress);
        mStorageEmpty = (TextView) mainView.findViewById(R.id.backups_storage_empty);

        mStorageEmpty.setText(res.getString(R.string.backups_folder_empty, mBackupFolder));

        mCloudLayout = (LinearLayout) mainView.findViewById(R.id.cloud_layout);

        mCloudList = (DragSortListView) mainView.findViewById(R.id.cloud_backups_list);
        mCloudList.setUiInterface(this);
        mCloudList.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode != null) {
                    return false;
                }

                mActionMode = getActivity().startActionMode(mActionCloudCallback);
                mSelectedView = (DragSortItemView) view;
                mSelectedView.setListSelected(true);
                mSelectedPosition = position;
                return true;
            }
        });
        mCloudProgress = (ProgressBar) mainView.findViewById(R.id.cloud_backups_progress);
        mCloudTitle = (TextView) mainView.findViewById(R.id.title_cloud);
        mCloudEmpty = (TextView) mainView.findViewById(R.id.backups_cloud_empty);

        prepareCloudStorage();

        if (isNew) {
            refreshStorageBackups();
            if (mCloudStorage != null) {
                mCloudStorage.refresh();
            }
        }
    }

    @Override
    public void restore(Bundle savedInstanceState) {
        mStorageBackups = (List<FileItem>) savedInstanceState.getSerializable(STORAGE_BACKUPS);
        redrawStorageBackups();
        mCloudBackups = (List<FileItem>) savedInstanceState.getSerializable(CLOUD_BACKUPS);
        redrawCloudBackups();
    }

    @Override
    public void save(Bundle outState) {
        outState.putSerializable(STORAGE_BACKUPS, (Serializable) mStorageBackups);
        outState.putSerializable(CLOUD_BACKUPS, (Serializable) mCloudBackups);
    }

    @Override
    public int getTitle() {
        return R.string.backuprestore_title;
    }

    @Override
    public void oNandroidFinished() {
        refreshStorageBackups();
    }

    private void prepareCloudStorage() {
        mCloudStorage = getCore().getCloudStorage();
        if (mCloudStorage == null) {
            mCloudLayout.setVisibility(View.GONE);
        } else {

            Resources res = getCore().getContext().getResources();

            mCloudLayout.setVisibility(View.VISIBLE);

            mCloudTitle.setText(res.getString(R.string.backups_in_cloud, res.getString(mCloudStorage.getName())));
            mCloudEmpty.setText(res.getString(R.string.backups_cloud_empty, res.getString(mCloudStorage.getName())));

            mCloudStorage.setCloudStorageListener(new CloudStorageListener() {

                @Override
                public void cloudStorageLoading() {
                    mCloudProgress.setVisibility(View.VISIBLE);
                    mCloudList.setVisibility(View.GONE);
                    mCloudEmpty.setVisibility(View.GONE);
                }

                @Override
                public void cloudStorageLoaded() {

                    List<CloudEntry> entries = (List<CloudEntry>) mCloudStorage.getEntries();
                    mCloudBackups = new ArrayList<FileItem>();
                    if (entries != null) {
                        mCloudBackups.addAll(entries);
                    }

                    redrawCloudBackups();
                }

                @Override
                public void cloudDownloadComplete() {
                    refreshStorageBackups();
                }

            });
        }
        
    }

    private void refreshStorageBackups() {

        mSelectedView = null;
        mSelectedPosition = -1;

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                Core core = getCore();

                ((Activity) core.getContext()).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mStorageProgress.setVisibility(View.VISIBLE);
                        mStorageList.setVisibility(View.GONE);
                        mStorageEmpty.setVisibility(View.GONE);
                    }

                });

                RecoveryPlugin rPlugin = (RecoveryPlugin) core.getPlugin(Core.PLUGIN_RECOVERY);
                SuperUserPlugin suPlugin = (SuperUserPlugin) core.getPlugin(Core.PLUGIN_SUPERUSER);

                String[] backups = rPlugin.getBackupList();
                mStorageBackups = new ArrayList<FileItem>();

                if (backups != null) {
                    for (String backup : backups) {
                        CommandResult cmd = suPlugin.run("ls -lR " + mBackupFolder + backup);
                        long size = 0;
                        if (cmd.success()) {
                            try {
                                String s = cmd.getOutString();
                                String[] lines = s.split("\n");
                                for (String line : lines) {
                                    while (line.indexOf("  ") >= 0) {
                                        line = line.replace("  ", " ");
                                    }
                                    String[] split2 = line.split(" ");
                                    if (split2.length < 3) {
                                        continue;
                                    }
                                    try {
                                        size += Long.parseLong(split2[3]);
                                    } catch (NumberFormatException ex) {
                                        // ignore
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        FileItem item = new FileItem(backup, backup, mBackupFolder + backup, false);
                        item.setSize(size);
                        item.setImageAttr(R.attr.sdcardIcon);
                        mStorageBackups.add(item);
                    }
                }
                Collections.sort(mStorageBackups, new Comparator<FileItem>() {

                    @Override
                    public int compare(FileItem lhs, FileItem rhs) {
                        String name1 = lhs.getName();
                        String name2 = rhs.getName();
                        return name2.compareTo(name1);
                    }
                });

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                redrawStorageBackups();
            }

        }.execute((Void) null);
    }

    private void redrawStorageBackups() {

        mStorageProgress.setVisibility(View.GONE);
        mStorageList.setVisibility(View.VISIBLE);
        mStorageEmpty.setVisibility(mStorageBackups.size() > 0 ? View.GONE : View.VISIBLE);

        Core core = getCore();

        mStorageList.setAdapter(new FileItemsAdapter(core, new BackupItemsAdapterHolder(),
                mStorageBackups));

        redraw();
    }

    private void redrawCloudBackups() {

        if (mCloudStorage == null) {
            return;
        }

        mCloudProgress.setVisibility(View.GONE);
        mCloudList.setVisibility(View.VISIBLE);
        mCloudEmpty.setVisibility(mCloudBackups.size() > 0 ? View.GONE : View.VISIBLE);

        Core core = getCore();

        mCloudList.setAdapter(new FileItemsAdapter(core, new BackupItemsAdapterHolder(), mCloudBackups));

        redraw();
    }

}
