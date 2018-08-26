package com.example.sign.Util;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2018/8/26 0026.
 */
public class MyApplication extends Application {
        private static Context context ;
        @Override
        public void onCreate() {
            context = getApplicationContext() ;
        }
        public static Context getContext(){
            return context ;
        }
}
