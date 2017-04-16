package com.akakanch.qcloudmanager2;
import org.json.JSONArray;
import org.json.JSONException;

import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Long Zhang on 2017/3/24.
 */

public class WebClient {

    private static String _newLine = System.getProperty("line.separator");
    public WebClient()
    {    }
    public String getContent(String url, String oriEncoding, String targetEncoding) throws IOException
    {
        URL u = new URL(url);
        URLConnection uc = u.openConnection();
        BufferedReader in;

        if(oriEncoding == null || oriEncoding.length() == 0)
        {
            in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        }
        else
        {
            in = new BufferedReader(new InputStreamReader(uc.getInputStream(), oriEncoding));
        }
        String line;
        StringBuilder sb = new StringBuilder();
        while((line = in.readLine()) != null)
        {
            sb.append(line); sb.append(_newLine);
        }
        if(targetEncoding == null || targetEncoding.length() == 0)
        {
            return sb.toString();
        }
        return new String(sb.toString().getBytes(), targetEncoding);
    }

    public ArrayList<String> getContent(String[] urls) throws  IOException{
        ArrayList<String> contentlist = new ArrayList<String>();
        for(String url : urls){
            String data = getContent(url, "utf-8", "utf-8");
            contentlist.add(data);
        }
        return contentlist;
    }

    public ArrayList<String> getContent(JSONArray urls) throws  IOException,JSONException{
        ArrayList<String> contentlist = new ArrayList<String>();
        for(int i=0;i<urls.length();i++){
            String data = getContent((String)urls.get(i), "utf-8", "utf-8");
            contentlist.add(data);
        }
        return contentlist;
    }
}
