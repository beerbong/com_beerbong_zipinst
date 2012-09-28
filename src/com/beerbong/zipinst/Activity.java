package com.beerbong.zipinst;

import java.io.DataOutputStream;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class Activity extends PreferenceActivity {
	
	private static final String DONATE_URL = "http://forum.xda-developers.com/donatetome.php?u=1806623";
	private static final int REQUEST_PICK_ZIP = 203;
	
	private Preference mChooseZip;
	private Context mContext;
	private String zipPath = null;

	@SuppressWarnings("deprecation")
	@Override
   public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = getApplicationContext();
		
      addPreferencesFromResource(R.xml.main);
      
      mChooseZip = findPreference("choose_zip");
	}
	
	@Override
   public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		if (preference == mChooseZip) {
			PackageManager packageManager = mContext.getPackageManager();
			Intent test = new Intent(Intent.ACTION_GET_CONTENT);
			test.setType("file/*");
			List<ResolveInfo> list = packageManager.queryIntentActivities(test, PackageManager.GET_ACTIVITIES);
			if(list.size() > 0) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
				intent.setType("file/*");
				startActivityForResult(intent, REQUEST_PICK_ZIP);
			} else {
				//No app installed to handle the intent - file explorer required
				Toast.makeText(mContext, R.string.install_file_manager_error, Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return false;
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_PICK_ZIP) {
			if (data == null) {
				//Nothing returned by user, probably pressed back button in file manager
				return;
			}

			zipPath = data.getData().getEncodedPath();
			
			if (zipPath.indexOf("extSdCard") >= 0 || zipPath.indexOf("/sdcard") < 0) {
				zipPath = null;
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
		      alert.setTitle(R.string.alert_sdcard_title);
		      alert.setMessage(R.string.alert_sdcard_message);
		      alert.setCancelable(false);
		      alert.setPositiveButton(R.string.alert_sdcard_ok, new DialogInterface.OnClickListener() {
		          public void onClick(DialogInterface dialog, int whichButton) {
		         	 dialog.dismiss();
		          }
		      });
		      alert.show();
		      return;
			}
			
			zipPath = zipPath.replace("storage/sdcard0", "sdcard");

			showRebootDialog();
		}
	}

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.menu, menu);
       return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
       Intent i;
       switch (item.getItemId()) {
       case R.id.about:
           i = new Intent(this, About.class);
           startActivity(i);
           break;
       case R.id.donate:
           i = new Intent(Intent.ACTION_VIEW, Uri.parse(DONATE_URL));
           startActivity(i);
           break;
       case R.id.exit:
           finish();
           break;
       }
       return true;
   }

	private void showRebootDialog() {
		
		if (zipPath == null) return;
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
      alert.setTitle(getString(R.string.alert_reboot_title, zipPath));
//      alert.setMessage(getString(R.string.alert_reboot_message, zipPath));
      
      String[] wipeOpts = mContext.getResources().getStringArray(R.array.wipe_options);
      final boolean[] selectedOpts = new boolean[wipeOpts.length];
      
      alert.setMultiChoiceItems(wipeOpts, selectedOpts, new DialogInterface.OnMultiChoiceClickListener() {
         public void onClick(DialogInterface dialog, int which, boolean isChecked) {
             selectedOpts[which] = isChecked;
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

         		 if (selectedOpts[0]) {
         			 os.writeBytes("echo '--wipe_data' >> /cache/recovery/command\n");
         		 }
         		 if (selectedOpts[1]) {
         			 os.writeBytes("echo '--wipe_cache' >> /cache/recovery/command\n");
         		 }
     	
         		 os.writeBytes("echo '--update_package=" + zipPath + "' >> /cache/recovery/command\n");
     	
         		 os.writeBytes("reboot recovery\n");
     	
         		 os.writeBytes("sync\n");
         		 os.writeBytes("exit\n");
         		 os.flush();
         		 p.waitFor();
     	         
         		 ((PowerManager)mContext.getSystemService(POWER_SERVICE)).reboot("recovery");
         	 } catch (Exception e) {
         		 e.printStackTrace();
         	 }
          }
      });
      
      alert.setNegativeButton(R.string.alert_cancel, new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int which) {
             dialog.dismiss();
             zipPath = null;
         }
     });
     alert.show();
	}
}