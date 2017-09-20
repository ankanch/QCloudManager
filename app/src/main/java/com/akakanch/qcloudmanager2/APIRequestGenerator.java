package com.akakanch.qcloudmanager2;

import android.util.Base64;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Long Zhang on 2017/3/24.
 */

public class APIRequestGenerator {
    //储存API可以value 和APIKey的ID
    private String APIkeyId = new String();
    private String APIkey = new String();

    //腾讯云服务地域
    public static String[] REGION_NAME = {"北京","上海","广州","香港","新加坡","北美"};
    public static String[] REGION= {"bj","sh","gz","hk","sg","ca",} ;

    public APIRequestGenerator(){
        APIkeyId = "NULL";
        APIkey = "NULL";
    }

    public APIRequestGenerator(String keyId,String keyValue){
        APIkeyId = keyId.replace("\n","").replace("\r","");
        APIkey = keyValue.replace("\n","").replace("\r","");
    }

    //根据给定map生成字典顺序排序后的请求头
    //返回一个包含了未编码(0)和编码后(1)的请求头数组,括号中为位置
    private String[] generatePublicRequestParameters(Map<String,String> vlist){
        String requestHeadEncoded = new String();  //储存编码后的参数列表
        String requestHead = new String();  //储存原始参数列表
        //按照字典顺序遍历构造请求参数列表
        SortedSet<String> keys = new TreeSet<String>(vlist.keySet());
        for (String key : keys) {
            String value = vlist.get(key);
            //编码并构造请求参数列表
            value = value.replace("_",".");  //下划线转换https://www.qcloud.com/document/api/377/4214
            requestHead = requestHeadEncoded + key + "=" + value + "&";
            try{
                if(!isChinese(value)) {
                    requestHeadEncoded = requestHeadEncoded + key + "=" + URLEncoder.encode(value, "UTF-8") + "&";
                }else{
                    requestHeadEncoded = requestHeadEncoded + key + "=" + value + "&";
                }
            }catch (UnsupportedEncodingException e){
                Log.v("ERROR-ENCODING=",e.getMessage());
            }
        }
        //去除最后一个&
        requestHead = requestHead.substring(0,requestHead.length()-1);
        requestHeadEncoded = requestHeadEncoded.substring(0,requestHeadEncoded.length()-1);
        return new String[]{requestHead,requestHeadEncoded};
    }

    //这个函数返回的string用于生成签名
    //该函数返回的是加上了请求路径和请求方法之后的请求
    private String generateRequestString(String raw,String request_url){
        String requestString = new String();
        requestString = "GET" + request_url + raw;
        return requestString;
    }

    //该函数返回可用于请求的URL（浏览器可以直接访问）
    private String generateRequestURL(String raw,String request_url){
        String requestURL = new String();
        requestURL = request_url + raw;
        return requestURL;
    }

