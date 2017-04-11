package com.akakanch.qcloudmanager2;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Long Zhang on 4/10/2017.
 */

public class AboutDialog extends Fragment {

    private View globeView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_about,container,false);
    }

    @Override
    public void onStart() {
        super.onStart();
        globeView = getView();
        Button btnRequestNew  = (Button)globeView.findViewById(R.id.button_request_new_function);
        Button btnBugReport = (Button)globeView.findViewById(R.id.button_submit_bug);
        Button btnRating = (Button)globeView.findViewById(R.id.button_rating_app_in_market);
        btnRequestNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(globeView,"test",Snackbar.LENGTH_LONG).show();
                //Intent browserIntent = new Intent(Intent.ACTION_SEND, Uri.parse("https://www.qcloud.com/login?s_url=https%3A%2F%2Fconsole.qcloud.com%2Fcapi"));
                //startActivity(browserIntent);
                //发送邮件
            }
        });
        btnBugReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //提交bug，通过网页
            }
        });
        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打开market评分
            }
        });
    }
}
