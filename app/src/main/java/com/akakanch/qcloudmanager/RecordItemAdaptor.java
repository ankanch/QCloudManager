package com.akakanch.qcloudmanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Long Zhang on 2017/3/31.
 */

public class RecordItemAdaptor extends ArrayAdapter<RecordItem> {

    private View globeView;

    public RecordItemAdaptor(Context context, ArrayList<RecordItem> users) {
        super(context, 0, users);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //获取指定项数据
        globeView = convertView;
        final RecordItem recordItem = getItem(position);
        //检查视图是否被复用，否则用view填充
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_record_item, parent, false);
        }
        TextView tvName = (TextView)convertView.findViewById(R.id.textView_recordname);
        TextView tvValue = (TextView)convertView.findViewById(R.id.textView_recordvalue);
        TextView tvType = (TextView)convertView.findViewById(R.id.textView_recordtype);
        ImageButton btnEdit = (ImageButton)convertView.findViewById(R.id.imageButton_edit);
        tvName.setText( recordItem.name);
        tvValue.setText("" + recordItem.value);
        tvType.setText("\t" + recordItem.type);
        final APIRequestGenerator APIRG = new APIRequestGenerator(recordItem.APIKeyID,recordItem.APIKey);
        final View globView = convertView;
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //构造一个用于修改的对话框
                LayoutInflater li = LayoutInflater.from(getContext());
                View changeDlgView = li.inflate(R.layout.layout_change_resolve_dialog, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setView(changeDlgView);
                final EditText inputName = (EditText)changeDlgView.findViewById(R.id.editText_name);
                final EditText inputValue = (EditText)changeDlgView.findViewById(R.id.editText_value);
                final Spinner type = (Spinner)changeDlgView.findViewById(R.id.spinner_type);
                final Button btnConfrim = (Button)changeDlgView.findViewById(R.id.button_confirm);
                final Button btnCancel = (Button)changeDlgView.findViewById(R.id.button_cancel);
                final Button btnDelete = (Button)changeDlgView.findViewById(R.id.button_delete);
                inputName.setText(recordItem.name);
                inputValue.setText(recordItem.value);
                ArrayList<String> typelist = new ArrayList<String>(Arrays.asList(getContext().getResources().getStringArray(R.array.strarr_dm_spinner_records_type)));
                type.setSelection(typelist.indexOf(recordItem.type));
                final AlertDialog dlg = builder.show();
                dlg.setCanceledOnTouchOutside(false);
                btnConfrim.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar.make(globeView,"请求已提交!",Snackbar.LENGTH_LONG).show();
                        String name = inputName.getText().toString();
                        String value = inputValue.getText().toString();
                        String rtype = type.getSelectedItem().toString();
                        //执行修改解析记录操作\
                        String changeURL = "https://" + APIRG.domian_changeRecord(recordItem.domain,new String().valueOf(recordItem.id),name,rtype,recordItem.line,value);
                        Log.v("changeRecord=",changeURL);
                        new PerformChange().execute(changeURL);
                        dlg.dismiss();
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dlg.cancel();
                    }
                });
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //执行删除解析记录操作
                        String deleteURL = "https://"+ APIRG.domian_deleteRecord(recordItem.domain,new String().valueOf(recordItem.id));
                        Log.v("deltetRecord=",deleteURL);
                        new PerformChange().execute(deleteURL);
                        dlg.dismiss();
                    }
                });
            }
        });

        return convertView;
    }

    //用于执行域名解析记录相关操作
    private class PerformChange extends AsyncTask<String, Void, String> {

        private final ProgressDialog loading = new ProgressDialog(getContext());

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
            }catch (JSONException e){
                Log.v("JSON-ERROR=",e.getMessage());
            }
            //Snackbar.make(getView(),"修改成功！请手动刷新。",Snackbar.LENGTH_LONG).show();
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
}