    //用于对上一个函数返回的string进行签名
    private String HmacSHA256Encode(String secret,String message){
        String enstr = new String();
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            enstr = Base64.encodeToString(sha256_HMAC.doFinal(message.getBytes()),Base64.DEFAULT);
        }
        catch (Exception e){
            System.out.println("Error");
        }
        return enstr.replace("\n","");
    }

    private static final boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    //判断是否有非英文字符
    private static final boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    //该函数用于测试生成签名是否规范，编码是否和腾讯一致
    //https://www.qcloud.com/document/api/377/4214
    public String test_encode(){
        String teststr = "Action=DescribeInstances&Nonce=11886&Region=gz&SecretId=AKIDz8krbsJ5yKBZQpn74WFkmLPx3gnPhESA&SignatureMethod=HmacSHA256&Timestamp=1465185768&instanceIds.0=ins-09dx96dg&limit=20&offset=0";
        teststr = "GETcvm.api.qcloud.com/v2/index.php?"  + teststr;
        Log.v("API_Gen_Test-reqStr=",teststr);
        String enc = HmacSHA256Encode("Gu5t9xGARNpq86cd98joQYCN3Cozk1qA",teststr);
        Log.v("API_Gen_Test-Singture=",enc);
        try {
            enc = URLEncoder.encode(enc, "UTF-8");
        }catch (UnsupportedEncodingException e){
            Log.v("error-coding",e.getMessage());
        }
        Log.v("API_Gen_Test-SigEncode=",enc);
        return enc;
    }

    //域名管理：获取解析记录列表
    public String domian_getRecordList(String domain){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","RecordList");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("SignatureMethod","HmacSHA256");
        para.put("domain",domain.replace("www.","").replace("http://",""));
        //para.put("recordType","A");
        para.put("offset","0");
        para.put("length","20");
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"cns.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"cns.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //域名管理：获取域名列表
    public String domain_getDomainList(){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","DomainList");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("SignatureMethod","HmacSHA256");
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"cns.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"cns.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //域名管理：添加域名
    public String domain_addDomain(String domainname){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","DomainCreate");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("SignatureMethod","HmacSHA256");
        para.put("domain",domainname);
        para.put("projectId","0");
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"cns.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"cns.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //域名管理：添加域名解析记录
    public String domian_addRecord(String domain,String subdomian,String recordType,String recordLine,String value,String mxpriority){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","RecordCreate");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("SignatureMethod","HmacSHA256");
        para.put("domain",domain.replace("www.","").replace("http://",""));
        para.put("subDomain",subdomian);
        para.put("recordType",recordType);
        para.put("recordLine",recordLine);
        para.put("value",value);
        if(recordType.equals("MX")){
            para.put("mx",mxpriority);
        }
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"cns.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"cns.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //域名管理：修改域名解析记录
    public String domian_changeRecord(String domain,String recordId,String subdomian,String recordType,String recordLine,String value){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","RecordModify");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("SignatureMethod","HmacSHA256");
        para.put("domain",domain.replace("www.","").replace("http://",""));
        para.put("recordId",recordId);
        para.put("subDomain",subdomian);
        para.put("recordType",recordType);
        para.put("recordLine",recordLine);
        para.put("value",value);
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"cns.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"cns.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //域名管理：删除域名解析记录
    public String domian_deleteRecord(String domain,String recordId){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","RecordDelete");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(888888)));
        para.put("SecretId",APIkeyId);
        para.put("SignatureMethod","HmacSHA256");
        para.put("domain",domain.replace("www.","").replace("http://",""));
        para.put("recordId",recordId);
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"cns.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"cns.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //本函数用来获取所有的实例列表（一次性）
    public String[] cvm_getAllInstanceList(){
        String[] REGION_LIST= {"bj","sh","gz","hk","sg","ca"} ;
        String[] urllist = new String[REGION_LIST.length];
        int i =0;
        for(String rg : REGION_LIST){
            String url = "https://" + cvm_getInstanceList(rg);
            urllist[i] = url;
            i++;
        }
        return urllist;
    }

    //云服务器管理：获取实例列表
    public String cvm_getInstanceList(String Region){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","DescribeInstances");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("SignatureMethod","HmacSHA256");
        para.put("Region",Region);
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"cvm.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"cvm.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //云服务器管理：启动实例
    public String cvm_bootInstance(String instanceID,String Region){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","StartInstances");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("Region",Region);
        para.put("SignatureMethod","HmacSHA256");
        para.put("instanceIds.0",instanceID);
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"cvm.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"cvm.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //云服务器管理：关闭实例（关机）
    public String cvm_shutdownInstance(String instanceID,String Region){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","StopInstances");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("Region",Region);
        para.put("SignatureMethod","HmacSHA256");
        para.put("instanceIds.0",instanceID);
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"cvm.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"cvm.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //云服务器管理：修改实例名称
    public String cvm_rename(String instanceID,String Region,String NewName){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","ModifyInstanceAttributes");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("instanceName",NewName);
        para.put("Region",Region);
        para.put("SignatureMethod","HmacSHA256");
        para.put("instanceId",instanceID);
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"cvm.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"cvm.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //云服务器管理：重启实例
    public String cvm_rebootInstance(String instanceID,String Region){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","RestartInstances");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("Region",Region);
        para.put("SignatureMethod","HmacSHA256");
        para.put("instanceIds.0",instanceID);
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"cvm.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"cvm.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //云服务器管理：重装系统
    public String cvm_reinstallInstance(String instanceID,String osid,String Region,String password){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","ResetInstances");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("Region",Region);
        para.put("SignatureMethod","HmacSHA256");
        para.put("instanceId",instanceID);
        para.put("imageType","2");
        para.put("imageId",osid);
        para.put("password",password);
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"cvm.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"cvm.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //云服务器管理：重置实例密码
    public String cvm_resetInstancePassword(String instanceID,String newPassword,String region){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","ResetInstancePassword");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("SignatureMethod","HmacSHA256");
        para.put("instanceIds.0",instanceID);
        para.put("password",newPassword);
        para.put("Region",region);
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"cvm.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"cvm.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //云服务器管理：退还实例
    public String cvm_returnInstance(String instanceID,String region){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","ReturnInstance");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("SignatureMethod","HmacSHA256");
        para.put("instanceId",instanceID);
        para.put("Region",region);
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"cvm.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"cvm.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //云服务器管理：创建新实例：按量使用
    public String cvm_createNewInstance(String zoneid,String cpu,String mem,String imageid,String storageSize,String instanceName,String password,String rootSize,String bandwidth){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","RunInstancesHour");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("SignatureMethod","HmacSHA256");
        para.put("zoneId",zoneid);
        para.put("cpu",cpu);
        para.put("mem",mem);
        para.put("imageId",imageid);
        para.put("storageSize",storageSize);
        para.put("instanceName",instanceName);
        para.put("bandwidth",bandwidth);
        para.put("password",password);
        para.put("bandwidthType","PayByTraffic"); //设置为按流量计费
        para.put("storageType","2");  //使用云端硬盘
        para.put("rootSize",rootSize);
        String region = new String();
        String[] regionlist = new String[]{"bj","gz","gz","sh","hk","ca"};
        String[] zonelist = new String[]{"800001","100003","100002","200001","300001","400001"};
        int pl = 0;
        for(String zid : zonelist){
            if(zid.equals(zoneid)){
                break;
            }
            pl++;
        }
        para.put("Region",regionlist[pl]);
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"cvm.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"cvm.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //云服务器管理：创建新实例：使用私有镜像创建
    public String cvm_createNewInstanceWithUserImage(String zoneid,String cpu,String mem,String imageid,String storageSize,String instanceName,String password,String rootSize,String bandwidth){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","RunInstancesHour");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("SignatureMethod","HmacSHA256");
        para.put("zoneId",zoneid);
        para.put("cpu",cpu);
        para.put("mem",mem);
        para.put("imageType","1");
        para.put("imageId",imageid);
        para.put("storageSize",storageSize);
        para.put("instanceName",instanceName);
        para.put("bandwidth",bandwidth);
        para.put("password",password);
        para.put("bandwidthType","PayByTraffic"); //设置为按流量计费
        para.put("storageType","2");  //使用云端硬盘
        para.put("rootSize",rootSize);
        String region = new String();
        String[] regionlist = new String[]{"bj","gz","gz","sh","hk","ca"};
        String[] zonelist = new String[]{"800001","100003","100002","200001","300001","400001"};
        int pl = 0;
        for(String zid : zonelist){
            if(zid.equals(zoneid)){
                break;
            }
            pl++;
        }
        para.put("Region",regionlist[pl]);
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"cvm.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"cvm.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //云服务器管理：创建新实例：获取可用区(暂时不使用)
    public String cvm_loadAvaiableZone(){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","DescribeAvailabilityZones");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("Region","bj");
        para.put("SignatureMethod","HmacSHA256");
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"cvm.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"cvm.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    public String[] systemimage_retriveAllImage(){
        String[] REGION= {"bj","sh","gz","hk","sg"} ;
        String[] urls = new String[5];
        int i = 0;
        for(String pos : REGION){
            String url = "https://" + systemimage_retriveImage(pos);
            urls[i] = url;
            i++;
        }
        return urls;
    }

    private String systemimage_retriveImage(String region){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","DescribeImages");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("SignatureMethod","HmacSHA256");
        para.put("imageType","1");   //只显示私有镜像
        para.put("Region",region);
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"image.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"image.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //删除镜像
    public String systemimage_deleteImage(String id,String region){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","DeleteImages");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("SignatureMethod","HmacSHA256");
        para.put("imageIds.0",id);
        para.put("Region",region);
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"image.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"image.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

    //创建自定义镜像
    public String systemimage_createImage(String instanceId,String region,String imageName,String des){
        Map<String,String> para = new HashMap<String, String>();
        para.put("Action","CreateImage");
        para.put("Timestamp",new String().valueOf(System.currentTimeMillis()/1000));
        para.put("Nonce",new String().valueOf(new Random().nextInt(88888)));
        para.put("SecretId",APIkeyId);
        para.put("SignatureMethod","HmacSHA256");
        para.put("instanceId",instanceId);
        para.put("Region",region);
        para.put("imageName",imageName);
        para.put("imageDescription",des);
        String[] requestlist = generatePublicRequestParameters(para);
        Log.v("raw_para_str=",requestlist[0]);
        String requestString = generateRequestString(requestlist[0],"image.api.qcloud.com/v2/index.php?");
        String singuture = HmacSHA256Encode(APIkey,requestString);
        Log.v("Singuture=",singuture);
        //编码
        try {
            singuture = URLEncoder.encode(singuture, "UTF-8");
            Log.v("Singuture-encode=",singuture);
        }catch (UnsupportedEncodingException e){
            Log.v("ERROR:ENCODING",e.getMessage());
        }
        //添加签名在尾部
        requestlist[1] = generateRequestURL(requestlist[1],"image.api.qcloud.com/v2/index.php?");
        requestlist[1] += "&Signature=" + singuture;
        return requestlist[1];
    }

}
