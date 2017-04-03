package com.akakanch.qcloudmanager;

/**
 * Created by Long Zhang on 2017/3/31.
 */

public class RecordItem {
    public String name = new String();
    public String value= new String();
    public String status= new String();
    public int id= 0;
    public String ttl= new String();
    public String line = new String();
    public String type = new String();
    public String mx = new String();


    public  RecordItem(int id,String status,String name,String value,String type){
        this.id = id;
        this.status = status;
        this.value = value;
        this.type = type;
        this.name = name;
    }
}
