package com.akakanch.qcloudmanager;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Long Zhang on 2017/3/26.
 */

public class CloudServerItemAdapter extends ArrayAdapter<CloudServerItem> {
    public CloudServerItemAdapter(Context context, ArrayList<CloudServerItem> users) {
        super(context, 0, users);
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
        ImageView ivOS = (ImageView)convertView.findViewById(R.id.imageView_os_img);
        Button bMenu = (Button)convertView.findViewById(R.id.button_item_menu);
        //设置数据
        tvName.setText(cvmItem.InstanceName);
        tvIp.setText(cvmItem.InstanceIP);
        tvOS.setText(cvmItem.InstanceOS);
        tvStatus.setText(cvmItem.Status);
        tvPayMode.setText(cvmItem.PayMode);
        ivOS.setImageResource(cvmItem.ImageID);
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
                                Snackbar.make((View)globeView.getParent(),getContext().getString(R.string.str_cm_contentmenu_tips_booting),Snackbar.LENGTH_LONG).show();
                               // cvmItem.Status = getContext().getString(R.string.str_cm_statusdes_booting);
                                break;
                            case R.id.menu_cvm_shutdown:
                                URL = "https://" + APIRG.cvm_shutdownInstance(cvmItem.InstanceID,cvmItem.InstanceRegion);
                                Log.v("SHUTDOWN=",URL);
                                Snackbar.make((View)globeView.getParent(),getContext().getString(R.string.str_cm_contentmenu_tips_shutdown),Snackbar.LENGTH_LONG).show();
                                //cvmItem.Status = getContext().getString(R.string.str_cm_statusdes_shutdowning);
                                break;
                            case R.id.menu_cvm_returninstance:
                                break;
                            case R.id.menu_cvm_resetpassword:
                                break;
                            case R.id.menu_cvm_reinstallos:
                                break;
                            case R.id.menu_cvm_reboot:
                                String RebootURL = "https://" + APIRG.cvm_rebootInstance(cvmItem.InstanceID,cvmItem.InstanceRegion);
                                Snackbar.make((View)globeView.getParent(),getContext().getString(R.string.str_cm_contentmenu_tips_rebooting),Snackbar.LENGTH_LONG).show();
                                //cvmItem.Status = getContext().getString(R.string.str_cm_statusdes_rebooting);
                                Log.v("REBOOT=",RebootURL);
                                break;
                        }
                        new doManageCVM().execute(URL);
                        //Snackbar.make(curview,cvmItem.InstanceName + "-" + menuItem.getTitle(),Snackbar.LENGTH_LONG ).show();
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
                    Snackbar.make((View)globeView.getParent(),"错误："+resMsg,Snackbar.LENGTH_LONG).show();
                    return;
                }
            }catch (JSONException e){
                Log.v("JSON-ERROR=",e.getMessage());
            }
            //
        }
    }

}
