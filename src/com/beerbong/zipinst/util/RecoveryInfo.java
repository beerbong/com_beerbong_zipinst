package com.beerbong.zipinst.util;

/**
 * @author Yamil Ghazi Kantelinen
 * @version 1.0
 */

public class RecoveryInfo {
 
    private int id;
    private String name = null;
    private String sdcard = null;
    
    public RecoveryInfo(int id, String name, String sdcard) {
        this.id = id;
        this.name = name;
        this.sdcard = sdcard;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSdcard() {
        return sdcard;
    }
    public void setSdcard(String sdcard) {
        this.sdcard = sdcard;
    }
}