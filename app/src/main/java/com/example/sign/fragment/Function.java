package com.example.sign.fragment;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.sign.MainActivity;
import com.example.sign.ManagerActivity;
import com.example.sign.Util.Util;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Administrator on 2018/8/27 0027.
 */
public class Function {
    /**
     * 发送二维码至服务器
     * @param callback
     */
    public static void sendRQcode(String action,String sno,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient() ;
        RequestBody requestBody = new FormBody.Builder().add("action",action).add("sno",sno).build() ;
        Request request = new Request.Builder().url(Util.serviceUrl + "android/checkRQcode").post(requestBody).build();
        client.newCall(request).enqueue(callback);
    }
}
