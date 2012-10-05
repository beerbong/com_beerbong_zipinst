package com.beerbong.zipinst.manager;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.ui.UIListener;
import com.beerbong.zipinst.util.Constants;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class FileManager implements UIListener {
	
	private SharedPreferences settings;
	private Activity mActivity;
	
	protected FileManager(Activity activity) {
		mActivity = activity;
		settings = mActivity.getSharedPreferences(Constants.PREFS_NAME, 0);
		
		UI.getInstance().addUIListener(this);
	}

	public void onPreferenceClicked(String id) {
		if (Constants.PREFERENCE_CHOOSE_ZIP.equals(id)) {
			PackageManager packageManager = mActivity.getPackageManager();
			Intent test = new Intent(Intent.ACTION_GET_CONTENT);
			test.setType("file/*");
			List<ResolveInfo> list = packageManager.queryIntentActivities(test, PackageManager.GET_ACTIVITIES);
			if(list.size() > 0) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
				intent.setType("file/*");
				mActivity.startActivityForResult(intent, Constants.REQUEST_PICK_ZIP);
			} else {
				//No app installed to handle the intent - file explorer required
				Toast.makeText(mActivity, R.string.install_file_manager_error, Toast.LENGTH_SHORT).show();
			}
		}
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.REQUEST_PICK_ZIP) {
			if (data == null) {
				//Nothing returned by user, probably pressed back button in file manager
				return;
			}

			String zipPath = data.getData().getEncodedPath();
			
			if (zipPath.startsWith("/extSdCard") || zipPath.startsWith("/storage/sdcard1")) {
				// external sdcard not allowed
				Toast.makeText(mActivity, R.string.install_file_manager_intsdcard, Toast.LENGTH_SHORT).show();
				return;
			}
			
			if (!zipPath.endsWith(".zip")) {
				// external sdcard not allowed
				Toast.makeText(mActivity, R.string.install_file_manager_zip, Toast.LENGTH_SHORT).show();
				return;
			}
			
			String sdcardPath = new String(zipPath);
			
			String internalStorage = settings.getString(Constants.PROPERTY_INTERNAL_STORAGE, Constants.DEFAULT_INTERNAL_STORAGE);
			
			if (zipPath.startsWith("/sdcard")) zipPath = zipPath.replace("/sdcard", "/" + internalStorage);
			else if (zipPath.startsWith("/storage/sdcard0")) zipPath = zipPath.replace("/storage/sdcard0", "/" + internalStorage);

			UI.getInstance().addPreference(zipPath, sdcardPath);
			
		}
	}
	public void onCreateOptionsMenu(Menu menu) {
	}
	public void onOptionsItemSelected(MenuItem item) {
	}
}