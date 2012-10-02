package com.beerbong.zipinst;

import com.beerbong.zipinst.activities.*;
import com.beerbong.zipinst.file.FileManager;
import com.beerbong.zipinst.file.RebootManager;
import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.util.Constants;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.*;

import android.view.*;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class Activity extends PreferenceActivity {

	private FileManager fileManager;
	private RebootManager rebootManager;
	
	@Override
   public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
      UI.create(this);

      fileManager = new FileManager(this);
      rebootManager = new RebootManager(this);
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
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.menu, menu);
       return true;
   }
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
   	Intent i;
   	switch (item.getItemId()) {
       	case R.id.sdcard:
       		i = new Intent(this, Sdcard.class);
       		startActivity(i);
       		break;
       	case R.id.about:
       		i = new Intent(this, About.class);
       		startActivity(i);
       		break;
       	case R.id.donate:
       		i = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.DONATE_URL));
       		startActivity(i);
       		break;
       	case R.id.exit:
       		finish();
       		break;
   	}
   	return true;
   }
}