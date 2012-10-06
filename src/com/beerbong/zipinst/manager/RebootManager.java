package com.beerbong.zipinst.manager;

import java.io.DataOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.PowerManager;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.ui.UIAdapter;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.Recovery;
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
      		try {
         		 Process p = Runtime.getRuntime().exec("su");
         		 DataOutputStream os = new DataOutputStream(p.getOutputStream());
         		 os.writeBytes("rm -f /cache/recovery/command\n");
         		 os.writeBytes("rm -f /cache/recovery/extendedcommand\n");
         		 os.writeBytes("rm -f /cache/recovery/openrecoveryscript\n");

         		 String[] commands = Recovery.getCWMCommands(mActivity, wipeOptions);
         		 int size = commands.length, i = 0;
         		 for (;i<size;i++) {
         			 os.writeBytes("echo '" + commands[i] + "' >> /cache/recovery/extendedcommand\n");
         		 }

         		 commands = Recovery.getTWRPCommands(mActivity, wipeOptions);
         		 size = commands.length;
         		 i = 0;
         		 for (;i<size;i++) {
         			 os.writeBytes("echo '" + commands[i] + "' >> /cache/recovery/openrecoveryscript\n");
         		 }

         		 os.writeBytes("reboot recovery\n");

         		 os.writeBytes("sync\n");
         		 os.writeBytes("exit\n");
         		 os.flush();
         		 p.waitFor();

         		 ((PowerManager)mActivity.getSystemService(Context.POWER_SERVICE)).reboot("recovery");
      		} catch (Exception e) {
      			e.printStackTrace();
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
}