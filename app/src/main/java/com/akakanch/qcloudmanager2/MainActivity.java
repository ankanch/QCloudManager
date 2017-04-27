package com.akakanch.qcloudmanager2;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FloatingActionButton fab;
    private boolean inIndexPage = true;
    private boolean inWorkPage = false;

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
        if(inIndexPage){
            menu.getItem(3).setVisible(true);menu.getItem(2).setVisible(true);
        }else{
            menu.getItem(3).setVisible(false);menu.getItem(2).setVisible(false);
        }
        if(inWorkPage){
            menu.getItem(1).setVisible(true);
        }else{
            menu.getItem(1).setVisible(false);
        }
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
        }else if(id == R.id.action_export_setting){
            //检查权限
            if( ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                Toast.makeText(this,"授权完毕。请再次尝试导出！",Toast.LENGTH_LONG).show();
            }else {
                exportAPIKey();
            }
        }else if(id == R.id.action_import_setting){
            //检查权限
            if( ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                Toast.makeText(this,"授权完毕。请再次尝试导入！",Toast.LENGTH_LONG).show();
            }else {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, 199);
            }
        }else if(id == R.id.action_kanch){
            LayoutInflater li = LayoutInflater.from(this);
            View changeDlgView = li.inflate(R.layout.layout_support, null);
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(changeDlgView);
            final AlertDialog dlg = builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 199 && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                Log.i("IMPORT-FILE-URI=", "Uri: " + uri.getPath());
                importAPIKey(uri.getPath().replace("/document/",""));
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        int id = item.getItemId();
        inIndexPage = false;
        inWorkPage = true;
        if (id == R.id.nav_index) {
            // 切换到主页面（默认关于页面）
            IndexPage ip = new IndexPage();
            fragmentTransaction.replace(R.id.content_main,ip);
            this.setTitle(R.string.str_ma_title_index);
            fab.setVisibility(View.INVISIBLE);
            inIndexPage = true;
            inWorkPage = false;
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
            inWorkPage = false;
        } else if (id == R.id.nav_view) {

        }
        fragmentTransaction.commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        invalidateOptionsMenu();
        return true;
    }

    public void openWebPage(View v){
        Snackbar.make(v,getString(R.string.str_ip_open_url),Snackbar.LENGTH_LONG).show();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.qcloud.com/login?s_url=https%3A%2F%2Fconsole.qcloud.com%2Fcapi"));
        startActivity(browserIntent);
    }

    public void openAlipay(View v){
        Snackbar.make(v,getString(R.string.str_about_thanks4your_support),Snackbar.LENGTH_LONG).show();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://qr.alipay.com/a6x09720wf8ltcafg1vp06a"));
        startActivity(browserIntent);
    }

    public void openPaypal(View v){
        Snackbar.make(v,getString(R.string.str_about_thanks4your_support),Snackbar.LENGTH_LONG).show();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://paypal.me/kanchz"));
        startActivity(browserIntent);
    }

    public void openAirbnb(View v){
        Snackbar.make(v,getString(R.string.str_about_thanks4your_support),Snackbar.LENGTH_LONG).show();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.airbnb.com/c/4f500a"));
        startActivity(browserIntent);
    }

    //导出API数据到外部储存
    public boolean exportAPIKey(){
        FileOutputStream outputStream;
        try {
            File file = new File(Environment.getExternalStorageDirectory(), "QCloudManager");
            if(!file.mkdirs()){
                Log.v("error-creating-dir=", "error");
            }
            outputStream = new FileOutputStream(file+"/apikeydata_on_"+new String().valueOf(System.currentTimeMillis()/1000));
            String defaultkey = read("API_KEY");
            String defaulyketId = read("API_KEY_ID");
            if (defaultkey.equals("NULL") || defaulyketId.equals("NULL")) {
                //不存在，则禁止导出
                return false;
            }
            outputStream.write((defaulyketId+"<@>"+defaultkey).getBytes());
            outputStream.close();
            Toast.makeText(getApplicationContext(),"导出成功，APIKey数据已经导出至：\r\n"+file.getPath(),Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"导出失败，请重试！",Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    //*/从外部储存导入API数据
    public boolean importAPIKey(String filepath){
        BufferedReader inputStream;
        try {
            inputStream = new BufferedReader(new FileReader(filepath));
            String var = ((String)inputStream.readLine()).replace("\r","").replace("\n","");
            inputStream.close();
            Log.v("file-content=",var);
            String defaultkey = new String();
            String defaulyketId = new String();
            String[] x= var.split("<@>");
            defaultkey = x[1];
            defaulyketId = x[0];
            save("API_KEY",defaultkey);
            save("API_KEY_ID",defaulyketId);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            IndexPage ip = new IndexPage();
            fragmentTransaction.replace(R.id.content_main,ip);
            fragmentTransaction.commit();
            this.setTitle(R.string.str_ma_title_index);
            Toast.makeText(this,"导入成功！",Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,"解析失败！可能是文件内容格式不正确！",Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }//*/

    public void save(String key, String value){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String read(String key){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String defaultValue = "NULL";
        String value = sharedPref.getString(key, defaultValue);
        return value;
    }
}
