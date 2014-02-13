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

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.beerbong.zipinst.R;

public class RecoveryPicker extends Dialog implements OnItemClickListener {

    public static final int ZIP = 0;
    public static final int TWRP = 1;

    public interface RecoveryPickerClicked {

        public void onRecoveryPickerClicked(int id);
    }

    private ListView mList;
    private RecoveryPickerClicked mListener;

    public RecoveryPicker(Context context, RecoveryPickerClicked listener) {
        super(context);
        setTitle(R.string.picker_recovery_title);
        setContentView(R.layout.picker_recovery);

        mListener = listener;

        mList = (ListView) findViewById(R.id.list);
        mList.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mListener.onRecoveryPickerClicked(position);
        dismiss();
    }
}
