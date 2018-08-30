package com.example.sign.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sign.MainActivity;
import com.example.sign.ManagerActivity;
import com.example.sign.PermissionAcitivity;
import com.example.sign.R;
import com.example.sign.Util.MyApplication;
import com.example.sign.fragment.LeaveFragment;
import com.example.sign.fragment.MyselfFragment;
import com.example.sign.fragment.RecordFragment;
import com.example.sign.fragment.SignFragment;
import com.google.zxing.qrcode.encoder.QRCode;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Administrator on 2018/8/27 0027.
 */
public class SignFragment extends Fragment {
    //二维码扫描结果
    private String QRcode ;
    //界面控件
    private Button scanStart ;
    private LinearLayout scanLayout ;
    private TextView scanCno ;
    private TextView scanCname ;
    private TextView scanTname ;
    private TextView scanRweek ;
    private TextView scanRday ;
    private TextView scanRnum ;
    private Button scanAfresh ;
    private Button scanOk ;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manager_sign,container,false) ;
        scanStart = (Button)view.findViewById(R.id.sign_scan_start) ;
        scanLayout = (LinearLayout)view.findViewById(R.id.sign_scan_layout) ;
        scanCno = (TextView)view.findViewById(R.id.sign_scan_cno) ;
        scanCname = (TextView)view.findViewById(R.id.sign_scan_cname) ;
        scanTname = (TextView)view.findViewById(R.id.sign_scan_tname) ;
        scanRweek = (TextView)view.findViewById(R.id.sign_scan_rweek) ;
        scanRday = (TextView)view.findViewById(R.id.sign_scan_rday) ;
        scanRnum = (TextView)view.findViewById(R.id.sign_scan_rnum) ;
        scanAfresh = (Button)view.findViewById(R.id.sign_scan_afresh) ;
        scanOk = (Button)view.findViewById(R.id.sign_scan_ok) ;
        //注册监听器
        scanStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanQycode();
            }
        });
        scanAfresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanLayout.setVisibility(View.GONE);
                scanStart.setVisibility(View.VISIBLE);
            }
        });
        scanOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanLayout.setVisibility(View.GONE);
                scanStart.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getContext(), PermissionAcitivity.class) ;
                startActivity(intent);
            }
        });
        return view;
    }
    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        /**
         * 处理二维码扫描结果
         */
        if (requestCode == 1) {
            //处理扫描结果（在界面上显示）
            switch(requestCode){
                case 1 :
                    try{
                        if (resultCode == getActivity().RESULT_OK){

                            if (null != data) {
                                Bundle bundle = data.getExtras();
                                if (bundle == null) {
                                    return;}
                                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                                    QRcode = bundle.getString(CodeUtils.RESULT_STRING);
                                    //利用正则表达式判断扫描的文字
                                    if (!QRcode.matches("\\d{14}")){
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getContext(),"你扫描的二维码格式错误，请重新扫描!",Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        return;
                                    }
                                    //解析处理后的字符串
                                    SharedPreferences prefs = getContext().getSharedPreferences("data", MODE_PRIVATE) ;
                                    String sno = prefs.getString("registerSno",null) ;
                                    if (sno != null){
                                        Function.sendRQcode(QRcode,sno, new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getContext(),"连接错误，请重新扫描!",Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                               final String result = response.body().string() ;
                                                if (result.equals("00")){
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(getContext(),"你还未选择这门课程！",Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }else if (result.equals("11")){
                                                   getActivity().runOnUiThread(new Runnable() {
                                                       @Override
                                                       public void run() {
                                                           Toast.makeText(getContext(),"二维码过期，请注意扫描二维码的时间！" ,Toast.LENGTH_SHORT).show();
                                                       }
                                                   });
                                                }else if (result.equals("22")){
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(getContext(),"你已经完成签到或者你已经完成请假！",Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }else {
                                                    final String name[] = result.split("\\|") ;
                                                   // Log.d("Main",name[1]) ;
                                                   // Log.d("Main",name[0]) ;
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            //修改UI组件
                                                            scanStart.setVisibility(View.GONE);
                                                            scanLayout.setVisibility(View.VISIBLE);

                                                            scanCno.setText(QRcode.substring(0,6));
                                                            scanCname.setText(name[1]);
                                                            scanTname.setText(name[0]);
                                                            scanRweek.setText(intToString(QRcode.substring(6,8),"week"));
                                                            scanRday.setText(intToString(QRcode.substring(8,9),"day"));
                                                            scanRnum.setText(intToString(QRcode.substring(9,10),"num"));

                                                            SharedPreferences.Editor editor = getContext().getSharedPreferences("rdata",MODE_PRIVATE).edit() ;
                                                            editor.putString("cno",QRcode.substring(0,6)) ;
                                                            editor.putString("week",QRcode.substring(6,8)) ;
                                                            editor.putString("day",QRcode.substring(8,9)) ;
                                                            editor.putString("num",QRcode.substring(9,10)) ;
                                                            editor.apply();
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getContext(),"未登录账号，或者缓存被清除！",Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }

                                } else if (bundle.getInt(CodeUtils.RESULT_TYPE)
                                        == CodeUtils.RESULT_FAILED) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getContext(),
                                                    "解析二维码失败", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break ;
                default:break ;

            }
        }
    }
    /**
     * 扫描二维码
     */
    private void scanQycode(){
        Intent intent = new Intent(getActivity(),CaptureActivity.class);
        startActivityForResult(intent,1);
    }
    /**
     * 将周次、星期、节次的数字信息解析成字符型
     */
    private String intToString(String s ,String type){
        String reslut = null ;
        if (type.equals("week")){
            reslut = "第 " + s + " 周" ;
        }else if (type.equals("day")){
            switch (Integer.parseInt(s)){
                case 0:
                    reslut = "星期日" ;
                    break;
                case 1:
                    reslut = "星期一" ;
                    break;
                case 2:
                    reslut = "星期二" ;
                    break;
                case 3:
                    reslut = "星期三" ;
                    break;
                case 4:
                    reslut = "星期四" ;
                    break;
                case 5:
                    reslut = "星期五" ;
                    break;
                case 6:
                    reslut = "星期六" ;
                    break;
                default:break;
            }
        }else if (type.equals("num")){
            switch (Integer.parseInt(s)){
                case 1:
                    reslut = "第一大节" ;
                    break;
                case 2:
                    reslut = "第二大节" ;
                    break;
                case 3:
                    reslut = "第三大节" ;
                    break;
                case 4:
                    reslut = "第四大节" ;
                    break;
                default:break;
            }
        }

        return reslut ;
    }

}
