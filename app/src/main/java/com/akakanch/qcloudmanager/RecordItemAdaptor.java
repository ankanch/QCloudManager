package com.akakanch.qcloudmanager;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Long Zhang on 2017/3/31.
 */

public class RecordItemAdaptor extends ArrayAdapter<RecordItem> {

    public RecordItemAdaptor(Context context, ArrayList<RecordItem> users) {
        super(context, 0, users);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //获取指定项数据
        final RecordItem recordItem = getItem(position);
        //检查视图是否被复用，否则用view填充
        if (convertView == null) {
            //convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_cloudserver_item_row, parent, false);
        }

        return convertView;
    }
}
