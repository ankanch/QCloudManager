package com.akakanch.qcloudmanager;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Long Zhang on 2017/3/22.
 */

public class CloudserverManager extends Fragment {
    private  APIRequestGenerator APIRG;
    private ListView cvmListView;
    private ArrayList<CloudServerItem> arrayOfCVM = new ArrayList<CloudServerItem>();
    private CloudServerItemAdapter cvmAdapter;
    private View globeView;
    private String defaultkey = new String();
    private String defaulyketId = new String();
    private Spinner locationSelector;
    private Button buttonRefresh;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            //        return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.layout_cloudserver,container,false);
    }

    @Override
    public void onStart() {
        super.onStart();
        globeView = getView();
        cvmAdapter = new CloudServerItemAdapter(getActivity(), arrayOfCVM);
        //设置关键变量ID
        locationSelector = (Spinner) getActivity().findViewById(R.id.spinner_location);
        buttonRefresh = (Button)getActivity().findViewById(R.id.button_refresh);
        cvmListView = (ListView)getActivity().findViewById(R.id.listview_cvm_list);
        cvmListView.setAdapter(cvmAdapter);
        //读取是否有key
        defaultkey =  read("API_KEY");
        defaulyketId = read("API_KEY_ID");
        if(defaultkey.equals("NULL") || defaulyketId.equals("NULL")){
            Snackbar.make(getView(),getString(R.string.str_tips_api_key_needed),Snackbar.LENGTH_LONG).show();
            return;
        }
        //初始化请求生成器
        APIRG = new APIRequestGenerator(defaulyketId,defaultkey);
        //设置刷新事件
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //找到用户选择的区域
                String cvmlocation = locationSelector.getSelectedItem().toString();
                int i = 0;
                for(String loc : APIRequestGenerator.REGION_NAME){
                    if(loc.equals(cvmlocation)){
                        cvmlocation = APIRequestGenerator.REGION[i];
                        break;
                    }
                    i++;
                }
                Log.v("SelectLocation=",cvmlocation);
                //构造刷新请求字符串,生成请求URL
                String readRecordListURL = "https://" + APIRG.cvm_getInstanceList(cvmlocation);
                Log.v("API-URL-Cloud-Server=",readRecordListURL);
                //发送获取实例请求，并加载实例
                new LoadInstanceList().execute(readRecordListURL);
            }
        });
        //测试代码
        CloudServerItem testitem = new CloudServerItem("Default Test","123.123.123","Operation System","Status",R.drawable.raw_centos,0);
        cvmAdapter.add(testitem);

    }

    //用于从腾讯获取实例列表
    private class LoadInstanceList extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            //开始向腾讯请求实例列表
            WebClient wb = new WebClient();
            String resultstr = new String();
            try {
                resultstr = wb.getContent(params[0], "utf-8", "utf-8");
            }catch (IOException e){
                Log.v("IO Exception=",e.getMessage());
                return "IO EXCEPTION";
            }
            return resultstr;
        }
        @Override
        protected void onPostExecute(String message) {
            //解析返回的JSON数据，加载资源列表
            try {
                JSONObject responsejson = new JSONObject(message);
                int resCode = (int)responsejson.get("code");
                //检查是否成功获取数据
                if(resCode != 0) {
                    String resMsg = (String) responsejson.get("message");
                    Snackbar.make(globeView,"错误："+resMsg,Snackbar.LENGTH_LONG).show();
                    return;
                }
                //继续解析
                int totalCount = (int)responsejson.get("totalCount");
                JSONArray instanceSet = (JSONArray)responsejson.get("instanceSet");
                for(int i=0;i<totalCount;i++){
                    JSONObject instance = (JSONObject)instanceSet.get(i);
                    String name = (String)instance.get("instanceName");
                    String ip = (String)((JSONArray)instance.get("wanIpSet")).get(0);
                    String os = (String)instance.get("os");
                    int status = (int)instance.get("status");
                    CloudServerItem item = new CloudServerItem(name,ip,os,new String().valueOf(status),R.drawable.raw_windowsserver,1);
                    cvmAdapter.add(item);
                }
                Snackbar.make(globeView,totalCount + "个实例找到。",Snackbar.LENGTH_LONG).show();
                return;
            }catch (JSONException e){
                Log.v("JSON-ERROR=",e.getMessage());
            }
            //加载资源列表
            CloudServerItem testitem = new CloudServerItem("Test","123.123.123","Test OS","Running",R.drawable.raw_windowsserver,1);
            cvmAdapter.add(testitem);
        }
    }

    public void save(String key,String value){
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String read(String key){
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String defaultValue = "NULL";
        String value = sharedPref.getString(key, defaultValue);
        return value;
    }
}
