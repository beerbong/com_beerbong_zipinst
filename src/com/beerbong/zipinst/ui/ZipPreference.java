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
    private boolean reorder;

    public ZipPreference(final Context context, boolean reorder) {
        super(context);
        this.reorder = reorder;
        
        if (reorder) {
            setLayoutResource(R.xml.order_preference);
        }
    }
    
    protected void onBindView(final View view) {
        super.onBindView(view);

        if (!reorder) return;
        
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
        if (reorder) {
            this.name = (String)title;
        } else {
            super.setTitle(title);
        }
    }
    @Override
    public CharSequence getTitle() {
        if (reorder) {
            return name;
        } else {
            return super.getTitle();
        }
    }
    @Override
    public void setSummary(CharSequence summary) {
        if (reorder) {
            this.path = (String)summary;
        } else {
            super.setSummary(summary);
        }
    }
    @Override
    public CharSequence getSummary() {
        if (reorder) {
            return this.path;
        } else {
            return super.getSummary();
        }
    }
}