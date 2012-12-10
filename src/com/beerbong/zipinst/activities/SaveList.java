package com.beerbong.zipinst.activities;

import com.beerbong.zipinst.manager.Manager;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class SaveList extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Manager.getFileManager().saveList();
        
        this.finish();
    }
}