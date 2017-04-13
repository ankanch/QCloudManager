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
        Button btnWebsite = (Button)globeView.findViewById(R.id.button_open_website);
        btnRequestNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(globeView,getActivity().getString(R.string.str_about_tips_requestnew),Snackbar.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"kanchisme@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "[QCloudManager - Request New Function]");
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        btnBugReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(globeView,getActivity().getString(R.string.str_about_tips_bugreport),Snackbar.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"kanchisme@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "[QCloudManager - Bug Report]");
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.akakanch.qcloudmanager2"));
                startActivity(browserIntent);
            }
        });
        btnWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://akakanch.com/projects/qcloudmanager"));
                startActivity(browserIntent);
            }
        });
    }
}
