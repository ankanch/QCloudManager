package com.akakanch.qcloudmanager;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Long Zhang on 2017/3/22.
 */

public class CloudserverManager extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            //        return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.layout_cloudserver,container,false);
    }
}
