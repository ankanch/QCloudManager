package com.akakanch.qcloudmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Long Zhang on 2017/3/26.
 */

public class CloudServerItemAdapter extends ArrayAdapter<CloudServerItem> {
    public CloudServerItemAdapter(Context context, ArrayList<CloudServerItem> users) {
        super(context, 0, users);
    }

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
        final View curview = convertView;
        final View buttonView = (View)bMenu;
        bMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu pm = new PopupMenu(getContext(),buttonView);
                pm.getMenuInflater().inflate(R.menu.cloudserver_context_menu, pm.getMenu());
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Snackbar.make(curview,cvmItem.InstanceName + "-" + menuItem.getTitle(),Snackbar.LENGTH_LONG ).show();
                        return false;
                    }
                });
                pm.show();
                Snackbar.make(curview,cvmItem.insid + "\tClicked.",Snackbar.LENGTH_LONG).show();
            }
        });

        return convertView;
    }
}
