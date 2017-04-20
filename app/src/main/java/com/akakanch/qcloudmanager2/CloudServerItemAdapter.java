package com.akakanch.qcloudmanager2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Long Zhang on 2017/3/26.
 */

public class CloudServerItemAdapter extends ArrayAdapter<CloudServerItem> {

    private Context ct;

    public CloudServerItemAdapter(Context context, ArrayList<CloudServerItem> users) {
        super(context, 0, users);
        ct = context;
    }
    private View globeView;

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //获取指定项数据
        final CloudServerItem cvmItem = getItem(position);
        //检查视图是否被复用，否则用view填充
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_cloudserver_item_row, parent, false);
        }
        //获取view上的数据项
        TextView tvName = (TextView)convertView.findViewById(R.id.textView_instanceName);
        TextView tvIp = (TextView)convertView.findViewById(R.id.textView_ipaddress);
        TextView tvOS = (TextView)convertView.findViewById(R.id.textView_os);
        TextView tvStatus = (TextView)convertView.findViewById(R.id.textView_status);
        TextView tvPayMode = (TextView)convertView.findViewById(R.id.textView_paymode);
        TextView tvRegionName = (TextView)convertView.findViewById(R.id.textView_regionName);
        ImageView ivOS = (ImageView)convertView.findViewById(R.id.imageView_os_img);
        Button bMenu = (Button)convertView.findViewById(R.id.button_item_menu);
        //设置数据
        tvName.setText(cvmItem.InstanceName);
        tvIp.setText(cvmItem.InstanceIP);
        tvOS.setText(cvmItem.InstanceOS);
        tvStatus.setText(cvmItem.Status);
        tvPayMode.setText(cvmItem.PayMode);
        ivOS.setImageResource(cvmItem.ImageID);
        tvRegionName.setText(cvmItem.RegionName);
        //设置菜单
        globeView = convertView;
        final View curview = convertView;
        final View buttonView = (View)bMenu;
        final APIRequestGenerator APIRG = new APIRequestGenerator(cvmItem.APIKeyID,cvmItem.APIKey);
        bMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu pm = new PopupMenu(getContext(),buttonView);
                pm.getMenuInflater().inflate(R.menu.cloudserver_context_menu, pm.getMenu());
                //不能删除包年包月的服务
                if(cvmItem.PayMode.equals(getContext().getString(R.string.str_cm_paymode_payfirst))) {
                    pm.getMenu().getItem(5).setVisible(false);
                }
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        String URL = new String();
                        switch(menuItem.getItemId()){
                            case R.id.menu_cvm_start:
                                URL = "https://" + APIRG.cvm_bootInstance(cvmItem.InstanceID,cvmItem.InstanceRegion);
                                Log.v("START=",URL);
                                Snackbar.make(globeView,getContext().getString(R.string.str_cm_contentmenu_tips_booting),Snackbar.LENGTH_LONG).show();
                                Log.v("start-id=",cvmItem.InstanceID);
                                break;
                            case R.id.menu_cvm_shutdown:
                                URL = "https://" + APIRG.cvm_shutdownInstance(cvmItem.InstanceID,cvmItem.InstanceRegion);
                                Log.v("SHUTDOWN=",URL);
                                Snackbar.make(globeView,getContext().getString(R.string.str_cm_contentmenu_tips_shutdown),Snackbar.LENGTH_LONG).show();
                                Log.v("shutdown-id=",cvmItem.InstanceID);
                                break;
                            case R.id.menu_cvm_returninstance:
                                URL = "https://" + APIRG.cvm_returnInstance(cvmItem.InstanceID,cvmItem.InstanceRegion);
                                Log.v("returnURL=",URL);
                                Snackbar.make(globeView,getContext().getString(R.string.str_cm_contentmenu_tips_return),Snackbar.LENGTH_LONG).show();
                                break;
                            case R.id.menu_cvm_resetpassword: {
                                if(!cvmItem.Status.equals(getContext().getString(R.string.str_cm_statusdes_shutdown))){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setMessage("只能在关机状态下修改密码！").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            }).show();
                                    break;
                                }
                                LayoutInflater li = LayoutInflater.from(getContext());
                                View changeDlgView = li.inflate(R.layout.layout_cloudserver_changepassword, null);
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setView(changeDlgView);
                                final EditText pwd = (EditText) changeDlgView.findViewById(R.id.editText_password_changepwd);
                                final Button confrim = (Button) changeDlgView.findViewById(R.id.button_confirm_changepwd);
                                final Button cancel = (Button) changeDlgView.findViewById(R.id.button_cancel_changepwd);
                                final AlertDialog dlg = builder.show();
                                dlg.setCanceledOnTouchOutside(false);
                                confrim.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String password = pwd.getText().toString();
                                        if(password.length()<8){
                                            Snackbar.make(globeView,"密码无效！请输入长度大于8的密码。",Snackbar.LENGTH_LONG).show();
                                            return;
                                        }
                                        String changepwd = "https://" + APIRG.cvm_resetInstancePassword(cvmItem.InstanceID,password,cvmItem.InstanceRegion);
                                        Log.v("changepwdURL=",changepwd);
                                        Log.v("resetpwd-id=",cvmItem.InstanceID);
                                        new doManageCVM().execute(changepwd);
                                        dlg.dismiss();
                                    }
                                });
                                cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dlg.cancel();
                                    }
                                });
                            }
                                break;
                            case R.id.menu_cvm_reinstallos:
                                LayoutInflater li = LayoutInflater.from(getContext());
                                View changeDlgView = li.inflate(R.layout.layout_cloudserver_reinstall, null);
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setView(changeDlgView);
                                final EditText pwd = (EditText)changeDlgView.findViewById(R.id.editText_password_reinstall);
                                final Spinner os = (Spinner)changeDlgView.findViewById(R.id.spinner_type_reinstall);
                                final Button confrim = (Button)changeDlgView.findViewById(R.id.button_confirm_reinstall);
                                final Button cancel = (Button)changeDlgView.findViewById(R.id.button_cancel_reintall);
                                final AlertDialog dlg = builder.show();
                                dlg.setCanceledOnTouchOutside(false);
                                confrim.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String password = pwd.getText().toString();
                                        if(password.length()<8){
                                            Snackbar.make(globeView,"密码无效！请输入长度大于8的密码。",Snackbar.LENGTH_LONG).show();
                                            return;
                                        }
                                        String ostype = os.getSelectedItem().toString().split("@")[1];
                                        String reinstallURL = "https://"+APIRG.cvm_reinstallInstance(cvmItem.InstanceID,ostype,cvmItem.InstanceRegion,password);
                                        Log.v("reinstallURL=",reinstallURL);
                                        Log.v("reinstall-id=",cvmItem.InstanceID);
                                        new doManageCVM().execute(reinstallURL);
                                        dlg.cancel();
                                    }
                                });
                                cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dlg.cancel();
                                    }
                                });
                                break;
                            case R.id.menu_cvm_reboot:
                                String RebootURL = "https://" + APIRG.cvm_rebootInstance(cvmItem.InstanceID,cvmItem.InstanceRegion);
                                Snackbar.make((View)globeView.getParent(),getContext().getString(R.string.str_cm_contentmenu_tips_rebooting),Snackbar.LENGTH_LONG).show();
                                Log.v("REBOOT=",RebootURL);
                                break;
                            case R.id.menu_cvm_createimage:
                                if(!cvmItem.Status.equals(getContext().getString(R.string.str_cm_statusdes_shutdown))){
                                    AlertDialog.Builder buildert = new AlertDialog.Builder(getContext());
                                    buildert.setMessage("只能在关机状态下创建镜像！").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }).show();
                                    break;
                                }
                                //
                                LayoutInflater lii = LayoutInflater.from(getContext());
                                View changeDlgViewx = lii.inflate(R.layout.layout_create_image, null);
                                final AlertDialog.Builder builderx = new AlertDialog.Builder(getContext());
                                builderx.setView(changeDlgViewx);
                                final EditText NAME = (EditText) changeDlgViewx.findViewById(R.id.editText_input_imagename);
                                final EditText DES = (EditText) changeDlgViewx.findViewById(R.id.editText_input_imagedes);
                                final Button confrimx = (Button) changeDlgViewx.findViewById(R.id.button_confirm_createimage);
                                final Button cancelx = (Button) changeDlgViewx.findViewById(R.id.button_cancel_createimage);
                                final AlertDialog dlgx = builderx.show();
                                dlgx.setCanceledOnTouchOutside(false);
                                confrimx.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String CreateImgaeURL = "https://"+APIRG.systemimage_createImage(cvmItem.InstanceID,cvmItem.InstanceRegion,NAME.getText().toString(),DES.getText().toString());
                                        Log.v("create-imgae-url=",CreateImgaeURL);
                                        new doManageCVM().execute(CreateImgaeURL);
                                        dlgx.cancel();
                                    }
                                });
                                cancelx.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dlgx.cancel();
                                    }
                                });
                                break;
                        }
                        new doManageCVM().execute(URL);
                        return false;
                    }
                });
                pm.show();
            }
        });
        return convertView;
    }

    //用于执行云服务器管理相关的操作
    //传入参数应该为要请求的URL
    private class doManageCVM extends AsyncTask<String, Void, String> {

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
                    Log.v("error-CSIA=",resMsg);
                    Toast.makeText(ct,"错误："+resMsg,Toast.LENGTH_LONG).show();
                    loading.dismiss();
                    return;
                }
            }catch (JSONException e){
                Log.v("JSON-ERROR=",e.getMessage());
            }
            //
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
