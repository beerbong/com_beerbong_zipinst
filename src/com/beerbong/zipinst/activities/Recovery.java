package com.beerbong.zipinst.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.manager.Manager;
import com.beerbong.zipinst.manager.RecoveryManager;
import com.beerbong.zipinst.util.RecoveryInfo;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class Recovery extends PreferenceActivity {

    private RadioGroup mGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        OnClickListener listener = new OnClickListener() {
            public void onClick(View v) {
                
                
            }
        };

        View view = LayoutInflater.from(this).inflate(R.xml.recovery, (ViewGroup)findViewById(R.id.recovery_layout));

        RadioButton cbCwmbased = (RadioButton)view.findViewById(R.id.cwmbased);
        RadioButton cbTwrp = (RadioButton)view.findViewById(R.id.twrp);
        RadioButton cb4ext = (RadioButton)view.findViewById(R.id.fourext);
        
        mGroup = (RadioGroup)view.findViewById(R.id.recovery_radio_group);
        
        final RecoveryManager manager = Manager.getRecoveryManager();
        RecoveryInfo info = manager.getRecovery();
        switch (info.getId()) {
            case R.id.cwmbased :
                cbCwmbased.setChecked(true);
                break;
            case R.id.twrp :
                cbTwrp.setChecked(true);
                break;
            case R.id.fourext :
                cb4ext.setChecked(true);
                break;
        }

        new AlertDialog.Builder(Recovery.this)
            .setTitle(R.string.recovery_alert_title)
            .setMessage(R.string.recovery_alert_summary)
            .setView(view)
            .setPositiveButton(R.string.recovery_alert_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    
                    int id = mGroup.getCheckedRadioButtonId();
                    
                    manager.setRecovery(id);
                    
                    Recovery.this.finish();
                }
            }).setNegativeButton(R.string.recovery_alert_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Recovery.this.finish();
                }
            }).show();
    }
}