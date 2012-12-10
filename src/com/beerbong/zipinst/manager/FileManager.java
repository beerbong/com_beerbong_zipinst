package com.beerbong.zipinst.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.Toast;

import com.beerbong.zipinst.R;
import com.beerbong.zipinst.ui.UI;
import com.beerbong.zipinst.ui.UIAdapter;
import com.beerbong.zipinst.util.Constants;
import com.beerbong.zipinst.util.StoredPreferences;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class FileManager extends UIAdapter {

    private SharedPreferences settings;
    private Activity mActivity;
    private NodeList pathList = null;

    protected FileManager(Activity activity) {
        mActivity = activity;
        settings = mActivity.getSharedPreferences(Constants.PREFS_NAME, 0);

        UI.getInstance().removeAllPreferences();
        UI.getInstance().addUIListener(this);
        
        init();
    }
    
    private void init() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(mActivity.getAssets().open("paths.xml"));
            
            pathList = doc.getElementsByTagName("path");
            
        } catch (Exception ex) {
            Toast.makeText(mActivity, R.string.paths_error, Toast.LENGTH_LONG).show();
            return;
        }
        
        Intent intent = mActivity.getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        
        if ("application/zip".equals(type) || "*/*".equals(type)) {
            if (Intent.ACTION_SEND.equals(action)) {
                handleSendZip(intent);
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                handleSendMultipleZips(intent);
            }
        }
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

            addZip(zipPath);

        }
    }
    public void saveList() {
        int size = StoredPreferences.size();
        if (size == 0) return;
        
        StringBuffer list = new StringBuffer();
        
        for (int i=0;i<size;i++) {
            String path = (String)StoredPreferences.getPreference(i).getTitle();
            list.append(path);
            if (i < size -1) list.append("\n");
        }
        
        SharedPreferences settings = mActivity.getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.PROPERTY_LIST, list.toString());
        editor.commit();
        
        Toast.makeText(mActivity, R.string.list_saved, Toast.LENGTH_SHORT).show();
    }
    public void loadList() {
        String list = mActivity.getSharedPreferences(Constants.PREFS_NAME, 0).getString(Constants.PROPERTY_LIST, "");
        
        StringTokenizer tokenizer = new StringTokenizer(list, "\n");
        while (tokenizer.hasMoreTokens()) {
            String path = tokenizer.nextToken();
            
            File file = new File(path);
            if (!file.exists()) {
                AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
                alert.setTitle(R.string.list_alert_title);
                alert.setMessage(mActivity.getString(R.string.list_file_not_exists, path));
                alert.setPositiveButton(R.string.recovery_alert_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                alert.show();
                return;
            }
            
            addZip(path);
        }

        Toast.makeText(mActivity, R.string.list_loaded, Toast.LENGTH_SHORT).show();
    }

    private void handleSendZip(Intent intent) {
        Uri zipUri = (Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (zipUri != null) {
            addZip(zipUri.getEncodedPath());
        }
    }
    private void handleSendMultipleZips(Intent intent) {
        ArrayList<Uri> zipUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (zipUris != null) {
            for (int i = 0;i < zipUris.size();i++) {
                addZip(zipUris.get(i).getEncodedPath());
            }
        }
    }
    private void addZip(String zipPath) {
        
        if (zipPath == null || !zipPath.endsWith(".zip")) {
            Toast.makeText(mActivity, R.string.install_file_manager_invalid_zip, Toast.LENGTH_SHORT).show();
            return;
        }
        
        for (int i=0;i<pathList.getLength();i++) {
            String name = pathList.item(i).getAttributes().getNamedItem("name").getNodeValue();
            String allowed = pathList.item(i).getAttributes().getNamedItem("allowed").getNodeValue();
            if ("0".equals(allowed) && zipPath.startsWith(name)) {
                // external sdcard not allowed
                Toast.makeText(mActivity, R.string.install_file_manager_intsdcard, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!zipPath.endsWith(".zip")) {
            Toast.makeText(mActivity, R.string.install_file_manager_zip, Toast.LENGTH_SHORT).show();
            return;
        }

        String sdcardPath = new String(zipPath);

        String internalStorage = settings.getString(Constants.PROPERTY_INTERNAL_STORAGE, Constants.DEFAULT_INTERNAL_STORAGE);
        
        for (int i=0;i<pathList.getLength();i++) {
            String name = pathList.item(i).getAttributes().getNamedItem("name").getNodeValue();
            String allowed = pathList.item(i).getAttributes().getNamedItem("allowed").getNodeValue();
            if ("1".equals(allowed) && zipPath.startsWith(name)) zipPath = zipPath.replace(name, "/" + internalStorage);
        }
        
        UI.getInstance().addPreference(zipPath, sdcardPath);
    }
}