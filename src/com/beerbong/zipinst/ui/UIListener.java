package com.beerbong.zipinst.ui;

import android.content.Intent;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public interface UIListener {

	public void onPreferenceClicked(String id);
	public void onActivityResult(int requestCode, int resultCode, Intent data);
}