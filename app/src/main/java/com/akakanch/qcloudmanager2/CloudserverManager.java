package com.akakanch.qcloudmanager2;

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
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

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
    private ProgressBar refresh_progress;
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
        return inflater.inflate(R.layout.layout_cloudserver,container,false);
    }

    @Override
    public void onStart() {
        super.onStart();
        AdView mAdView = (AdView) getActivity().findViewById(R.id.adView_cloudserver);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        globeView = getView();
        cvmAdapter = new CloudServerItemAdapter(getActivity(), arrayOfCVM);
        //设置关键变量ID
        locationSelector = (Spinner) getActivity().findViewById(R.id.spinner_location);
        buttonRefresh = (Button)getActivity().findViewById(R.id.button_refresh);
        cvmListView = (ListView)getActivity().findViewById(R.id.listview_cvm_list);
        cvmListView.setAdapter(cvmAdapter);
        refresh_progress = (ProgressBar)getActivity().findViewById(R.id.progressBar_cvmrefresh);
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
                buttonRefresh.setEnabled(false);
                refresh_progress.setVisibility(View.VISIBLE);
                cvmAdapter.clear();
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
                    String region = (String)instance.get("Region");
                    String iid = (String)instance.get("instanceId");
                    int paymode = (int)instance.get("cvmPayMode");
                    int status = (int)instance.get("status");
                    CloudServerItem item = new CloudServerItem(name,ip,os,getStatusDes(status),getPayMode(paymode),getOSImg(os),iid);
                    item.setAPIInfo(defaultkey,defaulyketId);
                    item.InstanceRegion = region;
                    cvmAdapter.add(item);
                }
                Snackbar.make(globeView,totalCount + "个实例找到。",Snackbar.LENGTH_LONG).show();
            }catch (JSONException e){
                Log.v("JSON-ERROR=",e.getMessage());
            }
            //
            buttonRefresh.setEnabled(true);
            refresh_progress.setVisibility(View.INVISIBLE);
        }
    }

    //该函数用于根据腾讯返回的状态码，获取状态字符串
    public String getStatusDes(int statuscode){
        String statusdes = new String();
        switch(statuscode){
            case 1:
                statusdes = getString(R.string.str_cm_statusdes_error);
                break;
            case 2:
                statusdes = getString(R.string.str_cm_statusdes_running);
                break;
            case 3:
                statusdes = getString(R.string.str_cm_statusdes_creating);
                break;
            case 4:
                statusdes = getString(R.string.str_cm_statusdes_shutdown);
                break;
            case 12:
                statusdes = getString(R.string.str_cm_statusdes_snapshooting);
                break;
            case 14:
                statusdes = getString(R.string.str_cm_statusdes_reinstall);
                break;
            case 7:
                statusdes = getString(R.string.str_cm_statusdes_rebooting);
                break;
            case 8:
                statusdes = getString(R.string.str_cm_statusdes_booting);
                break;
            case 9:
                statusdes = getString(R.string.str_cm_statusdes_shutdowning);
                break;
            case 11:
                statusdes = getString(R.string.str_cm_statusdes_formatting);
                break;
            default:
                statusdes = getString(R.string.str_cm_statusdes_others);
        }
        return statusdes;
    }

    //该函数用户根据腾讯返回的OS名字设置相应的OS图标
    public int getOSImg(String osname){
        if(osname.indexOf("ubuntu") >= 0){
            return R.drawable.raw_ubuntuhero;
        }else if(osname.indexOf("cent") >= 0){
            return R.drawable.raw_centos;
        }else if(osname.indexOf("core") >= 0){
            return R.drawable.raw_coreos;
        }else if(osname.indexOf("debian") >= 0){
            return R.drawable.raw_debian;
        }else if(osname.indexOf("free") >= 0){
            return R.drawable.raw_freebsd;
        }else if(osname.indexOf("open") >= 0){
            return R.drawable.raw_opensuse;
        }else if(osname.indexOf("windows") >= 0){
            return R.drawable.raw_windowsserver;
        }else{
            return R.drawable.raw_suse;
        }

    }

    //该函数根据腾讯返回的PayMode，获取支付方式字符串
    public String getPayMode(int paymode){
        switch (paymode){
            case 0:
                return getString(R.string.str_cm_paymode_latemonth);
            case 1:
                return getString(R.string.str_cm_paymode_payfirst);
            case 2:
                return getString(R.string.str_cm_paymode_paybyuse);
            default:
                return getString(R.string.str_cm_paymode_unknow);
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
