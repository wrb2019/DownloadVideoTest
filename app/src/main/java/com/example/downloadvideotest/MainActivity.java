package com.example.downloadvideotest;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final int WRITE_PERMISSION_CODE = 1000;
    //文件下载链接
    //private String url = "https://flv2.bn.netease.com/videolib1/1811/26/OqJAZ893T/HD/OqJAZ893T-mobile.mp4";//汽车视频
    //private String url = "https://222.52.51.133:8082/pic/2019-03/2019-03-18/46cff43cc4444ba7b76bf93d9844c81b.avi";//测试服务器上能播放的视频
    //private String url = "https://test.szjyxtech.com/pic/2019-03/2019-03-18/46cff43cc4444ba7b76bf93d9844c81b.avi";//测试服务器上不能播放的视频
    private String url = "http://wry201901.oss-cn-shenzhen.aliyuncs.com/inOrOutVideo/2019-02/2019-02-21/a8619daf775944fe9bf64689bb9dda9a.avi";//oss上能播放的视频
    //private String url = "http://wry201901.oss-cn-shenzhen.aliyuncs.com/inOrOutVideo/2019-02/2019-02-21/eae4a983b2e7430bb7cfa11bbf03a49b.avi";//oss上能播放的视频
    //private String url = "http://wry201901.oss-cn-shenzhen.aliyuncs.com/inOrOutVideo/2019-02/2019-02-21/0a7be14a4b4545478458a54c7dc23ed4.avi";//oss上能播放的视频
    //private String url = "http://wrb201901.oss-cn-hangzhou.aliyuncs.com/%E6%A1%82%E6%9E%97%E9%87%91%E9%93%B1%E6%98%9F.txt";//txt文件
    //private String url = "http://wrb201901.oss-cn-hangzhou.aliyuncs.com/%E5%B7%A5%E4%BD%9C%E6%80%BB%E7%BB%93.docx";//txt文件
    //private String url = "http://wrb201901.oss-cn-hangzhou.aliyuncs.com/毅峰路收费图.jpg";//图片文件
    //private String url = "http://vjs.zencdn.net/v/oceans.mp4";//videoJS视频
    //private String url = "F:/upload/46cff43cc4444ba7b76bf93d9844c81b.avi";//本地视频

    private Context mContext;

    private Button btnStartDownLoad, btnPauseDownLoad, btnCancelDownLoad;

    private DownLoadService.DownLoadBinder downLoadBinder;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e(TAG,"初始化 serviceConnection");
            downLoadBinder = (DownLoadService.DownLoadBinder) iBinder;
            Log.e(TAG,"***************************************************************************************************");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        baseDataInit();
        bindViews();//跟前端按钮对上
        viewsAddListener();
        viewsDataInit();
    }

    private void baseDataInit() {
        mContext = this;
    }

    private void bindViews() {
        btnStartDownLoad = findViewById(R.id.Main_btnStartDownLoad);
        btnPauseDownLoad = findViewById(R.id.Main_btnPauseDownLoad);
        btnCancelDownLoad = findViewById(R.id.Main_btnCancelDownLoad);
    }

    private void viewsAddListener() {
        btnStartDownLoad.setOnClickListener(this);
        btnPauseDownLoad.setOnClickListener(this);
        btnCancelDownLoad.setOnClickListener(this);
    }

    private void viewsDataInit() {
        checkUserPermission();
        Intent intent = new Intent(mContext, DownLoadService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View view) {
        if (downLoadBinder == null) {
            Toast.makeText(mContext, "下载服务创建失败！", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.e(TAG,"view.getId()="+view.getId());
        switch (view.getId()) {
            case R.id.Main_btnStartDownLoad: {
                downLoadBinder.startDownLoad(url);
                break;
            }
            case R.id.Main_btnPauseDownLoad: {
                downLoadBinder.pauseDownLoad();
                break;
            }
            case R.id.Main_btnCancelDownLoad: {
                downLoadBinder.cancelDownLoad();
                break;
            }
            default:break;
        }
    }

    /**
     * 检查用户权限
     */
    private void checkUserPermission() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mContext, "拒绝权限将无法开启下载服务", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:break;
        }
    }
}
