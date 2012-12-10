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

    public ZipPreference(final Context context) {
        super(context);
    }
    
    protected void onBindView(final View view) {
        super.onBindView(view);
        
        TextView nameView = (TextView)view.findViewById(R.id.title);
        if ((nameView != null) && (name != null)) {
            nameView.setText(name);
        }
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}