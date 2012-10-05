package com.beerbong.zipinst.manager;

import android.app.Activity;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class Manager {

	public static void start(Activity mActivity) {
		
		new FileManager(mActivity);
		new RebootManager(mActivity);
		new MenuManager(mActivity);
	}
}
