package com.akakanch.qcloudmanager;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Long Zhang on 2017/3/26.
 */

public class CloudServerItem {
    public int ImageID = R.drawable.side_nav_bar;
    public String InstanceName = new String();
    public String InstanceIP = new String();
    public String InstanceOS = new String();
    public String Status = new String();
    public int insid = 0;

    public CloudServerItem(String name,String ip,String os,String status,int image,int id){
        InstanceName = name;
        InstanceIP = ip;
        InstanceOS = os;
        Status = status;
        ImageID = image;
        insid = id;
    }


}
