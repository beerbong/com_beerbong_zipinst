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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.preferences.Preferences;
import com.beerbong.zipinst.preferences.Rule;
import com.beerbong.zipinst.store.FileItem;
import com.beerbong.zipinst.store.FileItemsAdapter;
import com.beerbong.zipinst.store.FileItemsAdapter.FileItemsAdapterHolder;
import com.beerbong.zipinst.ui.UIFragment;
import com.mobeta.android.dslv.DragSortListView;

public class FragmentRules extends UIFragment implements OnItemClickListener,
        FileItemsAdapterHolder {

    private DragSortListView.DropListener mDropListener = new DragSortListView.DropListener() {

        public void drop(int from, int to) {
            moveRule(from, to);
            redrawRules();
        }
    };

    private DragSortListView mRuleList;
    private TextView mHelp;

    @Override
    public int[] getVisibleMenuItems() {
        return new int[] { R.id.menu_add_rule };
    }

    @Override
    public void onOptionsItemSelected(int id) {
        if (id == R.id.menu_add_rule) {
            showAddRuleDialog();
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_rules;
    }

    @Override
    public View getMainView(View rootView) {
        return rootView.findViewById(R.id.rules_layout);
    }

    @Override
    public void create(boolean isNew) {

        mRuleList = (DragSortListView) getMainView().findViewById(R.id.rules_list);
        mRuleList.setUiInterface(this);
        mRuleList.setOnItemClickListener(this);
        mRuleList.setDropListener(mDropListener);
        mRuleList.setRemoveListener(new DragSortListView.RemoveListener() {

            @Override
            public void remove(int which) {
                Preferences prefs = getCore().getPreferences();
                prefs.removeRule(which);
                redrawRules();
            }
        });

        mHelp = (TextView) getMainView().findViewById(R.id.rules_fragment_help);

        redrawRules();
    }

    @Override
    public void restore(Bundle savedInstanceState) {
    }

    @Override
    public void save(Bundle outState) {
    }

    @Override
    public int getTitle() {
        return R.string.rules_title;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    public void redrawRules() {

        Preferences prefs = getCore().getPreferences();
        List<Rule> rules = prefs.getRules();
        List<FileItem> array = new ArrayList<FileItem>();
        array.addAll(rules);

        mHelp.setVisibility(rules.size() > 0 ? View.GONE : View.VISIBLE);

        mRuleList.setAdapter(new FileItemsAdapter(getCore(), this, array));

        redraw();
    }

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

    private void moveRule(int from, int to) {
        if (from == to) {
            return;
        }
        Preferences prefs = getCore().getPreferences();
        List<Rule> rules = prefs.getRules();
        Rule toMove = rules.get(from);
        while (rules.indexOf(toMove) != to) {
            int i = rules.indexOf(toMove);
            Collections.swap(rules, i, to < from ? i - 1 : i + 1);
        }
        prefs.setRules(rules);
    }

    public void showAddRuleDialog() {

        final Preferences prefs = getCore().getPreferences();

        LayoutInflater inflater = ((Activity) getCore().getContext()).getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_rules, null);
        final Spinner spinner = (Spinner) view.findViewById(R.id.rule_rule);
        final EditText editText = (EditText) view.findViewById(R.id.rule_input);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getCore().getContext(), R.array.rule_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getCore().getContext())
                .setTitle(R.string.alert_rules_title)
                .setPositiveButton(R.string.alert_rules_add, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = editText.getText().toString();
                        if (text != null && !"".equals(text)) {
                            prefs.addRule(text, spinner.getSelectedItemPosition());
                            redrawRules();
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
