package com.akakanch.qcloudmanager;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by Long Zhang on 2017/3/22.
 */

public class IndexPage extends Fragment {
    private Button btnOk;
    private EditText editText;
    private EditText editTextid;
    private boolean buttonOKmode = true;
    private String defaultkey =  new String();
    private String defaulyketId = new String();
    private boolean first = true;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_index,container,false);
    }

    @Override
    public void onStart() {
        super.onStart();
        AdView mAdView = (AdView) getActivity().findViewById(R.id.adView_index);
        AdRequest adRequest = new AdRequest.Builder().build();
        btnOk = (Button)getActivity().findViewById(R.id.button_OK);
        editText = (EditText)getActivity().findViewById(R.id.editText_apikey);
        editTextid = (EditText)getActivity().findViewById(R.id.editText_apikeyid);
        if(first) {
            //读取是否已经存在APIkey
            defaultkey = read("API_KEY");
            defaulyketId = read("API_KEY_ID");
            if (defaultkey.equals("NULL") || defaulyketId.equals("NULL")) {
                Snackbar.make(getView(), getString(R.string.str_tips_api_key_needed), Snackbar.LENGTH_LONG).show();
                buttonOKmode = true;
                btnOk.setText(getString(R.string.str_ip_ok));
            } else {
                Snackbar.make(getView(), getString(R.string.str_tips_api_key_found), Snackbar.LENGTH_LONG).show();
                editText.setText(defaultkey);
                editTextid.setText(defaulyketId);
                editText.setEnabled(false);
                editTextid.setEnabled(false);
                buttonOKmode = false;
                btnOk.setText(getString(R.string.str_ip_change));
            }
            first=false;
        }
        //设置事件
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //&*******************************正式版请删掉以下两行代码
                //save("API_KEY", "c6RcifSjz9B3qV2eKvNiy53wtmVWTGwU");
                //save("API_KEY_ID","AKIDHYhgbRrh8UhJm6pPNCxb6RitvvmZKj8Y");
                //&*******************************正式版请删掉上面两行代码
                if(buttonOKmode) {
                    //添加模式
                    String APIkey =  editText.getText().toString();
                    String APIkeyId = editTextid.getText().toString();
                    if (APIkey.length() < 8 || APIkeyId.length()<8) {
                        Snackbar.make(getView(), getString(R.string.str_tips_invaild_api_key), Snackbar.LENGTH_LONG).show();
                    } else {
                        //&*******************************正式版需要取消以下两行代码的注释
                        save("API_KEY", APIkey);
                        save("API_KEY_ID",APIkeyId);
                        editText.setText(APIkey);
                        editTextid.setText(APIkeyId);
                        editText.setEnabled(false);
                        editTextid.setEnabled(false);
                        btnOk.setText(getString(R.string.str_ip_change));
                        Snackbar.make(getView(), getString(R.string.str_tips_api_key_saved), Snackbar.LENGTH_LONG).show();
                        buttonOKmode = false;
                    }
                }else {
                    //修改模式
                    btnOk.setText(getString(R.string.str_ip_ok));
                    editText.setEnabled(true);
                    editTextid.setEnabled(true);
                    buttonOKmode = true;
                }
            }
        });
        //判断是否有保存的状态，如果有的话，继续编辑
        mAdView.loadAd(adRequest);
    }


    public void save(String key, String value){
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
