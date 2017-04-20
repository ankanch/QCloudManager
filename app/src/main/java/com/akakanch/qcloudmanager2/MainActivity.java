package com.akakanch.qcloudmanager2;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "请添加密钥！", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        IndexPage ip = new IndexPage();
        fragmentTransaction.replace(R.id.content_main,ip);
        fragmentTransaction.commit();
        this.setTitle(R.string.str_ma_title_index);
        fab.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        int id = item.getItemId();

        if (id == R.id.nav_index) {
            // 切换到主页面（默认关于页面）
            IndexPage ip = new IndexPage();
            fragmentTransaction.replace(R.id.content_main,ip);
            this.setTitle(R.string.str_ma_title_index);
            fab.setVisibility(View.INVISIBLE);
        } else if (id == R.id.nav_cloudserver) {
            //切换到云服务器管理页面
            CloudserverManager cm = new CloudserverManager();
            fragmentTransaction.replace(R.id.content_main,cm);
            this.setTitle(R.string.str_ma_title_cloudserver);
            fab.setVisibility(View.VISIBLE);
        }else if (id == R.id.nav_imagemanage) {
            //切换到云服务器管理页面
            Toast.makeText(this, "当前只支持北京，上海，香港，广州，新加坡这个5个地域的镜像。", Toast.LENGTH_SHORT).show();
            SystemImageInspector sii = new SystemImageInspector();
            fragmentTransaction.replace(R.id.content_main, sii);
            this.setTitle(R.string.str_sii_title);
            fab.setVisibility(View.INVISIBLE);
        } else if (id == R.id.nav_domain) {
            //切换到域名管理页面
            DomainManager dm = new DomainManager();
            fragmentTransaction.replace(R.id.content_main, dm);
            this.setTitle(R.string.str_ma_title_domain);
            fab.setVisibility(View.INVISIBLE);
        }else if (id == R.id.nav_about) {
            AboutDialog ad = new AboutDialog();
            fragmentTransaction.replace(R.id.content_main, ad);
            //fragmentTransaction.addToBackStack(null);
            this.setTitle(R.string.drawer_about);
            fab.setVisibility(View.INVISIBLE);
        } else if (id == R.id.nav_view) {

        }

        fragmentTransaction.commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openWebPage(View v){
        Snackbar.make(v,getString(R.string.str_ip_open_url),Snackbar.LENGTH_LONG).show();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.qcloud.com/login?s_url=https%3A%2F%2Fconsole.qcloud.com%2Fcapi"));
        startActivity(browserIntent);
    }
}
