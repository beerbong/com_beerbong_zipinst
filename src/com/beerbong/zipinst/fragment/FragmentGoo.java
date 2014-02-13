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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.http.DownloadFile;
import com.beerbong.zipinst.http.URLStringReader;
import com.beerbong.zipinst.http.URLStringReader.URLStringReaderListener;
import com.beerbong.zipinst.io.SystemProperties;
import com.beerbong.zipinst.preferences.Preferences;
import com.beerbong.zipinst.store.FileItem;
import com.beerbong.zipinst.store.FileItemsAdapter;
import com.beerbong.zipinst.store.FileItemsAdapter.FileItemsAdapterHolder;
import com.beerbong.zipinst.ui.UIFragment;
import com.beerbong.zipinst.ui.widget.Dialog;
import com.beerbong.zipinst.ui.widget.Dialog.OnDialogClosedListener;
import com.beerbong.zipinst.ui.widget.IndeterminateDialog;
import com.mobeta.android.dslv.DragSortListView;

public class FragmentGoo extends UIFragment implements URLStringReaderListener {

    private static final String NAVIGATE_ALL = "http://goo.im/json2&path=%s";
    private static final String NAVIGATE_COMPATIBLE = "http://goo.im/json2&path=%s&ro_board=%s";
    private static final String LOGIN_URL = "http://goo-inside.me/salt";
    private static final String ROOT = "/devs";
    private static final String PATH = "goo_path";
    private static final String FILES = "goo_files";

