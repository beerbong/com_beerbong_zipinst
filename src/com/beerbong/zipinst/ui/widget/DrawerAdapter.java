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

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beerbong.zipinst.R;

public class DrawerAdapter extends ArrayAdapter<DrawerItem> {

    public DrawerAdapter(Context context, List<DrawerItem> items) {
        super(context, R.layout.drawer_list_item, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout itemView;
        DrawerItem item = getItem(position);

        if (convertView == null) {
            itemView = new LinearLayout(getContext());
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            vi.inflate(R.layout.drawer_list_item, itemView, true);
        } else {
            itemView = (LinearLayout) convertView;
        }
        TextView text = (TextView) itemView.findViewById(R.id.text);
        ImageView icon = (ImageView) itemView.findViewById(R.id.icon);

        text.setText(item.getText());
        if (item.isChecked()) {
            if (item.getSelectedIcon() != null) {
                icon.setImageDrawable(item.getSelectedIcon());
            } else if (item.getIcon() != null) {
                icon.setImageDrawable(item.getIcon());
            }
        } else {
            if (item.getIcon() != null) {
                icon.setImageDrawable(item.getIcon());
            }
        }

        return itemView;
    }

}
