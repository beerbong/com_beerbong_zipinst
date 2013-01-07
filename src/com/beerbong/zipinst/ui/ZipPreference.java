package com.beerbong.zipinst.ui;

import com.beerbong.zipinst.R;

import android.content.Context;
import android.preference.Preference;
import android.view.View;
import android.widget.TextView;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class ZipPreference extends Preference {

    private String name;
    private String path;

    public ZipPreference(final Context context) {
        super(context);
        setLayoutResource(R.xml.order_preference);
    }
    
    protected void onBindView(final View view) {
        super.onBindView(view);

        TextView nameView = (TextView)view.findViewById(R.id.title);
        if ((nameView != null) && (name != null)) {
            nameView.setText(name);
        }
        TextView pathView = (TextView)view.findViewById(R.id.path);
        if ((pathView != null) && (path != null)) {
            pathView.setText(path);
        }
    }
    @Override
    public void setTitle(CharSequence title) {
        this.name = (String)title;
    }
    @Override
    public CharSequence getTitle() {
        return name;
    }
    @Override
    public void setSummary(CharSequence summary) {
        this.path = (String)summary;
    }
    @Override
    public CharSequence getSummary() {
        return this.path;
    }
}