package com.akakanch.qcloudmanager2;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
    private SwipeRefreshLayout swiprefresh;
    private ArrayList<CloudServerItem> arrayOfCVM = new ArrayList<CloudServerItem>();
    private CloudServerItemAdapter cvmAdapter;
    private View globeView;
    private String defaultkey = new String();
    private String defaulyketId = new String();
    private FloatingActionButton fab;
    //存放seekbar数据
    private static String SysDiskSize ="20";
    private static String DataDiskSize ="0";
    private static String Bandwidth ="1";
    //确认是否为第一次启动
    private boolean firstRun = true;

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
        if(!firstRun){
            return;
        }
        firstRun = false;
        globeView = getView();
        cvmAdapter = new CloudServerItemAdapter(getActivity(), arrayOfCVM);
        //设置关键变量ID
        cvmListView = (ListView)getActivity().findViewById(R.id.listview_cvm_list);
        swiprefresh = (SwipeRefreshLayout)getActivity().findViewById(R.id.swiperefresh_cvm);
        cvmListView.setAdapter(cvmAdapter);
        fab = (FloatingActionButton)getActivity().findViewById(R.id.fab);
        //读取是否有key
        defaultkey =  read("API_KEY");
        defaulyketId = read("API_KEY_ID");
        if(defaultkey.equals("NULL") || defaulyketId.equals("NULL")){
            Snackbar.make(getView(),getString(R.string.str_tips_api_key_needed),Snackbar.LENGTH_LONG).show();
            return;
        }
        //初始化请求生成器
        APIRG = new APIRequestGenerator(defaulyketId,defaultkey);
        //设置fab按钮事件
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetCVMandCreate(getActivity());
            }
        });
        //设置下拉刷新
        swiprefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Snackbar.make(globeView, "刷新中，请稍候。", Snackbar.LENGTH_LONG).show();
                cvmAdapter.clear();
                new LoadAllInstanceList().execute(defaulyketId, defaultkey);
            }
        });
        //设置刷新条颜色
        swiprefresh.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary,R.color.colorRefresh_A,R.color.colorRefresh_B);
        //自动加载所有镜像
        new LoadAllInstanceList().execute(defaulyketId, defaultkey);
        swiprefresh.setRefreshing(true);
        Snackbar.make(swiprefresh, "刷新中，请稍候。", Snackbar.LENGTH_LONG).show();

    }

    //用于创建按量使用的服务器（非自定义镜像）
    public void SetCVMandCreate(Context ct){
        //构造创建实例对话框
        final Context contextx = ct;
        LayoutInflater li = LayoutInflater.from(ct);
        View changeDlgView = li.inflate(R.layout.layout_cloudserver_create_new_server, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(ct);
        builder.setView(changeDlgView);
        Toast.makeText(ct,ct.getString(R.string.str_cm_create_tips_support),Toast.LENGTH_LONG).show();
        final EditText etName = (EditText)changeDlgView.findViewById(R.id.editText_name_create);
        final EditText etPassword = (EditText)changeDlgView.findViewById(R.id.editText_password_create);
        final Spinner spOS = (Spinner)changeDlgView.findViewById(R.id.spinner_os_create);
        final Spinner spZone = (Spinner)changeDlgView.findViewById(R.id.spinner_zone_create);
        final Spinner spRecorce = (Spinner)changeDlgView.findViewById(R.id.spinner_serverresource_create);
        final SeekBar sbSystemDisk = (SeekBar)changeDlgView.findViewById(R.id.seekBar_systemdisk);
        final SeekBar sbDataDisk = (SeekBar)changeDlgView.findViewById(R.id.seekBar_datadisk);
        final SeekBar sbBandwidth = (SeekBar)changeDlgView.findViewById(R.id.seekBar_banwidth);
        final TextView tvSysDiskSize = (TextView)changeDlgView.findViewById(R.id.textView_disksystemsize);
        final TextView tvDtaDiskSize = (TextView)changeDlgView.findViewById(R.id.textView_diskdatasize);
        final TextView tvBandwidthDiskSize = (TextView)changeDlgView.findViewById(R.id.textView_bandwidth);
        final Button btnConfirm = (Button)changeDlgView.findViewById(R.id.button_confirm_create);
        final Button btnCancel = (Button)changeDlgView.findViewById(R.id.button_cancel_create);
        final AlertDialog dlg = builder.show();
        dlg.setCanceledOnTouchOutside(false);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString();
                String password = etPassword.getText().toString();
                if(name.length()<6 || password.length()<8){
                    Snackbar.make(getView(),contextx.getString(R.string.str_cm_create_tips_invaild),Snackbar.LENGTH_LONG).show();
                    return;
                }
                //开始创建
                String osid = spOS.getSelectedItem().toString().split("@")[1];
                String zoneid = spZone.getSelectedItem().toString().split("@")[1];
                String CPU = spRecorce.getSelectedItem().toString().split("@")[1].split(",")[0];
                String MEM = spRecorce.getSelectedItem().toString().split("@")[1].split(",")[1];
                APIRG = new APIRequestGenerator(defaulyketId,defaultkey);
                String CreateURL = "https://"+APIRG.cvm_createNewInstance(zoneid,CPU,MEM,osid,DataDiskSize,name,password,SysDiskSize,Bandwidth);
                Log.v("create-URL=",CreateURL);
                new CreateNewInstance().execute(CreateURL);
                dlg.dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlg.cancel();
            }
        });
        //设置seekBar信息
        sbSystemDisk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i<=30) {
                    SysDiskSize = SysDiskSize.valueOf(i + 20);
                }else{
                    SysDiskSize = "50";
                }
                tvSysDiskSize.setText(SysDiskSize + " GB");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        sbDataDisk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //保证是10的倍数
                int x =i;
                while(x%10 != 0){
                    x--;
                }
                DataDiskSize = DataDiskSize.valueOf(x);
                tvDtaDiskSize.setText(DataDiskSize + " GB");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        sbBandwidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Bandwidth = Bandwidth.valueOf(i);
                tvBandwidthDiskSize.setText(Bandwidth + " Mbps" );
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        //
    }



    //用于从腾讯获取实例列表（单个地域）老版本，1.1.2之前的
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
                    String iid = new String();
                    try {
                         iid = (String) instance.get("instanceId");
                    }catch (Exception e){
                        iid = "null";
                    }
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
        }
    }

    //用于从腾讯获取实例列表(所有实例)
    private class LoadAllInstanceList extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            //开始向腾讯请求实例列表
            WebClient wb = new WebClient();
            APIRequestGenerator apirg = new APIRequestGenerator(params[0],params[1]);
            String[] urllist = apirg.cvm_getAllInstanceList();
            String[] resullist = new String[apirg.REGION.length];
            try {
                for(int i =0;i<urllist.length;i++) {
                    resullist[i] = wb.getContent(urllist[i], "utf-8", "utf-8");
                }
            }catch (IOException e){
                Log.v("IO Exception=",e.getMessage());
                return "IO EXCEPTION";
            }
            String resultbuf = new String();
            try{
                resultbuf =  new JSONArray(resullist).toString();
            }catch (JSONException e){
                Log.v("error when pocess json=",e.getMessage());
            }
            return resultbuf;
        }
        @Override
        protected void onPostExecute(String message) {
            try {
                JSONArray jd = new JSONArray(message);
                for (int x = 0; x < jd.length(); x++) {
                    String buf = (String) jd.get(x);
                    //解析返回的JSON数据，加载资源列表
                    try {
                        JSONObject responsejson = new JSONObject(buf);
                        int resCode = (int) responsejson.get("code");
                        //检查是否成功获取数据
                        if (resCode != 0) {
                            String resMsg = (String) responsejson.get("message");
                            Snackbar.make(globeView, "错误：" + resMsg, Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        //继续解析
                        int totalCount = (int) responsejson.get("totalCount");
                        JSONArray instanceSet = (JSONArray) responsejson.get("instanceSet");
                        for (int i = 0; i < totalCount; i++) {
                            JSONObject instance = (JSONObject) instanceSet.get(i);
                            String name = (String) instance.get("instanceName");
                            String ip = (String) ((JSONArray) instance.get("wanIpSet")).get(0);
                            String os = (String) instance.get("os");
                            String region = (String) instance.get("Region");
                            String iid = new String();
                            try {
                                iid = (String) instance.get("instanceId");
                            } catch (Exception e) {
                                iid = "null";
                            }
                            int paymode = (int) instance.get("cvmPayMode");
                            int status = (int) instance.get("status");
                            CloudServerItem item = new CloudServerItem(name, ip, os, getStatusDes(status), getPayMode(paymode), getOSImg(os), iid);
                            item.setAPIInfo(defaultkey, defaulyketId);
                            item.InstanceRegion = region;
                            item.RegionName = APIRequestGenerator.REGION_NAME[x];
                            cvmAdapter.add(item);
                        }
                        Snackbar.make(globeView, totalCount + "个实例找到。", Snackbar.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        Log.v("JSON-ERROR=", e.getMessage());
                    }
                }
            }catch (Exception e){
                Log.v("json error when parser=",e.getMessage());
                try {
                    Snackbar.make(globeView, "加载失败！请检查网络并重试。", Snackbar.LENGTH_LONG).show();
                }catch (Exception ee){
                    Log.v("outExp=",ee.getMessage());
                }
            }
            //刷新完成
            swiprefresh.setRefreshing(false);
            Snackbar.make(swiprefresh, "加载完毕!", Snackbar.LENGTH_LONG).show();
        }
    }

    //用于创建云服务器
    private class CreateNewInstance extends AsyncTask<String, Void, String> {

        final private ProgressDialog loading= new ProgressDialog(getContext());;

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
            }catch (JSONException e){
                Log.v("JSON-ERROR=",e.getMessage());
            }
            //
            loading.dismiss();
            Snackbar.make(globeView,"创建成功，请刷新(等待3分钟，否则会出错)！",Snackbar.LENGTH_LONG).show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setMessage("创建中...");
            loading.show();
            loading.setCancelable(false);
            loading.setCanceledOnTouchOutside(false);
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
