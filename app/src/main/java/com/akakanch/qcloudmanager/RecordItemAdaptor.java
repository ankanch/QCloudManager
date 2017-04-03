package com.akakanch.qcloudmanager;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_record_item, parent, false);
        }
        TextView tvName = (TextView)convertView.findViewById(R.id.textView_recordname);
        TextView tvValue = (TextView)convertView.findViewById(R.id.textView_recordvalue);
        TextView tvType = (TextView)convertView.findViewById(R.id.textView_recordtype);
        ImageButton btnEdit = (ImageButton)convertView.findViewById(R.id.imageButton_edit);
        tvName.setText( recordItem.name);
        tvValue.setText("" + recordItem.value);
        tvType.setText("\t" + recordItem.type);
        final View globView = convertView;
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(globView,"change button clicked.", Snackbar.LENGTH_LONG).show();
            }
        });

        return convertView;
    }
}
