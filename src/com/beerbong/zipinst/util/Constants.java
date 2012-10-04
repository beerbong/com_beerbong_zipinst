package com.beerbong.zipinst.util;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class Constants {
	
	public static final String PREFS_NAME = "ZipInstallerPrefsFile";
	
	public static final String PROPERTY_INTERNAL_STORAGE = "internal-storage";
	public static final String DEFAULT_INTERNAL_STORAGE = "emmc";

	// main preferences
	public static final String PREFERENCE_FILE_LIST = "file_list";
	public static final String PREFERENCE_CHOOSE_ZIP = "choose_zip";
	public static final String PREFERENCE_INSTALL_NOW = "install_now";

	// about preferences
	public static final String PREFERENCE_ABOUT_VERSION = "about_version";
	public static final String PREFERENCE_ABOUT_SITE = "about_pref";
	public static final String PREFERENCE_ABOUT_LICENSE = "license_pref";
	public static final String PREFERENCE_ABOUT_CHANGELOG = "changelog_pref";
	
	public static final String DONATE_URL = "http://forum.xda-developers.com/donatetome.php?u=1806623";
	public static final String ABOUT_URL = "http://forum.xda-developers.com/showthread.php?t=1906396";
	
	public static final int REQUEST_PICK_ZIP = 203;
	
	private static final long K = 1024;
	private static final long M = K * K;
	private static final long G = M * K;
	private static final long T = G * K;
	
	public static String formatSize(final long value){
	    final long[] dividers = new long[] { T, G, M, K, 1 };
	    final String[] units = new String[] { "TB", "GB", "MB", "KB", "B" };
	    if(value < 1)
	        throw new IllegalArgumentException("Invalid file size: " + value);
	    String result = null;
	    for(int i = 0; i < dividers.length; i++){
	        final long divider = dividers[i];
	        if(value >= divider){
	            result = format(value, divider, units[i]);
	            break;
	        }
	    }
	    return result;
	}

	private static String format(final long value,
	    final long divider,
	    final String unit){
	    final double result =
	        divider > 1 ? (double) value / (double) divider : (double) value;
	    return String.format("%.1f %s", Double.valueOf(result), unit);
	}
}