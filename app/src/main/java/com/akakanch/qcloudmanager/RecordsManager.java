package com.akakanch.qcloudmanager;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
    private FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_domain_recordlist, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        AdView mAdView = (AdView) getActivity().findViewById(R.id.adView_recordlist);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

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
        //设置添加新记录对话框
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //构造一个用于修改的对话框
                LayoutInflater li = LayoutInflater.from(getActivity());
                View changeDlgView = li.inflate(R.layout.layout_add_new_record, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(changeDlgView);
                final EditText etName = (EditText)changeDlgView.findViewById(R.id.editText_name_add);
                final EditText etValue = (EditText)changeDlgView.findViewById(R.id.editText_value_add);
                final Spinner spType = (Spinner)changeDlgView.findViewById(R.id.spinner_type_add);
                final Button btnConfirm = (Button)changeDlgView.findViewById(R.id.button_confirm_add);
                final Button btnCancel = (Button)changeDlgView.findViewById(R.id.button_cancel_add);
                final AlertDialog dlg = builder.show();
                dlg.setCanceledOnTouchOutside(false);
                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = etName.getText().toString();
                        String value = etValue.getText().toString();
                        String type = spType.getSelectedItem().toString();
                        if(name.length()<1 || value.length()<7){
                            Snackbar.make(globeView,"请输入正确数据.", BaseTransientBottomBar.LENGTH_LONG).show();
                            return;
                        }
                        //生成添加链接
                        String addURL  = "https://" + APIRG.domian_addRecord(domain,name,type,"默认",value);
                        Log.v("add_record=",addURL);
                        //在这里执行
                        new AddNewRecord().execute(addURL);
                        dlg.dismiss();
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dlg.cancel();
                    }
                });

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
                recordItemAdaptor.clear();
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
                    item.setAPIInfo(defaultkey,defaulyketId);
                    item.domain = getArguments().getString("DOMAIN");
                    recordItemAdaptor.add(item);
                }
                Snackbar.make(globeView,totalCount + "个记录找到。",Snackbar.LENGTH_LONG).show();
            }catch (JSONException e){
                Log.v("JSON-ERROR=",e.getMessage());
            }
            //
            btnRefresh.setEnabled(true);
            refresh_progress.setVisibility(View.INVISIBLE);
        }
    }

    //用于添加解析记录
    private class AddNewRecord extends AsyncTask<String, Void, String> {

        private final ProgressDialog loading = new ProgressDialog(getContext());

        @Override
        protected String doInBackground(String[] params) {
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
            }catch (JSONException e){
                Log.v("JSON-ERROR=",e.getMessage());
            }
            Snackbar.make(globeView,"添加成功！请手动刷新。",Snackbar.LENGTH_LONG).show();
            loading.dismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setMessage("更新中...");
            loading.show();
            loading.setCancelable(false);
            loading.setCanceledOnTouchOutside(false);
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
