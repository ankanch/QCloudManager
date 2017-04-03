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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Long Zhang on 2017/4/3.
 */

public class RecordsManager extends Fragment {
    private APIRequestGenerator APIRG;
    private ListView recordsListView;
    private ProgressBar refresh_progress;
    private ArrayList<RecordItem> arrayOfRecords = new ArrayList<RecordItem>();
    private View globeView;
    private String defaultkey = new String();
    private String defaulyketId = new String();
    private RecordItemAdaptor recordItemAdaptor;
    private Button btnRefresh;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_domain_recordlist, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        globeView = getView();
        recordItemAdaptor = new RecordItemAdaptor(getActivity(),arrayOfRecords);
        //获取关键控件变量
        btnRefresh = (Button)getActivity().findViewById(R.id.button_refresh_record);
        TextView tvTips = (TextView)getActivity().findViewById(R.id.textView_domain_record);
        refresh_progress = (ProgressBar)getActivity().findViewById(R.id.progressBar_refresh_recordslist);
        recordsListView = (ListView)getActivity().findViewById(R.id.listview_record_list);
        recordsListView.setAdapter(recordItemAdaptor);
        refresh_progress.setVisibility(View.INVISIBLE);
        final String domain = getArguments().getString("DOMAIN");
        tvTips.setText(domain);
        getActivity().setTitle(getActivity().getString(R.string.str_dm_title));
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
        final String recordslisturl = "https://" + APIRG.domian_getRecordList(domain);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnRefresh.setEnabled(false);
                refresh_progress.setVisibility(View.VISIBLE);
                Log.v("recordURL=",recordslisturl);
                new LoadRecordList().execute(recordslisturl);
            }
        });
        //自动刷新一次
        btnRefresh.setEnabled(false);
        refresh_progress.setVisibility(View.VISIBLE);
        new LoadRecordList().execute(recordslisturl);
    }

    //用于获取指定域名的记录列表
    private class LoadRecordList extends AsyncTask<String, Void, String> {

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
                responsejson = (JSONObject) responsejson.get("data");
                int totalCount = Integer.parseInt((String)(((JSONObject)(responsejson.get("info"))).get("record_total")));
                JSONArray recordsSet = (JSONArray)responsejson.get("records");
                for(int i=0;i<totalCount;i++){
                    JSONObject instance = (JSONObject)recordsSet.get(i);
                    String name = (String)instance.get("name");
                    String type = (String)instance.get("type");
                    String value = (String)instance.get("value");
                    int id = (int)instance.get("id");
                    String status = (String) instance.get("status");
                    RecordItem item = new RecordItem(id,status,name,value,type);
                    recordItemAdaptor.add(item);
                }
                Snackbar.make(globeView,totalCount + "个实例找到。",Snackbar.LENGTH_LONG).show();
            }catch (JSONException e){
                Log.v("JSON-ERROR=",e.getMessage());
            }
            //
            btnRefresh.setEnabled(true);
            refresh_progress.setVisibility(View.INVISIBLE);
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
