package com.example.sign;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sign.fragment.LeaveFragment;
import com.example.sign.fragment.MyselfFragment;
import com.example.sign.fragment.RecordFragment;
import com.example.sign.fragment.SignFragment;

public class ManagerActivity extends AppCompatActivity implements View.OnClickListener{
    private LinearLayout record ;
    private LinearLayout leave ;
    private LinearLayout sign ;
    private LinearLayout myself ;

    //图片及其文字
    private ImageView record_img ;
    private ImageView leave_img ;
    private ImageView sign_img ;
    private ImageView myself_img ;
    private TextView record_text ;
    private TextView leave_text ;
    private TextView sign_text ;
    private TextView myself_text ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        record = (LinearLayout)findViewById(R.id.manager_record) ;
        leave = (LinearLayout)findViewById(R.id.manager_leave) ;
        sign = (LinearLayout)findViewById(R.id.manager_sign) ;
        myself = (LinearLayout)findViewById(R.id.manager_myself) ;
        //加载图片和文字
        record_img = (ImageView)findViewById(R.id.manager_record_img) ;
        record_text = (TextView)findViewById(R.id.manager_record_text) ;
        leave_img = (ImageView)findViewById(R.id.manager_leave_img) ;
        leave_text = (TextView)findViewById(R.id.manager_leave_text) ;
        sign_img = (ImageView)findViewById(R.id.manager_sign_img) ;
        sign_text = (TextView)findViewById(R.id.manager_sign_text) ;
        myself_img = (ImageView)findViewById(R.id.manager_myself_img) ;
        myself_text = (TextView)findViewById(R.id.manager_myself_text) ;
        //注册监听器
        record.setOnClickListener(this);
        leave.setOnClickListener(this);
        sign.setOnClickListener(this);
        myself.setOnClickListener(this);
        replaceFragment(new RecordFragment());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.manager_record:
                replaceFragment(new RecordFragment());
                restoreColor();
                record_img.setBackgroundColor(Color.parseColor("#00FFFF"));
                record_text.setTextColor(Color.parseColor("#00FFFF"));
                break;
            case R.id.manager_leave:
                replaceFragment(new LeaveFragment());
                restoreColor();
                leave_img.setBackgroundColor(Color.parseColor("#00FFFF"));
                leave_text.setTextColor(Color.parseColor("#00FFFF"));
                break;
            case R.id.manager_sign:
                //replaceFragment(new SignFragment());
                restoreColor();
                sign_img.setBackgroundColor(Color.parseColor("#00FFFF"));
                sign_text.setTextColor(Color.parseColor("#00FFFF"));
                replaceFragment(new SignFragment());
                break;
            case R.id.manager_myself:
                replaceFragment(new MyselfFragment());
                restoreColor();
                myself_img.setBackgroundColor(Color.parseColor("#00FFFF"));
                myself_text.setTextColor(Color.parseColor("#00FFFF"));
                break;
            default:
                break;
        }
    }

    /**
     * 还原所有图片和文字颜色
     */
    private void restoreColor(){
        record_text.setTextColor(Color.parseColor("#404040"));
        record_img.setBackgroundColor(Color.parseColor("#FFFFFF"));
        leave_text.setTextColor(Color.parseColor("#404040"));
        leave_img.setBackgroundColor(Color.parseColor("#FFFFFF"));
        sign_text.setTextColor(Color.parseColor("#404040"));
        sign_img.setBackgroundColor(Color.parseColor("#FFFFFF"));
        myself_text.setTextColor(Color.parseColor("#404040"));
        myself_img.setBackgroundColor(Color.parseColor("#FFFFFF"));
    }

    /**
     * 动态添加碎片
     */
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager() ;
        FragmentTransaction transaction = fragmentManager.beginTransaction() ;
        transaction.replace(R.id.manager_fragment,fragment) ;
        transaction.commit() ;
    }
}
