package com.example.sign;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.example.sign.Util.MyApplication;
import com.example.sign.Util.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ManagerFace extends AppCompatActivity  {
    static Activity instace ;
    //private final String TAG = this.getClass().toString();
    private final String TAG = "TAG" ;
    private static final int REQUEST_CODE_IMAGE_CAMERA = 1;
    private static final int REQUEST_CODE_IMAGE_OP = 2;
    private static final int REQUEST_CODE_OP = 3;

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        instace = this ;
        detecterFace();

    }

    /* (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE_OP && resultCode == RESULT_OK) {
            Uri mPath = data.getData();
            String file = getPath(mPath);
            Bitmap bmp = Application.decodeImage(file);
            if (bmp == null || bmp.getWidth() <= 0 || bmp.getHeight() <= 0 ) {
                Log.e(TAG, "error");
            } else {
                Log.i(TAG, "bmp [" + bmp.getWidth() + "," + bmp.getHeight());
            }
            startRegister(bmp, file);
        } else if (requestCode == REQUEST_CODE_OP) {
            Log.i(TAG, "RESULT =" + resultCode);
            if (data == null) {
                return;
            }
            Bundle bundle = data.getExtras();
            String path = bundle.getString("imagePath");
            Log.i(TAG, "path="+path);
            detecterFace();
        } else if (requestCode == REQUEST_CODE_IMAGE_CAMERA && resultCode == RESULT_OK) {
            Uri mPath = ((Application)(getApplicationContext())).getCaptureImage();
            String file = getPath(mPath);
            Bitmap bmp = Application.decodeImage(file);
            startRegister(bmp, file);
        }else {
            ManagerFace.instace.finish();
        }
    }

    /**
     * 人脸检测
     * @param
     */
    private void detecterFace(){
        if( ((Application)getApplicationContext()).mFaceDB.mRegister.isEmpty() ) {
            Toast.makeText(this, "没有注册人脸，请先注册！", Toast.LENGTH_SHORT).show();
           // gainPicture();
            registerFace();
        } else {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("请选择相机")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setItems(new String[]{"后置相机", "前置相机"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startDetector(which);
                        }
                    })
                    .show();
        }
    }
    /**
     * 从数据库获取图片
     */
    private void gainPicture(){
         gainFacePicture(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                            Toast.makeText(getApplicationContext(),"服务器故障！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string() ;
                    if (TextUtils.isEmpty(result) || result.equals("000")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                                Toast.makeText(getApplicationContext(),"服务器故障！",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else if (result.equals("false")){
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext()) ;
                        alertDialog.setTitle("警告！") ;
                        alertDialog.setMessage("尚未注册人脸，是否现立即注册！") ;
                        alertDialog.setCancelable(false) ;
                        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                registerFace();
                            }
                        }) ;
                        alertDialog.setNegativeButton("CANCEL",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        }) ;
                        alertDialog.show();
                    }else {
                        //获取到图片******************************************************************
                        try {
                            byte face[] =   result.getBytes() ;
                            AFR_FSDKFace facePicture = new AFR_FSDKFace(face) ;
                            ((Application)ManagerFace.this.getApplicationContext()).mFaceDB.addFace("默认", facePicture);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        detecterFace();
                    }
                }
            });
    }
    /**
     * 人脸注册
     * @param
     */
    private void registerFace(){
        new AlertDialog.Builder(this)
                .setTitle("请选择注册方式")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setItems(new String[]{"打开图片", "拍摄照片"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 1:
                                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                                ContentValues values = new ContentValues(1);
                                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                ((Application)(getApplicationContext())).setCaptureImage(uri);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                                startActivityForResult(intent, REQUEST_CODE_IMAGE_CAMERA);
                                break;
                            case 0:
                                Intent getImageByalbum = new Intent(Intent.ACTION_GET_CONTENT);
                                getImageByalbum.addCategory(Intent.CATEGORY_OPENABLE);
                                getImageByalbum.setType("image/jpeg");
                                startActivityForResult(getImageByalbum, REQUEST_CODE_IMAGE_OP);
                                break;
                            default:;
                        }
                    }
                })
                .show();
    }
    /**
     * @param uri
     * @return
     */
    private String getPath(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(this, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                    // TODO handle non-primary volumes
                } else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(getApplicationContext(), contentUri, null, null);
                } else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[] {
                            split[1]
                    };

                    return getDataColumn(this, contentUri, selection, selectionArgs);
                }
            }
        }
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor actualimagecursor = this.getContentResolver().query(uri, proj, null, null, null);
        int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        actualimagecursor.moveToFirst();
        String img_path = actualimagecursor.getString(actual_image_column_index);
        String end = img_path.substring(img_path.length() - 4);
        if (0 != end.compareToIgnoreCase(".jpg") && 0 != end.compareToIgnoreCase(".png")) {
            return null;
        }
        return img_path;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param mBitmap
     */
    private void startRegister(Bitmap mBitmap, String file) {
        Intent it = new Intent(ManagerFace.this, RegisterActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("imagePath", file);
        it.putExtras(bundle);
        startActivityForResult(it, REQUEST_CODE_OP);
    }

    private void startDetector(int camera) {
        finish();
        Intent it = new Intent(ManagerFace.this, DetecterActivity.class);
        it.putExtra("Camera", camera);
        startActivityForResult(it, REQUEST_CODE_OP);
    }

    /**
     * 发送请求获取数据库中的图片
     */
    public void gainFacePicture(okhttp3.Callback callback){
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("data", Context.MODE_PRIVATE) ;
        String sno = prefs.getString("registerSno",null) ;
        OkHttpClient client = new OkHttpClient() ;
        RequestBody requestBody = new FormBody.Builder().add("sno",sno).build() ;
        Request request = new Request.Builder().url(Util.serviceUrl + "android/gainFacePicturer").post(requestBody).build();
        client.newCall(request).enqueue(callback);
    }
}