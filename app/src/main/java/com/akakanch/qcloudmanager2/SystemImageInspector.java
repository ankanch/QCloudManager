package com.akakanch.qcloudmanager2;

import android.app.Fragment;
import android.app.ProgressDialog;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    private ProgressBar refresh_progress;
    private Button refreshbutton;
    private ArrayList<SystemImageItem> imageList = new ArrayList<SystemImageItem>();
    private SystemImageItemAdaptor imageAdaptor;
    private View globeView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_system_image_inspector,container,false);
    }

    @Override
    public void onStart() {
        super.onStart();
        lvImageList = (ListView) getActivity().findViewById(R.id.listview_systemimage_list);
        tvHeaderTips = (TextView)getActivity().findViewById(R.id.textView_systemimage_inspector_tips);
        refresh_progress = (ProgressBar)getActivity().findViewById(R.id.progressBar_systemimage_inspector);
        imageAdaptor = new SystemImageItemAdaptor(getActivity(), imageList);
        lvImageList.setAdapter(imageAdaptor);
        globeView = getView();
        refreshbutton = (Button)getActivity().findViewById(R.id.button_refresh_systemimage);
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
        //设置刷新事件
        refreshbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(String url : urllist){
                    new LoadSystemImage().execute(url);
                    Log.v("imagelisturl=",url);
                }
            }
        });
        //
        //手动添加几个项目
        SystemImageItem test = new SystemImageItem("IMAGE ID ASSAKDJ","Name xxx","This is description.","X OS","2106-1-1","OK");
        imageList.add(test);
        //自动刷新
    }

    //用于获取镜像列表
    private class LoadSystemImage extends AsyncTask<String, Void, String> {
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
            refresh_progress.setVisibility(View.INVISIBLE);
            refreshbutton.setEnabled(true);
            Snackbar.make(globeView,"刷新完毕！",Snackbar.LENGTH_LONG).show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            refresh_progress.setVisibility(View.VISIBLE);
            refreshbutton.setEnabled(false);
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
