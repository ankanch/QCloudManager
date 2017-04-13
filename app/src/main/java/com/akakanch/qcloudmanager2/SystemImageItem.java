package com.akakanch.qcloudmanager2;

import java.util.ArrayList;

/**
 * Created by Long Zhang on 4/13/2017.
 */

public class SystemImageItem {

    public String imageID = new String();
    public String imageName = new String();
    public String imageDescription = new String();
    public String osName = new String();
    public String createTime = new String();
    public String imageStatus = new String();

    //APIkey信息（用于与Adaptor里面的popupmenu交互）
    public String APIKey = new String();
    public String APIKeyID = new String();

    public SystemImageItem(){

    }

    public SystemImageItem(String id,String name,String description,String osname,String timecreate,String status){
        imageID = id;
        imageName = name;
        imageDescription = description;
        osName = osname;
        createTime = timecreate;
        imageStatus = status;
    }

    public void setAPIInfo(String key,String keyid){
        APIKey = key;
        APIKeyID = keyid;
    }
}
