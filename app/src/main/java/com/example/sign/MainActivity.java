package com.example.sign;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sign.R;
import com.example.sign.Util.MyApplication;
import com.example.sign.Util.Util;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText registerSno ;
    private EditText registerPwd ;
    private Button registerOk ;
    private CheckBox registerMind ;
    private CheckBox registerAuto ;
    private String sno ;
    private String passward ;
    private boolean mind ;
    private boolean auto ;
    //进度条
    private ProgressDialog progressDialog ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //背景图片和系统状态栏融合
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView() ;
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        //加载控件
        registerSno = (EditText)findViewById(R.id.register_sno) ;
        registerPwd = (EditText)findViewById(R.id.register_passward) ;
        registerOk = (Button)findViewById(R.id.register_ok) ;
        registerMind = (CheckBox)findViewById(R.id.register_mind) ;
        registerAuto = (CheckBox)findViewById(R.id.register_auto) ;
        //从本地读取缓存
        contains();
        //如果选中自动登陆，就会自动选择记住密码
        registerAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerMind.setChecked(true);
            }
        });
        //如果取消记住密码，就会自动取消自动登陆
        registerMind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!registerMind.isChecked()){
                    registerAuto.setChecked(false);
                }
            }
        });
        //登陆
        registerOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sno = registerSno.getText().toString() ;
                String passward = registerPwd.getText().toString() ;
                boolean mind = registerMind.isChecked() ;
                boolean auto = registerAuto.isChecked() ;
                if (sno.equals("")){
                    Toast.makeText(MainActivity.this,"请输入账号！",Toast.LENGTH_SHORT).show();
                    return;
                }else if (passward.equals("")){
                    Toast.makeText(MainActivity.this,"请输入密码！",Toast.LENGTH_SHORT).show();
                    return;
                }
                register();
            }
        });
    }
    /**
     * 登陆
     */
    public void register(){
        sno = registerSno.getText().toString() ;
        passward = registerPwd.getText().toString() ;
        mind = registerMind.isChecked() ;
        auto = registerAuto.isChecked() ;
        //将账号和密码和服务器上传服务器匹配
        checkRegister(new Callback() {
            //具体的Callback
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        registerPwd.setText("");
                        Toast.makeText(MainActivity.this,"连接服务器出错，请重新登陆！",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string() ;
                if("false".equals(result)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            registerPwd.setText("");
                            Toast.makeText(MyApplication.getContext(),"账号或密码错误，请重新登陆！",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                }else if ("true".equals(result)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    });
                    if (mind){
                        SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit() ;
                        editor.putString("registerSno",sno) ;
                        editor.putString("registerPwd",passward) ;
                        editor.putBoolean("registerMind",mind) ;
                        editor.putBoolean("registerAuto",auto) ;
                        editor.apply();
                    }else {
                        SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit() ;
                        editor.putString("registerSno",sno) ;
                        editor.apply();
                    }
                    skipActivity();
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            registerPwd.setText("");
                            Toast.makeText(MyApplication.getContext(),"出现未知错误，请重新登陆！",Toast.LENGTH_SHORT).show();
                            Log.d("Main",result) ;
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        });
    }
    /**
     * 上传账号密码进行匹配
     */
    public void checkRegister(okhttp3.Callback callback){
        //显示登陆进度提示
        progressDialog = new ProgressDialog(MainActivity.this) ;
        progressDialog.setMessage("正在登陆");
        progressDialog.setCancelable(false);
        progressDialog.show();

        OkHttpClient client = new OkHttpClient() ;
        RequestBody requestBody = new FormBody.Builder().add("sno",sno).add("passward",passward).build() ;
        Request request = new Request.Builder().url(Util.serviceUrl + "android/checkRegister").post(requestBody).build();
        client.newCall(request).enqueue(callback);
    }
    /**
     * 密码和账号匹配成功，跳转活动
     */
    public void skipActivity(){
        Intent intent = new Intent(this,ManagerActivity.class) ;
        startActivity(intent);
        this.finish();
    }
    /**
     * 搜索本地的账号密码以及登陆设置
     */
    public void contains(){
        SharedPreferences prefs = getSharedPreferences("data",MODE_PRIVATE) ;
        String snos = prefs.getString("registerSno",null) ;
        String pwds = prefs.getString("registerPwd",null) ;
        boolean minds = prefs.getBoolean("registerMind",false) ;
        boolean autos = prefs.getBoolean("registerAuto",false) ;
        if (minds){
            registerSno.setText(snos);
            registerPwd.setText(pwds);
            registerMind.setChecked(true);
        }
        if (autos){
            register();
        }
    }

}
