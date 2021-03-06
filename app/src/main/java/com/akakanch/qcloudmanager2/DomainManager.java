package com.akakanch.qcloudmanager2;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
 * Created by Long Zhang on 2017/3/22.
 */

public class DomainManager extends Fragment {
    private  APIRequestGenerator APIRG;
    private String defaultkey = new String();
    private String defaulyketId = new String();
    private SwipeRefreshLayout swiprefresh;
    private TextView domainTips;
    private ListView domainListView;
    private ArrayList<String> arrayOfDomain = new ArrayList<String>();
    private ArrayAdapter domainmAdapter;
    private View globeView;
    //确认是否为第一次启动
    private boolean firstRun = true;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.layout_domain, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!firstRun){
            return;
        }
        firstRun = false;
        globeView = getView();
        domainmAdapter  = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,arrayOfDomain);
        //获取关键控件变量
        domainTips = (TextView)getActivity().findViewById(R.id.textView_domain_tips);
        domainListView = (ListView)getActivity().findViewById(R.id.listView_domain);
        domainListView.setAdapter(domainmAdapter);
        swiprefresh = (SwipeRefreshLayout)getActivity().findViewById(R.id.swiperefresh_domain);
        //读取是否有key
        defaultkey =  read("API_KEY");
        defaulyketId = read("API_KEY_ID");
        if(defaultkey.equals("NULL") || defaulyketId.equals("NULL")){
            Snackbar.make(getView(),getString(R.string.str_tips_api_key_needed),Snackbar.LENGTH_LONG).show();
            return;
        }
        //初始化请求生成器
        APIRG = new APIRequestGenerator(defaulyketId,defaultkey);
        //
        domainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name = (String) domainmAdapter.getItem(i);
                Snackbar.make(globeView,getString(R.string.str_dm_loading_records),Snackbar.LENGTH_LONG).show();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                RecordsManager ip = new RecordsManager();
                Bundle args = new Bundle();
                args.putString("DOMAIN",name);
                ip.setArguments(args);
                fragmentTransaction.replace(R.id.content_main,ip);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                FloatingActionButton fab = (FloatingActionButton)getActivity().findViewById(R.id.fab);
                fab.setVisibility(View.VISIBLE);
            }
        });
        //设置下拉刷新
        swiprefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Snackbar.make(globeView, "刷新中，请稍候。", Snackbar.LENGTH_LONG).show();
                domainmAdapter.clear();
                new LoadDomainList().execute("https://" + APIRG.domain_getDomainList());
                Log.v("url=","https://" + APIRG.domain_getDomainList());
            }
        });
        //设置刷新条颜色
        swiprefresh.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary,R.color.colorRefresh_A,R.color.colorRefresh_B);
        //自动刷新一次
        String URL = "https://" + APIRG.domain_getDomainList();
        new LoadDomainList().execute(URL);
        swiprefresh.setRefreshing(true);
        Snackbar.make(swiprefresh, "刷新中，请稍候。", Snackbar.LENGTH_LONG).show();
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_refresh){
            domainmAdapter.clear();
            new LoadDomainList().execute("https://" + APIRG.domain_getDomainList());
            swiprefresh.setRefreshing(true);
            Snackbar.make(swiprefresh, "刷新中，请稍候。", Snackbar.LENGTH_LONG).show();
        }
        return true;
    }



    //用于从腾讯获取实例列表
    private class LoadDomainList extends AsyncTask<String, Void, String> {

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
                domainmAdapter.clear();
                responsejson = (JSONObject)responsejson.get("data");
                //继续解析
                JSONArray instanceSet = (JSONArray)responsejson.get("domains");
                //int domain_total = (int)((JSONObject)responsejson.get("info")).get("domain_total");
                int domain_total = instanceSet.length();
                for(int i=0;i<domain_total;i++){
                    JSONObject instance = (JSONObject)instanceSet.get(i);
                    int id = (int)instance.get("id");
                    String status = (String)instance.get("status");
                    String ttl = (String) instance.get("ttl");
                    String created_on = (String)instance.get("created_on");
                    String updated_on = (String) instance.get("updated_on");
                    String punycode = (String)instance.get("punycode");
                    String name = (String) instance.get("name");
                    String grade_title = (String)instance.get("grade_title");
                    domainmAdapter.add(name);
                }
                try{
                Snackbar.make(globeView,domain_total + "个域名找到。",Snackbar.LENGTH_LONG).show();
                domainTips.setText(domain_total + "个域名，点击可查看解析记录");
                }catch(Exception e){
                    Log.v("NO-SUITABLE-PARENT",e.getMessage());
                }
            }catch (JSONException e){
                Log.v("JSON-ERROR=",e.getMessage());
                Snackbar.make(swiprefresh, e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
            //刷新完成
            try {
                swiprefresh.setRefreshing(false);
                Snackbar.make(swiprefresh, "加载完毕!", Snackbar.LENGTH_LONG).show();
            }catch (Exception e){
                Log.v("NO-VIEW-FOUND=",e.getMessage());
            }
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
