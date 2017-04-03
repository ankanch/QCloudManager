package com.akakanch.qcloudmanager;

/**
 * Created by Long Zhang on 2017/3/31.
 */

public class DomainItem {
    public String status = new String();
    public String ttl = new String();
    public String created_on = new String();
    public String updated_on = new String();
    public String punycode = new String();
    public String name = new String();
    public String grade_title = new String();
    public RecordItem recordItem;

    public DomainItem(String status,String ttl,String created_on,String updated_on,String punycode,String name,String grade_title){
        this.status = status;
        this.created_on = created_on;
        this.ttl = ttl;
        this.updated_on = updated_on;
        this.punycode = punycode;
        this.name = name;
        this.grade_title = grade_title;
    }
}
