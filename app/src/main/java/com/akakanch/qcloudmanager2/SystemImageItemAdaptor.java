package com.akakanch.qcloudmanager2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Long Zhang on 4/13/2017.
 */

public class SystemImageItemAdaptor extends ArrayAdapter<SystemImageItem> {
    private Context ct;
    //存放seekbar数据
    private static String SysDiskSize ="20";
    private static String DataDiskSize ="0";
    private static String Bandwidth ="1";
    private  APIRequestGenerator APIRG;
    private View globeView;

    public SystemImageItemAdaptor(Context context, ArrayList<SystemImageItem> users) {
        super(context, 0, users);
        ct = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //获取指定项数据
        final SystemImageItem imageItem = getItem(position);
        //检查视图是否被复用，否则用view填充
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_systemimage_item_row, parent, false);
        }
        globeView = convertView;
        //设置关键变量
        final TextView tvName = (TextView)convertView.findViewById(R.id.textView_image_name);
        final TextView tvID = (TextView)convertView.findViewById(R.id.textView_imgae_id);
        final TextView tvCreateTime = (TextView)convertView.findViewById(R.id.textView_image_createtime);
        final TextView tvOS = (TextView)convertView.findViewById(R.id.textView_image_operatingsystem);
        final TextView tvDescription = (TextView)convertView.findViewById(R.id.textView_image_description);
        final Button btnMenu = (Button)convertView.findViewById(R.id.button_systemimage_menu);
        final APIRequestGenerator APIRG = new APIRequestGenerator(imageItem.APIKeyID,imageItem.APIKey);
        //设置数据
        tvName.setText(imageItem.imageName);
        tvID.setText("镜像ID："+imageItem.imageID);
        tvCreateTime.setText(imageItem.createTime);
        tvOS.setText(imageItem.osName);
        tvDescription.setText("状态："+imageItem.imageStatus);
        //设置菜单事件
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu pm = new PopupMenu(getContext(),btnMenu);
                pm.getMenuInflater().inflate(R.menu.systemimage_context_menu, pm.getMenu());
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        String url = new String();
                        switch(menuItem.getItemId()){
                            case R.id.menu_systemimage_createcvm:
                                SetCVMandCreate(ct,imageItem.imageID,imageItem.osName, imageItem.region,imageItem.APIKeyID,imageItem.APIKey);
                                break;
                            case R.id.menu_systemimage_delete:
                                url = "https://"+APIRG.systemimage_deleteImage(imageItem.imageID,imageItem.region);
                                Log.v("delete-url=",url);
                                break;
                        }
                        new CreateNew().execute(url);
                        return false;
                    }
                });
                pm.show();
            }
        });
        //
        return convertView;
    }

    //用于创建按量使用的服务器（----自定义镜像）
    public void SetCVMandCreate(final Context ct, String osid, String osname,String rc,final String defaulyketId,final String defaultkey){
        //构造创建实例对话框
        final Context contextx = ct;
        LayoutInflater li = LayoutInflater.from(ct);
        View changeDlgView = li.inflate(R.layout.layout_cloudserver_create_new_server, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(ct);
        builder.setView(changeDlgView);
        Toast.makeText(ct,ct.getString(R.string.str_sii_create_tips_support),Toast.LENGTH_LONG).show();
        final EditText etName = (EditText)changeDlgView.findViewById(R.id.editText_name_create);
        final EditText etPassword = (EditText)changeDlgView.findViewById(R.id.editText_password_create);
        final Spinner spOS = (Spinner)changeDlgView.findViewById(R.id.spinner_os_create);
        //设置当前系统为用户自定义镜像
        String[] os = new String[1];
        os[0] = "私有镜像:" + osname;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ct, android.R.layout.simple_spinner_item, os);
        spOS.setAdapter(adapter);
        spOS.setEnabled(false);
        //---------
        final Spinner spZone = (Spinner)changeDlgView.findViewById(R.id.spinner_zone_create);
        //限制地域为镜像地域
        String[] reg = new String[]{"北京一区@800001", "上海一区@200001", "广州二区@100002",
                                    "广州三区@100003", "香港一区(2核2G及以上)@300001", "北美一区@400001"};
        String[] regioncode = new String[]{"bj","sh","gz","gz","hk","ca","sg",};
        int i =0;
        Log.v("rc=",rc);
        for(String x : regioncode){
            if(x.equals(rc)){
                break;
            }
            i++;
        }
        ArrayAdapter<String> adapterb = new ArrayAdapter<String>(ct, android.R.layout.simple_spinner_item, new String[]{reg[i]});
        spZone.setAdapter(adapterb);
        spZone.setEnabled(false);
        //----------------
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
        final String OSID = osid;
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString();
                String password = etPassword.getText().toString();
                if(name.length()<4){
                    etName.setError(contextx.getString(R.string.str_cm_create_tips_error_name));
                    return;
                }if(password.length()<8){
                    etPassword.setError(contextx.getString(R.string.str_cm_create_tips_error_password));
                    return;
                }
                //开始创建
                String zoneid = spZone.getSelectedItem().toString().split("@")[1];
                String CPU = spRecorce.getSelectedItem().toString().split("@")[1].split(",")[0];
                String MEM = spRecorce.getSelectedItem().toString().split("@")[1].split(",")[1];
                APIRG = new APIRequestGenerator(defaulyketId,defaultkey);
                String CreateURL = "https://"+APIRG.cvm_createNewInstanceWithUserImage(zoneid,CPU,MEM,OSID,DataDiskSize,name,password,SysDiskSize,Bandwidth);
                Log.v("create-URL=",CreateURL);
                new CreateNewInstancefORuSERiMAGE().execute(CreateURL);
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

    //用于创建云服务器: 使用私有镜像
    private class CreateNewInstancefORuSERiMAGE extends AsyncTask<String, Void, String> {
        final ProgressDialog loading= new ProgressDialog(ct);
        @Override
        protected String doInBackground(String[] params) {
            //开始向腾讯请求实例列表
            WebClient wb = new WebClient();
            String resultstr = new String();
            try {
                resultstr = wb.getContent(params[0], "utf-8", "utf-8");
            }catch (IOException e){
                Log.v("IO Exception=",e.getMessage());
                loading.dismiss();
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
                    Toast.makeText(ct,"错误："+resMsg,Toast.LENGTH_LONG).show();
                    loading.dismiss();
                    return;
                }
                //继续解析
            }catch (JSONException e){
                Log.v("JSON-ERROR=",e.getMessage());
                Snackbar.make(globeView,"JSON解析错误！",Snackbar.LENGTH_LONG).show();
                loading.dismiss();
                return;
            }
            loading.dismiss();
            //Snackbar.make(,"创建成功，请刷新(等待3分钟，否则会出错)！",Snackbar.LENGTH_LONG).show();
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

    //用于获取镜像列表
    private class CreateNew extends AsyncTask<String, Void, String> {
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
                loading.dismiss();
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
                    Toast.makeText(ct,"错误："+resMsg,Toast.LENGTH_LONG).show();
                    loading.dismiss();
                    return;
                }
            }catch (JSONException e){
                Log.v("JSON-ERROR=",e.getMessage());
                Snackbar.make(globeView,"JSON解析错误！",Snackbar.LENGTH_LONG).show();
                loading.dismiss();
                return;
            }
            loading.dismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setMessage("执行中...");
            loading.show();
            loading.setCancelable(false);
            loading.setCanceledOnTouchOutside(false);
        }
    }


}
