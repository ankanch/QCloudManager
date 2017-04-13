package com.akakanch.qcloudmanager2;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
        //设置刷新事件
        refreshbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        //
        //手动添加几个项目
        SystemImageItem test = new SystemImageItem("IMAGE ID ASSAKDJ","Name xxx","This is description.","X OS","2106-1-1","OK");
        imageList.add(test);
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
