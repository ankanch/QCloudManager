package com.akakanch.qcloudmanager2;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Long Zhang on 4/13/2017.
 */

public class SystemImageItemAdaptor extends ArrayAdapter<SystemImageItem> {
    private Context ct;

    public SystemImageItemAdaptor(Context context, ArrayList<SystemImageItem> users) {
        super(context, 0, users);
        ct = context;
    }

    private View globeView;

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //获取指定项数据
        final SystemImageItem imageItem = getItem(position);
        //检查视图是否被复用，否则用view填充
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_systemimage_item_row, parent, false);
        }
        //设置关键变量
        final TextView tvName = (TextView)convertView.findViewById(R.id.textView_image_name);
        final TextView tvID = (TextView)convertView.findViewById(R.id.textView_imgae_id);
        final TextView tvCreateTime = (TextView)convertView.findViewById(R.id.textView_image_createtime);
        final TextView tvOS = (TextView)convertView.findViewById(R.id.textView_image_operatingsystem);
        final TextView tvDescription = (TextView)convertView.findViewById(R.id.textView_image_description);
        final Button btnMenu = (Button)convertView.findViewById(R.id.button_systemimage_menu);
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
                                break;
                            case R.id.menu_systemimage_delete:
                                break;
                        }
                        return false;
                    }
                });
                pm.show();
            }
        });
        //

        return convertView;
    }




}
