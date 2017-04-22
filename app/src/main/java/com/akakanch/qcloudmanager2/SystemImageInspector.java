package com.akakanch.qcloudmanager2;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Long Zhang on 4/12/2017.
 */

public class SystemImageInspector extends Fragment {

    private  APIRequestGenerator APIRG;
    private String defaultkey = new String();
    private String defaulyketId = new String();
    private ListView lvImageList ;
    private TextView tvHeaderTips;
    private SwipeRefreshLayout swiprefresh;
    private ArrayList<SystemImageItem> imageList = new ArrayList<SystemImageItem>();
    private SystemImageItemAdaptor imageAdaptor;
    private View globeView;
    //确认是否为第一次启动
    private boolean firstRun = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_system_image_inspector,container,false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!firstRun){
            return;
        }
        firstRun = false;
        lvImageList = (ListView) getActivity().findViewById(R.id.listview_systemimage_list);
        tvHeaderTips = (TextView)getActivity().findViewById(R.id.textView_systemimage_inspector_tips);
        imageAdaptor = new SystemImageItemAdaptor(getActivity(), imageList);
        swiprefresh = (SwipeRefreshLayout)getActivity().findViewById(R.id.swiperefresh_systemimage);
        lvImageList.setAdapter(imageAdaptor);
        globeView = getView();
        AdView mAdView = (AdView) getActivity().findViewById(R.id.adView_systemimage);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        //读取是否有key
        defaultkey =  read("API_KEY");
        defaulyketId = read("API_KEY_ID");
        if(defaultkey.equals("NULL") || defaulyketId.equals("NULL")){
            Snackbar.make(getView(),getString(R.string.str_tips_api_key_needed),Snackbar.LENGTH_LONG).show();
            return;
        }
        //初始化请求生成器
        APIRG = new APIRequestGenerator(defaulyketId,defaultkey);
        final String[] urllist = APIRG.systemimage_retriveAllImage();
        //设置下拉刷新
        swiprefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    new LoadSystemImage().execute(new JSONArray(urllist).toString());
                    swiprefresh.setRefreshing(true);
                    Snackbar.make(swiprefresh, "刷新中，请稍候。", Snackbar.LENGTH_LONG).show();
                }catch(JSONException e){
                    Log.v("ERROR_IN_REFRESH_TRS=",e.getMessage());
                }
            }
        });
        //设置刷新条颜色
        swiprefresh.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary,R.color.colorRefresh_A,R.color.colorRefresh_B);
        //自动刷新
        try {
            new LoadSystemImage().execute(new JSONArray(urllist).toString());
            swiprefresh.setRefreshing(true);
            Snackbar.make(swiprefresh, "刷新中，请稍候。", Snackbar.LENGTH_LONG).show();
        }catch(JSONException e){
            Log.v("ERROR_IN_REFRESH_TRS=",e.getMessage());
        }
    }

    //用于获取镜像列表
    private class LoadSystemImage extends AsyncTask<String, Void, String> {
        private String[] region;
        @Override
        protected String doInBackground(String[] params) {
            //开始向腾讯请求实例列表
            WebClient wb = new WebClient();
            String resultstr = new String();
            try {
                JSONArray urllist = new JSONArray(params[0]); //获得url列表
                region = new String[urllist.length()];
                ArrayList<String> returndata = new ArrayList<String>();   //用于储存返回结果
                for(int i=0;i<urllist.length();i++){ //构建地域列表
                    String url = (String) urllist.get(i);
                    region[i] = url.substring(url.indexOf("Region") + 7, url.indexOf("Region") + 9);
                }
                try {
                    returndata = wb.getContent(urllist);
                } catch (IOException e) {
                    Log.v("IO Exception=", e.getMessage());
                    return "IO EXCEPTION";
                }
                resultstr = new JSONArray(returndata).toString();
            }catch (JSONException e){
                Log.v("error-in-json-array=",e.getMessage());
            }
            return resultstr;
        }
        @Override
        protected void onPostExecute(String message) {
            //将message反向解析为JSONArray
            try {
                JSONArray reusltdata = new JSONArray(message);
                int imagecount = 0;
                imageAdaptor.clear();
                for(int t=0;t<reusltdata.length();t++) {
                    String datax = (String)reusltdata.get(t);
                    //解析返回的JSON数据，加载资源列表
                    try {
                        JSONObject responsejson = new JSONObject(datax);
                        int resCode = (int) responsejson.get("code");
                        //检查是否成功获取数据
                        if (resCode != 0) {
                            String resMsg = (String) responsejson.get("message");
                            try {
                                Snackbar.make(globeView, "错误：" + resMsg, Snackbar.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Log.v("NO-SUITABLE-PARENT", e.getMessage());
                            }
                            return;
                        }
                        //imageAdaptor.clear();
                        //继续解析
                        int count = (int) responsejson.get("totalCount");
                        Log.v("total-count=", new String().valueOf(count));
                        imagecount += count;
                        JSONArray imageset = (JSONArray) responsejson.get("imageSet");
                        for (int i = 0; i < count; i++) {
                            JSONObject imagedata = (JSONObject) imageset.get(i);
                            SystemImageItem imageitem = new SystemImageItem();
                            imageitem.imageName = (String) imagedata.get("imageName");
                            try {
                                imageitem.imageID = (String) imagedata.get("unImgId");
                            } catch (Exception e) {
                                imageitem.imageID = "未知";
                            }
                            imageitem.imageDescription = (String) imagedata.get("imageDescription");
                            imageitem.imageStatus = getImageStatus((int) imagedata.get("status"));
                            imageitem.osName = (String) imagedata.get("osName");
                            imageitem.createTime = (String) imagedata.get("createTime");
                            imageitem.region = region[t];
                            imageitem.APIKey = defaultkey;
                            imageitem.APIKeyID = defaulyketId;
                            imageAdaptor.add(imageitem);
                        }
                    } catch (JSONException e) {
                        Log.v("JSON-ERROR=", e.getMessage());
                    }
                }
                //刷新完成
                try {
                    swiprefresh.setRefreshing(false);
                    Snackbar.make(swiprefresh, "加载完毕!", Snackbar.LENGTH_LONG).show();
                    tvHeaderTips.setText("私有镜像列表加载完毕，共" +new String().valueOf(imagecount)+"个。");
                }catch (Exception e){
                    Log.v("NO-VIEW-FOUND=",e.getMessage());
                }
            }catch (JSONException e){
                Log.v("json-error-in-parsing=",e.getMessage());
            }
        }
    }

    public String getImageStatus(int statuscode){
        switch(statuscode){
            case 1:
                return "创建中";
            case 2:
                return "正常";
            case 3:
                return "使用中";
            case 4:
                return "同步中";
            case 5:
                return "复制中";
            default:
                return "未知状态";
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
