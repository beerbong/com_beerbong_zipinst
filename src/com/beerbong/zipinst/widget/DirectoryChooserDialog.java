/*
 * Copyright (C) 2013 ZipInstaller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use mContext file except in compliance with the License.
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

package com.beerbong.zipinst.widget;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.Environment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DirectoryChooserDialog {

    private Context mContext;
    private DirectoryChooserListener mDirectoryChooserListener = null;
    private TextView mTitleView;

    private String mSdcardDirectory = "";
    private boolean mCancelable = true;

    private String mCurrentDir = "";
    private List<String> mSubdirs = null;
    private ArrayAdapter<String> mListAdapter = null;

    public interface DirectoryChooserListener {

        public void onDirectoryChosen(String chosenDir);
    }

    public DirectoryChooserDialog(Context context, DirectoryChooserListener directoryChooserListener) {
        mContext = context;
        mSdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        mDirectoryChooserListener = directoryChooserListener;

        try {
            mSdcardDirectory = new File(mSdcardDirectory).getCanonicalPath();
        } catch (IOException ioe) {
        }
    }

    public void setCancelable(boolean cancelable) {
        mCancelable = cancelable;
    }

    public void chooseDirectory() {
        chooseDirectory(mSdcardDirectory);
    }

    public void chooseDirectory(String dir) {
        File dirFile = new File(dir);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            dir = mSdcardDirectory;
        }

        try {
            dir = new File(dir).getCanonicalPath();
        } catch (IOException ioe) {
            return;
        }

        mCurrentDir = dir;
        mSubdirs = getDirectories(dir);

        class DirectoryOnClickListener implements DialogInterface.OnClickListener {

            public void onClick(DialogInterface dialog, int item) {
                mCurrentDir += "/"
                        + ((AlertDialog) dialog).getListView().getAdapter().getItem(item);
                updateDirectory();
            }
        }

        AlertDialog.Builder dialogBuilder = createDirectoryChooserDialog(dir, mSubdirs,
                new DirectoryOnClickListener());

        dialogBuilder.setPositiveButton(android.R.string.ok, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mDirectoryChooserListener != null) {
                    mDirectoryChooserListener.onDirectoryChosen(mCurrentDir);
                }
            }
        }).setNegativeButton(android.R.string.cancel, null);

        final AlertDialog dirsDialog = dialogBuilder.create();

        dirsDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (mCurrentDir.equals(mSdcardDirectory)) {
                        return false;
                    } else {
                        mCurrentDir = new File(mCurrentDir).getParent();
                        updateDirectory();
                    }

                    return true;
                } else {
                    return false;
                }
            }
        });

        dirsDialog.show();
    }

    private List<String> getDirectories(String dir) {
        List<String> dirs = new ArrayList<String>();

        try {
            File dirFile = new File(dir);
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs;
            }

            for (File file : dirFile.listFiles()) {
                if (file.isDirectory()) {
                    dirs.add(file.getName());
                }
            }
        } catch (Exception e) {
        }

        Collections.sort(dirs, new Comparator<String>() {

            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });

        return dirs;
    }

    private AlertDialog.Builder createDirectoryChooserDialog(String title, List<String> listItems,
            DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);

        LinearLayout titleLayout = new LinearLayout(mContext);
        titleLayout.setOrientation(LinearLayout.VERTICAL);

        mTitleView = new TextView(mContext);
        mTitleView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        mTitleView.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
        mTitleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        mTitleView.setText(title);

        titleLayout.addView(mTitleView);

        dialogBuilder.setCustomTitle(titleLayout);

        mListAdapter = createListAdapter(listItems);

        dialogBuilder.setSingleChoiceItems(mListAdapter, -1, onClickListener);
        dialogBuilder.setCancelable(mCancelable);

        return dialogBuilder;
    }

    private void updateDirectory() {
        mSubdirs.clear();
        mSubdirs.addAll(getDirectories(mCurrentDir));
        mTitleView.setText(mCurrentDir);

        mListAdapter.notifyDataSetChanged();
    }

    private ArrayAdapter<String> createListAdapter(List<String> items) {
        return new ArrayAdapter<String>(mContext, android.R.layout.select_dialog_item,
                android.R.id.text1, items) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                    tv.setEllipsize(null);
                }
                return v;
            }
        };
    }
}