package com.beerbong.zipinst.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class Recovery {

	public static String[] getCommands(Activity mActivity, boolean[] wipeOptions) throws Exception {
		List<String> commands = new ArrayList();

		int size = StoredPreferences.size(), i = 0;

		commands.add("ui_print(\"-------------------------------------\");");
		commands.add("ui_print(\" ZipInstaller " + mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0).versionName + "\");");
		commands.add("ui_print(\"-------------------------------------\");");
		
		if (wipeOptions[0]) {
			commands.add("ui_print(\" Wiping data\");");
			commands.add("format(\"/data\");");
		 }
		 if (wipeOptions[1]) {
			 commands.add("ui_print(\" Wiping cache\");");
			 commands.add("format(\"/cache\");");
			 commands.add("ui_print(\" Wiping dalvik cache\");");
			 commands.add("__system(\"rm -r /data/dalvik-cache\");");
		 }
		
		commands.add("ui_print(\" Installing zips\");");
		for (;i<size;i++) {
			commands.add("install_zip(\"" + StoredPreferences.getPreference(i).getKey() + "\");");
		}
		
		return commands.toArray(new String[commands.size()]);
	}
	
	public static void main(String[] args) {
		int count = 5;
	}
}