package com.beerbong.zipinst.manager;

import java.io.DataOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.ui.UIAdapter;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.StoredPreferences;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class RebootManager extends UIAdapter {

    private Activity mActivity;

    protected RebootManager(Activity mActivity) {
        this.mActivity = mActivity;

        UI.getInstance().addUIListener(this);
    }

    public void onPreferenceClicked(String id) {

        if (Constants.PREFERENCE_INSTALL_NOW.equals(id)) {
            showRebootDialog();
        }
    }
    public void showBackupDialog() {
        showBackupDialog(null);
    }
    
    private void showBackupDialog(final boolean[] wipeOptions) {
        
        UI.getInstance().removeAllPreferences();
        
        AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
        alert.setTitle(R.string.alert_backup_title);
        alert.setMessage(R.string.alert_backup_message);
        
        final EditText input = new EditText(mActivity);
        alert.setView(input);
        input.setText(Constants.getDateAndTime());
        input.selectAll();
        
        alert.setPositiveButton(R.string.alert_backup_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                reboot(wipeOptions, input.getText().toString());
            }
        });
      
        alert.setNegativeButton(R.string.alert_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
    private void showRebootDialog() {
        
        if (StoredPreferences.size() == 0) return;

        AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
        alert.setTitle(R.string.alert_reboot_title);
      
        String[] wipeOpts = mActivity.getResources().getStringArray(R.array.wipe_options);
        final boolean[] wipeOptions = new boolean[wipeOpts.length];
      
        alert.setMultiChoiceItems(wipeOpts, wipeOptions, new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                wipeOptions[which] = isChecked;
            }
        });

        alert.setPositiveButton(R.string.alert_reboot_now, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                
                if (wipeOptions[0]) {
                    showBackupDialog(wipeOptions);
                } else {
                    reboot(wipeOptions, null);
                }

            }
        });
      
        alert.setNegativeButton(R.string.alert_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
    
    private void reboot(boolean[] wipeOptions, String backupFolder) {
        try {
            
            RecoveryManager manager = Manager.getRecoveryManager();
            
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            
            os.writeBytes("rm -f /cache/recovery/command\n");
            os.writeBytes("rm -f /cache/recovery/extendedcommand\n");
            os.writeBytes("rm -f /cache/recovery/openrecoveryscript\n");
            
            String file = manager.getCommandsFile();

            String[] commands = manager.getCommands(wipeOptions, backupFolder);
            if (commands != null) {
                int size = commands.length, i = 0;
                for (;i<size;i++) {
                    os.writeBytes("echo '" + commands[i] + "' >> /cache/recovery/" + file + "\n");
                }
            }

            os.writeBytes("reboot recovery\n");

            os.writeBytes("sync\n");
            os.writeBytes("exit\n");
            os.flush();
            p.waitFor();

            Runtime.getRuntime().exec("/system/bin/reboot recovery");
                
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}