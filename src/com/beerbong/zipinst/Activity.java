package com.beerbong.zipinst;

import com.beerbong.zipinst.manager.Manager;
import com.beerbong.zipinst.ui.UI;

import android.content.Intent;
import android.os.Bundle;
import android.preference.*;

import android.view.*;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class Activity extends PreferenceActivity {
	
	@Override
   public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
      UI.create(this);

      Manager.start(this);
	}
	
	@Override
   public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		
		return UI.getInstance().onPreferenceTreeClick(preference);
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		UI.getInstance().onActivityResult(requestCode, resultCode, data);
		
	}
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {

       return UI.getInstance().onCreateOptionsMenu(menu);
       
   }
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
   	
   	return UI.getInstance().onOptionsItemSelected(item);
   	
   }
}