    private class GooItemsAdapterHolder implements FileItemsAdapterHolder {

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
            return R.layout.item_goo;
        }

    }

    private IndeterminateDialog mDialog;
    private DragSortListView mList;
    private Switch mSwitchCompatible;
    private TextView mNavigating;
    private String mDevice;
    private String mPath;
    private List<FileItem> mFiles;
    private MenuItem mLoginItem;

    @Override
    public int[] getVisibleMenuItems() {
        return new int[] {R.id.menu_login};
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        mLoginItem = menu.findItem(R.id.menu_login);
        mLoginItem.setTitle(getCore().getPreferences().isLogged() ? R.string.menu_logout : R.string.menu_login);
    }

    @Override
    public void onOptionsItemSelected(int id) {
        if (id == R.id.menu_login) {
            Preferences prefs = getCore().getPreferences();
            if (prefs.isLogged()) {
                prefs.logout();
                Dialog.toast(getCore().getContext(), R.string.logged_out);
                mLoginItem.setTitle(R.string.menu_login);
            } else {
                showLoginDialog();
            }
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_goo;
    }

    @Override
    public View getMainView(View rootView) {
        return rootView.findViewById(R.id.goo_layout);
    }

    @Override
    public void create(boolean isNew) {

        mDevice = SystemProperties.getProperty("ro.product.device");

        View mainView = getMainView();

        mList = (DragSortListView) mainView.findViewById(R.id.file_list);
        mSwitchCompatible = (Switch) mainView.findViewById(R.id.switch_compatible);
        mNavigating = (TextView) mainView.findViewById(R.id.navigating_folder);

        mSwitchCompatible.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPath = ROOT;
                browse();
            }

        });

        mList.setUiInterface(this);
        mList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final FileItem item = mFiles.get(position);
                if (item.getKey() == null) {
                    mPath = item.getPath();
                    browse();
                } else {
                    String message = getCore()
                            .getContext()
                            .getResources()
                            .getString(R.string.goo_download_summary,
                                    new Object[] { item.getName() });
                    Dialog.dialog(getCore().getContext(), message, R.string.goo_download_title,
                            true, R.string.new_version_download, android.R.string.cancel,
                            new OnDialogClosedListener() {

                                @Override
                                public void dialogOk() {
                                    new DownloadFile(getCore(), item.getPath(), item.getName(),
                                            item.getMd5());
                                }

                                @Override
                                public void dialogCancel() {
                                }

                            });
                }
            }

        });

        if (isNew) {
            mPath = ROOT;
            mNavigating.setText(getCore().getContext().getResources()
                    .getString(R.string.navigating_folder, mPath));
            browse();
        }
    }

    @Override
    public void restore(Bundle savedInstanceState) {
        mFiles = (List<FileItem>) savedInstanceState.getSerializable(FILES);
        mPath = savedInstanceState.getString(PATH);
        redrawFiles();
    }

    @Override
    public void save(Bundle outState) {
        outState.putString(PATH, mPath);
        outState.putSerializable(FILES, (Serializable) mFiles);
    }

    @Override
    public int getTitle() {
        return R.string.goo_title;
    }

    @Override
    public void onReadEnd(String buffer) {

        mFiles = new ArrayList<FileItem>();

        try {

            JSONObject object = (JSONObject) new JSONTokener(buffer).nextValue();
            JSONArray list = object.getJSONArray("list");

            if (!mPath.equals(ROOT)) {
                String path = mPath.substring(0, mPath.lastIndexOf("/"));
                FileItem item = new FileItem(null, getCore().getContext().getResources()
                        .getString(R.string.up_a_level), path, false);
                item.setImageAttr(R.attr.folderIcon);
                mFiles.add(item);
            }

            for (int i = 0; i < list.length(); i++) {

                JSONObject result = list.getJSONObject(i);
                String fileName = result.optString("filename");

                if (fileName != null && !"".equals(fileName.trim())) {

                    if (mSwitchCompatible.isChecked()
                            && !mDevice.equals(result.optString("ro_board"))) {
                        continue;
                    }

                    String path = result.optString("path");

                    FileItem item = new FileItem(fileName, fileName, "http://goo.im" + path, false);
                    item.setMd5(result.optString("md5"));
                    item.setSize(result.optLong("filesize"));
                    item.setImageAttr(R.attr.fileIcon);
                    mFiles.add(item);

                } else {

                    String folder = result.optString("folder");
                    String folderName = folder.substring(folder.lastIndexOf("/") + 1);

                    FileItem item = new FileItem(null, folderName, folder, false);
                    item.setImageAttr(R.attr.folderIcon);
                    mFiles.add(item);
                }
            }
            redrawFiles();
        } catch (Exception ex) {
            ex.printStackTrace();
            Dialog.toast(getCore().getContext(), R.string.goo_browse_error);
        }
        mDialog.dismiss();
    }

    @Override
    public void onReadError(Exception ex) {
        mDialog.dismiss();
        ex.printStackTrace();
        Dialog.toast(getCore().getContext(), R.string.goo_browse_error);
    }

    private void redrawFiles() {
        mNavigating.setText(getCore().getContext().getResources()
                .getString(R.string.navigating_folder, mPath));

        mList.setAdapter(new FileItemsAdapter(getCore(), new GooItemsAdapterHolder(), mFiles));
    }

    private void browse() {
        String folder = mSwitchCompatible.isChecked() ? String.format(NAVIGATE_COMPATIBLE, mPath,
                mDevice) : String.format(NAVIGATE_ALL, mPath);
        new URLStringReader(this).execute(folder);
        mDialog = new IndeterminateDialog(getCore().getContext(), R.string.goo_browse_searching,
                null);
    }

    public void showLoginDialog() {

        final Preferences prefs = getCore().getPreferences();

        LayoutInflater inflater = ((Activity) getCore().getContext()).getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_login, null);
        final EditText username = (EditText) view.findViewById(R.id.username);
        final EditText password = (EditText) view.findViewById(R.id.password);

        username.setText(prefs.getLoginUserName());

        AlertDialog.Builder builder = new AlertDialog.Builder(getCore().getContext())
                .setTitle(R.string.goo_login)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        String user = username.getText() == null ? "" : username.getText()
                                .toString();
                        String pass = password.getText() == null ? "" : password.getText()
                                .toString();

                        prefs.setLoginUserName(user);

                        try {
                            mDialog = new IndeterminateDialog(getCore().getContext(), R.string.goo_loging,
                                    null);
                            String path = LOGIN_URL + "&username="
                                    + URLEncoder.encode(user, "UTF-8") + "&password="
                                    + URLEncoder.encode(pass, "UTF-8");
                            new URLStringReader(new URLStringReaderListener() {

                                @Override
                                public void onReadEnd(String buffer) {
                                    mDialog.dismiss();
                                    if (buffer != null && buffer.length() == 32) {
                                        prefs.login(buffer);
                                        Dialog.toast(getCore().getContext(), R.string.logged_in);
                                        mLoginItem.setTitle(R.string.menu_logout);
                                    } else if (buffer != null) {
                                        Dialog.toast(getCore().getContext(), R.string.logged_invalid);
                                    } else {
                                        Dialog.toast(getCore().getContext(), R.string.logged_down);
                                    }
                                }

                                @Override
                                public void onReadError(Exception ex) {
                                    mDialog.dismiss();
                                    ex.printStackTrace();
                                    Dialog.toast(getCore().getContext(), R.string.logged_error);
                                }
                            }).execute(path);
                        } catch (UnsupportedEncodingException ex) {
                            // should never get here
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
        builder.setView(view);
        builder.create().show();
    }

}
